package me.s0nd3r.watermc.core.world;

import it.unimi.dsi.fastutil.HashCommon;
import org.jspecify.annotations.Nullable;
import org.spigotmc.WatchdogThread;

import java.util.Arrays;
import java.util.concurrent.Future;

/// Optimized chunk map for main thread.
/// - Single-entry cache: Maintains a fast-access entry cache for the most recently accessed item
/// - Thread safety: Enforces single-threaded access with runtime checks
///
/// This map is designed to be accessed from a single thread at a time.
/// All mutating operations will throw [IllegalStateException]
/// if the current thread is not the owning thread.
///
/// @see it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap
public final class ChunkCache<V> {
    private static final long EMPTY_KEY = Long.MIN_VALUE;
    private static final float FACTOR = 0.5F;
    private static final int MIN_N = HashCommon.arraySize(1024, FACTOR);

    // WaterMc start - 2x2 neighbourhood cache
    /// Recently used chunks, one slot per parity of (x, z): the four chunks around a chunk corner never
    /// evict each other. Pathfinding and collisions near a border alternate between exactly those chunks,
    /// which kept missing the old single-entry cache.
    private static final boolean NEIGHBOURHOOD = Boolean.parseBoolean(System.getProperty("watermc.chunk-cache-neighbourhood", "true"));
    private final long[] recentKeys = {EMPTY_KEY, EMPTY_KEY, EMPTY_KEY, EMPTY_KEY};
    private final @Nullable Object[] recentValues = new Object[4];

    /// Slot for a chunk key: low bit of x, low bit of z.
    static int slot(final long k) {
        return NEIGHBOURHOOD ? ((int) k & 1) | (((int) (k >>> 32) & 1) << 1) : 0;
    }

    private void forget(final long k) {
        final int slot = slot(k);
        if (this.recentKeys[slot] == k) {
            this.recentKeys[slot] = EMPTY_KEY;
            this.recentValues[slot] = null;
        }
    }
    // WaterMc end - 2x2 neighbourhood cache
    private Thread thread;

    private transient long[] key;
    private transient @Nullable V[] value;
    private transient int mask;
    private transient boolean containsNullKey;
    private transient int n;
    private transient int maxFill;
    private int size;

    public ChunkCache(Thread thread) {
        n = MIN_N;
        mask = n - 1;
        maxFill = HashCommon.maxFill(n, FACTOR);
        key = new long[n + 1];
        //noinspection unchecked
        value = (V[]) new Object[n + 1];
        this.thread = thread;
    }

    /// Retrieves the value associated with the specified key.
    ///
    /// This method implements a single-entry cache optimization:
    ///
    /// If the requested key matches the most recently accessed key,
    /// the cached value is returned immediately without a hash map lookup.
    ///
    /// This method does not perform thread checks for performance reasons.
    ///
    /// # Safety
    ///
    /// The caller must ensure that the current thread is the owning thread.
    ///
    /// @param k The key whose associated value is to be returned
    /// @return The value associated with the key, or `null` if no mapping exists
    /// @implNote This method updates the single-entry cache on successful lookups
    /// @see #isSameThread()
    public @Nullable V get(long k) {
        // WaterMc start - 2x2 neighbourhood cache
        final int slot = slot(k);
        if (this.recentKeys[slot] == k) {
            //noinspection unchecked
            final V cached = (V) this.recentValues[slot];
            if (cached != null) {
                return cached;
            }
        }
        final V found = this.getMiss(k);
        if (found != null) {
            this.recentKeys[slot] = k;
            this.recentValues[slot] = found;
        }
        return found;
        // WaterMc end - 2x2 neighbourhood cache
    }

    public @Nullable V getMiss(long k) { // WaterMc - public for tests
        if (k == 0L) {
            if (this.containsNullKey) {
                return this.value[this.n];
            } else {
                return null;
            }
        } else {
            long curr;
            final long[] key = this.key;
            int pos;
            if ((curr = key[pos = (int) HashCommon.mix(k) & this.mask]) == 0) {
                return null;
            } else if (k == curr) {
                return this.value[pos];
            } else {
                while (true) {
                    if ((curr = key[pos = pos + 1 & this.mask]) == 0) {
                        return null;
                    }
                    if (k == curr) {
                        return this.value[pos];
                    }
                }
            }
        }
    }

    /// Removes the mapping for the specified key from this map.
    ///
    /// If the removed key matches the cached key, the single-entry cache is invalidated.
    ///
    /// @param k The key whose mapping is to be removed
    /// @return The previous value associated with the key, or `null` if no mapping existed
    /// @throws IllegalStateException If the current thread is not the owning thread
    public @Nullable V remove(long k) {
        // Safety: throws IllegalStateException for all non-owning threads
        ensureSameThread();
        this.forget(k); // WaterMc - 2x2 neighbourhood cache
        if (((k) == (0))) {
            if (containsNullKey) return removeNullEntry();
            return null;
        }
        long curr;
        final long[] key = this.key;
        int pos;
        if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return null;
        if (((k) == (curr))) return removeEntry(pos);
        while (true) {
            if (((curr = key[pos = (pos + 1) & mask]) == (0))) return null;
            if (((k) == (curr))) return removeEntry(pos);
        }
    }

    /// Associates the specified entry in this map.
    ///
    /// If the key matches the cached key, the single-entry cache is invalidated.
    ///
    /// @param k The key with which the specified value is to be associated
    /// @param chunk The value to be associated with the specified key
    /// @return The previous value associated with the key, or null if no mapping existed
    /// @throws IllegalStateException If the current thread is not the owning thread
    public @Nullable V put(long k, V chunk) {
        // Safety: throws IllegalStateException for all non-owning threads
        ensureSameThread();
        this.forget(k); // WaterMc - 2x2 neighbourhood cache
        final int pos = find(k);
        if (pos < 0) {
            insert(-pos - 1, k, chunk);
            return null;
        }
        final V oldValue = value[pos];
        value[pos] = chunk;
        return oldValue;
    }

    /// Removes all elements from this map.
    ///
    /// This method also clears the single-entry cache.
    ///
    /// @throws IllegalStateException If the current thread is not the owning thread
    public void clear() {
        // Safety: throws IllegalStateException for all non-owning threads
        ensureSameThread();
        // WaterMc start - 2x2 neighbourhood cache
        Arrays.fill(this.recentKeys, EMPTY_KEY);
        Arrays.fill(this.recentValues, null);
        // WaterMc end - 2x2 neighbourhood cache
        if (size == 0) return;
        size = 0;
        containsNullKey = false;
        Arrays.fill(key, (0));
        Arrays.fill(value, null);
    }

    /// Changes the owning thread of this map to the current thread.
    ///
    /// # Safety
    ///
    /// The caller must ensure proper happens before relationships
    /// when transferring ownership between threads.
    ///
    /// This should be done through proper synchronization mechanisms like
    /// [Thread#join()] or [Future#get()].
    ///
    /// @implNote This method does not perform synchronization
    public void setThread() {
        this.thread = Thread.currentThread();
    }

    /// Returns whether the current thread is the same as the owning thread.
    ///
    /// @return The current thread owns this map
    public boolean isSameThread() {
        return Thread.currentThread() == this.thread;
    }

    /// Ensure that the current thread is the owning thread,
    /// or the watchdog thread on crash.
    ///
    /// @throws IllegalStateException If the current thread is not the owning thread
    /// @see #isSameThread()
    /// @see #setThread()
    public void ensureSameThread() {
        if (!isSameThread() && !(Thread.currentThread() instanceof WatchdogThread)) {
            throw new IllegalStateException("Thread failed main thread check: Cannot update chunk status asynchronously, context=thread=" + Thread.currentThread().getName());
        }
    }

    // from fastutil
    private V removeEntry(final int pos) {
        final V oldValue = value[pos];
        value[pos] = null;
        size--;
        shiftKeys(pos);
        if (n > MIN_N && size < maxFill / 4 && n > it.unimi.dsi.fastutil.Hash.DEFAULT_INITIAL_SIZE) {
            rehash(n / 2);
        }
        return oldValue;
    }

    // from fastutil
    private void shiftKeys(int pos) {
        int last, slot;
        long curr;
        final long[] key = this.key;
        final @Nullable V[] value = this.value;
        for (;;) {
            pos = ((last = pos) + 1) & mask;
            for (;;) {
                if (((curr = key[pos]) == (0))) {
                    key[last] = (0);
                    value[last] = null;
                    return;
                }
                slot = (int) HashCommon.mix((curr)) & mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                pos = (pos + 1) & mask;
            }
            key[last] = curr;
            value[last] = value[pos];
        }
    }

    // from fastutil
    private V removeNullEntry() {
        containsNullKey = false;
        final V oldValue = value[n];
        value[n] = null;
        size--;
        if (n > MIN_N && size < maxFill / 4 && n > it.unimi.dsi.fastutil.Hash.DEFAULT_INITIAL_SIZE) rehash(n / 2);
        return oldValue;
    }

    // from fastutil
    private void rehash(final int newN) {
        final long[] key = this.key;
        final @Nullable V[] value = this.value;
        final int mask = newN - 1;
        final long[] newKey = new long[newN + 1];
        //noinspection unchecked
        final @Nullable V[] newValue = (V[])new Object[newN + 1];
        int i = n, pos;
        for (int j = realSize(); j-- != 0;) {
            //noinspection StatementWithEmptyBody
            while (((key[--i]) == (0)));
            if (!((newKey[pos = (int) HashCommon.mix((key[i])) & mask]) == (0)))
                //noinspection StatementWithEmptyBody
                while (!((newKey[pos = (pos + 1) & mask]) == (0)));
            newKey[pos] = key[i];
            newValue[pos] = value[i];
        }
        newValue[newN] = value[n];
        n = newN;
        this.mask = mask;
        maxFill = HashCommon.maxFill(n, FACTOR);
        this.key = newKey;
        this.value = newValue;
    }

    // from fastutil
    private int realSize() {
        return containsNullKey ? size - 1 : size;
    }

    // from fastutil
    private int find(final long k) {
        if (((k) == (0))) return containsNullKey ? n : -(n + 1);
        long curr;
        final long[] key = this.key;
        int pos;
        if (((curr = key[pos = (int) HashCommon.mix((k)) & mask]) == (0))) return -(pos + 1);
        if (((k) == (curr))) return pos;
        while (true) {
            if (((curr = key[pos = (pos + 1) & mask]) == (0))) return -(pos + 1);
            if (((k) == (curr))) return pos;
        }
    }

    // from fastutil
    private void insert(final int pos, final long k, final V v) {
        if (pos == n) containsNullKey = true;
        key[pos] = k;
        value[pos] = v;
        if (size++ >= maxFill) rehash(HashCommon.arraySize(size + 1, FACTOR));
    }
}
