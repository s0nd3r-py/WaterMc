package me.s0nd3r.watermc.storage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Согласованная точка сохранения игроков и мира.
 *
 * <p>Асинхронная запись данных игроков (Leaf {@code AsyncPlayerDataSaving}) идёт через однопоточный пул,
 * а чанки — через отдельный RegionFile I/O. Если при остановке или {@code /save-all flush} чанки
 * сбрасываются раньше, чем дописались файлы игроков, аварийное завершение в этот момент оставляет
 * контейнеры в новом состоянии, а инвентарь — в старом (дюп). Барьер дожидается всех ранее поставленных
 * записей игроков (FIFO-маркер в том же однопоточном пуле) и журналов {@code data/*.wdb} до сброса чанков.</p>
 */
public final class PersistenceBarrier {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");

    private PersistenceBarrier() {
    }

    /**
     * Ждёт, пока выполнятся все задачи, поставленные в {@code executor} до вызова.
     * Корректно только для однопоточного FIFO-пула.
     *
     * @return {@code true}, если очередь опустела в пределах таймаута
     */
    public static boolean awaitQueued(final ExecutorService executor, final long timeout, final TimeUnit unit) {
        if (executor == null || executor.isShutdown()) {
            return true;
        }
        final Future<?> marker;
        try {
            marker = executor.submit(() -> { });
        } catch (final java.util.concurrent.RejectedExecutionException ex) {
            return true;
        }
        try {
            marker.get(timeout, unit);
            return true;
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final TimeoutException ex) {
            LOGGER.warn("Player data writes did not finish within {} {}", timeout, unit);
            return false;
        } catch (final Exception ex) {
            LOGGER.error("Error while waiting for player data writes", ex);
            return false;
        }
    }

    /** Дожидается записи игроков и журналов data/*.wdb перед сбросом чанков. */
    public static void beforeWorldFlush(final long timeout, final TimeUnit unit) {
        awaitQueued(me.s0nd3r.watermc.core.async.AsyncPlayerDataSaving.IO_POOL, timeout, unit);
        WaterRecordLog.flushAll(timeout, unit);
    }
}
