package me.s0nd3r.watermc.core.world;

import io.papermc.paper.entity.activation.ActivationType;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.s0nd3r.watermc.core.config.modules.opt.DynamicActivationofBrain;
import me.s0nd3r.watermc.core.util.KDTree2D;
import me.s0nd3r.watermc.core.util.KDTree3D;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Entity activation range (EAR) without per-player entity lists: every chunk around the players is read once and
 * each entity is checked against the nearest player through a k-d tree. Ported from Leaf ver/26.3 "optimize entity
 * activation" (hayanesuru, GPL-3.0). Unlike Paper's version, the activation range is a circle on X/Z instead of a
 * square, so entities in the corners of the old square go inactive. One instance per level: no global lock, safe
 * with parallel world ticking.
 */
public final class EntityActivation {
    private static final ActivationType[] ACTIVATION_TYPES = ActivationType.values();
    private static final ServerPlayer[] EMPTY_PLAYERS = {};

    private final ObjectArrayList<Entity> entityListCache = new ObjectArrayList<>();
    private final LongOpenHashSet chunks = new LongOpenHashSet();
    private final KDTree2D kdTree2 = new KDTree2D();
    private final KDTree3D kdTree3 = new KDTree3D();

    public void activateEntities(final ServerLevel world) {
        final int miscActivationRange = world.spigotConfig.miscActivationRange;
        final int raiderActivationRange = world.spigotConfig.raiderActivationRange;
        final int animalActivationRange = world.spigotConfig.animalActivationRange;
        final int monsterActivationRange = world.spigotConfig.monsterActivationRange;
        final int waterActivationRange = world.spigotConfig.waterActivationRange;
        final int flyingActivationRange = world.spigotConfig.flyingMonsterActivationRange;
        final int villagerActivationRange = world.spigotConfig.villagerActivationRange;

        world.wakeupInactiveRemainingAnimals = Math.min(world.wakeupInactiveRemainingAnimals + 1, world.spigotConfig.wakeUpInactiveAnimals);
        world.wakeupInactiveRemainingVillagers = Math.min(world.wakeupInactiveRemainingVillagers + 1, world.spigotConfig.wakeUpInactiveVillagers);
        world.wakeupInactiveRemainingMonsters = Math.min(world.wakeupInactiveRemainingMonsters + 1, world.spigotConfig.wakeUpInactiveMonsters);
        world.wakeupInactiveRemainingFlying = Math.min(world.wakeupInactiveRemainingFlying + 1, world.spigotConfig.wakeUpInactiveFlying);

        int maxRange = Math.max(monsterActivationRange, animalActivationRange);
        maxRange = Math.max(maxRange, raiderActivationRange);
        maxRange = Math.max(maxRange, miscActivationRange);
        maxRange = Math.max(maxRange, flyingActivationRange);
        maxRange = Math.max(maxRange, waterActivationRange);
        maxRange = Math.max(maxRange, villagerActivationRange);
        maxRange = Math.min((world.spigotConfig.simulationDistance << 4) - 8, maxRange);

        final double[] ranges = new double[ACTIVATION_TYPES.length];
        ranges[ActivationType.WATER.ordinal()] = waterActivationRange;
        ranges[ActivationType.FLYING_MONSTER.ordinal()] = flyingActivationRange;
        ranges[ActivationType.VILLAGER.ordinal()] = villagerActivationRange;
        ranges[ActivationType.MONSTER.ordinal()] = monsterActivationRange;
        ranges[ActivationType.ANIMAL.ordinal()] = animalActivationRange;
        ranges[ActivationType.RAIDER.ordinal()] = raiderActivationRange;
        ranges[ActivationType.MISC.ordinal()] = miscActivationRange;
        for (int i = 0; i < ranges.length; i++) {
            if (ranges[i] > 0.0D) {
                ranges[i] = ranges[i] * ranges[i];
            }
        }

        final long currentTick = MinecraftServer.currentTick;
        final ServerPlayer[] players = world.players().toArray(EMPTY_PLAYERS);
        final double[] pxl = new double[players.length];
        final double[] pyl = new double[players.length];
        final double[] pzl = new double[players.length];
        int playerSize = 0;
        for (final ServerPlayer p : players) {
            p.activatedTick = currentTick;
            if (world.spigotConfig.ignoreSpectatorActivation && p.isSpectator()) {
                continue;
            }
            if (!world.purpurConfig.idleTimeoutTickNearbyEntities && p.isAfk()) {
                continue; // Purpur - AFK API
            }
            players[playerSize] = p;
            pxl[playerSize] = p.getX();
            pyl[playerSize] = p.getY();
            pzl[playerSize] = p.getZ();
            playerSize++;
        }
        if (playerSize == 0) {
            return;
        }

        final boolean dab = DynamicActivationofBrain.enabled;
        final int[] indices = new int[playerSize];
        this.kdTree2.build(new double[][] {pxl, pzl}, indices); // only the first playerSize entries are read
        if (dab) {
            this.kdTree3.build(new double[][] {pxl, pyl, pzl}, indices);
        }

        final ObjectArrayList<Entity> entities = this.entityListCache;
        final LongOpenHashSet chunks = this.chunks;
        try {
            final ca.spottedleaf.moonrise.patches.chunk_system.level.entity.EntityLookup lookup =
                ((ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemLevel) world).moonrise$getEntityLookup();
            final double worldHeight = world.getHeight();
            for (int k = 0; k < playerSize; k++) {
                final AABB box = players[k].getBoundingBox().inflate(maxRange, worldHeight, maxRange);
                lookup.leaf$getEntities(box, chunks, entities);
                ca.spottedleaf.moonrise.common.PlatformHooks.get().addToGetEntities(world, null, box, null, entities);
            }

            final boolean tickMarkers = world.paperConfig().entities.markers.tick;
            final boolean dontEnableIfInWater = DynamicActivationofBrain.dontEnableIfInWater;
            final double startSq = DynamicActivationofBrain.startDistanceSquared;
            final double scale = Math.pow(2.0D, -DynamicActivationofBrain.activationDistanceMod);
            final int maxPriority = DynamicActivationofBrain.maximumActivationPrio;
            final Object[] raw = entities.elements();
            for (int i = 0, size = entities.size(); i < size; i++) {
                final Entity entity = (Entity) raw[i];
                if (!tickMarkers && entity instanceof Marker) {
                    continue;
                }
                if (currentTick <= entity.activatedTick) {
                    continue;
                }
                final Vec3 p = entity.position();
                if (entity.defaultActivationState) {
                    entity.activatedTick = currentTick;
                } else {
                    final double max = ranges[entity.activationType.ordinal()];
                    if (this.kdTree2.nearestSqr(p.x, p.z, max) != max) {
                        entity.activatedTick = currentTick;
                    }
                }
                if (dab && entity.getType().dabEnabled
                    && (!dontEnableIfInWater || !entity.isInWater() || entity.getType().is(net.minecraft.tags.EntityTypeTags.CAN_BREATHE_UNDER_WATER))) {
                    final double distSq = this.kdTree3.nearestSqr(p.x, p.y, p.z, 16384.0D);
                    entity.activatedPriority = distSq > startSq ? Math.min(maxPriority, Math.max((int) (distSq * scale), 1)) : 1;
                } else {
                    entity.activatedPriority = 1;
                }
            }
        } finally {
            entities.clear();
            chunks.clear();
        }
    }
}
