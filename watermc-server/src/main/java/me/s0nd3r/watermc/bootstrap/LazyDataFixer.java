package me.s0nd3r.watermc.bootstrap;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.function.Supplier;

/** Builds schemas once, only when a conversion or explicit schema lookup needs them. */
public final class LazyDataFixer implements DataFixer {
    private final Supplier<DataFixer> factory;
    private volatile DataFixer delegate;

    public LazyDataFixer(Supplier<DataFixer> factory) { this.factory = Objects.requireNonNull(factory); }

    private DataFixer delegate() {
        DataFixer value = this.delegate;
        if (value == null) {
            synchronized (this) {
                value = this.delegate;
                if (value == null) this.delegate = value = Objects.requireNonNull(this.factory.get());
            }
        }
        return value;
    }

    public boolean isInitialized() { return this.delegate != null; }

    @Override public <T> Dynamic<T> update(TypeReference type, Dynamic<T> input, int from, int to) {
        return from >= to ? input : this.delegate().update(type, input, from, to);
    }
    @Override public Schema getSchema(int version) { return this.delegate().getSchema(version); }
}
