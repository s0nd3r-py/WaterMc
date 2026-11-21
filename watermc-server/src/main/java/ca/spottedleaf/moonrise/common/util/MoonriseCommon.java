package ca.spottedleaf.moonrise.common.util;

import ca.spottedleaf.concurrentutil.executor.thread.BalancedPrioritisedThreadPool;
import ca.spottedleaf.concurrentutil.numa.OSNuma;
import ca.spottedleaf.moonrise.common.PlatformHooks;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class MoonriseCommon {

    private static final Logger LOGGER = LogUtils.getClassLogger();
    // Gale start - metrics - chunk system IO threads
    public static int chunkSystemIOThreads;
    public static int chunkSystemWorkerThreads;
    // Gale end - metrics - chunk system IO threads

    public static final long WORKER_QUEUE_HOLD_TIME = (long)(20.0e6); // 20ms
    public static final BalancedPrioritisedThreadPool WORKER_POOL = new BalancedPrioritisedThreadPool(
        WORKER_QUEUE_HOLD_TIME,
            new Consumer<>() {
                private final AtomicInteger idGenerator = new AtomicInteger();

                @Override
                public void accept(Thread thread) {
                    thread.setDaemon(true);
                    thread.setName(PlatformHooks.get().getBrand() + " Common Worker #" + this.idGenerator.getAndIncrement());
                    if (me.s0nd3r.watermc.core.config.modules.async.ThreadPriorities.enabled) thread.setPriority(me.s0nd3r.watermc.core.config.modules.async.ThreadPriorities.chunkWorkers); // WaterMc - thread priorities
                    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(final Thread thread, final Throwable throwable) {
                            LOGGER.error("Uncaught exception in thread " + thread.getName(), throwable);
                        }
                    });
                }
            }
    );
    public static final BalancedPrioritisedThreadPool.OrderedStreamGroup CLIENT_GROUP = MoonriseCommon.WORKER_POOL.createOrderedStreamGroup();
    public static final BalancedPrioritisedThreadPool.OrderedStreamGroup SERVER_GROUP = MoonriseCommon.WORKER_POOL.createOrderedStreamGroup();

    // WaterMc start - startup boost: all cores generate chunks while the main thread waits for world preparation
    private static int normalWorkerThreads = 1;
    private static boolean workerCountExplicit;
    private static boolean startupBoostActive;
    private static int boostedWorkerThreads;

    public static synchronized void beginStartupBoost() {
        if (startupBoostActive || workerCountExplicit
            || !Boolean.parseBoolean(System.getProperty("watermc.startup-worker-boost", "true"))) {
            return;
        }
        final int boosted = me.s0nd3r.watermc.runtime.WorkerThreads.startupBoost(OSNuma.getNativeInstance().getTotalCores(), normalWorkerThreads);
        if (boosted <= normalWorkerThreads) {
            return;
        }
        startupBoostActive = true;
        boostedWorkerThreads = boosted;
        WORKER_POOL.adjustThreadCount(boosted);
        LOGGER.info("Preparing worlds with " + boosted + " chunk worker threads (" + normalWorkerThreads + " after startup)");
    }

    public static synchronized void endStartupBoost() {
        if (!startupBoostActive) {
            return;
        }
        startupBoostActive = false;
        WORKER_POOL.adjustThreadCount(normalWorkerThreads);
    }
    // WaterMc end

    public static synchronized void adjustWorkerThreads(final int configWorkerThreads, final int configIoThreads) {
        // WaterMc start - use the free cores for chunk generation (Moonrise default left 4-thread CPUs with one worker)
        int defaultWorkerThreads = me.s0nd3r.watermc.runtime.WorkerThreads.defaultFor(OSNuma.getNativeInstance().getTotalCores());
        defaultWorkerThreads = me.s0nd3r.watermc.runtime.WorkerThreads.intProperty("WorkerThreadCount", defaultWorkerThreads);
        // WaterMc end

        workerCountExplicit = configWorkerThreads > 0 || me.s0nd3r.watermc.runtime.WorkerThreads.property("WorkerThreadCount") != null; // WaterMc
        int workerThreads = configWorkerThreads;

        if (workerThreads <= 0) {
            workerThreads = defaultWorkerThreads;
        }

        final int ioThreads = Math.max(1, configIoThreads);

        // Gale start - metrics - chunk system IO threads
        chunkSystemIOThreads = ioThreads;
        chunkSystemWorkerThreads = workerThreads;
        // Gale end - metrics - chunk system IO threads

        normalWorkerThreads = workerThreads; // WaterMc
        WORKER_POOL.adjustThreadCount(startupBoostActive ? Math.max(workerThreads, boostedWorkerThreads) : workerThreads); // WaterMc - keep a running startup boost
        IO_POOL.adjustThreadCount(ioThreads);

        LOGGER.info(PlatformHooks.get().getBrand() + " is using " + workerThreads + " worker threads, " + ioThreads + " I/O threads");
    }

    public static final long IO_QUEUE_HOLD_TIME = (long)(25.0e6); // 25ms
    public static final BalancedPrioritisedThreadPool IO_POOL = new BalancedPrioritisedThreadPool(
        IO_QUEUE_HOLD_TIME,
            new Consumer<>() {
                private final AtomicInteger idGenerator = new AtomicInteger();

                @Override
                public void accept(final Thread thread) {
                    thread.setDaemon(true);
                    thread.setName(PlatformHooks.get().getBrand() + " I/O Worker #" + this.idGenerator.getAndIncrement());
                    if (me.s0nd3r.watermc.core.config.modules.async.ThreadPriorities.enabled) thread.setPriority(me.s0nd3r.watermc.core.config.modules.async.ThreadPriorities.ioWorkers); // WaterMc - thread priorities
                    thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                        @Override
                        public void uncaughtException(final Thread thread, final Throwable throwable) {
                            LOGGER.error("Uncaught exception in thread " + thread.getName(), throwable);
                        }
                    });
                }
            }
    );
    public static final BalancedPrioritisedThreadPool.OrderedStreamGroup CLIENT_IO_GROUP = IO_POOL.createOrderedStreamGroup();
    public static final BalancedPrioritisedThreadPool.OrderedStreamGroup SERVER_IO_GROUP = IO_POOL.createOrderedStreamGroup();

    public static void haltExecutors() {
        MoonriseCommon.WORKER_POOL.shutdown(false);
        LOGGER.info("Awaiting termination of worker pool for up to 60s...");
        if (!MoonriseCommon.WORKER_POOL.join(TimeUnit.SECONDS.toMillis(60L))) {
            LOGGER.error("Worker pool did not shut down in time!");
            MoonriseCommon.WORKER_POOL.halt(false);
        }

        MoonriseCommon.IO_POOL.shutdown(false);
        LOGGER.info("Awaiting termination of I/O pool for up to 60s...");
        if (!MoonriseCommon.IO_POOL.join(TimeUnit.SECONDS.toMillis(60L))) {
            LOGGER.error("I/O pool did not shut down in time!");
            MoonriseCommon.IO_POOL.halt(false);
        }
    }

    private MoonriseCommon() {}
}
