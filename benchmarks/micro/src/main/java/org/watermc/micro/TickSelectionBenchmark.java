package org.watermc.micro;

import java.util.concurrent.TimeUnit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.LevelTicks;
import net.minecraft.world.ticks.ScheduledTick;
import org.openjdk.jmh.annotations.*;
import org.watermc.ticks.reference.ReferenceLevelTicks;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
@Threads(1)
@State(Scope.Thread)
public class TickSelectionBenchmark {
    @Param({"64", "4096", "16384"}) public int containers;
    @Param({"FUTURE", "MIXED", "DISABLED", "DUE_BUDGET0"}) public String scenario;
    private enum Kind { TICK }
    private LevelTicks<Kind> water;
    private ReferenceLevelTicks<Kind> original;
    private long time;

    @Setup(Level.Trial)
    public void prepare() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        boolean ticking = !this.scenario.equals("MIXED") && !this.scenario.equals("DISABLED");
        this.water = new LevelTicks<>(key -> ticking);
        this.original = new ReferenceLevelTicks<>(key -> ticking);
        this.time = 0;
        for (int chunk = 0; chunk < this.containers; ++chunk) {
            boolean due = this.scenario.equals("DISABLED") || this.scenario.equals("DUE_BUDGET0")
                || this.scenario.equals("MIXED") && chunk == 0;
            long deadline = due ? Long.MIN_VALUE : Long.MAX_VALUE;
            BlockPos pos = new BlockPos(chunk * 16, 64, 0);
            var a = new LevelChunkTicks<Kind>();
            var b = new LevelChunkTicks<Kind>();
            a.schedule(new ScheduledTick<>(Kind.TICK, pos, deadline, chunk));
            b.schedule(new ScheduledTick<>(Kind.TICK, pos, deadline, chunk));
            this.water.addContainer(new ChunkPos(chunk, 0), a);
            this.original.addContainer(new ChunkPos(chunk, 0), b);
        }
    }

    @Benchmark
    public Object original() {
        this.original.tick(++this.time, 0, (pos, type) -> { throw new AssertionError("Нулевой лимит"); });
        return this.original;
    }

    @Benchmark
    public Object watermc() {
        this.water.tick(++this.time, 0, (pos, type) -> { throw new AssertionError("Нулевой лимит"); });
        return this.water;
    }

    @TearDown(Level.Trial)
    public void verifyUnconsumedTicks() {
        if (this.water.count() != this.containers || this.original.count() != this.containers) {
            throw new AssertionError("Микротест неожиданно выполнил событие при лимите 0");
        }
    }
}
