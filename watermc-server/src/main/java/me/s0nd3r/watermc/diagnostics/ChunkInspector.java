package me.s0nd3r.watermc.diagnostics;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Встроенный профилировщик тяжёлых чанков ({@code /watermc inspect}).
 *
 * <p>Пока сэмплирование выключено, в тиковых циклах стоит одна проверка {@code volatile boolean}.
 * Во время окна сэмплирования время тика каждой сущности и block entity суммируется по чанку.
 * Каждый мир тикается одним потоком, поэтому карта мира пишется без блокировок; итог читается
 * в основном потоке после выключения сэмплирования, между тиками.</p>
 */
public final class ChunkInspector {

    public static volatile boolean active;

    private static final Map<String, Long2ObjectOpenHashMap<Sample>> SAMPLES = new ConcurrentHashMap<>();
    private static volatile long startTick;

    private ChunkInspector() {
    }

    public static long start() {
        return active ? System.nanoTime() : 0L;
    }

    public static void begin(final long currentTick) {
        SAMPLES.clear();
        startTick = currentTick;
        active = true;
    }

    /** Останавливает сэмплирование и возвращает топ чанков. */
    public static Result finish(final long currentTick, final int limit) {
        active = false;
        final long ticks = Math.max(1L, currentTick - startTick);
        final List<Row> rows = top(SAMPLES, limit);
        SAMPLES.clear();
        return new Result(ticks, rows);
    }

    public static void recordEntity(final String world, final int chunkX, final int chunkZ, final long start) {
        if (start == 0L) {
            return;
        }
        final Sample sample = sample(world, chunkX, chunkZ);
        sample.entityNanos += System.nanoTime() - start;
        sample.entityTicks++;
    }

    public static void recordBlockEntity(final String world, final int chunkX, final int chunkZ, final boolean hopper, final long start) {
        if (start == 0L) {
            return;
        }
        final long nanos = System.nanoTime() - start;
        final Sample sample = sample(world, chunkX, chunkZ);
        if (hopper) {
            sample.hopperNanos += nanos;
            sample.hopperTicks++;
        } else {
            sample.blockEntityNanos += nanos;
            sample.blockEntityTicks++;
        }
    }

    private static Sample sample(final String world, final int chunkX, final int chunkZ) {
        return SAMPLES.computeIfAbsent(world, w -> new Long2ObjectOpenHashMap<>())
            .computeIfAbsent(((long) chunkZ << 32) | (chunkX & 0xFFFFFFFFL), k -> new Sample());
    }

    static List<Row> top(final Map<String, Long2ObjectOpenHashMap<Sample>> samples, final int limit) {
        final List<Row> rows = new ArrayList<>();
        for (final Map.Entry<String, Long2ObjectOpenHashMap<Sample>> world : samples.entrySet()) {
            for (final Long2ObjectMap.Entry<Sample> entry : world.getValue().long2ObjectEntrySet()) {
                final long key = entry.getLongKey();
                rows.add(new Row(world.getKey(), (int) key, (int) (key >> 32), entry.getValue()));
            }
        }
        rows.sort(Comparator.comparingLong(Row::totalNanos).reversed());
        return rows.size() > limit ? List.copyOf(rows.subList(0, limit)) : rows;
    }

    public static final class Sample {
        public long entityNanos;
        public long blockEntityNanos;
        public long hopperNanos;
        public int entityTicks;
        public int blockEntityTicks;
        public int hopperTicks;
    }

    public record Row(String world, int chunkX, int chunkZ, Sample sample) {
        public long totalNanos() {
            return this.sample.entityNanos + this.sample.blockEntityNanos + this.sample.hopperNanos;
        }
    }

    public record Result(long ticks, List<Row> rows) {
        public double msPerTick(final long nanos) {
            return nanos / (double) this.ticks / 1_000_000.0;
        }
    }
}
