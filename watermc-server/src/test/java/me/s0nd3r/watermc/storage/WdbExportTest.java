package me.s0nd3r.watermc.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/** Выгрузка .wdb в JSON: списки сервера — в ванильном виде массива, прочие журналы — объект ключ → значение. */
class WdbExportTest {

    private static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void serverListExportsAsVanillaArrayWithRemovalsApplied(@TempDir Path dir) throws Exception {
        Path wdb = dir.resolve("ops.wdb");
        WaterRecordLog log = WaterRecordLog.open(wdb, true).log();
        log.put("a", utf8("{\"uuid\":\"a\",\"name\":\"Alice\",\"level\":4}"));
        log.put("b", utf8("{\"uuid\":\"b\",\"name\":\"Bob\",\"level\":4}"));
        log.remove("a");
        log.put("c", utf8("{\"uuid\":\"c\",\"name\":\"Cat\",\"level\":2}"));
        log.flush().get();
        log.close();

        WdbExport.Result result = WdbExport.export(wdb, dir.resolve("export"), LocalDateTime.of(2026, 11, 19, 12, 0, 0));
        assertEquals(2, result.entries());
        assertEquals("ops-20261119-120000.json", result.output().getFileName().toString());
        JsonArray array = JsonParser.parseString(Files.readString(result.output())).getAsJsonArray();
        assertEquals(2, array.size());
        assertEquals("Bob", array.get(0).getAsJsonObject().get("name").getAsString());
        assertEquals(2, array.get(1).getAsJsonObject().get("level").getAsInt());
        assertTrue(Files.readAllBytes(wdb).length > 0, "the journal itself is untouched");
        try (var files = Files.list(dir.resolve("export"))) {
            assertTrue(files.noneMatch(p -> p.toString().endsWith(".tmp")), "no temporary file left behind");
        }
    }

    @Test
    void otherJournalsExportAsObjectAndKeepNonJsonValues(@TempDir Path dir) throws Exception {
        Path wdb = dir.resolve("script-variables.wdb");
        WaterRecordLog log = WaterRecordLog.open(wdb, false).log();
        log.put("coins", utf8("42"));
        log.put("motd", utf8("not json {"));
        log.flush().get();
        log.close();

        List<WdbExport.Result> results = WdbExport.exportAll(dir, dir.resolve("export"), LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        assertEquals(1, results.size());
        JsonObject object = JsonParser.parseString(Files.readString(results.get(0).output())).getAsJsonObject();
        assertEquals(42, object.get("coins").getAsInt());
        assertEquals("not json {", object.get("motd").getAsString());
    }

    @Test
    void infoCountsLiveEntriesAndRecords(@TempDir Path dir) throws Exception {
        Path wdb = dir.resolve("whitelist.wdb");
        WaterRecordLog log = WaterRecordLog.open(wdb, false).log();
        log.put("x", utf8("{}"));
        log.put("x", utf8("{\"v\":1}"));
        log.put("y", utf8("{}"));
        log.flush().get();
        log.close();
        WdbExport.Info info = WdbExport.info(wdb);
        assertEquals("whitelist", info.name());
        assertEquals(2, info.entries());
        assertEquals(3, info.records());
        JsonElement doc = WdbExport.document("whitelist", WaterRecordLog.read(wdb).entries());
        assertTrue(doc.isJsonArray());
    }
}
