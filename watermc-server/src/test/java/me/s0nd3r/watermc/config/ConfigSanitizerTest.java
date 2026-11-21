package me.s0nd3r.watermc.config;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import me.s0nd3r.watermc.ai.PoiSearchBackoff;

import static org.junit.jupiter.api.Assertions.*;

class ConfigSanitizerTest {

    @Test
    void findsOnlyTopLevelDeadKeys() {
        final List<String> fileKeys = List.of(
            "config-version",
            "performance", "performance.optimise-random-tick", "performance.optimize-random-tick",
            "async", "async.async-pathfinding", "async.async-pathfinding.enabled", "async.async-pathfinding.max-threads",
            "async.old-feature", "async.old-feature.enabled", "async.old-feature.ratio",
            "misc.region-format", "misc.region-format.custom", "misc.region-format.custom.anything",
            "removed-section", "removed-section.child");
        final Set<String> registered = Set.of("performance", "async", "performance.optimize-random-tick",
            "async.async-pathfinding.enabled", "async.async-pathfinding.max-threads", "misc.region-format");
        final Set<String> lenient = Set.of("misc.region-format");

        assertEquals(List.of("async.old-feature", "performance.optimise-random-tick", "removed-section"),
            ConfigSanitizer.findDeadKeys(fileKeys, registered, lenient));
    }

    @Test
    void nothingIsRemovedWhenEverythingIsRegistered() {
        assertTrue(ConfigSanitizer.findDeadKeys(List.of("a", "a.b"), Set.of("a.b"), Set.of()).isEmpty());
    }

    @Test
    void reportsConflictingProxyAndKeepaliveSettings() {
        final YamlConfiguration spigot = new YamlConfiguration();
        spigot.set("settings.bungeecord", true);
        final YamlConfiguration paper = new YamlConfiguration();
        paper.set("proxies.velocity.enabled", true);
        final YamlConfiguration purpur = new YamlConfiguration();
        purpur.set("settings.use-alternate-keepalive", true);
        final YamlConfiguration water = new YamlConfiguration();
        water.set("server-tuning.misc.keepalive.send-multiple", true);

        final List<String> conflicts = ConfigSanitizer.findConflicts(Map.of("spigot.yml", spigot, "paper-global.yml", paper, "purpur.yml", purpur, "watermc.yml", water));
        assertEquals(3, conflicts.size(), conflicts.toString());

        final YamlConfiguration bukkit = new YamlConfiguration();
        bukkit.set("settings.connection-throttle", -1);
        assertTrue(ConfigSanitizer.findConflicts(Map.of("bukkit.yml", bukkit)).isEmpty());
    }

    @Test
    void poiBackoffGrowsOnlyWhileTheWorldIsUnchanged() {
        final PoiSearchBackoff backoff = new PoiSearchBackoff();
        final long v = PoiSearchBackoff.version();
        assertEquals(0, backoff.onSearchFinished(false, 100, v), "first failure keeps vanilla rhythm");
        assertEquals(20, backoff.onSearchFinished(false, 100, v));
        assertEquals(40, backoff.onSearchFinished(false, 100, v));
        assertEquals(80, backoff.onSearchFinished(false, 100, v));
        assertEquals(100, backoff.onSearchFinished(false, 100, v), "capped");
        assertEquals(0, backoff.onSearchFinished(false, 100, v + 1), "a POI change resets the back-off");
        assertEquals(20, backoff.onSearchFinished(false, 100, v + 1));
        assertEquals(0, backoff.onSearchFinished(true, 100, v + 1), "success resets");
        assertEquals(0, backoff.onSearchFinished(false, 0, v + 1), "disabled");

        final PoiSearchBackoff tracked = new PoiSearchBackoff();
        tracked.onSearchFinished(false, 100);
        assertFalse(tracked.worldChangedSinceFailure());
        PoiSearchBackoff.bump();
        assertTrue(tracked.worldChangedSinceFailure());
    }
}
