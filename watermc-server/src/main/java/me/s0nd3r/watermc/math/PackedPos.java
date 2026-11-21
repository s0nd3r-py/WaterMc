package me.s0nd3r.watermc.math;

/** Minecraft's signed 26/12/26 block coordinate layout, without position objects. */
public final class PackedPos {
    public static final int MIN_HORIZONTAL = -33_554_432;
    public static final int MAX_HORIZONTAL = 33_554_431;
    public static final int MIN_Y = -2_048;
    public static final int MAX_Y = 2_047;

    private PackedPos() {}

    /** Matches BlockPos: coordinates outside the representable range wrap. */
    public static long pack(int x, int y, int z) {
        return ((long) x & 0x3FFFFFFL) << 38
            | ((long) z & 0x3FFFFFFL) << 12 | ((long) y & 0xFFFL);
    }

    public static int x(long pos) { return (int) (pos >> 38); }
    public static int y(long pos) { return (int) (pos << 52 >> 52); }
    public static int z(long pos) { return (int) (pos << 26 >> 38); }

    public static long offset(long pos, int dx, int dy, int dz) {
        return pack(x(pos) + dx, y(pos) + dy, z(pos) + dz);
    }

    /** Coordinates must already be local (0..15), as in NMS section access. */
    public static int sectionIndex(int x, int y, int z) {
        return y << 8 | z << 4 | x;
    }
}
