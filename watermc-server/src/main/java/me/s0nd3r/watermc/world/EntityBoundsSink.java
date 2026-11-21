package me.s0nd3r.watermc.world;

import net.minecraft.world.phys.AABB;

/**
 * Список сущностей секции, который хранит копию их габаритов в плоском массиве {@code double[]}.
 *
 * <p>Поиск соседей в толпе проверял пересечение для каждой сущности секции: ссылка из массива → объект
 * сущности → объект {@code AABB} → поля. Это два промаха кэша на кандидата, и в загоне из 600 мобов
 * они занимали четверть основного потока. С плоской копией проверка читает подряд идущие числа, а сам
 * объект сущности трогается только при совпадении.</p>
 *
 * <p>Копия точная: те же значения {@code double}, что и в {@code Entity.bb}. {@code Entity.setBoundingBox} —
 * единственное место записи габаритов, и оно сразу обновляет копию у текущего владельца.</p>
 */
public interface EntityBoundsSink {

    /** Записать габариты сущности, стоящей в списке под номером {@code index}. */
    void waterWriteBounds(int index, AABB bb);

    /** Аварийное отключение: {@code -Dwatermc.packed-entity-bounds=false}. */
    boolean ENABLED = Boolean.parseBoolean(System.getProperty("watermc.packed-entity-bounds", "true"));

    /**
     * Самопроверка: {@code -Dwatermc.packed-entity-bounds.verify=true} сверяет копию с настоящими габаритами
     * сущности при каждой проверке и бросает исключение при расхождении. Для стендов и отладки.
     */
    boolean VERIFY = Boolean.getBoolean("watermc.packed-entity-bounds.verify");

    /** Сверка копии с габаритами сущности (только в режиме {@link #VERIFY}). */
    static void verify(final double[] bounds, final int index, final net.minecraft.world.entity.Entity entity) {
        final AABB bb = entity.getBoundingBox();
        final int base = index * STRIDE;
        if (bounds[base + MIN_X] != bb.minX || bounds[base + MIN_Y] != bb.minY || bounds[base + MIN_Z] != bb.minZ
            || bounds[base + MAX_X] != bb.maxX || bounds[base + MAX_Y] != bb.maxY || bounds[base + MAX_Z] != bb.maxZ) {
            throw new IllegalStateException("Packed bounds out of sync for " + entity + " at index " + index + ": " + bb);
        }
    }

    /** Смещения шести чисел в записи одной сущности. */
    int MIN_X = 0, MIN_Y = 1, MIN_Z = 2, MAX_X = 3, MAX_Y = 4, MAX_Z = 5, STRIDE = 6;

    static void write(final double[] bounds, final int index, final AABB bb) {
        final int base = index * STRIDE;
        bounds[base + MIN_X] = bb.minX;
        bounds[base + MIN_Y] = bb.minY;
        bounds[base + MIN_Z] = bb.minZ;
        bounds[base + MAX_X] = bb.maxX;
        bounds[base + MAX_Y] = bb.maxY;
        bounds[base + MAX_Z] = bb.maxZ;
    }

    /** Та же проверка, что в поиске Moonrise/Leaf: {@code true}, если габариты НЕ пересекаются с рамкой. */
    static boolean disjoint(final double[] bounds, final int index,
                            final double boxMinX, final double boxMinY, final double boxMinZ,
                            final double boxMaxX, final double boxMaxY, final double boxMaxZ) {
        final int base = index * STRIDE;
        return bounds[base + MAX_X] <= boxMinX || bounds[base + MIN_X] >= boxMaxX
            || bounds[base + MAX_Y] <= boxMinY || bounds[base + MIN_Y] >= boxMaxY
            || bounds[base + MAX_Z] <= boxMinZ || bounds[base + MIN_Z] >= boxMaxZ;
    }
}
