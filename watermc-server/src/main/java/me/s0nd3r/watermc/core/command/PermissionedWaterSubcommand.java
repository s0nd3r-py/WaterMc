package me.s0nd3r.watermc.core.command;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public abstract class PermissionedWaterSubcommand implements WaterSubcommand {

    private final Permission permission;

    protected PermissionedWaterSubcommand(Permission permission) {
        this.permission = permission;
    }

    protected PermissionedWaterSubcommand(String permission, PermissionDefault permissionDefault) {
        this(new Permission(permission, permissionDefault));
    }

    @Override
    public boolean testPermission(CommandSender sender) {
        return sender.hasPermission(this.permission);
    }

    @Override
    public Permission getPermission() {
        return this.permission;
    }
}
