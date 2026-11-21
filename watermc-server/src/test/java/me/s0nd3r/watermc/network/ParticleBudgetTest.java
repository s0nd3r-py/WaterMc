package me.s0nd3r.watermc.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParticleBudgetTest {
    @Test
    void enforcesBothLimitsAndResetsOnTickChange() {
        ParticleBudget budget = new ParticleBudget();
        assertTrue(budget.tryConsume(4, 5, 2, 10));
        assertTrue(budget.tryConsume(4, 5, 2, 10));
        assertFalse(budget.tryConsume(4, 1, 2, 10));
        assertTrue(budget.tryConsume(5, 10, 2, 10));
        assertFalse(budget.tryConsume(5, 1, 2, 10));
    }

    @Test
    void rejectedLargePacketDoesNotConsumeAllowance() {
        ParticleBudget budget = new ParticleBudget();
        assertFalse(budget.tryConsume(0, Integer.MAX_VALUE, 1, 10));
        assertTrue(budget.tryConsume(0, 10, 1, 10));
    }

    @Test
    void countsZeroAndNegativeAsOne() {
        ParticleBudget budget = new ParticleBudget();
        assertTrue(budget.tryConsume(0, 0, 10, 2));
        assertTrue(budget.tryConsume(0, -1, 10, 2));
        assertFalse(budget.tryConsume(0, 0, 10, 2));
    }

    @Test
    void tickWrapAndIndependentPlayersDoNotShareAllowance() {
        ParticleBudget first = new ParticleBudget();
        ParticleBudget second = new ParticleBudget();
        assertTrue(first.tryConsume(Integer.MAX_VALUE, 1, 1, 1));
        assertFalse(first.tryConsume(Integer.MAX_VALUE, 1, 1, 1));
        assertTrue(second.tryConsume(Integer.MAX_VALUE, 1, 1, 1));
        assertTrue(first.tryConsume(Integer.MIN_VALUE, 1, 1, 1));
    }

    @Test
    void handlesLargeCountsAndReloadedLimitsWithoutOverflow() {
        ParticleBudget budget = new ParticleBudget();
        assertTrue(budget.tryConsume(1, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertFalse(budget.tryConsume(1, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertFalse(budget.tryConsume(1, 1, 1, 1));
        assertTrue(budget.tryConsume(2, 1, 1, 1));
    }
}
