package me.s0nd3r.watermc.bootstrap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ServerLayoutTest {

    @TempDir
    Path root;

    private void write(final Path path, final String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private List<String> rootEntries() throws IOException {
        try (Stream<Path> stream = Files.list(this.root)) {
            return stream.map(p -> p.getFileName().toString()).sorted().toList();
        }
    }

    @Test
    void freshServerGetsCleanConfigAndDataDirectories() throws IOException {
        final ServerLayout layout = new ServerLayout(this.root);
        final ServerLayout.MigrationReport report = layout.migrate();
        assertTrue(report.isEmpty());
        assertTrue(Files.isDirectory(this.root.resolve("config")));
        assertTrue(Files.isDirectory(this.root.resolve("data")));
        assertEquals(List.of("config", "data"), this.rootEntries());
        assertEquals(this.root.resolve("worlds"), layout.defaultWorldContainer("world"));
        assertEquals(this.root.resolve("config").resolve("bukkit.yml"), layout.configFile("bukkit.yml"));
        assertEquals(this.root.resolve("data").resolve("ops.json"), layout.dataFile("ops.json"));
    }

    @Test
    void migratesLegacyRootFilesWithoutLosingContent() throws IOException {
        for (final String name : ServerLayout.ROOT_CONFIG_FILES) {
            this.write(this.root.resolve(name), "config:" + name);
        }
        for (final String name : ServerLayout.ROOT_DATA_FILES) {
            this.write(this.root.resolve(name), "[\"" + name + "\"]");
        }
        this.write(this.root.resolve("config").resolve("leaf-global.yml"), "leaf: true");
        this.write(this.root.resolve("config").resolve("gale-world-defaults.yml"), "gale-world: true");
        this.write(this.root.resolve("server.properties"), "motd=x");
        this.write(this.root.resolve("eula.txt"), "eula=true");

        final ServerLayout layout = new ServerLayout(this.root);
        final ServerLayout.MigrationReport report = layout.migrate();

        assertTrue(report.conflicts().isEmpty(), report.conflicts().toString());
        assertEquals(ServerLayout.ROOT_CONFIG_FILES.size() + ServerLayout.ROOT_DATA_FILES.size() + 2, report.moved().size());
        for (final String name : ServerLayout.ROOT_CONFIG_FILES) {
            assertEquals("config:" + name, Files.readString(layout.configFile(name)));
            assertFalse(Files.exists(this.root.resolve(name)));
        }
        for (final String name : ServerLayout.ROOT_DATA_FILES) {
            assertEquals("[\"" + name + "\"]", Files.readString(layout.dataFile(name)));
        }
        assertEquals("leaf: true", Files.readString(layout.configFile("watermc.yml")));
        assertEquals("gale-world: true", Files.readString(layout.configFile("watermc-world.yml")));
        assertFalse(Files.exists(layout.configFile("leaf-global.yml")));
        assertEquals(List.of("config", "data", "eula.txt", "server.properties"), this.rootEntries());

        // Повторный запуск идемпотентен.
        assertTrue(layout.migrate().isEmpty());
    }

    @Test
    void neverOverwritesExistingFilesInNewLocation() throws IOException {
        this.write(this.root.resolve("ops.json"), "old");
        this.write(this.root.resolve("data").resolve("ops.json"), "new");
        this.write(this.root.resolve("config").resolve("leaf-global.yml"), "leaf");
        this.write(this.root.resolve("config").resolve("watermc.yml"), "water");

        final ServerLayout.MigrationReport report = new ServerLayout(this.root).migrate();

        assertEquals(2, report.conflicts().size(), report.conflicts().toString());
        assertEquals("new", Files.readString(this.root.resolve("data").resolve("ops.json")));
        assertEquals("old", Files.readString(this.root.resolve("ops.json")), "the conflicting legacy file stays in place");
        assertEquals("water", Files.readString(this.root.resolve("config").resolve("watermc.yml")));
        assertEquals("leaf", Files.readString(this.root.resolve("config").resolve("leaf-global.yml")));
    }

    @Test
    void existingRootWorldKeepsLegacyWorldContainer() throws IOException {
        Files.createDirectories(this.root.resolve("world").resolve("region"));
        final ServerLayout layout = new ServerLayout(this.root);
        assertEquals(this.root, layout.defaultWorldContainer("world"));
        assertEquals(this.root, layout.defaultWorldContainer("other"), "a vanilla world/ folder means the root is the legacy container");

        final ServerLayout fresh = new ServerLayout(this.root.resolve("fresh"));
        Files.createDirectories(fresh.root().resolve("survival"));
        assertEquals(fresh.root(), fresh.defaultWorldContainer("survival"));
        assertEquals(fresh.root().resolve("worlds"), fresh.defaultWorldContainer("lobby"));
    }

    @Test
    void configReferencesPreferExistingLegacyPathThenConfigDirectory() throws IOException {
        final ServerLayout layout = new ServerLayout(this.root);
        assertEquals(this.root.resolve("config").resolve("permissions.yml"), layout.resolveConfigReference("permissions.yml"));
        this.write(this.root.resolve("perms").resolve("custom.yml"), "x");
        assertEquals(this.root.resolve("perms").resolve("custom.yml"), layout.resolveConfigReference("perms/custom.yml"));
        final Path absolute = this.root.resolve("abs.yml").toAbsolutePath();
        assertEquals(absolute, layout.resolveConfigReference(absolute.toString()));
    }

    @Test
    void worldConfigRenameAndLegacyArchive() throws IOException {
        final Path world = this.root.resolve("worlds").resolve("world");
        this.write(world.resolve("gale-world.yml"), "per-world");
        assertTrue(ServerLayout.migrateWorldConfig(world));
        assertEquals("per-world", Files.readString(world.resolve("watermc-world.yml")));
        assertFalse(ServerLayout.migrateWorldConfig(world), "second run is a no-op");

        final ServerLayout layout = new ServerLayout(this.root);
        this.write(layout.configFile("gale-global.yml"), "one");
        final Path first = layout.archiveLegacyConfig(layout.configFile("gale-global.yml"));
        this.write(layout.configFile("gale-global.yml"), "two");
        final Path second = layout.archiveLegacyConfig(layout.configFile("gale-global.yml"));
        assertNotEquals(first, second);
        assertEquals("one", Files.readString(first));
        assertEquals("two", Files.readString(second));
        assertEquals(this.root.resolve("config").resolve("legacy"), first.getParent());
    }
}
