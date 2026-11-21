package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;
import me.s0nd3r.watermc.core.config.annotations.Experimental;

public class OptimizeNoActionTime extends ConfigModules {
    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".optimize-no-action-time";
    }

    @Experimental
    public static boolean disableLightCheck = false;

    @Override
    public void onLoaded() {
        disableLightCheck = config.getBoolean(getBasePath() + ".disable-light-check", disableLightCheck);
    }
}
