package me.s0nd3r.watermc.integration.protocol.core.invoker;

import me.s0nd3r.watermc.integration.protocol.core.LeavesProtocol;

import java.lang.reflect.Method;

public class EmptyInvokerHolder<T> extends AbstractInvokerHolder<T> {
    public EmptyInvokerHolder(LeavesProtocol owner, Method invoker, T handler) {
        super(owner, invoker, handler, null);
    }

    public void invoke() {
        invoke0(false);
    }
}
