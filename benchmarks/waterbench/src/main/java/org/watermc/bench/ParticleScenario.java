package org.watermc.bench;

import java.lang.reflect.Method;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/** Exercises native particles and exempt API/forced paths on Leaf and WaterMc. */
final class ParticleScenario implements Scenario {
    private final int ticks;
    private final int packets;
    private World world;
    private Object handle;
    private Object flame;
    private Object vibration;
    private Method send;
    private Method targeted;
    private int tick;
    private long normalSent;
    private long forcedSent;
    private long alwaysSent;
    private long vibrationSent;
    private long targetedSent;
    private long expectedNormal;

    ParticleScenario(int seconds, int packets) {
        this.ticks = Math.max(1, seconds) * 20;
        this.packets = Math.max(1, packets);
    }

    @Override public String name() { return "particles"; }

    @Override
    public void prepare() {
        this.world = BenchWorld.get();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(new Location(this.world, 0.5, 0.5, 0.5));
            player.setAllowFlight(true);
            player.setFlying(true);
            // Verify the API's unscaled health still agrees with gameplay health under scaling.
            double health = player.getHealth();
            double scale = player.getHealthScale();
            boolean scaled = player.isHealthScaled();
            player.setHealthScale(40);
            player.setHealthScaled(true);
            player.setHealth(7);
            try {
                Object nms = player.getClass().getMethod("getHandle").invoke(player);
                float actual = (float) nms.getClass().getMethod("getHealth").invoke(nms);
                if (actual != 7 || player.getHealth() != 7) throw new AssertionError("Scaled health mismatch");
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            } finally {
                player.setHealth(health);
                player.setHealthScale(scale);
                player.setHealthScaled(scaled);
            }
        }
        try {
            this.handle = this.world.getClass().getMethod("getHandle").invoke(this.world);
            Class<?> options = Class.forName("net.minecraft.core.particles.ParticleOptions");
            this.flame = Class.forName("net.minecraft.core.particles.ParticleTypes").getField("FLAME").get(null);
            this.send = this.handle.getClass().getMethod("sendParticles", options, boolean.class, boolean.class,
                double.class, double.class, double.class, int.class, double.class, double.class, double.class, double.class);
            this.targeted = this.handle.getClass().getMethod("sendParticles",
                Class.forName("net.minecraft.server.level.ServerPlayer"), options, boolean.class, boolean.class,
                double.class, double.class, double.class, int.class, double.class, double.class, double.class, double.class);
            Class<?> blockPos = Class.forName("net.minecraft.core.BlockPos");
            Class<?> positionSource = Class.forName("net.minecraft.world.level.gameevent.PositionSource");
            Object destination = Class.forName("net.minecraft.world.level.gameevent.BlockPositionSource")
                .getConstructor(blockPos).newInstance(blockPos.getConstructor(int.class, int.class, int.class).newInstance(1, 1, 1));
            this.vibration = Class.forName("net.minecraft.core.particles.VibrationParticleOption")
                .getConstructor(positionSource, int.class).newInstance(destination, 5);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        Bukkit.getLogger().info("WaterBench API health scaling check passed; server=" + Bukkit.getName());
    }

    private int emit(boolean force, boolean always) {
        return emit(this.flame, force, always);
    }

    private int emit(Object particle, boolean force, boolean always) {
        try {
            return (int) this.send.invoke(this.handle, particle, force, always,
                0.5, 0.5, 0.5, 1, 0.0, 0.0, 0.0, 0.0);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TickResult tick() {
        if (++this.tick <= 200) return TickResult.SKIP; // join traffic and JIT warmup
        if (this.tick > 200 + this.ticks) return TickResult.FINISHED;
        this.expectedNormal += (long) this.packets * Bukkit.getOnlinePlayers().size();
        for (int i = 0; i < this.packets; ++i) this.normalSent += emit(false, false);
        // Send after exhausting the normal allowance, to prove these paths remain exempt.
        this.forcedSent += emit(true, false);
        this.alwaysSent += emit(false, true);
        this.vibrationSent += emit(this.vibration, false, false);
        try {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Object nms = player.getClass().getMethod("getHandle").invoke(player);
                if ((boolean) this.targeted.invoke(this.handle, nms, this.flame, false, false,
                    0.5, 0.5, 0.5, 1, 0.0, 0.0, 0.0, 0.0)) ++this.targetedSent;
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        this.world.spawnParticle(Particle.HAPPY_VILLAGER, 0.5, 0.5, 0.5, 1, 0, 0, 0, 0, null, false);
        return TickResult.COUNT;
    }

    @Override
    public void cleanup() {
        Bukkit.getLogger().info(String.format(Locale.ROOT,
            "PARTICLE_RESULT normal_sent=%d normal_requested=%d forced_sent=%d always_sent=%d vibration_sent=%d targeted_sent=%d api_calls=%d",
            this.normalSent, this.expectedNormal, this.forcedSent, this.alwaysSent,
            this.vibrationSent, this.targetedSent, this.ticks));
    }
}
