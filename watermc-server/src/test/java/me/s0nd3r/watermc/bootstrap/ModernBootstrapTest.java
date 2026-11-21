package me.s0nd3r.watermc.bootstrap;

import com.mojang.serialization.Dynamic;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModernBootstrapTest {
    @Test void actualMinecraftBootstrapAndCurrentDataLeaveDfuUninitialised() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        LazyDataFixer fixer = assertInstanceOf(LazyDataFixer.class, DataFixers.getDataFixer());
        assertFalse(fixer.isInitialized());
        int version = net.minecraft.SharedConstants.getCurrentVersion().dataVersion().version();
        Dynamic<?> input = new Dynamic<>(NbtOps.INSTANCE, new CompoundTag());
        assertSame(input, fixer.update(References.CHUNK, input, version, version));
        DataFixers.optimize(Set.of(References.CHUNK)).join();
        assertFalse(fixer.isInitialized());
    }
}
