package io.papermc.paper.pluginremap;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import me.s0nd3r.watermc.pluginremap.JarHashCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

/** Плагины и библиотеки, пришедшие не из каталога plugins/: неиспользованные за запуск записи удаляются при остановке. */
@DefaultQualifier(NonNull.class)
final class UnknownOriginRemappedPluginIndex extends RemappedPluginIndex {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Set<String> used = ConcurrentHashMap.newKeySet();

    UnknownOriginRemappedPluginIndex(final Path dir, final JarHashCache hashes) {
        super(dir, true, hashes);
    }

    @Override
    @Nullable Path getIfPresent(final Path in) {
        final String hash = this.hashes.sha256(in);
        if (this.state.skippedHashes.contains(hash)) {
            return in;
        }

        final @Nullable Path path = super.getIfPresent(hash);
        if (path != null) {
            this.used.add(hash);
        }
        return path;
    }

    @Override
    Path input(final Path in) {
        final String hash = this.hashes.sha256(in);
        this.used.add(hash);
        return super.input(in, hash);
    }

    void write(final boolean clean) {
        if (clean) {
            final Iterator<Map.Entry<String, String>> it = this.state.hashes.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<String, String> next = it.next();
                if (this.used.contains(next.getKey())) {
                    continue;
                }
                it.remove();
                this.markDirty();
                final Path file = this.dir().resolve(next.getValue());
                try {
                    Files.deleteIfExists(file);
                } catch (final IOException ex) {
                    LOGGER.warn("Failed to delete no longer needed cached jar '{}'", file, ex);
                }
            }
        }
        super.write();
    }
}
