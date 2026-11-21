package org.watermc.bench;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.plugin.Plugin;

/**
 * Chain reaction of a solid TNT cube. Each round uses untouched terrain far away from the previous one,
 * so every round starts from the same state.
 */
final class TntScenario implements Scenario {

    private static final int GROUND_Y = -60; // first air block above the default flat preset
    private static final int ROUND_SPACING = 512;
    private static final int ROUND_TIMEOUT_TICKS = 20 * 60;
    private static final int SETTLE_TICKS = 40;

    private final Plugin plugin;
    private final int rounds;
    private final int side;
    private World world;

    private int round = -1;
    private int phaseTick;
    private Phase phase = Phase.BUILD;
    private int settle;

    private enum Phase { BUILD, IGNITE, RUN, CLEAN }

    TntScenario(Plugin plugin, int rounds, int side) {
        this.plugin = plugin;
        this.rounds = rounds;
        this.side = side;
    }

    @Override
    public String name() {
        return "tnt";
    }

    @Override
    public void prepare() {
        this.world = BenchWorld.get();
        for (int r = 0; r < this.rounds; r++) {
            BenchWorld.loadArea(this.plugin, this.world, originX(r), 0, 5);
        }
        this.round = 0;
        this.phase = Phase.BUILD;
    }

    private static int originX(int round) {
        return 4096 + round * ROUND_SPACING;
    }

    @Override
    public TickResult tick() {
        switch (this.phase) {
            case BUILD -> {
                if (this.round >= this.rounds) return TickResult.FINISHED;
                int ox = originX(this.round) - this.side / 2;
                int oz = -this.side / 2;
                for (int x = 0; x < this.side; x++) {
                    for (int y = 0; y < this.side; y++) {
                        for (int z = 0; z < this.side; z++) {
                            this.world.getBlockAt(ox + x, GROUND_Y + y, oz + z).setType(Material.TNT, false);
                        }
                    }
                }
                BenchWorld.moveSpectators(this.world, originX(this.round), GROUND_Y + this.side, 0);
                this.phase = Phase.IGNITE;
                this.phaseTick = 0;
                return TickResult.SKIP;
            }
            case IGNITE -> {
                // give clients a few ticks to receive the cube before it blows up
                if (++this.phaseTick < 40) return TickResult.SKIP;
                int half = this.side / 2;
                Location center = new Location(this.world, originX(this.round) + 0.5, GROUND_Y + half, 0.5);
                center.getBlock().setType(Material.AIR, false);
                this.world.spawn(center, TNTPrimed.class, tnt -> tnt.setFuseTicks(1));
                this.phase = Phase.RUN;
                this.phaseTick = 0;
                this.settle = 0;
                return TickResult.COUNT;
            }
            case RUN -> {
                this.phaseTick++;
                boolean anyTnt = !this.world.getEntitiesByClass(TNTPrimed.class).isEmpty();
                if (!anyTnt) this.settle++;
                if (this.settle > SETTLE_TICKS || this.phaseTick > ROUND_TIMEOUT_TICKS) {
                    this.phase = Phase.CLEAN;
                }
                return TickResult.COUNT;
            }
            case CLEAN -> {
                removeLeftovers();
                this.round++;
                this.phase = Phase.BUILD;
                return TickResult.SKIP;
            }
        }
        return TickResult.SKIP;
    }

    private void removeLeftovers() {
        for (Entity e : this.world.getEntities()) {
            if (e instanceof Item || e instanceof FallingBlock || e instanceof TNTPrimed) {
                e.remove();
            }
        }
    }

    @Override
    public void cleanup() {
        if (this.world != null) {
            removeLeftovers();
            this.world.removePluginChunkTickets(this.plugin);
        }
    }
}
