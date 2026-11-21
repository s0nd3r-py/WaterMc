package me.s0nd3r.watermc.diagnostics;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Лёгкий HTTP-эндпоинт метрик (выключен по умолчанию).
 *
 * <p>Снимок собирается в основном потоке раз в 20 тиков и публикуется одной volatile-ссылкой;
 * HTTP-поток только читает готовый снимок и никогда не обращается к миру.
 * {@code /metrics} — формат Prometheus, {@code /metrics.json} — JSON.</p>
 */
public final class WaterMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");

    public static volatile boolean enabled;
    private static volatile Snapshot snapshot = Snapshot.EMPTY;
    private static HttpServer http;

    private WaterMetrics() {
    }

    public record Snapshot(double tps1m, double tps5m, double tps15m, double msptAverage, long heapUsed, long heapMax,
                           int loadedChunks, int entities, int players, long tick) {
        static final Snapshot EMPTY = new Snapshot(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

        public String prometheus() {
            return "# TYPE watermc_tps gauge\n"
                + "watermc_tps{window=\"1m\"} " + num(this.tps1m) + "\n"
                + "watermc_tps{window=\"5m\"} " + num(this.tps5m) + "\n"
                + "watermc_tps{window=\"15m\"} " + num(this.tps15m) + "\n"
                + "# TYPE watermc_mspt gauge\nwatermc_mspt " + num(this.msptAverage) + "\n"
                + "# TYPE watermc_heap_used_bytes gauge\nwatermc_heap_used_bytes " + this.heapUsed + "\n"
                + "# TYPE watermc_heap_max_bytes gauge\nwatermc_heap_max_bytes " + this.heapMax + "\n"
                + "# TYPE watermc_loaded_chunks gauge\nwatermc_loaded_chunks " + this.loadedChunks + "\n"
                + "# TYPE watermc_entities gauge\nwatermc_entities " + this.entities + "\n"
                + "# TYPE watermc_players gauge\nwatermc_players " + this.players + "\n"
                + "# TYPE watermc_tick counter\nwatermc_tick " + this.tick + "\n";
        }

        public String json() {
            return "{\"tps\":{\"1m\":" + num(this.tps1m) + ",\"5m\":" + num(this.tps5m) + ",\"15m\":" + num(this.tps15m) + "}"
                + ",\"mspt\":" + num(this.msptAverage)
                + ",\"heap\":{\"used\":" + this.heapUsed + ",\"max\":" + this.heapMax + "}"
                + ",\"loadedChunks\":" + this.loadedChunks + ",\"entities\":" + this.entities
                + ",\"players\":" + this.players + ",\"tick\":" + this.tick + "}";
        }

        private static String num(final double value) {
            return String.format(Locale.ROOT, "%.3f", value);
        }
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    /** Вызывается в конце каждого тика; собирает снимок раз в 20 тиков, только если метрики включены. */
    public static void onTickEnd(final MinecraftServer server) {
        if (!enabled || server.getTickCount() % 20 != 0) {
            return;
        }
        int chunks = 0;
        int entities = 0;
        for (final ServerLevel level : server.getAllLevels()) {
            chunks += level.getChunkSource().getFullChunksCount();
            entities += level.moonrise$getEntityLookup().getEntityCount();
        }
        final double[] tps = server.server.getTPS();
        final Runtime runtime = Runtime.getRuntime();
        snapshot = new Snapshot(tps[0], tps[1], tps[2], server.server.getAverageTickTime(),
            runtime.totalMemory() - runtime.freeMemory(), runtime.maxMemory(), chunks, entities, server.getPlayerCount(), server.getTickCount());
    }

    public static synchronized void startHttp(final String host, final int port) {
        if (http != null) {
            return;
        }
        try {
            final HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);
            server.createContext("/metrics", exchange -> respond(exchange, "text/plain; version=0.0.4", snapshot.prometheus()));
            server.createContext("/metrics.json", exchange -> respond(exchange, "application/json", snapshot.json()));
            server.setExecutor(Executors.newSingleThreadExecutor(r -> {
                final Thread thread = new Thread(r, "WaterMc Metrics HTTP");
                thread.setDaemon(true);
                return thread;
            }));
            server.start();
            http = server;
            enabled = true;
            LOGGER.info("Metrics available at http://{}:{}/metrics", host, port);
        } catch (final IOException ex) {
            LOGGER.error("Could not start the metrics HTTP endpoint on {}:{}", host, port, ex);
        }
    }

    public static synchronized void stopHttp() {
        if (http != null) {
            http.stop(0);
            http = null;
        }
        enabled = false;
    }

    private static void respond(final HttpExchange exchange, final String type, final String body) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        final byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
