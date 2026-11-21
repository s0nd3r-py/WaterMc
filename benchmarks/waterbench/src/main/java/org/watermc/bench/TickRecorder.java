package org.watermc.bench;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Records per-tick timings, main-thread allocations, heap usage, CPU load and GC events.
 * Only JDK management APIs are used so the numbers are comparable across Leaf and Water.
 */
final class TickRecorder {

    private static final com.sun.management.ThreadMXBean THREADS =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
    private static final com.sun.management.OperatingSystemMXBean OS =
        (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    private static final MemoryMXBean MEMORY = ManagementFactory.getMemoryMXBean();

    record GcEvent(long atMillis, String name, String action, String cause, long durationMillis, boolean pause) {}

    private final long mainThreadId;
    private double[] tickMillis = new double[4096];
    private long[] tickAlloc = new long[4096];
    private long[] tickHeap = new long[4096];
    private double[] tickCpu = new double[4096];
    private int size;

    private long allocAtTickStart = -1;
    private final List<GcEvent> gcEvents = new ArrayList<>();
    private final List<NotificationListener> listeners = new ArrayList<>();
    private volatile boolean recording;
    private long recordingGeneration;
    private long startedAtMillis;
    private double lastCpu = -1;
    private long lastCpuTimeNanos = -1;
    private long lastWallNanos;
    private static final int CORES = Runtime.getRuntime().availableProcessors();

    TickRecorder(Thread mainThread) {
        this.mainThreadId = mainThread.threadId();
        if (THREADS.isThreadAllocatedMemorySupported() && !THREADS.isThreadAllocatedMemoryEnabled()) {
            THREADS.setThreadAllocatedMemoryEnabled(true);
        }
    }

    void start() {
        this.size = 0;
        this.lastCpu = -1;
        this.lastCpuTimeNanos = -1;
        final long generation;
        synchronized (this.gcEvents) {
            this.gcEvents.clear();
            this.startedAtMillis = System.currentTimeMillis();
            this.recording = true;
            generation = ++this.recordingGeneration;
        }
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(bean instanceof NotificationEmitter emitter)) continue;
            NotificationListener listener = (notification, handback) -> onGcNotification(generation, notification, handback);
            emitter.addNotificationListener(listener, null, null);
            this.listeners.add(listener);
        }
    }

    void stop() {
        synchronized (this.gcEvents) {
            this.recording = false;
            ++this.recordingGeneration;
        }
        for (GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(bean instanceof NotificationEmitter emitter)) continue;
            for (NotificationListener listener : this.listeners) {
                try {
                    emitter.removeNotificationListener(listener);
                } catch (Exception ignored) {
                    // listener was registered on another bean
                }
            }
        }
        this.listeners.clear();
    }

    boolean isRecording() {
        return this.recording;
    }

    void onTickStart() {
        if (!this.recording) return;
        this.allocAtTickStart = THREADS.getThreadAllocatedBytes(this.mainThreadId);
    }

    void onTickEnd(double durationMillis, boolean counted) {
        if (!this.recording || !counted) return;
        long alloc = this.allocAtTickStart < 0 ? 0 : THREADS.getThreadAllocatedBytes(this.mainThreadId) - this.allocAtTickStart;
        if (this.size == this.tickMillis.length) {
            int newLen = this.size * 2;
            this.tickMillis = Arrays.copyOf(this.tickMillis, newLen);
            this.tickAlloc = Arrays.copyOf(this.tickAlloc, newLen);
            this.tickHeap = Arrays.copyOf(this.tickHeap, newLen);
            this.tickCpu = Arrays.copyOf(this.tickCpu, newLen);
        }
        // process CPU usage over the last second (0..1 of all cores); getProcessCpuLoad() is unreliable on Windows
        if (this.size % 20 == 0) {
            long cpu = OS.getProcessCpuTime();
            long wall = System.nanoTime();
            if (this.lastCpuTimeNanos >= 0 && wall > this.lastWallNanos) {
                this.lastCpu = (double) (cpu - this.lastCpuTimeNanos) / ((wall - this.lastWallNanos) * (double) CORES);
            }
            this.lastCpuTimeNanos = cpu;
            this.lastWallNanos = wall;
        }
        this.tickMillis[this.size] = durationMillis;
        this.tickAlloc[this.size] = alloc;
        this.tickHeap[this.size] = MEMORY.getHeapMemoryUsage().getUsed();
        this.tickCpu[this.size] = this.lastCpu;
        this.size++;
    }

    private void onGcNotification(long generation, Notification notification, Object handback) {
        if (!GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION.equals(notification.getType())) return;
        GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
        String name = info.getGcName();
        // G1 "Concurrent GC" and ZGC "Cycles" beans report concurrent work, not stop-the-world time
        boolean pause = !(name.contains("Concurrent") || name.contains("Cycles"));
        synchronized (this.gcEvents) {
            // Позднее уведомление прежнего сценария не должно попадать в новый.
            if (!this.recording || generation != this.recordingGeneration) return;
            this.gcEvents.add(new GcEvent(System.currentTimeMillis() - this.startedAtMillis, name, info.getGcAction(),
                info.getGcCause(), info.getGcInfo().getDuration(), pause));
        }
    }

    int size() {
        return this.size;
    }

    Summary summarize(String label) {
        double[] sorted = Arrays.copyOf(this.tickMillis, this.size);
        Arrays.sort(sorted);
        double sum = 0;
        for (double v : sorted) sum += v;
        long allocSum = 0;
        for (int i = 0; i < this.size; i++) allocSum += this.tickAlloc[i];
        long heapMax = 0;
        for (int i = 0; i < this.size; i++) heapMax = Math.max(heapMax, this.tickHeap[i]);
        double cpuSum = 0;
        int cpuCount = 0;
        for (int i = 0; i < this.size; i += 20) {
            if (this.tickCpu[i] >= 0) {
                cpuSum += this.tickCpu[i];
                cpuCount++;
            }
        }
        long pauseCount = 0, pauseTotal = 0, pauseMax = 0;
        synchronized (this.gcEvents) {
            for (GcEvent e : this.gcEvents) {
                if (!e.pause()) continue;
                pauseCount++;
                pauseTotal += e.durationMillis();
                pauseMax = Math.max(pauseMax, e.durationMillis());
            }
        }
        int over50 = 0;
        for (double v : sorted) if (v > 50.0) over50++;
        return new Summary(label, this.size,
            this.size == 0 ? 0 : sum / this.size,
            percentile(sorted, 0.50), percentile(sorted, 0.95), percentile(sorted, 0.99),
            this.size == 0 ? 0 : sorted[this.size - 1],
            over50,
            this.size == 0 ? 0 : allocSum / this.size,
            heapMax,
            cpuCount == 0 ? -1 : cpuSum / cpuCount,
            pauseCount, pauseTotal, pauseMax);
    }

    private static double percentile(double[] sorted, double p) {
        if (sorted.length == 0) return 0;
        int idx = (int) Math.ceil(p * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(sorted.length - 1, idx))];
    }

    void writeCsv(Path dir, String label) throws IOException {
        Files.createDirectories(dir);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(dir.resolve(label + "-ticks.csv")))) {
            out.println("tick,mspt,main_alloc_bytes,heap_used_bytes,process_cpu");
            for (int i = 0; i < this.size; i++) {
                out.printf(Locale.ROOT, "%d,%.4f,%d,%d,%.4f%n", i, this.tickMillis[i], this.tickAlloc[i], this.tickHeap[i], this.tickCpu[i]);
            }
        }
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(dir.resolve(label + "-gc.csv")))) {
            out.println("at_ms,gc,action,cause,duration_ms,pause");
            synchronized (this.gcEvents) {
                for (GcEvent e : this.gcEvents) {
                    out.printf(Locale.ROOT, "%d,%s,%s,%s,%d,%b%n", e.atMillis(), e.name(), e.action(), e.cause(), e.durationMillis(), e.pause());
                }
            }
        }
    }

    record Summary(String label, int ticks, double meanMspt, double p50, double p95, double p99, double max,
                   int ticksOver50ms, long avgMainAllocPerTick, long maxHeapUsed, double avgProcessCpu,
                   long gcPauses, long gcPauseTotalMs, long gcPauseMaxMs) {

        double tps() {
            // Ticks are capped to 20/s; a tick slower than 50 ms pushes the next one back
            double avgInterval = Math.max(50.0, this.meanMspt);
            return 1000.0 / avgInterval;
        }

        String toJson() {
            return String.format(Locale.ROOT,
                "{\"label\":\"%s\",\"ticks\":%d,\"mean_mspt\":%.3f,\"p50\":%.3f,\"p95\":%.3f,\"p99\":%.3f,\"max\":%.3f," +
                    "\"ticks_over_50ms\":%d,\"est_tps\":%.2f,\"avg_main_alloc_per_tick\":%d,\"max_heap_used\":%d," +
                    "\"avg_process_cpu\":%.4f,\"gc_pauses\":%d,\"gc_pause_total_ms\":%d,\"gc_pause_max_ms\":%d," +
                    "\"java\":\"%s\",\"vm\":\"%s\"}",
                this.label, this.ticks, this.meanMspt, this.p50, this.p95, this.p99, this.max, this.ticksOver50ms, tps(),
                this.avgMainAllocPerTick, this.maxHeapUsed, this.avgProcessCpu, this.gcPauses, this.gcPauseTotalMs,
                this.gcPauseMaxMs, System.getProperty("java.version"), System.getProperty("java.vm.name"));
        }

        String toHuman() {
            return String.format(Locale.ROOT,
                "[%s] ticks=%d mean=%.2fms p50=%.2f p95=%.2f p99=%.2f max=%.2f >50ms=%d tps~%.2f | alloc/tick=%.1fKiB heapMax=%dMiB cpu=%.0f%% | gcPauses=%d total=%dms max=%dms",
                this.label, this.ticks, this.meanMspt, this.p50, this.p95, this.p99, this.max, this.ticksOver50ms, tps(),
                this.avgMainAllocPerTick / 1024.0, this.maxHeapUsed >> 20, this.avgProcessCpu * 100, this.gcPauses,
                this.gcPauseTotalMs, this.gcPauseMaxMs);
        }
    }
}
