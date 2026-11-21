package me.s0nd3r.watermc.integration.protocol.core.invoker;

import me.s0nd3r.watermc.integration.protocol.core.LeavesProtocol;
import me.s0nd3r.watermc.integration.protocol.core.ProtocolHandler;

import java.lang.reflect.Method;

public class InitInvokerHolder extends AbstractInvokerHolder<ProtocolHandler.Init> {
    public InitInvokerHolder(LeavesProtocol owner, Method invoker, ProtocolHandler.Init handler) {
        super(owner, invoker, handler, null);
    }

    public void invoke() {
        invoke0(true);
    }
}
