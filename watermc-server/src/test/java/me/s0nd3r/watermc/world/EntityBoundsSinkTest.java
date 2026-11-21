package me.s0nd3r.watermc.world;

import java.util.Random;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Проверка пересечения по плоской копии совпадает с {@link AABB#intersects(AABB)} бит в бит. */
class EntityBoundsSinkTest {

    @Test
    void packedComparisonMatchesAabbIntersects() {
        final Random random = new Random(20261119L);
        final double[] bounds = new double[8 * EntityBoundsSink.STRIDE];
        for (int iteration = 0; iteration < 500_000; iteration++) {
            final AABB entity = randomBox(random);
            final AABB query = random.nextInt(8) == 0 ? entity : randomBox(random);
            final int index = random.nextInt(8);
            EntityBoundsSink.write(bounds, index, entity);
            final boolean disjoint = EntityBoundsSink.disjoint(bounds, index, query.minX, query.minY, query.minZ, query.maxX, query.maxY, query.maxZ);
            assertEquals(!entity.intersects(query), disjoint, () -> entity + " vs " + query);
        }
    }

    @Test
    void copyIsExact() {
        final double[] bounds = new double[EntityBoundsSink.STRIDE];
        final AABB box = new AABB(-0.30000000000000004, 63.99999999, Math.nextUp(1.0), 0.3, 65.4, Double.MIN_VALUE);
        EntityBoundsSink.write(bounds, 0, box);
        assertArrayEquals(new double[]{box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ}, bounds);
    }

    private static AABB randomBox(final Random random) {
        // мелкая сетка, чтобы часто попадать в касание граней (граница — строгое неравенство)
        final double x = random.nextInt(16) * 0.25, y = random.nextInt(16) * 0.25, z = random.nextInt(16) * 0.25;
        return new AABB(x, y, z, x + random.nextInt(8) * 0.25, y + random.nextInt(8) * 0.25, z + random.nextInt(8) * 0.25);
    }
}
