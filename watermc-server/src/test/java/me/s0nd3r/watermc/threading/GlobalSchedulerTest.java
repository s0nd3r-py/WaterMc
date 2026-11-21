package me.s0nd3r.watermc.threading;

import io.papermc.paper.threadedregions.scheduler.FoliaGlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalSchedulerTest {
    private static Plugin plugin() {
        Plugin plugin = mock(Plugin.class);
        when(plugin.isEnabled()).thenReturn(true);
        when(plugin.getLogger()).thenReturn(Logger.getLogger("WaterSchedulerTest"));
        when(plugin.getDescription()).thenReturn(new PluginDescriptionFile("WaterSchedulerTest", "1", "Unused"));
        return plugin;
    }

    @Test
    void deadlinesRepeatsAndNestedSubmissionsStayOnTheOwner() {
        FoliaGlobalRegionScheduler scheduler = new FoliaGlobalRegionScheduler();
        Plugin plugin = plugin();
        Thread owner = Thread.currentThread();
        List<String> events = new ArrayList<>();
        ScheduledTask once = scheduler.runDelayed(plugin, task -> {
            assertSame(owner, Thread.currentThread()); events.add("once");
            scheduler.run(plugin, nested -> events.add("nested"));
        }, 2);
        AtomicInteger repeated = new AtomicInteger();
        ScheduledTask repeating = scheduler.runAtFixedRate(plugin, task -> {
            events.add("repeat");
            if (repeated.incrementAndGet() == 2) task.cancel();
        }, 1, 2);
        assertEquals(List.of(), events);
        scheduler.tick(); assertEquals(List.of("repeat"), events);
        scheduler.tick(); assertEquals(List.of("repeat", "once"), events);
        scheduler.tick(); assertEquals(List.of("repeat", "once", "repeat", "nested"), events);
        scheduler.tick(); scheduler.tick();
        assertEquals(4, events.size());
        assertEquals(ScheduledTask.ExecutionState.FINISHED, once.getExecutionState());
        assertEquals(ScheduledTask.ExecutionState.CANCELLED, repeating.getExecutionState());
    }

    @Test
    void manyProducersKeepPerProducerFifoAndDeliverExactlyOnce() throws Exception {
        FoliaGlobalRegionScheduler scheduler = new FoliaGlobalRegionScheduler();
        Plugin plugin = plugin();
        int producers = 8, count = 3000;
        int[] next = new int[producers];
        CompletableFuture<?>[] completed = new CompletableFuture<?>[producers];
        Thread[] threads = new Thread[producers];
        Thread owner = Thread.currentThread();
        AtomicInteger errors = new AtomicInteger();
        for (int p = 0; p < producers; ++p) {
            int producer = p;
            CompletableFuture<Void> done = new CompletableFuture<>(); completed[p] = done;
            threads[p] = Thread.ofPlatform().start(() -> {
                try {
                    for (int i = 0; i < count; ++i) {
                        int sequence = i;
                        scheduler.run(plugin, task -> {
                            if (Thread.currentThread() != owner || next[producer]++ != sequence) errors.incrementAndGet();
                        });
                    }
                    done.complete(null);
                } catch (Throwable failure) { done.completeExceptionally(failure); }
            });
        }
        CompletableFuture.allOf(completed).get(20, TimeUnit.SECONDS);
        for (Thread thread : threads) thread.join();
        scheduler.tick(); scheduler.tick();
        for (int n : next) assertEquals(count, n);
        assertEquals(0, errors.get());
    }

    @Test
    void concurrentSubmissionWhileTickingCannotLoseOverdueTasks() throws Exception {
        FoliaGlobalRegionScheduler scheduler = new FoliaGlobalRegionScheduler();
        Plugin plugin = plugin();
        AtomicInteger delivered = new AtomicInteger();
        CompletableFuture<Void> completed = new CompletableFuture<>();
        Thread producer = Thread.ofPlatform().start(() -> {
            try {
                for (int i = 0; i < 20000; ++i) scheduler.run(plugin, task -> delivered.incrementAndGet());
                completed.complete(null);
            } catch (Throwable failure) { completed.completeExceptionally(failure); }
        });
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while ((!completed.isDone() || delivered.get() != 20000) && System.nanoTime() < deadline) scheduler.tick();
        completed.get(10, TimeUnit.SECONDS); producer.join();
        assertEquals(20000, delivered.get());
    }

    @Test
    void pluginCancellationCoversPendingAndFutureTasksWithoutCancellingOtherPlugins() {
        FoliaGlobalRegionScheduler scheduler = new FoliaGlobalRegionScheduler();
        Plugin first = plugin(), second = plugin();
        AtomicInteger called = new AtomicInteger();
        ScheduledTask pending = scheduler.run(first, task -> called.addAndGet(100));
        ScheduledTask future = scheduler.runDelayed(first, task -> called.addAndGet(1000), 10);
        scheduler.run(second, task -> called.incrementAndGet());
        scheduler.cancelTasks(first);
        assertEquals(ScheduledTask.ExecutionState.CANCELLED, pending.getExecutionState());
        assertEquals(ScheduledTask.ExecutionState.CANCELLED, future.getExecutionState());
        for (int i = 0; i < 12; ++i) scheduler.tick();
        assertEquals(1, called.get());
    }

    @Test
    void cancellationOfExecutingRepeaterPreventsReschedulingAndFinishesItsState() throws Exception {
        FoliaGlobalRegionScheduler scheduler = new FoliaGlobalRegionScheduler();
        Plugin plugin = plugin();
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicInteger called = new AtomicInteger();
        ScheduledTask task = scheduler.runAtFixedRate(plugin, self -> {
            called.incrementAndGet(); entered.countDown();
            try { if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("release timed out"); }
            catch (InterruptedException failure) { throw new RuntimeException(failure); }
        }, 1, 1);
        CompletableFuture<Void> completed = new CompletableFuture<>();
        Thread owner = Thread.ofPlatform().start(() -> {
            try { scheduler.tick(); completed.complete(null); }
            catch (Throwable failure) { completed.completeExceptionally(failure); }
        });
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            scheduler.cancelTasks(plugin);
            assertEquals(ScheduledTask.ExecutionState.CANCELLED_RUNNING, task.getExecutionState());
        } finally { release.countDown(); }
        completed.get(10, TimeUnit.SECONDS); owner.join();
        assertEquals(ScheduledTask.ExecutionState.CANCELLED, task.getExecutionState());
        scheduler.tick(); scheduler.tick();
        assertEquals(1, called.get());
    }

    @Test
    void validationAndFarFutureDelaysRemainSafe() {
        FoliaGlobalRegionScheduler scheduler = new FoliaGlobalRegionScheduler();
        Plugin plugin = plugin();
        assertThrows(IllegalArgumentException.class, () -> scheduler.runDelayed(plugin, task -> {}, 0));
        assertThrows(IllegalArgumentException.class, () -> scheduler.runAtFixedRate(plugin, task -> {}, 1, 0));
        scheduler.tick();
        AtomicInteger called = new AtomicInteger();
        scheduler.runDelayed(plugin, task -> called.incrementAndGet(), Long.MAX_VALUE);
        scheduler.tick(); scheduler.tick(); assertEquals(0, called.get());
        when(plugin.isEnabled()).thenReturn(false);
        assertThrows(IllegalPluginAccessException.class, () -> scheduler.run(plugin, task -> {}));
    }
}
