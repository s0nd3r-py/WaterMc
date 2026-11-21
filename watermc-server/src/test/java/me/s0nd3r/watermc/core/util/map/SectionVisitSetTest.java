package me.s0nd3r.watermc.core.util.map;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Битовое множество посещённых секций ведёт себя как прежний LongOpenHashSet; огромные области — без лишней памяти. */
class SectionVisitSetTest {

    @Test
    void bitSetMatchesHashSetInsideBounds() {
        Random random = new Random(3);
        for (int round = 0; round < 50; round++) {
            int minX = random.nextInt(2000) - 1000, minY = -4, minZ = random.nextInt(2000) - 1000;
            int maxX = minX + random.nextInt(20), maxY = 19, maxZ = minZ + random.nextInt(20);
            SectionVisitSet set = new SectionVisitSet(minX, minY, minZ, maxX, maxY, maxZ);
            assertTrue(set.usesBitSet());
            LongOpenHashSet reference = new LongOpenHashSet();
            for (int i = 0; i < 5000; i++) {
                int x = minX + random.nextInt(maxX - minX + 1), y = minY + random.nextInt(maxY - minY + 1), z = minZ + random.nextInt(maxZ - minZ + 1);
                assertEquals(reference.add(ca.spottedleaf.moonrise.common.util.CoordinateUtils.getChunkSectionKey(x, y, z)), set.mark(x, y, z));
            }
        }
    }

    @Test
    void hugeSearchBoxFallsBackInsteadOfAllocatingOrThrowing() {
        SectionVisitSet set = new SectionVisitSet(-100_000, -4, -100_000, 100_000, 19, 100_000);
        assertFalse(set.usesBitSet());
        assertTrue(set.mark(5, 0, 5));
        assertFalse(set.mark(5, 0, 5));
        assertTrue(set.mark(-99_999, 19, 99_999));
    }
}
