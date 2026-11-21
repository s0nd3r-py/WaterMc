package me.s0nd3r.watermc.world;

import ca.spottedleaf.concurrentutil.util.Priority;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

/**
 * Prefetches chunks around the expected nether-portal exit while the entity waits in the portal.
 *
 * <p>Backported from DivineMC 26.x ({@code org.bxteam.divinemc.async.world.PortalSearchPrefetcher},
 * BX-Team / DivineMC, GPL-3.0) to 1.21.11. Chunks are only loaded from disk ({@code gen = false}) to
 * {@code EMPTY} with a temporary ticket at low priority; nothing is generated and nothing is changed in the
 * world, so behaviour stays vanilla: only the time of the later synchronous portal search shrinks. Thread
 * safety: the Moonrise scheduler accepts loads from any thread, including other world threads under
 * parallel world ticking.</p>
 */
public final class PortalSearchPrefetcher {
    private static final int MAX_PREFETCH_CHUNK_RADIUS = 4;

    private PortalSearchPrefetcher() {
    }

    /** Chunk radius to prefetch for a given portal search radius in blocks. */
    public static int chunkRadius(int searchRadiusBlocks) {
        return Math.min(MAX_PREFETCH_CHUNK_RADIUS, (Math.max(0, searchRadiusBlocks) >> 4) + 1);
    }

    public static void onPortalEnter(final Entity entity, final Portal portal) {
        if (!me.s0nd3r.watermc.core.config.modules.async.PortalSearchPrefetch.enabled) return;
        if (!(portal instanceof NetherPortalBlock)) return;
        if (!(entity.level() instanceof ServerLevel currentLevel)) return;
        if (portal.getPortalTransitionTime(currentLevel, entity) < 2) return; // instant transition: nothing to win

        final ResourceKey<Level> newDimension = currentLevel.getTypeKey() == LevelStem.NETHER ? Level.OVERWORLD : Level.NETHER;
        final ServerLevel newLevel = currentLevel.getServer().getLevel(newDimension);
        if (newLevel == null) return;

        final boolean toNether = newLevel.getTypeKey() == LevelStem.NETHER;
        final double scale = DimensionType.getTeleportationScale(currentLevel.dimensionType(), newLevel.dimensionType());
        final BlockPos exit = newLevel.getWorldBorder().clampToBounds(entity.getX() * scale, entity.getY(), entity.getZ() * scale);

        int searchRadius = newLevel.paperConfig().environment.portalSearchRadius;
        if (currentLevel.paperConfig().environment.portalSearchVanillaDimensionScaling && toNether) {
            searchRadius = (int) (searchRadius / newLevel.dimensionType().coordinateScale());
        }

        final int radius = chunkRadius(searchRadius);
        final int centerX = exit.getX() >> 4;
        final int centerZ = exit.getZ() >> 4;
        final var scheduler = newLevel.moonrise$getChunkTaskScheduler();
        for (int dz = -radius; dz <= radius; ++dz) {
            for (int dx = -radius; dx <= radius; ++dx) {
                scheduler.scheduleChunkLoad(centerX + dx, centerZ + dz, false, ChunkStatus.EMPTY, true, Priority.LOW, null);
            }
        }
    }
}
