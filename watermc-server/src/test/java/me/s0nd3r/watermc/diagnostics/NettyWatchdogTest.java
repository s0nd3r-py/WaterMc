package me.s0nd3r.watermc.diagnostics;

import io.netty.channel.DefaultEventLoopGroup;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Зависший event loop обнаруживается один раз за зависание, а после восстановления отметка снимается. */
class NettyWatchdogTest {

    @Test
    void detectsStuckLoopOnceAndClearsAfterRecovery() throws Exception {
        NettyWatchdog.enabled = false; // the monitor thread stays idle; the test drives check() itself
        NettyWatchdog.stallMillis = 5_000L;
        DefaultEventLoopGroup group = new DefaultEventLoopGroup(1);
        try {
            NettyWatchdog.watch(group);
            NettyWatchdog.Loop loop = NettyWatchdog.loops().stream()
                .filter(l -> l.executor == group.iterator().next()).findFirst().orElseThrow();

            CountDownLatch release = new CountDownLatch(1);
            CountDownLatch blocked = new CountDownLatch(1);
            group.execute(() -> {
                blocked.countDown();
                try {
                    release.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
            });
            assertTrue(blocked.await(5, TimeUnit.SECONDS));

            NettyWatchdog.check(1_000L);          // queue heartbeat behind the blocking task
            assertEquals(1_000L, loop.queuedAt);
            NettyWatchdog.check(3_000L);
            assertFalse(loop.reported, "below the stall threshold");
            NettyWatchdog.check(6_000L);
            assertTrue(loop.reported, "stall reported");

            release.countDown();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (loop.queuedAt != 0L && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
            assertEquals(0L, loop.queuedAt, "heartbeat ran after recovery");
            assertFalse(loop.reported, "stall flag cleared");
            assertNotNull(loop.thread);
        } finally {
            group.shutdownGracefully(0, 1, TimeUnit.SECONDS).syncUninterruptibly();
            NettyWatchdog.enabled = true;
        }
    }
}
