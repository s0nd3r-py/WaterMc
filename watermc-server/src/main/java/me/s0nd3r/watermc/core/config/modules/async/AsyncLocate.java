package me.s0nd3r.watermc.core.config.modules.async;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Idea from DivineMC "Async locate command" (BX-Team / DivineMC, GPL-3.0), restricted to safe cases. */
public class AsyncLocate extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".async-locate";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, config.pickStringRegionBased("""
            /locate structure and /locate biome run off the main thread when a player or the console runs them.
            The command then returns 0 and the answer arrives a moment later. Command blocks, functions and
            /execute keep the synchronous version (their result value is used). /locate poi always stays
            synchronous: the POI storage is not thread-safe.""", """
            玩家或控制台执行的 /locate 结构和生物群系查找在主线程之外运行."""));
    }
}
