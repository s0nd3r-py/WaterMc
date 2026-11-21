package me.s0nd3r.watermc.bootstrap;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/** Переход с Paper/Spigot/Purpur/Leaf: файлы, каталоги, конфиги других ядер и миры. */
class ServerUpgradeTest {

    @TempDir
    Path root;

    @AfterEach
    void clearProperty() {
        System.clearProperty(ServerLayout.MOVE_WORLDS_PROPERTY);
    }

    private void write(final Path path, final String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private void world(final String name) throws IOException {
        this.write(this.root.resolve(name).resolve("level.dat"), "level:" + name);
        this.write(this.root.resolve(name).resolve("region").resolve("r.0.0.mca"), "region:" + name);
    }

    private List<String> rootEntries() throws IOException {
        try (Stream<Path> stream = Files.list(this.root)) {
            return stream.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }

    @Test
    void paperRemapCacheBecomesWaterRemapCache() throws IOException {
        this.write(this.root.resolve("plugins/.paper-remapped/index.json"), "{}");
        this.write(this.root.resolve("plugins/.paper-remapped/Essentials.jar"), "jar");
        this.write(this.root.resolve("plugins/Essentials.jar"), "original");

        final ServerLayout.MigrationReport report = new ServerLayout(this.root).migrate();

        assertTrue(report.conflicts().isEmpty(), report.conflicts().toString());
        assertEquals("jar", Files.readString(this.root.resolve("plugins/.water-remapped/Essentials.jar")));
        assertEquals("{}", Files.readString(this.root.resolve("plugins/.water-remapped/index.json")));
        assertEquals("original", Files.readString(this.root.resolve("plugins/Essentials.jar")));
        assertFalse(Files.exists(this.root.resolve("plugins/.paper-remapped")));
    }

    @Test
    void serviceFilesDirectoriesAndForeignConfigsLeaveTheRoot() throws IOException {
        this.write(this.root.resolve(".console_history"), "say hi");
        this.write(this.root.resolve("map-color-cache.dat"), "cache");
        this.write(this.root.resolve("debug").resolve("listeners.txt"), "dump");
        this.write(this.root.resolve("crash-reports").resolve("crash-1.txt"), "crash");
        this.write(this.root.resolve("codeofconduct").resolve("en_us.txt"), "be nice");
        this.write(this.root.resolve("tuinity.yml"), "tuinity: true");
        this.write(this.root.resolve("leaves.yml"), "leaves: true");
        this.write(this.root.resolve("paper.yml-README.txt"), "moved");
        this.write(this.root.resolve("server.properties"), "motd=x");

        final ServerLayout layout = new ServerLayout(this.root);
        final ServerLayout.MigrationReport report = layout.migrate();

        assertTrue(report.conflicts().isEmpty(), report.conflicts().toString());
        assertEquals("say hi", Files.readString(this.root.resolve("data/console_history")));
        assertEquals("cache", Files.readString(this.root.resolve("data/map-color-cache.dat")));
        assertEquals("dump", Files.readString(this.root.resolve("logs/debug/listeners.txt")));
        assertEquals("crash", Files.readString(this.root.resolve("logs/crash-reports/crash-1.txt")));
        assertEquals("be nice", Files.readString(this.root.resolve("config/codeofconduct/en_us.txt")));
        assertEquals("tuinity: true", Files.readString(this.root.resolve("config/legacy/tuinity.yml")));
        assertEquals("leaves: true", Files.readString(this.root.resolve("config/legacy/leaves.yml")));
        assertEquals(List.of("config", "data", "logs", "server.properties"), this.rootEntries());
    }

    @Test
    void directoryMergeNeverOverwrites() throws IOException {
        this.write(this.root.resolve("logs/crash-reports/a.txt"), "new");
        this.write(this.root.resolve("crash-reports/a.txt"), "old");
        this.write(this.root.resolve("crash-reports/b.txt"), "old-b");

        final ServerLayout.MigrationReport report = new ServerLayout(this.root).migrate();

        assertEquals("new", Files.readString(this.root.resolve("logs/crash-reports/a.txt")));
        assertEquals("old-b", Files.readString(this.root.resolve("logs/crash-reports/b.txt")));
        assertEquals("old", Files.readString(this.root.resolve("crash-reports/a.txt")), "the conflicting file stays where it was");
        assertEquals(1, report.conflicts().size(), report.conflicts().toString());
    }

    @Test
    void rootWorldsMoveIntoWorldsDirectory() throws IOException {
        this.world("world");
        this.world("world_nether");
        this.world("world_the_end");
        this.world("creative"); // мир Multiverse
        Files.createDirectories(this.root.resolve("not-a-world"));
        this.write(this.root.resolve("plugins/Something/level.dat"), "plugin data, not a world");

        final ServerLayout layout = new ServerLayout(this.root);
        assertEquals(this.root, layout.defaultWorldContainer("world"), "before the upgrade the root is the container");
        final ServerLayout.WorldMove move = layout.moveRootWorlds();

        assertNull(move.skippedReason());
        assertEquals(4, move.moved().size(), move.moved().toString());
        for (final String name : List.of("world", "world_nether", "world_the_end", "creative")) {
            assertEquals("level:" + name, Files.readString(this.root.resolve("worlds").resolve(name).resolve("level.dat")));
            assertEquals("region:" + name, Files.readString(this.root.resolve("worlds").resolve(name).resolve("region/r.0.0.mca")));
            assertFalse(Files.exists(this.root.resolve(name)));
        }
        assertTrue(Files.isDirectory(this.root.resolve("not-a-world")));
        assertTrue(Files.isRegularFile(this.root.resolve("plugins/Something/level.dat")));
        assertEquals(this.root.resolve("worlds"), layout.defaultWorldContainer("world"));
        assertFalse(layout.moveRootWorlds().happened(), "second run is a no-op");
    }

    @Test
    void anyConflictKeepsAllWorldsInPlace() throws IOException {
        this.world("world");
        this.world("world_nether");
        this.write(this.root.resolve("worlds/world_nether/level.dat"), "already here");

        final ServerLayout.WorldMove move = new ServerLayout(this.root).moveRootWorlds();

        assertFalse(move.happened());
        assertNotNull(move.skippedReason());
        assertTrue(Files.isRegularFile(this.root.resolve("world/level.dat")));
        assertTrue(Files.isRegularFile(this.root.resolve("world_nether/level.dat")));
        assertEquals("already here", Files.readString(this.root.resolve("worlds/world_nether/level.dat")));
    }

    @Test
    void lockedWorldIsNotMoved() throws IOException {
        this.world("world");
        final Path lock = this.root.resolve("world/session.lock");
        Files.writeString(lock, "x");
        try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.WRITE); FileLock ignored = channel.lock()) {
            final ServerLayout.WorldMove move = new ServerLayout(this.root).moveRootWorlds();
            assertFalse(move.happened());
            assertTrue(move.skippedReason().contains("world"), move.skippedReason());
        }
        assertTrue(Files.isRegularFile(this.root.resolve("world/level.dat")));
    }

    @Test
    void movingWorldsCanBeDisabled() throws IOException {
        this.world("world");
        System.setProperty(ServerLayout.MOVE_WORLDS_PROPERTY, "false");
        final ServerLayout.WorldMove move = new ServerLayout(this.root).moveRootWorlds();
        assertFalse(move.happened());
        assertTrue(move.skippedReason().contains(ServerLayout.MOVE_WORLDS_PROPERTY));
        assertTrue(Files.isRegularFile(this.root.resolve("world/level.dat")));
    }

    @Test
    void failureHalfwayRollsBackAlreadyMovedWorlds() throws IOException {
        // Windows не даёт переместить каталог, в котором открыт файл: так моделируется сбой посреди переноса.
        Assumptions.assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("win"), "needs Windows file locking semantics");
        this.world("a_world");
        this.world("b_world");
        try (FileChannel open = FileChannel.open(this.root.resolve("b_world/region/r.0.0.mca"), StandardOpenOption.READ)) {
            final ServerLayout.WorldMove move = new ServerLayout(this.root).moveRootWorlds();
            Assumptions.assumeFalse(move.happened(), "the file system allowed moving a directory with an open file");
            assertTrue(move.skippedReason().contains("возвращены"), move.skippedReason());
        }
        assertEquals("level:a_world", Files.readString(this.root.resolve("a_world/level.dat")), "the first world was moved back");
        assertEquals("level:b_world", Files.readString(this.root.resolve("b_world/level.dat")));
        assertFalse(Files.exists(this.root.resolve("worlds/a_world")));
    }

    @Test
    void upgradeReportIsWritten() throws IOException {
        final Path report = new ServerLayout(this.root).writeUpgradeReport(List.of("line one", "line two"));
        assertEquals(this.root.resolve("data"), report.getParent());
        assertEquals(List.of("line one", "line two"), Files.readAllLines(report));
    }
}
