package me.s0nd3r.watermc.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PortalSearchPrefetcherTest {

    @Test
    void prefetchRadiusIsBoundedAndFollowsSearchRadius() {
        assertEquals(4, PortalSearchPrefetcher.chunkRadius(128), "overworld default radius is capped at 4 chunks (9x9)");
        assertEquals(2, PortalSearchPrefetcher.chunkRadius(16), "nether radius scaled by 8");
        assertEquals(1, PortalSearchPrefetcher.chunkRadius(0));
        assertEquals(1, PortalSearchPrefetcher.chunkRadius(-5));
        assertEquals(4, PortalSearchPrefetcher.chunkRadius(Integer.MAX_VALUE));
    }

    @Test
    void disabledByDefault() {
        assertFalse(me.s0nd3r.watermc.core.config.modules.async.PortalSearchPrefetch.enabled);
    }
}
