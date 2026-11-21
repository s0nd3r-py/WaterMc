package me.s0nd3r.watermc.storage;

import com.github.benmanes.caffeine.cache.Cache;
import java.lang.ref.WeakReference;
import net.minecraft.world.level.chunk.PalettedContainer;
import me.s0nd3r.watermc.util.WaterCaches;

/** Owner-thread snapshots only. Neither the global value nor the owner slot retains live sections. */
public final class SectionPacketCache {
    private static final long MAXIMUM_BYTES = Math.max(0L, Long.getLong("watermc.section-packet-cache-bytes", 32L << 20));
    private static final Cache<WaterSectionStorage, Entry<?>> CACHE = WaterCaches.newBuilder().weakKeys()
        .maximumWeight(MAXIMUM_BYTES).weigher((WaterSectionStorage key, Entry<?> value) -> value.weight).build();
    private SectionPacketCache() {}
    public static boolean enabled() { return MAXIMUM_BYTES != 0; }
    public static void invalidateAll() { CACHE.invalidateAll(); } // WaterMc - heap guard

    public static final class Slot<T> implements Runnable {
        private final WaterSectionStorage source;
        private volatile WeakReference<Entry<T>> cached;
        public Slot(WaterSectionStorage source) { this.source = source; source.setMutationListener(this); }
        public boolean owns(WaterSectionStorage storage) { return this.source == storage; }
        public PalettedContainer.Data<T> get(PalettedContainer.Data<T> sourceData) {
            WeakReference<Entry<T>> reference = this.cached;
            Entry<T> entry = reference == null ? null : reference.get();
            return entry != null && entry.sourceData.get() == sourceData ? entry.encoded : null;
        }
        public void store(PalettedContainer.Data<T> sourceData, PalettedContainer.Data<T> encoded) {
            Entry<T> entry = new Entry<>(sourceData, encoded);
            CACHE.put(this.source, entry);
            this.cached = new WeakReference<>(entry);
        }
        @Override public void run() {
            if (this.cached != null) {
                // Logical invalidation only: no cache lock or allocation in a block mutation.
                // The next packet replaces the bounded old entry; weak keys allow chunk reclamation.
                this.cached = null;
            }
        }
    }
    private static final class Entry<T> {
        final WeakReference<PalettedContainer.Data<T>> sourceData;
        final PalettedContainer.Data<T> encoded;
        final int weight;
        Entry(PalettedContainer.Data<T> sourceData, PalettedContainer.Data<T> encoded) {
            this.sourceData = new WeakReference<>(sourceData);
            this.encoded = encoded;
            this.weight = 512 + encoded.storage().getRaw().length * Long.BYTES
                + (encoded.configuration() instanceof net.minecraft.world.level.chunk.Configuration.Global ? 0 : encoded.palette().getSize() * 16);
        }
    }
}
