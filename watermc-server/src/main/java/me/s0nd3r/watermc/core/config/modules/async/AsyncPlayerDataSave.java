package me.s0nd3r.watermc.core.config.modules.async;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class AsyncPlayerDataSave extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".async-playerdata-save";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        config.addCommentRegionBased(getBasePath(), """
                Make PlayerData saving asynchronously.""",
            """
                异步保存玩家数据.""");

        enabled = config.getBoolean(getBasePath() + ".enabled", enabled);

        if (enabled) {
            me.s0nd3r.watermc.core.async.AsyncPlayerDataSaving.init();
        }
    }
}
