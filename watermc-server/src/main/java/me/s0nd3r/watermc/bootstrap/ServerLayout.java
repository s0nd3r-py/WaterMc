package me.s0nd3r.watermc.bootstrap;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Раскладка каталога сервера WaterMc.
 *
 * <pre>
 * &lt;root&gt;/
 *   watermc-1.21.11.jar, server.properties, eula.txt
 *   config/   bukkit.yml, spigot.yml, purpur.yml, commands.yml, help.yml, permissions.yml,
 *             paper-global.yml, paper-world-defaults.yml, watermc.yml, watermc-world.yml
 *   data/     ops.json, whitelist.json, banned-players.json, banned-ips.json, usercache.json, version_history.json
 *   worlds/   миры (если в корне нет существующего мира и путь не задан явно)
 *   logs/, plugins/, libraries/
 * </pre>
 *
 * Миграция никогда не перезаписывает файл, уже существующий в новом каталоге: старый файл
 * в корне в таком случае остаётся на месте, а конфликт попадает в {@link MigrationReport#conflicts()}.
 */
public final class ServerLayout {

    public static final String CONFIG_DIR = "config";
    public static final String DATA_DIR = "data";
    public static final String WORLDS_DIR = "worlds";

    public static final String WATERMC_GLOBAL_FILE = "watermc.yml";
    public static final String WATERMC_WORLD_FILE = "watermc-world.yml";

    /** Конфигурации ядра, которые раньше создавались в корне сервера. */
    public static final List<String> ROOT_CONFIG_FILES = List.of(
        "bukkit.yml", "spigot.yml", "purpur.yml", "commands.yml", "help.yml", "permissions.yml"
    );

    /** Служебные JSON-файлы, которые раньше создавались в корне сервера. */
    public static final List<String> ROOT_DATA_FILES = List.of(
        "ops.json", "whitelist.json", "banned-players.json", "banned-ips.json", "usercache.json", "version_history.json"
    );

    /** Унаследованные имена конфигураций форков в config/ и их новые имена. */
    public static final List<Rename> LEGACY_CONFIG_RENAMES = List.of(
        new Rename("leaf-global.yml", WATERMC_GLOBAL_FILE),
        new Rename("gale-world-defaults.yml", WATERMC_WORLD_FILE)
    );

    /** Имя прежнего глобального конфига Gale; его значения переносятся в watermc.yml. */
    public static final String LEGACY_GALE_GLOBAL_FILE = "gale-global.yml";
    /** Имя прежнего конфига Gale внутри папки мира. */
    public static final String LEGACY_GALE_WORLD_FILE = "gale-world.yml";
    /** Каталог, куда убираются полностью перенесённые устаревшие файлы. */
    public static final String LEGACY_BACKUP_DIR = "legacy";
    public static final String LOGS_DIR = "logs";

    /** Служебные файлы, которые ядро раньше писало в корень (путь относительно корня → новый путь). */
    public static final List<Rename> ROOT_FILE_RELOCATIONS = List.of(
        new Rename(".console_history", DATA_DIR + "/console_history"),
        new Rename("map-color-cache.dat", DATA_DIR + "/map-color-cache.dat")
    );

    /** Служебные каталоги в корне и их новое место. */
    public static final List<Rename> ROOT_DIR_RELOCATIONS = List.of(
        new Rename("codeofconduct", CONFIG_DIR + "/codeofconduct"),
        new Rename("syncmatics", DATA_DIR + "/syncmatics"),
        new Rename("replay", DATA_DIR + "/replay"),
        new Rename("debug", LOGS_DIR + "/debug"),
        new Rename("dumps", LOGS_DIR + "/dumps"),
        new Rename("crash-reports", LOGS_DIR + "/crash-reports"),
        new Rename("plugins/.paper-remapped", "plugins/.water-remapped")
    );

    /**
     * Конфиги других ядер, настройки которых WaterMc не читает (или уже перенёс). Они не удаляются,
     * а убираются в config/legacy/, чтобы корень был чистым, а значения оставались под рукой.
     */
    public static final List<String> FOREIGN_CONFIGS = List.of(
        "tuinity.yml", "airplane.yml", "leaves.yml", "mirai.yml", "patina.yml", "petal.yml",
        "divinemc.yml", "sparklypaper.yml", "kaiiju.yml", "canvas.yml", "paper.yml-README.txt"
    );

    /** Каталоги в корне, которые никогда не считаются мирами. */
    public static final List<String> RESERVED_ROOT_DIRS = List.of(
        CONFIG_DIR, DATA_DIR, WORLDS_DIR, LOGS_DIR, "plugins", "libraries", "versions", "cache", "scripts", "crash-reports", "debug", "dumps"
    );

    /** Отключить перенос миров из корня: {@code -Dwatermc.upgrade.move-worlds=false}. */
    public static final String MOVE_WORLDS_PROPERTY = "watermc.upgrade.move-worlds";

    private static volatile ServerLayout current = new ServerLayout(Path.of(""));

    private final Path root;

    public ServerLayout(final Path root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    public static ServerLayout current() {
        return current;
    }

    public static void install(final ServerLayout layout) {
        current = Objects.requireNonNull(layout, "layout");
    }

    public Path root() {
        return this.root;
    }

    public Path configDir() {
        return this.root.resolve(CONFIG_DIR);
    }

    public Path dataDir() {
        return this.root.resolve(DATA_DIR);
    }

    public Path configFile(final String name) {
        return this.configDir().resolve(name);
    }

    public Path dataFile(final String name) {
        return this.dataDir().resolve(name);
    }

    /** {@link File}-вариант для унаследованного кода и Bukkit API, принимающих {@code java.io.File}. */
    public File configFileLegacy(final String name) {
        return this.configFile(name).toFile();
    }

    public File dataFileLegacy(final String name) {
        return this.dataFile(name).toFile();
    }

    /**
     * Путь к конфигурации, указанной относительным именем (например {@code settings.permissions-file}).
     * Существующий файл по прежнему пути уважается, иначе путь разрешается внутри config/.
     */
    public Path resolveConfigReference(final String configured) {
        final Path path = Path.of(configured);
        if (path.isAbsolute()) {
            return path;
        }
        final Path legacy = this.root.resolve(path);
        if (Files.exists(legacy, LinkOption.NOFOLLOW_LINKS)) {
            return legacy;
        }
        return this.configDir().resolve(path);
    }

    /**
     * Каталог миров по умолчанию, когда он не задан через {@code --world-dir} или {@code settings.world-container}.
     * Существующий сервер с миром в корне продолжает использовать корень, чтобы миры не «пропали».
     */
    public Path defaultWorldContainer(final String levelName) {
        if (this.hasLegacyRootWorld(levelName)) {
            return this.root;
        }
        return this.root.resolve(WORLDS_DIR);
    }

    public boolean hasLegacyRootWorld(final String levelName) {
        final String name = levelName == null || levelName.isBlank() ? "world" : levelName;
        return Files.isDirectory(this.root.resolve(name)) || Files.isDirectory(this.root.resolve("world"));
    }

    /** Создаёт config/ и data/. Каталог миров создаётся загрузчиком мира по мере необходимости. */
    public void createDirectories() throws IOException {
        Files.createDirectories(this.configDir());
        Files.createDirectories(this.dataDir());
    }

    /** Переносит конфиги и JSON из корня в config/ и data/, а также переименовывает конфиги форков. */
    public MigrationReport migrate() throws IOException {
        this.createDirectories();
        final List<String> moved = new ArrayList<>();
        final List<String> conflicts = new ArrayList<>();

        for (final String name : ROOT_CONFIG_FILES) {
            moveIfAbsent(this.root.resolve(name), this.configFile(name), moved, conflicts);
        }
        for (final String name : ROOT_DATA_FILES) {
            moveIfAbsent(this.root.resolve(name), this.dataFile(name), moved, conflicts);
        }
        for (final Rename rename : LEGACY_CONFIG_RENAMES) {
            // Сначала прежнее имя в config/, затем — на случай очень старого сервера — в корне.
            moveIfAbsent(this.configFile(rename.from()), this.configFile(rename.to()), moved, conflicts);
            moveIfAbsent(this.root.resolve(rename.from()), this.configFile(rename.to()), moved, conflicts);
        }
        for (final Rename rename : ROOT_FILE_RELOCATIONS) {
            moveIfAbsent(this.root.resolve(rename.from()), this.root.resolve(rename.to()), moved, conflicts);
        }
        for (final Rename rename : ROOT_DIR_RELOCATIONS) {
            moveDirectoryMerging(this.root.resolve(rename.from()), this.root.resolve(rename.to()), moved, conflicts);
        }
        for (final String name : FOREIGN_CONFIGS) {
            final Path file = this.root.resolve(name);
            if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
                moved.add(file + " -> " + this.archiveLegacyConfig(file) + " (настройки другого ядра, сохранены без изменений)");
            }
        }
        return new MigrationReport(Collections.unmodifiableList(moved), Collections.unmodifiableList(conflicts));
    }

    /**
     * Переносит каталог целиком; если цель уже есть — переносит по одному только отсутствующие в ней элементы,
     * остальные остаются на месте и попадают в конфликты. Ничего не перезаписывается и не удаляется.
     */
    static void moveDirectoryMerging(final Path from, final Path to, final List<String> moved, final List<String> conflicts) throws IOException {
        if (!Files.isDirectory(from, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        if (!Files.exists(to, LinkOption.NOFOLLOW_LINKS)) {
            Files.createDirectories(to.toAbsolutePath().getParent());
            move(from, to);
            moved.add(from + " -> " + to);
            return;
        }
        if (!Files.isDirectory(to, LinkOption.NOFOLLOW_LINKS)) {
            conflicts.add(from + " -> " + to + " (цель не каталог)");
            return;
        }
        final List<Path> entries;
        try (java.util.stream.Stream<Path> stream = Files.list(from)) {
            entries = stream.toList();
        }
        for (final Path entry : entries) {
            final Path target = to.resolve(entry.getFileName().toString());
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                conflicts.add(entry + " -> " + target);
            } else {
                move(entry, target);
                moved.add(entry + " -> " + target);
            }
        }
        try (java.util.stream.Stream<Path> stream = Files.list(from)) {
            if (stream.findAny().isEmpty()) {
                Files.delete(from);
            }
        }
    }

    // ---------- миры ----------

    /** Результат переноса миров: перенесённые миры или причина, по которой перенос не выполнялся. */
    public record WorldMove(List<String> moved, @org.jspecify.annotations.Nullable String skippedReason) {
        public boolean happened() {
            return !this.moved.isEmpty();
        }
    }

    /** Миры в корне: каталоги с level.dat, кроме служебных. */
    public List<Path> rootWorlds() throws IOException {
        final List<Path> worlds = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(this.root)) {
            for (final Path dir : stream.sorted().toList()) {
                final String name = dir.getFileName().toString();
                if (Files.isDirectory(dir, LinkOption.NOFOLLOW_LINKS) && !name.startsWith(".")
                    && !RESERVED_ROOT_DIRS.contains(name) && Files.isRegularFile(dir.resolve("level.dat"))) {
                    worlds.add(dir);
                }
            }
        }
        return worlds;
    }

    /**
     * Переносит все миры из корня в worlds/. Выполняется по принципу «всё или ничего»: если хотя бы один
     * мир нельзя перенести (каталог занят другим процессом, в worlds/ уже есть мир с таким именем, ошибка
     * файловой системы), уже перенесённые миры возвращаются обратно и сервер продолжает работать с корнем.
     */
    public WorldMove moveRootWorlds() throws IOException {
        if (!Boolean.parseBoolean(System.getProperty(MOVE_WORLDS_PROPERTY, "true"))) {
            return new WorldMove(List.of(), "отключено -D" + MOVE_WORLDS_PROPERTY + "=false");
        }
        final List<Path> worlds = this.rootWorlds();
        if (worlds.isEmpty()) {
            return new WorldMove(List.of(), null);
        }
        final Path container = this.root.resolve(WORLDS_DIR);
        for (final Path world : worlds) {
            final Path target = container.resolve(world.getFileName().toString());
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                return new WorldMove(List.of(), "в worlds/ уже есть " + world.getFileName() + " — миры оставлены в корне");
            }
            final String locked = lockedBy(world);
            if (locked != null) {
                return new WorldMove(List.of(), locked);
            }
        }
        Files.createDirectories(container);
        final List<Path[]> done = new ArrayList<>();
        try {
            for (final Path world : worlds) {
                final Path target = container.resolve(world.getFileName().toString());
                move(world, target);
                done.add(new Path[]{world, target});
            }
        } catch (final IOException ex) {
            for (int i = done.size() - 1; i >= 0; i--) {
                try {
                    move(done.get(i)[1], done.get(i)[0]);
                } catch (final IOException rollback) {
                    ex.addSuppressed(rollback);
                }
            }
            return new WorldMove(List.of(), "ошибка переноса (" + ex.getMessage() + "), уже перенесённые миры возвращены в корень");
        }
        final List<String> moved = new ArrayList<>(done.size());
        for (final Path[] pair : done) {
            moved.add(pair[0] + " -> " + pair[1]);
        }
        return new WorldMove(Collections.unmodifiableList(moved), null);
    }

    /** Занят ли мир другим процессом (session.lock удерживается). */
    private static @org.jspecify.annotations.Nullable String lockedBy(final Path world) {
        final Path lock = world.resolve("session.lock");
        if (!Files.isRegularFile(lock)) {
            return null;
        }
        try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(lock, java.nio.file.StandardOpenOption.WRITE)) {
            final java.nio.channels.FileLock fileLock = channel.tryLock();
            if (fileLock == null) {
                return "мир " + world.getFileName() + " открыт другим процессом — миры оставлены в корне";
            }
            fileLock.release();
            return null;
        } catch (final java.nio.channels.OverlappingFileLockException | IOException ex) {
            return "мир " + world.getFileName() + " открыт другим процессом (" + ex.getMessage() + ") — миры оставлены в корне";
        }
    }

    /** Записывает отчёт об апгрейде в data/upgrade-report-*.txt. */
    public Path writeUpgradeReport(final List<String> lines) throws IOException {
        Files.createDirectories(this.dataDir());
        final String stamp = me.s0nd3r.watermc.util.WaterDateFormats.format(me.s0nd3r.watermc.util.WaterDateFormats.BACKUP_STAMP, java.time.Instant.now());
        Path file = this.dataDir().resolve("upgrade-report-" + stamp + ".txt");
        for (int i = 1; Files.exists(file); i++) {
            file = this.dataDir().resolve("upgrade-report-" + stamp + "-" + i + ".txt");
        }
        Files.write(file, lines, java.nio.charset.StandardCharsets.UTF_8);
        return file;
    }

    public Path logsDir() {
        return this.root.resolve(LOGS_DIR);
    }

    /** Переименовывает конфиг Gale внутри папки мира в watermc-world.yml. */
    public static boolean migrateWorldConfig(final Path worldDirectory) throws IOException {
        final List<String> moved = new ArrayList<>(1);
        moveIfAbsent(worldDirectory.resolve(LEGACY_GALE_WORLD_FILE), worldDirectory.resolve(WATERMC_WORLD_FILE), moved, new ArrayList<>(1));
        return !moved.isEmpty();
    }

    /** Убирает полностью перенесённый устаревший файл в config/legacy/, ничего не удаляя. */
    public Path archiveLegacyConfig(final Path legacyFile) throws IOException {
        final Path backupDir = this.configDir().resolve(LEGACY_BACKUP_DIR);
        Files.createDirectories(backupDir);
        Path target = backupDir.resolve(legacyFile.getFileName());
        for (int i = 1; Files.exists(target, LinkOption.NOFOLLOW_LINKS); i++) {
            target = backupDir.resolve(legacyFile.getFileName() + "." + i);
        }
        move(legacyFile, target);
        return target;
    }

    public static void moveIfAbsentPublic(final Path from, final Path to, final List<String> moved) throws IOException {
        moveIfAbsent(from, to, moved, new ArrayList<>(1));
    }

    static void moveIfAbsent(final Path from, final Path to, final List<String> moved, final List<String> conflicts) throws IOException {
        if (!Files.isRegularFile(from, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        if (Files.exists(to, LinkOption.NOFOLLOW_LINKS)) {
            if (!Files.isSameFile(from, to)) {
                conflicts.add(from + " -> " + to);
            }
            return;
        }
        Files.createDirectories(to.toAbsolutePath().getParent());
        move(from, to);
        moved.add(from + " -> " + to);
    }

    private static void move(final Path from, final Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.ATOMIC_MOVE);
        } catch (final AtomicMoveNotSupportedException ignored) {
            Files.move(from, to);
        }
    }

    public record Rename(String from, String to) {
    }

    public record MigrationReport(List<String> moved, List<String> conflicts) {
        public boolean isEmpty() {
            return this.moved.isEmpty() && this.conflicts.isEmpty();
        }
    }
}
