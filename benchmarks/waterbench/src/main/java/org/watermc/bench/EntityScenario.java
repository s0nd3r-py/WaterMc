package org.watermc.bench;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Many AI-driven mobs inside a closed pen. Mix is chosen to exercise brains (villagers), goal selectors,
 * pathfinding (zombies vs villagers), collisions and entity tracking.
 */
final class EntityScenario implements Scenario {

    private static final int GROUND_Y = -60;
    private static final int CENTER_X = -4096;
    private static final int DEFAULT_HALF = 40;
    private static final int WARMUP_TICKS = 20 * 15;

    private static final EntityType[] MIX = {
        EntityType.COW, EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN,
        EntityType.VILLAGER, EntityType.VILLAGER,
        EntityType.ZOMBIE, EntityType.SKELETON, EntityType.IRON_GOLEM, EntityType.WOLF
    };

    private final Plugin plugin;
    private final int count;
    private final int durationTicks;
    private final int durationSeconds;
    private long measurementStartedAt;
    private int measuredTicks;
    private final List<Entity> spawned = new ArrayList<>();
    private World world;
    private int tick;
    private final boolean stablePopulation;
    private final int half;
    private final EntityType[] mix;
    private final Integer crammingRule;

    EntityScenario(Plugin plugin, int count, int durationSeconds) {
        this(plugin, count, durationSeconds, false);
    }

    EntityScenario(Plugin plugin, int count, int durationSeconds, boolean stablePopulation) {
        this(plugin, count, durationSeconds, stablePopulation, DEFAULT_HALF, MIX, null);
    }

    /** Плотная толпа: count коров в загоне (2*half+1)^2; crammingRule — gamerule maxEntityCramming (null — не трогать). */
    static EntityScenario crowd(Plugin plugin, int count, int durationSeconds, int half, Integer crammingRule) {
        return new EntityScenario(plugin, count, durationSeconds, false, half, new EntityType[]{EntityType.COW}, crammingRule);
    }

    private EntityScenario(Plugin plugin, int count, int durationSeconds, boolean stablePopulation, int half, EntityType[] mix, Integer crammingRule) {
        this.half = half;
        this.mix = mix;
        this.crammingRule = crammingRule;
        this.stablePopulation = stablePopulation;
        this.plugin = plugin;
        this.count = count;
        this.durationTicks = durationSeconds * 20;
        this.durationSeconds = durationSeconds;
    }

    @Override
    public String name() {
        return this.stablePopulation ? "soak" : this.mix.length == 1 ? "crowd" : "entities";
    }

    @Override
    public void prepare() {
        this.world = BenchWorld.get();
        if (this.crammingRule != null) {
            this.world.setGameRule(org.bukkit.GameRule.MAX_ENTITY_CRAMMING, this.crammingRule);
        }
        final int HALF = this.half;
        BenchWorld.loadArea(this.plugin, this.world, CENTER_X, 0, 6);
        // closed pen so mobs never wander into chunks without tickets
        for (int i = -HALF; i <= HALF; i++) {
            for (int y = 0; y < 4; y++) {
                this.world.getBlockAt(CENTER_X + i, GROUND_Y + y, -HALF).setType(Material.BARRIER, false);
                this.world.getBlockAt(CENTER_X + i, GROUND_Y + y, HALF).setType(Material.BARRIER, false);
                this.world.getBlockAt(CENTER_X - HALF, GROUND_Y + y, i).setType(Material.BARRIER, false);
                this.world.getBlockAt(CENTER_X + HALF, GROUND_Y + y, i).setType(Material.BARRIER, false);
            }
        }
        Random random = new Random(42L);
        for (int i = 0; i < this.count; i++) {
            EntityType type = this.mix[i % this.mix.length];
            double x = CENTER_X + (random.nextDouble() * 2 - 1) * (HALF - 2);
            double z = (random.nextDouble() * 2 - 1) * (HALF - 2);
            Entity entity = this.world.spawnEntity(new Location(this.world, x, GROUND_Y, z), type);
            if (entity instanceof Mob mob) {
                mob.setRemoveWhenFarAway(false);
                mob.setPersistent(true);
            }
            if (this.stablePopulation) entity.setInvulnerable(true);
            this.spawned.add(entity);
        }
        BenchWorld.moveSpectators(this.world, CENTER_X, GROUND_Y, 0);
        this.tick = 0;
    }

    @Override
    public TickResult tick() {
        ++this.tick;
        if (this.stablePopulation && this.tick % 20 == 0) verifyPopulation();
        if (this.tick <= WARMUP_TICKS) return TickResult.SKIP;
        if (this.stablePopulation) {
            long now = System.nanoTime();
            if (this.measurementStartedAt == 0) this.measurementStartedAt = now;
            long elapsed = now - this.measurementStartedAt;
            if (elapsed >= java.util.concurrent.TimeUnit.SECONDS.toNanos(this.durationSeconds)) {
                verifyPopulation();
                this.plugin.getLogger().info("SOAK_CHECKS measured_ticks=" + this.measuredTicks
                    + " wall_millis=" + elapsed / 1_000_000L + " alive=" + this.count);
                return TickResult.FINISHED;
            }
            ++this.measuredTicks;
        } else if (this.tick > WARMUP_TICKS + this.durationTicks) {
            return TickResult.FINISHED;
        }
        return TickResult.COUNT;
    }

    private void verifyPopulation() {
        long alive = this.spawned.stream().filter(entity -> entity.isValid() && !entity.isDead()).count();
        if (alive != this.count) throw new AssertionError("Изменилось число мобов в длительном тесте: " + alive);
    }

    @Override
    public void cleanup() {
        for (Entity e : this.spawned) e.remove();
        this.spawned.clear();
        if (this.world != null) {
            for (Entity e : this.world.getEntities()) {
                if (!(e instanceof Player)) e.remove();
            }
            this.world.removePluginChunkTickets(this.plugin);
        }
    }
}
