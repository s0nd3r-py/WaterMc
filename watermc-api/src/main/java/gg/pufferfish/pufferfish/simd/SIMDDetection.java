// Gale - Pufferfish - SIMD support

package gg.pufferfish.pufferfish.simd;

public class SIMDDetection {

    private static boolean isInitialized = false;
    static int intVectorBitSize;
    static int floatVectorBitSize;
    static int intElementSize;
    static int floatElementSize;
    static boolean testRunCompleted;
    static boolean unsupportingLaneSize;
    static boolean isEnabled;

    @SuppressWarnings("deprecation")
    public static void initialize() {
        try {
            SIMDChecker.initialize();
        } catch (Throwable ignored) {}
    }

    static void setInitialized() {
        isInitialized = true;
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static int intVectorBitSize() {
        return intVectorBitSize;
    }

    public static int floatVectorBitSize() {
        return floatVectorBitSize;
    }

    public static int intElementSize() {
        return intElementSize;
    }

    public static int floatElementSize() {
        return floatElementSize;
    }

    public static boolean testRunCompleted() {
        return testRunCompleted;
    }

    public static boolean unsupportingLaneSize() {
        return unsupportingLaneSize;
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

}
