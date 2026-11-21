package me.s0nd3r.watermc.integration.protocol.core.invoker;

import me.s0nd3r.watermc.integration.protocol.core.IdentifierSelector;
import me.s0nd3r.watermc.integration.protocol.core.LeavesCustomPayload;
import me.s0nd3r.watermc.integration.protocol.core.LeavesProtocol;
import me.s0nd3r.watermc.integration.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class PayloadReceiverInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.PayloadReceiver> {
    public PayloadReceiverInvokerHolder(LeavesProtocol owner, Method invoker, ProtocolHandler.PayloadReceiver handler) {
        super(owner, invoker, handler, null, handler.stage().identifier(), handler.payload());
    }

    public void invoke(IdentifierSelector selector, LeavesCustomPayload payload) {
        invoke0(false, selector.select(handler.stage()), payload);
    }
}
