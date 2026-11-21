package io.papermc.paper.threadedregions.scheduler;

import ca.spottedleaf.concurrentutil.util.ConcurrentUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.Plugin;
import java.lang.invoke.VarHandle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jctools.queues.MpscUnboundedArrayQueue;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

public class FoliaGlobalRegionScheduler implements GlobalRegionScheduler {

    private volatile long tickCount = 0L;
    // Producers only publish tasks; only the ticking owner touches deadline buckets.
    private final MpscUnboundedArrayQueue<GlobalScheduledTask> submissions = new MpscUnboundedArrayQueue<>(1024);
    // Cancellation metadata also covers tasks which are queued or currently executing.
    private final Set<GlobalScheduledTask> liveTasks = ConcurrentHashMap.newKeySet();
    private final Long2ObjectOpenHashMap<List<GlobalScheduledTask>> tasksByDeadline = new Long2ObjectOpenHashMap<>();

    /** Must be called by the single ticking owner. */
    public void tick() {
        final long now = ++this.tickCount;
        // Snapshot the producer boundary so continuously arriving work cannot extend this drain.
        final long boundary = this.submissions.currentProducerIndex();
        while (this.submissions.currentConsumerIndex() < boundary) {
            final GlobalScheduledTask task = this.submissions.relaxedPoll();
            // Do not spin on a producer which reserved a slot but has not published it yet.
            if (task == null) break;
            if (task.getStateVolatile() != GlobalScheduledTask.STATE_IDLE) continue;
            this.tasksByDeadline.computeIfAbsent(Math.max(now, task.deadline), key -> new ObjectArrayList<>()).add(task);
        }
        final List<GlobalScheduledTask> run = this.tasksByDeadline.remove(now);
        if (run == null) return;
        for (int i = 0, len = run.size(); i < len; ++i) run.get(i).run();
    }

    @Override
    public void execute(final Plugin plugin, final Runnable run) {
        Objects.requireNonNull(plugin, "Plugin may not be null");
        Objects.requireNonNull(run, "Runnable may not be null");

        this.run(plugin, (final ScheduledTask task) -> {
            run.run();
        });
    }

    @Override
    public ScheduledTask run(final Plugin plugin, final Consumer<ScheduledTask> task) {
        return this.runDelayed(plugin, task, 1);
    }

    @Override
    public ScheduledTask runDelayed(final Plugin plugin, final Consumer<ScheduledTask> task, final long delayTicks) {
        Objects.requireNonNull(plugin, "Plugin may not be null");
        Objects.requireNonNull(task, "Task may not be null");
        if (delayTicks <= 0) {
            throw new IllegalArgumentException("Delay ticks may not be <= 0");
        }

        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register task while disabled");
        }

        final GlobalScheduledTask ret = new GlobalScheduledTask(plugin, -1, task);

        this.liveTasks.add(ret);
        this.scheduleInternal(ret, delayTicks);

        if (!plugin.isEnabled()) {
            // handle race condition where plugin is disabled asynchronously
            ret.cancel();
        }

        return ret;
    }

    @Override
    public ScheduledTask runAtFixedRate(final Plugin plugin, final Consumer<ScheduledTask> task, final long initialDelayTicks, final long periodTicks) {
        Objects.requireNonNull(plugin, "Plugin may not be null");
        Objects.requireNonNull(task, "Task may not be null");
        if (initialDelayTicks <= 0) {
            throw new IllegalArgumentException("Initial delay ticks may not be <= 0");
        }
        if (periodTicks <= 0) {
            throw new IllegalArgumentException("Period ticks may not be <= 0");
        }

        if (!plugin.isEnabled()) {
            throw new IllegalPluginAccessException("Plugin attempted to register task while disabled");
        }

        final GlobalScheduledTask ret = new GlobalScheduledTask(plugin, periodTicks, task);

        this.liveTasks.add(ret);
        this.scheduleInternal(ret, initialDelayTicks);

        if (!plugin.isEnabled()) {
            // handle race condition where plugin is disabled asynchronously
            ret.cancel();
        }

        return ret;
    }

    @Override
    public void cancelTasks(final Plugin plugin) {
        Objects.requireNonNull(plugin, "Plugin may not be null");

        for (final GlobalScheduledTask task : this.liveTasks) {
            if (task.plugin == plugin) task.cancel();
        }
    }

    private void scheduleInternal(final GlobalScheduledTask task, final long delay) {
        final long now = this.tickCount;
        // Preserve far-future delays without turning addition overflow into an immediately due task.
        task.deadline = delay > Long.MAX_VALUE - now ? Long.MAX_VALUE : now + delay;
        this.submissions.offer(task);
    }

    private final class GlobalScheduledTask implements ScheduledTask, Runnable {

        private static final int STATE_IDLE                = 0;
        private static final int STATE_EXECUTING           = 1;
        private static final int STATE_EXECUTING_CANCELLED = 2;
        private static final int STATE_FINISHED            = 3;
        private static final int STATE_CANCELLED           = 4;

        private final Plugin plugin;
        private final long repeatDelay; // in ticks
        private Consumer<ScheduledTask> run;
        private volatile int state;
        private long deadline;

        private static final VarHandle STATE_HANDLE = ConcurrentUtil.getVarHandle(GlobalScheduledTask.class, "state", int.class);

        private GlobalScheduledTask(final Plugin plugin, final long repeatDelay, final Consumer<ScheduledTask> run) {
            this.plugin = plugin;
            this.repeatDelay = repeatDelay;
            this.run = run;
        }

        private final int getStateVolatile() {
            return (int)STATE_HANDLE.getAcquire(this);
        }

        private final int compareAndExchangeStateVolatile(final int expect, final int update) {
            return (int)STATE_HANDLE.compareAndExchange(this, expect, update);
        }

        private final void setStateVolatile(final int value) {
            STATE_HANDLE.setVolatile(this, value);
        }

        @Override
        public void run() {
            final boolean repeating = this.isRepeatingTask();
            if (STATE_IDLE != this.compareAndExchangeStateVolatile(STATE_IDLE, STATE_EXECUTING)) {
                // cancelled
                return;
            }

            try {
                this.run.accept(this);
            } catch (final Throwable throwable) {
                this.plugin.getLogger().log(Level.WARNING, "Global task for " + this.plugin.getDescription().getFullName() + " generated an exception", throwable);
            } finally {
                boolean reschedule = false;
                if (!repeating) {
                    this.setStateVolatile(STATE_FINISHED);
                } else if (STATE_EXECUTING == this.compareAndExchangeStateVolatile(STATE_EXECUTING, STATE_IDLE)) {
                    reschedule = true;
                } // else: cancelled repeating task

                if (!reschedule) {
                    if (repeating) this.setStateVolatile(STATE_CANCELLED);
                    this.run = null;
                    FoliaGlobalRegionScheduler.this.liveTasks.remove(this);
                } else {
                    FoliaGlobalRegionScheduler.this.scheduleInternal(this, this.repeatDelay);
                }
            }
        }

        @Override
        public Plugin getOwningPlugin() {
            return this.plugin;
        }

        @Override
        public boolean isRepeatingTask() {
            return this.repeatDelay > 0;
        }

        @Override
        public CancelledState cancel() {
            for (int curr = this.getStateVolatile();;) {
                switch (curr) {
                    case STATE_IDLE: {
                        if (STATE_IDLE == (curr = this.compareAndExchangeStateVolatile(STATE_IDLE, STATE_CANCELLED))) {
                            this.state = STATE_CANCELLED;
                            this.run = null;
                            FoliaGlobalRegionScheduler.this.liveTasks.remove(this);
                            return CancelledState.CANCELLED_BY_CALLER;
                        }
                        // try again
                        continue;
                    }
                    case STATE_EXECUTING: {
                        if (!this.isRepeatingTask()) {
                            return CancelledState.RUNNING;
                        }
                        if (STATE_EXECUTING == (curr = this.compareAndExchangeStateVolatile(STATE_EXECUTING, STATE_EXECUTING_CANCELLED))) {
                            return CancelledState.NEXT_RUNS_CANCELLED;
                        }
                        // try again
                        continue;
                    }
                    case STATE_EXECUTING_CANCELLED: {
                        return CancelledState.NEXT_RUNS_CANCELLED_ALREADY;
                    }
                    case STATE_FINISHED: {
                        return CancelledState.ALREADY_EXECUTED;
                    }
                    case STATE_CANCELLED: {
                        return CancelledState.CANCELLED_ALREADY;
                    }
                    default: {
                        throw new IllegalStateException("Unknown state: " + curr);
                    }
                }
            }
        }

        @Override
        public ExecutionState getExecutionState() {
            final int state = this.getStateVolatile();
            switch (state) {
                case STATE_IDLE:
                    return ExecutionState.IDLE;
                case STATE_EXECUTING:
                    return ExecutionState.RUNNING;
                case STATE_EXECUTING_CANCELLED:
                    return ExecutionState.CANCELLED_RUNNING;
                case STATE_FINISHED:
                    return ExecutionState.FINISHED;
                case STATE_CANCELLED:
                    return ExecutionState.CANCELLED;
                default: {
                    throw new IllegalStateException("Unknown state: " + state);
                }
            }
        }
    }
}
