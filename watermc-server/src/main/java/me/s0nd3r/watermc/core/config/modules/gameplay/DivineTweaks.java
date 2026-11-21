package me.s0nd3r.watermc.core.config.modules.gameplay;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Gameplay switches adapted from DivineMC (BX-Team / DivineMC, GPL-3.0). All are off by default. */
public class DivineTweaks extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.GAMEPLAY.getBaseKeyName();
    }

    public static boolean disableLeafDecay = false;
    public static boolean crafter1gtDelay = false;

    @Override
    public void onLoaded() {
        disableLeafDecay = config.getBoolean(getBasePath() + ".disable-leaf-decay", disableLeafDecay, config.pickStringRegionBased("""
            Leaves that lost their log never decay. Distances are still updated as in vanilla, so turning this off
            later lets disconnected leaves decay normally.""", """
            树叶不会腐烂."""));
        crafter1gtDelay = config.getBoolean(getBasePath() + ".crafter-1gt-delay", crafter1gtDelay, config.pickStringRegionBased("""
            The crafter fires 1 game tick after it is powered instead of 4 (DivineMC). Changes redstone timing:
            only enable on servers that want this behaviour.""", """
            合成器在激活后 1 刻而不是 4 刻工作."""));
    }
}
