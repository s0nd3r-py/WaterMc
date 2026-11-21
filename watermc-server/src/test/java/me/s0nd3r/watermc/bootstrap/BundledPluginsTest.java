package me.s0nd3r.watermc.bootstrap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import me.s0nd3r.watermc.core.config.modules.network.MultiVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/** Встроенный ViaVersion: свой jar администратора важнее, в конфиге Via меняются только наши ключи. */
class BundledPluginsTest {

    @Test
    void onlyManagedKeysChangeInViaConfig() {
        String original = """
            # comment kept
            check-for-updates: false
            block-versions:
              - '<1.8'
              - '1.9'
            block-disconnect-msg: 'old'
            max-pps: 800
            """;
        String out = BundledPlugins.setTopLevelKey(original, "block-versions", "\n  - '<1.20.5'");
        out = BundledPlugins.setTopLevelKey(out, "block-disconnect-msg", " 'new'");
        assertTrue(out.contains("# comment kept"));
        assertTrue(out.contains("check-for-updates: false"));
        assertTrue(out.contains("max-pps: 800"));
        assertTrue(out.contains("block-versions:\n  - '<1.20.5'\n"));
        assertFalse(out.contains("'<1.8'"));
        assertTrue(out.contains("block-disconnect-msg: 'new'"));
        assertEquals("a: 1\nblock-versions: []\n", BundledPlugins.setTopLevelKey("a: 1", "block-versions", "[]"));
    }

    @Test
    void adminCopyWinsAndDisabledLoadsNothing(@TempDir Path dir) {
        boolean old = MultiVersion.enabled;
        try {
            MultiVersion.enabled = false;
            assertTrue(BundledPlugins.resolve(Set.of(), dir.resolve("plugins"), dir.resolve("cache")).isEmpty());
            MultiVersion.enabled = true;
            assertTrue(BundledPlugins.resolve(Set.of("ViaVersion"), dir.resolve("plugins"), dir.resolve("cache")).isEmpty());
            assertFalse(Files.exists(dir.resolve("plugins/ViaVersion/config.yml")), "the admin's Via config is not touched");
        } finally {
            MultiVersion.enabled = old;
        }
    }

    @Test
    void versionValidation() {
        assertTrue(BundledPlugins.validVersion(""));
        assertTrue(BundledPlugins.validVersion("1.20.5"));
        assertTrue(BundledPlugins.validVersion("1.21"));
        assertFalse(BundledPlugins.validVersion("<1.20"));
        assertFalse(BundledPlugins.validVersion("latest"));
    }
}
