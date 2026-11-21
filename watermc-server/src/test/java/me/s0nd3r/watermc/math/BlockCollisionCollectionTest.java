package me.s0nd3r.watermc.math;

import ca.spottedleaf.moonrise.patches.collisions.CollisionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import me.s0nd3r.watermc.math.reference.ReferenceCollisions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BlockCollisionCollectionTest {
    @BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void primitiveCollectorMatchesOriginalForBlocksEdgesFiltersAndUnloadedChunks() {
        Level world = mock(Level.class);
        ChunkSource source = mock(ChunkSource.class);
        ChunkAccess chunk = mock(ChunkAccess.class);
        when(world.getChunkSource()).thenReturn(source);
        when(world.getMinSectionY()).thenReturn(0);
        when(world.getMaxSectionY()).thenReturn(0);
        when(world.getBlockState(any(BlockPos.class))).thenReturn(Blocks.AIR.defaultBlockState());
        PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY), null);
        LevelChunkSection section = new LevelChunkSection(states, null);
        BlockState[] samples = {Blocks.STONE.defaultBlockState(), Blocks.STONE_SLAB.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState(), Blocks.OAK_STAIRS.defaultBlockState()};
        for (int z = 0; z < 16; ++z) for (int x = 0; x < 16; ++x) section.setBlockState(x, 0, z, samples[(x + z) & 3]);
        when(source.getChunk(eq(0), eq(0), eq(ChunkStatus.FULL), anyBoolean())).thenReturn(chunk);
        when(chunk.getSections()).thenReturn(new LevelChunkSection[]{section});
        Random random = new Random(20261110);
        for (int test = 0; test < 400; ++test) {
            double x = random.nextDouble()*20 - 2, z = random.nextDouble()*20 - 2;
            AABB search = new AABB(x, -0.5, z, x + 2, 2, z + 2);
            List<VoxelShape> voxels = new ArrayList<>();
            List<AABB> boxes = new ArrayList<>();
            int flags = (test & 1) == 0 ? 0 : CollisionUtil.COLLISION_FLAG_COLLIDE_WITH_UNLOADED_CHUNKS;
            if ((test & 4) != 0) flags |= CollisionUtil.COLLISION_FLAG_CHECK_ONLY;
            BiPredicate<BlockState, BlockPos> filter = (test & 2) == 0 ? null : (state, pos) -> (pos.getX() & 1) == 0;
            boolean expectedFound = ReferenceCollisions.getCollisionsForBlocksOrWorldBorder(world, null, search, voxels, boxes, flags, filter);
            try (FastAABB flat = FastAABB.acquire()) {
                boolean actualFound = CollisionUtil.getCollisionsForBlocksOrWorldBorderFlat(world, null, search, flat.voxels(), flat, flags, filter);
                assertEquals(expectedFound, actualFound);
                assertEquals(boxes.size(), flat.size());
                assertEquals(voxels.size(), flat.voxels().size());
                AABB moving = new AABB(x, 1, z, x + 0.6, 2.8, z + 0.6);
                Vec3 movement = new Vec3(0.7, -1.2, -0.4);
                Vec3 expected = ReferenceCollisions.performCollisions(movement, moving, voxels, boxes);
                flat.resolve(movement.x, movement.y, movement.z, moving);
                assertEquals(expected.x, flat.resultX());
                assertEquals(expected.y, flat.resultY());
                assertEquals(expected.z, flat.resultZ());
            }
        }
    }
}
