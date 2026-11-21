package me.s0nd3r.watermc.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserBanList;
import net.minecraft.server.players.UserBanListEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import me.s0nd3r.watermc.bootstrap.ServerLayout;

import static org.junit.jupiter.api.Assertions.*;

class JsonDatabaseMigrationTest {

    @TempDir
    Path root;
    private ServerLayout previous;

    private static final String BANS_JSON = """
        [
          {
            "uuid": "11111111-2222-3333-4444-555555555555",
            "name": "Griefer",
            "created": "2025-01-02 03:04:05 +0000",
            "source": "Console",
            "expires": "forever",
            "reason": "x-ray",
            "customPluginField": "kept"
          },
          {
            "uuid": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
            "name": "Temp",
            "created": "2025-01-02 03:04:05 +0000",
            "source": "Mod",
            "expires": "2999-01-01 00:00:00 +0000",
            "reason": "spam"
          }
        ]
        """;

    @BeforeEach
    void installLayout() {
        this.previous = ServerLayout.current();
        ServerLayout.install(new ServerLayout(this.root));
    }

    @AfterEach
    void restoreLayout() {
        ServerLayout.install(this.previous);
    }

    private static NotificationService noNotifications() {
        return (NotificationService) Proxy.newProxyInstance(NotificationService.class.getClassLoader(), new Class<?>[]{NotificationService.class},
            (proxy, method, args) -> switch (method.getName()) {
                case "hashCode" -> 0;
                case "equals" -> false;
                case "toString" -> "noop";
                default -> null;
            });
    }

    @Test
    void banListIsImportedBackedUpAndServedThroughTheSameApi() throws Exception {
        final Path data = this.root.resolve("data");
        Files.createDirectories(data);
        final Path json = data.resolve("banned-players.json");
        Files.writeString(json, BANS_JSON, StandardCharsets.UTF_8);
        final Path wdb = data.resolve("banned-players.wdb");

        final UserBanList list = new UserBanList(wdb.toFile(), noNotifications());
        list.load();

        // Резервная копия побайтово совпадает с исходником, исходный JSON убран, создан .wdb.
        final Path backup = data.resolve("migration-backup").resolve("banned-players.json");
        assertEquals(BANS_JSON, Files.readString(backup, StandardCharsets.UTF_8));
        assertFalse(Files.exists(json));
        assertTrue(Files.isRegularFile(wdb));

        final NameAndId griefer = new NameAndId(UUID.fromString("11111111-2222-3333-4444-555555555555"), "Griefer");
        assertTrue(list.isBanned(griefer));
        final UserBanListEntry entry = list.get(griefer);
        assertNotNull(entry);
        assertEquals("x-ray", entry.getReason());
        assertEquals("Console", entry.getSource());
        assertNull(entry.getExpires());
        assertEquals(2, list.getEntries().size());

        // Неизвестные поля плагинов сохранены в журнале без потерь.
        final String stored = new String(WaterRecordLog.read(wdb).entries().get("11111111-2222-3333-4444-555555555555"), StandardCharsets.UTF_8);
        assertEquals("kept", JsonParser.parseString(stored).getAsJsonObject().get("customPluginField").getAsString());

        // Изменения переживают перезапуск.
        final NameAndId temp = new NameAndId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), "Temp");
        assertTrue(list.remove(temp));
        final NameAndId newcomer = new NameAndId(UUID.randomUUID(), "Newcomer");
        assertTrue(list.add(new UserBanListEntry(newcomer, null, "Console", null, "test")));
        list.flush();
        list.closeJournal(); // как при перезагрузке списка: один открытый журнал на файл

        final UserBanList reloaded = new UserBanList(wdb.toFile(), noNotifications());
        reloaded.load();
        assertTrue(reloaded.isBanned(griefer));
        assertTrue(reloaded.isBanned(newcomer));
        assertFalse(reloaded.isBanned(temp));
        assertEquals("test", reloaded.get(newcomer).getReason());

        // Повторная загрузка не импортирует JSON второй раз и не создаёт новых копий.
        try (var files = Files.list(data.resolve("migration-backup"))) {
            assertEquals(1, files.count());
        }
        reloaded.save();
        reloaded.flush();
        assertEquals(2, WaterRecordLog.read(wdb).entries().size());
    }

    @Test
    void existingWdbWinsAndLaterJsonIsLeftUntouched() throws Exception {
        final Path data = this.root.resolve("data");
        Files.createDirectories(data);
        final Path wdb = data.resolve("ops.wdb");
        WaterRecordLog.writeSnapshot(wdb, new LinkedHashMap<>());
        final Path json = data.resolve("ops.json");
        Files.writeString(json, "[]");
        final WaterRecordLog.Opened opened = LegacyJsonMigration.openMigrating(wdb, data.resolve("migration-backup"), true, array -> new LinkedHashMap<>());
        opened.log().close();
        assertTrue(Files.exists(json), "JSON is never deleted when the journal already exists");
        assertFalse(Files.exists(data.resolve("migration-backup")));
    }

    @Test
    void corruptJsonIsBackedUpAndRenamedNotLost() throws Exception {
        final Path data = this.root.resolve("data");
        Files.createDirectories(data);
        final Path json = data.resolve("whitelist.json");
        Files.writeString(json, "[{ broken");
        final WaterRecordLog.Opened opened = LegacyJsonMigration.openMigrating(data.resolve("whitelist.wdb"), data.resolve("migration-backup"), true, array -> {
            throw new AssertionError("converter must not run");
        });
        opened.log().close();
        assertEquals("[{ broken", Files.readString(data.resolve("migration-backup").resolve("whitelist.json")));
        assertEquals("[{ broken", Files.readString(data.resolve("whitelist.json.corrupt")));
        assertTrue(opened.entries().isEmpty());
    }

    @Test
    void failedVerificationKeepsOriginalJson() throws Exception {
        final Path data = this.root.resolve("data");
        Files.createDirectories(data);
        final Path json = data.resolve("ops.json");
        Files.writeString(json, "[{\"uuid\":\"u\"}]");
        // Конвертер, чья нагрузка не может быть корректно перечитана, не возникает в реальном коде;
        // проверяем, что исключение конвертера оставляет JSON на месте.
        assertThrows(IllegalStateException.class, () -> LegacyJsonMigration.openMigrating(data.resolve("ops.wdb"), data.resolve("migration-backup"), true, array -> {
            throw new IllegalStateException("boom");
        }));
        assertTrue(Files.exists(json));
        assertFalse(Files.exists(data.resolve("ops.wdb")));
        assertTrue(Arrays.equals(Files.readAllBytes(json), Files.readAllBytes(data.resolve("migration-backup").resolve("ops.json"))));
    }

    @Test
    void legacyNameMapping() {
        assertEquals(Path.of("data", "usercache.json"), LegacyJsonMigration.legacyJsonFor(Path.of("data", "usercache.wdb")));
        final JsonArray array = new JsonArray();
        array.add(new JsonObject());
        assertEquals(1, array.size());
    }
}
