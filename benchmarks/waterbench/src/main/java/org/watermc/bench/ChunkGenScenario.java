package org.watermc.bench;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.Plugin;

import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Generates a square of chunks in a fresh normal world with a fixed seed and measures throughput.
 * Generation runs on worker threads, so tick times show how much it disturbs the main thread.
 */
final class ChunkGenScenario implements Scenario {

    private static final String WORLD = "waterbench_gen";
    private static final int MAX_IN_FLIGHT = 64;

    private final Plugin plugin;
    private final int radius;
    private World world;
    private int nextIndex;
    private int total;
    private final AtomicInteger inFlight = new AtomicInteger();
    private final AtomicInteger done = new AtomicInteger();
    private final AtomicReference<Throwable> failure = new AtomicReference<>();
    private long startNanos;
    private long endNanos;

    ChunkGenScenario(Plugin plugin, int radius) {
        if (radius < 0 || radius > 256) throw new IllegalArgumentException("radius must be 0..256");
        this.plugin = plugin;
        this.radius = radius;
    }

    @Override
    public String name() {
        return "chunkgen";
    }

    @Override
    public void prepare() {
        if (Files.exists(Bukkit.getWorldContainer().toPath().resolve(WORLD))) {
            throw new IllegalStateException("Generation benchmark requires a fresh run directory: " + WORLD);
        }
        this.world = new WorldCreator(WORLD).seed(1234567890L).createWorld();
        if (this.world == null) throw new IllegalStateException("could not create " + WORLD);
        this.world.setAutoSave(false);
        int side = this.radius * 2 + 1;
        this.total = side * side;
        this.nextIndex = 0;
        this.done.set(0);
        this.failure.set(null);
        this.endNanos = 0;
        this.startNanos = System.nanoTime();
    }

    @Override
    public TickResult tick() {
        if (this.failure.get() != null) {
            throw new IllegalStateException("Chunk generation failed; this run is invalid", this.failure.get());
        }
        int side = this.radius * 2 + 1;
        while (this.nextIndex < this.total && this.inFlight.get() < MAX_IN_FLIGHT) {
            // offset far from spawn so the spawn area chunks do not count
            int x = 1000 + this.nextIndex % side;
            int z = 1000 + this.nextIndex / side;
            this.nextIndex++;
            this.inFlight.incrementAndGet();
            this.world.getChunkAtAsync(x, z, true).whenComplete((chunk, err) -> {
                if (err != null || chunk == null) {
                    this.failure.compareAndSet(null, err == null ? new IllegalStateException("null chunk") : err);
                } else {
                    this.done.incrementAndGet();
                }
                this.inFlight.decrementAndGet();
            });
        }
        if (this.done.get() >= this.total) {
            if (this.endNanos == 0) this.endNanos = System.nanoTime();
            return TickResult.FINISHED;
        }
        return TickResult.COUNT;
    }

    double chunksPerSecond() {
        long end = this.endNanos == 0 ? System.nanoTime() : this.endNanos;
        return this.done.get() / ((end - this.startNanos) / 1e9);
    }

    int generated() {
        return this.done.get();
    }

    @Override
    public void cleanup() {
        if (this.world != null) {
            Bukkit.unloadWorld(this.world, false);
            this.world = null;
        }
    }
}
