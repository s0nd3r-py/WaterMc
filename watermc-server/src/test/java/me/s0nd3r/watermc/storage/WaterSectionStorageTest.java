package me.s0nd3r.watermc.storage;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.util.SimpleBitStorage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaterSectionStorageTest {
    @Test
    void uniformSectionsMaterializeOnlyWhenChangedAndCopiesAreIndependent() {
        WaterSectionStorage flat = new WaterSectionStorage(16, 7);
        assertTrue(flat.isCompact());
        assertEquals(7, flat.getAndSet(123, 7));
        flat.set(20, 7);
        assertTrue(flat.isCompact());
        assertEquals(7, flat.getAndSet(123, 42));
        assertFalse(flat.isCompact());
        assertEquals(42, flat.get(123));
        assertEquals(7, flat.get(124));
        WaterSectionStorage copy = flat.copy();
        copy.set(123, 99);
        assertEquals(42, flat.get(123));
        assertEquals(99, copy.get(123));
        assertTrue(new WaterSectionStorage(16, 7).copy().isCompact());
    }

    @Test
    void randomEditsAndEncodedSnapshotsMatchVanillaBitStorage() {
        Random random = new Random(20261110);
        for (int bits : new int[]{1, 4, 9, 15, 16, 31, 32}) {
            int bound = bits >= 31 ? Integer.MAX_VALUE : (1 << bits);
            WaterSectionStorage flat = new WaterSectionStorage(bits, 0);
            SimpleBitStorage reference = new SimpleBitStorage(bits, 4096);
            for (int i = 0; i < 20_000; ++i) {
                int index = random.nextInt(4096), value = random.nextInt(bound);
                assertEquals(reference.getAndSet(index, value), flat.getAndSet(index, value));
            }
            assertArrayEquals(reference.getRaw(), flat.getRaw());
            assertEquals(reference.getRaw().length, flat.encodedLength());
            int[] unpacked = new int[4096];
            flat.unpack(unpacked);
            for (int i = 0; i < 4096; ++i) assertEquals(reference.get(i), unpacked[i]);
            long[] snapshot = flat.getRaw();
            snapshot[0] = -1;
            assertEquals(reference.get(0), flat.get(0));
        }
    }

    @Test
    void rejectsInvalidIndicesAndStateIds() {
        assertThrows(IllegalArgumentException.class, () -> new WaterSectionStorage(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new WaterSectionStorage(33, 0));
        assertThrows(IllegalArgumentException.class, () -> new WaterSectionStorage(4, 16));
        WaterSectionStorage flat = new WaterSectionStorage(4, 0);
        assertThrows(IndexOutOfBoundsException.class, () -> flat.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> flat.set(4096, 1));
        assertThrows(IllegalArgumentException.class, () -> flat.set(0, -1));
        assertThrows(IllegalArgumentException.class, () -> flat.getAndSet(0, 16));
        assertThrows(IndexOutOfBoundsException.class, () -> flat.unpack(new int[4095]));
    }

    @Test
    void concurrentFirstWritesAndAdjacentCellsDoNotOverwriteEachOther() throws Exception {
        for (int round = 0; round < 30; ++round) {
            WaterSectionStorage flat = new WaterSectionStorage(16, 0);
            CompletableFuture<Void> start = new CompletableFuture<>();
            CompletableFuture<Void> first = new CompletableFuture<>(), second = new CompletableFuture<>();
            Thread a = worker(flat, 0, start, first);
            Thread b = worker(flat, 1, start, second);
            start.complete(null);
            CompletableFuture.allOf(first, second).get(10, TimeUnit.SECONDS);
            a.join(); b.join();
            for (int i = 0; i < 4096; ++i) assertEquals(i + 1, flat.get(i));
        }
    }

    private static volatile long allocationSink;

    @Test
    void steadyStateReadsAndMutationsAllocateNoBytes() {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        org.junit.jupiter.api.Assumptions.assumeTrue(bean.isThreadAllocatedMemorySupported());
        bean.setThreadAllocatedMemoryEnabled(true);
        for (boolean compact : new boolean[]{true, false}) {
            WaterSectionStorage flat = new WaterSectionStorage(16, 7);
            if (!compact) flat.set(0, 8);
            for (int i = 0; i < 20; ++i) measureAllocations(bean, flat, compact);
            for (int i = 0; i < 5; ++i) assertEquals(0, measureAllocations(bean, flat, compact));
            assertEquals(compact, flat.isCompact());
        }
    }

    private static long measureAllocations(com.sun.management.ThreadMXBean bean, WaterSectionStorage flat, boolean compact) {
        long before = bean.getCurrentThreadAllocatedBytes();
        allocationLoop(flat, compact);
        return bean.getCurrentThreadAllocatedBytes() - before;
    }

    private static void allocationLoop(WaterSectionStorage flat, boolean compact) {
        long sum = 0;
        for (int i = 0; i < 1_000_000; ++i) {
            int index = i & 4095;
            sum += flat.get(index);
            flat.set(index, compact ? 7 : i & 65535);
            sum += flat.getAndSet(index, compact ? 7 : (i + 1) & 65535);
        }
        allocationSink = sum;
    }

    private static Thread worker(WaterSectionStorage flat, int parity, CompletableFuture<Void> start, CompletableFuture<Void> done) {
        return Thread.ofPlatform().start(() -> {
            try {
                start.join();
                for (int i = parity; i < 4096; i += 2) assertEquals(0, flat.getAndSet(i, i + 1));
                done.complete(null);
            } catch (Throwable failure) { done.completeExceptionally(failure); }
        });
    }
}
