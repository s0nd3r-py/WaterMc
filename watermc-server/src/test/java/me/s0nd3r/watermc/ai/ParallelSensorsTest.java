package me.s0nd3r.watermc.ai;

import net.minecraft.world.entity.ai.sensing.NearestItemSensor;
import net.minecraft.world.entity.ai.sensing.PlayerSensor;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Параллельно выполняются только проверенные ванильные сенсоры; выключено по умолчанию. */
class ParallelSensorsTest {

    @BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void onlyAuditedVanillaSensorsRunInParallel() {
        assertTrue(ParallelSensors.audited(new PlayerSensor()));
        assertTrue(ParallelSensors.audited(new NearestItemSensor()));
        assertFalse(ParallelSensors.audited(new TemptingSensor(e -> false)), "tempting uses the shared temptation lookup");
    }

    @Test
    void disabledByDefault() {
        assertFalse(me.s0nd3r.watermc.core.config.modules.async.ParallelSensorPhase.enabled);
    }
}
