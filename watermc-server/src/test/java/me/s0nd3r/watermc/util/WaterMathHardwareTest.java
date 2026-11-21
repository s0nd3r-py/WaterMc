package me.s0nd3r.watermc.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaterMathHardwareTest {
    @BeforeAll
    static void reportHardwareAvailable() {
        System.setProperty("water.hardwareFma", "true");
        System.setProperty("water.allow-unfused-explosion-math", "true");
    }

    @Test
    void retainsFusedRoundingWhenHardwareIsAvailableEvenWithOptIn() {
        double a = 0.5 + Math.scalb(1.0, -28);
        double b = 1.0 - Math.scalb(1.0, -27);
        assertTrue(WaterMath.HARDWARE_FMA);
        assertEquals(Math.fma(a, b, -0.5), WaterMath.fma(a, b, -0.5));
    }
}
