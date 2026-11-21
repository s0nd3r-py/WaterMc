package me.s0nd3r.watermc.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;

/**
 * Предварительная отсечка мобкапа до обхода чанков.
 *
 * <p>С per-player мобкапом чанк может заспавнить категорию, только если хотя бы у одного игрока рядом
 * {@code limit - mobCountNear > 0}. Если это неверно для <b>каждого</b> игрока мира, ни один чанк не
 * пройдёт проверку — категория отбрасывается один раз за тик, и обход чанков для неё (биомы, свет,
 * позиции) не выполняется. Проверка по всем игрокам мира, а не по ближайшим, поэтому отсечка
 * никогда не запрещает спавн, который был бы разрешён.</p>
 */
public final class MobcapPrecheck {

    private MobcapPrecheck() {
    }

    /** Есть ли хоть один игрок, у которого лимит категории не исчерпан. */
    public static boolean anyBelowLimit(final int limit, final int[] mobCountsNear) {
        for (final int count : mobCountsNear) {
            if (limit - count > 0) {
                return true;
            }
        }
        return false;
    }

    public static int limitFor(final ServerLevel level, final MobCategory category) {
        int limit = category.getMaxInstancesPerChunk();
        final org.bukkit.entity.SpawnCategory spawnCategory = org.bukkit.craftbukkit.util.CraftSpawnCategory.toBukkit(category);
        if (org.bukkit.craftbukkit.util.CraftSpawnCategory.isValidForLimits(spawnCategory)) {
            limit = level.getWorld().getSpawnLimit(spawnCategory);
        }
        return limit;
    }

    /** Возвращает категории, которые ещё могут заспавниться хотя бы у одного игрока. */
    public static List<MobCategory> dropFullCategories(final ServerLevel level, final List<MobCategory> categories) {
        if (categories.isEmpty() || !level.paperConfig().entities.spawning.perPlayerMobSpawns) {
            return categories;
        }
        final List<ServerPlayer> players = level.players();
        final int[] counts = new int[players.size()];
        List<MobCategory> result = null;
        for (int c = 0; c < categories.size(); c++) {
            final MobCategory category = categories.get(c);
            for (int p = 0; p < counts.length; p++) {
                counts[p] = level.getChunkSource().chunkMap.getMobCountNear(players.get(p), category);
            }
            final boolean spawnable = anyBelowLimit(limitFor(level, category), counts);
            if (!spawnable && result == null) {
                result = new ArrayList<>(categories.subList(0, c));
            } else if (spawnable && result != null) {
                result.add(category);
            }
        }
        return result == null ? categories : result;
    }
}
