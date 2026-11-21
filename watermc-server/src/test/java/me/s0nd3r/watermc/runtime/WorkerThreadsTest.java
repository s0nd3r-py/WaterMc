package me.s0nd3r.watermc.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkerThreadsTest {

    @Test
    void defaultsLeaveRoomForTheMainThreadAndGrowMonotonically() {
        assertEquals(1, WorkerThreads.defaultFor(1));
        assertEquals(1, WorkerThreads.defaultFor(2));
        assertEquals(2, WorkerThreads.defaultFor(4));
        assertEquals(3, WorkerThreads.defaultFor(6));
        assertEquals(5, WorkerThreads.defaultFor(8));
        assertEquals(9, WorkerThreads.defaultFor(16));
        int previous = 0;
        for (int cores = 1; cores <= 256; cores++) {
            int workers = WorkerThreads.defaultFor(cores);
            assertTrue(workers >= previous, "non-decreasing at " + cores);
            assertTrue(workers >= 1 && (cores <= 3 || workers <= cores - 2), "keeps two cores free at " + cores);
            previous = workers;
        }
    }

    @Test
    void startupBoostUsesEveryCoreButNeverShrinksThePool() {
        assertEquals(4, WorkerThreads.startupBoost(4, 2));
        assertEquals(16, WorkerThreads.startupBoost(16, 9));
        assertEquals(6, WorkerThreads.startupBoost(4, 6), "an explicit larger pool is kept");
        assertEquals(1, WorkerThreads.startupBoost(0, 1));
    }

    @Test
    void waterMcPropertyWinsOverLegacyPaperProperty() {
        try {
            System.setProperty("Paper.WaterTestCount", "3");
            assertEquals(3, WorkerThreads.intProperty("WaterTestCount", 1));
            System.setProperty("WaterMc.WaterTestCount", "5");
            assertEquals(5, WorkerThreads.intProperty("WaterTestCount", 1));
            System.setProperty("WaterMc.WaterTestCount", "x");
            assertEquals(1, WorkerThreads.intProperty("WaterTestCount", 1));
        } finally {
            System.clearProperty("Paper.WaterTestCount");
            System.clearProperty("WaterMc.WaterTestCount");
        }
    }
}
