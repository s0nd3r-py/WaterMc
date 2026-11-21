package me.s0nd3r.watermc.bootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import joptsimple.OptionSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import me.s0nd3r.watermc.WaterMain;

import static org.junit.jupiter.api.Assertions.*;

class WaterMainTest {

    private static OptionSet parse(final String... args) {
        return WaterMain.parse(args).options();
    }

    @Test
    void acceptsTheHostingWhitelist() {
        final OptionSet options = parse(
            "--port", "25570", "--host", "0.0.0.0", "--world-dir", "custom-worlds", "--plugins", "mods",
            "--config", "custom.properties", "--bukkit-settings", "b.yml", "--spigot-settings", "s.yml",
            "--paper-settings", "paper-dir", "--purpur-settings", "p.yml", "--watermc-settings", "w.yml",
            "--commands-settings", "c.yml", "--help-settings", "h.yml", "--permissions-settings", "perm.yml",
            "--nojline", "--noconsole", "--forceUpgrade");
        assertEquals(25570, options.valueOf("port"));
        assertEquals(25570, options.valueOf("server-port"), "server.properties reads the server-port alias");
        assertEquals("0.0.0.0", options.valueOf("server-ip"));
        assertEquals(new File("custom-worlds"), options.valueOf("universe"), "the vanilla bootstrap reads the universe alias");
        assertEquals(new File("w.yml"), options.valueOf("watermc-settings"));
        assertEquals(new File("paper-dir"), options.valueOf("paper-settings"));
        assertTrue(options.has("nojline"));
        assertTrue(options.has("noconsole"));
        assertTrue(options.has("forceUpgrade"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"--demo", "--bonusChest", "--eraseCache", "--jfrProfile", "--singleplayer", "--safeMode", "-demo", "--safemode"})
    void rejectsRemovedFlags(final String flag) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> WaterMain.parse(new String[]{flag}));
        assertTrue(ex.getMessage().contains("не поддерживается"), ex.getMessage());
    }

    @Test
    void rejectsUnknownFlagsAndPositionalArguments() {
        assertThrows(joptsimple.OptionException.class, () -> parse("--level-name", "world"));
        assertThrows(joptsimple.OptionException.class, () -> parse("--add-plugin", "x.jar"));
        assertThrows(IllegalArgumentException.class, () -> parse("something"));
    }

    @Test
    void hostingPanelNoguiIsAcceptedAsNoOp() {
        final OptionSet options = parse("nogui", "--nogui", "--port", "1");
        assertEquals(1, options.valueOf("port"));
        assertFalse(options.has("nogui"));
    }

    @Test
    void configFilesDefaultToConfigDirectory() {
        final OptionSet options = parse();
        final Path config = ServerLayout.current().configDir();
        for (final String option : List.of("bukkit-settings", "spigot-settings", "purpur-settings", "watermc-settings", "commands-settings", "help-settings", "permissions-settings")) {
            final File file = (File) options.valueOf(option);
            assertEquals(config.toAbsolutePath(), file.toPath().toAbsolutePath().getParent(), option);
        }
        assertEquals(config.toFile(), options.valueOf("paper-settings"));
        assertEquals(new File("server.properties").toPath().toAbsolutePath(), ((File) options.valueOf("config")).toPath().toAbsolutePath());
        assertFalse(options.has("universe"), "world container is chosen by ServerLayout when not given");
        // Ключи, которые ядро всё ещё опрашивает, безопасно отсутствуют.
        assertFalse(options.has("demo"));
        assertNull(options.valueOf("world"));
        assertTrue(options.valuesOf("add-plugin").isEmpty());
    }

    @Test
    void versionAndHelpDoNotBootTheServer() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertEquals(WaterMain.EXIT_OK, WaterMain.run(new String[]{"--version"}, new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8)));
        assertTrue(out.toString(StandardCharsets.UTF_8).startsWith("WaterMc"), out.toString(StandardCharsets.UTF_8));

        out.reset();
        assertEquals(WaterMain.EXIT_OK, WaterMain.run(new String[]{"--help"}, new PrintStream(out, true, StandardCharsets.UTF_8), new PrintStream(err, true, StandardCharsets.UTF_8)));
        final String help = out.toString(StandardCharsets.UTF_8);
        assertTrue(help.contains("--watermc-settings"), help);
        assertFalse(help.contains("--demo"), help);
    }

    @Test
    void removedFlagExitsWithUsageError() {
        final ByteArrayOutputStream err = new ByteArrayOutputStream();
        assertEquals(WaterMain.EXIT_USAGE, WaterMain.run(new String[]{"--demo"}, new PrintStream(new ByteArrayOutputStream()), new PrintStream(err, true, StandardCharsets.UTF_8)));
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("--demo"));
    }

    @Test
    void entryPointIsHeadless() throws Exception {
        assertNotNull(WaterMain.versionString()); // инициализирует WaterMain
        assertEquals("true", System.getProperty("java.awt.headless"));
        for (final Class<?> type : List.of(WaterMain.class, org.bukkit.craftbukkit.Main.class, ServerLayout.class)) {
            final String bytecode = classBytes(type);
            assertFalse(bytecode.contains("java/awt"), type + " references AWT");
            assertFalse(bytecode.contains("javax/swing"), type + " references Swing");
            assertFalse(bytecode.contains("GraphicsEnvironment"), type + " references GraphicsEnvironment");
        }
        assertThrows(ClassNotFoundException.class, () -> Class.forName("net.minecraft.server.gui.MinecraftServerGui", false, WaterMain.class.getClassLoader()));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("com.destroystokyo.paper.gui.GuiStatsComponent", false, WaterMain.class.getClassLoader()));
    }

    @Test
    void legacyMainDelegatesAndKeepsPublicFields() throws Exception {
        final Class<?> legacy = org.bukkit.craftbukkit.Main.class;
        assertTrue(java.lang.reflect.Modifier.isStatic(legacy.getField("useJline").getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(legacy.getField("useConsole").getModifiers()));
        assertNotNull(legacy.getField("BOOT_TIME").get(null));
        assertTrue(classBytes(legacy).contains("me/s0nd3r/watermc/WaterMain"));
    }

    private static String classBytes(final Class<?> type) throws Exception {
        try (InputStream in = type.getResourceAsStream(type.getSimpleName() + ".class")) {
            assertNotNull(in, type.getName());
            return new String(in.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }
}
