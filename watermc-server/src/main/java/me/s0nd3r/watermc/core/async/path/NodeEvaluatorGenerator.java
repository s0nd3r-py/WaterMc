package me.s0nd3r.watermc.core.async.path;

import net.minecraft.world.level.pathfinder.NodeEvaluator;

public interface NodeEvaluatorGenerator {
    NodeEvaluator generate(NodeEvaluatorFeatures nodeEvaluatorFeatures);
}
