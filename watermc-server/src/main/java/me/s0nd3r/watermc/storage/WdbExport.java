package me.s0nd3r.watermc.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Read-only export of {@code .wdb} journals back to readable JSON.
 *
 * <p>The five server lists are written in their vanilla shape (a JSON array of entries), so an export can be
 * inspected, diffed or handed to external tools that expect {@code ops.json} and friends. Other journals are
 * written as an object {@code key → value}. The journal itself is only read: a record that is still being
 * appended is ignored, exactly as on server start.</p>
 */
public final class WdbExport {
    /** Journals whose vanilla JSON form is an array of entries. */
    public static final Set<String> LIST_FILES = Set.of("ops", "whitelist", "banned-players", "banned-ips", "usercache");
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT);

    public record Result(Path output, int entries, boolean truncatedTail) {}

    public record Info(String name, long bytes, int entries, long records) {}

    private WdbExport() {}

    public static String baseName(Path wdb) {
        String file = wdb.getFileName().toString();
        return file.endsWith(".wdb") ? file.substring(0, file.length() - 4) : file;
    }

    public static List<Path> journals(Path dataDir) throws IOException {
        if (!Files.isDirectory(dataDir)) return List.of();
        try (Stream<Path> files = Files.list(dataDir)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".wdb") && Files.isRegularFile(p)).sorted().toList();
        }
    }

    public static Info info(Path wdb) throws IOException {
        WaterRecordLog.Contents contents = WaterRecordLog.read(wdb);
        return new Info(baseName(wdb), Files.size(wdb), contents.entries().size(), contents.records());
    }

    static JsonElement value(byte[] payload) {
        String text = new String(payload, StandardCharsets.UTF_8);
        try {
            return JsonParser.parseString(text);
        } catch (JsonParseException notJson) {
            return new JsonPrimitive(text);
        }
    }

    /** Builds the export document for the given journal contents. */
    public static JsonElement document(String name, Map<String, byte[]> entries) {
        if (LIST_FILES.contains(name)) {
            JsonArray array = new JsonArray(entries.size());
            for (byte[] payload : entries.values()) array.add(value(payload));
            return array;
        }
        JsonObject object = new JsonObject();
        for (Map.Entry<String, byte[]> entry : entries.entrySet()) object.add(entry.getKey(), value(entry.getValue()));
        return object;
    }

    /** Writes {@code <outDir>/<name>-<timestamp>.json} through a temporary file and an atomic move. */
    public static Result export(Path wdb, Path outDir, LocalDateTime now) throws IOException {
        WaterRecordLog.Contents contents = WaterRecordLog.read(wdb);
        String name = baseName(wdb);
        Files.createDirectories(outDir);
        Path output = outDir.resolve(name + "-" + STAMP.format(now) + ".json");
        Path temp = outDir.resolve(output.getFileName() + ".tmp");
        Files.writeString(temp, PRETTY.toJson(document(name, contents.entries())) + "\n", StandardCharsets.UTF_8);
        Files.move(temp, output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        return new Result(output, contents.entries().size(), contents.truncatedTail());
    }

    public static List<Result> exportAll(Path dataDir, Path outDir, LocalDateTime now) throws IOException {
        List<Result> results = new ArrayList<>();
        for (Path wdb : journals(dataDir)) results.add(export(wdb, outDir, now));
        return results;
    }
}
