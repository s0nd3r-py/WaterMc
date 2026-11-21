package me.s0nd3r.watermc.core.config.modules.network;

import java.util.ArrayList;
import java.util.List;
import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

/** Raytrace entity culling, ported from DivineMC ver/26.3 (BX-Team / DivineMC, GPL-3.0). Off by default. */
public class RaytraceEntityCulling extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.NETWORK.getBaseKeyName() + ".raytrace-entity-culling";
    }

    public static boolean enabled = false;
    public static int threads = 0;
    public static int checkIntervalMs = 50;
    public static int maxTraceDistance = 64;
    public static double forceVisibleRadius = 4.0D;
    public static long visibilityTimeoutMs = 1000L;
    public static double aabbExpansion = 0.5D;
    public static int boundingBoxLimit = 20;
    public static boolean cullPlayers = true;
    public static boolean skipMarkerArmorStands = true;
    public static List<String> skippedEntityTypes = new ArrayList<>();

    @Override
    public void onLoaded() {
        final String base = getBasePath();
        enabled = config.getBoolean(base + ".enabled", enabled, config.pickStringRegionBased("""
            Stop sending entities that a player provably cannot see (fully hidden behind opaque blocks).
            Saves bandwidth and client FPS and hides mobs, players and items from ESP/x-ray style cheats.
            Runs on its own threads; any failure keeps the entity visible. Each pass checks the entities of
            the player's world, so the cost grows with players x entities: measure before enabling on large
            servers. Hidden players also disappear from the locator bar.""", """
            不发送玩家完全看不到的实体 (被不透明方块完全遮挡)."""));
        threads = config.getInt(base + ".threads", threads, "Threads; 0 = processors / 8, between 1 and 4.");
        checkIntervalMs = Math.max(10, config.getInt(base + ".check-interval-ms", checkIntervalMs, "Milliseconds between passes for one player."));
        maxTraceDistance = Math.clamp(config.getInt(base + ".max-trace-distance", maxTraceDistance, "Entities farther than this (blocks) are not culled. 16-160."), 16, 160);
        forceVisibleRadius = config.getDouble(base + ".force-visible-radius", forceVisibleRadius, "Entities closer than this are always visible.");
        visibilityTimeoutMs = config.getLong(base + ".visibility-timeout-ms", visibilityTimeoutMs, "An entity proven visible stays visible this long (no flicker).");
        aabbExpansion = config.getDouble(base + ".aabb-expansion", aabbExpansion, "Box expansion for visibility tests (safety margin).");
        boundingBoxLimit = config.getInt(base + ".bounding-box-limit", boundingBoxLimit, "Entities with a larger box are never culled.");
        cullPlayers = config.getBoolean(base + ".cull-players", cullPlayers, "Also hide players behind walls.");
        skipMarkerArmorStands = config.getBoolean(base + ".skip-marker-armor-stands", skipMarkerArmorStands, "Never hide invisible armor stands (holograms).");
        skippedEntityTypes = config.getList(base + ".skipped-entity-types", skippedEntityTypes, "Entity types that are never culled, e.g. minecraft:item_display.");
    }

    @Override
    public void onPostLoaded() { // registries exist only after bootstrap, onLoaded runs before it
        final String base = getBasePath();
        for (final EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            type.waterCullingSkip = false;
        }
        for (final String name : skippedEntityTypes) {
            final String id = name.contains(":") ? name : "minecraft:" + name;
            EntityType.byString(id).ifPresentOrElse(type -> type.waterCullingSkip = true,
                () -> me.s0nd3r.watermc.core.config.WaterConfig.LOGGER.warn("Unknown entity type {} in {}.skipped-entity-types", name, base));
        }
    }
}
