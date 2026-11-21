package me.s0nd3r.watermc.diagnostics;

import io.papermc.paper.plugin.provider.classloader.ConfiguredPluginClassLoader;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audit of privileged actions: OP grants and removals, console commands issued by plugins, switches into
 * creative or spectator, plugins disabled at runtime.
 *
 * <p>Each entry names the plugin whose code is on the call stack (the first class loaded by a plugin class
 * loader), so a backdoor hidden in a plugin shows up in the log with its owner. Optionally the whole call
 * stack is logged to find the exact class and method.</p>
 *
 * <p>The stack is only walked when an audited action happens; these are rare, so the cost is negligible.</p>
 */
public final class SecurityAudit {

    private static final Logger LOGGER = LoggerFactory.getLogger("WaterMc Audit");
    private static final StackWalker WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static volatile boolean ops = true;
    public static volatile boolean consoleCommands = true;
    public static volatile boolean gameModes = true;
    public static volatile boolean pluginDisable = true;
    public static volatile boolean stackTraces = false;

    private SecurityAudit() {
    }

    /** Name of the first plugin on the current call stack, or {@code null} when the server itself acts. */
    public static @Nullable String callerPlugin() {
        final Optional<String> plugin = WALKER.walk(frames -> frames
            .map(frame -> frame.getDeclaringClass().getClassLoader())
            .filter(loader -> loader instanceof ConfiguredPluginClassLoader)
            .map(loader -> ((ConfiguredPluginClassLoader) loader).getConfiguration().getName())
            .findFirst());
        return plugin.orElse(null);
    }

    static String by(final @Nullable String plugin) {
        return plugin == null ? "server or command" : "plugin " + plugin;
    }

    private static void log(final boolean warn, final String message) {
        if (stackTraces) {
            final Throwable stack = new Throwable("Call stack (diagnostics.security-audit.stack-traces)");
            if (warn) LOGGER.warn(message, stack); else LOGGER.info(message, stack);
        } else if (warn) {
            LOGGER.warn(message);
        } else {
            LOGGER.info(message);
        }
    }

    public static void opGranted(final String player) {
        if (ops) log(true, "OP granted to " + player + " by " + by(callerPlugin()));
    }

    public static void opRemoved(final String player) {
        if (ops) log(false, "OP removed from " + player + " by " + by(callerPlugin()));
    }

    /** Console commands are logged only when a plugin issues them; commands typed into the console are not. */
    public static void consoleCommand(final String command) {
        if (!consoleCommands) return;
        final String plugin = callerPlugin();
        if (plugin != null) log(false, "Plugin " + plugin + " ran console command: /" + command);
    }

    /** Only switches into the privileged modes are logged, so minigames that flip survival/adventure stay quiet. */
    public static void gameModeChanged(final String player, final String from, final String to, final String cause) {
        if (!gameModes || from.equals(to) || !(to.equals("creative") || to.equals("spectator"))) return;
        log(true, "Game mode of " + player + " changed " + from + " -> " + to + " (cause " + cause + ") by " + by(callerPlugin()));
    }

    public static void pluginDisabled(final String plugin) {
        if (pluginDisable) log(true, "Plugin " + plugin + " was disabled at runtime by " + by(callerPlugin()));
    }
}
