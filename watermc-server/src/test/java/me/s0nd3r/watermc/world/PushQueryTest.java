package me.s0nd3r.watermc.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Дифференциальная проверка: толкание по урезанному поиску даёт ровно те же наблюдаемые эффекты,
 * что и по полному списку соседей (LivingEntity.pushEntities до правки).
 */
class PushQueryTest {

    /** Сосед: номер (для сравнения набора толкаемых) и признак пассажира. */
    record Neighbour(int id, boolean passenger) {
    }

    record Effects(boolean randomConsumed, boolean crammed, List<Neighbour> pushed) {
    }

    /** Исходная логика pushEntities на полном списке. */
    static Effects reference(final List<Neighbour> full, final int cramming, final int maxCollisions, final int numCollisionsBefore, final boolean roll) {
        final boolean consumed = cramming > 0 && full.size() > cramming - 1;
        boolean crammed = false;
        if (consumed && roll) {
            int nonPassengers = 0;
            for (final Neighbour n : full) {
                if (!n.passenger()) {
                    nonPassengers++;
                }
            }
            crammed = nonPassengers > cramming - 1;
        }
        return new Effects(consumed, crammed, pushed(full, maxCollisions, numCollisionsBefore));
    }

    /** Новая логика: урезанный поиск в том же порядке + PushQuery. */
    static Effects limited(final List<Neighbour> full, final int cramming, final int maxCollisions, final int numCollisionsBefore, final boolean roll) {
        final int limit = PushQuery.limit(cramming, maxCollisions);
        final List<Neighbour> list = new ArrayList<>(full.subList(0, Math.min(full.size(), limit)));
        final boolean truncated = PushQuery.truncated(list.size(), limit);
        final boolean consumed = cramming > 0 && list.size() > cramming - 1;
        final boolean crammed = consumed && roll && PushQuery.crammed(list, truncated, () -> full, Neighbour::passenger, cramming);
        return new Effects(consumed, crammed, pushed(list, maxCollisions, numCollisionsBefore));
    }

    private static List<Neighbour> pushed(final List<Neighbour> list, final int maxCollisions, final int numCollisionsBefore) {
        int numCollisions = Math.max(0, numCollisionsBefore - maxCollisions);
        final List<Neighbour> out = new ArrayList<>();
        for (final Neighbour n : list) {
            if (numCollisions >= maxCollisions) {
                break;
            }
            numCollisions++;
            out.add(n);
        }
        return out;
    }

    @Test
    void limitedScanHasIdenticalObservableEffects() {
        final Random random = new Random(20261119L);
        int truncatedCases = 0;
        int fallbackCases = 0;
        for (int iteration = 0; iteration < 200_000; iteration++) {
            final int size = random.nextInt(4) == 0 ? random.nextInt(400) : random.nextInt(60);
            final double passengerRate = random.nextInt(5) == 0 ? 0.9 : 0.1;
            final List<Neighbour> full = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                full.add(new Neighbour(i, random.nextDouble() < passengerRate));
            }
            final int cramming = random.nextInt(6) == 0 ? 0 : random.nextInt(40);
            final int maxCollisions = random.nextInt(6) == 0 ? 0 : random.nextInt(16);
            if (PushQuery.limit(cramming, maxCollisions) == 0) {
                continue; // pushEntities returns before searching
            }
            final int numCollisions = random.nextInt(20);
            final boolean roll = random.nextBoolean();
            final Effects expected = reference(full, cramming, maxCollisions, numCollisions, roll);
            final Effects actual = limited(full, cramming, maxCollisions, numCollisions, roll);
            assertEquals(expected, actual, () -> "size=" + full.size() + " cramming=" + cramming + " maxCollisions=" + maxCollisions + " numCollisions=" + numCollisions);
            if (full.size() > PushQuery.limit(cramming, maxCollisions)) {
                truncatedCases++;
                if (roll && cramming > 0) {
                    fallbackCases++;
                }
            }
        }
        assertTrue(truncatedCases > 10_000, "the scenario space must actually exercise truncation: " + truncatedCases);
        assertTrue(fallbackCases > 1_000, "and the cramming decision on truncated lists: " + fallbackCases);
    }

    @Test
    void fullScanOnlyWhenTheTruncatedListCannotDecide() {
        final List<String> crowd = List.of("a", "b", "c", "d");
        final int[] fullCalls = {0};
        // 4 не-пассажира при давке 3: решается без полного поиска
        assertTrue(PushQuery.crammed(crowd, true, () -> {
            fullCalls[0]++;
            return crowd;
        }, s -> false, 3));
        assertEquals(0, fullCalls[0]);
        // все пассажиры в урезанном списке — нужен полный
        PushQuery.crammed(crowd, true, () -> {
            fullCalls[0]++;
            return List.of("a", "b", "c", "d", "e");
        }, s -> true, 3);
        assertEquals(1, fullCalls[0]);
        // не урезан — полный поиск не нужен никогда
        assertFalse(PushQuery.crammed(crowd, false, () -> {
            fullCalls[0]++;
            return crowd;
        }, s -> true, 3));
        assertEquals(1, fullCalls[0]);
    }
}
