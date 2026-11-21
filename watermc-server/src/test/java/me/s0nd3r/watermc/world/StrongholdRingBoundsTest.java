package me.s0nd3r.watermc.world;

import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Every position the vanilla ring loop can produce (with the worst biome snap) lies inside the bounds. */
class StrongholdRingBoundsTest {

    @Test
    void vanillaRingPositionsStayInsideBounds() {
        final Random params = new Random(42);
        for (int run = 0; run < 3000; run++) {
            final int distance = run == 0 ? 32 : params.nextInt(1024);
            final int spread = run == 0 ? 3 : 1 + params.nextInt(64);
            final int count = run == 0 ? 128 : 1 + params.nextInt(400);
            final long[] bounds = StrongholdRingBounds.compute(distance, spread, count);
            final Random random = new Random(params.nextLong());
            double d = random.nextDouble() * Math.PI * 2.0;
            int i = 0;
            int i1 = 0;
            int s = spread;
            for (int i2 = 0; i2 < count; i2++) {
                final double d1 = 4 * distance + distance * i1 * 6 + (random.nextDouble() - 0.5) * (distance * 2.5);
                final int x = (int) Math.round(Math.cos(d) * d1) + random.nextInt(15) - 7;
                final int z = (int) Math.round(Math.sin(d) * d1) + random.nextInt(15) - 7;
                final long distSq = (long) x * x + (long) z * z;
                assertTrue(distSq >= bounds[0] && distSq <= bounds[1], "d=" + distance + " s=" + spread + " c=" + count + " at " + x + "," + z);
                d += (Math.PI * 2) / s;
                if (++i == s) {
                    i1++;
                    i = 0;
                    s += 2 * s / (i1 + 1);
                    s = Math.min(s, count - i2);
                    d += random.nextDouble() * Math.PI * 2.0;
                }
            }
        }
    }

    @Test
    void vanillaStrongholdsSkipTheSpawnArea() {
        final long[] bounds = StrongholdRingBounds.compute(32, 3, 128);
        assertTrue(bounds[0] > 70L * 70L, "spawn chunks never wait for the ring calculation");
        assertArrayEquals(new long[] {0L, Long.MAX_VALUE}, StrongholdRingBounds.compute(32, 0, 128));
    }
}
