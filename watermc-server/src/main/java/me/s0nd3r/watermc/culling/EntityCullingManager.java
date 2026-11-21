// Ported from DivineMC ver/26.3 (BX-Team / DivineMC, GPL-3.0): server-side port of tr7zw's EntityCulling
// (occlusion library: LogisticsCraft/OcclusionCulling, MIT). Adapted to WaterMc 1.21.11, off by default.
package me.s0nd3r.watermc.culling;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Raytrace entity culling (server-side port of tr7zw's EntityCulling): untracks entities a
 * player provably cannot see, cutting tracker bandwidth and blinding entity-ESP cheats.
 *
 * <p>One {@link PlayerCullTracker} per real player, all running on a single bounded scheduled
 * pool with a fixed delay, so passes for one player never overlap and thread count does not
 * scale with player count.
 */
public final class EntityCullingManager {
    static final Logger LOGGER = LogManager.getLogger("EntityCulling");

    private static final Set<PlayerCullTracker> TRACKERS = ConcurrentHashMap.newKeySet();
    private static volatile ScheduledThreadPoolExecutor pool;

    private EntityCullingManager() { }

    public static boolean isCulled(ServerPlayer player, Entity entity) {
        if (!me.s0nd3r.watermc.core.config.modules.network.RaytraceEntityCulling.enabled) {
            return false;
        }
        final PlayerCullTracker tracker = player.waterCullTracker;
        return tracker != null && tracker.isCulled(entity.getId());
    }

    public static void tickPlayer(ServerPlayer player) {
        if (!me.s0nd3r.watermc.core.config.modules.network.RaytraceEntityCulling.enabled || !player.isRealPlayer) {
            return;
        }
        final PlayerCullTracker tracker = player.waterCullTracker;
        if (tracker == null) {
            register(player);
        } else if (tracker.pollCulledChanged()) {
            refreshWaypoints(player);
        }
    }

    private static void refreshWaypoints(ServerPlayer player) {
        if (player.connection == null || !player.isReceivingWaypoints()) {
            return;
        }
        final net.minecraft.server.waypoints.ServerWaypointManager waypoints = player.level().getWaypointManager();
        if (waypoints.locatorBarEnabled) {
            waypoints.updatePlayer(player);
        }
    }

    private static synchronized void register(ServerPlayer player) {
        if (player.waterCullTracker != null) {
            return;
        }
        final PlayerCullTracker tracker = new PlayerCullTracker(player);
        player.waterCullTracker = tracker;
        TRACKERS.add(tracker);
        final long interval = Math.max(10L, me.s0nd3r.watermc.core.config.modules.network.RaytraceEntityCulling.checkIntervalMs);
        tracker.future = pool().scheduleWithFixedDelay(tracker, interval, interval, TimeUnit.MILLISECONDS);
    }

    static void unregister(ServerPlayer player, PlayerCullTracker tracker) {
        TRACKERS.remove(tracker);
        if (player.waterCullTracker == tracker) {
            player.waterCullTracker = null;
        }
        final ScheduledFuture<?> future = tracker.future;
        if (future != null) {
            future.cancel(false);
        }
    }

    public static void onBlockChange(Level level, BlockPos pos) {
        if (!me.s0nd3r.watermc.core.config.modules.network.RaytraceEntityCulling.enabled || TRACKERS.isEmpty()) {
            return;
        }
        final int traceDistance = me.s0nd3r.watermc.core.config.modules.network.RaytraceEntityCulling.maxTraceDistance;
        for (final PlayerCullTracker tracker : TRACKERS) {
            tracker.notifyBlockChange(level, pos, traceDistance);
        }
    }

    private static ScheduledThreadPoolExecutor pool() {
        ScheduledThreadPoolExecutor pool = EntityCullingManager.pool;
        if (pool == null) {
            synchronized (EntityCullingManager.class) {
                pool = EntityCullingManager.pool;
                if (pool == null) {
                    final int configured = me.s0nd3r.watermc.core.config.modules.network.RaytraceEntityCulling.threads;
                    final int threads = configured > 0
                        ? configured
                        : Math.clamp(Runtime.getRuntime().availableProcessors() / 8, 1, 4);
                    pool = new ScheduledThreadPoolExecutor(
                        threads,
                        new WaterCullingThreadFactory()
                    );
                    pool.setRemoveOnCancelPolicy(true);
                    EntityCullingManager.pool = pool;
                }
            }
        }
        return pool;
    }

    public static void shutdown() {
        final ScheduledThreadPoolExecutor pool = EntityCullingManager.pool;
        if (pool != null) {
            pool.shutdown();
            try {
                pool.awaitTermination(3L, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) { }
        }
    }
}
