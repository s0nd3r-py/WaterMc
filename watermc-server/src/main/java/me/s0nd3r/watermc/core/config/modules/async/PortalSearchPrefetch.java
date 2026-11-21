package me.s0nd3r.watermc.core.config.modules.async;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Backport of DivineMC "Async portal pre-search" (BX-Team / DivineMC, GPL-3.0). */
public class PortalSearchPrefetch extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".portal-search-prefetch";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        config.addCommentRegionBased(getBasePath(), """
                When an entity starts standing in a nether portal, load the chunks around the expected exit point
                in the other dimension from disk in the background (low priority, no generation). The portal search
                at teleport time then finds them in memory instead of reading region files on the main thread.""",
            """
                实体进入下界传送门时, 在后台预先从磁盘加载另一维度预计出口附近的区块 (低优先级, 不生成新区块).""");
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled);
    }
}
