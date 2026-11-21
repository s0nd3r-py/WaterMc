package me.s0nd3r.watermc.core.async.path;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.world.level.pathfinder.BinaryHeap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import org.apache.commons.lang3.Validate;

/**
 * Пул {@link NodeEvaluator} для асинхронного поиска пути.
 * WaterMc: глобальный монитор заменён неблокирующими структурами — потоки путей больше не
 * сериализуются на получении/возврате оценщика, а создание нового оценщика выполняется вне блокировки.
 */
public final class NodeEvaluatorCache {

    private static final Map<Integer, Queue<NodeEvaluator>> nodeEvaluators = new ConcurrentHashMap<>();
    // NodeEvaluator не переопределяет equals/hashCode, поэтому ключи сравниваются по идентичности.
    private static final Map<NodeEvaluator, NodeEvaluatorGenerator> nodeEvaluatorToGenerator = new ConcurrentHashMap<>();

    public static final ThreadLocal<BinaryHeap> HEAP_LOCAL = ThreadLocal.withInitial(BinaryHeap::new);
    public static final ThreadLocal<Node[]> NEIGHBORS_LOCAL = ThreadLocal.withInitial(() -> new Node[32]);

    private NodeEvaluatorCache() {
    }

    private static Queue<NodeEvaluator> pool(final int features) {
        return nodeEvaluators.computeIfAbsent(features, key -> new ConcurrentLinkedQueue<>());
    }

    public static NodeEvaluator takeNodeEvaluator(NodeEvaluatorGenerator generator, NodeEvaluator localNodeEvaluator) {
        final int nodeEvaluatorFeatures = NodeEvaluatorFeatures.fromNodeEvaluator(localNodeEvaluator);
        NodeEvaluator nodeEvaluator = pool(nodeEvaluatorFeatures).poll();

        if (nodeEvaluator == null) {
            nodeEvaluator = generator.generate(NodeEvaluatorFeatures.unpack(nodeEvaluatorFeatures));
        }

        nodeEvaluatorToGenerator.put(nodeEvaluator, generator);

        return nodeEvaluator;
    }

    public static void returnNodeEvaluator(final NodeEvaluator nodeEvaluator) {
        final NodeEvaluatorGenerator generator = nodeEvaluatorToGenerator.remove(nodeEvaluator);
        Validate.notNull(generator, "NodeEvaluator already returned");

        pool(NodeEvaluatorFeatures.fromNodeEvaluator(nodeEvaluator)).offer(nodeEvaluator);
    }

    public static void removeNodeEvaluator(final NodeEvaluator nodeEvaluator) {
        nodeEvaluatorToGenerator.remove(nodeEvaluator);
    }
}
