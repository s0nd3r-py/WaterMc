package io.papermc.paper.pluginremap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import io.papermc.paper.util.MappingEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.s0nd3r.watermc.pluginremap.JarHashCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.spongepowered.configurate.loader.AtomicFiles;

/**
 * Индекс кэша переотображённых плагинов: SHA-256 исходного jar → имя переотображённого файла в каталоге.
 *
 * <p>WaterMc: хэши берутся из общего {@link JarHashCache} (неизменённый jar не перечитывается между запусками
 * и хэшируется не больше одного раза за запуск); очистка устаревших записей — за один проход по множеству
 * хэшей вместо перебора «каждая запись × каждый плагин»; индекс пишется на диск только при изменениях.
 * Формат {@code index.json} прежний.</p>
 */
@DefaultQualifier(NonNull.class)
class RemappedPluginIndex {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private static final String INDEX_FILE_NAME = "index.json";

    protected final State state;
    protected final JarHashCache hashes;
    private final Path dir;
    private final Path indexFile;
    private final boolean handleDuplicateFileNames;
    private boolean dirty;

    static final class State {
        final Map<String, String> hashes = new HashMap<>();
        final Set<String> skippedHashes = new HashSet<>();
        private final String mappingsHash = MappingEnvironment.mappingsHash();
    }

    RemappedPluginIndex(final Path dir, final boolean handleDuplicateFileNames, final JarHashCache hashes) {
        this.dir = dir;
        this.hashes = hashes;
        this.handleDuplicateFileNames = handleDuplicateFileNames;
        try {
            Files.createDirectories(this.dir);
        } catch (final IOException ex) {
            throw new UncheckedIOException(ex);
        }

        this.indexFile = dir.resolve(INDEX_FILE_NAME);
        if (Files.isRegularFile(this.indexFile)) {
            this.state = this.readIndex();
        } else {
            this.state = new State();
            this.dirty = true;
        }
    }

    private State readIndex() {
        final State state;
        try (final BufferedReader reader = Files.newBufferedReader(this.indexFile)) {
            state = GSON.fromJson(reader, State.class);
        } catch (final Exception ex) {
            // Повреждённый индекс — это только кэш: начинаем заново, а не падаем при запуске.
            LOGGER.warn("Plugin remapping index '{}' is unreadable; the cache will be rebuilt", this.indexFile, ex);
            this.dirty = true;
            return new State();
        }

        // Маппинги изменились (новая версия сервера): весь кэш недействителен.
        if (state == null || !MappingEnvironment.mappingsHash().equals(state.mappingsHash)) {
            if (state != null) {
                for (final String fileName : state.hashes.values()) {
                    this.deleteCached(fileName);
                }
            }
            this.dirty = true;
            return new State();
        }
        return state;
    }

    Path dir() {
        return this.dir;
    }

    /**
     * Returns a list of cached paths if all of the input paths are present in the cache.
     * The returned list may contain paths from different directories.
     *
     * @param paths plugin jar paths to check
     * @return null if any of the paths are not present in the cache, otherwise a list of the cached paths
     */
    @Nullable List<Path> getAllIfPresent(final List<Path> paths) {
        final List<String> inputHashes = new ArrayList<>(paths.size());
        for (final Path path : paths) {
            inputHashes.add(this.hashes.sha256(path));
        }
        final Set<String> used = new HashSet<>(inputHashes);

        // Удалить кэш плагинов, которых больше нет
        if (this.state.hashes.keySet().removeIf(hash -> {
            if (used.contains(hash)) {
                return false;
            }
            this.deleteCached(this.state.hashes.get(hash));
            return true;
        })) {
            this.dirty = true;
        }
        if (this.state.skippedHashes.retainAll(used)) {
            this.dirty = true;
        }

        final List<Path> ret = new ArrayList<>(paths.size());
        for (int i = 0; i < paths.size(); i++) {
            final String inputHash = inputHashes.get(i);
            if (this.state.skippedHashes.contains(inputHash)) {
                ret.add(paths.get(i));
                continue;
            }
            final @Nullable Path cached = this.getIfPresent(inputHash);
            if (cached == null) {
                return null;
            }
            ret.add(cached);
        }
        return ret;
    }

    private void deleteCached(final @Nullable String fileName) {
        if (fileName == null) {
            return;
        }
        final Path filePath = this.dir.resolve(fileName);
        try {
            Files.deleteIfExists(filePath);
        } catch (final IOException ex) {
            LOGGER.warn("Failed to delete no longer needed file '{}'", filePath, ex);
        }
    }

    private String createCachedFileName(final Path in) {
        if (this.handleDuplicateFileNames) {
            final String fileName = in.getFileName().toString();
            final int i = fileName.lastIndexOf(".jar");
            return (i < 0 ? fileName : fileName.substring(0, i)) + "-" + System.currentTimeMillis() + ".jar";
        }
        return in.getFileName().toString();
    }

    /**
     * Returns the given path if the file was previously skipped for being remapped, otherwise the cached path or null.
     */
    @Nullable Path getIfPresent(final Path in) {
        final String inHash = this.hashes.sha256(in);
        if (this.state.skippedHashes.contains(inHash)) {
            return in;
        }
        return this.getIfPresent(inHash);
    }

    /** Returns the cached path if a remapped file is present for the given hash, otherwise null. */
    protected @Nullable Path getIfPresent(final String inHash) {
        final @Nullable String fileName = this.state.hashes.get(inHash);
        if (fileName == null) {
            return null;
        }
        final Path path = this.dir.resolve(fileName);
        return Files.exists(path) ? path : null;
    }

    Path input(final Path in) {
        return this.input(in, this.hashes.sha256(in));
    }

    /** Marks the given file as skipped for remapping (already Mojang-mapped or a Paper plugin). */
    void skip(final Path in) {
        if (this.state.skippedHashes.add(this.hashes.sha256(in))) {
            this.dirty = true;
        }
    }

    protected Path input(final Path in, final String hashString) {
        final String name = this.createCachedFileName(in);
        this.state.hashes.put(hashString, name);
        this.dirty = true;
        return this.dir.resolve(name);
    }

    protected void markDirty() {
        this.dirty = true;
    }

    void write() {
        if (!this.dirty) {
            return;
        }
        try (final BufferedWriter writer = AtomicFiles.atomicBufferedWriter(this.indexFile, StandardCharsets.UTF_8)) {
            GSON.toJson(this.state, writer);
            this.dirty = false;
        } catch (final IOException ex) {
            LOGGER.warn("Failed to write index file '{}'", this.indexFile, ex);
        }
    }
}
