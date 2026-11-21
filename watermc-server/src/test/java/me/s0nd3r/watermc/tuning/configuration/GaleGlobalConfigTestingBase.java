package me.s0nd3r.watermc.tuning.configuration;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/** Initialise the inherited Gale configuration for tests that do not start a server. */
public final class GaleGlobalConfigTestingBase {
    private GaleGlobalConfigTestingBase() {}

    public static void setup(ConfigurationNode node) {
        if (TuningGlobalConfiguration.get() == null) {
            try {
                TuningGlobalConfiguration.set(node.require(TuningGlobalConfiguration.class));
            } catch (SerializationException failure) {
                throw new RuntimeException(failure);
            }
        }
    }
}
