package me.s0nd3r.watermc.network;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.phys.Vec3;
import me.s0nd3r.watermc.core.config.modules.water.ExplosionPacketCoalescing;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Ограничивает необязательные визуальные пакеты взрывов на игрока за тик.
 * Пакеты с импульсом передаются сразу и без изменений, даже сверх лимита:
 * сложение векторов меняет округление и порядок относительно других пакетов,
 * а отложенная очередь мира может потерять импульс при телепорте игрока.
 * По умолчанию ограничение выключено.
 */
public final class ExplosionPacketLimiter {
    private ExplosionPacketLimiter() {}

    public static final class PlayerState {
        private int tick = Integer.MIN_VALUE;
        private int sent;

        public boolean shouldSend(int now, int maximum, boolean carriesKnockback) {
            if (this.tick != now) {
                this.tick = now;
                this.sent = 0;
            }
            if (this.sent < maximum) {
                ++this.sent;
                return true;
            }
            return carriesKnockback;
        }
    }

    public static void send(
        ServerLevel level,
        ServerPlayer player,
        Vec3 center,
        float radius,
        int blockCount,
        @Nullable Vec3 knockback,
        ParticleOptions particle,
        Holder<SoundEvent> sound,
        WeightedList<ExplosionParticleInfo> blockParticles
    ) {
        if (player.water$explosionState.shouldSend(MinecraftServer.currentTick,
                ExplosionPacketCoalescing.maxPacketsPerTick, knockback != null)) {
            player.connection.send(new ClientboundExplodePacket(center, radius, blockCount,
                Optional.ofNullable(knockback), particle, sound, blockParticles));
        }
    }
}
