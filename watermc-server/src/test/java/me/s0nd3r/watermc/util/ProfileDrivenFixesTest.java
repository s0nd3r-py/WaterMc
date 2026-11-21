package me.s0nd3r.watermc.util;

import me.s0nd3r.watermc.storage.WaterSectionStorage;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Правки, найденные профилированием сцены с 1500 мобами. */
class ProfileDrivenFixesTest {

    @Test
    void sectionStorageStillRejectsOutOfRangeIndexesInBothStates() {
        final WaterSectionStorage storage = new WaterSectionStorage(15, 7);
        assertEquals(7, storage.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> storage.get(-1), "compact state");
        assertThrows(IndexOutOfBoundsException.class, () -> storage.get(WaterSectionStorage.SIZE), "compact state");
        storage.set(5, 9);
        assertEquals(9, storage.get(5));
        assertEquals(7, storage.get(6));
        assertThrows(IndexOutOfBoundsException.class, () -> storage.get(-1), "materialized state");
        assertThrows(IndexOutOfBoundsException.class, () -> storage.get(WaterSectionStorage.SIZE), "materialized state");
    }

    @Test
    void inactiveProfilerIsNotRegisteredButActiveOneStillIs() {
        try (Profiler.Scope outer = Profiler.use(InactiveProfiler.INSTANCE)) {
            assertSame(InactiveProfiler.INSTANCE, Profiler.get());
            // Раньше повторный use() на том же потоке бросал «Profiler is already active» даже для неактивного.
            try (Profiler.Scope nested = Profiler.use(InactiveProfiler.INSTANCE)) {
                assertSame(InactiveProfiler.INSTANCE, Profiler.get());
            }
        }
        final ProfilerFiller active = new net.minecraft.util.profiling.ActiveProfiler(System::nanoTime, () -> 0, () -> true);
        try (Profiler.Scope scope = Profiler.use(active)) {
            assertNotSame(InactiveProfiler.INSTANCE, Profiler.get(), "a real profiler is still installed for /debug and /perf");
        }
        assertSame(InactiveProfiler.INSTANCE, Profiler.get());
    }
}
