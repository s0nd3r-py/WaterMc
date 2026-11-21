package me.s0nd3r.watermc.integration.protocol.core;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.NotNull;

public record Context(@NotNull GameProfile profile, Connection connection) {

}
