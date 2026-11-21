package me.s0nd3r.watermc.integration.protocol.jade.provider;

import net.minecraft.resources.Identifier;

public interface JadeProvider {

    Identifier getUid();

    default int getDefaultPriority() {
        return 0;
    }
}
