package me.s0nd3r.watermc.script;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import me.s0nd3r.watermc.storage.WaterRecordLog;

/**
 * Глобальные переменные скриптов ({@code $имя}). Сохраняются между перезапусками в
 * {@code data/script-variables.wdb} (журнал с дозаписью и защитой от оборванных записей).
 *
 * <p>Хранить можно только число, текст, логическое значение и списки из них — объект игрока или блока
 * сохранить нельзя (сохраните {@code player.uuid} или {@code player.name}).</p>
 */
public final class GlobalStore {

    private final Map<String, Object> values = new ConcurrentHashMap<>();
    private final Map<String, String> persisted = new HashMap<>();
    /** Names changed by {@link #set} since the last flush; lists are re-checked anyway (they can change in place). */
    private final Set<String> dirty = ConcurrentHashMap.newKeySet();
    private final @Nullable WaterRecordLog log;

    private GlobalStore(final @Nullable WaterRecordLog log) {
        this.log = log;
    }

    public static GlobalStore inMemory() {
        return new GlobalStore(null);
    }

    public static GlobalStore open(final Path file) throws IOException {
        final WaterRecordLog.Opened opened = WaterRecordLog.open(file, false);
        final GlobalStore store = new GlobalStore(opened.log());
        for (final Map.Entry<String, byte[]> entry : opened.entries().entrySet()) {
            final String json = new String(entry.getValue(), StandardCharsets.UTF_8);
            store.values.put(entry.getKey(), decode(JsonParser.parseString(json)));
            store.persisted.put(entry.getKey(), json);
        }
        return store;
    }

    public @Nullable Object get(final String name) {
        return this.values.get(name);
    }

    public void set(final String name, final @Nullable Object value) {
        if (value == null) {
            this.values.remove(name);
            this.dirty.add(name);
            return;
        }
        if (!storable(value, 0)) {
            throw new IllegalArgumentException("в $" + name + " можно хранить только числа, текст, true/false и списки из них, а не " + Values.typeOf(value));
        }
        this.values.put(name, value);
        this.dirty.add(name);
    }

    public Set<String> names() {
        return this.values.keySet();
    }

    /**
     * Записывает изменившиеся переменные. Вызывается периодически и при выгрузке скриптов.
     *
     * <p>Кодируются только имена, изменённые через {@link #set}, и списки (их содержимое может меняться на
     * месте). Раньше каждый вызов заново кодировал в JSON все переменные, и при большом числе переменных
     * сохранение раз в минуту занимало заметное время основного потока.</p>
     */
    public synchronized void flush() {
        if (this.log == null) {
            this.dirty.clear();
            return;
        }
        final Set<String> check = new java.util.LinkedHashSet<>(this.dirty);
        this.dirty.removeAll(check);
        for (final Map.Entry<String, Object> entry : this.values.entrySet()) {
            if (entry.getValue() instanceof List<?>) {
                check.add(entry.getKey());
            }
        }
        for (final String name : check) {
            final Object value = this.values.get(name);
            if (value == null) {
                if (this.persisted.remove(name) != null) {
                    this.log.remove(name);
                }
                continue;
            }
            final String encoded;
            try {
                encoded = encode(value).toString();
            } catch (final IllegalArgumentException ex) {
                continue; // в список положили объект — такое значение не сохраняется
            }
            if (!encoded.equals(this.persisted.get(name))) {
                this.log.put(name, encoded.getBytes(StandardCharsets.UTF_8));
                this.persisted.put(name, encoded);
            }
        }
        if (this.log.shouldCompact(this.persisted.size())) {
            final Map<String, byte[]> snapshot = new HashMap<>();
            this.persisted.forEach((k, v) -> snapshot.put(k, v.getBytes(StandardCharsets.UTF_8)));
            this.log.compact(() -> snapshot);
        }
    }

    public void close() throws IOException {
        this.flush();
        if (this.log != null) {
            this.log.close();
        }
    }

    private static boolean storable(final Object value, final int depth) {
        if (depth > 16) {
            return false;
        }
        if (value == null || value instanceof Double || value instanceof String || value instanceof Boolean) {
            return true;
        }
        if (value instanceof List<?> list) {
            for (final Object item : list) {
                if (!storable(item, depth + 1)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    static JsonElement encode(final Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof Double d) {
            return new JsonPrimitive(d);
        }
        if (value instanceof String s) {
            return new JsonPrimitive(s);
        }
        if (value instanceof Boolean b) {
            return new JsonPrimitive(b);
        }
        if (value instanceof List<?> list) {
            final JsonArray array = new JsonArray(list.size());
            for (final Object item : list) {
                array.add(encode(item));
            }
            return array;
        }
        throw new IllegalArgumentException("не сохраняется: " + Values.typeOf(value));
    }

    static Object decode(final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonArray()) {
            final List<Object> list = new ArrayList<>(element.getAsJsonArray().size());
            for (final JsonElement item : element.getAsJsonArray()) {
                list.add(decode(item));
            }
            return list;
        }
        final JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isNumber()) {
            return primitive.getAsDouble();
        }
        return primitive.getAsString();
    }
}
