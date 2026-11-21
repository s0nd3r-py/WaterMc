package org.watermc.bench;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Reproducible server benchmark. Usage:
 * <pre>
 *   /wbench run idle:60,entities:1500:120,tnt:5:14,chunkgen:20
 *   -Dwaterbench.auto=&lt;same spec&gt; -Dwaterbench.label=leaf-g1 -Dwaterbench.players=4 -Dwaterbench.shutdown=true
 * </pre>
 */
public final class WaterBenchPlugin extends JavaPlugin implements Listener {

    private TickRecorder recorder;
    private final Deque<Scenario> queue = new ArrayDeque<>();
    private Scenario current;
    private boolean prepared;
    private boolean countThisTick;
    private boolean measurementStarted;
    private final boolean requireProbeAcks = Boolean.getBoolean("waterbench.probe-acks");
    private record AckWindow(String token, java.util.Set<java.util.UUID> awaiting, long startedAt) {}
    private volatile AckWindow awaitingPhase;
    private boolean phaseBoundaryPending;
    private int phaseSequence;
    private String label = "run";
    private boolean shutdownWhenDone;
    private int waitForPlayers;
    private int waitTicks;

    @Override
    public void onEnable() {
        this.recorder = new TickRecorder(Thread.currentThread());
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "watermc:bench_ack", (channel, player, data) -> {
            AckWindow window = this.awaitingPhase;
            if (window != null && data.length <= 256 && new String(data, java.nio.charset.StandardCharsets.UTF_8).equals(window.token())) {
                window.awaiting().remove(player.getUniqueId());
            }
        });
        String auto = System.getProperty("waterbench.auto");
        if (auto != null && !auto.isBlank()) {
            this.label = System.getProperty("waterbench.label", "auto");
            this.shutdownWhenDone = Boolean.getBoolean("waterbench.shutdown");
            this.waitForPlayers = Integer.getInteger("waterbench.players", 0);
            // let the server finish starting before the first scenario
            Bukkit.getScheduler().runTaskLater(this, () -> enqueue(auto), 20L * 10);
        }
    }

    private void enqueue(String spec) {
        for (String part : spec.split(",")) {
            String[] a = part.trim().split(":");
            Scenario s = switch (a[0].toLowerCase(Locale.ROOT)) {
                case "idle" -> new IdleScenario(arg(a, 1, 60));
                case "entities" -> new EntityScenario(this, arg(a, 1, 1500), arg(a, 2, 120));
                case "tnt" -> new TntScenario(this, arg(a, 1, 5), arg(a, 2, 14));
                case "chunkgen" -> new ChunkGenScenario(this, arg(a, 1, 20));
                case "tntburst" -> new TntBurstScenario(this, arg(a, 1, 3), arg(a, 2, 1000));
                case "soak" -> new EntityScenario(this, arg(a, 1, 40), arg(a, 2, 3600), true);
                case "crowd" -> EntityScenario.crowd(this, arg(a, 1, 600), arg(a, 2, 60), arg(a, 3, 6), a.length > 4 ? Integer.valueOf(Integer.parseInt(a[4])) : null);
                case "explosionpackets" -> new ExplosionPacketsScenario();
                case "compat" -> new CompatibilityScenario(this);
                case "particles" -> new ParticleScenario(arg(a, 1, 10), arg(a, 2, 600));
                default -> throw new IllegalArgumentException("unknown scenario " + a[0]);
            };
            this.queue.add(s);
        }
        getLogger().info("Queued " + this.queue.size() + " scenario(s) for label '" + this.label + "'");
    }

    private static int arg(String[] a, int i, int def) {
        return a.length > i ? Integer.parseInt(a[i]) : def;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTickStart(ServerTickStartEvent event) {
        try {
            advanceScenario();
        } catch (RuntimeException | AssertionError failure) {
            getLogger().log(java.util.logging.Level.SEVERE, "BENCHMARK_FAILED", failure);
            this.current = null;
            this.queue.clear();
            this.awaitingPhase = null;
            this.phaseBoundaryPending = false;
            this.recorder.stop();
            if (this.shutdownWhenDone) Bukkit.shutdown();
        }
    }

    private void advanceScenario() {
        this.countThisTick = false;
        if (this.phaseBoundaryPending) {
            AckWindow window = this.awaitingPhase;
            if (window != null && !window.awaiting().isEmpty()) {
                if (System.nanoTime() - window.startedAt() > java.util.concurrent.TimeUnit.MINUTES.toNanos(5)) {
                    throw new AssertionError("Клиенты не подтвердили полный приём: " + window.awaiting());
                }
                return;
            }
            this.phaseBoundaryPending = false;
            this.awaitingPhase = null;
            if (this.queue.isEmpty()) {
                getLogger().info("BENCHMARK_SCENARIOS_COMPLETE epoch_ms=" + System.currentTimeMillis());
                getLogger().info("All scenarios finished");
                if (this.shutdownWhenDone) Bukkit.getScheduler().runTaskLater(this, Bukkit::shutdown, 40L);
                return;
            }
        }
        if (this.current == null) {
            if (this.queue.isEmpty()) return;
            if (this.waitForPlayers > 0 && Bukkit.getOnlinePlayers().size() < this.waitForPlayers && this.waitTicks++ < 20 * 120) {
                return;
            }
            this.current = this.queue.poll();
            this.prepared = false;
        }
        if (!this.prepared) {
            getLogger().info("Preparing scenario " + this.current.name());
            ++this.phaseSequence;
            this.measurementStarted = false;
            this.current.prepare();
            this.prepared = true;
            this.recorder.start();
            sendPhaseMarker("START", this.current.name());
            return; // preparation tick is never counted
        }
        this.recorder.onTickStart();
        Scenario.TickResult result = this.current.tick();
        switch (result) {
            case COUNT -> {
                this.countThisTick = true;
                if (!this.measurementStarted) {
                    this.measurementStarted = true;
                    getLogger().info("BENCHMARK_MEASUREMENT_START scenario=" + this.current.name()
                        + " epoch_ms=" + System.currentTimeMillis());
                }
            }
            case SKIP -> this.countThisTick = false;
            case FINISHED -> finishCurrent();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTickEnd(ServerTickEndEvent event) {
        if (this.current != null && this.countThisTick) {
            this.recorder.onTickEnd(event.getTickDuration(), true);
        }
    }

    private void sendPhaseMarker(String boundary, String scenario) {
        String marker = "WATERBENCH_PHASE_" + boundary + ":" + this.label + ":" + scenario + ":" + this.phaseSequence;
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(marker));
    }

    private void finishCurrent() {
        Scenario done = this.current;
        this.current = null;
        this.recorder.stop();
        String name = this.label + "-" + done.name();
        TickRecorder.Summary summary = this.recorder.summarize(name);
        String extra = "";
        if (done instanceof ChunkGenScenario gen) {
            extra = String.format(Locale.ROOT, " chunks=%d chunks/s=%.1f", gen.generated(), gen.chunksPerSecond());
        }
        getLogger().info("RESULT " + summary.toHuman() + extra);
        Path dir = getDataFolder().toPath().resolve("results").resolve(this.label);
        try {
            this.recorder.writeCsv(dir, name);
            String json = summary.toJson();
            if (done instanceof ChunkGenScenario gen) {
                json = json.substring(0, json.length() - 1)
                    + String.format(Locale.ROOT, ",\"chunks\":%d,\"chunks_per_s\":%.2f}", gen.generated(), gen.chunksPerSecond());
            }
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("summary.jsonl"), json + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            getLogger().warning("Could not write results: " + e);
        }
        done.cleanup();
        AckWindow window = new AckWindow(this.label + ":" + done.name() + ":" + this.phaseSequence,
            java.util.concurrent.ConcurrentHashMap.newKeySet(), System.nanoTime());
        if (this.requireProbeAcks) {
            var players = Bukkit.getOnlinePlayers();
            if (players.size() != this.waitForPlayers) throw new AssertionError("Изменилось число клиентов перед подтверждением");
            players.forEach(player -> window.awaiting().add(player.getUniqueId()));
        }
        this.awaitingPhase = window;
        this.phaseBoundaryPending = true;
        sendPhaseMarker("END", done.name());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        if (args.length >= 2 && args[0].equalsIgnoreCase("run")) {
            if (args.length >= 3) this.label = args[2];
            enqueue(args[1]);
            sender.sendMessage("WaterBench: queued " + args[1]);
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("WaterBench: current=" + (this.current == null ? "none" : this.current.name())
                + " queued=" + this.queue.size() + " recordedTicks=" + this.recorder.size());
            return true;
        }
        return false;
    }

    private static final class IdleScenario implements Scenario {
        private final int ticks;
        private int tick;

        // first 30 s are skipped: right after startup the JIT is still compiling
        private static final int WARMUP = 20 * 30;

        IdleScenario(int seconds) {
            this.ticks = seconds * 20;
        }

        @Override public String name() { return "idle"; }
        @Override public void prepare() { this.tick = 0; }
        @Override public TickResult tick() {
            this.tick++;
            if (this.tick <= WARMUP) return TickResult.SKIP;
            return this.tick > WARMUP + this.ticks ? TickResult.FINISHED : TickResult.COUNT;
        }
        @Override public void cleanup() {}
    }
}
