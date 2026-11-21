package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Idea from ServerCore "dynamic" settings (Wesley1808, MIT), reimplemented in {@code me.s0nd3r.watermc.world.DynamicDistance}. */
public class DynamicDistanceConfig extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".dynamic-distance";
    }

    public static boolean enabled = false;
    public static double targetMspt = 40.0D;
    public static int minViewDistance = 4;
    public static int minSimulationDistance = 4;
    public static int intervalSeconds = 5;

    @Override
    public void onLoaded() {
        final String base = getBasePath();
        enabled = config.getBoolean(base + ".enabled", enabled, config.pickStringRegionBased("""
            When the server stays above target-mspt (+5 ms), lower simulation distance and then view distance by
            one chunk per step; when it is back under target-mspt (-5 ms), raise them again up to the values from
            server.properties. While enabled, per-world view/simulation distances are replaced by the shared value.""", """
            服务器卡顿时自动降低模拟距离和视距, 恢复后再提高."""));
        targetMspt = Math.clamp(config.getDouble(base + ".target-mspt", targetMspt, "Milliseconds per tick to aim for, 10-50."), 10.0D, 50.0D);
        minViewDistance = Math.clamp(config.getInt(base + ".min-view-distance", minViewDistance, "Lowest view distance, 2-32."), 2, 32);
        minSimulationDistance = Math.clamp(config.getInt(base + ".min-simulation-distance", minSimulationDistance, "Lowest simulation distance, 2-32."), 2, 32);
        intervalSeconds = Math.clamp(config.getInt(base + ".interval-seconds", intervalSeconds, "Seconds between steps, 1-60."), 1, 60);
    }
}
