package me.s0nd3r.watermc.world;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Поиск сущностей для толкания с ранней остановкой.
 *
 * <p>{@code LivingEntity.pushEntities} использует список соседей только двумя способами:
 * <ol>
 *     <li>давка (gamerule {@code maxEntityCramming} = {@code i}): нужно знать, что в списке не меньше {@code i}
 *     сущностей, и в 1/4 случаев — что среди них не меньше {@code i} не-пассажиров;</li>
 *     <li>толкание первых сущностей списка, не больше {@code max-entity-collisions}.</li>
 * </ol>
 * Поэтому достаточно найти первые {@link #limit(int, int)} сущностей — в том же порядке, что и полный поиск
 * (порядок обхода у урезанного поиска Moonrise тот же, он просто останавливается раньше). В плотной толпе
 * (фермы мобов) это избавляет от перебора сотен соседей на каждую сущность каждый тик.</p>
 *
 * <p>Поведение эквивалентно полному поиску: условие {@code size > i - 1}, от которого зависит трата случайного
 * числа, у урезанного списка такое же; если урезанного списка не хватает, чтобы решить вопрос о давке
 * (мешают пассажиры), выполняется полный поиск.</p>
 */
public final class PushQuery {

    /** Аварийное отключение: {@code -Dwatermc.limited-push-query=false}. */
    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("watermc.limited-push-query", "true"));

    private PushQuery() {
    }

    /** Сколько сущностей нужно найти; 0 — поиск не нужен вовсе. */
    public static int limit(final int maxCramming, final int maxCollisions) {
        return Math.max(Math.max(maxCramming, 0), Math.max(maxCollisions, 0));
    }

    /** Урезан ли список (найдено столько, сколько просили; полный список может быть длиннее). */
    public static boolean truncated(final int found, final int limit) {
        return found >= limit;
    }

    /**
     * Решение о давке для уже выпавшего броска {@code random.nextInt(4) == 0}.
     *
     * @param list      найденные сущности (урезанные или полные)
     * @param truncated урезан ли список
     * @param full      полный список (вызывается только если урезанного не хватает)
     */
    public static <E> boolean crammed(final List<E> list, final boolean truncated, final Supplier<List<E>> full,
                                      final Predicate<E> isPassenger, final int maxCramming) {
        int nonPassengers = countNonPassengers(list, isPassenger);
        if (nonPassengers <= maxCramming - 1 && truncated) {
            nonPassengers = countNonPassengers(full.get(), isPassenger);
        }
        return nonPassengers > maxCramming - 1;
    }

    private static <E> int countNonPassengers(final List<E> list, final Predicate<E> isPassenger) {
        int count = 0;
        for (int i = 0, size = list.size(); i < size; i++) {
            if (!isPassenger.test(list.get(i))) {
                count++;
            }
        }
        return count;
    }
}
