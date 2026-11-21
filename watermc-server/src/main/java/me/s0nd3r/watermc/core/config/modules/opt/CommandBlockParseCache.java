package me.s0nd3r.watermc.core.config.modules.opt;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Idea from DivineMC "Command block parse results caching" (BX-Team / DivineMC, GPL-3.0), reimplemented. */
public class CommandBlockParseCache extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName() + ".command-block-parse-cache";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, config.pickStringRegionBased("""
            Command blocks keep the parsed form of their command and only re-parse when the command text (after
            ServerCommandEvent) or the command dispatcher changes. Every run gets a fresh command source, so output,
            success count and relative coordinates behave exactly as without the cache.""", """
            命令方块缓存命令解析结果."""));
    }
}
