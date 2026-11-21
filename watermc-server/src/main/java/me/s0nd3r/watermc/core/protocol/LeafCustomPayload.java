package me.s0nd3r.watermc.core.protocol;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface LeafCustomPayload extends CustomPacketPayload {

    @Override
    Type<? extends LeafCustomPayload> type();
}
