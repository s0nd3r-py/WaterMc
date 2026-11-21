package me.s0nd3r.watermc.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.s0nd3r.watermc.core.config.modules.network.MultiVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Built-in multi-version support: ViaVersion and ViaBackwards (both GPL-3.0, ViaVersion team) ship inside the
 * server jar and are loaded as ordinary plugins through Paper's {@code --add-plugin} mechanism, so plugins that
 * depend on them (and their API) work unchanged.
 *
 * <p>An admin's own ViaVersion in {@code plugins/} always wins: then neither bundled jar is loaded (a bundled
 * ViaBackwards must match the ViaVersion it runs on). The jars are extracted once per content hash into
 * {@code cache/bundled-plugins/}.</p>
 */
public final class BundledPlugins {
    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc");
    static final String RESOURCE_DIR = "watermc/bundled-plugins/";
    static final List<String> VIA_JARS = List.of("ViaVersion", "ViaBackwards");

    private BundledPlugins() {
    }

    /** Jars to register after the plugins directory was scanned; empty when disabled or replaced by the admin's own copy. */
    public static List<Path> resolve(final Set<String> registeredPluginNames, final Path pluginsDir, final Path cacheDir) {
        if (!MultiVersion.enabled) {
            return List.of();
        }
        if (registeredPluginNames.contains("ViaVersion")) {
            LOGGER.info("Multi-version: using ViaVersion from {}; the built-in copy is not loaded", pluginsDir);
            return List.of();
        }
        final List<Path> jars = new ArrayList<>();
        for (final String name : VIA_JARS) {
            if (registeredPluginNames.contains(name)) {
                LOGGER.warn("Multi-version: {} is in {} but ViaVersion is not; the built-in {} is skipped", name, pluginsDir, name);
                continue;
            }
            try {
                final Path jar = extract(name, cacheDir);
                if (jar != null) {
                    jars.add(jar);
                }
            } catch (final IOException ex) {
                LOGGER.error("Multi-version: could not extract built-in {}", name, ex);
            }
        }
        if (!jars.isEmpty()) {
            try {
                applyViaConfig(pluginsDir.resolve("ViaVersion").resolve("config.yml"));
            } catch (final IOException ex) {
                LOGGER.error("Multi-version: could not write ViaVersion settings", ex);
            }
            LOGGER.info("Multi-version: loading built-in {}", jars.stream().map(p -> p.getFileName().toString()).toList());
        }
        return jars;
    }

    static Path extract(final String name, final Path cacheDir) throws IOException {
        final byte[] bytes;
        try (InputStream in = BundledPlugins.class.getClassLoader().getResourceAsStream(RESOURCE_DIR + name + ".jar")) {
            if (in == null) {
                LOGGER.error("Multi-version: {} is missing from this build", name);
                return null;
            }
            bytes = in.readAllBytes();
        }
        final String hash = sha256(bytes).substring(0, 16);
        final Path target = cacheDir.resolve(name + "-" + hash + ".jar");
        if (!Files.isRegularFile(target) || Files.size(target) != bytes.length) {
            Files.createDirectories(cacheDir);
            final Path temp = Files.createTempFile(cacheDir, name, ".tmp");
            Files.write(temp, bytes);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            try (var old = Files.list(cacheDir)) {
                for (final Path stale : old.filter(p -> p.getFileName().toString().startsWith(name + "-") && !p.equals(target)).toList()) {
                    Files.deleteIfExists(stale);
                }
            }
        }
        return target;
    }

    static String sha256(final byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (final java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Writes only the keys WaterMc manages ({@code block-versions}, {@code block-disconnect-msg}) into ViaVersion's
     * config; every other line, comment and setting of the file stays as the admin left it.
     */
    static void applyViaConfig(final Path config) throws IOException {
        final String blockVersions = MultiVersion.minVersion.isBlank() ? "[]" : "\n  - '<" + MultiVersion.minVersion + "'";
        final String message = "'" + MultiVersion.blockedMessage.replace("'", "''") + "'";
        String text = Files.isRegularFile(config) ? Files.readString(config, StandardCharsets.UTF_8) : "";
        text = setTopLevelKey(text, "block-versions", blockVersions);
        text = setTopLevelKey(text, "block-disconnect-msg", " " + message);
        Files.createDirectories(config.getParent());
        Files.writeString(config, text, StandardCharsets.UTF_8);
    }

    /** Replaces a top-level YAML key together with its indented continuation lines, or appends it. */
    static String setTopLevelKey(final String text, final String key, final String valueWithSeparator) {
        final String value = valueWithSeparator.startsWith("\n") ? valueWithSeparator : (valueWithSeparator.startsWith(" ") ? valueWithSeparator : " " + valueWithSeparator);
        final Pattern pattern = Pattern.compile("(?m)^" + Pattern.quote(key) + ":.*(?:\\R(?:[ \\t]+.*|[ \\t]*-.*))*");
        final Matcher matcher = pattern.matcher(text);
        final String replacement = key + ":" + value;
        if (matcher.find()) {
            return text.substring(0, matcher.start()) + replacement + text.substring(matcher.end());
        }
        return (text.isEmpty() || text.endsWith("\n") ? text : text + "\n") + replacement + "\n";
    }

    /** Version text like 1.20.5 or 1.21; empty means every version ViaVersion supports. */
    public static boolean validVersion(final String version) {
        return version.isBlank() || version.matches("1\\.\\d{1,2}(\\.\\d{1,2})?");
    }
}
