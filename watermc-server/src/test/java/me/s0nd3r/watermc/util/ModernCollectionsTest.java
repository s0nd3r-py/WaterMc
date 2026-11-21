package me.s0nd3r.watermc.util;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModernCollectionsTest {
    private enum Key { FIRST, SECOND }
    @Test void identityCachePreservesEqualButDistinctKeysAndPublishesOnce() throws Exception {
        ReadMostlyIdentityCache<String, Object> cache = new ReadMostlyIdentityCache<>();
        String first = new String("same"), second = new String("same");
        Object a = cache.getOrCompute(first, key -> new Object()), b = cache.getOrCompute(second, key -> new Object());
        assertNotSame(a, b); assertSame(a, cache.getOrCompute(first, key -> fail("unexpected load")));
        AtomicInteger loads = new AtomicInteger(); Object shared = new Object();
        CompletableFuture<?>[] done = new CompletableFuture<?>[8]; Thread[] threads = new Thread[8];
        String third = "third";
        for (int i = 0; i < 8; ++i) {
            CompletableFuture<Void> completed = new CompletableFuture<>(); done[i] = completed;
            threads[i] = Thread.ofPlatform().start(() -> {
                try { assertSame(shared, cache.getOrCompute(third, key -> { loads.incrementAndGet(); return shared; })); completed.complete(null); }
                catch (Throwable failure) { completed.completeExceptionally(failure); }
            });
        }
        CompletableFuture.allOf(done).get(10, TimeUnit.SECONDS); for (Thread thread : threads) thread.join(); assertEquals(1, loads.get());
    }
    @Test void ordinalCacheKeepsThePublishedValueForEachEnumConstant() {
        OrdinalCache<Key, Object> cache = new OrdinalCache<>(Key.values().length);
        Object a = cache.getOrCompute(Key.FIRST, key -> new Object());
        assertSame(a, cache.getOrCompute(Key.FIRST, key -> fail("unexpected load")));
        assertNotSame(a, cache.getOrCompute(Key.SECOND, key -> new Object()));
    }
    @Test void primitiveRingMeanMatchesThePreviousChronologicalSumExactly() {
        RollingTpsHistory history = new RollingTpsHistory(40);
        ArrayList<Double> reference = new ArrayList<>(); Random random = new Random(12112026);
        assertEquals(0.1, history.average());
        for (int i = 0; i < 10000; ++i) {
            double value = random.nextDouble(-1.0, 20.0);
            if (reference.size() == 40) reference.removeFirst(); reference.add(value); history.add(value);
            double sum = 0; for (double item : reference) sum += item;
            assertEquals(Double.doubleToLongBits(sum / reference.size()), Double.doubleToLongBits(history.average()));
        }
    }
    @Test void warmedRingAndCacheHitsAllocateZeroBytes() {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory.getThreadMXBean();
        org.junit.jupiter.api.Assumptions.assumeTrue(bean.isThreadAllocatedMemorySupported()); bean.setThreadAllocatedMemoryEnabled(true);
        RollingTpsHistory history = new RollingTpsHistory(40); ReadMostlyIdentityCache<Object, Object> cache = new ReadMostlyIdentityCache<>();
        Object key = new Object(); Function<Object, Object> factory = ignored -> key; cache.getOrCompute(key, factory);
        for (int i = 0; i < 20; ++i) measure(bean, history, cache, key, factory);
        for (int i = 0; i < 5; ++i) assertEquals(0L, measure(bean, history, cache, key, factory));
    }
    private static volatile double sink;
    private static long measure(com.sun.management.ThreadMXBean bean, RollingTpsHistory history, ReadMostlyIdentityCache<Object,Object> cache, Object key, Function<Object,Object> factory) {
        long before = bean.getCurrentThreadAllocatedBytes(); double sum = 0;
        for (int i = 0; i < 10000; ++i) { history.add(i % 20); sum += history.average(); if (cache.getOrCompute(key, factory) != key) throw new AssertionError(); }
        sink = sum; return bean.getCurrentThreadAllocatedBytes() - before;
    }
    @Test void tickWaitUsesTheAbsoluteCallersDeadlineWithoutNegativeParks() {
        long guard = TickWait.SPIN_GUARD_NANOS;
        assertEquals(0, TickWait.coarseParkNanos(-1)); assertEquals(0, TickWait.coarseParkNanos(guard));
        assertEquals(100, TickWait.coarseParkNanos(guard + 100));
        assertEquals(Long.MAX_VALUE - guard, TickWait.coarseParkNanos(Long.MAX_VALUE));
        Thread.currentThread().interrupt();
        try { TickWait.pause(this, 0); assertTrue(Thread.currentThread().isInterrupted()); }
        finally { Thread.interrupted(); }
    }
}
