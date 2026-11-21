package me.s0nd3r.watermc.core.command.subcommands;

import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;
import me.s0nd3r.watermc.core.command.WaterCommand;
import me.s0nd3r.watermc.core.command.PermissionedWaterSubcommand;
import me.s0nd3r.watermc.core.config.modules.water.Diagnostics;
import me.s0nd3r.watermc.diagnostics.WaterDrain;

/** {@code /watermc drain [host:port]} — закрыть вход, перевести игроков на резервный узел и остановить сервер. */
public final class DrainCommand extends PermissionedWaterSubcommand {

    public static final String LITERAL_ARGUMENT = "drain";
    public static final String PERM = WaterCommand.BASE_PERM + "." + LITERAL_ARGUMENT;

    public DrainCommand() {
        super(PERM, PermissionDefault.FALSE); // только консоль или явно выданное право
    }

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        WaterDrain.Target target = null;
        if (args.length > 0) {
            try {
                target = WaterDrain.Target.parse(args[0]);
            } catch (final IllegalArgumentException ex) {
                sender.sendMessage(Component.text("Usage: /" + WaterCommand.COMMAND_LABEL + " drain [host:port] (" + ex.getMessage() + ")", NamedTextColor.RED));
                return true;
            }
        }
        if (!WaterDrain.start(MinecraftServer.getServer(), target, Diagnostics.drainTimeoutSeconds * 1000L)) {
            sender.sendMessage(Component.text("Drain is already in progress.", NamedTextColor.RED));
            return true;
        }
        sender.sendMessage(Component.text(target == null
            ? "Draining: logins closed, players disconnected; the server stops after data is saved."
            : "Draining: logins closed, players transferred to " + target.host() + ":" + target.port() + "; the server stops after data is saved.", NamedTextColor.GOLD));
        return true;
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String subCommand, final String[] args) {
        return args.length == 1 ? List.of("host:port") : List.of();
    }
}
