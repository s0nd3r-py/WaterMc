package io.papermc.paper.pluginremap;

import com.mojang.logging.LogUtils;
import io.papermc.paper.util.AtomicFiles;
import io.papermc.paper.util.MappingEnvironment;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.neoforged.art.api.Renamer;
import net.neoforged.art.api.Transformer;
import net.neoforged.art.internal.RenamerImpl;
import net.neoforged.srgutils.IMappingFile;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;

import static io.papermc.paper.pluginremap.InsertManifestAttribute.addNamespaceManifestAttribute;

/**
 * Сервер в Spigot-именах: classpath для переотображения плагинов.
 *
 * <p>WaterMc: создаётся лениво, при первом плагине, которому нужно переотображение. Раньше при новой версии
 * сервера jar переотображался на каждом запуске с включённым ремаппером, даже если все плагины уже в
 * Mojang-именах.</p>
 */
@DefaultQualifier(NonNull.class)
final class ReobfServer {
    private static final Logger LOGGER = LogUtils.getClassLogger();

    private final Path remapClasspathDir;
    private final Supplier<CompletableFuture<IMappingFile>> mappings;
    private final Executor executor;
    private @Nullable CompletableFuture<Void> load;

    ReobfServer(final Path remapClasspathDir, final Supplier<CompletableFuture<IMappingFile>> mappings, final Executor executor) {
        this.remapClasspathDir = remapClasspathDir;
        this.mappings = mappings;
        this.executor = executor;
    }

    synchronized CompletableFuture<Path> remapped() {
        if (this.load == null) {
            if (Files.exists(this.remappedPath())) {
                if (PluginRemapper.DEBUG_LOGGING) {
                    LOGGER.info("Have cached reobf server for current mappings.");
                }
                this.load = CompletableFuture.completedFuture(null);
            } else {
                this.load = this.mappings.get().thenAcceptAsync(this::remap, this.executor);
            }
        }
        return this.load.thenApply($ -> this.remappedPath());
    }

    private Path remappedPath() {
        return this.remapClasspathDir.resolve(MappingEnvironment.mappingsHash() + ".jar");
    }

    private void remap(final IMappingFile mappings) {
        try {
            Files.createDirectories(this.remapClasspathDir);
            for (final Path file : PluginRemapper.list(this.remapClasspathDir, Files::isRegularFile)) {
                Files.delete(file);
            }
        } catch (final IOException ex) {
            throw new RuntimeException(ex);
        }

        LOGGER.info("Preparing the Spigot-mapped server classpath for plugin remapping (once per server version)...");
        final long startRemap = System.currentTimeMillis();
        try (final DebugLogger log = DebugLogger.forOutputFile(this.remappedPath())) {
            AtomicFiles.atomicWrite(this.remappedPath(), writeTo -> {
                try (final RenamerImpl renamer = (RenamerImpl) Renamer.builder()
                    .logger(log)
                    .debug(log.debug())
                    .threads(Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() - 1)))
                    .add(Transformer.renamerFactory(mappings, false))
                    .add(addNamespaceManifestAttribute(InsertManifestAttribute.SPIGOT_NAMESPACE))
                    .build()) {
                    renamer.run(serverJar().toFile(), writeTo.toFile(), true);
                }
            });
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to remap server jar", ex);
        }
        LOGGER.info("Server classpath for plugin remapping ready in {} ms", System.currentTimeMillis() - startRemap);
    }

    private static Path serverJar() {
        try {
            return Path.of(ReobfServer.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (final URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }
}
