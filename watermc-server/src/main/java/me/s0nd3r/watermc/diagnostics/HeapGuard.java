package me.s0nd3r.watermc.diagnostics;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import javax.management.NotificationEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Адаптивная разгрузка памяти.
 *
 * <p>Слушает уведомления JVM о превышении порога заполнения старого поколения <b>после сборки мусора</b>
 * ({@code collectionUsageThreshold}) — это сигнал, что живых данных действительно много, а не просто
 * накопился мусор. При срабатывании выполняет зарегистрированные очистители холодных кэшей
 * (не чаще раза в 30 секунд). Сам GC не вызывается.</p>
 */
public final class HeapGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");
    private static final List<Runnable> RELIEVERS = new CopyOnWriteArrayList<>();
    private static final AtomicLong LAST_RUN = new AtomicLong();
    private static final long COOLDOWN_NANOS = 30_000_000_000L;
    private static volatile boolean installed;

    private HeapGuard() {
    }

    /** Регистрирует очистку холодного кэша; она должна быть потокобезопасной. */
    public static void registerReliever(final Runnable reliever) {
        RELIEVERS.add(reliever);
    }

    public static synchronized void install(final double thresholdFraction) {
        if (installed) {
            return;
        }
        for (final MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getType() == MemoryType.HEAP && pool.isCollectionUsageThresholdSupported() && pool.getUsage().getMax() > 0) {
                final String name = pool.getName().toLowerCase(java.util.Locale.ROOT);
                if (name.contains("old") || name.contains("tenured")) {
                    pool.setCollectionUsageThreshold((long) (pool.getUsage().getMax() * thresholdFraction));
                }
            }
        }
        final NotificationEmitter emitter = (NotificationEmitter) ManagementFactory.getMemoryMXBean();
        emitter.addNotificationListener((notification, handback) -> {
            if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(notification.getType())) {
                relieve("заполнение старого поколения после GC выше " + Math.round(thresholdFraction * 100) + "%");
            }
        }, null, null);
        installed = true;
    }

    /** Запускает очистку; возвращает число выполненных очистителей (0 при кулдауне). */
    public static int relieve(final String reason) {
        final long now = System.nanoTime();
        final long last = LAST_RUN.get();
        if (last != 0L && now - last < COOLDOWN_NANOS) {
            return 0;
        }
        if (!LAST_RUN.compareAndSet(last, now)) {
            return 0;
        }
        int ran = 0;
        for (final Runnable reliever : RELIEVERS) {
            try {
                reliever.run();
                ran++;
            } catch (final RuntimeException ex) {
                LOGGER.warn("Cache cleanup failed", ex);
            }
        }
        LOGGER.warn("Heap guard: {}; cold caches cleared: {}", reason, ran);
        return ran;
    }

    static void resetForTests() {
        RELIEVERS.clear();
        LAST_RUN.set(0L);
    }
}
