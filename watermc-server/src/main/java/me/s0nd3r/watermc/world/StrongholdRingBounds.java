package me.s0nd3r.watermc.world;

/**
 * Radial bounds of concentric-ring structures (strongholds). Every ring position lies in an annulus around 0,0
 * that follows from the placement parameters alone, so a chunk outside it can be rejected without waiting for the
 * ring calculation (which blocks chunk generation near spawn on new worlds). Idea from ModernFix (embeddedt,
 * LGPL-3.0); the ring loop below mirrors vanilla {@code ChunkGeneratorStructureState.generateRingPositions}.
 */
public final class StrongholdRingBounds {
    /** Rounding to chunk coordinates (sqrt(0.5)) plus the biome snap of findBiomeHorizontal, ±7 chunks per axis (7*sqrt(2)), rounded up. */
    static final double POSITION_SLACK = 11.0D;

    private StrongholdRingBounds() {
    }

    /** {innerSq, outerSq}: no ring position has a squared chunk distance below innerSq or above outerSq. */
    public static long[] compute(final int distance, final int spread, final int count) {
        if (spread <= 0 || count <= 0) {
            return new long[] {0L, Long.MAX_VALUE}; // vanilla angle becomes NaN with spread 0, positions collapse to the origin
        }
        int ringSpread = spread;
        int inRing = 0;
        int circle = 0;
        int maxCircle = 0;
        for (int i2 = 0; i2 < count; i2++) {
            maxCircle = circle;
            if (++inRing == ringSpread) {
                circle++;
                inRing = 0;
                ringSpread += 2 * ringSpread / (circle + 1);
                ringSpread = Math.min(ringSpread, count - i2);
            }
        }
        final double noise = distance * 2.5D * 0.5D;
        final double inner = Math.max(0.0D, 4.0D * distance - noise - POSITION_SLACK);
        final double outer = 4.0D * distance + 6.0D * distance * maxCircle + noise + POSITION_SLACK;
        return new long[] {(long) Math.floor(inner * inner), (long) Math.ceil(outer * outer)};
    }
}
