package me.s0nd3r.watermc.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaterMathExperimentalTest {
    @BeforeAll
    static void optIn() {
        System.setProperty("water.hardwareFma", "false");
        System.setProperty("water.allow-unfused-explosion-math", "true");
    }

    @Test
    void permitsDifferentRoundingOnlyWithExplicitExperimentalOptIn() {
        double a = 0.5 + Math.scalb(1.0, -28);
        double b = 1.0 - Math.scalb(1.0, -27);
        assertFalse(WaterMath.HARDWARE_FMA);
        assertEquals(a * b - 0.5, WaterMath.fma(a, b, -0.5));
        assertNotEquals(Math.fma(a, b, -0.5), WaterMath.fma(a, b, -0.5));
    }
}
