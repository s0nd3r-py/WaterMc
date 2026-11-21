package me.s0nd3r.watermc.event;

import org.bukkit.event.HandlerList;

/**
 * Проверка наличия слушателей до создания объекта события.
 *
 * <p>{@link HandlerList#getRegisteredListeners()} возвращает уже «запечённый» массив (пересобирается только при
 * регистрации/отмене регистрации), поэтому проверка — одно чтение поля и длины массива без аллокаций.
 * Если слушателей нет, объект события, его {@code Location} и прочие аргументы не создаются.</p>
 */
public final class WaterEvents {

    private WaterEvents() {
    }

    public static boolean hasListeners(final HandlerList handlers) {
        return handlers.getRegisteredListeners().length != 0;
    }

    public static boolean hasPlayerMoveListeners() {
        return hasListeners(org.bukkit.event.player.PlayerMoveEvent.getHandlerList());
    }
}
