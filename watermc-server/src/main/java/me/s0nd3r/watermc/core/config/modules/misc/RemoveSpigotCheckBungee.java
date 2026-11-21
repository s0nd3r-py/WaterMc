package me.s0nd3r.watermc.core.config.modules.misc;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class RemoveSpigotCheckBungee extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName() + ".remove-spigot-check-bungee-config";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath(), enabled, config.pickStringRegionBased("""
                Enable player enter backend server through proxy
                without backend server enabling its bungee mode.""",
            """
                使服务器无需打开 bungee 模式即可让玩家加入后端服务器."""));
    }
}
