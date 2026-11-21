package me.s0nd3r.watermc.core.command;

import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.util.permissions.CraftDefaultPermissions;

import java.util.HashMap;
import java.util.Map;

public final class WaterCommands {

    public static final String COMMAND_BASE_PERM = CraftDefaultPermissions.LEAF_ROOT + ".command";

    private WaterCommands() {
    }

    private static final Map<String, Command> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put(WaterCommand.COMMAND_LABEL, new WaterCommand());
    }

    public static void registerCommands(final MinecraftServer server) {
        COMMANDS.forEach((s, command) -> server.server.getCommandMap().register(s, "watermc", command));
    }
}
