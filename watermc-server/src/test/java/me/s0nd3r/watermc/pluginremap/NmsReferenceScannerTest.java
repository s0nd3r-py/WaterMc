package me.s0nd3r.watermc.pluginremap;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class NmsReferenceScannerTest {

    @TempDir
    Path dir;

    private Path jar(final String name, final Map<String, byte[]> entries) throws IOException {
        final Path path = this.dir.resolve(name);
        try (OutputStream out = Files.newOutputStream(path); ZipOutputStream zip = new ZipOutputStream(out)) {
            for (final Map.Entry<String, byte[]> e : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(e.getKey()));
                zip.write(e.getValue());
                zip.closeEntry();
            }
        }
        return path;
    }

    private static byte[] classWith(final String constant) {
        // упрощённый «класс»: магия + произвольные байты + константа где-то посередине
        final byte[] head = {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, 0, 0, 0, 65};
        final byte[] body = ("Lorg/bukkit/entity/Player;" + constant + "java/lang/Object").getBytes(StandardCharsets.US_ASCII);
        final byte[] out = new byte[head.length + body.length];
        System.arraycopy(head, 0, out, 0, head.length);
        System.arraycopy(body, 0, out, head.length, body.length);
        return out;
    }

    @Test
    void apiOnlyJarIsSkipped() throws IOException {
        final Path jar = this.jar("api.jar", Map.of(
            "plugin.yml", "name: X\nmain: a.B\n".getBytes(StandardCharsets.UTF_8),
            "a/B.class", classWith("Lorg/bukkit/plugin/java/JavaPlugin;"),
            // текст net/minecraft/ вне .class не считается
            "config.yml", "path: net/minecraft/".getBytes(StandardCharsets.UTF_8)));
        assertFalse(NmsReferenceScanner.referencesServerInternals(jar));
    }

    @Test
    void anyServerReferenceRequiresRemapping() throws IOException {
        for (final String marker : new String[]{"Lnet/minecraft/world/entity/Entity;", "com/mojang/brigadier/Command",
            "org/bukkit/craftbukkit/entity/CraftPlayer"}) {
            final Path jar = this.jar("nms-" + Math.abs(marker.hashCode()) + ".jar", Map.of(
                "a/Clean.class", classWith(""),
                "a/Nms.class", classWith(marker)));
            assertTrue(NmsReferenceScanner.referencesServerInternals(jar), marker);
        }
    }

    @Test
    void unreadableJarIsRemappedAsBefore() throws IOException {
        final Path broken = Files.writeString(this.dir.resolve("broken.jar"), "not a zip");
        assertTrue(NmsReferenceScanner.referencesServerInternals(broken));
    }

    @Test
    void markerAtTheVeryEndAndLargeClassesAreFound() throws IOException {
        final byte[] big = new byte[300_000];
        final byte[] marker = "net/minecraft/".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(marker, 0, big, big.length - marker.length, marker.length);
        assertTrue(NmsReferenceScanner.referencesServerInternals(this.jar("big.jar", Map.of("a/Big.class", big))));
        assertFalse(NmsReferenceScanner.containsMarker("net/minecraf".getBytes(StandardCharsets.US_ASCII), 12));
    }
}
