package me.s0nd3r.watermc.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;

/** Fixed enum universe; the factory must be pure because concurrent first misses can race. */
public final class OrdinalCache<K extends Enum<K>, V> {
    private final AtomicReferenceArray<V> values;
    public OrdinalCache(int size) { this.values = new AtomicReferenceArray<>(size); }
    public V getOrCompute(K key, Function<? super K, ? extends V> factory) {
        int index = key.ordinal();
        V value = this.values.getAcquire(index);
        if (value != null) return value;
        V candidate = Objects.requireNonNull(factory.apply(key));
        V witness = this.values.compareAndExchange(index, null, candidate);
        return witness == null ? candidate : witness;
    }
}
