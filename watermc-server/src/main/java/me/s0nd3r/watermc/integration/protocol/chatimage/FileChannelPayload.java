package me.s0nd3r.watermc.integration.protocol.chatimage;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import me.s0nd3r.watermc.integration.protocol.core.LeavesCustomPayload;

public record FileChannelPayload(String message) implements LeavesCustomPayload {

    @ID
    private static final Identifier ID = ChatImageProtocol.id("get_file_channel");

    @Codec
    private static final StreamCodec<RegistryFriendlyByteBuf, FileChannelPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, FileChannelPayload::message, FileChannelPayload::new
    );
}