package me.s0nd3r.watermc.util;

import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WaterMathExactTest {
    @BeforeAll
    static void configureWithoutHardware() {
        System.setProperty("water.hardwareFma", "false");
        System.clearProperty("water.allow-unfused-explosion-math");
    }

    @Test
    void preservesFusedRoundingAtCancellationBoundariesByDefault() {
        double a = 0.5 + Math.scalb(1.0, -28);
        double b = 1.0 - Math.scalb(1.0, -27);
        assertFalse(WaterMath.HARDWARE_FMA);
        assertEquals(0.0, a * b - 0.5);
        assertEquals(-Math.scalb(1.0, -55), WaterMath.fma(a, b, -0.5));
        assertEquals(Double.MAX_VALUE, WaterMath.fma(Double.MAX_VALUE, 2.0, -Double.MAX_VALUE));
        assertEquals(2.0 * Double.MIN_VALUE, WaterMath.fma(Double.MIN_VALUE, 0.5, Double.MIN_VALUE));
    }

    @Test
    void matchesMathFmaForSpecialValuesAndFixedRandomBitPatterns() {
        double[] special = {0.0, -0.0, Double.MIN_VALUE, -Double.MIN_VALUE, Double.MIN_NORMAL,
            Double.MAX_VALUE, -Double.MAX_VALUE, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN};
        for (double a : special) for (double b : special) for (double c : special) compare(a, b, c);
        Random random = new Random(0x5741544552L);
        for (int i = 0; i < 1000; ++i) compare(Double.longBitsToDouble(random.nextLong()),
            Double.longBitsToDouble(random.nextLong()), Double.longBitsToDouble(random.nextLong()));
    }

    private static void compare(double a, double b, double c) {
        assertEquals(Double.doubleToLongBits(Math.fma(a, b, c)), Double.doubleToLongBits(WaterMath.fma(a, b, c)));
    }
}
