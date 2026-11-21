package me.s0nd3r.watermc.pluginremap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class JarHashCacheTest {

    @TempDir
    Path dir;

    private Path jar(final String name, final int size, final long seed) throws IOException {
        final byte[] bytes = new byte[size];
        new Random(seed).nextBytes(bytes);
        return Files.write(this.dir.resolve(name), bytes);
    }

    @Test
    void hashMatchesThePreviousFormat() throws IOException {
        final Path jar = this.jar("a.jar", 300_000, 1);
        assertEquals(io.papermc.paper.util.Hashing.sha256(jar), JarHashCache.load(this.dir.resolve("cache")).sha256(jar));
    }

    @Test
    void unchangedJarIsReadOncePerRunAndNotAtAllAfterRestart() throws IOException {
        final Path cacheDir = this.dir.resolve("cache");
        final Path a = this.jar("a.jar", 100_000, 1);
        final Path b = this.jar("b.jar", 100_000, 2);

        final JarHashCache first = JarHashCache.load(cacheDir);
        final String hashA = first.sha256(a);
        first.sha256(a);
        first.sha256(b);
        first.sha256(a);
        assertEquals(2, first.computedHashes(), "each jar is read once per run");
        first.save();

        final JarHashCache restarted = JarHashCache.load(cacheDir);
        assertEquals(hashA, restarted.sha256(a));
        restarted.sha256(b);
        assertEquals(0, restarted.computedHashes(), "unchanged jars are not read after a restart");
    }

    @Test
    void changedJarIsRehashed() throws IOException {
        final Path cacheDir = this.dir.resolve("cache");
        final Path a = this.jar("a.jar", 50_000, 1);
        final JarHashCache first = JarHashCache.load(cacheDir);
        final String before = first.sha256(a);
        first.save();

        // другое содержимое того же размера, время изменения сдвинуто
        this.jar("a.jar", 50_000, 99);
        Files.setLastModifiedTime(a, FileTime.fromMillis(Files.getLastModifiedTime(a).toMillis() + 5_000));
        final JarHashCache restarted = JarHashCache.load(cacheDir);
        final String after = restarted.sha256(a);
        assertNotEquals(before, after);
        assertEquals(io.papermc.paper.util.Hashing.sha256(a), after);
        assertEquals(1, restarted.computedHashes());
    }

    @Test
    void removedJarsArePrunedAndCorruptCacheIsIgnored() throws IOException {
        final Path cacheDir = this.dir.resolve("cache");
        final Path a = this.jar("a.jar", 1_000, 1);
        final JarHashCache cache = JarHashCache.load(cacheDir);
        cache.sha256(a);
        cache.save();
        Files.delete(a);
        cache.save();
        assertEquals("", Files.readString(cacheDir.resolve("hashes.tsv")));

        Files.writeString(cacheDir.resolve("hashes.tsv"), "garbage\tx\ty\tz\nonly-two\tcolumns\n");
        final Path b = this.jar("b.jar", 1_000, 2);
        assertEquals(io.papermc.paper.util.Hashing.sha256(b), JarHashCache.load(cacheDir).sha256(b));
    }
}
