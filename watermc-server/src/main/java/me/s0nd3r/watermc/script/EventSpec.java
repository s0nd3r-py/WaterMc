package me.s0nd3r.watermc.script;

import java.util.List;
import java.util.Map;

/**
 * Описание события, на которое можно подписаться через {@code on <имя>:}.
 *
 * @param variables имена переменных, которые событие передаёт обработчику (в этом порядке)
 * @param cancellable можно ли {@code cancel} (только до первого {@code wait})
 * @param waitAllowed допустим ли {@code wait} в обработчике
 */
public record EventSpec(String name, List<String> variables, boolean cancellable, boolean waitAllowed, String description) {

    public static final Map<String, EventSpec> ALL = Map.ofEntries(
        spec("load", List.of(), false, true, "скрипты загружены (старт сервера или /watermc script reload)"),
        spec("unload", List.of(), false, false, "скрипты выгружаются (остановка или перезагрузка)"),
        spec("join", List.of("player"), false, true, "игрок зашёл"),
        spec("quit", List.of("player"), false, true, "игрок вышел"),
        spec("chat", List.of("player", "message"), true, true, "сообщение в чат; обработчик выполняется в основном потоке, cancel до первого wait скрывает сообщение"),
        spec("player_command", List.of("player", "line"), true, true, "игрок вводит команду (line — строка без «/», с аргументами)"),
        spec("damage", List.of("player", "cause", "damage"), true, true, "игрок получает урон (cause — причина строчными буквами, damage — число)"),
        spec("interact", List.of("player", "block", "action"), true, true, "игрок кликает основной рукой (block — блок или none; action — right_click_block, left_click_block, right_click_air, left_click_air, physical)"),
        spec("kill", List.of("player", "entity"), false, true, "игрок убил моба (entity — тип, например zombie)"),
        spec("world_change", List.of("player", "from"), false, true, "игрок перешёл в другой мир (from — имя прежнего мира)"),
        spec("break", List.of("player", "block"), true, true, "игрок ломает блок"),
        spec("place", List.of("player", "block"), true, true, "игрок ставит блок"),
        spec("death", List.of("player", "killer"), false, true, "игрок умер (killer — игрок-убийца или none)"),
        spec("respawn", List.of("player"), false, true, "игрок возродился")
    );

    private static Map.Entry<String, EventSpec> spec(final String name, final List<String> vars, final boolean cancellable, final boolean waitAllowed, final String description) {
        return Map.entry(name, new EventSpec(name, vars, cancellable, waitAllowed, description));
    }

    /** Переменные обработчика команды: отправитель, игрок (none для консоли), все аргументы, затем именованные. */
    public static final List<String> COMMAND_VARIABLES = List.of("sender", "player", "args");
}
