package me.s0nd3r.watermc.core.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Кэш соседних чанков не должен возвращать устаревшее значение ни при какой последовательности операций. */
class ChunkCacheNeighbourhoodTest {

    private static long key(final int x, final int z) {
        return ((long) x & 0xFFFFFFFFL) | (((long) z & 0xFFFFFFFFL) << 32);
    }

    @Test
    void matchesReferenceMap() {
        final Random random = new Random(20261119L);
        final ChunkCache<String> cache = new ChunkCache<>(Thread.currentThread());
        final Map<Long, String> reference = new HashMap<>();
        for (int step = 0; step < 300_000; step++) {
            // небольшая область вокруг начала координат, включая отрицательные и ключ 0
            final long k = key(random.nextInt(8) - 4, random.nextInt(8) - 4);
            switch (random.nextInt(8)) {
                case 0 -> {
                    final String v = "v" + step;
                    assertEquals(reference.put(k, v), cache.put(k, v));
                }
                case 1 -> assertEquals(reference.remove(k), cache.remove(k));
                case 2 -> {
                    if (random.nextInt(200) == 0) {
                        cache.clear();
                        reference.clear();
                    }
                }
                default -> assertEquals(reference.get(k), cache.get(k), "step " + step);
            }
        }
    }

    @Test
    void cornerChunksUseDistinctSlots() {
        assertEquals(4, java.util.stream.LongStream.of(key(0, 0), key(1, 0), key(0, 1), key(1, 1))
            .mapToInt(ChunkCache::slot).distinct().count());
        assertEquals(4, java.util.stream.LongStream.of(key(-1, -1), key(0, -1), key(-1, 0), key(0, 0))
            .mapToInt(ChunkCache::slot).distinct().count());
    }
}
