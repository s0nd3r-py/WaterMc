package org.watermc.bench;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Deterministic flat world shared by all scenarios.
 */
final class BenchWorld {

    static final String NAME = "waterbench_flat";

    private BenchWorld() {}

    @SuppressWarnings("removal") // old game rule constants still work and exist on both Leaf and Water
    static World get() {
        World world = Bukkit.getWorld(NAME);
        if (world != null) return world;
        world = new WorldCreator(NAME)
            .type(WorldType.FLAT)
            .generateStructures(false)
            .seed(20261108L)
            .createWorld();
        if (world == null) throw new IllegalStateException("Could not create benchmark world");
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setTime(18000L); // midnight, so undead mobs do not burn
        world.setStorm(false);
        world.setAutoSave(false);
        return world;
    }

    static void loadArea(Plugin plugin, World world, int blockX, int blockZ, int chunkRadius) {
        int cx = blockX >> 4, cz = blockZ >> 4;
        for (int x = cx - chunkRadius; x <= cx + chunkRadius; x++) {
            for (int z = cz - chunkRadius; z <= cz + chunkRadius; z++) {
                world.getChunkAt(x, z); // synchronous load is fine here: runs during an uncounted tick
                world.addPluginChunkTicket(x, z, plugin);
            }
        }
    }

    /** Moves every online player (benchmark bots) to watch a point from a fixed offset. */
    static void moveSpectators(World world, double x, double y, double z) {
        int i = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            double angle = (Math.PI * 2 * i++) / Math.max(1, Bukkit.getOnlinePlayers().size());
            Location loc = new Location(world, x + Math.cos(angle) * 24, y + 20, z + Math.sin(angle) * 24);
            loc.setDirection(new Location(world, x, y, z).toVector().subtract(loc.toVector()));
            player.setGameMode(GameMode.CREATIVE);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.teleport(loc);
        }
    }
}
