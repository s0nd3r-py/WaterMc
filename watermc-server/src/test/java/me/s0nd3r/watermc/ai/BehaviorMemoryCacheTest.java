package me.s0nd3r.watermc.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** The cached memory precondition always equals a fresh check, across set/erase/clear and brain swaps. */
class BehaviorMemoryCacheTest {

    @BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void cacheMatchesDirectCheck() throws Exception {
        final MemoryModuleType<Boolean> a = MemoryModuleType.IS_PANICKING;
        final MemoryModuleType<net.minecraft.util.Unit> b = MemoryModuleType.IS_PREGNANT;
        Brain<LivingEntity> brain = new Brain<>(List.of(a, b), List.of(), ImmutableList.of(), () -> null);
        final LivingEntity owner = mock(LivingEntity.class);
        final Brain<?>[] current = {brain};
        doAnswer(inv -> current[0]).when(owner).getBrain();
        final Behavior<LivingEntity> behavior = new Behavior<>(ImmutableMap.of(a, MemoryStatus.VALUE_PRESENT, b, MemoryStatus.VALUE_ABSENT)) {
        };
        final Method check = Behavior.class.getDeclaredMethod("hasRequiredMemories", LivingEntity.class);
        check.setAccessible(true);

        final Random random = new Random(3);
        for (int i = 0; i < 2000; i++) {
            switch (random.nextInt(6)) {
                case 0 -> brain.setMemory(a, true);
                case 1 -> brain.eraseMemory(a);
                case 2 -> brain.setMemory(b, net.minecraft.util.Unit.INSTANCE);
                case 3 -> brain.eraseMemory(b);
                case 4 -> brain.clearMemories();
                default -> current[0] = brain = brain.copyWithoutBehaviors();
            }
            final boolean expected = brain.checkMemory(a, MemoryStatus.VALUE_PRESENT) && brain.checkMemory(b, MemoryStatus.VALUE_ABSENT);
            assertEquals(expected, check.invoke(behavior, owner), "step " + i);
        }
    }
}
