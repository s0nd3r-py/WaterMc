package me.s0nd3r.watermc.ai;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BrainActivationTest {
    @Test
    void unseenTimeDoublesTheIntervalAndRespectsTheCap() {
        BrainActivation.State state = new BrainActivation.State();
        state.observe(false, 0, 5);
        for (int tick = 1; tick <= 140; ++tick) {
            int expected = tick < 20 ? 1 : tick < 40 ? 2 : tick < 60 ? 4 : tick < 80 ? 8 : tick < 100 ? 16 : 20;
            assertEquals(expected, state.tick(20, 20));
        }
        state.observe(true, 141, 5);
        assertEquals(1, state.tick(20, 20));
        state.observe(false, 142, 5);
        assertEquals(1, state.tick(20, 20));
    }

    @Test
    void visibilityPollingAndResetHandleTickCounterWrap() {
        BrainActivation.State state = new BrainActivation.State();
        assertTrue(state.needsVisibilityCheck(0));
        state.observe(false, Integer.MAX_VALUE - 2, 5);
        assertFalse(state.needsVisibilityCheck(Integer.MAX_VALUE));
        assertFalse(state.needsVisibilityCheck(Integer.MIN_VALUE + 1));
        assertTrue(state.needsVisibilityCheck(Integer.MIN_VALUE + 2));
        state.reset();
        assertTrue(state.needsVisibilityCheck(100));
        assertEquals(1, state.tick(1, 100));
    }

    @Test
    void invalidAndExtremeConfigurationCannotCreateZeroOrNegativeIntervals() {
        assertEquals(1, BrainActivation.interval(-10, 0, 0));
        assertEquals(20, BrainActivation.interval(Integer.MAX_VALUE, 1, 20));
        assertEquals(Integer.MAX_VALUE, BrainActivation.interval(Integer.MAX_VALUE, 1, Integer.MAX_VALUE));
        for (int interval : new int[]{1, 2, 4, 8, 16, 20}) {
            int calls = 0;
            for (int tick = 0; tick < 320; ++tick) if (BrainActivation.shouldTick(tick, 7, interval)) ++calls;
            assertEquals(320 / interval, calls);
        }
    }
}
