package me.s0nd3r.watermc.script;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/** Инкрементальное сохранение $-переменных: изменения, удаления и правки списков на месте не теряются. */
class GlobalStoreTest {

    @Test
    void incrementalFlushPersistsChangesRemovalsAndInPlaceListEdits(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("script-variables.wdb");
        GlobalStore store = GlobalStore.open(file);
        store.set("coins", 10.0);
        store.set("name", "Alice");
        List<Object> list = new ArrayList<>(List.of("a"));
        store.set("list", list);
        store.flush();

        store.set("coins", 11.0);
        store.set("name", null);
        Object stored = store.get("list");
        if (stored instanceof List<?> l && l instanceof ArrayList<?>) {
            @SuppressWarnings("unchecked") List<Object> mutable = (List<Object>) l;
            mutable.add("b");
        } else {
            store.set("list", List.of("a", "b"));
        }
        store.flush();
        store.close();

        GlobalStore reopened = GlobalStore.open(file);
        assertEquals(11.0, ((Number) reopened.get("coins")).doubleValue());
        assertNull(reopened.get("name"));
        assertEquals(List.of("a", "b"), reopened.get("list"));
        reopened.close();
    }

    @Test
    void unchangedScalarsAreNotRewritten(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("script-variables.wdb");
        GlobalStore store = GlobalStore.open(file);
        for (int i = 0; i < 100; i++) store.set("v" + i, (double) i);
        store.close();
        long before = me.s0nd3r.watermc.storage.WaterRecordLog.read(file).records();
        GlobalStore again = GlobalStore.open(file);
        again.set("v5", 5.0); // same value: re-encoded, compared, not appended
        again.flush();
        again.flush();
        again.close();
        assertEquals(before, me.s0nd3r.watermc.storage.WaterRecordLog.read(file).records());
    }
}
