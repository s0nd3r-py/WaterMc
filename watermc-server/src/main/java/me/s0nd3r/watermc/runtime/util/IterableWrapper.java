package me.s0nd3r.watermc.runtime.util;

import org.jspecify.annotations.NullMarked;

import java.util.Iterator;

@NullMarked
public record IterableWrapper<T>(Iterator<T> iterator) implements Iterable<T> {
    @Override
    public Iterator<T> iterator() {
        return iterator;
    }
}
