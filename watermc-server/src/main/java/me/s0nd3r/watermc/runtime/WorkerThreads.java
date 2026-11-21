package me.s0nd3r.watermc.runtime;

/**
 * Default size of the chunk system worker pool (generation, loading, lighting).
 *
 * <p>Moonrise defaults to cores/4 with a minimum of one, so a 4-thread CPU generates chunks on a single
 * thread while the other cores sit idle. WaterMc leaves room for the main thread, Netty and GC, then gives
 * the remaining cores to the chunk system:
 * <ul>
 *   <li>1–2 cores: 1 worker</li>
 *   <li>3–4 cores: 2 workers</li>
 *   <li>5–8 cores: cores − 3</li>
 *   <li>9+ cores: cores / 2 + 1</li>
 * </ul>
 * Explicit configuration ({@code chunk-system.worker-threads}) or the system property always wins.
 */
public final class WorkerThreads {
    private WorkerThreads() {}

    public static int defaultFor(int cores) {
        if (cores <= 2) return 1;
        if (cores <= 4) return 2;
        if (cores <= 8) return cores - 3;
        return cores / 2 + 1;
    }

    /**
     * Worker count while worlds are prepared at startup. The main thread only waits for chunks during that window,
     * so generation may use every core; the pool returns to {@code normal} once the worlds are ready.
     */
    public static int startupBoost(int cores, int normal) {
        return Math.max(normal, Math.max(1, cores));
    }

    /** Reads {@code WaterMc.<name>} first, then the legacy {@code Paper.<name>} spelling. */
    public static String property(String name) {
        String value = System.getProperty("WaterMc." + name);
        return value != null ? value : System.getProperty("Paper." + name);
    }

    public static int intProperty(String name, int fallback) {
        String value = property(name);
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static boolean booleanProperty(String name) {
        return Boolean.parseBoolean(property(name));
    }
}
