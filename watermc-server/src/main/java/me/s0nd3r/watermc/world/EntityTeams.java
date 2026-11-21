package me.s0nd3r.watermc.world;

/**
 * Быстрый путь поиска команды для не-игровых сущностей.
 *
 * <p>Имя сущности на табло — {@code UUID.toString()}, всегда 36 символов; имя игрока не длиннее 16.
 * {@code Scoreboard} считает записи команд с ключом такой длины. Пока их нет, {@code Entity.getTeam()} для
 * не-игрока сразу возвращает {@code null} и не обращается к строке UUID.</p>
 *
 * <p>Зачем: предикат толкания ({@code EntitySelector.pushableBy}) спрашивает команду у каждого соседа.
 * В толпе из 600 коров чтение строки UUID и её хэша (холодная строка, промахи кэша) занимало
 * треть основного потока, хотя на сервере не было ни одной команды с сущностями.</p>
 */
public final class EntityTeams {

    /** Длина {@code UUID.toString()}. */
    public static final int UUID_NAME_LENGTH = 36;

    private EntityTeams() {
    }
}
