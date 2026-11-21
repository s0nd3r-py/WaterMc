package me.s0nd3r.watermc.core.util.map;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSpliterator;

import java.io.Serial;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * WaterMc: потокобезопасный {@link LongOpenHashSet} для multithreaded tracker.
 * Вместо монитора используется {@link java.util.concurrent.locks.ReentrantReadWriteLock}: проверки
 * {@code contains} из нескольких потоков трекера выполняются параллельно, а записи с главного потока
 * остаются эксклюзивными. Блокировка реентерабельна, поэтому унаследованные bulk-операции fastutil,
 * которые вызывают переопределённые {@code add}/{@code remove}, не блокируют сами себя.
 */
public class SyncLongOpenHashSet extends LongOpenHashSet {
    private transient java.util.concurrent.locks.ReentrantReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SyncLongOpenHashSet() {
    }

    @Override
    public boolean remove(final long k) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.remove(k);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(final long k) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.add(k);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean contains(final long k) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.contains(k);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Deprecated
    public boolean rem(final long k) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.remove(k);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long[] toLongArray() {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.toLongArray();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.toArray();
        } finally {
            lock.unlock();
        }
    }

    @Deprecated
    @Override
    public long[] toLongArray(final long[] a) {
        return toArray(a);
    }

    @Override
    public long[] toArray(final long[] a) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(final LongCollection c) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.addAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAll(final LongCollection c) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.containsAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeAll(final LongCollection c) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.removeAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeIf(Predicate<? super Long> filter) {
        return super.removeIf(filter);
    }

    @Override
    public boolean retainAll(final LongCollection c) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.retainAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Deprecated
    public boolean add(final Long k) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.add(k.longValue());
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Deprecated
    public boolean contains(final Object k) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.contains((((Long) k).longValue()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Deprecated
    public boolean remove(final Object k) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.remove((((Long) k).longValue()));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public LongIterator longIterator() {
        return super.longIterator();
    }

    @Override
    public LongSpliterator longSpliterator() {
        return super.longSpliterator();
    }

    @Override
    public java.util.stream.LongStream longStream() {
        return super.longStream();
    }

    @Override
    public java.util.stream.LongStream longParallelStream() {
        return super.longParallelStream();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.toArray(a);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.toArray(generator);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public LongIterator iterator() {
        return super.iterator();
    }

    @Override
    public LongSpliterator spliterator() {
        return super.spliterator();
    }

    @Deprecated
    @Override
    public java.util.stream.Stream<Long> stream() {
        return super.stream();
    }

    @Deprecated
    @Override
    public java.util.stream.Stream<Long> parallelStream() {
        return super.parallelStream();
    }

    @Override
    public void forEach(final java.util.function.LongConsumer action) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            super.forEach(action);
        } finally {
            lock.unlock();
        }
    }

    @Deprecated
    @Override
    public void forEach(Consumer<? super Long> action) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            super.forEach(action);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean addAll(final Collection<? extends Long> c) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.addAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.containsAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.removeAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.retainAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean removeIf(final java.util.function.LongPredicate filter) {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            return super.removeIf(filter);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        final java.util.concurrent.locks.Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            super.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.toString();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int hashCode() {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.hashCode();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            return super.equals(o);
        } finally {
            lock.unlock();
        }
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            s.defaultWriteObject();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public LongOpenHashSet clone() {
        final java.util.concurrent.locks.Lock lock = this.lock.readLock();
        lock.lock();
        try {
            final SyncLongOpenHashSet copy = (SyncLongOpenHashSet) super.clone();
            copy.lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
            return copy;
        } finally {
            lock.unlock();
        }
    }
}
