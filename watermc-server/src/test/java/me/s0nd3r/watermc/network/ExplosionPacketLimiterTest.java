package me.s0nd3r.watermc.network;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExplosionPacketLimiterTest {
    @Test
    void neverDropsKnockbackAfterVisualAllowanceIsExhausted() {
        var state = new ExplosionPacketLimiter.PlayerState();
        assertTrue(state.shouldSend(1, 2, false));
        assertTrue(state.shouldSend(1, 2, true));
        assertFalse(state.shouldSend(1, 2, false));
        for (int i = 0; i < 100_000; ++i) assertTrue(state.shouldSend(1, 2, true));
        assertFalse(state.shouldSend(1, 2, false));
    }

    @Test
    void resetsOnTickChangeIncludingIntegerWrap() {
        var state = new ExplosionPacketLimiter.PlayerState();
        assertTrue(state.shouldSend(Integer.MAX_VALUE, 1, false));
        assertFalse(state.shouldSend(Integer.MAX_VALUE, 1, false));
        assertTrue(state.shouldSend(Integer.MIN_VALUE, 1, false));
        assertFalse(state.shouldSend(Integer.MIN_VALUE, 1, false));
        assertTrue(state.shouldSend(Integer.MIN_VALUE, 1, true));
    }

    @Test
    void keepsPlayerAllowancesIndependentAndHonorsLoweredLimits() {
        var first = new ExplosionPacketLimiter.PlayerState();
        var second = new ExplosionPacketLimiter.PlayerState();
        assertTrue(first.shouldSend(0, 3, false));
        assertTrue(first.shouldSend(0, 3, false));
        assertFalse(first.shouldSend(0, 1, false));
        assertTrue(second.shouldSend(0, 1, false));
        assertTrue(first.shouldSend(0, 1, true));
    }

    @Test
    void cannotQueueKnockbackAcrossWorldTransfersInTheSameTick() {
        // Состояние не содержит очереди или ссылки на мир: каждый импульс проходит
        // немедленно, независимо от того, какой мир теперь владеет игроком.
        var player = new ExplosionPacketLimiter.PlayerState();
        assertTrue(player.shouldSend(7, 1, false));
        assertTrue(player.shouldSend(7, 1, true)); // мир A
        assertTrue(player.shouldSend(7, 1, true)); // телепорт в мир B, тот же тик
        assertFalse(player.shouldSend(7, 1, false));
    }
}
