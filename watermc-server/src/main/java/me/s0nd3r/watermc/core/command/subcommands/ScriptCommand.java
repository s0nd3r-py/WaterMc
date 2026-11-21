package me.s0nd3r.watermc.core.command.subcommands;

import java.util.List;
import java.util.Map;
import me.s0nd3r.watermc.script.ScriptEngine;
import me.s0nd3r.watermc.script.bukkit.BukkitScripts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import me.s0nd3r.watermc.core.command.WaterCommand;
import me.s0nd3r.watermc.core.command.PermissionedWaterSubcommand;

/** {@code /watermc script <reload|status|functions>} — управление встроенными скриптами. */
public final class ScriptCommand extends PermissionedWaterSubcommand {

    public static final String LITERAL_ARGUMENT = "script";
    public static final String PERM = WaterCommand.BASE_PERM + "." + LITERAL_ARGUMENT;

    public ScriptCommand() {
        super(PERM, PermissionDefault.OP);
    }

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        final String action = args.length == 0 ? "status" : args[0].toLowerCase(java.util.Locale.ROOT);
        switch (action) {
            case "reload" -> {
                if (!BukkitScripts.enabled) {
                    sender.sendMessage(Component.text("Scripts are disabled in config/watermc.yml (scripts.enabled).", NamedTextColor.RED));
                    return true;
                }
                final long start = System.nanoTime();
                final ScriptEngine.LoadResult result = BukkitScripts.reload();
                if (result == null) {
                    sender.sendMessage(Component.text("Scripts failed to start, see console.", NamedTextColor.RED));
                    return true;
                }
                sender.sendMessage(Component.text(String.format(java.util.Locale.ROOT, "Scripts reloaded in %.1f ms: %d loaded, %d with errors, %d commands.",
                    (System.nanoTime() - start) / 1e6, result.filesLoaded(), result.filesFailed(), result.commands().size()),
                    result.errors().isEmpty() ? NamedTextColor.GREEN : NamedTextColor.YELLOW));
                for (final String error : result.errors()) {
                    sender.sendMessage(Component.text("  " + error, NamedTextColor.RED));
                }
            }
            case "status" -> {
                final ScriptEngine engine = BukkitScripts.engine();
                final ScriptEngine.LoadResult result = BukkitScripts.lastResult();
                if (engine == null || result == null) {
                    sender.sendMessage(Component.text("Scripts are not running.", NamedTextColor.GRAY));
                    return true;
                }
                sender.sendMessage(Component.text("Scripts: " + result.filesLoaded() + " loaded, " + result.filesFailed() + " with errors; events: "
                    + String.join(", ", new java.util.TreeSet<>(result.events())) + "; commands: " + result.commands().size()
                    + "; waiting executions: " + engine.pendingWaits() + "; $variables: " + engine.globals().names().size(), NamedTextColor.GOLD));
                for (final String error : result.errors()) {
                    sender.sendMessage(Component.text("  " + error, NamedTextColor.RED));
                }
            }
            case "functions" -> {
                final ScriptEngine engine = BukkitScripts.engine();
                if (engine == null) {
                    sender.sendMessage(Component.text("Scripts are not running.", NamedTextColor.GRAY));
                    return true;
                }
                for (final Map.Entry<String, String> fn : engine.functions().entrySet()) {
                    sender.sendMessage(Component.text("  " + fn.getKey() + "()  — " + fn.getValue(), NamedTextColor.YELLOW));
                }
            }
            default -> sender.sendMessage(Component.text("Usage: /" + WaterCommand.COMMAND_LABEL + " script <reload|status|functions>", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        return args.length == 1 ? List.of("reload", "status", "functions") : List.of();
    }
}
