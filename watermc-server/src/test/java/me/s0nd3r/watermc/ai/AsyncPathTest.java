package me.s0nd3r.watermc.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import me.s0nd3r.watermc.core.async.path.AsyncPath;
import me.s0nd3r.watermc.core.async.path.AsyncPathProcessor;
import me.s0nd3r.watermc.core.config.modules.async.AsyncPathfinding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AsyncPathTest {
    private ThreadPoolExecutor old;
    @BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
    }
    @BeforeEach void executor() {
        old = AsyncPathProcessor.PATH_PROCESSING_EXECUTOR;
        AsyncPathProcessor.PATH_PROCESSING_EXECUTOR = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(32), Thread.ofPlatform().daemon().name("WaterPathTest").factory());
    }
    @AfterEach void cleanup() throws Exception {
        AsyncPathProcessor.PATH_PROCESSING_EXECUTOR.shutdownNow();
        assertTrue(AsyncPathProcessor.PATH_PROCESSING_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS));
        AsyncPathProcessor.PATH_PROCESSING_EXECUTOR = old;
    }
    private PathFinder finder() { return new PathFinder(new WalkNodeEvaluator(), 100); }
    private void barrier() throws Exception { AsyncPathProcessor.PATH_PROCESSING_EXECUTOR.submit(() -> {}).get(10, TimeUnit.SECONDS); }

    @Test void workerPublishesOnceAndCallbacksRunWhenTheOwnerPolls() throws Exception {
        Thread owner = Thread.currentThread();
        AtomicReference<Thread> worker = new AtomicReference<>(), callback = new AtomicReference<>();
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger computations = new AtomicInteger(), callbacks = new AtomicInteger();
        BlockPos target = new BlockPos(1, 0, 0);
        Path expected = new Path(List.of(new Node(0, 0, 0), new Node(1, 0, 0)), target, true);
        AsyncPath path = new AsyncPath(finder(), new ArrayList<>(), Set.of(target), finder -> {
            worker.set(Thread.currentThread()); computations.incrementAndGet();
            try { if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("timed out"); }
            catch (InterruptedException failure) { throw new RuntimeException(failure); }
            return expected;
        });
        try {
            assertFalse(path.isProcessed());
            AsyncPathProcessor.awaitProcessing(path, result -> { assertSame(path, result); callbacks.incrementAndGet(); callback.set(Thread.currentThread()); });
        } finally { release.countDown(); }
        barrier(); assertNull(callback.get());
        assertTrue(path.isProcessed()); assertSame(owner, callback.get());
        assertNotSame(owner, worker.get()); assertFalse(worker.get().isVirtual());
        assertEquals(2, path.getNodeCount()); assertTrue(path.canReach()); assertEquals(1, computations.get()); assertEquals(1, callbacks.get());
        path.isDone(); path.getTarget(); assertEquals(1, callbacks.get());
    }

    @Test void nullAndFailedSearchesBecomeTerminalWithoutRecomputation() throws Exception {
        for (boolean fail : new boolean[]{false, true}) {
            AtomicInteger computations = new AtomicInteger(), callbacks = new AtomicInteger();
            AsyncPath path = new AsyncPath(finder(), new ArrayList<>(), Set.of(BlockPos.ZERO), finder -> {
                computations.incrementAndGet(); if (fail) throw new IllegalStateException("expected test failure"); return null;
            });
            barrier();
            AsyncPathProcessor.awaitProcessing(path, result -> { assertNull(result); callbacks.incrementAndGet(); });
            assertTrue(path.isProcessed()); assertTrue(path.isDone()); assertFalse(path.canReach()); assertEquals(0, path.getNodeCount());
            path.getTarget(); path.getDistToTarget();
            assertEquals(1, computations.get()); assertEquals(1, callbacks.get());
        }
    }

    @Test void targetIdentityIsIndependentOfMutableCallerPositions() throws Exception {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(2, 3, 4);
        Set<BlockPos> targets = new it.unimi.dsi.fastutil.objects.ObjectOpenHashSet<>(); targets.add(mutable);
        AsyncPath path = new AsyncPath(finder(), new ArrayList<>(), targets, finder -> new Path(List.of(), new BlockPos(2, 3, 4), false));
        mutable.set(9, 9, 9); targets.clear(); barrier();
        assertTrue(path.hasSameProcessingPositions(Set.of(new BlockPos(2, 3, 4))));
        assertFalse(path.hasSameProcessingPositions(targets));
    }

    @Test void productionRejectionPolicyDoesNotReturnAnUnfinishablePathAfterShutdown() {
        int oldMax = AsyncPathfinding.asyncPathfindingMaxThreads, oldSize = AsyncPathfinding.asyncPathfindingQueueSize;
        try {
            AsyncPathProcessor.PATH_PROCESSING_EXECUTOR.shutdownNow(); AsyncPathProcessor.PATH_PROCESSING_EXECUTOR = null;
            AsyncPathfinding.asyncPathfindingMaxThreads = 1; AsyncPathfinding.asyncPathfindingQueueSize = 32;
            AsyncPathProcessor.init(); AsyncPathProcessor.PATH_PROCESSING_EXECUTOR.shutdown();
            assertThrows(RejectedExecutionException.class, () -> new AsyncPath(finder(), new ArrayList<>(), Set.of(BlockPos.ZERO), finder -> null));
        } finally { AsyncPathfinding.asyncPathfindingMaxThreads = oldMax; AsyncPathfinding.asyncPathfindingQueueSize = oldSize; }
    }
}
