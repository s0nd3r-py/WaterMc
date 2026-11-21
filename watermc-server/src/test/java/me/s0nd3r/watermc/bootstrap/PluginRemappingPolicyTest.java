package me.s0nd3r.watermc.bootstrap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PluginRemappingPolicyTest {
    @Test void enabledByDefaultLikePaper() {
        String old = System.getProperty("watermc.enable-legacy-remapper"), disabled = System.getProperty("paper.disablePluginRemapping");
        try {
            System.clearProperty("watermc.enable-legacy-remapper"); System.clearProperty("paper.disablePluginRemapping");
            assertTrue(LegacyPluginRemapping.enabled());
        } finally { restore("watermc.enable-legacy-remapper", old); restore("paper.disablePluginRemapping", disabled); }
    }
    @Test void bothOptOutFlagsAreHonoured() {
        String old = System.getProperty("watermc.enable-legacy-remapper"), disabled = System.getProperty("paper.disablePluginRemapping");
        try {
            System.setProperty("watermc.enable-legacy-remapper", "false"); System.clearProperty("paper.disablePluginRemapping");
            assertFalse(LegacyPluginRemapping.enabled());
            System.clearProperty("watermc.enable-legacy-remapper"); System.setProperty("paper.disablePluginRemapping", "true");
            assertFalse(LegacyPluginRemapping.enabled());
        } finally { restore("watermc.enable-legacy-remapper", old); restore("paper.disablePluginRemapping", disabled); }
    }
    private static void restore(String key, String value) { if (value == null) System.clearProperty(key); else System.setProperty(key, value); }
}
