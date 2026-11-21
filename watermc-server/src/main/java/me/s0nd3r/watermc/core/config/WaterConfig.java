package me.s0nd3r.watermc.core.config;

import io.papermc.paper.SparksFly;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.s0nd3r.watermc.core.config.modules.misc.SentryDSN;
import me.s0nd3r.watermc.core.config.modules.opt.FastBiomeManagerSeedObfuscation;
import org.jspecify.annotations.NullMarked;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/*
 *  Yoinked from: https://github.com/xGinko/AnarchyExploitFixes/ & https://github.com/LuminolMC/Luminol
 *  @author: @xGinko & @MrHua269
 */
@NullMarked
public class WaterConfig {

    public static final Logger LOGGER = LogManager.getLogger("WaterMc"); // WaterMc - [WaterMc] log prefix
    protected static final File I_CONFIG_FOLDER = new File("config");
    protected static final String I_CONFIG_PKG = "me.s0nd3r.watermc.core.config.modules";
    protected static final String I_GLOBAL_CONFIG_FILE = me.s0nd3r.watermc.bootstrap.ServerLayout.WATERMC_GLOBAL_FILE; // WaterMc - config/watermc.yml

    // WaterMc start - --watermc-settings
    private static volatile File globalConfigFile = new File(I_CONFIG_FOLDER, I_GLOBAL_CONFIG_FILE);

    public static void useGlobalConfigFile(final @org.jspecify.annotations.Nullable File file) {
        globalConfigFile = file != null ? file : new File(I_CONFIG_FOLDER, I_GLOBAL_CONFIG_FILE);
    }

    public static File globalConfigFile() {
        return globalConfigFile;
    }
    // WaterMc end - --watermc-settings
    protected static final String I_LEVEL_CONFIG_FILE = "leaf-world-defaults.yml"; // Leaf TODO - Per level config

    private static final String SPARK_EXTRA_CONFIG_PROPERTY =  "spark.serverconfigs.extra";
    private static final String SPARK_HIDDEN_PATHS_PROPERTY =  "spark.serverconfigs.hiddenpaths";

    private static WaterGlobalConfig leafGlobalConfig;

    //private static int preMajorVer;
    private static int preMinorVer;
    //private static int currMajorVer;
    private static int currMinorVer;

    /* Load & Reload */

    // Reload config (async)
    public static CompletableFuture<Void> reloadAsync(CommandSender sender) {
        return CompletableFuture.runAsync(() -> {
            try {
                long begin = System.nanoTime();

                ConfigModules.clearModules();
                loadConfig(false);
                ConfigModules.loadAfterBootstrap();

                final String success = String.format("Successfully reloaded config in %sms.", (System.nanoTime() - begin) / 1_000_000);
                Command.broadcastCommandMessage(sender, Component.text(success, NamedTextColor.GREEN));
            } catch (Exception e) {
                Command.broadcastCommandMessage(sender, Component.text("Failed to reload config. See error in console!", NamedTextColor.RED));
                LOGGER.error("Failed to reload config!", e);
            }
        }, Util.ioPool());
    }

    // Init config
    public static void loadConfig() {
        try {
            long begin = System.nanoTime();
            LOGGER.info("Loading config...");

            purgeOutdated();
            loadConfig(true);

            LOGGER.info("Successfully loaded config in {}ms.", (System.nanoTime() - begin) / 1_000_000);
        } catch (Exception e) {
            LOGGER.error("Failed to load config modules!", e);
        }
    }

    /* Load Global Config */

    private static void loadConfig(boolean init) throws Exception {
        // Create config folder
        final File configFolder = globalConfigFile().getAbsoluteFile().getParentFile(); // WaterMc - --watermc-settings
        createDirectory(configFolder != null ? configFolder : I_CONFIG_FOLDER);

        leafGlobalConfig = new WaterGlobalConfig(init);

        // Load config modules
        ConfigModules.initModules();
    }

    public static WaterGlobalConfig config() {
        return leafGlobalConfig;
    }

    /* Create config folder */

    protected static void createDirectory(File dir) throws IOException {
        try {
            Files.createDirectories(dir.toPath());
        } catch (FileAlreadyExistsException e) { // Thrown if dir exists but is not a directory
            if (dir.delete()) createDirectory(dir);
        }
    }

    /* Scan classes under package */

    public static Set<Class<?>> getClasses(String pack) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageDirName = pack.replace('.', '/');
        Enumeration<URL> dirs;

        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    findClassesInPackageByFile(pack, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        findClassesInPackageByJar(pack, entries, packageDirName, classes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return classes;
    }

    private static void findClassesInPackageByFile(String packageName, String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] dirfiles = dir.listFiles((file) -> file.isDirectory() || file.getName().endsWith(".class"));
        if (dirfiles != null) {
            for (File file : dirfiles) {
                if (file.isDirectory()) {
                    findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, Set<Class<?>> classes) {
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }

            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');

                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                }

                if (name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    // TODO
    public static void loadConfigVersion(String preVer, String currVer) {
        int currMinor;
        int preMinor;

        // First time user
        if (preVer == null) {

        }
    }

    /* Register Spark profiler extra server configurations */

    private static List<String> buildSparkExtraConfigs() {
        List<String> extraConfigs = new ArrayList<>(Arrays.asList(
            "config/" + me.s0nd3r.watermc.bootstrap.ServerLayout.WATERMC_GLOBAL_FILE,
            "config/" + me.s0nd3r.watermc.bootstrap.ServerLayout.WATERMC_WORLD_FILE
        ));

        String existing = System.getProperty(SPARK_EXTRA_CONFIG_PROPERTY);
        if (existing != null) {
            extraConfigs.addAll(Arrays.asList(existing.split(",")));
        }

        // Use same way in spark's BukkitServerConfigProvider#getNestedFiles to get all world configs
        // It may spam in the spark profiler, but it's ok, since spark uses YamlConfigParser.INSTANCE
        // instead of using SplitYamlConfigParser.INSTANCE for the extra config
        // However it's better to choose bundled spark for better view.
        for (World world : Bukkit.getWorlds()) {
            Path galeWorldFolder = world.getWorldFolder().toPath().resolve(me.s0nd3r.watermc.bootstrap.ServerLayout.WATERMC_WORLD_FILE);
            extraConfigs.add(galeWorldFolder.toString().replace("\\", "/").replace("./", "")); // WaterMc world config
        }

        return extraConfigs;
    }

    private static List<String> buildSparkHiddenPaths() {
        String existing = System.getProperty(SPARK_HIDDEN_PATHS_PROPERTY);

        List<String> extraHidden = existing != null ? new ArrayList<>(Arrays.asList(existing.split(","))) : new ArrayList<>();
        extraHidden.add(SentryDSN.sentryDsnConfigPath); // Hide Sentry DSN key
        extraHidden.add(FastBiomeManagerSeedObfuscation.seedObfKeyPath); // Hide FastBiomeManagerSeedObfuscation key

        return extraHidden;
    }

    // Sync with WaterServerConfigProvider
    public static void regSparkExtraConfig() {
        // Spark plugin is used
        if (SparksFly.isPluginPreferred() && Bukkit.getServer().getPluginManager().getPlugin("spark") != null) {
            String extraConfigs = String.join(",", buildSparkExtraConfigs());
            System.setProperty(SPARK_EXTRA_CONFIG_PROPERTY, extraConfigs);

            String hiddenPaths = String.join(",", buildSparkHiddenPaths());
            System.setProperty(SPARK_HIDDEN_PATHS_PROPERTY, hiddenPaths);
        }
    }

    /* Purge and backup old Leaf config & Pufferfish config */

    private static void purgeOutdated() {
        boolean foundLegacy = false;
        String pufferfishConfig = "pufferfish.yml";
        String leafConfigV1 = "leaf.yml";
        String leafConfigV2 = "leaf_config";

        String backupDir = "config/backup" + me.s0nd3r.watermc.util.WaterDateFormats.format(me.s0nd3r.watermc.util.WaterDateFormats.BACKUP_STAMP, java.time.Instant.now()) + "/";

        File pufferfishConfigFile = new File(pufferfishConfig);
        File leafConfigV1File = new File(leafConfigV1);
        File leafConfigV2File = new File(leafConfigV2);
        File backupDirFile = new File(backupDir);

        try {
            if (pufferfishConfigFile.exists() && pufferfishConfigFile.isFile()) {
                createDirectory(backupDirFile);
                Files.move(pufferfishConfigFile.toPath(), Path.of(backupDir + pufferfishConfig), StandardCopyOption.REPLACE_EXISTING);
                foundLegacy = true;
            }
            if (leafConfigV1File.exists() && leafConfigV1File.isFile()) {
                createDirectory(backupDirFile);
                Files.move(leafConfigV1File.toPath(), Path.of(backupDir + leafConfigV1), StandardCopyOption.REPLACE_EXISTING);
                foundLegacy = true;
            }
            if (leafConfigV2File.exists() && leafConfigV2File.isDirectory()) {
                createDirectory(backupDirFile);
                Files.move(leafConfigV2File.toPath(), Path.of(backupDir + leafConfigV2), StandardCopyOption.REPLACE_EXISTING);
                foundLegacy = true;
            }

            if (foundLegacy) {
                LOGGER.warn("Found legacy Leaf/Pufferfish config files, moved to backup directory: {}", backupDir);
                LOGGER.warn("The WaterMc config is config/watermc.yml. Transfer the old values manually and restart the server!");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to purge old configs.", e);
        }
    }
}
