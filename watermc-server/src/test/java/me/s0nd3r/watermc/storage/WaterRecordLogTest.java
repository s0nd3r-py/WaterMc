package me.s0nd3r.watermc.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class WaterRecordLogTest {

    @TempDir
    Path dir;

    private static byte[] b(final String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private static String s(final byte[] b) {
        return new String(b, StandardCharsets.UTF_8);
    }

    @Test
    void appendsAreReplayedInOrder() throws Exception {
        final Path file = this.dir.resolve("ops.wdb");
        final WaterRecordLog.Opened opened = WaterRecordLog.open(file, true);
        assertTrue(opened.entries().isEmpty());
        final WaterRecordLog log = opened.log();
        log.put("a", b("1"));
        log.put("b", b("2"));
        log.put("a", b("3"));
        log.remove("b");
        log.put("c", b("4"));
        log.flush().get(10, TimeUnit.SECONDS);

        final WaterRecordLog.Contents contents = WaterRecordLog.read(file);
        assertFalse(contents.truncatedTail());
        assertEquals(List.of("a", "c"), List.copyOf(contents.entries().keySet()));
        assertEquals("3", s(contents.entries().get("a")));
        assertEquals(5, contents.records());

        log.clear();
        log.put("d", b("5"));
        log.close();
        assertEquals(Map.of("d", "5"), toStrings(WaterRecordLog.read(file).entries()));
    }

    @Test
    void addingAnEntryAppendsInsteadOfRewriting() throws Exception {
        final Path file = this.dir.resolve("whitelist.wdb");
        final WaterRecordLog log = WaterRecordLog.open(file, false).log();
        for (int i = 0; i < 100; i++) {
            log.put("k" + i, b("v" + i));
        }
        log.flush().get(10, TimeUnit.SECONDS);
        final long before = Files.size(file);
        log.put("new", b("x"));
        log.flush().get(10, TimeUnit.SECONDS);
        final long record = 1 + 4 + 3 + 4 + 1 + 4;
        assertEquals(before + record, Files.size(file), "one change = one appended record");
        log.close();
    }

    @Test
    void tornTailAfterCrashIsDroppedAndEarlierRecordsSurvive() throws Exception {
        final Path file = this.dir.resolve("banned-players.wdb");
        final WaterRecordLog log = WaterRecordLog.open(file, true).log();
        log.put("a", b("alpha"));
        log.put("b", b("beta"));
        log.close();
        final long valid = Files.size(file);

        // Неполная запись: заголовок записи без нагрузки и CRC.
        try (FileChannel ch = FileChannel.open(file, StandardOpenOption.APPEND)) {
            ch.write(ByteBuffer.wrap(new byte[]{WaterRecordLog.OP_PUT, 0, 0, 0, 5, 'x', 'y'}));
        }
        final WaterRecordLog.Opened reopened = WaterRecordLog.open(file, true);
        assertTrue(reopened.truncatedTail());
        assertEquals(Map.of("a", "alpha", "b", "beta"), toStrings(reopened.entries()));
        assertEquals(valid, Files.size(file), "the torn tail is truncated");
        reopened.log().put("c", b("gamma"));
        reopened.log().close();
        assertEquals(Map.of("a", "alpha", "b", "beta", "c", "gamma"), toStrings(WaterRecordLog.read(file).entries()));
    }

    @Test
    void corruptedRecordStopsReplayAtLastGoodRecord() throws Exception {
        final Path file = this.dir.resolve("banned-ips.wdb");
        final WaterRecordLog log = WaterRecordLog.open(file, true).log();
        log.put("a", b("alpha"));
        log.put("b", b("beta"));
        log.close();
        final byte[] data = Files.readAllBytes(file);
        data[data.length - 6] ^= 0x5A; // повреждаем нагрузку последней записи
        Files.write(file, data);
        final WaterRecordLog.Contents contents = WaterRecordLog.read(file);
        assertTrue(contents.truncatedTail());
        assertEquals(Map.of("a", "alpha"), toStrings(contents.entries()));
    }

    @Test
    void compactionKeepsOnlyLiveEntries() throws Exception {
        final Path file = this.dir.resolve("usercache.wdb");
        final WaterRecordLog log = WaterRecordLog.open(file, false).log();
        for (int i = 0; i < 500; i++) {
            log.put("same", b("v" + i));
        }
        log.flush().get(10, TimeUnit.SECONDS);
        assertTrue(log.shouldCompact(1));
        final long big = Files.size(file);
        log.compact(() -> Map.of("same", b("v499"))).get(10, TimeUnit.SECONDS);
        assertTrue(Files.size(file) < big / 50);
        assertFalse(log.shouldCompact(1));
        log.put("other", b("o"));
        log.close();
        assertEquals(Map.of("same", "v499", "other", "o"), toStrings(WaterRecordLog.read(file).entries()));
        assertFalse(Files.exists(file.resolveSibling("usercache.wdb.tmp")));
    }

    @Test
    void failedCompactionKeepsJournalWritable() throws Exception {
        final Path file = this.dir.resolve("ops.wdb");
        final WaterRecordLog log = WaterRecordLog.open(file, true).log();
        log.put("a", b("1"));
        log.flush().get(10, TimeUnit.SECONDS);
        // Второй дескриптор на том же файле: на Windows атомарная замена при компакции завершится отказом.
        try (FileChannel other = FileChannel.open(file, StandardOpenOption.READ)) {
            try {
                log.compact(() -> Map.of("a", b("1"))).get(10, TimeUnit.SECONDS);
            } catch (final java.util.concurrent.ExecutionException expectedOnWindows) {
                assertInstanceOf(IOException.class, expectedOnWindows.getCause());
            }
            log.put("b", b("2"));
            log.flush().get(10, TimeUnit.SECONDS);
        }
        log.close();
        assertEquals(Map.of("a", "1", "b", "2"), toStrings(WaterRecordLog.read(file).entries()));
        assertFalse(Files.exists(file.resolveSibling("ops.wdb.tmp")));
    }

    @Test
    void rejectsForeignFiles() throws IOException {
        final Path file = this.dir.resolve("ops.wdb");
        Files.writeString(file, "[{\"uuid\":\"x\"}]");
        assertThrows(IOException.class, () -> WaterRecordLog.read(file));
    }

    private static Map<String, String> toStrings(final Map<String, byte[]> entries) {
        final Map<String, String> out = new LinkedHashMap<>();
        entries.forEach((k, v) -> out.put(k, s(v)));
        return out;
    }
}
