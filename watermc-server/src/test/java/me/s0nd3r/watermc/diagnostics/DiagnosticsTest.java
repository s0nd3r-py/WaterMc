package me.s0nd3r.watermc.diagnostics;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.event.HandlerList;
import org.junit.jupiter.api.Test;
import me.s0nd3r.watermc.event.WaterEvents;
import me.s0nd3r.watermc.world.MobcapPrecheck;

import static org.junit.jupiter.api.Assertions.*;

class DiagnosticsTest {

    @Test
    void mobcapPrecheckOnlyDropsWhenEveryPlayerIsFull() {
        assertFalse(MobcapPrecheck.anyBelowLimit(70, new int[]{70, 71, 120}), "all players at or above the cap");
        assertTrue(MobcapPrecheck.anyBelowLimit(70, new int[]{70, 69}), "one player can still spawn");
        assertFalse(MobcapPrecheck.anyBelowLimit(70, new int[0]), "no players -> nothing can spawn");
        assertFalse(MobcapPrecheck.anyBelowLimit(0, new int[]{0}), "limit 0 disables the category");
    }

    @Test
    void chunkInspectorRanksChunksByTotalTime() {
        final Map<String, Long2ObjectOpenHashMap<ChunkInspector.Sample>> samples = new java.util.HashMap<>();
        final Long2ObjectOpenHashMap<ChunkInspector.Sample> world = new Long2ObjectOpenHashMap<>();
        for (int i = 0; i < 15; i++) {
            final ChunkInspector.Sample s = new ChunkInspector.Sample();
            s.entityNanos = i * 1_000L;
            s.hopperNanos = i == 3 ? 1_000_000L : 0L;
            world.put(((long) (-i) << 32) | (i & 0xFFFFFFFFL), s);
        }
        samples.put("world", world);
        final List<ChunkInspector.Row> top = ChunkInspector.top(samples, 10);
        assertEquals(10, top.size());
        assertEquals(3, top.get(0).chunkX(), "hopper-heavy chunk first");
        assertEquals(-3, top.get(0).chunkZ(), "negative chunk coordinates survive packing");
        assertEquals(14, top.get(1).chunkX());
        for (int i = 1; i < top.size(); i++) {
            assertTrue(top.get(i - 1).totalNanos() >= top.get(i).totalNanos());
        }
    }

    @Test
    void chunkInspectorSamplesOnlyWhileActive() {
        assertEquals(0L, ChunkInspector.start(), "inactive inspector costs one volatile read and returns 0");
        ChunkInspector.recordEntity("world", 1, 1, 0L);
        ChunkInspector.begin(100);
        final long start = ChunkInspector.start();
        assertNotEquals(0L, start);
        ChunkInspector.recordEntity("world", -2, 5, start);
        ChunkInspector.recordBlockEntity("world", -2, 5, true, ChunkInspector.start());
        ChunkInspector.recordBlockEntity("world", 7, 7, false, ChunkInspector.start());
        final ChunkInspector.Result result = ChunkInspector.finish(120, 10);
        assertFalse(ChunkInspector.active);
        assertEquals(20, result.ticks());
        assertEquals(2, result.rows().size());
        final ChunkInspector.Row heavy = result.rows().stream().filter(r -> r.chunkX() == -2).findFirst().orElseThrow();
        assertEquals(5, heavy.chunkZ());
        assertEquals(1, heavy.sample().entityTicks);
        assertEquals(1, heavy.sample().hopperTicks);
    }

    @Test
    void pluginWatchdogReportsPluginsAboveThreshold() {
        PluginWatchdog.drain(0);
        PluginWatchdog.recordNanos("Heavy", "com.example.Heavy$Listener", 9_000_000L);
        PluginWatchdog.recordNanos("Heavy", "com.example.Heavy$Task", 8_000_000L);
        PluginWatchdog.recordNanos("Light", "com.example.Light", 1_000_000L);
        final List<PluginWatchdog.Report> reports = PluginWatchdog.drain(15_000_000L);
        assertEquals(1, reports.size());
        final PluginWatchdog.Report heavy = reports.get(0);
        assertEquals("Heavy", heavy.plugin());
        assertEquals(17_000_000L, heavy.totalNanos());
        assertEquals("com.example.Heavy$Listener", heavy.hottestSource());
        assertEquals(2, heavy.calls());
        assertTrue(PluginWatchdog.drain(0).isEmpty(), "counters reset every tick");

        final boolean previous = PluginWatchdog.enabled;
        PluginWatchdog.enabled = false;
        try {
            assertEquals(0L, PluginWatchdog.start());
            PluginWatchdog.record("X", "Y", 0L);
            assertTrue(PluginWatchdog.drain(0).isEmpty());
        } finally {
            PluginWatchdog.enabled = previous;
        }
    }

    @Test
    void heapGuardRunsRelieversWithCooldown() {
        HeapGuard.resetForTests();
        final AtomicInteger cleared = new AtomicInteger();
        HeapGuard.registerReliever(cleared::incrementAndGet);
        HeapGuard.registerReliever(() -> {
            throw new IllegalStateException("one broken reliever must not stop the others");
        });
        HeapGuard.registerReliever(cleared::incrementAndGet);
        assertEquals(2, HeapGuard.relieve("test"));
        assertEquals(2, cleared.get());
        assertEquals(0, HeapGuard.relieve("test"), "cooldown prevents thrashing");
        HeapGuard.resetForTests();
    }

    @Test
    void drainParsesTargetsAndTransfersEveryPlayer() {
        assertEquals(new WaterDrain.Target("lobby.example.net", 25566), WaterDrain.Target.parse("lobby.example.net:25566"));
        assertEquals(new WaterDrain.Target("10.0.0.2", 25565), WaterDrain.Target.parse("10.0.0.2"));
        assertEquals(new WaterDrain.Target("::1", 25570), WaterDrain.Target.parse("[::1]:25570"));
        assertThrows(IllegalArgumentException.class, () -> WaterDrain.Target.parse("host:0"));
        assertThrows(IllegalArgumentException.class, () -> WaterDrain.Target.parse(""));
        assertThrows(NumberFormatException.class, () -> WaterDrain.Target.parse("host:abc"));

        final List<String> transferred = new ArrayList<>();
        final List<String> kicked = new ArrayList<>();
        final WaterDrain.Target target = WaterDrain.Target.parse("backup:25566");
        assertEquals(3, WaterDrain.moveAll(List.of("a", "b", "c"), target, (p, t) -> transferred.add(p + "->" + t.host() + ":" + t.port()), kicked::add));
        assertEquals(List.of("a->backup:25566", "b->backup:25566", "c->backup:25566"), transferred);
        assertTrue(kicked.isEmpty());
        assertEquals(0, WaterDrain.moveAll(List.of("d"), null, (p, t) -> transferred.add(p), kicked::add));
        assertEquals(List.of("d"), kicked);
        assertFalse(WaterDrain.isDraining());
    }

    @Test
    void metricsSnapshotFormats() {
        final WaterMetrics.Snapshot snapshot = new WaterMetrics.Snapshot(20, 19.5, 19.9, 12.25, 1024, 4096, 300, 1500, 7, 1200);
        final String prometheus = snapshot.prometheus();
        assertTrue(prometheus.contains("watermc_tps{window=\"1m\"} 20.000"));
        assertTrue(prometheus.contains("watermc_mspt 12.250"));
        assertTrue(prometheus.contains("watermc_loaded_chunks 300"));
        final String json = snapshot.json();
        assertTrue(json.startsWith("{\"tps\":{\"1m\":20.000"));
        assertTrue(json.contains("\"entities\":1500"));
        assertTrue(json.contains("\"players\":7"));
        assertFalse(WaterMetrics.enabled, "metrics are disabled by default");
    }

    @Test
    void eventListenerPrecheck() {
        assertFalse(WaterEvents.hasListeners(new HandlerList()));
        assertFalse(WaterEvents.hasPlayerMoveListeners());
    }
}
