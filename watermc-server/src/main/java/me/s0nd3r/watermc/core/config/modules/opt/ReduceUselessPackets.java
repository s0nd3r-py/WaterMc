package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class ReduceUselessPackets extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".reduce-packets";
    }

    public static boolean reduceUselessEntityMovePackets = false;
    public static boolean filterClientboundSetEntityMotionPacket = false;
    public static boolean disableUselessParticles = false;

    @Override
    public void onLoaded() {
        reduceUselessEntityMovePackets = config.getBoolean(getBasePath() + ".reduce-entity-move-packets", reduceUselessEntityMovePackets);
        filterClientboundSetEntityMotionPacket = config.getBoolean(getBasePath() + ".reduce-entity-motion-packets", filterClientboundSetEntityMotionPacket);
        disableUselessParticles = config.getBoolean(getBasePath() + ".disable-useless-particles", disableUselessParticles);
    }
}
