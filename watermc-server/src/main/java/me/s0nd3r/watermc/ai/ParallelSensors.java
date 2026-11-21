package me.s0nd3r.watermc.ai;

import ca.spottedleaf.moonrise.common.util.TickThread;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import me.s0nd3r.watermc.core.config.modules.async.ParallelSensorPhase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs due mob sensors in parallel right before the entity phase of a world; the world thread waits for all of
 * them, so the world does not change while they read it.
 *
 * <p>Idea from DivineMC "Parallel sensor phase" (BX-Team / DivineMC, GPL-3.0). Differences in WaterMc:</p>
 * <ul>
 *   <li>Sensor timers are not touched here: a sensor is scanned early and marked, and the mob's own brain tick
 *   counts down exactly as in vanilla and skips the second scan. DivineMC reset the timer in the pre-pass, which
 *   with the same-tick brain decrement made sensors fire one tick early and ignored brain ticks skipped by DAB.</li>
 *   <li>Only mobs in entity-ticking chunks take part: sensors read at most a few blocks around the mob, which is
 *   then always loaded, so a worker never has to load a chunk through the waiting world thread.</li>
 *   <li>Work is split into a few batches per thread instead of one future per mob.</li>
 * </ul>
 * <p>Only sensors audited as read-only (writes stay in the mob's own brain, no plugin events) run here.</p>
 */
public final class ParallelSensors {
    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");
    private static final Set<String> AUDITED = Set.of(
        "AdultSensor", "AdultSensorAnyType", "AxolotlAttackablesSensor", "BreezeAttackEntitySensor", "FrogAttackablesSensor",
        "GolemSensor", "HoglinSpecificSensor", "HurtBySensor", "IsInWaterSensor", "MobSensor", "NearestItemSensor",
        "NearestLivingEntitySensor", "PiglinBruteSpecificSensor", "PiglinSpecificSensor", "PlayerSensor",
        "SecondaryPoiSensor", "VillagerBabiesSensor", "VillagerHostilesSensor", "WardenEntitySensor");
    private static volatile ExecutorService pool;
    private static volatile int poolThreads;
    private static long lastErrorLog;

    private ParallelSensors() {
    }

    /** Exact vanilla classes only (subclasses from plugins are not audited). */
    static boolean audited(final Sensor<?> sensor) {
        final Class<?> type = sensor.getClass();
        return type.getPackageName().equals("net.minecraft.world.entity.ai.sensing") && AUDITED.contains(type.getSimpleName());
    }

    private static ExecutorService pool() {
        final int wanted = ParallelSensorPhase.threads > 0 ? ParallelSensorPhase.threads
            : me.s0nd3r.watermc.runtime.WorkerThreads.defaultFor(Runtime.getRuntime().availableProcessors());
        ExecutorService current = pool;
        if (current == null || poolThreads != wanted) {
            synchronized (ParallelSensors.class) {
                current = pool;
                if (current == null || poolThreads != wanted) {
                    if (current != null) {
                        current.shutdown();
                    }
                    final AtomicInteger id = new AtomicInteger();
                    current = Executors.newFixedThreadPool(wanted, run -> {
                        final TickThread thread = new TickThread(run, "WaterMc Sensor Worker #" + id.incrementAndGet());
                        thread.setDaemon(true);
                        return thread;
                    });
                    pool = current;
                    poolThreads = wanted;
                }
            }
        }
        return current;
    }

    record Job(Mob mob, Sensor<? super Mob>[] sensors) {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void preTick(final ServerLevel level) {
        if (!ParallelSensorPhase.enabled) {
            return;
        }
        final List<Job> jobs = new ArrayList<>();
        final net.minecraft.world.TickRateManager rates = level.tickRateManager();
        final long now = MinecraftServer.currentTick;
        level.waterEntityTickList().forEach(entity -> {
            if (!(entity instanceof Mob mob) || mob.isRemoved() || mob.activatedTick < now || rates.isEntityFrozen(mob)
                || !level.isPositionEntityTicking(mob.blockPosition())) {
                return;
            }
            List<Sensor<? super Mob>> due = null;
            for (final Sensor<?> sensor : mob.getBrain().waterSensors()) {
                if (sensor.waterWantsPrecompute() && audited(sensor)) {
                    if (due == null) {
                        due = new ArrayList<>(2);
                    }
                    due.add((Sensor<? super Mob>) sensor);
                }
            }
            if (due != null) {
                jobs.add(new Job(mob, due.toArray(new Sensor[0])));
            }
        });
        if (jobs.isEmpty()) {
            return;
        }
        final ExecutorService executor = pool();
        final int batches = Math.min(jobs.size(), poolThreads * 4);
        final int per = (jobs.size() + batches - 1) / batches;
        final CompletableFuture<?>[] futures = new CompletableFuture<?>[(jobs.size() + per - 1) / per];
        for (int b = 0, from = 0; from < jobs.size(); b++, from += per) {
            final List<Job> slice = jobs.subList(from, Math.min(jobs.size(), from + per));
            futures[b] = CompletableFuture.runAsync(() -> {
                for (final Job job : slice) {
                    for (final Sensor<? super Mob> sensor : job.sensors()) {
                        try {
                            sensor.waterPrecompute(level, job.mob());
                        } catch (final Throwable t) {
                            logOnce(level, job.mob(), t); // no mark set: the brain tick runs this sensor itself
                        }
                    }
                }
            }, executor);
        }
        CompletableFuture.allOf(futures).join();
    }

    private static synchronized void logOnce(final ServerLevel level, final Mob mob, final Throwable t) {
        final long now = System.nanoTime();
        if (now - lastErrorLog > 30_000_000_000L) {
            lastErrorLog = now;
            LOGGER.error("Parallel sensor scan failed for {} in {}; it falls back to the normal tick", mob.getType(), level.getWorld().getName(), t);
        }
    }
}
