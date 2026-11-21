package me.s0nd3r.watermc.engine.common.ai.non_poi_block_search;

// From Lithium (CaffeineMC: jcw780, 2No2Name; LGPL-3.0) via Leaf ver/26.3 "optimize non-POI block searches" (HaHaWTH).

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

public interface LithiumMoveToBlockGoal {
    boolean lithium$findNearestBlock(Predicate<BlockState> requiredBlock,
                                     BiPredicate<ChunkAccess, BlockPos.MutableBlockPos> lithium$isValidTarget,
                                     final boolean shouldChunkLoad);
}
