package me.s0nd3r.watermc.ticks;

import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.SavedTick;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraft.world.ticks.TickPriority;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import me.s0nd3r.watermc.ticks.reference.ReferenceLevelTicks;

import static org.junit.jupiter.api.Assertions.*;

class LevelTicksTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    private enum Kind { BLOCK, FLUID, OTHER }

    private static ScheduledTick<Kind> scheduled(int x, long when, long order) {
        return new ScheduledTick<>(Kind.BLOCK, new BlockPos(x, 64, 0), when, order);
    }

    // Считаем реальные полные обходы карты, не время выполнения на занятом ПК.
    private static final class CountingMap extends Long2LongOpenHashMap {
        int scans;
        CountingMap() { defaultReturnValue(Long.MAX_VALUE); }
        @Override
        public Long2LongMap.FastEntrySet long2LongEntrySet() {
            ++scans;
            return super.long2LongEntrySet();
        }
    }

    private static CountingMap installCounter(Object scheduler) throws Exception {
        Field field = scheduler.getClass().getDeclaredField("nextTickForContainer");
        field.setAccessible(true);
        CountingMap counter = new CountingMap();
        field.set(scheduler, counter);
        return counter;
    }

    @Test
    void avoidsFutureOnlyScansWithoutChangingDeliveries() throws Exception {
        LevelTicks<Kind> actual = new LevelTicks<>(key -> true);
        ReferenceLevelTicks<Kind> reference = new ReferenceLevelTicks<>(key -> true);
        CountingMap actualCounter = installCounter(actual);
        CountingMap referenceCounter = installCounter(reference);
        for (int chunk = 0; chunk < 4096; ++chunk) {
            LevelChunkTicks<Kind> a = new LevelChunkTicks<>();
            LevelChunkTicks<Kind> b = new LevelChunkTicks<>();
            a.schedule(scheduled(chunk * 16, 10_000, chunk));
            b.schedule(scheduled(chunk * 16, 10_000, chunk));
            actual.addContainer(new ChunkPos(chunk, 0), a);
            reference.addContainer(new ChunkPos(chunk, 0), b);
        }
        for (long time = 0; time < 1000; ++time) {
            actual.tick(time, 100_000, (pos, type) -> fail("Событие выполнено раньше срока"));
            reference.tick(time, 100_000, (pos, type) -> fail("Ошибка эталона"));
        }
        assertEquals(1, actualCounter.scans);
        assertEquals(1000, referenceCounter.scans);
        List<BlockPos> a = new ArrayList<>();
        List<BlockPos> b = new ArrayList<>();
        actual.tick(10_000, 100_000, (pos, type) -> a.add(pos));
        reference.tick(10_000, 100_000, (pos, type) -> b.add(pos));
        assertEquals(4096, a.size());
        assertEquals(b, a);
        System.out.println("WATER_TICK_SCAN_RESULT containers=4096 idle_ticks=1000 reference_scans=1000 watermc_scans=1 deliveries=4096");
    }

    @Test
    void invalidatesDeadlineForEarlierInsertAndKeepsOverdueDisabledChunks() {
        Set<Long> disabled = new HashSet<>();
        LevelTicks<Kind> ticks = new LevelTicks<>(key -> !disabled.contains(key));
        LevelChunkTicks<Kind> container = new LevelChunkTicks<>();
        ticks.addContainer(new ChunkPos(0, 0), container);
        ticks.schedule(scheduled(1, 1000, 0));
        ticks.tick(0, 10, (pos, type) -> fail());
        ticks.schedule(scheduled(2, 2, 1));
        disabled.add(ChunkPos.asLong(0, 0));
        ticks.tick(2, 10, (pos, type) -> fail());
        disabled.clear();
        List<BlockPos> result = new ArrayList<>();
        ticks.tick(3, 0, (pos, type) -> fail());
        ticks.tick(4, 10, (pos, type) -> result.add(pos));
        assertEquals(List.of(new BlockPos(2, 64, 0)), result);
        ticks.tick(999, 10, (pos, type) -> fail());
        ticks.tick(1000, 10, (pos, type) -> result.add(pos));
        assertEquals(2, result.size());
    }

    @Test
    void seesUnpackedTicksAfterEmptyCacheAndUnloadReload() {
        LevelTicks<Kind> ticks = new LevelTicks<>(key -> true);
        ticks.tick(0, 10, (pos, type) -> fail());
        LevelChunkTicks<Kind> pending = new LevelChunkTicks<>(List.of(
            new SavedTick<>(Kind.FLUID, new BlockPos(1, 64, 1), 5, TickPriority.HIGH)));
        ChunkPos pos = new ChunkPos(0, 0);
        ticks.addContainer(pos, pending);
        pending.unpack(10);
        ticks.removeContainer(pos);
        ticks.tick(15, 10, (block, type) -> fail());
        ticks.addContainer(pos, pending);
        List<Kind> result = new ArrayList<>();
        ticks.tick(15, 10, (block, type) -> result.add(type));
        assertEquals(List.of(Kind.FLUID), result);
    }

    @Test
    void handlesExtremeTimesWithoutDeadlineArithmetic() {
        LevelTicks<Kind> ticks = new LevelTicks<>(key -> true);
        ticks.addContainer(new ChunkPos(0, 0), new LevelChunkTicks<>());
        ticks.schedule(scheduled(1, Long.MAX_VALUE, 1));
        ticks.tick(Long.MIN_VALUE, 10, (pos, type) -> fail());
        ticks.schedule(scheduled(2, Long.MIN_VALUE, 0));
        List<BlockPos> result = new ArrayList<>();
        ticks.tick(Long.MIN_VALUE, 10, (pos, type) -> result.add(pos));
        assertEquals(List.of(new BlockPos(2, 64, 0)), result);
        ticks.tick(Long.MAX_VALUE - 1, 10, (pos, type) -> fail());
        ticks.tick(Long.MAX_VALUE, 10, (pos, type) -> result.add(pos));
        assertEquals(2, result.size());
    }

    @Test
    void remainsConservativeWhenTickPredicateSchedulesAnotherContainer() {
        List<BlockPos> actual = reentrant(false);
        assertEquals(reentrant(true), actual);
        assertEquals(List.of(new BlockPos(0, 64, 0), new BlockPos(17, 64, 0)), actual);
    }

    private static List<BlockPos> reentrant(boolean baseline) {
        LevelTicks<Kind>[] a = new LevelTicks[1];
        ReferenceLevelTicks<Kind>[] b = new ReferenceLevelTicks[1];
        boolean[] inserted = {false};
        java.util.function.LongPredicate check = key -> {
            if (!inserted[0]) {
                inserted[0] = true;
                if (baseline) b[0].schedule(scheduled(17, 101, 2));
                else a[0].schedule(scheduled(17, 101, 2));
            }
            return true;
        };
        a[0] = new LevelTicks<>(check);
        b[0] = new ReferenceLevelTicks<>(check);
        for (int chunk = 0; chunk < 2; ++chunk) {
            LevelChunkTicks<Kind> container = new LevelChunkTicks<>();
            container.schedule(scheduled(chunk * 16, chunk == 0 ? 100 : 500, chunk));
            if (baseline) b[0].addContainer(new ChunkPos(chunk, 0), container);
            else a[0].addContainer(new ChunkPos(chunk, 0), container);
        }
        List<BlockPos> result = new ArrayList<>();
        for (int time = 0; time <= 101; ++time) {
            if (baseline) b[0].tick(time, 10, (pos, type) -> result.add(pos));
            else a[0].tick(time, 10, (pos, type) -> result.add(pos));
        }
        return result;
    }

    @Test
    void matchesOriginalAcrossRandomizedSchedulingRemovalCopyAndTicking() {
        for (int seed = 0; seed < 12; ++seed) {
            Random random = new Random(seed);
            Set<Long> disabled = new HashSet<>();
            LevelTicks<Kind> actual = new LevelTicks<>(key -> !disabled.contains(key));
            ReferenceLevelTicks<Kind> reference = new ReferenceLevelTicks<>(key -> !disabled.contains(key));
            LevelChunkTicks<Kind>[] a = new LevelChunkTicks[8];
            LevelChunkTicks<Kind>[] b = new LevelChunkTicks[8];
            for (int chunk = 0; chunk < 8; ++chunk) {
                a[chunk] = new LevelChunkTicks<>();
                b[chunk] = new LevelChunkTicks<>();
                actual.addContainer(new ChunkPos(chunk, 0), a[chunk]);
                reference.addContainer(new ChunkPos(chunk, 0), b[chunk]);
            }
            long time = -20;
            for (int operation = 0; operation < 2500; ++operation) {
                int chunk = random.nextInt(8);
                int x = chunk * 16 + random.nextInt(16);
                BlockPos probe = new BlockPos(x, 64, random.nextInt(4));
                Kind type = Kind.values()[random.nextInt(3)];
                switch (random.nextInt(9)) {
                    case 0, 1, 2 -> {
                        ScheduledTick<Kind> tick = new ScheduledTick<>(type, probe,
                            time + random.nextInt(200) - 20,
                            TickPriority.values()[random.nextInt(TickPriority.values().length)], operation);
                        actual.schedule(tick);
                        reference.schedule(tick);
                    }
                    case 3 -> disabled.add(ChunkPos.asLong(chunk, 0));
                    case 4 -> disabled.remove(ChunkPos.asLong(chunk, 0));
                    case 5 -> {
                        BoundingBox area = new BoundingBox(chunk * 16, 60, 0, chunk * 16 + 7, 70, 2);
                        actual.clearArea(area);
                        reference.clearArea(area);
                    }
                    case 6 -> {
                        int source = random.nextInt(4);
                        BoundingBox area = new BoundingBox(source * 16, 60, 0, source * 16 + 15, 70, 3);
                        actual.copyArea(area, new Vec3i(64, 0, 0));
                        reference.copyArea(area, new Vec3i(64, 0, 0));
                    }
                    case 7 -> {
                        ChunkPos pos = new ChunkPos(chunk, 0);
                        actual.removeContainer(pos);
                        reference.removeContainer(pos);
                        actual.addContainer(pos, a[chunk]);
                        reference.addContainer(pos, b[chunk]);
                    }
                    default -> {
                        time += random.nextInt(15) - 3;
                        int limit = random.nextInt(12);
                        List<String> actualTrace = new ArrayList<>();
                        List<String> referenceTrace = new ArrayList<>();
                        long followupTime = time + 1;
                        int order = 100_000 + operation;
                        actual.tick(time, limit, (pos, kind) -> {
                            actualTrace.add(pos + ":" + kind + ":" + actual.willTickThisTick(probe, type));
                            if ((pos.getX() & 7) == 0) actual.schedule(new ScheduledTick<>(kind, pos, followupTime, order));
                        });
                        reference.tick(time, limit, (pos, kind) -> {
                            referenceTrace.add(pos + ":" + kind + ":" + reference.willTickThisTick(probe, type));
                            if ((pos.getX() & 7) == 0) reference.schedule(new ScheduledTick<>(kind, pos, followupTime, order));
                        });
                        assertEquals(referenceTrace, actualTrace, "seed=" + seed + ", operation=" + operation);
                    }
                }
                assertEquals(reference.count(), actual.count());
                assertEquals(reference.hasScheduledTick(probe, type), actual.hasScheduledTick(probe, type));
            }
            disabled.clear();
            List<String> endA = new ArrayList<>();
            List<String> endB = new ArrayList<>();
            actual.tick(Long.MAX_VALUE, 100_000, (pos, type) -> endA.add(pos + ":" + type));
            reference.tick(Long.MAX_VALUE, 100_000, (pos, type) -> endB.add(pos + ":" + type));
            assertEquals(endB, endA, "Финальная очередь seed=" + seed);
        }
        System.out.println("WATER_TICK_DIFFERENTIAL_RESULT seeds=12 operations=30000 matched=true");
    }
}
