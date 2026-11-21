package me.s0nd3r.watermc.core.command.subcommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import me.s0nd3r.watermc.core.command.WaterCommand;
import me.s0nd3r.watermc.core.command.PermissionedWaterSubcommand;
import me.s0nd3r.watermc.core.config.WaterConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.permissions.PermissionDefault;

public final class ReloadCommand extends PermissionedWaterSubcommand {

    public final static String LITERAL_ARGUMENT = "reload";
    public static final String PERM = WaterCommand.BASE_PERM + "." + LITERAL_ARGUMENT;

    public ReloadCommand() {
        super(PERM, PermissionDefault.OP);
    }

    @Override
    public boolean execute(final CommandSender sender, final String subCommand, final String[] args) {
        this.doGaleReload(sender);
        this.doLeafReload(sender);
        return true;
    }

    // Gale start - Gale commands - /gale reload command
    private void doGaleReload(final CommandSender sender) {
        Command.broadcastCommandMessage(sender, Component.text("Reloading WaterMc world config...", NamedTextColor.GREEN));

        MinecraftServer server = ((CraftServer) sender.getServer()).getServer();
        server.galeConfigurations.reloadConfigs(server);
        server.server.reloadCount++;

        Command.broadcastCommandMessage(sender, Component.text("WaterMc world config reload complete.", NamedTextColor.GREEN));
    }
    // Gale end - Gale commands - /gale reload command

    private void doLeafReload(final CommandSender sender) {
        Command.broadcastCommandMessage(sender, Component.text("Reloading WaterMc config...", NamedTextColor.GREEN));

        WaterConfig.reloadAsync(sender);
    }
}
