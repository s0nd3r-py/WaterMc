package me.s0nd3r.watermc.integration.protocol.core.invoker;

import net.minecraft.network.FriendlyByteBuf;
import me.s0nd3r.watermc.integration.protocol.core.IdentifierSelector;
import me.s0nd3r.watermc.integration.protocol.core.LeavesProtocol;
import me.s0nd3r.watermc.integration.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class BytebufReceiverInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.BytebufReceiver> {
    public BytebufReceiverInvokerHolder(LeavesProtocol owner, Method invoker, ProtocolHandler.BytebufReceiver handler) {
        super(owner, invoker, handler, null, handler.stage().identifier(), FriendlyByteBuf.class);
    }

    public boolean invoke(IdentifierSelector selector, FriendlyByteBuf buf) {
        return invoke0(false, selector.select(handler.stage()), buf) instanceof Boolean b && b;
    }
}
