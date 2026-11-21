package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class CheckSurvivalBeforeGrowth extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".check-survival-before-growth";
    }

    public static boolean cactusCheckSurvivalBeforeGrowth = false;

    @Override
    public void onLoaded() {
        cactusCheckSurvivalBeforeGrowth = config.getBoolean(getBasePath() + ".cactus-check-survival", cactusCheckSurvivalBeforeGrowth,
            config.pickStringRegionBased("""
                    Check if a cactus can survive before growing.""",
                """
                    在仙人掌生长前检查其是否能够存活。"""));
    }
}
