package me.s0nd3r.watermc.ai;

import me.s0nd3r.watermc.engine.common.ai.non_poi_block_search.NonPOISearchDistances.MoveToBlockGoalDistances;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The chunk-aware search picks the minimum sort key; that equals vanilla's first hit only if the key rises strictly along vanilla's loop. */
class NonPoiSearchOrderTest {

    @Test
    void sortKeyFollowsVanillaLoopOrder() {
        for (final int range : new int[] {1, 2, 8, 16, 24, 32}) {
            int previous = Integer.MIN_VALUE;
            int visited = 0;
            // MoveToBlockGoal.findNearestBlock, one layer
            for (int ring = 0; ring < range; ring++) {
                for (int dX = 0; dX <= ring; dX = dX > 0 ? -dX : 1 - dX) {
                    for (int dZ = dX < ring && dX > -ring ? ring : 0; dZ <= ring; dZ = dZ > 0 ? -dZ : 1 - dZ) {
                        assertEquals(ring, MoveToBlockGoalDistances.getRing(dX, dZ));
                        final int key = MoveToBlockGoalDistances.getVanillaSortOrderInt(ring, dX, dZ);
                        assertTrue(key > previous, "range " + range + " at " + dX + "," + dZ);
                        previous = key;
                        visited++;
                    }
                }
            }
            assertEquals((2 * range - 1) * (2 * range - 1), visited, "vanilla visits every column of the square once");
        }
    }
}
