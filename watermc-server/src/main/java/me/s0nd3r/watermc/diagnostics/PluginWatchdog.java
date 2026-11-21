package me.s0nd3r.watermc.diagnostics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Счётчик времени слушателей событий и синхронных задач плагинов в тиковых потоках.
 *
 * <p>Замер — два {@link System#nanoTime()} на вызов слушателя; при выключенном watchdog — одна проверка
 * {@code volatile boolean}. В конце тика плагины, суммарно потратившие больше порога, выводятся в консоль
 * с указанием самого тяжёлого класса слушателя/задачи. Предупреждения по одному плагину не чаще
 * {@link #warnCooldownNanos}.</p>
 */
public final class PluginWatchdog {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");

    public static volatile boolean enabled = true;
    public static volatile long thresholdNanos = 15_000_000L;
    public static volatile long warnCooldownNanos = 30_000_000_000L;
    /** No warnings right after startup: classes are still loading and the JIT is cold, first calls are slow. */
    public static volatile long startupGraceNanos = 60_000_000_000L;
    private static long firstTickNanos;

    private static final Map<String, Accumulator> CURRENT = new ConcurrentHashMap<>();
    private static final Map<String, Long> LAST_WARN = new ConcurrentHashMap<>();

    private PluginWatchdog() {
    }

    /** Отметка начала; {@code 0}, если watchdog выключен. */
    public static long start() {
        return enabled ? System.nanoTime() : 0L;
    }

    public static void record(final String plugin, final String source, final long start) {
        if (start == 0L) {
            return;
        }
        recordNanos(label(plugin, source), source, System.nanoTime() - start);
    }

    /** Built-in WaterMc features run under the internal "Minecraft" plugin; name them so the report is useful. */
    static String label(final String plugin, final String source) {
        if ("Minecraft".equals(plugin) && source.startsWith("me.s0nd3r.watermc.")) {
            return source.startsWith("me.s0nd3r.watermc.script.") ? "WaterMc scripts" : "WaterMc";
        }
        return plugin;
    }

    static void recordNanos(final String plugin, final String source, final long nanos) {
        CURRENT.computeIfAbsent(plugin, key -> new Accumulator()).add(source, nanos);
    }

    /** Сводка за тик и сброс счётчиков. Возвращает плагины выше порога. */
    public static List<Report> drain(final long threshold) {
        if (CURRENT.isEmpty()) {
            return List.of();
        }
        final List<Report> reports = new ArrayList<>();
        for (final Map.Entry<String, Accumulator> entry : CURRENT.entrySet()) {
            final Accumulator acc = entry.getValue();
            final long total = acc.total.sum();
            if (total >= threshold) {
                reports.add(new Report(entry.getKey(), total, acc.hottestSource, acc.hottestNanos, acc.calls.sum()));
            }
        }
        CURRENT.clear();
        reports.sort(Comparator.comparingLong(Report::totalNanos).reversed());
        return reports;
    }

    /** Вызывается в конце тика сервера. */
    public static void endTick() {
        if (!enabled) {
            CURRENT.clear();
            return;
        }
        final long now = System.nanoTime();
        if (firstTickNanos == 0L) {
            firstTickNanos = now;
        }
        if (now - firstTickNanos < startupGraceNanos) {
            CURRENT.clear();
            return;
        }
        for (final Report report : drain(thresholdNanos)) {
            final Long last = LAST_WARN.get(report.plugin());
            if (last != null && now - last < warnCooldownNanos) {
                continue;
            }
            LAST_WARN.put(report.plugin(), now);
            LOGGER.warn("Plugin {} used {} ms of the main thread in one tick ({} calls); heaviest: {} ({} ms)",
                report.plugin(), ms(report.totalNanos()), report.calls(), report.hottestSource(), ms(report.hottestNanos()));
        }
    }

    private static String ms(final long nanos) {
        return String.format(java.util.Locale.ROOT, "%.2f", nanos / 1_000_000.0);
    }

    public record Report(String plugin, long totalNanos, String hottestSource, long hottestNanos, long calls) {
    }

    static final class Accumulator {
        final LongAdder total = new LongAdder();
        final LongAdder calls = new LongAdder();
        volatile String hottestSource = "?";
        volatile long hottestNanos;

        void add(final String source, final long nanos) {
            this.total.add(nanos);
            this.calls.increment();
            if (nanos > this.hottestNanos) {
                synchronized (this) {
                    if (nanos > this.hottestNanos) {
                        this.hottestNanos = nanos;
                        this.hottestSource = source;
                    }
                }
            }
        }
    }
}
