package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public final class VanillaParticleBudget extends ConfigModules {
    public static boolean enabled = false;
    public static int maxPacketsPerTick = 128;
    public static int maxParticlesPerTick = 4096;

    public String getBasePath() {
        return EnumConfigCategory.NETWORK.getBaseKeyName() + ".vanilla-particle-budget";
    }

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, """
            [WaterMc] Limits normal vanilla particle packets sent to each player per server tick.
            Packets exceeding either allowance are skipped, not queued. Count=0 costs one particle.
            Bukkit/Paper plugin particles, targeted effects, vibration, forced and always-show
            particles are exempt.
            This changes visual effects only; disabled by default. It does not limit level-event
            particles, explosion packets, entity effects, or packets sent directly by plugins.""");
        maxPacketsPerTick = Math.max(1, config.getInt(getBasePath() + ".max-packets-per-tick", maxPacketsPerTick));
        maxParticlesPerTick = Math.max(1, config.getInt(getBasePath() + ".max-particles-per-tick", maxParticlesPerTick));
    }
}
