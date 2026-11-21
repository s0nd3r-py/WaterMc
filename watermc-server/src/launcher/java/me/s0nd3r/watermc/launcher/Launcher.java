package me.s0nd3r.watermc.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Загрузчик WaterMc: распаковывает сервер и библиотеки из jar и запускает сервер.
 *
 * <p>Заменяет загрузчик paperclip, унаследованный от Leaf. Формат jar тот же ({@code META-INF/versions.list},
 * {@code META-INF/libraries.list}, {@code META-INF/main-class}); распакованные файлы лежат там же
 * ({@code versions/}, {@code libraries/}), поэтому переход между ядрами ничего не ломает.</p>
 *
 * <p>Отличие — проверка уже распакованных файлов. Прежний загрузчик на каждом запуске перечитывал и хэшировал
 * все ~540 файлов (≈125 МБ): около 3 секунд до старта сервера. WaterMc хранит в
 * {@code libraries/.watermc-launcher} размер, время изменения и SHA-256 каждого проверенного файла.
 * Если размер и время совпадают, а ожидаемый хэш из jar не изменился, файл не читается. Изменённый,
 * повреждённый или отсутствующий файл проверяется и при необходимости распаковывается заново, как раньше.
 * Проверка всех файлов при каждом запуске: {@code -Dwatermc.launcher.verify-all=true}.</p>
 *
 * <p>Поддерживаемые свойства прежнего загрузчика: {@code bundlerRepoDir}, {@code bundlerMainClass},
 * {@code paperclip.patchonly}.</p>
 */
final class Launcher {

    private static final String STAMPS = ".watermc-launcher";
    private static final boolean VERIFY_ALL = Boolean.getBoolean("watermc.launcher.verify-all");

    private Launcher() {
    }

    static void launch(final String[] args) throws Exception {
        final Path repo = Paths.get(System.getProperty("bundlerRepoDir", "")).toAbsolutePath();
        if (repo.toString().contains("!")) {
            System.err.println("WaterMc cannot run in a directory whose path contains '!'. Please rename the folder: " + repo);
            System.exit(1);
        }
        if (Launcher.class.getResource("/META-INF/patches.list") != null) {
            throw new IllegalStateException("This jar contains binary patches, which the WaterMc launcher does not apply");
        }

        final List<Entry> entries = new ArrayList<Entry>();
        entries.addAll(readList("versions"));
        entries.addAll(readList("libraries"));

        final Path stampsFile = repo.resolve("libraries").resolve(STAMPS);
        final Map<String, Stamp> stamps = readStamps(stampsFile);
        final Map<String, Stamp> updated = ensureAll(repo, entries, stamps);
        if (!updated.equals(stamps)) {
            writeStamps(stampsFile, updated);
        }

        if (Boolean.getBoolean("paperclip.patchonly")) {
            System.exit(0);
        }

        final URL[] urls = new URL[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            urls[i] = entries.get(i).target(repo).toUri().toURL();
        }
        final String mainClass = mainClass();
        System.out.println("Starting " + mainClass);
        final URLClassLoader loader = new URLClassLoader(urls, Launcher.class.getClassLoader().getParent());
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Class<?> main = Class.forName(mainClass, true, loader);
                    final Method method = main.getMethod("main", String[].class);
                    method.invoke(null, (Object) args);
                } catch (final java.lang.reflect.InvocationTargetException ex) {
                    sneakyThrow(ex.getCause());
                } catch (final Throwable t) {
                    sneakyThrow(t);
                }
            }
        }, "ServerMain");
        thread.setContextClassLoader(loader);
        thread.start();
    }

    /** Проверить или распаковать все файлы; вернуть отметки для сохранения. */
    static Map<String, Stamp> ensureAll(final Path repo, final List<Entry> entries, final Map<String, Stamp> stamps) throws Exception {
        final Map<String, Stamp> result = new HashMap<String, Stamp>();
        final ExecutorService pool = Executors.newFixedThreadPool(Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors())));
        try {
            final List<Future<Stamp>> futures = new ArrayList<Future<Stamp>>();
            for (final Entry entry : entries) {
                futures.add(pool.submit(new java.util.concurrent.Callable<Stamp>() {
                    @Override
                    public Stamp call() throws Exception {
                        return ensure(repo, entry, stamps.get(entry.key()));
                    }
                }));
            }
            for (int i = 0; i < entries.size(); i++) {
                try {
                    result.put(entries.get(i).key(), futures.get(i).get());
                } catch (final java.util.concurrent.ExecutionException ex) {
                    throw new IOException("Failed to prepare " + entries.get(i).path, ex.getCause());
                }
            }
        } finally {
            pool.shutdownNow();
        }
        return result;
    }

    /** Файл на месте и верен — вернуть его отметку; иначе распаковать из jar. */
    static Stamp ensure(final Path repo, final Entry entry, final Stamp known) throws IOException {
        final Path target = entry.target(repo);
        if (Files.isRegularFile(target)) {
            final BasicFileAttributes attributes = Files.readAttributes(target, BasicFileAttributes.class);
            final long size = attributes.size();
            final long modified = attributes.lastModifiedTime().toMillis();
            if (!VERIFY_ALL && known != null && known.size == size && known.modified == modified && known.sha256.equals(entry.sha256)) {
                return known;
            }
            if (sha256(target).equals(entry.sha256)) {
                return new Stamp(size, modified, entry.sha256);
            }
        }
        extract(entry, target);
        final BasicFileAttributes attributes = Files.readAttributes(target, BasicFileAttributes.class);
        return new Stamp(attributes.size(), attributes.lastModifiedTime().toMillis(), entry.sha256);
    }

    private static void extract(final Entry entry, final Path target) throws IOException {
        final String resource = "/META-INF/" + entry.kind + "/" + entry.path;
        final Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        final Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        final MessageDigest digest = newDigest();
        try (InputStream in = Launcher.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Missing " + resource + " in the server jar");
            }
            try (OutputStream out = Files.newOutputStream(tmp)) {
                final byte[] buffer = new byte[1 << 16];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                    out.write(buffer, 0, read);
                }
            }
        }
        final String actual = hex(digest.digest());
        if (!actual.equals(entry.sha256)) {
            Files.deleteIfExists(tmp);
            throw new IOException("Corrupted " + resource + " in the server jar: expected " + entry.sha256 + ", got " + actual);
        }
        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
    }

    static List<Entry> readList(final String kind) throws IOException {
        final List<Entry> out = new ArrayList<Entry>();
        try (InputStream in = Launcher.class.getResourceAsStream("/META-INF/" + kind + ".list")) {
            if (in == null) {
                return out;
            }
            final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                final String[] parts = line.split("\t");
                if (parts.length != 3) {
                    throw new IOException("Malformed line in " + kind + ".list: " + line);
                }
                out.add(new Entry(kind, parts[0].toLowerCase(java.util.Locale.ROOT), parts[2]));
            }
        }
        return out;
    }

    private static String mainClass() throws IOException {
        final String override = System.getProperty("bundlerMainClass");
        if (override != null && !override.isEmpty()) {
            return override;
        }
        try (InputStream in = Launcher.class.getResourceAsStream("/META-INF/main-class")) {
            if (in == null) {
                throw new IOException("Missing META-INF/main-class");
            }
            return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)).readLine().trim();
        }
    }

    static Map<String, Stamp> readStamps(final Path file) {
        final Map<String, Stamp> stamps = new HashMap<String, Stamp>();
        if (!Files.isRegularFile(file)) {
            return stamps;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] parts = line.split("\t");
                if (parts.length == 4) {
                    try {
                        stamps.put(parts[0], new Stamp(Long.parseLong(parts[1]), Long.parseLong(parts[2]), parts[3]));
                    } catch (final NumberFormatException ignored) {
                        // повреждённая строка — файл будет проверен заново
                    }
                }
            }
        } catch (final IOException ex) {
            stamps.clear();
        }
        return stamps;
    }

    static void writeStamps(final Path file, final Map<String, Stamp> stamps) {
        try {
            Files.createDirectories(file.getParent());
            final Path tmp = file.resolveSibling(STAMPS + ".tmp");
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(tmp), StandardCharsets.UTF_8))) {
                for (final Map.Entry<String, Stamp> e : stamps.entrySet()) {
                    writer.write(e.getKey() + "\t" + e.getValue().size + "\t" + e.getValue().modified + "\t" + e.getValue().sha256 + "\n");
                }
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException ignored) {
            // отметки — только ускорение
        }
    }

    static String sha256(final Path file) throws IOException {
        final MessageDigest digest = newDigest();
        try (InputStream in = Files.newInputStream(file)) {
            final byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }
        return hex(digest.digest());
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String hex(final byte[] bytes) {
        final char[] digits = "0123456789abcdef".toCharArray();
        final char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            out[i * 2] = digits[(bytes[i] >> 4) & 0xF];
            out[i * 2 + 1] = digits[bytes[i] & 0xF];
        }
        return new String(out);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(final Throwable t) throws T {
        throw (T) t;
    }

    static final class Entry {
        final String kind;
        final String sha256;
        final String path;

        Entry(final String kind, final String sha256, final String path) {
            this.kind = kind;
            this.sha256 = sha256;
            this.path = path;
        }

        Path target(final Path repo) {
            return repo.resolve(this.kind).resolve(this.path);
        }

        String key() {
            return this.kind + "/" + this.path;
        }
    }

    static final class Stamp {
        final long size;
        final long modified;
        final String sha256;

        Stamp(final long size, final long modified, final String sha256) {
            this.size = size;
            this.modified = modified;
            this.sha256 = sha256;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Stamp)) {
                return false;
            }
            final Stamp s = (Stamp) o;
            return s.size == this.size && s.modified == this.modified && s.sha256.equals(this.sha256);
        }

        @Override
        public int hashCode() {
            return (int) (this.size * 31 + this.modified) ^ this.sha256.hashCode();
        }
    }
}
