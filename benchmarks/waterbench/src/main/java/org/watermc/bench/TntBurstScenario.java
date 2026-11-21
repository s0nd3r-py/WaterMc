package org.watermc.bench;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.Plugin;

/**
 * Worst case for clients: N primed TNT with the same fuse, so all of them explode in the same tick
 * (TNT cannons, mass ignition). Each round runs on untouched terrain.
 */
final class TntBurstScenario implements Scenario {

    private static final int GROUND_Y = -60;
    private static final int ROUND_SPACING = 512;
    private static final int FUSE = 80;
    private static final int ROUND_TICKS = FUSE + 100;

    private final Plugin plugin;
    private final int rounds;
    private final int count;
    private World world;
    private int round;
    private int tick;

    TntBurstScenario(Plugin plugin, int rounds, int count) {
        this.plugin = plugin;
        this.rounds = rounds;
        this.count = count;
    }

    @Override
    public String name() {
        return "tntburst";
    }

    private static int originX(int round) {
        return -16384 + round * ROUND_SPACING;
    }

    @Override
    public void prepare() {
        this.world = BenchWorld.get();
        for (int r = 0; r < this.rounds; r++) {
            BenchWorld.loadArea(this.plugin, this.world, originX(r), 0, 5);
        }
        this.round = 0;
        this.tick = -1;
    }

    @Override
    public TickResult tick() {
        if (this.round >= this.rounds) return TickResult.FINISHED;
        if (this.tick == -1) {
            // cube of primed TNT, one per block, all with the same fuse
            int side = (int) Math.ceil(Math.cbrt(this.count));
            int spawned = 0;
            BenchWorld.moveSpectators(this.world, originX(this.round), GROUND_Y + side, 0);
            for (int y = 0; y < side && spawned < this.count; y++) {
                for (int x = 0; x < side && spawned < this.count; x++) {
                    for (int z = 0; z < side && spawned < this.count; z++) {
                        Location loc = new Location(this.world, originX(this.round) - side / 2.0 + x + 0.5, GROUND_Y + y, -side / 2.0 + z + 0.5);
                        this.world.spawn(loc, TNTPrimed.class, t -> {
                            t.setFuseTicks(FUSE);
                            t.setVelocity(new org.bukkit.util.Vector(0, 0, 0));
                        });
                        spawned++;
                    }
                }
            }
            this.tick = 0;
            return TickResult.SKIP;
        }
        this.tick++;
        if (this.tick > ROUND_TICKS) {
            for (Entity e : this.world.getEntities()) {
                if (e instanceof Item || e instanceof FallingBlock || e instanceof TNTPrimed) e.remove();
            }
            this.round++;
            this.tick = -1;
            return TickResult.SKIP;
        }
        return TickResult.COUNT;
    }

    @Override
    public void cleanup() {
        if (this.world != null) {
            for (Entity e : this.world.getEntities()) {
                if (e instanceof Item || e instanceof FallingBlock || e instanceof TNTPrimed) e.remove();
            }
            this.world.removePluginChunkTickets(this.plugin);
        }
    }
}
