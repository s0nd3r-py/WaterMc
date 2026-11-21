package me.s0nd3r.watermc.version;

import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.ServerBuildInfoImpl;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WaterBrandTest {
    @Test
    void advertisesInheritedBrandCompatibility() {
        ServerBuildInfo info = new ServerBuildInfoImpl(Key.key("watermc", "watermc"), "WaterMc",
            "1.21.11", "1.21.11", OptionalInt.empty(), Instant.EPOCH, Optional.empty(), Optional.empty());
        for (Key inherited : new Key[] {ServerBuildInfo.BRAND_PAPER_ID, ServerBuildInfo.BRAND_GALE_ID,
            ServerBuildInfo.BRAND_PUFFERFISH_ID, ServerBuildInfo.BRAND_PURPUR_ID, ServerBuildInfo.BRAND_LEAF_ID}) {
            assertTrue(info.isBrandCompatible(inherited), inherited.toString());
        }
        assertTrue(info.isBrandCompatible(info.brandId()));
        assertFalse(info.isBrandCompatible(Key.key("papermc", "folia")));
    }
}
