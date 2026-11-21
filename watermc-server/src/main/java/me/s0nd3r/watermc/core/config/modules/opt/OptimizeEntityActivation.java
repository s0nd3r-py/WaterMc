package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Leaf ver/26.3 "optimize entity activation" (hayanesuru, GPL-3.0). */
public class OptimizeEntityActivation extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".optimize-entity-activation";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, config.pickStringRegionBased("""
            Faster entity activation range check: each chunk near players is read once and every entity is matched
            to its nearest player with a k-d tree, instead of one entity list per player. Pays off with many players
            close together. Note: the activation range becomes a circle around the player (Paper uses a square), so
            mobs in the far corners of the old square tick as inactive.""", """
            更快的实体激活范围检查 (激活范围改为圆形)."""));
    }
}
