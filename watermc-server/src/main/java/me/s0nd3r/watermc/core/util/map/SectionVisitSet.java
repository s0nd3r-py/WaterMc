package me.s0nd3r.watermc.core.util.map;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

/**
 * Visited-section set for the breadth-first POI search in {@code PoiAccess}.
 *
 * <p>Based on Leaf "Optimize PoiAccess visited section tracking" (original patch by HaHaWTH, co-authored by
 * MrlingXD, GPL-3.0-only; attribution preserved as required by its section 7(b) term). The search box is
 * bounded, so a bit per section replaces a hash set and its rehashing.</p>
 *
 * <p>WaterMc change: when the box is very large (a plugin or command searching thousands of blocks) the bit
 * array would cost megabytes or overflow; then the set falls back to the original hash set instead of
 * allocating or throwing.</p>
 */
public final class SectionVisitSet {
    /** Above this many sections (128 KiB of bits) the hash set is cheaper than the bit array. */
    static final long MAX_BITSET_SECTIONS = 1L << 20;

    private final int minX;
    private final int minY;
    private final int minZ;
    private final int sizeX;
    private final int sizeY;
    private final long[] bits;
    private final LongOpenHashSet fallback;

    public SectionVisitSet(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
        final long sizeX = (long) maxX - minX + 1L;
        final long sizeY = (long) maxY - minY + 1L;
        final long sizeZ = (long) maxZ - minZ + 1L;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        if (sizeX <= 0L || sizeY <= 0L || sizeZ <= 0L || sizeX * sizeY > MAX_BITSET_SECTIONS
            || sizeX * sizeY * sizeZ > MAX_BITSET_SECTIONS) {
            this.sizeX = 0;
            this.sizeY = 0;
            this.bits = null;
            this.fallback = new LongOpenHashSet();
        } else {
            this.sizeX = (int) sizeX;
            this.sizeY = (int) sizeY;
            this.bits = new long[(int) ((sizeX * sizeY * sizeZ + 63L) >>> 6)];
            this.fallback = null;
        }
    }

    boolean usesBitSet() {
        return this.bits != null;
    }

    /** Marks a section inside the bounds. @return true if it was not visited before. */
    public boolean mark(final int x, final int y, final int z) {
        if (this.bits == null) {
            return this.fallback.add(ca.spottedleaf.moonrise.common.util.CoordinateUtils.getChunkSectionKey(x, y, z));
        }
        final int index = (((z - this.minZ) * this.sizeX + (x - this.minX)) * this.sizeY) + (y - this.minY);
        final int word = index >>> 6;
        final long mask = 1L << (index & 63);
        final long previous = this.bits[word];
        if ((previous & mask) != 0L) {
            return false;
        }
        this.bits[word] = previous | mask;
        return true;
    }
}
