package me.s0nd3r.watermc.core.config.modules.misc;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

public class RemoveChangeNonEditableSignWarning extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName();
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".remove-change-non-editable-sign-warning", enabled,
            config.pickStringRegionBased(
                "Enable to prevent console spam.",
                "移除修改无法编辑的告示牌时输出的警告."
            ));
    }
}
