package me.s0nd3r.watermc.math;

import java.util.Random;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PackedPosTest {
    @Test
    void signedBoundariesAndOverflowMatchMinecraftLayout() {
        int[] values = {Integer.MIN_VALUE, -33_554_433, -33_554_432, -2049, -2048, -1, 0, 1, 2047, 2048, 33_554_431, 33_554_432, Integer.MAX_VALUE};
        for (int x : values) for (int y : values) for (int z : values) verify(x, y, z);
        Random random = new Random(0x5741544552L);
        for (int i = 0; i < 100_000; ++i) verify(random.nextInt(), random.nextInt(), random.nextInt());
    }

    private static void verify(int x, int y, int z) {
        long expected = ((x & 67_108_863L) << 38) | ((z & 67_108_863L) << 12) | (y & 4095L);
        long actual = PackedPos.pack(x, y, z);
        assertEquals(expected, actual);
        assertEquals(x << 6 >> 6, PackedPos.x(actual));
        assertEquals(y << 20 >> 20, PackedPos.y(actual));
        assertEquals(z << 6 >> 6, PackedPos.z(actual));
        assertEquals(expected, BlockPos.asLong(x, y, z));
        assertEquals(BlockPos.getX(actual), PackedPos.x(actual));
        assertEquals(BlockPos.getY(actual), PackedPos.y(actual));
        assertEquals(BlockPos.getZ(actual), PackedPos.z(actual));
    }

    @Test
    void offsetWrapsEachCoordinateIndependently() {
        Random random = new Random(321);
        for (int i = 0; i < 100_000; ++i) {
            long p = random.nextLong();
            int dx = random.nextInt(), dy = random.nextInt(), dz = random.nextInt();
            long expected = PackedPos.pack(PackedPos.x(p) + dx, PackedPos.y(p) + dy, PackedPos.z(p) + dz);
            assertEquals(expected, PackedPos.offset(p, dx, dy, dz));
            assertEquals(expected, BlockPos.offset(p, dx, dy, dz));
        }
    }

    @Test
    void allSectionCellsHaveDistinctYzxIndices() {
        boolean[] seen = new boolean[4096];
        for (int y = 0; y < 16; ++y) for (int z = 0; z < 16; ++z) for (int x = 0; x < 16; ++x) {
            int index = PackedPos.sectionIndex(x, y, z);
            assertFalse(seen[index]);
            seen[index] = true;
            assertEquals(x, index & 15);
            assertEquals(z, index >> 4 & 15);
            assertEquals(y, index >> 8);
        }
    }
}
