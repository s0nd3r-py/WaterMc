package me.s0nd3r.watermc.util;

import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;

/** Численные операции взрывов с точной семантикой Math.fma по умолчанию. */
public final class WaterMath {
    /** Разрешена ли JVM аппаратная FMA-интринсика; это не обещание состояния JIT-прогрева. */
    public static final boolean HARDWARE_FMA = detectHardwareFma();
    private static final boolean ALLOW_UNFUSED_FALLBACK = Boolean.getBoolean("water.allow-unfused-explosion-math");

    private WaterMath() {}

    /**
     * Сохраняет единственное округление Math.fma, включая старые CPU.
     * Экспериментальный -Dwater.allow-unfused-explosion-math=true разрешает
     * два округления только без аппаратной FMA. Это может изменить результаты
     * на численных границах и не является режимом полной эквивалентности Leaf.
     */
    public static double fma(double a, double b, double c) {
        return !HARDWARE_FMA && ALLOW_UNFUSED_FALLBACK ? a * b + c : Math.fma(a, b, c);
    }

    private static boolean detectHardwareFma() {
        String forced = System.getProperty("water.hardwareFma");
        if (forced != null) return Boolean.parseBoolean(forced);
        try {
            HotSpotDiagnosticMXBean bean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
            return Boolean.parseBoolean(bean.getVMOption("UseFMA").getValue());
        } catch (Throwable t) {
            // На неизвестной JVM сохраняем Math.fma и его точную семантику.
            return true;
        }
    }
}
