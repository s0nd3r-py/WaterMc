package me.s0nd3r.watermc.core.config.modules.async;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;

/** Lower priority for chunk and I/O worker threads; idea from threadtweak (getchoo, MIT). */
public class ThreadPriorities extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".thread-priorities";
    }

    public static boolean enabled = false;
    public static int chunkWorkers = Thread.NORM_PRIORITY - 1;
    public static int ioWorkers = Thread.NORM_PRIORITY - 2;

    @Override
    public void onLoaded() {
        final String base = getBasePath();
        enabled = config.getBoolean(base + ".enabled", enabled, config.pickStringRegionBased("""
            Run chunk generation/loading workers and chunk I/O threads below the main server thread, so a busy
            chunk system (world pre-generation, many players exploring) takes CPU from the tick loop last.
            Works on Windows; on Linux the JVM ignores thread priorities unless started as root with
            -XX:ThreadPriorityPolicy=1. Takes effect after a restart.""", """
            让区块工作线程和 I/O 线程的优先级低于主线程."""));
        chunkWorkers = Math.clamp(config.getInt(base + ".chunk-workers", chunkWorkers, "1-10, Java thread priority (main thread = 5)."), Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
        ioWorkers = Math.clamp(config.getInt(base + ".io-workers", ioWorkers, "1-10, Java thread priority."), Thread.MIN_PRIORITY, Thread.MAX_PRIORITY);
    }
}
