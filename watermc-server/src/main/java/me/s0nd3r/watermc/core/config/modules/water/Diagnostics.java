package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;
import me.s0nd3r.watermc.diagnostics.HeapGuard;
import me.s0nd3r.watermc.diagnostics.NettyWatchdog;
import me.s0nd3r.watermc.diagnostics.PluginWatchdog;
import me.s0nd3r.watermc.diagnostics.WaterMetrics;

public class Diagnostics extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName() + ".diagnostics";
    }

    public static boolean metricsEnabled = false;
    public static String metricsHost = "127.0.0.1";
    public static int metricsPort = 9225;
    public static double heapGuardThreshold = 0.82;
    public static boolean heapGuardEnabled = true;
    public static int drainTimeoutSeconds = 15;
    private static boolean relieversRegistered;

    @Override
    public void onLoaded() {
        final String base = getBasePath();
        PluginWatchdog.enabled = config.getBoolean(base + ".plugin-watchdog.enabled", true, """
            [WaterMc] Measure plugin listeners and sync tasks on the tick threads and warn when a plugin
            uses more than threshold-ms of a single tick.""");
        PluginWatchdog.thresholdNanos = Math.max(1L, config.getLong(base + ".plugin-watchdog.threshold-ms", 15L)) * 1_000_000L;
        PluginWatchdog.warnCooldownNanos = Math.max(0L, config.getLong(base + ".plugin-watchdog.warn-cooldown-seconds", 30L)) * 1_000_000_000L;
        PluginWatchdog.startupGraceNanos = Math.max(0L, config.getLong(base + ".plugin-watchdog.startup-grace-seconds", 60L, """
            [WaterMc] No plugin-watchdog warnings during the first seconds after startup (cold JIT, class loading).""")) * 1_000_000_000L;

        NettyWatchdog.enabled = config.getBoolean(base + ".netty-watchdog.enabled", true, """
            [WaterMc] Detect stuck Netty event loops (network threads). A stuck loop freezes packets for every
            player on it; the watchdog logs the thread stack once per stall and again when it recovers.""");
        NettyWatchdog.stallMillis = Math.max(1L, config.getLong(base + ".netty-watchdog.stall-seconds", 10L)) * 1000L;
        NettyWatchdog.printStack = config.getBoolean(base + ".netty-watchdog.print-stack", true);

        me.s0nd3r.watermc.diagnostics.SecurityAudit.ops = config.getBoolean(base + ".security-audit.ops", true, """
            [WaterMc] Security audit: log OP grants/removals, console commands issued by plugins, switches into
            creative/spectator and plugins disabled at runtime, naming the plugin whose code made the call.
            Helps to find backdoors hidden in plugins.""");
        me.s0nd3r.watermc.diagnostics.SecurityAudit.consoleCommands = config.getBoolean(base + ".security-audit.console-commands", true);
        me.s0nd3r.watermc.diagnostics.SecurityAudit.gameModes = config.getBoolean(base + ".security-audit.game-modes", true);
        me.s0nd3r.watermc.diagnostics.SecurityAudit.pluginDisable = config.getBoolean(base + ".security-audit.plugin-disable", true);
        me.s0nd3r.watermc.diagnostics.SecurityAudit.stackTraces = config.getBoolean(base + ".security-audit.stack-traces", false, """
            [WaterMc] Also log the full call stack for every audited action (to find the exact class and method).""");

        heapGuardEnabled = config.getBoolean(base + ".heap-guard.enabled", heapGuardEnabled, """
            [WaterMc] When the old generation stays above threshold-percent after a GC, clear cold caches.""");
        heapGuardThreshold = Math.min(0.99, Math.max(0.5, config.getDouble(base + ".heap-guard.threshold-percent", heapGuardThreshold * 100) / 100.0));

        metricsEnabled = config.getBoolean(base + ".metrics.enabled", metricsEnabled, """
            [WaterMc] Built-in metrics endpoint: GET /metrics (Prometheus) and /metrics.json.
            Disabled by default. Bind to 127.0.0.1 unless the port is firewalled.""");
        metricsHost = config.getString(base + ".metrics.host", metricsHost);
        metricsPort = config.getInt(base + ".metrics.port", metricsPort);

        drainTimeoutSeconds = Math.max(1, config.getInt(base + ".drain.timeout-seconds", drainTimeoutSeconds, """
            [WaterMc] /watermc drain: how long to wait for players to leave before stopping."""));
    }

    @Override
    public void onPostLoaded() {
        if (heapGuardEnabled) {
            if (!relieversRegistered) {
                relieversRegistered = true;
                HeapGuard.registerReliever(me.s0nd3r.watermc.storage.SectionPacketCache::invalidateAll);
                HeapGuard.registerReliever(() -> me.s0nd3r.watermc.integration.protocol.jade.provider.ItemStorageExtensionProvider.targetCache.invalidateAll());
            }
            HeapGuard.install(heapGuardThreshold);
        }
        if (metricsEnabled) {
            WaterMetrics.startHttp(metricsHost, metricsPort);
        } else {
            WaterMetrics.stopHttp();
        }
    }
}
