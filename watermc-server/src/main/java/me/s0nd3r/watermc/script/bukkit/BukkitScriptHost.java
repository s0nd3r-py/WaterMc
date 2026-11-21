package me.s0nd3r.watermc.script.bukkit;

import java.util.ArrayList;
import java.util.List;
import me.s0nd3r.watermc.script.ScriptHost;
import me.s0nd3r.watermc.script.ScriptMessageable;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Реализация {@link ScriptHost} поверх Bukkit API. Все задачи выполняются в основном потоке. */
final class BukkitScriptHost implements ScriptHost {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc Scripts");

    private final Plugin owner;

    BukkitScriptHost(final Plugin owner) {
        this.owner = owner;
    }

    @Override
    public void broadcast(final String message) {
        Bukkit.getServer().broadcast(ScriptObjects.color(message));
    }

    @Override
    public void dispatchConsoleCommand(final String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public void logInfo(final String message) {
        LOGGER.info(message);
    }

    @Override
    public void logError(final String message) {
        LOGGER.warn(message);
    }

    @Override
    public Task schedule(final long delayTicks, final Runnable task) {
        final BukkitTask scheduled = Bukkit.getScheduler().runTaskLater(this.owner, task, Math.max(1L, delayTicks));
        return scheduled::cancel;
    }

    @Override
    public Task scheduleRepeating(final long periodTicks, final Runnable task) {
        final long period = Math.max(1L, periodTicks);
        final BukkitTask scheduled = Bukkit.getScheduler().runTaskTimer(this.owner, task, period, period);
        return scheduled::cancel;
    }

    @Override
    public List<Object> onlinePlayers() {
        final List<Object> out = new ArrayList<>();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            out.add(new ScriptObjects.PlayerObject(player));
        }
        return out;
    }

    @Override
    public Object playerByName(final String name) {
        final Player player = Bukkit.getPlayerExact(name);
        return player == null ? null : new ScriptObjects.PlayerObject(player);
    }

    @Override
    public Object worldByName(final String name) {
        final World world = Bukkit.getWorld(name);
        return world == null ? null : new ScriptObjects.WorldObject(world);
    }

    @Override
    public ScriptMessageable console() {
        return new ScriptObjects.SenderObject(Bukkit.getConsoleSender());
    }
}
