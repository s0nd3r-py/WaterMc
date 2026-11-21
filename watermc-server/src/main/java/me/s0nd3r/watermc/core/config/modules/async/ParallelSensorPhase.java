package me.s0nd3r.watermc.core.config.modules.async;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Idea from DivineMC "Parallel sensor phase" (BX-Team / DivineMC, GPL-3.0); timer handling rewritten for WaterMc. */
public class ParallelSensorPhase extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".parallel-sensor-phase";
    }

    public static boolean enabled = false;
    public static int threads = 0;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, config.pickStringRegionBased("""
            Before entities tick, run due mob sensors (read-only scans for nearby players, mobs, items and blocks)
            in parallel while the world thread waits. Only audited sensors take part; sensor timers stay vanilla,
            including when brain ticks are skipped by DAB or EAR. Reads happen at the start of the entity phase
            instead of during each mob's own tick.""", """
            在实体刻之前并行运行到期的生物传感器 (只读扫描)."""));
        threads = config.getInt(getBasePath() + ".threads", threads, config.pickStringRegionBased("""
            Worker threads; 0 = chunk worker default for this CPU.""", """
            线程数; 0 = 自动."""));
    }
}
