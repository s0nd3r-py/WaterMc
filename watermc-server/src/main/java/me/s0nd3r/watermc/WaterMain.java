package me.s0nd3r.watermc;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import me.s0nd3r.watermc.bootstrap.ServerLayout;

import static java.util.Arrays.asList;

/**
 * Точка входа WaterMc.
 *
 * <p>Сервер всегда headless: окно консоли (Swing/AWT) не создаётся и классы GUI JVM не загружаются.
 * Принимается только белый список аргументов для сервера и хостинг-панелей (Pterodactyl/Wings).
 * {@link org.bukkit.craftbukkit.Main} остаётся тонким делегатом для совместимости с плагинами.</p>
 */
public final class WaterMain {

    /** Флаги клиента/отладки, удалённые из WaterMc. Их использование завершает запуск с понятной ошибкой. */
    public static final Set<String> REMOVED_FLAGS = Set.of(
        "demo", "bonusChest", "eraseCache", "jfrProfile", "singleplayer", "safeMode"
    );

    /** Аргументы, которые хостинг-панели традиционно передают; на headless-сервере они ничего не делают. */
    public static final Set<String> IGNORED_COMPAT_ARGUMENTS = Set.of("nogui", "--nogui", "-nogui");

    public static final int EXIT_OK = 0;
    public static final int EXIT_USAGE = 2;

    static {
        // Paper - Reset loggers after shutdown. Должно быть установлено до первого обращения к JUL.
        System.setProperty("java.util.logging.manager", "io.papermc.paper.log.CustomLogManager");
        System.setProperty("java.awt.headless", "true");
    }

    private WaterMain() {
    }

    public static void main(final String[] args) {
        final int code = run(args, System.out, System.err);
        if (code != EXIT_OK) {
            System.exit(code);
        }
    }

    /**
     * Разбирает аргументы и запускает сервер. Возвращает код выхода для случаев, когда сервер не запускался.
     */
    public static int run(final String[] args, final PrintStream out, final PrintStream err) {
        final Object bootTime = org.bukkit.craftbukkit.Main.BOOT_TIME; // фиксирует момент старта как можно раньше
        if (bootTime == null) {
            throw new IllegalStateException("BOOT_TIME");
        }
        if (System.getProperty("jdk.nio.maxCachedBufferSize") == null) {
            System.setProperty("jdk.nio.maxCachedBufferSize", "262144"); // Paper - cap per-thread NIO cache size
        }

        final ParsedArguments parsed;
        try {
            parsed = parse(args);
        } catch (final IllegalArgumentException | OptionException ex) {
            err.println("[WaterMc] " + ex.getMessage());
            err.println("[WaterMc] Используйте --help для списка поддерживаемых аргументов.");
            return EXIT_USAGE;
        }
        final OptionSet options = parsed.options();

        if (options.has("help")) {
            printHelp(out);
            return EXIT_OK;
        }
        if (options.has("version")) {
            out.println(versionString());
            return EXIT_OK;
        }

        final String path = Path.of("").toAbsolutePath().toString();
        if (path.contains("!") || path.contains("+")) {
            err.println("Cannot run server in a directory with ! or + in the pathname. Please rename the affected folders and try again.");
            return EXIT_USAGE;
        }

        // Paper - Improve java version check
        final String javaVersionName = System.getProperty("java.version");
        if (javaVersionName.contains("-")) {
            if (!Boolean.getBoolean("Paper.IgnoreJavaVersion")) {
                err.println("Unsupported Java detected (" + javaVersionName + "). Only general availability versions of Java are supported.");
                return EXIT_USAGE;
            }
            err.println("Unsupported Java detected (" + javaVersionName + "), but the check was skipped. Proceed with caution!");
        }

        final ServerLayout layout = ServerLayout.current();
        try {
            final ServerLayout.MigrationReport report = layout.migrate();
            report.moved().forEach(moved -> out.println("[WaterMc] Перенесено: " + moved));
            report.conflicts().forEach(conflict -> err.println("[WaterMc] Не перенесено, файл уже существует: " + conflict));
            final ServerLayout.WorldMove worlds = explicitWorldContainer(options, layout)
                ? new ServerLayout.WorldMove(java.util.List.of(), null)
                : layout.moveRootWorlds();
            worlds.moved().forEach(moved -> out.println("[WaterMc] Мир перенесён: " + moved));
            if (worlds.skippedReason() != null) {
                err.println("[WaterMc] Миры не перенесены в worlds/: " + worlds.skippedReason());
            }
            if (!report.isEmpty() || worlds.happened() || worlds.skippedReason() != null) {
                final java.util.List<String> lines = new java.util.ArrayList<>();
                lines.add("WaterMc: перенос сервера в раскладку config/ data/ worlds/");
                lines.add("Ничего не удалено: файлы и миры перемещены, JSON-базы сохранены в data/migration-backup/,");
                lines.add("конфиги других ядер — в config/legacy/.");
                lines.add("");
                report.moved().forEach(line -> lines.add("перенесено: " + line));
                worlds.moved().forEach(line -> lines.add("мир: " + line));
                report.conflicts().forEach(line -> lines.add("НЕ перенесено (уже существует): " + line));
                if (worlds.skippedReason() != null) {
                    lines.add("миры остались в корне: " + worlds.skippedReason());
                }
                if (worlds.happened()) {
                    lines.add("");
                    lines.add("Внимание: если плагины (резервные копии, карты) хранят путь к миру как \"world/...\",");
                    lines.add("укажите в их настройках \"worlds/world/...\". Отключить перенос миров: -D" + ServerLayout.MOVE_WORLDS_PROPERTY + "=false");
                }
                out.println("[WaterMc] Отчёт об апгрейде: " + layout.writeUpgradeReport(lines));
            }
        } catch (final IOException ex) {
            err.println("[WaterMc] Не удалось подготовить каталоги config/ и data/: " + ex);
            return EXIT_USAGE;
        }

        try {
            if (options.has("nojline")) {
                System.setProperty(net.minecrell.terminalconsole.TerminalConsoleAppender.JLINE_OVERRIDE_PROPERTY, "false");
                org.bukkit.craftbukkit.Main.useJline = false;
            }
            if (options.has("noconsole")) {
                org.bukkit.craftbukkit.Main.useConsole = false;
                org.bukkit.craftbukkit.Main.useJline = false;
                System.setProperty(net.minecrell.terminalconsole.TerminalConsoleAppender.JLINE_OVERRIDE_PROPERTY, "false");
            }

            System.setProperty("library.jansi.version", "Paper"); // Paper - prevent git builds from crashing on Windows
            System.setProperty("jdk.console", "java.base"); // Paper - revert default console provider back to java.base so we can have our own jline

            io.papermc.paper.PaperBootstrap.boot(options);
        } catch (final Throwable t) {
            t.printStackTrace(err);
        }
        return EXIT_OK;
    }

    /** Каталог миров задан явно: {@code --world-dir} или {@code settings.world-container} в bukkit.yml. */
    static boolean explicitWorldContainer(final OptionSet options, final ServerLayout layout) {
        if (options.has("universe")) {
            return true;
        }
        final Object bukkitSettings = options.valueOf("bukkit-settings");
        final java.io.File bukkit = bukkitSettings instanceof java.io.File file ? file : layout.configFileLegacy("bukkit.yml");
        if (!bukkit.isFile()) {
            return false;
        }
        return org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(bukkit).isSet("settings.world-container");
    }

    /** Разбор с проверкой белого списка. Публичен для тестов. */
    public static ParsedArguments parse(final String[] args) {
        final List<String> filtered = new ArrayList<>(args.length);
        for (final String arg : args) {
            if (IGNORED_COMPAT_ARGUMENTS.contains(arg)) {
                continue;
            }
            final String removed = removedFlag(arg);
            if (removed != null) {
                throw new IllegalArgumentException("Флаг --" + removed + " не поддерживается WaterMc и был удалён.");
            }
            filtered.add(arg);
        }
        final OptionSet options = createParser().parse(filtered.toArray(String[]::new));
        if (!options.nonOptionArguments().isEmpty()) {
            throw new IllegalArgumentException("Неизвестные аргументы: " + options.nonOptionArguments());
        }
        return new ParsedArguments(options);
    }

    private static String removedFlag(final String arg) {
        if (!arg.startsWith("-")) {
            return null;
        }
        String name = arg.startsWith("--") ? arg.substring(2) : arg.substring(1);
        final int eq = name.indexOf('=');
        if (eq >= 0) {
            name = name.substring(0, eq);
        }
        for (final String removed : REMOVED_FLAGS) {
            if (removed.equalsIgnoreCase(name)) {
                return removed;
            }
        }
        return null;
    }

    /**
     * Белый список аргументов. Внутренние алиасы ({@code universe}, {@code server-port}, {@code server-ip})
     * оставлены, потому что по этим ключам ядро и {@code server.properties} читают значения из {@link OptionSet}.
     */
    public static OptionParser createParser() {
        final ServerLayout layout = ServerLayout.current();
        final OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("?", "help"), "Показать справку").forHelp();

        parser.acceptsAll(asList("p", "port", "server-port"), "Порт").withRequiredArg().ofType(Integer.class).describedAs("Port");
        parser.acceptsAll(asList("h", "host", "server-ip"), "Адрес для прослушивания").withRequiredArg().ofType(String.class).describedAs("Hostname or IP");
        parser.acceptsAll(asList("W", "world-dir", "universe", "world-container"), "Каталог миров (по умолчанию worlds/)")
            .withRequiredArg().ofType(File.class).describedAs("Directory containing worlds");
        parser.acceptsAll(asList("P", "plugins"), "Каталог плагинов").withRequiredArg().ofType(File.class)
            .defaultsTo(layout.root().resolve("plugins").toFile()).describedAs("Plugin directory");
        parser.acceptsAll(asList("c", "config"), "Файл server.properties").withRequiredArg().ofType(File.class)
            .defaultsTo(layout.root().resolve("server.properties").toFile()).describedAs("Properties file");

        configFile(parser, layout, "bukkit-settings", "bukkit.yml", "b");
        configFile(parser, layout, "spigot-settings", "spigot.yml", "S");
        configFile(parser, layout, "purpur-settings", "purpur.yml", null);
        configFile(parser, layout, "watermc-settings", ServerLayout.WATERMC_GLOBAL_FILE, null);
        configFile(parser, layout, "commands-settings", "commands.yml", "C");
        configFile(parser, layout, "help-settings", "help.yml", null);
        configFile(parser, layout, "permissions-settings", "permissions.yml", null);
        parser.accepts("paper-settings", "Каталог конфигураций Paper (paper-global.yml, paper-world-defaults.yml)")
            .withRequiredArg().ofType(File.class).defaultsTo(layout.configDir().toFile()).describedAs("Config directory");

        parser.accepts("nojline", "Отключить jline и эмулировать ванильную консоль");
        parser.accepts("noconsole", "Отключить консоль");
        parser.acceptsAll(asList("v", "version"), "Показать версию WaterMc");
        parser.accepts("forceUpgrade", "Принудительно обновить миры");
        return parser;
    }

    private static void configFile(final OptionParser parser, final ServerLayout layout, final String option, final String fileName, final String shortName) {
        final List<String> names = shortName == null ? List.of(option) : List.of(shortName, option);
        parser.acceptsAll(names, "Файл " + fileName).withRequiredArg().ofType(File.class)
            .defaultsTo(layout.configFileLegacy(fileName)).describedAs("Yml file");
    }

    private static void printHelp(final PrintStream out) {
        try {
            out.println("WaterMc " + versionString());
            createParser().printHelpOn(out);
        } catch (final IOException ex) {
            throw new java.io.UncheckedIOException(ex);
        }
    }

    public static String versionString() {
        final String implementation = WaterMain.class.getPackage().getImplementationVersion();
        return implementation == null ? "WaterMc (development build)" : "WaterMc " + implementation;
    }

    public record ParsedArguments(OptionSet options) {
        public boolean has(final String option) {
            return this.options.has(option);
        }
    }
}
