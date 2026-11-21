package me.s0nd3r.watermc.util;

import java.util.concurrent.locks.LockSupport;

/** Coarse park plus a bounded final spin; the caller retains its absolute tick deadline and task polling. */
public final class TickWait {
    public static final long SPIN_GUARD_NANOS = Math.max(0L, Math.min(500_000L, Long.getLong("watermc.tick-spin-nanos", 50_000L)));
    private TickWait() {}
    public static long coarseParkNanos(long remaining) { return remaining <= SPIN_GUARD_NANOS ? 0L : remaining - SPIN_GUARD_NANOS; }
    public static void pause(Object blocker, long remaining) {
        if (remaining <= 0) return;
        long coarse = coarseParkNanos(remaining);
        if (coarse > 0) LockSupport.parkNanos(blocker, coarse);
        else Thread.onSpinWait();
    }
}
