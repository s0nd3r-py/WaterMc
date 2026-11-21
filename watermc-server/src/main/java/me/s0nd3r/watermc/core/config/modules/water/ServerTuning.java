package me.s0nd3r.watermc.core.config.modules.water;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.file.YamlConfiguration;
import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.WaterConfig;
import me.s0nd3r.watermc.tuning.configuration.TuningGlobalConfiguration;
import org.jspecify.annotations.Nullable;
import me.s0nd3r.watermc.bootstrap.ServerLayout;

/**
 * WaterMc: глобальные настройки, ранее хранившиеся в {@code config/gale-global.yml}.
 * Теперь они находятся в секции {@code server-tuning} файла {@code config/watermc.yml}.
 * При первом запуске значения берутся из старого файла, после чего он перемещается в {@code config/legacy/}.
 */
public class ServerTuning extends ConfigModules {

    public static final String BASE_PATH = "server-tuning";

    private @Nullable YamlConfiguration legacy;
    private @Nullable Path legacyFile;

    public String getBasePath() {
        return BASE_PATH;
    }

    @Override
    public void onLoaded() {
        this.loadLegacy();
        final TuningGlobalConfiguration c = TuningGlobalConfiguration.createDefault();

        config.addComment(BASE_PATH, """
            [WaterMc] Тонкая настройка сервера (ранее gale-global.yml).
            Значения из старого gale-global.yml переносятся автоматически при первом запуске.""");

        final TuningGlobalConfiguration.SmallOptimizations.ReducedIntervals intervals = c.smallOptimizations.reducedIntervals;
        intervals.increaseTimeStatistics = this.i("small-optimizations.reduced-intervals.increase-time-statistics", intervals.increaseTimeStatistics);
        intervals.updateEntityLineOfSight = this.i("small-optimizations.reduced-intervals.update-entity-line-of-sight", intervals.updateEntityLineOfSight);

        c.gameplayMechanics.enableBookWriting = this.b("gameplay-mechanics.enable-book-writing", c.gameplayMechanics.enableBookWriting);

        final TuningGlobalConfiguration.Misc misc = c.misc;
        misc.verifyChatOrder = this.b("misc.verify-chat-order", misc.verifyChatOrder);
        misc.premiumAccountSlowLoginTimeout = this.i("misc.premium-account-slow-login-timeout", misc.premiumAccountSlowLoginTimeout);
        misc.ignoreNullLegacyStructureData = this.b("misc.ignore-null-legacy-structure-data", misc.ignoreNullLegacyStructureData);
        misc.keepalive.sendMultiple = this.b("misc.keepalive.send-multiple", misc.keepalive.sendMultiple);
        misc.lastTickTimeInTpsCommand.enabled = this.b("misc.last-tick-time-in-tps-command.enabled", misc.lastTickTimeInTpsCommand.enabled);
        misc.lastTickTimeInTpsCommand.addOversleep = this.b("misc.last-tick-time-in-tps-command.add-oversleep", misc.lastTickTimeInTpsCommand.addOversleep);

        final TuningGlobalConfiguration.LogToConsole log = c.logToConsole;
        log.invalidStatistics = this.b("log-to-console.invalid-statistics", log.invalidStatistics);
        log.ignoredAdvancements = this.b("log-to-console.ignored-advancements", log.ignoredAdvancements);
        log.setBlockInFarChunk = this.b("log-to-console.set-block-in-far-chunk", log.setBlockInFarChunk);
        log.unrecognizedRecipes = this.b("log-to-console.unrecognized-recipes", log.unrecognizedRecipes);
        log.legacyMaterialInitialization = this.b("log-to-console.legacy-material-initialization", log.legacyMaterialInitialization);
        log.nullIdDisconnections = this.b("log-to-console.null-id-disconnections", log.nullIdDisconnections);
        log.playerLoginLocations = this.b("log-to-console.player-login-locations", log.playerLoginLocations);
        log.invalidLegacyTextComponent = this.b("log-to-console.invalid-legacy-text-component", log.invalidLegacyTextComponent);
        log.chat.emptyMessageWarning = this.b("log-to-console.chat.empty-message-warning", log.chat.emptyMessageWarning);
        log.chat.expiredMessageWarning = this.b("log-to-console.chat.expired-message-warning", log.chat.expiredMessageWarning);
        log.chat.notSecureMarker = this.b("log-to-console.chat.not-secure-marker", log.chat.notSecureMarker);
        log.pluginLibraryLoader.downloads = this.b("log-to-console.plugin-library-loader.downloads", log.pluginLibraryLoader.downloads);
        log.pluginLibraryLoader.startLoadLibrariesForPlugin = this.b("log-to-console.plugin-library-loader.start-load-libraries-for-plugin", log.pluginLibraryLoader.startLoadLibrariesForPlugin);
        log.pluginLibraryLoader.libraryLoaded = this.b("log-to-console.plugin-library-loader.library-loaded", log.pluginLibraryLoader.libraryLoaded);

        TuningGlobalConfiguration.set(c);
        if (TuningGlobalConfiguration.isPostProcessingReady()) {
            c.applyPostProcessing(); // перезагрузка на работающем сервере
        }
    }

    @Override
    public void onPostLoaded() {
        // Значения уже записаны в watermc.yml (сохранение идёт сразу после onPostLoaded); старый файл убирается, не удаляясь.
        if (this.legacyFile != null && Files.isRegularFile(this.legacyFile)) {
            try {
                final Path archived = ServerLayout.current().archiveLegacyConfig(this.legacyFile);
                WaterConfig.LOGGER.info("Settings from {} moved to {}; the old file is kept as {}", this.legacyFile.getFileName(), ServerLayout.WATERMC_GLOBAL_FILE, archived);
            } catch (final Exception ex) {
                WaterConfig.LOGGER.warn("Could not move {} to config/legacy/", this.legacyFile, ex);
            }
        }
    }

    private void loadLegacy() {
        final File global = WaterConfig.globalConfigFile().getAbsoluteFile();
        final File dir = global.getParentFile();
        final Path candidate = (dir != null ? dir.toPath() : ServerLayout.current().configDir()).resolve(ServerLayout.LEGACY_GALE_GLOBAL_FILE);
        if (Files.isRegularFile(candidate)) {
            this.legacyFile = candidate;
            this.legacy = YamlConfiguration.loadConfiguration(candidate.toFile());
        }
    }

    private boolean b(final String key, final boolean def) {
        final boolean fallback = this.legacy != null && this.legacy.isBoolean(key) ? this.legacy.getBoolean(key) : def;
        return config.getBoolean(BASE_PATH + "." + key, fallback);
    }

    private int i(final String key, final int def) {
        final int fallback = this.legacy != null && this.legacy.isInt(key) ? this.legacy.getInt(key) : def;
        return config.getInt(BASE_PATH + "." + key, fallback);
    }
}
