package me.s0nd3r.watermc.integration.protocol.jade.provider;

import me.s0nd3r.watermc.integration.protocol.jade.accessor.Accessor;
import me.s0nd3r.watermc.integration.protocol.jade.util.ViewGroup;

import java.util.List;

public interface ServerExtensionProvider<T> extends JadeProvider {
    List<ViewGroup<T>> getGroups(Accessor<?> request);
}