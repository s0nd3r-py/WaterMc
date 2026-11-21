package me.s0nd3r.watermc.util;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import me.s0nd3r.watermc.core.util.map.SyncLongOpenHashSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegacyConstructReplacementTest {

    @Test
    void dateFormatsMatchPreviousSimpleDateFormatOutput() {
        final Date date = new Date(1_762_948_800_123L);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT).format(date),
            WaterDateFormats.format(WaterDateFormats.USER_CACHE, date));
        assertEquals(new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z", Locale.ROOT).format(date),
            WaterDateFormats.format(WaterDateFormats.BAN_EXPIRATION, date));
        assertEquals(new SimpleDateFormat("HH:mm:ss", Locale.ROOT).format(date),
            WaterDateFormats.format(WaterDateFormats.CLOCK, date));
    }

    @Test
    void userCacheDatesRoundTripIncludingFilesWrittenBySimpleDateFormat() throws Exception {
        final String legacy = "2026-12-01 23:59:58 +0300";
        final Date expected = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT).parse(legacy);
        assertEquals(expected, WaterDateFormats.parseOffset(WaterDateFormats.USER_CACHE, legacy));

        final Date now = new Date((System.currentTimeMillis() / 1000L) * 1000L);
        assertEquals(now, WaterDateFormats.parseOffset(WaterDateFormats.USER_CACHE, WaterDateFormats.format(WaterDateFormats.USER_CACHE, now)));
        assertThrows(java.time.format.DateTimeParseException.class, () -> WaterDateFormats.parseOffset(WaterDateFormats.USER_CACHE, "garbage"));
    }

    @Test
    void dateFormatsAreSafeUnderConcurrency() throws Exception {
        final ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            final Future<?>[] futures = new Future<?>[8];
            for (int t = 0; t < futures.length; t++) {
                final long base = 1_700_000_000_000L + t * 86_400_000L;
                futures[t] = pool.submit(() -> {
                    for (int i = 0; i < 2_000; i++) {
                        final Date date = new Date(base + i * 1000L);
                        assertEquals(date, WaterDateFormats.parseOffset(WaterDateFormats.USER_CACHE, WaterDateFormats.format(WaterDateFormats.USER_CACHE, date)));
                    }
                    return null;
                });
            }
            for (final Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void syncLongSetBulkOperationsAreReentrant() {
        final SyncLongOpenHashSet set = new SyncLongOpenHashSet();
        set.addAll(LongArrayList.wrap(new long[]{1, 2, 3, 4}));
        assertTrue(set.containsAll(LongArrayList.wrap(new long[]{1, 4})));
        set.removeAll(LongArrayList.wrap(new long[]{2, 3}));
        set.retainAll(LongArrayList.wrap(new long[]{1, 9}));
        assertEquals(1, set.size());
        assertTrue(set.removeIf((java.util.function.LongPredicate) value -> value == 1L));
        assertTrue(set.isEmpty());

        set.add(42L);
        final LongOpenHashSet copy = set.clone();
        assertInstanceOf(SyncLongOpenHashSet.class, copy);
        copy.add(43L);
        assertFalse(set.contains(43L), "clone is independent");
        assertEquals(new LongOpenHashSet(new long[]{42L}), set);
    }

    @Test
    void syncLongSetConcurrentReadersSeeConsistentState() throws Exception {
        final SyncLongOpenHashSet set = new SyncLongOpenHashSet();
        for (long i = 0; i < 1_000; i++) {
            set.add(i * 2);
        }
        final AtomicBoolean stop = new AtomicBoolean();
        final AtomicBoolean failed = new AtomicBoolean();
        final CountDownLatch started = new CountDownLatch(4);
        final ExecutorService pool = Executors.newFixedThreadPool(5);
        try {
            for (int r = 0; r < 4; r++) {
                pool.submit(() -> {
                    started.countDown();
                    while (!stop.get()) {
                        for (long i = 0; i < 1_000; i++) {
                            if (!set.contains(i * 2)) {
                                failed.set(true); // постоянные ключи никогда не удаляются
                            }
                        }
                    }
                });
            }
            started.await(10, TimeUnit.SECONDS);
            final Future<?> writer = pool.submit(() -> {
                for (int round = 0; round < 200; round++) {
                    for (long i = 0; i < 500; i++) {
                        set.add(1_000_001L + i * 2); // нечётные ключи вызывают рост таблицы
                    }
                    for (long i = 0; i < 500; i++) {
                        set.remove(1_000_001L + i * 2);
                    }
                }
            });
            writer.get(60, TimeUnit.SECONDS);
        } finally {
            stop.set(true);
            pool.shutdown();
            assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        }
        assertFalse(failed.get());
        assertEquals(1_000, set.size());
    }
}
