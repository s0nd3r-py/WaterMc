package me.s0nd3r.watermc.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.world.phys.AABB;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Связи сущности со списками: случайные операции против эталонной карты «список → номер». */
class EntityBoundsLinksTest {

    static final class RecordingSink implements EntityBoundsSink {
        final Map<Integer, AABB> written = new java.util.HashMap<>();

        @Override
        public void waterWriteBounds(final int index, final AABB bb) {
            this.written.put(index, bb);
        }
    }

    @Test
    void linksMatchReference() {
        final Random random = new Random(20261119L);
        final List<RecordingSink> sinks = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            sinks.add(new RecordingSink());
        }
        final EntityBoundsLinks links = new EntityBoundsLinks();
        final Map<EntityBoundsSink, Integer> reference = new IdentityHashMap<>();
        for (int step = 0; step < 100_000; step++) {
            final RecordingSink sink = sinks.get(random.nextInt(sinks.size()));
            final int index = random.nextInt(64);
            switch (random.nextInt(4)) {
                case 0 -> {
                    links.link(sink, index);
                    reference.put(sink, index);
                }
                case 1 -> {
                    links.move(sink, index);
                    reference.computeIfPresent(sink, (k, v) -> index);
                }
                case 2 -> {
                    links.unlink(sink);
                    reference.remove(sink);
                }
                default -> {
                    final AABB bb = new AABB(step, 0, 0, step + 1, 1, 1);
                    links.write(bb);
                    for (final Map.Entry<EntityBoundsSink, Integer> entry : reference.entrySet()) {
                        assertSame(bb, ((RecordingSink) entry.getKey()).written.get(entry.getValue()));
                    }
                }
            }
            assertEquals(reference.size(), links.count());
            for (final RecordingSink candidate : sinks) {
                assertEquals(reference.getOrDefault(candidate, -1), links.indexIn(candidate));
            }
        }
    }
}
