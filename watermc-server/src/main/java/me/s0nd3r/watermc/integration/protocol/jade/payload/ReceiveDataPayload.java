package me.s0nd3r.watermc.integration.protocol.jade.payload;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import me.s0nd3r.watermc.integration.protocol.core.LeavesCustomPayload;
import me.s0nd3r.watermc.integration.protocol.jade.JadeProtocol;

public record ReceiveDataPayload(CompoundTag tag) implements LeavesCustomPayload {

    @ID
    private static final Identifier PACKET_RECEIVE_DATA = JadeProtocol.id("receive_data");

    @Codec
    private static final StreamCodec<FriendlyByteBuf, ReceiveDataPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.COMPOUND_TAG, ReceiveDataPayload::tag, ReceiveDataPayload::new
    );
}
