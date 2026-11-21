package ca.spottedleaf.moonrise.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class TickThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TickThread.class);
    private static final boolean HARD_THROW = !me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled || !me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.disableHardThrow; // Leaf - SparklyPaper - parallel world ticking - THIS SHOULD NOT BE DISABLED SINCE IT CAN CAUSE DATA CORRUPTION!!! Anyhow, for production servers, if you want to make a test run to see if the server could crash, you can test it with this disabled

    private static String getThreadContext() {
        return "thread=" + Thread.currentThread().getName();
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void ensureTickThread(final String reason) {
        if (!isTickThread()) {
            LOGGER.error("Thread failed main thread check: " + reason + ", context=" + getThreadContext(), new Throwable());
            if (HARD_THROW) // Leaf - SparklyPaper - parallel world ticking
            throw new IllegalStateException(reason);
        }
    }

    public static void ensureTickThread(final Level world, final BlockPos pos, final String reason) {
        if (!isTickThreadFor(world, pos)) {
            final String ex = "Thread failed main thread check: " +
                               reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", block_pos=" + pos;
            // Leaf start - SparklyPaper - parallel world ticking
            final String threadInfo = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? ex + " - " + getTickThreadInformation(world.getServer()) : ex;
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
            // Leaf end - SparklyPaper - parallel world ticking
        }
    }

    public static void ensureTickThread(final Level world, final BlockPos pos, final int blockRadius, final String reason) {
        if (!isTickThreadFor(world, pos, blockRadius)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", block_pos=" + pos + ", block_radius=" + blockRadius;
            // Leaf start - SparklyPaper - parallel world ticking
            final String threadInfo = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? ex + " - " + getTickThreadInformation(world.getServer()) : ex;
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
            // Leaf end - SparklyPaper - parallel world ticking
        }
    }

    public static void ensureTickThread(final Level world, final ChunkPos pos, final String reason) {
        if (!isTickThreadFor(world, pos)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", chunk_pos=" + pos;
            // Leaf start - SparklyPaper - parallel world ticking
            final String threadInfo = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? ex + " - " + getTickThreadInformation(world.getServer()) : ex;
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
            // Leaf end - SparklyPaper - parallel world ticking
        }
    }

    public static void ensureTickThread(final Level world, final int chunkX, final int chunkZ, final String reason) {
        if (!isTickThreadFor(world, chunkX, chunkZ)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", chunk_pos=" + new ChunkPos(chunkX, chunkZ);
            // Leaf start - SparklyPaper - parallel world ticking
            final String threadInfo = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? ex + " - " + getTickThreadInformation(world.getServer()) : ex;
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
            // Leaf end - SparklyPaper - parallel world ticking
        }
    }

    public static void ensureTickThread(final Entity entity, final String reason) {
        if (!isTickThreadFor(entity)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", entity=" + EntityUtil.dumpEntity(entity);
            // Leaf start - SparklyPaper - parallel world ticking
            final String threadInfo = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? ex + " - " + getTickThreadInformation(entity.level().getServer()) : ex;
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
            // Leaf end - SparklyPaper - parallel world ticking
        }
    }

    public static void ensureTickThread(final Level world, final AABB aabb, final String reason) {
        if (!isTickThreadFor(world, aabb)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", aabb=" + aabb;
            // Leaf start - SparklyPaper - parallel world ticking
            final String threadInfo = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? ex + " - " + getTickThreadInformation(world.getServer()) : ex;
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
            // Leaf end - SparklyPaper - parallel world ticking
        }
    }

    public static void ensureTickThread(final Level world, final double blockX, final double blockZ, final String reason) {
        if (!isTickThreadFor(world, blockX, blockZ)) {
            final String ex = "Thread failed main thread check: " +
                reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + ", block_pos=" + new Vec3(blockX, 0.0, blockZ);
            // Leaf start - SparklyPaper - parallel world ticking
            final String threadInfo = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? ex + " - " + getTickThreadInformation(world.getServer()) : ex;
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
            // Leaf end - SparklyPaper - parallel world ticking
        }
    }

    // Leaf start - SparklyPaper - parallel world ticking
    // Copied from `TickThread#ensureTickThread(final String reason)`
    // This is an additional method to check if the tick thread is bound to a specific world because, by default, Paper's isTickThread methods do not provide this information
    // Because we only tick worlds in parallel (instead of regions), we can use this for our checks
    public static void ensureTickThread(final net.minecraft.server.level.ServerLevel world, final String reason) {
        if (!isTickThreadFor(world)) {
            final String threadInfo = "Thread failed main thread check: " + reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + " - " + getTickThreadInformation(world.getServer());
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
        }
    }

    // This is an additional method to check if it is a tick thread but ONLY a tick thread
    public static void ensureOnlyTickThread(final String reason) {
        if (!isTickThread() || isServerLevelTickThread()) {
            final String threadInfo = "Thread failed tick-thread-only check: " + reason + ", context=" + getThreadContext();
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
        }
    }

    // This is an additional method to check if the tick thread is bound to a specific world or if it is an async thread.
    public static void ensureTickThreadOrAsyncThread(final net.minecraft.server.level.ServerLevel world, final String reason) {
        if (isTickThread() && !isTickThreadFor(world)) {
            final String threadInfo = "Thread failed main thread or async thread check: " + reason + ", context=" + getThreadContext() + ", world=" + WorldUtil.getWorldName(world) + " - " + getTickThreadInformation(world.getServer());
            LOGGER.error(threadInfo, new Throwable());
            if (HARD_THROW) throw new IllegalStateException(threadInfo);
        }
    }

    private static String getTickThreadInformation(net.minecraft.server.MinecraftServer minecraftServer) {
        StringBuilder sb = new StringBuilder();
        Thread currentThread = Thread.currentThread();
        sb.append("Is tick thread? ");
        sb.append(currentThread instanceof TickThread);
        sb.append("; Is server level tick thread? ");
        sb.append(currentThread instanceof ServerLevelTickThread);
        if (currentThread instanceof ServerLevelTickThread serverLevelTickThread) {
            sb.append("; Currently ticking level: ");
            sb.append(serverLevelTickThread.currentTickingServerLevel != null ? serverLevelTickThread.currentTickingServerLevel.getWorld().getName() : "null");
        }
        sb.append("; Is iterating over levels? ");
        sb.append(minecraftServer.isIteratingOverLevels);
        sb.append("; Are we going to hard throw? ");
        sb.append(HARD_THROW);
        return sb.toString();
    }

    public static boolean isServerLevelTickThread() {
        return Thread.currentThread() instanceof ServerLevelTickThread;
    }
    // Leaf end - SparklyPaper - parallel world ticking

    public final int id; /* We don't override getId as the spec requires that it be unique (with respect to all other threads) */

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();

    public TickThread(final String name) {
        this(null, name);
    }

    public TickThread(final Runnable run, final String name) {
        this(null, run, name);
    }

    public TickThread(final ThreadGroup group, final Runnable run, final String name) {
        this(group, run, name, ID_GENERATOR.incrementAndGet());
    }

    private TickThread(final ThreadGroup group, final Runnable run, final String name, final int id) {
        super(group, run, name);
        this.id = id;
    }

    public static TickThread getCurrentTickThread() {
        return (TickThread)Thread.currentThread();
    }

    public static boolean isTickThread() {
        return Thread.currentThread() instanceof TickThread;
    }

    public static boolean isShutdownThread() {
        return false;
    }

    public static boolean isTickThreadFor(final Level world, final BlockPos pos) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final BlockPos pos, final int blockRadius) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final ChunkPos pos) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final Vec3 pos) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final int chunkX, final int chunkZ) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final AABB aabb) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final double blockX, final double blockZ) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final Vec3 position, final Vec3 deltaMovement, final int buffer) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final int fromChunkX, final int fromChunkZ, final int toChunkX, final int toChunkZ) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Level world, final int chunkX, final int chunkZ, final int radius) {
        return isTickThreadFor(world); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    public static boolean isTickThreadFor(final Entity entity) {
        return isTickThreadFor(entity.level()); // Leaf - SparklyPaper - parallel world ticking - use methods for what they were made for
    }

    // Leaf start - SparklyPaper - parallel world ticking
    // This is an additional method to check if the tick thread is bound to a specific world because, by default, Paper's isTickThread methods do not provide this information
    // Because we only tick worlds in parallel (instead of regions), we can use this for our checks
    public static boolean isTickThreadFor(final Level world) {
        if (Thread.currentThread() instanceof ServerLevelTickThread serverLevelTickThread) {
            return serverLevelTickThread.currentTickingServerLevel == world;
        }
        return isTickThread();
    }

    public static class ServerLevelTickThread extends TickThread {

        public net.minecraft.server.level.ServerLevel currentTickingServerLevel;

        public ServerLevelTickThread(String name) {
            super(name);
        }

        public ServerLevelTickThread(Runnable run, String name) {
            super(run, name);
        }
    }
    // Leaf end - SparklyPaper - parallel world ticking
}
