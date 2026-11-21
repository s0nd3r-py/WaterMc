package me.s0nd3r.watermc.ai;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.Target;
import me.s0nd3r.watermc.core.async.path.AsyncPath;
import me.s0nd3r.watermc.core.async.path.AsyncPathProcessor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AsyncAStarEquivalenceTest {
    @Test
    void actualVanillaAStarMatchesItsOwnerExecutionOnTwentyImmutableGraphs() throws Exception {
        ThreadPoolExecutor old = AsyncPathProcessor.PATH_PROCESSING_EXECUTOR;
        ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(32), Thread.ofPlatform().daemon().name("WaterAStarTest").factory());
        AtomicReference<Thread> worker = new AtomicReference<>();
        try {
            AsyncPathProcessor.PATH_PROCESSING_EXECUTOR = pool;
            Random random = new Random(20261111);
            for (int scenario = 0; scenario < 20; ++scenario) {
                boolean[][] walls = new boolean[8][8];
                if (scenario == 0) for (int z = 0; z < 8; ++z) walls[4][z] = true;
                if (scenario > 1) for (int x = 0; x < 8; ++x) for (int z = 0; z < 8; ++z) walls[x][z] = random.nextInt(4) == 0;
                walls[0][0] = false; walls[7][7] = false;
                Path expected = search(new PathFinder(new Grid(walls), 512), new Grid(walls));
                if (scenario == 0) assertFalse(expected.canReach());
                if (scenario == 1) assertTrue(expected.canReach());
                AsyncPath actual = new AsyncPath(new PathFinder(new Grid(walls), 512), new ArrayList<>(), Set.of(new BlockPos(7, 0, 7)), finder -> {
                    worker.set(Thread.currentThread()); return search(finder, new Grid(walls));
                });
                pool.submit(() -> {}).get(10, TimeUnit.SECONDS);
                assertTrue(actual.isProcessed());
                assertEquals(expected.canReach(), actual.canReach());
                assertEquals(expected.getDistToTarget(), actual.getDistToTarget());
                assertEquals(expected.getNodeCount(), actual.getNodeCount());
                for (int i = 0; i < expected.getNodeCount(); ++i) {
                    Node a = expected.getNode(i), b = actual.getNode(i);
                    assertEquals(a.x, b.x); assertEquals(a.y, b.y); assertEquals(a.z, b.z);
                }
            }
            assertNotSame(Thread.currentThread(), worker.get()); assertFalse(worker.get().isVirtual());
            System.out.println("WATER_ASYNC_ASTAR_DIFFERENTIAL grids=20 matched=true");
        } finally {
            AsyncPathProcessor.PATH_PROCESSING_EXECUTOR = old;
            pool.shutdownNow(); assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static Path search(PathFinder finder, Grid grid) {
        try {
            Method search = PathFinder.class.getDeclaredMethod("findPath", NodeEvaluator.class, BinaryHeap.class, Node[].class,
                int.class, BooleanSupplier.class, Node.class, List.class, float.class, int.class, float.class);
            search.setAccessible(true);
            List<Map.Entry<Target, BlockPos>> targets = List.of(Map.entry(grid.getTarget(7, 0, 7), new BlockPos(7, 0, 7)));
            return (Path) search.invoke(finder, grid, new BinaryHeap(), new Node[32], 512, (BooleanSupplier) () -> false,
                grid.getStart(), targets, 128.0F, 0, 1.0F);
        } catch (ReflectiveOperationException failure) { throw new IllegalStateException(failure); }
    }

    private static final class Grid extends NodeEvaluator {
        private final boolean[][] walls;
        Grid(boolean[][] walls) { this.walls = walls; }
        @Override public Node getStart() { return this.getNode(0, 0, 0); }
        @Override public Target getTarget(double x, double y, double z) { return this.getTargetNodeAt(x, y, z); }
        @Override public int getNeighbors(Node[] output, Node node) {
            int count = 0;
            count = this.add(output, count, node.x + 1, node.z);
            count = this.add(output, count, node.x, node.z + 1);
            count = this.add(output, count, node.x - 1, node.z);
            return this.add(output, count, node.x, node.z - 1);
        }
        private int add(Node[] output, int count, int x, int z) {
            if (x >= 0 && x < 8 && z >= 0 && z < 8 && !this.walls[x][z]) {
                Node node = this.getNode(x, 0, z); node.type = PathType.WALKABLE;
                if (!node.closed) output[count++] = node;
            }
            return count;
        }
        @Override public PathType getPathTypeOfMob(PathfindingContext context, int x, int y, int z, Mob mob) { return PathType.WALKABLE; }
        @Override public PathType getPathType(PathfindingContext context, int x, int y, int z) { return PathType.WALKABLE; }
    }
}
