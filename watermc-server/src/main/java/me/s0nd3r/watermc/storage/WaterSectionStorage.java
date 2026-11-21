package me.s0nd3r.watermc.storage;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntConsumer;
import net.minecraft.util.BitStorage;
import net.minecraft.util.SimpleBitStorage;

/** Flat global state IDs. Entries are independently atomic; snapshots require the chunk owner. */
public final class WaterSectionStorage implements BitStorage {
    public static final int SIZE = 4096;
    private static final VarHandle ENTRY = MethodHandles.arrayElementVarHandle(long[].class);
    private static final VarHandle VALUES;
    static {
        try {
            VALUES = MethodHandles.lookup().findVarHandle(WaterSectionStorage.class, "values", long[].class);
        } catch (ReflectiveOperationException failure) {
            throw new ExceptionInInitializerError(failure);
        }
    }
    private final int bits;
    private final long mask;
    private final int defaultId;
    private volatile long[] values;
    private Runnable mutationListener;

    public WaterSectionStorage(int bits, int defaultId) {
        if (bits < 1 || bits > 32) throw new IllegalArgumentException("Invalid state ID bit width: " + bits);
        this.bits = bits;
        this.mask = (1L << bits) - 1L;
        this.validate(defaultId);
        this.defaultId = defaultId;
    }

    private void validate(int value) {
        if (value < 0 || ((long) value & ~this.mask) != 0) throw new IllegalArgumentException("State ID outside bit width: " + value);
    }

    private long[] materialize() {
        long[] current = (long[]) VALUES.getAcquire(this);
        if (current == null) {
            long[] candidate = new long[SIZE];
            if (this.defaultId != 0) Arrays.fill(candidate, this.defaultId);
            if (VALUES.compareAndSet(this, null, candidate)) return candidate;
            current = (long[]) VALUES.getAcquire(this);
        }
        return current;
    }

    @Override
    public int get(int index) {
        long[] current = (long[]) VALUES.getAcquire(this);
        if (current == null) {
            Objects.checkIndex(index, SIZE);
            return this.defaultId;
        }
        // The array VarHandle already bounds-checks (ArrayIndexOutOfBoundsException); a second explicit check was
        // 2.3% of tick CPU in a 1500-mob profile.
        return (int) (long) ENTRY.getAcquire(current, index);
    }

    @Override
    public int getAndSet(int index, int value) {
        Objects.checkIndex(index, SIZE);
        this.validate(value);
        long[] current = (long[]) VALUES.getAcquire(this);
        // A same-value CAS still publishes preceding writes while preserving compact storage.
        if (current == null && value == this.defaultId && VALUES.compareAndSet(this, null, null)) return this.defaultId;
        if (current == null) current = this.materialize();
        int previous = (int) (long) ENTRY.getAndSet(current, index, (long) value);
        if (previous != value && this.mutationListener != null) this.mutationListener.run();
        return previous;
    }

    @Override
    public void set(int index, int value) {
        Objects.checkIndex(index, SIZE);
        this.validate(value);
        long[] current = (long[]) VALUES.getAcquire(this);
        if (current == null && value == this.defaultId && VALUES.compareAndSet(this, null, null)) return;
        if (current == null) current = this.materialize();
        int previous = (int) (long) ENTRY.getAcquire(current, index);
        ENTRY.setRelease(current, index, (long) value);
        if (previous != value && this.mutationListener != null) this.mutationListener.run();
    }

    /** Owner-thread cache invalidation hook. Copies intentionally have no listener. */
    public void setMutationListener(Runnable listener) { this.mutationListener = Objects.requireNonNull(listener); }

    public int encodedLength() { return (SIZE + 64 / this.bits - 1) / (64 / this.bits); }
    public boolean isCompact() { return VALUES.getAcquire(this) == null; }
    @Override public int getBits() { return this.bits; }
    @Override public int getSize() { return SIZE; }

    /** Wire-format snapshot, not a mutable view of the flat backing store. */
    @Override
    public long[] getRaw() {
        SimpleBitStorage encoded = new SimpleBitStorage(this.bits, SIZE);
        for (int i = 0; i < SIZE; ++i) encoded.set(i, this.get(i));
        return encoded.getRaw();
    }

    @Override
    public void getAll(IntConsumer consumer) {
        Objects.requireNonNull(consumer);
        for (int i = 0; i < SIZE; ++i) consumer.accept(this.get(i));
    }

    @Override
    public void unpack(int[] output) {
        Objects.checkFromToIndex(0, SIZE, output.length);
        for (int i = 0; i < SIZE; ++i) output[i] = this.get(i);
    }

    @Override
    public WaterSectionStorage copy() {
        WaterSectionStorage copy = new WaterSectionStorage(this.bits, this.defaultId);
        long[] current = (long[]) VALUES.getAcquire(this);
        if (current != null) {
            long[] snapshot = new long[SIZE];
            for (int i = 0; i < SIZE; ++i) snapshot[i] = (long) ENTRY.getAcquire(current, i);
            VALUES.setRelease(copy, snapshot);
        }
        return copy;
    }
}
