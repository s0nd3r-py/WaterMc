package me.s0nd3r.watermc.bootstrap;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LazyDataFixerTest {
    @Test void modernAndNewerDataDoNotBuildSchemas() {
        AtomicInteger builds = new AtomicInteger();
        LazyDataFixer fixer = new LazyDataFixer(() -> { builds.incrementAndGet(); return delegate(new AtomicInteger()); });
        Dynamic<?> input = new Dynamic<>(JsonOps.INSTANCE, new com.google.gson.JsonObject());
        assertSame(input, fixer.update(() -> "chunk", input, 5000, 5000));
        assertSame(input, fixer.update(() -> "chunk", input, 5001, 5000));
        assertFalse(fixer.isInitialized()); assertEquals(0, builds.get());
    }
    @Test void concurrentOldDataInitializesOnceAndForwardsEveryConversion() throws Exception {
        AtomicInteger builds = new AtomicInteger(), updates = new AtomicInteger();
        LazyDataFixer fixer = new LazyDataFixer(() -> { builds.incrementAndGet(); return delegate(updates); });
        CompletableFuture<?>[] completed = new CompletableFuture<?>[8];
        Thread[] threads = new Thread[8];
        for (int i = 0; i < threads.length; ++i) {
            CompletableFuture<Void> done = new CompletableFuture<>(); completed[i] = done;
            threads[i] = Thread.ofPlatform().start(() -> {
                try {
                    Dynamic<?> input = new Dynamic<>(JsonOps.INSTANCE, new com.google.gson.JsonObject());
                    for (int n = 0; n < 100; ++n) assertSame(input, fixer.update(() -> "chunk", input, 100, 5000));
                    done.complete(null);
                } catch (Throwable failure) { done.completeExceptionally(failure); }
            });
        }
        CompletableFuture.allOf(completed).get(10, TimeUnit.SECONDS);
        for (Thread thread : threads) thread.join();
        assertEquals(1, builds.get()); assertEquals(800, updates.get()); assertTrue(fixer.isInitialized());
    }
    @Test void explicitSchemaLookupInitializesAndDelegatesTheVersion() {
        AtomicInteger version = new AtomicInteger();
        LazyDataFixer fixer = new LazyDataFixer(() -> new DataFixer() {
            @Override public <T> Dynamic<T> update(TypeReference type, Dynamic<T> input, int from, int to) { return input; }
            @Override public Schema getSchema(int requested) { version.set(requested); return null; }
        });
        assertNull(fixer.getSchema(1234)); assertEquals(1234, version.get()); assertTrue(fixer.isInitialized());
    }
    private static DataFixer delegate(AtomicInteger updates) {
        return new DataFixer() {
            @Override public <T> Dynamic<T> update(TypeReference type, Dynamic<T> input, int from, int to) {
                assertEquals(100, from); assertEquals(5000, to); updates.incrementAndGet(); return input;
            }
            @Override public Schema getSchema(int version) { return null; }
        };
    }
}
