package me.s0nd3r.watermc.config;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.s0nd3r.watermc.bootstrap.ServerLayout;
import me.s0nd3r.watermc.util.WaterDateFormats;

/**
 * Санитайзер конфигураций.
 * <ul>
 *     <li>Удаляет из {@code watermc.yml} ключи, которые больше не регистрирует ни один модуль (переименованные
 *     или удалённые опции). Значения перед удалением сохраняются в {@code config/legacy/removed-keys-*.yml}.</li>
 *     <li>Предупреждает о конфликтующих настройках между bukkit.yml / spigot.yml / paper-global.yml / purpur.yml / watermc.yml.</li>
 * </ul>
 */
public final class ConfigSanitizer {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");
    private static final Set<String> ALWAYS_KEEP = Set.of("config-version");

    private ConfigSanitizer() {
    }

    /**
     * Ключи файла, которые не зарегистрированы, не являются секцией зарегистрированного ключа и не лежат
     * внутри «свободной» секции (map-значения) или зарегистрированного ключа. Возвращаются только верхние
     * мёртвые ключи (их потомки удаляются вместе с ними).
     */
    public static List<String> findDeadKeys(final Collection<String> fileKeysDeep, final Set<String> registered, final Set<String> lenient) {
        final TreeSet<String> sorted = new TreeSet<>(fileKeysDeep);
        final List<String> dead = new ArrayList<>();
        outer:
        for (final String key : sorted) {
            if (ALWAYS_KEEP.contains(key) || registered.contains(key)) {
                continue;
            }
            for (final String deadParent : dead) {
                if (key.startsWith(deadParent + ".")) {
                    continue outer;
                }
            }
            for (final String path : registered) {
                if (path.startsWith(key + ".")) {
                    continue outer; // секция над зарегистрированным ключом
                }
            }
            for (final String path : lenient) {
                if (key.startsWith(path + ".")) {
                    continue outer;
                }
            }
            dead.add(key);
        }
        return dead;
    }

    /** Убирает мёртвые ключи из watermc.yml, предварительно сохранив их значения. */
    public static List<String> sanitize(final File file, final Set<String> registered, final Set<String> lenient, final java.util.function.Consumer<String> remover) {
        if (!file.isFile()) {
            return List.of();
        }
        final YamlConfiguration raw = YamlConfiguration.loadConfiguration(file);
        final List<String> dead = findDeadKeys(raw.getKeys(true), registered, lenient);
        if (dead.isEmpty()) {
            return dead;
        }
        try {
            final YamlConfiguration backup = new YamlConfiguration();
            for (final String key : dead) {
                backup.set(key, raw.get(key));
            }
            final Path dir = ServerLayout.current().configDir().resolve(ServerLayout.LEGACY_BACKUP_DIR);
            Files.createDirectories(dir);
            final Path target = dir.resolve("removed-keys-" + WaterDateFormats.format(WaterDateFormats.BACKUP_STAMP, java.time.Instant.now()) + ".yml");
            backup.save(target.toFile());
            dead.forEach(remover);
            LOGGER.warn("{}: removed obsolete keys {}; previous values saved to {}", file.getName(), dead, target);
        } catch (final Exception ex) {
            LOGGER.warn("Could not back up obsolete keys {}; they were left unchanged", dead, ex);
            return List.of();
        }
        return dead;
    }

    /** Правила конфликтов. Ключ карты — имя файла (bukkit.yml, spigot.yml, paper-global.yml, purpur.yml, watermc.yml). */
    public static List<String> findConflicts(final Map<String, YamlConfiguration> configs) {
        final List<String> conflicts = new ArrayList<>();
        final YamlConfiguration bukkit = configs.getOrDefault("bukkit.yml", new YamlConfiguration());
        final YamlConfiguration spigot = configs.getOrDefault("spigot.yml", new YamlConfiguration());
        final YamlConfiguration paper = configs.getOrDefault("paper-global.yml", new YamlConfiguration());
        final YamlConfiguration purpur = configs.getOrDefault("purpur.yml", new YamlConfiguration());
        final YamlConfiguration water = configs.getOrDefault("watermc.yml", new YamlConfiguration());

        final boolean bungee = spigot.getBoolean("settings.bungeecord", false);
        final boolean velocity = paper.getBoolean("proxies.velocity.enabled", false);
        if (bungee && velocity) {
            conflicts.add("spigot.yml settings.bungeecord и paper-global.yml proxies.velocity.enabled включены одновременно — оставьте один режим прокси.");
        }
        if ((bungee || velocity) && bukkit.getInt("settings.connection-throttle", 4000) > 0) {
            conflicts.add("Сервер за прокси, но bukkit.yml settings.connection-throttle > 0: все игроки приходят с адреса прокси, установите -1.");
        }
        if (purpur.getBoolean("settings.use-alternate-keepalive", false) && water.getBoolean("server-tuning.misc.keepalive.send-multiple", false)) {
            conflicts.add("purpur.yml settings.use-alternate-keepalive и watermc.yml server-tuning.misc.keepalive.send-multiple меняют keepalive одновременно — включите только одно.");
        }
        final int spigotSave = bukkit.getInt("ticks-per.autosave", 6000);
        if (spigotSave <= 0 && water.getBoolean("async.async-playerdata-save.enabled", false)) {
            conflicts.add("bukkit.yml ticks-per.autosave отключён, а асинхронное сохранение игроков включено: данные игроков будут сохраняться только при выходе и остановке.");
        }
        return conflicts;
    }

    public static void warnConflicts() {
        final ServerLayout layout = ServerLayout.current();
        final Map<String, YamlConfiguration> configs = new java.util.HashMap<>();
        for (final String name : List.of("bukkit.yml", "spigot.yml", "paper-global.yml", "purpur.yml", ServerLayout.WATERMC_GLOBAL_FILE)) {
            final File file = layout.configFileLegacy(name);
            if (file.isFile()) {
                configs.put(name, YamlConfiguration.loadConfiguration(file));
            }
        }
        for (final String conflict : findConflicts(configs)) {
            LOGGER.warn("Conflicting settings: {}", conflict);
        }
    }
}
