package me.s0nd3r.watermc.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.serialization.Codec;
import io.papermc.paper.profile.PaperFilledProfileCache;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.util.EncoderCache;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CacheMigrationTest {
    @Test
    void expiryAndBoundedMaintenanceRespectTheConfiguredPolicy() {
        AtomicLong clock = new AtomicLong();
        Cache<Integer, String> cache = WaterCaches.newBuilder().maximumSize(16)
            .expireAfterAccess(Duration.ofSeconds(5)).ticker(clock::get).build();
        cache.put(1, "first");
        clock.set(TimeUnit.SECONDS.toNanos(4));
        assertEquals("first", cache.getIfPresent(1));
        clock.set(TimeUnit.SECONDS.toNanos(8));
        assertEquals("first", cache.getIfPresent(1));
        clock.set(TimeUnit.SECONDS.toNanos(14));
        assertNull(cache.getIfPresent(1));
        for (int i = 0; i < 1000; ++i) cache.put(i, "value");
        cache.cleanUp();
        assertTrue(cache.estimatedSize() <= 16);
    }

    @Test
    void weakKeysKeepIdentitySemantics() {
        Cache<String, Integer> cache = WaterCaches.newBuilder().weakKeys().build();
        String first = new String("same"), second = new String("same");
        cache.put(first, 1);
        cache.put(second, 2);
        assertEquals(1, cache.getIfPresent(first));
        assertEquals(2, cache.getIfPresent(second));
        java.lang.ref.Reference.reachabilityFence(first);
        java.lang.ref.Reference.reachabilityFence(second);
    }

    @Test
    void concurrentMissesLoadOnceAndWorldDependentLoadsStayOnTheCaller() throws Exception {
        AtomicInteger loads = new AtomicInteger();
        LoadingCache<String, String> cache = WaterCaches.newBuilder().maximumSize(16).build(key -> {
            loads.incrementAndGet();
            return key;
        });
        CompletableFuture<Void> start = new CompletableFuture<>();
        CompletableFuture<?>[] workers = new CompletableFuture<?>[8];
        Thread[] threads = new Thread[workers.length];
        for (int i = 0; i < workers.length; ++i) {
            CompletableFuture<String> result = new CompletableFuture<>();
            workers[i] = result;
            threads[i] = Thread.ofPlatform().start(() -> {
                try { start.join(); result.complete(cache.get("value")); }
                catch (Throwable failure) { result.completeExceptionally(failure); }
            });
        }
        start.complete(null);
        CompletableFuture.allOf(workers).get(10, TimeUnit.SECONDS);
        for (int i = 0; i < workers.length; ++i) { assertEquals("value", workers[i].join()); threads[i].join(); }
        assertEquals(1, loads.get());
        Thread owner = Thread.currentThread();
        LoadingCache<String, Thread> owned = WaterCaches.newBuilder().build(key -> Thread.currentThread());
        assertSame(owner, owned.get("world"));
    }

    @Test
    void encoderCacheReturnsIndependentNbtCopies() {
        Codec<Map<String, Integer>> codec = new EncoderCache(32).wrap(Codec.unboundedMap(Codec.STRING, Codec.INT));
        Map<String, Integer> input = Map.of("value", 7);
        CompoundTag first = (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, input).getOrThrow();
        first.putInt("value", 99);
        CompoundTag second = (CompoundTag) codec.encodeStart(NbtOps.INSTANCE, input).getOrThrow();
        assertNotSame(first, second);
        assertEquals(7, second.getIntOr("value", -1));
    }

    @Test
    void skinProfileCachesShareIdLoadsAndCacheNegativeResults() {
        MinecraftSessionService service = mock(MinecraftSessionService.class);
        UserNameToIdResolver names = mock(UserNameToIdResolver.class);
        PaperFilledProfileCache paper = mock(PaperFilledProfileCache.class);
        UUID id = UUID.fromString("57415445-5200-0000-0000-000000000001");
        GameProfile profile = new GameProfile(id, "WaterTest");
        ProfileResult result = mock(ProfileResult.class);
        when(result.profile()).thenReturn(profile);
        when(service.fetchProfile(id, true)).thenReturn(result);
        when(names.get("WaterTest")).thenReturn(Optional.of(new NameAndId(id, "WaterTest")));
        ProfileResolver.Cached cache = new ProfileResolver.Cached(service, names, paper);
        assertSame(profile, cache.fetchByName("WaterTest").orElseThrow());
        assertSame(profile, cache.fetchByName("WaterTest").orElseThrow());
        assertSame(profile, cache.fetchById(id).orElseThrow());
        verify(service, times(1)).fetchProfile(id, true);
        verify(paper, times(1)).add(profile);
        UUID missing = UUID.fromString("57415445-5200-0000-0000-000000000002");
        assertTrue(cache.fetchById(missing).isEmpty());
        assertTrue(cache.fetchById(missing).isEmpty());
        verify(service, times(1)).fetchProfile(missing, true);
        assertTrue(cache.fetchByName("invalid name!").isEmpty());
        verify(names, never()).get("invalid name!");
    }
}
