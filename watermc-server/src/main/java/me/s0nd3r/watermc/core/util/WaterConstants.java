package me.s0nd3r.watermc.core.util;

public final class WaterConstants {

    private WaterConstants() {
    }

    public static final boolean DISABLE_VANILLA_PROFILER = Boolean.getBoolean("Leaf.disable-vanilla-profiler");
    // Water start - ignore Leaf.enableFMA on CPUs without hardware FMA (software Math.fma is ~100x slower)
    public static final boolean ENABLE_FMA = resolveEnableFma();

    private static boolean resolveEnableFma() {
        if (!Boolean.getBoolean("Leaf.enableFMA")) return false;
        if (me.s0nd3r.watermc.util.WaterMath.HARDWARE_FMA) return true;
        org.apache.logging.log4j.LogManager.getLogger("Water").warn("-DLeaf.enableFMA=true ignored: this CPU has no hardware FMA, Math.fma would fall back to a slow software implementation");
        return false;
    }
    // Water end - ignore Leaf.enableFMA on CPUs without hardware FMA
    public static final boolean ENABLE_IO_URING = Boolean.getBoolean("Leaf.enable-io-uring");
    public static final boolean ENABLE_BASE64CODER_WARNING = Boolean.getBoolean("Leaf.enable-base64coder-warning");
    public static final boolean DISABLE_VANILLA_DEBUG_FEATURE = Boolean.getBoolean("Leaf.disable-vanilla-debug-feature");
    public static final String LINEAR_V2_READ_ONLY_FLAG = "Leaf.linear-v2-read-only";
    public static final boolean LINEAR_V2_READ_ONLY = Boolean.getBoolean(LINEAR_V2_READ_ONLY_FLAG);

    public static final String DISABLE_VANILLA_PROFILER_DOCS_URL = "https://www.leafmc.one/docs/config/system-properties#dleaf-disable-vanilla-profiler";
}
