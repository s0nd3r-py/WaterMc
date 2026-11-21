// Ported from DivineMC ver/26.3 (BX-Team / DivineMC, GPL-3.0): server-side port of tr7zw's EntityCulling
// (occlusion library: LogisticsCraft/OcclusionCulling, MIT). Adapted to WaterMc 1.21.11, off by default.
package me.s0nd3r.watermc.culling;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** Daemon, below-normal-priority threads named "WaterMc Entity Culling #n". */
final class WaterCullingThreadFactory implements ThreadFactory {
    private final AtomicInteger id = new AtomicInteger();

    @Override
    public Thread newThread(final Runnable run) {
        final Thread thread = new Thread(run, "WaterMc Entity Culling #" + this.id.incrementAndGet());
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 2);
        return thread;
    }
}
