package me.s0nd3r.watermc.pluginremap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * SHA-256 файлов плагинов с кэшем по размеру и времени изменения.
 *
 * <p>Индекс переотображённых плагинов ищет кэш по SHA-256 исходного jar. Раньше хэш считался при каждом запуске
 * для каждого плагина, и часто по нескольку раз за запуск: файл читался целиком. Теперь хэш считается один раз
 * и хранится в {@code hashes.tsv} рядом с индексом. Пока у файла те же размер и время изменения (с точностью
 * до наносекунд, если её даёт файловая система), файл не читается.</p>
 *
 * <p>Если файл подменили, сохранив и размер, и время изменения, кэш этого не заметит; так же устроены
 * инкрементальные проверки в Gradle и rsync. Отключение: {@code -Dwatermc.remapper.trust-timestamps=false}
 * (хэш считается всегда, но не больше одного раза за запуск).</p>
 *
 * <p>Формат хэша совпадает с прежним (шестнадцатеричный, верхний регистр), поэтому старые индексы остаются
 * действительными.</p>
 */
public final class JarHashCache {

    private static final boolean TRUST_TIMESTAMPS = Boolean.parseBoolean(System.getProperty("watermc.remapper.trust-timestamps", "true"));
    private static final String FILE_NAME = "hashes.tsv";

    record Stamp(long size, long modifiedNanos, String hash) {
    }

    private final Path file;
    private final Map<String, Stamp> stamps = new ConcurrentHashMap<>();
    /** Хэши, уже проверенные в этом запуске (даже без доверия к времени изменения файл читается один раз). */
    private final Map<String, Stamp> thisRun = new ConcurrentHashMap<>();
    private volatile boolean dirty;
    private int computed;

    private JarHashCache(final Path file) {
        this.file = file;
    }

    /** Загрузить кэш из каталога {@code dir}; повреждённый или отсутствующий файл — пустой кэш. */
    public static JarHashCache load(final Path dir) {
        final JarHashCache cache = new JarHashCache(dir.resolve(FILE_NAME));
        if (Files.isRegularFile(cache.file)) {
            try (BufferedReader reader = Files.newBufferedReader(cache.file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String[] parts = line.split("\t");
                    if (parts.length == 4) {
                        try {
                            cache.stamps.put(parts[0], new Stamp(Long.parseLong(parts[1]), Long.parseLong(parts[2]), parts[3]));
                        } catch (final NumberFormatException ignored) {
                            // строка повреждена — файл просто будет перехэширован
                        }
                    }
                }
            } catch (final IOException ignored) {
                cache.stamps.clear();
            }
        }
        return cache;
    }

    /** SHA-256 файла в формате Paper (шестнадцатеричный, верхний регистр). */
    public String sha256(final Path jar) {
        final String key = jar.toAbsolutePath().normalize().toString();
        final BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(jar, BasicFileAttributes.class);
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to read attributes of '" + jar + "'", ex);
        }
        if (!attributes.isRegularFile()) {
            throw new IllegalArgumentException("'" + jar + "' is not a regular file!");
        }
        final long size = attributes.size();
        final long modified = attributes.lastModifiedTime().to(TimeUnit.NANOSECONDS);
        final Stamp seen = this.thisRun.get(key);
        if (seen != null && seen.size == size && seen.modifiedNanos == modified) {
            return seen.hash;
        }
        final Stamp stored = this.stamps.get(key);
        if (TRUST_TIMESTAMPS && stored != null && stored.size == size && stored.modifiedNanos == modified) {
            this.thisRun.put(key, stored);
            return stored.hash;
        }
        final Stamp fresh = new Stamp(size, modified, hashFile(jar));
        synchronized (this) {
            this.computed++;
        }
        this.stamps.put(key, fresh);
        this.thisRun.put(key, fresh);
        this.dirty = true;
        return fresh.hash;
    }

    /** Сколько файлов было прочитано и хэшировано (для тестов и отладки). */
    public synchronized int computedHashes() {
        return this.computed;
    }

    /** Сохранить кэш, убрав записи о файлах, которых больше нет. */
    public synchronized void save() {
        final int before = this.stamps.size();
        this.stamps.keySet().removeIf(path -> !Files.isRegularFile(Path.of(path)));
        if (!this.dirty && this.stamps.size() == before) {
            return;
        }
        try {
            Files.createDirectories(this.file.getParent());
            final Path tmp = this.file.resolveSibling(FILE_NAME + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                for (final Map.Entry<String, Stamp> entry : this.stamps.entrySet()) {
                    final Stamp stamp = entry.getValue();
                    writer.write(entry.getKey() + "\t" + stamp.size + "\t" + stamp.modifiedNanos + "\t" + stamp.hash + "\n");
                }
            }
            Files.move(tmp, this.file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            this.dirty = false;
        } catch (final IOException ignored) {
            // кэш хэшей — только ускорение; без него всё работает
        }
    }

    static String hashFile(final Path jar) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        final byte[] buffer = new byte[1 << 16];
        try (InputStream in = Files.newInputStream(jar)) {
            int read;
            while ((read = in.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to take hash of file '" + jar + "'", ex);
        }
        return HexFormat.of().withUpperCase().formatHex(digest.digest());
    }
}
