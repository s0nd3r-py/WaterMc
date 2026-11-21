package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Idea from ModernFix "cache_strongholds" (embeddedt, LGPL-3.0), reimplemented. */
public class StrongholdEarlyReject extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".stronghold-early-reject";
    }

    public static boolean enabled = true; // result-identical (StrongholdRingBoundsTest), 2-3x faster spawn selection on new worlds

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, config.pickStringRegionBased("""
            Chunks that are too close to or too far from 0,0 to ever hold a stronghold skip the stronghold check
            instead of waiting for the stronghold ring calculation. Speeds up spawn preparation on new worlds;
            strongholds end up exactly where they would without it.""", """
            跳过不可能生成要塞的区块的要塞检查, 加快新世界出生点准备."""));
    }
}
