package me.s0nd3r.watermc.util;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import java.util.concurrent.ForkJoinPool;

/** Shared maintenance policy; cache bounds, reference strength and expiry remain call-site decisions. */
public final class WaterCaches {
    private WaterCaches() {}

    public static Caffeine<Object, Object> newBuilder() {
        return Caffeine.newBuilder()
            .executor(ForkJoinPool.commonPool())
            .scheduler(Scheduler.systemScheduler());
    }
}
