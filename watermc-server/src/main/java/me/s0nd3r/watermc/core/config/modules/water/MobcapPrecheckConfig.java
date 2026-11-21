package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class MobcapPrecheckConfig extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".mobcap-precheck";
    }

    public static boolean enabled = true;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, """
            [WaterMc] Skip iterating spawning chunks for a mob category when every player in the world
            has already reached the per-player mob cap for it (requires per-player-mob-spawns).""");
    }
}
