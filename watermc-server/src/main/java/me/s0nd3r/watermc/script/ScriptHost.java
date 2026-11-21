package me.s0nd3r.watermc.script;

import java.util.List;

/**
 * Связь движка с сервером. Ядро языка не зависит от Bukkit: в тестах подставляется простая
 * реализация, на сервере — {@code me.s0nd3r.watermc.script.bukkit.BukkitScriptHost}.
 */
public interface ScriptHost {

    void broadcast(String message);

    void dispatchConsoleCommand(String command);

    void logInfo(String message);

    void logError(String message);

    /** Выполнить задачу в основном потоке через {@code delayTicks} тиков. */
    Task schedule(long delayTicks, Runnable task);

    /** Выполнять задачу в основном потоке каждые {@code periodTicks} тиков. */
    Task scheduleRepeating(long periodTicks, Runnable task);

    List<Object> onlinePlayers();

    /** Игрок онлайн по имени (без учёта регистра) или {@code null}. */
    Object playerByName(String name);

    /** Мир по имени или {@code null}. */
    Object worldByName(String name);

    /** Консоль как получатель сообщений. */
    ScriptMessageable console();

    interface Task {
        void cancel();
    }
}
