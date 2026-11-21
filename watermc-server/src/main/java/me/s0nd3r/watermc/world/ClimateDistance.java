package me.s0nd3r.watermc.world;

import net.minecraft.world.level.biome.Climate;

/**
 * Flat, allocation-free distance for the biome R-tree.
 *
 * <p>Vanilla walks a {@code Parameter[]} and calls {@code Parameter.distance} for each of the 7 climate
 * dimensions; every call dereferences a separate record. Biome lookup runs for every 4×4×4 cell during
 * generation, so the tree search showed up as ~9% of worker time. Here the bounds of a node live in one
 * {@code long[14]} ({@code min0, max0, min1, max1, ...}) and the loop is unrolled. The arithmetic is the
 * same integer math as {@link Climate.Parameter#distance(long)}, so results are bit-identical.
 */
public final class ClimateDistance {
    public static final int DIMENSIONS = 7;

    private ClimateDistance() {}

    public static long[] flatten(Climate.Parameter[] space) {
        if (space.length < DIMENSIONS) {
            throw new IllegalArgumentException("Climate parameter space needs " + DIMENSIONS + " dimensions, got " + space.length);
        }
        long[] bounds = new long[DIMENSIONS * 2];
        for (int i = 0; i < DIMENSIONS; i++) {
            bounds[i * 2] = space[i].min();
            bounds[i * 2 + 1] = space[i].max();
        }
        return bounds;
    }

    private static long axis(long min, long max, long value) {
        long above = value - max;
        long below = min - value;
        long d = above > 0L ? above : Math.max(below, 0L);
        return d * d;
    }

    public static long distance(long[] bounds, long[] values) {
        return axis(bounds[0], bounds[1], values[0])
            + axis(bounds[2], bounds[3], values[1])
            + axis(bounds[4], bounds[5], values[2])
            + axis(bounds[6], bounds[7], values[3])
            + axis(bounds[8], bounds[9], values[4])
            + axis(bounds[10], bounds[11], values[5])
            + axis(bounds[12], bounds[13], values[6]);
    }
}
