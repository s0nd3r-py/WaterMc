package me.s0nd3r.watermc.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.s0nd3r.watermc.bootstrap.ServerLayout;

/**
 * Перенос JSON-баз ({@code ops.json}, {@code whitelist.json}, {@code banned-*.json}, {@code usercache.json})
 * в журналы {@code .wdb}.
 *
 * <p>Порядок действий исключает потерю данных: копия исходника в {@code data/migration-backup/} →
 * разбор → запись снимка {@code .wdb} (tmp + fsync + атомарная замена) → контрольное чтение и побайтовое
 * сравнение → только затем исходный JSON удаляется. При любой ошибке JSON остаётся на месте.</p>
 */
public final class LegacyJsonMigration {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");
    public static final String BACKUP_DIR = "migration-backup";

    private LegacyJsonMigration() {
    }

    public static Path backupDirectory() {
        return ServerLayout.current().dataDir().resolve(BACKUP_DIR);
    }

    /** Имя JSON-файла, соответствующего журналу: {@code ops.wdb} → {@code ops.json}. */
    public static Path legacyJsonFor(final Path wdb) {
        final String name = wdb.getFileName().toString();
        final String base = name.endsWith(".wdb") ? name.substring(0, name.length() - 4) : name;
        return wdb.resolveSibling(base + ".json");
    }

    /**
     * Открывает журнал, при необходимости импортировав JSON.
     *
     * @param converter JSON-массив → записи журнала (ключ → нагрузка) в порядке массива
     */
    public static WaterRecordLog.Opened openMigrating(final Path wdb, final Path backupDir, final boolean syncEachWrite,
                                                      final Function<JsonArray, LinkedHashMap<String, byte[]>> converter) throws IOException {
        final Path json = legacyJsonFor(wdb);
        if (!Files.exists(wdb, LinkOption.NOFOLLOW_LINKS) && Files.isRegularFile(json, LinkOption.NOFOLLOW_LINKS)) {
            migrate(json, wdb, backupDir, converter);
        }
        return WaterRecordLog.open(wdb, syncEachWrite);
    }

    static void migrate(final Path json, final Path wdb, final Path backupDir,
                        final Function<JsonArray, LinkedHashMap<String, byte[]>> converter) throws IOException {
        final Path backup = backup(json, backupDir);
        final JsonArray array;
        try (Reader reader = Files.newBufferedReader(json, StandardCharsets.UTF_8)) {
            final JsonElement parsed = JsonParser.parseReader(reader);
            array = parsed == null || parsed.isJsonNull() ? new JsonArray() : parsed.getAsJsonArray();
        } catch (final RuntimeException ex) {
            // Повреждённый JSON: прежнее поведение Spigot — начать с пустого списка. Данные сохранены в резервной копии.
            final Path corrupt = json.resolveSibling(json.getFileName() + ".corrupt");
            Files.move(json, corrupt, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.error("{} is corrupted and cannot be imported; copy: {}, original renamed to {}", json, backup, corrupt, ex);
            return;
        }
        final LinkedHashMap<String, byte[]> entries = converter.apply(array);
        WaterRecordLog.writeSnapshot(wdb, entries);
        verify(wdb, entries);
        Files.delete(json);
        LOGGER.info("Imported {} entries from {} into {} (backup: {})", entries.size(), json.getFileName(), wdb.getFileName(), backup);
    }

    private static void verify(final Path wdb, final Map<String, byte[]> expected) throws IOException {
        final WaterRecordLog.Contents contents = WaterRecordLog.read(wdb);
        if (contents.truncatedTail() || contents.entries().size() != expected.size()) {
            throw new IOException("Контрольное чтение " + wdb + " не совпало: " + contents.entries().size() + " из " + expected.size());
        }
        for (final Map.Entry<String, byte[]> entry : expected.entrySet()) {
            if (!Arrays.equals(entry.getValue(), contents.entries().get(entry.getKey()))) {
                throw new IOException("Контрольное чтение " + wdb + " не совпало по ключу " + entry.getKey());
            }
        }
    }

    /** Копирует файл в каталог резервных копий, не перезаписывая прежние копии. */
    public static Path backup(final Path file, final Path backupDir) throws IOException {
        Files.createDirectories(backupDir);
        Path target = backupDir.resolve(file.getFileName());
        for (int i = 1; Files.exists(target, LinkOption.NOFOLLOW_LINKS); i++) {
            target = backupDir.resolve(file.getFileName() + "." + i);
        }
        Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
        return target;
    }

    public static byte[] utf8(final String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
