package me.s0nd3r.watermc.math;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.Test;
import me.s0nd3r.watermc.math.reference.ReferenceCollisions;
import static org.junit.jupiter.api.Assertions.*;

class FastAABBTest {
    @Test
    void randomMixedCollisionsMatchOriginalBitForBit() {
        Random random = new Random(0xAABB21);
        VoxelShape stairs = Shapes.or(Shapes.box(0, 0, 0, 1, 0.5, 1), Shapes.box(0, 0.5, 0.5, 1, 1, 1));
        try (FastAABB flat = FastAABB.acquire()) {
            for (int test = 0; test < 12_000; ++test) {
                try (FastAABB trial = FastAABB.acquire()) {
                    List<AABB> targets = new ArrayList<>();
                    for (int i = 0, size = random.nextInt(24); i < size; ++i) {
                        double x = random.nextDouble()*6 - 3, y = random.nextDouble()*6 - 3, z = random.nextDouble()*6 - 3;
                        targets.add(new AABB(x, y, z, x + random.nextDouble()*2, y + random.nextDouble()*2, z + random.nextDouble()*2));
                    }
                    trial.addAll(targets);
                    if ((test & 1) != 0) trial.voxels().add(stairs.move(random.nextInt(3) - 1, random.nextInt(3) - 1, random.nextInt(3) - 1));
                    if ((test & 3) == 0) trial.voxels().add(Shapes.block());
                    AABB source = new AABB(-0.3, random.nextDouble()*2 - 1, -0.3, 0.3, 1.8, 0.3);
                    double x = random.nextDouble()*6 - 3, y = random.nextDouble()*6 - 3, z = random.nextDouble()*6 - 3;
                    compare(trial, source, targets, x, y, z);
                }
            }
            assertEquals(0, flat.size(), "Nested workspaces must not change their parent");
        }
    }

    @Test
    void epsilonSignedZeroNonfiniteAndWorldLimitsMatchOriginal() {
        double[] values = {-Double.MAX_VALUE, -30_000_000.0, -1, -1e-7, -1e-8, -0.0, 0.0, 1e-8, 1e-7, 1, 30_000_000.0, Double.MAX_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN};
        List<AABB> targets = List.of(new AABB(-1, -64, -1, 1, 320, 1), new AABB(2, Double.NEGATIVE_INFINITY, 2, 3, Double.POSITIVE_INFINITY, 3));
        try (FastAABB flat = FastAABB.acquire()) {
            flat.addAll(targets);
            for (double x : values) for (double y : values) for (double z : values) {
                compare(flat, new AABB(-0.3, 0, -0.3, 0.3, 1.8, 0.3), targets, x, y, z);
            }
            for (double x : values) {
                compare(flat, new AABB(x, -0.0, x, x + 1, 1, x + 1), targets, 1, -1, 0.5);
            }
        }
    }

    private static void compare(FastAABB flat, AABB source, List<AABB> targets, double x, double y, double z) {
        Vec3 expected = ReferenceCollisions.performCollisions(new Vec3(x, y, z), source, flat.voxels(), targets);
        flat.resolve(x, y, z, source);
        assertEquals(Double.doubleToLongBits(expected.x), Double.doubleToLongBits(flat.resultX()), "X");
        assertEquals(Double.doubleToLongBits(expected.y), Double.doubleToLongBits(flat.resultY()), "Y");
        assertEquals(Double.doubleToLongBits(expected.z), Double.doubleToLongBits(flat.resultZ()), "Z");
    }

    @Test
    void growthReuseAndExceptionalExitClearReferences() {
        FastAABB workspace;
        try (FastAABB flat = FastAABB.acquire()) {
            workspace = flat;
            for (int i = 0; i < 20_000; ++i) flat.add(i, i + 1, i + 2, i + 3, i + 4, i + 5);
            assertEquals(20_000, flat.size());
            assertEquals(20_000.0, flat.minY(19_999));
            flat.entityBoxes().add(new AABB(0, 0, 0, 1, 1, 1));
            flat.voxels().add(Shapes.block());
        }
        assertThrows(IllegalStateException.class, workspace::close);
        try (FastAABB flat = FastAABB.acquire()) {
            assertSame(workspace, flat);
            assertEquals(0, flat.size());
            assertTrue(flat.entityBoxes().isEmpty());
            assertTrue(flat.voxels().isEmpty());
        }
        assertThrows(IllegalArgumentException.class, () -> {
            try (FastAABB flat = FastAABB.acquire()) {
                flat.add(0, 0, 0, 1, 1, 1);
                throw new IllegalArgumentException("test callback failure");
            }
        });
        try (FastAABB flat = FastAABB.acquire()) { assertEquals(0, flat.size()); }
    }

    @Test
    void workspacesAreIsolatedAcrossThreads() throws Exception {
        try (FastAABB parent = FastAABB.acquire()) {
            parent.add(1, 2, 3, 4, 5, 6);
            CompletableFuture<Void> done = new CompletableFuture<>();
            Thread worker = Thread.ofPlatform().start(() -> {
                try (FastAABB other = FastAABB.acquire()) {
                    assertNotSame(parent, other);
                    assertEquals(0, other.size());
                    assertThrows(IllegalStateException.class, parent::close);
                    other.add(0, 0, 0, 1, 1, 1);
                    done.complete(null);
                } catch (Throwable failure) { done.completeExceptionally(failure); }
            });
            done.get(10, TimeUnit.SECONDS);
            worker.join();
            assertEquals(1, parent.size());
            assertEquals(2.0, parent.minY(0));
        }
    }

    @Test
    void warmedPrimitiveResolutionDoesNotAllocate() {
        com.sun.management.ThreadMXBean bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        org.junit.jupiter.api.Assumptions.assumeTrue(bean.isThreadAllocatedMemorySupported());
        bean.setThreadAllocatedMemoryEnabled(true);
        try (FastAABB flat = FastAABB.acquire()) {
            flat.add(-1, -1, -1, 1, 0, 1);
            AABB box = new AABB(-0.3, 0, -0.3, 0.3, 1.8, 0.3);
            for (int i = 0; i < 100_000; ++i) flat.resolve(0.1, -0.1, 0.2, box);
            // Warm the accounting call site and its measurement method as well as the solver.
            for (int i = 0; i < 20; ++i) measureAllocations(bean, flat, box, 10_000);
            for (int sample = 0; sample < 5; ++sample) {
                assertEquals(0L, measureAllocations(bean, flat, box, 100_000),
                    "Primitive collision resolution must not allocate after warmup");
            }
        }
    }
    private static long measureAllocations(com.sun.management.ThreadMXBean bean, FastAABB flat, AABB box, int count) {
        long before = bean.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < count; ++i) flat.resolve(0.1, -0.1, 0.2, box);
        return bean.getCurrentThreadAllocatedBytes() - before;
    }
}
