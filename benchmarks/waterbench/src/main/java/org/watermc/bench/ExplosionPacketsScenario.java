package org.watermc.bench;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/** Проверяет реальную отправку пакетов лимитера; мир, урон и скорость не изменяет. */
final class ExplosionPacketsScenario implements Scenario {
    private Field maximum;
    private int previousMaximum;
    private Method send;
    private java.lang.reflect.Constructor<?> vector;
    private Object particle;
    private Object sound;
    private Object debris;
    private int tick;

    @Override public String name() { return "explosionpackets"; }

    @Override
    public void prepare() {
        try {
            this.maximum = Class.forName("org.dreeam.leaf.config.modules.water.ExplosionPacketCoalescing")
                .getField("maxPacketsPerTick");
            this.previousMaximum = this.maximum.getInt(null);
            this.maximum.setInt(null, 2);
            Class<?> vec = Class.forName("net.minecraft.world.phys.Vec3");
            Class<?> weighted = Class.forName("net.minecraft.util.random.WeightedList");
            this.vector = vec.getConstructor(double.class, double.class, double.class);
            this.send = Class.forName("org.watermc.network.ExplosionPacketLimiter").getMethod("send",
                Class.forName("net.minecraft.server.level.ServerLevel"),
                Class.forName("net.minecraft.server.level.ServerPlayer"), vec, float.class, int.class, vec,
                Class.forName("net.minecraft.core.particles.ParticleOptions"),
                Class.forName("net.minecraft.core.Holder"), weighted);
            this.particle = Class.forName("net.minecraft.core.particles.ParticleTypes").getField("EXPLOSION").get(null);
            this.sound = Class.forName("net.minecraft.sounds.SoundEvents").getField("GENERIC_EXPLODE").get(null);
            this.debris = weighted.getMethod("of").invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Не подготовлена проверка пакетов взрывов", e);
        }
    }

    @Override
    public TickResult tick() {
        ++this.tick;
        if (this.tick <= 200) return TickResult.SKIP;
        if (this.tick == 201) {
            if (Bukkit.getOnlinePlayers().isEmpty()) throw new AssertionError("Нет получателей пакетов");
            double[][] vectors = {null, {0.1, 0.2, 0.3}, null, {-0.4, 0.01, 0.0625}, {0, 0, 0}, null};
            try {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Object level = player.getWorld().getClass().getMethod("getHandle").invoke(player.getWorld());
                    Object handle = player.getClass().getMethod("getHandle").invoke(player);
                    for (int i = 0; i < vectors.length; ++i) {
                        Object center = this.vector.newInstance(9000.0 + i, 64.0, 0.0);
                        double[] v = vectors[i];
                        Object knockback = v == null ? null : this.vector.newInstance(v[0], v[1], v[2]);
                        this.send.invoke(null, level, handle, center, 2.0F, i, knockback,
                            this.particle, this.sound, this.debris);
                    }
                }
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Не отправлены тестовые пакеты взрывов", e);
            }
            Bukkit.getLogger().info("EXPLOSION_PACKET_CHECKS visual_limit=2 attempts_per_player=6 expected_packets_per_player=4 expected_knockbacks_per_player=3");
        }
        return this.tick > 240 ? TickResult.FINISHED : TickResult.COUNT;
    }

    @Override
    public void cleanup() {
        try {
            if (this.maximum != null) this.maximum.setInt(null, this.previousMaximum);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
