package me.s0nd3r.watermc.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Objects;
import java.util.function.Function;

/** Immutable published maps give identity-based, allocation-free hits; misses are rare and serialized. */
public final class ReadMostlyIdentityCache<K, V> {
    private volatile Reference2ObjectOpenHashMap<K, V> snapshot = new Reference2ObjectOpenHashMap<>();
    public V getOrCompute(K key, Function<? super K, ? extends V> factory) {
        V value = this.snapshot.get(key);
        if (value != null) return value;
        synchronized (this) {
            value = this.snapshot.get(key);
            if (value == null) {
                value = Objects.requireNonNull(factory.apply(key));
                Reference2ObjectOpenHashMap<K, V> updated = new Reference2ObjectOpenHashMap<>(this.snapshot);
                updated.put(key, value);
                this.snapshot = updated;
            }
        }
        return value;
    }
}
