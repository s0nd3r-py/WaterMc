package me.s0nd3r.watermc.diagnostics;

import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watchdog for Netty event loops: the network counterpart of the main-thread watchdog.
 *
 * <p>Once per {@link #intervalMillis} every event loop gets a tiny heartbeat task. A loop that has not run its
 * heartbeat for {@link #stallMillis} is stuck (a blocking call in a packet handler, a deadlock, an endless loop
 * in a plugin's channel handler): every player on that loop stops receiving and sending packets. The watchdog
 * then logs the stack of the stuck thread, once per stall, and logs again when the loop recovers.</p>
 *
 * <p>Cost: one queued runnable per event loop per second. The monitor is a single daemon thread that only reads
 * timestamps; it never touches channels.</p>
 */
public final class NettyWatchdog {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");

    public static volatile boolean enabled = true;
    public static volatile long stallMillis = 10_000L;
    public static volatile long intervalMillis = 1_000L;
    public static volatile boolean printStack = true;

    private static final List<Loop> LOOPS = new CopyOnWriteArrayList<>();
    private static final Set<EventLoopGroup> GROUPS = ConcurrentHashMap.newKeySet();
    private static Thread monitor;

    private NettyWatchdog() {
    }

    static final class Loop {
        final EventExecutor executor;
        volatile Thread thread;
        /** Time the outstanding heartbeat was queued; 0 when none is outstanding. */
        volatile long queuedAt;
        volatile boolean reported;

        Loop(final EventExecutor executor) {
            this.executor = executor;
        }
    }

    /** Starts watching every event loop of the group. Idempotent per group. */
    public static synchronized void watch(final EventLoopGroup group) {
        if (!GROUPS.add(group)) {
            return;
        }
        for (final EventExecutor executor : group) {
            LOOPS.add(new Loop(executor));
        }
        if (monitor == null) {
            monitor = new Thread(NettyWatchdog::run, "WaterMc Netty Watchdog");
            monitor.setDaemon(true);
            monitor.setPriority(Thread.NORM_PRIORITY + 1);
            monitor.start();
        }
    }

    private static void run() {
        while (true) {
            try {
                Thread.sleep(Math.max(100L, intervalMillis));
            } catch (final InterruptedException ex) {
                return;
            }
            if (!enabled) {
                continue;
            }
            check(System.nanoTime() / 1_000_000L);
        }
    }

    static void check(final long nowMillis) {
        for (final Loop loop : LOOPS) {
            if (loop.executor.isShuttingDown()) {
                continue;
            }
            final long queued = loop.queuedAt;
            if (queued == 0L) {
                loop.queuedAt = nowMillis;
                try {
                    loop.executor.execute(() -> heartbeat(loop));
                } catch (final RuntimeException rejected) {
                    loop.queuedAt = 0L; // shutting down
                }
            } else if (!loop.reported && nowMillis - queued >= stallMillis) {
                loop.reported = true;
                report(loop, nowMillis - queued);
            }
        }
    }

    private static void heartbeat(final Loop loop) {
        loop.thread = Thread.currentThread();
        final long queued = loop.queuedAt;
        loop.queuedAt = 0L;
        if (loop.reported) {
            loop.reported = false;
            LOGGER.warn("Netty event loop {} recovered after {} ms", loop.thread.getName(), System.nanoTime() / 1_000_000L - queued);
        }
    }

    private static void report(final Loop loop, final long stalledMillis) {
        final Thread thread = loop.thread;
        final String name = thread == null ? String.valueOf(loop.executor) : thread.getName();
        LOGGER.error("Netty event loop {} has not responded for {} ms: players on it receive and send no packets", name, stalledMillis);
        if (printStack && thread != null) {
            final StringBuilder stack = new StringBuilder();
            for (final StackTraceElement element : thread.getStackTrace()) {
                stack.append("\n\tat ").append(element);
            }
            LOGGER.error("Stack of {} (state {}):{}", name, thread.getState(), stack);
        }
    }

    static List<Loop> loops() {
        return LOOPS;
    }
}
