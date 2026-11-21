package org.bukkit.craftbukkit.block;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.CraftFluidCollisionMode;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftLocation;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.craftbukkit.util.CraftVoxelShape;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class CraftBlock implements Block {
    private final net.minecraft.world.level.LevelAccessor world;
    private final BlockPos position;

    private CraftBlock(LevelAccessor world, BlockPos position) {
        this.world = world;
        this.position = position.immutable();
    }

    public static CraftBlock at(LevelAccessor world, BlockPos position) {
        return new CraftBlock(world, position);
    }

    // Leaf start - SparklyPaper - parallel world ticking
    private ServerLevel getServerLevel() {
        return this.world instanceof ServerLevel serverLevel ? serverLevel : null;
    }

    private boolean needsBuffering(ServerLevel level) {
        // No ServerLevel means no queue, can't buffer
        return level != null && me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled && me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.asyncUnsafeReadHandling == me.s0nd3r.watermc.core.async.world.UnsafeReadPolicy.BUFFERED && !ca.spottedleaf.moonrise.common.util.TickThread.isTickThreadFor(level);
    }

    private void checkStrictMode(ServerLevel level, String methodName) {
        // Only check if PWT enabled, mode is STRICT, and we have a ServerLevel
        if (level != null && me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled && me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.asyncUnsafeReadHandling == me.s0nd3r.watermc.core.async.world.UnsafeReadPolicy.STRICT) {
            ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(level, this.position, "PWT: Async unsafe block read (strict mode): " + methodName);
        }
    }

    private <T> T executeBufferedRead(ServerLevel level, me.s0nd3r.watermc.core.async.world.ReadOperationType type, Object[] params, T defaultValue, String methodName) {
        if (level == null) { // Should not happen if called correctly after needsBuffering
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: executeBufferedRead called with null ServerLevel for " + methodName);
            return defaultValue;
        }

        // Leaf start - SparklyPaper - parallel world ticking - Deadlock prevention
        if (Thread.currentThread() instanceof ca.spottedleaf.moonrise.common.util.TickThread.ServerLevelTickThread) {
            try {
                me.s0nd3r.watermc.core.async.world.WorldReadRequest request = new me.s0nd3r.watermc.core.async.world.WorldReadRequest(type, params, null);
                Object result = level.executeReadRequest(request);
                return (T) result;
            } catch (Exception e) {
                return defaultValue;
            }
        }
        // Leaf end - SparklyPaper - parallel world ticking - Deadlock prevention

        java.util.concurrent.CompletableFuture<Object> future = new java.util.concurrent.CompletableFuture<>();

        // Leaf start - SparklyPaper - parallel world ticking - Shutdown handling for async reads
        if (level.isShuttingDown()) {
            future.completeExceptionally(new IllegalStateException("World " + level.getWorld().getName() + " is shutting down. Cannot queue new buffered read: " + type));
        } else {
            me.s0nd3r.watermc.core.async.world.WorldReadRequest request = new me.s0nd3r.watermc.core.async.world.WorldReadRequest(type, params, future);
            level.asyncReadRequestQueue.offer(request); // Assumes queue exists on ServerLevel
        }
        // Leaf end - SparklyPaper - parallel world ticking - Shutdown handling for async reads

        try {
            Object result = future.join(); // Block until tick thread completes it
            if (result == null) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Buffered async read returned null for " + methodName + " - returning default.");
                return defaultValue;
            }
            return (T) result;
        } catch (java.util.concurrent.CompletionException e) {
            // Leaf start - SparklyPaper - parallel world ticking - Shutdown handling for async reads
            if (e.getCause() instanceof IllegalStateException && e.getCause().getMessage() != null && e.getCause().getMessage().contains("shutting down")) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Async block read for " + methodName + " cancelled due to world shutdown: " + e.getCause().getMessage());
            } else {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Async block read failed for " + methodName + " on tick thread.", e.getCause() != null ? e.getCause() : e);
            }
            return defaultValue; // Return default or rethrow if appropriate for your error handling strategy
            // Leaf end - SparklyPaper - parallel world ticking - Shutdown handling for async reads
        } catch (Exception e) {
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Unexpected error during async block read for " + methodName, e);
            return defaultValue;
        }
    }
    // Leaf end - SparklyPaper - parallel world ticking

    public net.minecraft.world.level.block.state.BlockState getNMS() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            if (needsBuffering(level)) {
                // Buffered path
                return executeBufferedRead(level, me.s0nd3r.watermc.core.async.world.ReadOperationType.BLOCK_GET_NMS_STATE, new Object[]{this.position}, Blocks.AIR.defaultBlockState(), "getNMS");
            } else {
                // Strict/Disabled/Non-ServerLevel path
                checkStrictMode(level, "getNMS");
                try {
                    return this.world.getBlockState(this.position);
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getNMS" + (level == null ? " (Not a ServerLevel)" : ""), e);
                    return Blocks.AIR.defaultBlockState();
                }
            }
        } else {
            return this.world.getBlockState(this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    public net.minecraft.world.level.material.FluidState getNMSFluid() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "getNMSFluid");

            try {
                return this.world.getFluidState(this.position);
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getNMSFluid" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState();
            }
        } else {
            return this.world.getFluidState(this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    public BlockPos getPosition() {
        return this.position;
    }

    public LevelAccessor getHandle() {
        return this.world;
    }

    @Override
    public World getWorld() {
        return this.world.getMinecraftWorld().getWorld();
    }

    public CraftWorld getCraftWorld() {
        return (CraftWorld) this.getWorld();
    }

    @Override
    public Location getLocation() {
        return CraftLocation.toBukkit(this.position, this.getWorld());
    }

    @Override
    public Location getLocation(Location loc) {
        if (loc != null) {
            loc.setWorld(this.getWorld());
            loc.setX(this.position.getX());
            loc.setY(this.position.getY());
            loc.setZ(this.position.getZ());
            loc.setYaw(0);
            loc.setPitch(0);
        }

        return loc;
    }

    public BlockVector getVector() {
        return new BlockVector(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public int getX() {
        return this.position.getX();
    }

    @Override
    public int getY() {
        return this.position.getY();
    }

    @Override
    public int getZ() {
        return this.position.getZ();
    }

    @Override
    public Chunk getChunk() {
        return this.getWorld().getChunkAt(this);
    }

    @Deprecated // Leaf - SparklyPaper - parallel world ticking - Magic value
    public void setData(final byte data) {
        this.setData(data, net.minecraft.world.level.block.Block.UPDATE_ALL);
    }

    @Deprecated // Leaf - SparklyPaper - parallel world ticking - Magic value
    public void setData(final byte data, boolean applyPhysics) {
        if (applyPhysics) {
            this.setData(data, net.minecraft.world.level.block.Block.UPDATE_ALL);
        } else {
            this.setData(data, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    private void setData(final byte data, @net.minecraft.world.level.block.Block.UpdateFlags int flags) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled && world instanceof ServerLevel serverWorld) ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(serverWorld, position, "Cannot modify world asynchronously"); // Leaf - SparklyPaper - parallel world ticking
        this.world.setBlock(this.position, CraftMagicNumbers.getBlock(this.getType(), data), flags);
    }

    @Override
    @Deprecated // Leaf - SparklyPaper - parallel world ticking - Magic value
    public byte getData() {
        net.minecraft.world.level.block.state.BlockState state = this.getNMS(); // Leaf - SparklyPaper - parallel world ticking
        return CraftMagicNumbers.toLegacyData(state);
    }

    @Override
    public BlockData getBlockData() {
        return CraftBlockData.fromData(this.getNMS());
    }

    @Override
    public void setType(final Material type) {
        this.setType(type, true);
    }

    @Override
    public void setType(Material type, boolean applyPhysics) {
        Preconditions.checkArgument(type != null, "Material cannot be null");
        // Leaf - SparklyPaper - parallel world ticking - Delegates to setBlockData, which delegates to setTypeAndData (which has checks)
        this.setBlockData(type.createBlockData(), applyPhysics);
    }

    @Override
    public void setBlockData(BlockData data) {
        this.setBlockData(data, true);
    }

    @Override
    public void setBlockData(BlockData data, boolean applyPhysics) {
        Preconditions.checkArgument(data != null, "BlockData cannot be null");
        this.setBlockState(((CraftBlockData) data).getState(), applyPhysics);
    }

    boolean setBlockState(final net.minecraft.world.level.block.state.BlockState state, final boolean applyPhysics) {
        return CraftBlock.setBlockState(this.world, this.position, this.getNMS(), state, applyPhysics);
    }

    public static boolean setBlockState(LevelAccessor world, BlockPos pos, net.minecraft.world.level.block.state.BlockState oldState, net.minecraft.world.level.block.state.BlockState newState, boolean applyPhysics) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled && world instanceof ServerLevel serverWorld) ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(serverWorld, pos, "Cannot modify world asynchronously"); // Leaf - SparklyPaper - parallel world ticking
        // SPIGOT-611: need to do this to prevent glitchiness. Easier to handle this here (like /setblock) than to fix weirdness in block entity cleanup
        if (oldState.hasBlockEntity() && newState.getBlock() != oldState.getBlock()) { // SPIGOT-3725 remove old block entity if block changes
            // SPIGOT-4612: faster - just clear tile
            if (world instanceof net.minecraft.world.level.Level) {
                ((net.minecraft.world.level.Level) world).removeBlockEntity(pos);
            } else {
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 0);
            }
        }

        if (applyPhysics) {
            return world.setBlock(pos, newState, net.minecraft.world.level.block.Block.UPDATE_ALL);
        } else {
            boolean success = world.setBlock(pos, newState,
                    net.minecraft.world.level.block.Block.UPDATE_CLIENTS |
                    net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE |
                    net.minecraft.world.level.block.Block.UPDATE_SKIP_ON_PLACE);
            if (success && world instanceof net.minecraft.world.level.Level) {
                world.getMinecraftWorld().sendBlockUpdated(
                    pos,
                    oldState,
                    newState,
                    net.minecraft.world.level.block.Block.UPDATE_ALL
                );
            }
            return success;
        }
    }

    @Override
    public Material getType() {
        return this.getNMS().getBukkitMaterial(); // Paper - optimise getType calls // Leaf - SparklyPaper - parallel world ticking
    }

    @Override
    public byte getLightLevel() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "getLightLevel"); // Strict check if applicable

            try {
                // Requires Level for getMaxLocalRawBrightness
                if (this.world instanceof net.minecraft.world.level.Level nmsLevel) {
                    return (byte) nmsLevel.getMaxLocalRawBrightness(this.position);
                }
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: getLightLevel called on non-Level - returning 0");
                return 0;
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getLightLevel" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return 0;
            }
        } else {
            return (byte) this.world.getMinecraftWorld().getMaxLocalRawBrightness(this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public byte getLightFromSky() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "getLightFromSky"); // Strict check if applicable

            try {
                // LevelAccessor has getBrightness
                return (byte) this.world.getBrightness(LightLayer.SKY, this.position);
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getLightFromSky" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return 0;
            }
        } else {
            return (byte) this.world.getBrightness(LightLayer.SKY, this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public byte getLightFromBlocks() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "getLightFromBlocks"); // Strict check if applicable

            try {
                // LevelAccessor has getBrightness
                return (byte) this.world.getBrightness(LightLayer.BLOCK, this.position);
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getLightFromBlocks" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return 0;
            }
        } else {
            return (byte) this.world.getBrightness(LightLayer.BLOCK, this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    public Block getFace(final BlockFace face) {
        return this.getRelative(face, 1);
    }

    public Block getFace(final BlockFace face, final int distance) {
        return this.getRelative(face, distance);
    }

    @Override
    public Block getRelative(final int modX, final int modY, final int modZ) {
        return this.getWorld().getBlockAt(this.getX() + modX, this.getY() + modY, this.getZ() + modZ);
    }

    @Override
    public Block getRelative(BlockFace face) {
        return this.getRelative(face, 1);
    }

    @Override
    public Block getRelative(BlockFace face, int distance) {
        return this.getRelative(face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }

    @Override
    public BlockFace getFace(final Block block) {
        BlockFace[] values = BlockFace.values();

        for (BlockFace face : values) {
            if ((this.getX() + face.getModX() == block.getX()) && (this.getY() + face.getModY() == block.getY()) && (this.getZ() + face.getModZ() == block.getZ())) {
                return face;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "CraftBlock{pos=" + this.position + ",type=" + this.getType() + ",data=" + this.getNMS() + ",fluid=" + this.getNMSFluid() + '}';
    }

    public static BlockFace notchToBlockFace(Direction notch) {
        if (notch == null) {
            return BlockFace.SELF;
        }
        switch (notch) {
            case DOWN:
                return BlockFace.DOWN;
            case UP:
                return BlockFace.UP;
            case NORTH:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.SOUTH;
            case WEST:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.EAST;
            default:
                return BlockFace.SELF;
        }
    }

    public static Direction blockFaceToNotch(BlockFace face) {
        if (face == null) {
            return null;
        }
        switch (face) {
            case DOWN:
                return Direction.DOWN;
            case UP:
                return Direction.UP;
            case NORTH:
                return Direction.NORTH;
            case SOUTH:
                return Direction.SOUTH;
            case WEST:
                return Direction.WEST;
            case EAST:
                return Direction.EAST;
            default:
                return null;
        }
    }

    @Override
    public BlockState getState() {
        return CraftBlockStates.getBlockState(this, me.s0nd3r.watermc.core.config.modules.opt.TileEntitySnapshotCreation.enabled); // Leaf - EMC - Don't use snapshots for acquiring blockstate
    }

    // Paper start
    @Override
    public BlockState getState(boolean useSnapshot) {
        return CraftBlockStates.getBlockState(this, useSnapshot);
    }
    // Paper end

    @Override
    public Biome getBiome() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            if (needsBuffering(level)) {
                // Buffered path
                net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> nmsResult = executeBufferedRead(
                    level, me.s0nd3r.watermc.core.async.world.ReadOperationType.BLOCK_GET_BIOME, new Object[]{this.position}, null, "getBiome"
                );
                return (nmsResult != null) ? CraftBiome.minecraftHolderToBukkit(nmsResult) : Biome.PLAINS; // Default biome
            } else {
                // Strict/Disabled/Non-ServerLevel path
                checkStrictMode(level, "getBiome");
                try {
                    // Use Bukkit API, which should delegate safely (or buffer itself if needed)
                    return this.getWorld().getBiome(this.getX(), this.getY(), this.getZ());
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getBiome" + (level == null ? " (Not a ServerLevel)" : ""), e);
                    return Biome.PLAINS; // Default biome on error
                }
            }
        }  else {
            return this.getWorld().getBiome(this.getX(), this.getY(), this.getZ());
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    // Paper start
    @Override
    public Biome getComputedBiome() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            if (needsBuffering(level)) {
                // Buffered path
                net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> nmsResult = executeBufferedRead(
                    level, me.s0nd3r.watermc.core.async.world.ReadOperationType.BLOCK_GET_COMPUTED_BIOME, new Object[]{this.position}, null, "getComputedBiome"
                );
                return (nmsResult != null) ? CraftBiome.minecraftHolderToBukkit(nmsResult) : Biome.PLAINS; // Default biome
            } else {
                // Strict/Disabled/Non-ServerLevel path
                checkStrictMode(level, "getComputedBiome");
                try {
                    // Use Bukkit API
                    return this.getWorld().getComputedBiome(this.getX(), this.getY(), this.getZ());
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getComputedBiome" + (level == null ? " (Not a ServerLevel)" : ""), e);
                    return Biome.PLAINS; // Default biome on error
                }
            }
        } else {
            return this.getWorld().getComputedBiome(this.getX(), this.getY(), this.getZ());
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }
    // Paper end

    @Override
    public void setBiome(Biome bio) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled && world instanceof ServerLevel serverWorld) ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(serverWorld, position, "Cannot modify world asynchronously"); // Leaf - SparklyPaper - parallel world ticking
        this.getWorld().setBiome(this.getX(), this.getY(), this.getZ(), bio);
    }

    @Override
    public double getTemperature() {
        return this.world.getBiome(this.position).value().getTemperature(this.position, this.world.getSeaLevel());
    }

    @Override
    public double getHumidity() {
        return this.getWorld().getHumidity(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public boolean isBlockPowered() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "isBlockPowered"); // Strict check if applicable

            try {
                // Requires Level.getDirectSignalTo
                if (this.world instanceof net.minecraft.world.level.Level nmsLevel) {
                    return nmsLevel.getDirectSignalTo(this.position) > 0;
                }
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: isBlockPowered called on non-Level - returning false");
                return false;
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for isBlockPowered" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return false;
            }
        } else {
            return this.world.getMinecraftWorld().getDirectSignalTo(this.position) > 0;
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public boolean isBlockIndirectlyPowered() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            if (needsBuffering(level)) {
                // Buffered path
                return executeBufferedRead(level, me.s0nd3r.watermc.core.async.world.ReadOperationType.BLOCK_IS_INDIRECTLY_POWERED, new Object[]{this.position}, false, "isBlockIndirectlyPowered");
            } else {
                // Strict/Disabled/Non-ServerLevel path
                checkStrictMode(level, "isBlockIndirectlyPowered");
                try {
                    // Requires Level.hasNeighborSignal
                    if (this.world instanceof net.minecraft.world.level.Level nmsLevel) {
                        return nmsLevel.hasNeighborSignal(this.position);
                    }
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: isBlockIndirectlyPowered called on non-Level - returning false");
                    return false;
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for isBlockIndirectlyPowered" + (level == null ? " (Not a ServerLevel)" : ""), e);
                    return false;
                }
            }
        } else {
            return this.world.getMinecraftWorld().hasNeighborSignal(this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CraftBlock other)) {
            return false;
        }

        return this.position.equals(other.position) && this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        return this.position.hashCode() ^ this.getWorld().hashCode();
    }

    @Override
    public boolean isBlockFacePowered(BlockFace face) {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "isBlockFacePowered"); // Strict check if applicable

            try {
                if (this.world instanceof net.minecraft.world.level.Level nmsLevel) {
                    return nmsLevel.hasSignal(this.position, CraftBlock.blockFaceToNotch(face));
                }
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: isBlockFacePowered called on non-Level - returning false");
                return false;
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for isBlockFacePowered" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return false;
            }
        } else {
            return this.world.getMinecraftWorld().hasSignal(this.position, CraftBlock.blockFaceToNotch(face));
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public boolean isBlockFaceIndirectlyPowered(BlockFace face) {
        return pwt$isBlockFaceIndirectlyPowered(face); // Leaf - SparklyPaper - parallel world ticking
    }

    @Override
    public int getBlockPower(BlockFace face) {
        return pwt$getBlockPower(face); // Leaf - SparklyPaper - parallel world ticking
    }

    // Leaf - SparklyPaper - parallel world ticking - Static helper, safe
    public static int getPower(int power, net.minecraft.world.level.block.state.BlockState state) { // Leaf - SparklyPaper - parallel world ticking - private -> public
        if (!state.is(Blocks.REDSTONE_WIRE)) {
            return power;
        } else {
            return Math.max(state.getValue(RedStoneWireBlock.POWER), power);
        }
    }

    @Override
    public int getBlockPower() {
        return this.getBlockPower(BlockFace.SELF);
    }

    @Override
    public boolean isEmpty() {
        return this.getNMS().isAir();
    }

    @Override
    public boolean isLiquid() {
        return this.getNMS().liquid();
    }

    // Paper start
    @Override
    public boolean isBuildable() {
        return this.getNMS().isSolid(); // This is in fact isSolid, despite the fact that isSolid below returns blocksMotion
    }
    @Override
    public boolean isBurnable() {
        return this.getNMS().ignitedByLava();
    }
    @Override
    public boolean isReplaceable() {
        return this.getNMS().canBeReplaced();
    }
    @Override
    public boolean isSolid() {
        return this.getNMS().blocksMotion();
    }

    @Override
    public boolean isCollidable() {
        return getNMS().getBlock().hasCollision;
    }
    // Paper end

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        // Leaf - SparklyPaper - parallel world ticking - Uses safe getNMS()
        return PistonMoveReaction.getById(this.getNMS().getPistonPushReaction().ordinal());
    }

    @Override
    public boolean breakNaturally() {
        // Leaf start - SparklyPaper - parallel world ticking - Write operation check
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();
            if (level != null) {
                ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(level, position, "PWT: Cannot modify world asynchronously (breakNaturally)");
            } else {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Attempted breakNaturally on non-ServerLevel - safety not guaranteed.");
            }
            // Delegates to overload
            return this.breakNaturally(null, true, true); // Default to dropping XP and triggering effects
        } else {
            return this.breakNaturally(null);
        }
        // Leaf end - SparklyPaper - parallel world ticking - Write operation check
    }

    @Override
    public boolean breakNaturally(ItemStack item) {
        // Paper start
        return me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? this.breakNaturally(item, true, true) : this.breakNaturally(item, false); // Leaf - SparklyPaper - parallel world ticking - Delegates to full overload
    }

    @Override
    public boolean breakNaturally(boolean triggerEffect, boolean dropExperience) {
        return this.breakNaturally(null, triggerEffect, dropExperience);// Leaf - SparklyPaper - parallel world ticking - Delegates to full overload
    }

    @Override
    public boolean breakNaturally(ItemStack item, boolean triggerEffect, boolean dropExperience) {
        return this.breakNaturally(item, triggerEffect, dropExperience, false);
    }

    @Override
    public boolean breakNaturally(ItemStack item, boolean triggerEffect, boolean dropExperience, boolean forceEffect) {
        // Paper end
        return pwt$breakNaturally(item, triggerEffect, dropExperience, forceEffect); // Leaf - SparklyPaper - parallel world ticking - Write operation check
    }

    @Override
    public boolean applyBoneMeal(BlockFace face) {
        return pwt$applyBoneMeal(face); // Leaf - SparklyPaper - parallel world ticking - Write operation check
    }

    @Override
    public Collection<ItemStack> getDrops() {
        return this.getDrops(null);
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack item) {
        return this.getDrops(item, null);
    }

    @Override
    public Collection<ItemStack> getDrops(ItemStack item, Entity entity) {
        return pwt$getDrops(item, entity); // Leaf - SparklyPaper - parallel world ticking - Write operation check
    }

    @Override
    public boolean isPreferredTool(ItemStack item) {
        // Leaf - SparklyPaper - parallel world ticking - Uses safe getNMS()
        net.minecraft.world.level.block.state.BlockState state = this.getNMS();
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        return CraftBlockData.isPreferredTool(state, nms);
    }

    @Override
    public float getBreakSpeed(Player player) {
        Preconditions.checkArgument(player != null, "player cannot be null");
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            // Requires LevelReader (LevelAccessor is sufficient)
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "getBreakSpeed"); // Strict check if applicable

            try {
                // Uses safe getNMS()
                return this.getNMS().getDestroyProgress(((CraftPlayer) player).getHandle(), this.world, this.position);
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getBreakSpeed" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return 0.0f;
            }
        } else {
            return this.getNMS().getDestroyProgress(((CraftPlayer) player).getHandle(), this.world, this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    // Leaf - SparklyPaper - parallel world ticking - Metadata methods delegate to CraftWorld, assumed safe
    @Override
    public void setMetadata(String metadataKey, MetadataValue newMetadataValue) {
        this.getCraftWorld().getBlockMetadata().setMetadata(this, metadataKey, newMetadataValue);
    }

    @Override
    public List<MetadataValue> getMetadata(String metadataKey) {
        return this.getCraftWorld().getBlockMetadata().getMetadata(this, metadataKey);
    }

    @Override
    public boolean hasMetadata(String metadataKey) {
        return this.getCraftWorld().getBlockMetadata().hasMetadata(this, metadataKey);
    }

    @Override
    public void removeMetadata(String metadataKey, Plugin owningPlugin) {
        this.getCraftWorld().getBlockMetadata().removeMetadata(this, metadataKey, owningPlugin);
    }

    @Override
    public boolean isPassable() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            // Requires LevelReader (LevelAccessor is sufficient)
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "isPassable"); // Strict check if applicable

            try {
                // Uses safe getNMS()
                VoxelShape shape = this.getNMS().getCollisionShape(this.world, this.position);
                return shape.isEmpty();
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for isPassable" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return true; // Default to passable on error? Or false? Passable seems safer.
            }
        } else {
            return this.getNMS().getCollisionShape(this.world, this.position).isEmpty();
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public RayTraceResult rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        Preconditions.checkArgument(start != null, "Location start cannot be null");
        Preconditions.checkArgument(this.getWorld().equals(start.getWorld()), "Location start cannot be a different world");
        start.checkFinite();

        Preconditions.checkArgument(direction != null, "Vector direction cannot be null");
        direction.checkFinite();
        Preconditions.checkArgument(direction.lengthSquared() > 0, "Direction's magnitude (%s) must be greater than 0", direction.lengthSquared());

        Preconditions.checkArgument(fluidCollisionMode != null, "FluidCollisionMode cannot be null");
        if (maxDistance < 0.0D) {
            return null;
        }

        return pwt$rayTrace(start, direction, maxDistance, fluidCollisionMode); // Leaf - SparklyPaper - parallel world ticking
    }

    @Override
    public BoundingBox getBoundingBox() {
        return pwt$getBoundingBox(); // Leaf - SparklyPaper - parallel world ticking
    }

    @Override
    public org.bukkit.util.VoxelShape getCollisionShape() {
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            // Requires LevelReader (LevelAccessor is sufficient)
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "getCollisionShape"); // Strict check if applicable

            try {
                // Uses safe getNMS()
                VoxelShape shape = this.getNMS().getCollisionShape(this.world, this.position);
                return new CraftVoxelShape(shape); // Wrap NMS shape
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getCollisionShape" + (level == null ? " (Not a ServerLevel)" : ""), e);
                return new CraftVoxelShape(net.minecraft.world.phys.shapes.Shapes.empty()); // Default to empty shape on error
            }
        } else {
            VoxelShape shape = this.getNMS().getCollisionShape(this.world, this.position);
            return new CraftVoxelShape(shape);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public boolean canPlace(BlockData data) {
        Preconditions.checkArgument(data != null, "BlockData cannot be null");
        // Leaf start - SparklyPaper - parallel world ticking
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            if (needsBuffering(level)) {
                // Buffered path
                return executeBufferedRead(level, me.s0nd3r.watermc.core.async.world.ReadOperationType.BLOCK_CAN_PLACE, new Object[]{this.position, data}, false, "canPlace");
            } else {
                // Strict/Disabled/Non-ServerLevel path
                checkStrictMode(level, "canPlace");
                try {
                    net.minecraft.world.level.block.state.BlockState nmsData = ((CraftBlockData) data).getState();
                    // LevelAccessor has canSurvive
                    return nmsData.canSurvive(this.world, this.position);
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for canPlace" + (level == null ? " (Not a ServerLevel)" : ""), e);
                    return false; // Default to false on error
                }
            }
        } else {
            net.minecraft.world.level.block.state.BlockState iblockdata = ((CraftBlockData) data).getState();
            net.minecraft.world.level.Level world = this.world.getMinecraftWorld();

            return iblockdata.canSurvive(world, this.position);
        }
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public String getTranslationKey() {
        return this.getNMS().getBlock().getDescriptionId();
    }

    @Override
    public boolean isSuffocating() {
        return this.getNMS().isSuffocating(this.world, this.position);
    }

    // Paper start
    @Override
    public com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup() {
        // Leaf start - SparklyPaper - parallel world ticking
        net.minecraft.world.level.block.SoundType nmsSoundType = me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled ? this.getNMS().getSoundType() : getNMS().getBlock().defaultBlockState().getSoundType();
        return new com.destroystokyo.paper.block.CraftBlockSoundGroup(nmsSoundType);
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public org.bukkit.SoundGroup getBlockSoundGroup() {
        return org.bukkit.craftbukkit.CraftSoundGroup.getSoundGroup(this.getNMS().getSoundType());
    }

    @Override
    public String translationKey() {
        return this.getNMS().getBlock().getDescriptionId();
    }

    public boolean isValidTool(ItemStack itemStack) {
        return getDrops(itemStack).size() != 0;
    }

    @Override
    public void tick() {
        // Leaf start - SparklyPaper - parallel world ticking - Write operation check
        // (block ticks can modify state)
        ServerLevel level;
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            level = getServerLevel();
            if (level != null) {
                ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(level, position, "PWT: Cannot tick block asynchronously (tick)");
            } else {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Attempted tick on non-ServerLevel - safety not guaranteed.");
                return; // Cannot tick without ServerLevel
            }
        } else {
            level = this.world.getMinecraftWorld();
        }
        // Leaf end - SparklyPaper - parallel world ticking -

        this.getNMS().tick(level, this.position, level.random);
    }


    @Override
    public void fluidTick() {
        // Leaf start - SparklyPaper - parallel world ticking
        // Fluid ticks can modify state
        ServerLevel level;
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            level = getServerLevel();
            if (level != null) {
                ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(level, position, "PWT: Cannot tick fluid asynchronously (fluidTick)");
            } else {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Attempted fluidTick on non-ServerLevel - safety not guaranteed.");
                return; // Cannot tick fluid without ServerLevel
            }
        } else {
            level = this.world.getMinecraftWorld();
        }
        this.getNMSFluid().tick(level, this.position, this.getNMS());
        // Leaf end - SparklyPaper - parallel world ticking
    }

    @Override
    public void randomTick() {
        // Leaf start - SparklyPaper - parallel world ticking - Write operation check
        // (random ticks can modify state)
        ServerLevel level;
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            level = getServerLevel();
            if (level != null) {
                ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(level, position, "PWT: Cannot randomTick block asynchronously");
            } else {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Attempted randomTick on non-ServerLevel - safety not guaranteed.");
                return;
            }
        } else {
            level = this.world.getMinecraftWorld();
        }
        // Leaf end - SparklyPaper - parallel world ticking - Write operation check
        this.getNMS().randomTick(level, this.position, level.random);
    }
    // Paper end

    private boolean pwt$isBlockFaceIndirectlyPowered(BlockFace face) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "isBlockFaceIndirectlyPowered"); // Strict check if applicable

            try {
                // Requires Level.getSignal and potentially relative block access (which might need safety checks)
                if (this.world instanceof net.minecraft.world.level.Level nmsLevel) {
                    int power = nmsLevel.getSignal(this.position, CraftBlock.blockFaceToNotch(face));
                    Block relative = this.getRelative(face); // getRelative delegates, safety depends on target block
                    if (relative.getType() == Material.REDSTONE_WIRE) {
                        // getData might need buffering if called on relative block
                        return Math.max(power, relative.getData()) > 0;
                    }
                    return power > 0;
                }
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: isBlockFaceIndirectlyPowered called on non-Level - returning false");
                return false;
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for isBlockFaceIndirectlyPowered" + (level == null ? " (Not a ServerLevel)" : ""), e);

                return false;
            }
        } else {
            int power = this.world.getMinecraftWorld().getSignal(this.position, CraftBlock.blockFaceToNotch(face));

            Block relative = this.getRelative(face);
            if (relative.getType() == Material.REDSTONE_WIRE) {
                return Math.max(power, relative.getData()) > 0; // todo remove legacy usage
            }

            return power > 0;
        }
    }

    private int pwt$getBlockPower(BlockFace face) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            if (needsBuffering(level)) {
                // Buffered path
                return executeBufferedRead(level, me.s0nd3r.watermc.core.async.world.ReadOperationType.BLOCK_GET_BLOCK_POWER, new Object[]{this.position, face}, 0, "getBlockPower");
            } else {
                // Strict/Disabled/Non-ServerLevel path
                checkStrictMode(level, "getBlockPower");
                try {
                    // Requires Level for hasSignal and getBlockState
                    if (this.world instanceof net.minecraft.world.level.Level nmsLevel) {
                        int power = 0;
                        BlockPos currentPos = this.position; // Use immutable position

                        // Check neighbors using relative positions
                        if ((face == BlockFace.DOWN || face == BlockFace.SELF) && nmsLevel.hasSignal(currentPos.below(), Direction.DOWN))
                            power = CraftBlock.getPower(power, nmsLevel.getBlockState(currentPos.below()));
                        if ((face == BlockFace.UP || face == BlockFace.SELF) && nmsLevel.hasSignal(currentPos.above(), Direction.UP))
                            power = CraftBlock.getPower(power, nmsLevel.getBlockState(currentPos.above()));
                        if ((face == BlockFace.EAST || face == BlockFace.SELF) && nmsLevel.hasSignal(currentPos.east(), Direction.EAST))
                            power = CraftBlock.getPower(power, nmsLevel.getBlockState(currentPos.east()));
                        if ((face == BlockFace.WEST || face == BlockFace.SELF) && nmsLevel.hasSignal(currentPos.west(), Direction.WEST))
                            power = CraftBlock.getPower(power, nmsLevel.getBlockState(currentPos.west()));
                        if ((face == BlockFace.NORTH || face == BlockFace.SELF) && nmsLevel.hasSignal(currentPos.north(), Direction.NORTH))
                            power = CraftBlock.getPower(power, nmsLevel.getBlockState(currentPos.north()));
                        if ((face == BlockFace.SOUTH || face == BlockFace.SELF) && nmsLevel.hasSignal(currentPos.south(), Direction.SOUTH))
                            power = CraftBlock.getPower(power, nmsLevel.getBlockState(currentPos.south()));

                        // Need to call isBlockIndirectlyPowered/isBlockFaceIndirectlyPowered safely
                        boolean indirect = (face == BlockFace.SELF) ? this.isBlockIndirectlyPowered() : this.isBlockFaceIndirectlyPowered(face);
                        return power > 0 ? power : (indirect ? 15 : 0);
                    } else {
                        org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: getBlockPower called on non-Level - returning 0");
                        return 0;
                    }
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getBlockPower" + (level == null ? " (Not a ServerLevel)" : ""), e);
                    return 0;
                }
            }
        } else {
            int power = 0;
            net.minecraft.world.level.Level world = this.world.getMinecraftWorld();
            int x = this.getX();
            int y = this.getY();
            int z = this.getZ();
            if ((face == BlockFace.DOWN || face == BlockFace.SELF) && world.hasSignal(new BlockPos(x, y - 1, z), Direction.DOWN))
                power = CraftBlock.getPower(power, world.getBlockState(new BlockPos(x, y - 1, z)));
            if ((face == BlockFace.UP || face == BlockFace.SELF) && world.hasSignal(new BlockPos(x, y + 1, z), Direction.UP))
                power = CraftBlock.getPower(power, world.getBlockState(new BlockPos(x, y + 1, z)));
            if ((face == BlockFace.EAST || face == BlockFace.SELF) && world.hasSignal(new BlockPos(x + 1, y, z), Direction.EAST))
                power = CraftBlock.getPower(power, world.getBlockState(new BlockPos(x + 1, y, z)));
            if ((face == BlockFace.WEST || face == BlockFace.SELF) && world.hasSignal(new BlockPos(x - 1, y, z), Direction.WEST))
                power = CraftBlock.getPower(power, world.getBlockState(new BlockPos(x - 1, y, z)));
            if ((face == BlockFace.NORTH || face == BlockFace.SELF) && world.hasSignal(new BlockPos(x, y, z - 1), Direction.NORTH))
                power = CraftBlock.getPower(power, world.getBlockState(new BlockPos(x, y, z - 1)));
            if ((face == BlockFace.SOUTH || face == BlockFace.SELF) && world.hasSignal(new BlockPos(x, y, z + 1), Direction.SOUTH))
                power = CraftBlock.getPower(power, world.getBlockState(new BlockPos(x, y, z + 1)));
            return power > 0 ? power : (face == BlockFace.SELF ? this.isBlockIndirectlyPowered() : this.isBlockFaceIndirectlyPowered(face)) ? 15 : 0;
        }
    }

    private boolean pwt$breakNaturally(ItemStack item, boolean triggerEffect, boolean dropExperience, boolean forceEffect) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            // Order matters here, need to drop before setting to air so skulls can get their data
            // (already done in simpler overload, but check again for safety)
            ServerLevel level = getServerLevel();
            if (level != null) {
                ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(level, position, "PWT: Cannot modify world asynchronously (breakNaturally item...)");
            } else {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Attempted breakNaturally(item...) on non-ServerLevel - safety not guaranteed.");
            }

            // Get NMS state safely

            net.minecraft.world.level.block.state.BlockState state = this.getNMS();
            net.minecraft.world.level.block.Block block = state.getBlock();
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            boolean droppedItems = false;

            // Experience dropping requires ServerLevel
            ServerLevel serverLevelForDrops = getServerLevel(); // Re-get ServerLevel specifically for drop logic

            if (serverLevelForDrops != null) { // Only attempt drops/XP if we have a ServerLevel
                // Check if block should drop items
                if (!state.isAir() && (item == null || !state.requiresCorrectToolForDrops() || nmsItem.isCorrectToolForDrops(state))) {
                    // Drop items using ServerLevel
                    net.minecraft.world.level.block.Block.dropResources(state, serverLevelForDrops, this.position, this.world.getBlockEntity(this.position), null, nmsItem, false);

                    // Drop experience using ServerLevel
                    if (dropExperience) {
                        int xp = block.getExpDrop(state, serverLevelForDrops, this.position, nmsItem, true);
                        if (xp > 0) { // Only pop if there's XP to drop
                            block.popExperience(serverLevelForDrops, this.position, xp);

                        }
                    }
                    droppedItems = true;
                }
            } else {
                // Log if we couldn't drop XP because it wasn't a ServerLevel
                if (dropExperience && !state.isAir()) { // Only warn if XP was requested and block wasn't air
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Cannot drop experience for breakNaturally: Not a ServerLevel.");
                }
                // Still trigger effects if requested and possible with LevelAccessor
                if (triggerEffect && !state.isAir()) {
                    int eventId = (state.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock)
                        ? net.minecraft.world.level.block.LevelEvent.SOUND_EXTINGUISH_FIRE
                        : net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK;
                    int eventData = (eventId == net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK)
                        ? net.minecraft.world.level.block.Block.getId(state) : 0;
                    this.world.levelEvent(eventId, this.position, eventData);
                }
            }

            // Trigger effect using LevelAccessor (safe)
            if ((droppedItems && triggerEffect) || (forceEffect && block != Blocks.AIR)) {
                int eventId = (state.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock)
                    ? net.minecraft.world.level.block.LevelEvent.SOUND_EXTINGUISH_FIRE
                    : net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK;
                int eventData = (eventId == net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK)
                    ? net.minecraft.world.level.block.Block.getId(state) : 0;
                this.world.levelEvent(eventId, this.position, eventData);
            }

            // Remove the block using LevelAccessor (safe)
            boolean destroyed = this.world.removeBlock(this.position, false);
            if (destroyed) {
                // Call destroy hook using LevelAccessor (safe)
                block.destroy(this.world, this.position, state);
            }
            // Special case handling - requires Level, check again
            if (droppedItems && this.world instanceof net.minecraft.world.level.Level nmsLevelForSpecialCases) {
                if (block instanceof net.minecraft.world.level.block.IceBlock iceBlock) {
                    iceBlock.afterDestroy(nmsLevelForSpecialCases, this.position, nmsItem);
                } else if (block instanceof net.minecraft.world.level.block.TurtleEggBlock turtleEggBlock) {
                    turtleEggBlock.decreaseEggs(nmsLevelForSpecialCases, this.position, state);
                }
            } else if (droppedItems) { // Log if special cases couldn't run
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Cannot perform post-break special actions: Not a Level.");
            }
            // Return true if the block was successfully destroyed AND items were dropped (or would have dropped if tool was right)
            return destroyed && droppedItems;
        } else {
            // Order matters here, need to drop before setting to air so skulls can get their data
            net.minecraft.world.level.block.state.BlockState state = this.getNMS();
            net.minecraft.world.level.block.Block block = state.getBlock();
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            boolean result = false;

            // Modelled off Player#hasCorrectToolForDrops
            if (block != Blocks.AIR && (item == null || !state.requiresCorrectToolForDrops() || nmsItem.isCorrectToolForDrops(state))) {
                net.minecraft.world.level.block.Block.dropResources(state, this.world.getMinecraftWorld(), this.position, this.world.getBlockEntity(this.position), null, nmsItem, false); // Paper - Properly handle xp dropping
                // Paper start - improve Block#breakNaturally
                if (dropExperience)
                    block.popExperience(this.world.getMinecraftWorld(), this.position, block.getExpDrop(state, this.world.getMinecraftWorld(), this.position, nmsItem, true));
                // Paper end
                result = true;
            }

            if ((result && triggerEffect) || (forceEffect && block != Blocks.AIR)) {
                if (state.getBlock() instanceof net.minecraft.world.level.block.BaseFireBlock) {
                    this.world.levelEvent(net.minecraft.world.level.block.LevelEvent.SOUND_EXTINGUISH_FIRE, this.position, 0);
                } else {
                    this.world.levelEvent(net.minecraft.world.level.block.LevelEvent.PARTICLES_DESTROY_BLOCK, this.position, net.minecraft.world.level.block.Block.getId(state));
                }
            }

            // SPIGOT-6778: Directly call setBlock instead of setBlockState, so that the block entity is not removed and custom remove logic is run.
            // Paper start - improve breakNaturally
            boolean destroyed = this.world.removeBlock(this.position, false);
            if (destroyed) {
                block.destroy(this.world, this.position, state);
            }
            if (result) {
                // special cases
                if (block instanceof net.minecraft.world.level.block.IceBlock iceBlock) {
                    iceBlock.afterDestroy(this.world.getMinecraftWorld(), this.position, nmsItem);
                } else if (block instanceof net.minecraft.world.level.block.TurtleEggBlock turtleEggBlock) {
                    turtleEggBlock.decreaseEggs(this.world.getMinecraftWorld(), this.position, state);
                }
            }
            return destroyed && result;
            // Paper end
        }
    }

    private boolean pwt$applyBoneMeal(BlockFace face) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();
            if (level != null) {
                ca.spottedleaf.moonrise.common.util.TickThread.ensureTickThread(level, position, "PWT: Cannot modify world asynchronously (applyBoneMeal)");
            } else {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Attempted applyBoneMeal on non-ServerLevel - safety not guaranteed.");
                return false; // Cannot bonemeal without ServerLevel
            }

            // Original logic requires ServerLevel (level is guaranteed non-null here)
            Direction direction = CraftBlock.blockFaceToNotch(face);
            BlockFertilizeEvent event = null;
            UseOnContext context = new UseOnContext(level, null, InteractionHand.MAIN_HAND, Items.BONE_MEAL.getDefaultInstance(), new BlockHitResult(Vec3.ZERO, direction, this.getPosition(), false));

            // SPIGOT-6895: Call StructureGrowEvent and BlockFertilizeEvent
            level.captureTreeGeneration = true;
            InteractionResult result = BoneMealItem.applyBonemeal(context);
            level.captureTreeGeneration = false;

            if (!level.capturedBlockStates.isEmpty()) {
                TreeType treeType = SaplingBlock.getTreeTypeRT(); // Use thread-local getter
                SaplingBlock.setTreeTypeRT(null); // Use thread-local setter
                // Need Bukkit BlockState list for events
                List<org.bukkit.block.BlockState> bukkitStates = new ArrayList<>(level.capturedBlockStates.values());
                level.capturedBlockStates.clear(); // Clear NMS map
                StructureGrowEvent structureEvent = null;

                if (treeType != null) {
                    structureEvent = new StructureGrowEvent(this.getLocation(), treeType, true, null, bukkitStates);
                    Bukkit.getPluginManager().callEvent(structureEvent);
                }

                event = new BlockFertilizeEvent(this, null, bukkitStates); // Use 'this' as the CraftBlock
                event.setCancelled(structureEvent != null && structureEvent.isCancelled());
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    for (BlockState state : bukkitStates) {
                        CraftBlockState craftBlockState = (CraftBlockState) state;
                        craftBlockState.place(craftBlockState.getFlags()); // This performs the actual block changes
                        // Notify observers using the captured state info
                        level.checkCapturedTreeStateForObserverNotify(this.position, craftBlockState);
                    }
                }
            }

            // Return true only if bonemeal succeeded AND the event wasn't cancelled
            return result == InteractionResult.SUCCESS && (event == null || !event.isCancelled());
        } else {
            Direction direction = CraftBlock.blockFaceToNotch(face);
            BlockFertilizeEvent event = null;
            ServerLevel world = this.getCraftWorld().getHandle();
            UseOnContext context = new UseOnContext(world, null, InteractionHand.MAIN_HAND, Items.BONE_MEAL.getDefaultInstance(), new BlockHitResult(Vec3.ZERO, direction, this.getPosition(), false));

            // SPIGOT-6895: Call StructureGrowEvent and BlockFertilizeEvent
            world.captureTreeGeneration = true;
            InteractionResult result = BoneMealItem.applyBonemeal(context);
            world.captureTreeGeneration = false;

            if (!world.capturedBlockStates.isEmpty()) {
                TreeType treeType = SaplingBlock.treeType;
                SaplingBlock.treeType = null;
                List<BlockState> states = new ArrayList<>(world.capturedBlockStates.values());
                world.capturedBlockStates.clear();
                StructureGrowEvent structureEvent = null;

                if (treeType != null) {
                    structureEvent = new StructureGrowEvent(this.getLocation(), treeType, true, null, states);
                    Bukkit.getPluginManager().callEvent(structureEvent);
                }

                event = new BlockFertilizeEvent(CraftBlock.at(world, this.getPosition()), null, states);
                event.setCancelled(structureEvent != null && structureEvent.isCancelled());
                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled()) {
                    for (BlockState state : states) {
                        CraftBlockState craftBlockState = (CraftBlockState) state;
                        craftBlockState.place(craftBlockState.getFlags());
                        world.checkCapturedTreeStateForObserverNotify(this.position, craftBlockState); // Paper - notify observers even if grow failed
                    }
                }
            }

            return result == InteractionResult.SUCCESS && (event == null || !event.isCancelled());
        }
    }

    private Collection<ItemStack> pwt$getDrops(ItemStack item, Entity entity) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            // Requires ServerLevel for Block.getDrops call
            ServerLevel level = getServerLevel();
            if (level == null) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.WARNING, "PWT: Cannot getDrops: Not a ServerLevel.");
                return Collections.emptyList();
            }

            checkStrictMode(level, "getDrops"); // Strict check if applicable

            try {
                net.minecraft.world.level.block.state.BlockState state = this.getNMS(); // Use safe getNMS
                net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
                net.minecraft.world.entity.Entity nmsEntity = (entity == null) ? null : ((CraftEntity) entity).getHandle();

                // Check tool requirement
                if (item == null || !state.requiresCorrectToolForDrops() || nmsItem.isCorrectToolForDrops(state)) {
                    // Call NMS getDrops using the verified ServerLevel
                    List<net.minecraft.world.item.ItemStack> nmsDrops = net.minecraft.world.level.block.Block.getDrops(
                        state, level, this.position, this.world.getBlockEntity(this.position), nmsEntity, nmsItem
                    );
                    // Convert to Bukkit ItemStacks
                    return nmsDrops.stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
                } else {
                    // Tool was required but not correct/present
                    return Collections.emptyList();
                }
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getDrops", e);
                return Collections.emptyList();
            }
        } else {
            net.minecraft.world.level.block.state.BlockState state = this.getNMS();
            net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);

            // Modelled off Player#hasCorrectToolForDrops
            if (item == null || CraftBlockData.isPreferredTool(state, nms)) {
                return net.minecraft.world.level.block.Block.getDrops(state, this.world.getMinecraftWorld(), this.position, this.world.getBlockEntity(this.position), entity == null ? null : ((CraftEntity) entity).getHandle(), nms)
                    .stream().map(CraftItemStack::asBukkitCopy).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }
    }

    private RayTraceResult pwt$rayTrace(Location start, Vector direction, double maxDistance, FluidCollisionMode fluidCollisionMode) {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            ServerLevel level = getServerLevel();

            if (needsBuffering(level)) {
                // Buffered path
                HitResult nmsHitResult = executeBufferedRead(
                    level, me.s0nd3r.watermc.core.async.world.ReadOperationType.BLOCK_RAY_TRACE,
                    new Object[]{this.position, start, direction, maxDistance, fluidCollisionMode}, // Pass all params
                    null, "rayTrace"
                );
                // Convert NMS result (can be null) to Bukkit result
                return CraftRayTraceResult.convertFromInternal(this.world, nmsHitResult);
            } else {
                // Strict/Disabled/Non-ServerLevel path
                checkStrictMode(level, "rayTrace");
                try {
                    // Calculate start and end points for the ray trace
                    Vec3 startPos = CraftLocation.toVec3(start);
                    Vector dirNormalized = direction.clone().normalize(); // Normalize once
                    Vec3 endPos = startPos.add(dirNormalized.getX() * maxDistance, dirNormalized.getY() * maxDistance, dirNormalized.getZ() * maxDistance);

                    // Perform the clip using LevelAccessor (safe)
                    // Pass the block's position as the context position for the clip function
                    HitResult nmsHitResult = this.world.clip(
                        new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, CraftFluidCollisionMode.toFluid(fluidCollisionMode), CollisionContext.empty()),
                        this.position // Provide the block's position here
                    );

                    // Convert NMS result to Bukkit result
                    return CraftRayTraceResult.convertFromInternal(this.world, nmsHitResult);
                } catch (Exception e) {
                    org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for rayTrace" + (level == null ? " (Not a ServerLevel)" : ""), e);
                    return null; // Return null on error
                }
            }
        } else {
            Vector dir = direction.clone().normalize().multiply(maxDistance);
            Vec3 startPos = CraftLocation.toVec3(start);
            Vec3 endPos = startPos.add(dir.getX(), dir.getY(), dir.getZ());

            HitResult hitResult = this.world.clip(new ClipContext(startPos, endPos, ClipContext.Block.OUTLINE, CraftFluidCollisionMode.toFluid(fluidCollisionMode), CollisionContext.empty()), this.position);
            return CraftRayTraceResult.convertFromInternal(this.world, hitResult);
        }
    }

    private BoundingBox pwt$getBoundingBox() {
        if (me.s0nd3r.watermc.core.config.modules.async.SparklyPaperParallelWorldTicking.enabled) {
            // Requires LevelReader (LevelAccessor is sufficient)
            ServerLevel level = getServerLevel();

            checkStrictMode(level, "getBoundingBox"); // Strict check if applicable

            try {
                // Uses safe getNMS()
                VoxelShape shape = this.getNMS().getShape(this.world, this.position);
                if (shape.isEmpty()) {
                    // Return a zero-sized box at the block's corner if shape is empty
                    return new BoundingBox(getX(), getY(), getZ(), getX(), getY(), getZ());
                }
                // Get AABB relative to 0,0,0 and offset by block position
                AABB aabb = shape.bounds();
                return new BoundingBox(
                    this.getX() + aabb.minX, this.getY() + aabb.minY, this.getZ() + aabb.minZ,
                    this.getX() + aabb.maxX, this.getY() + aabb.maxY, this.getZ() + aabb.maxZ
                );
            } catch (Exception e) {
                org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "PWT: Direct access failed for getBoundingBox" + (level == null ? " (Not a ServerLevel)" : ""), e);
                // Default to a full 1x1x1 box on error
                return new BoundingBox(getX(), getY(), getZ(), getX() + 1, getY() + 1, getZ() + 1);
            }
        } else {
            VoxelShape shape = this.getNMS().getShape(this.world, this.position);

            if (shape.isEmpty()) {
                return new BoundingBox(); // Return an empty bounding box if the block has no dimension
            }

            AABB aabb = shape.bounds();
            return new BoundingBox(this.getX() + aabb.minX, this.getY() + aabb.minY, this.getZ() + aabb.minZ, this.getX() + aabb.maxX, this.getY() + aabb.maxY, this.getZ() + aabb.maxZ);
        }
    }
}
