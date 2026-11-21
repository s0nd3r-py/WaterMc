package me.s0nd3r.watermc.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DynamicDistanceTest {

    @Test
    void lowersSimulationFirstAndRestoresViewFirst() {
        assertArrayEquals(new int[] {10, 9}, DynamicDistance.step(60, 40, 10, 10, 4, 4, 10, 10));
        assertArrayEquals(new int[] {9, 4}, DynamicDistance.step(60, 40, 10, 4, 4, 4, 10, 10));
        assertArrayEquals(new int[] {4, 4}, DynamicDistance.step(60, 40, 4, 4, 4, 4, 10, 10), "never below the minimum");
        assertArrayEquals(new int[] {5, 4}, DynamicDistance.step(20, 40, 4, 4, 4, 4, 10, 10));
        assertArrayEquals(new int[] {10, 5}, DynamicDistance.step(20, 40, 10, 4, 4, 4, 10, 10));
        assertArrayEquals(new int[] {10, 10}, DynamicDistance.step(20, 40, 10, 10, 4, 4, 10, 10), "never above server.properties");
        assertArrayEquals(new int[] {7, 6}, DynamicDistance.step(40, 40, 7, 6, 4, 4, 10, 10), "inside the dead band nothing moves");
    }
}
