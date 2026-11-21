package me.s0nd3r.watermc.core.config.modules.water;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class ReduceTntFusePackets extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".reduce-packets.tnt-fuse";
    }

    public static boolean enabled = false;
    public static int resyncInterval = 10;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, """
            [Water] Primed TNT updates its fuse every tick, which sends one entity metadata packet
            per TNT per player per tick. The client counts the fuse down on its own, so with this
            enabled the fuse is only resent every 'resync-interval' ticks, or right away when a
            plugin changes it. Server-side fuse, explosions and the API are unchanged.""");
        resyncInterval = Math.max(1, config.getInt(getBasePath() + ".resync-interval", resyncInterval));
    }
}
