package me.s0nd3r.watermc.integration.protocol.jade.provider;

import net.minecraft.nbt.CompoundTag;
import me.s0nd3r.watermc.integration.protocol.jade.accessor.Accessor;

public interface ServerDataProvider<T extends Accessor<?>> extends JadeProvider {
    void appendServerData(CompoundTag data, T accessor);
}
