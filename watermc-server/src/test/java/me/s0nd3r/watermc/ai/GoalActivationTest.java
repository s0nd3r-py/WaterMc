package me.s0nd3r.watermc.ai;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import me.s0nd3r.watermc.core.config.modules.opt.DynamicActivationofBrain;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoalActivationTest {
    @BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    @Test
    void realMobAiStepSlowsOrdinaryGoalsButKeepsEveryTickGoalsAlive() throws Exception {
        boolean oldEnabled = DynamicActivationofBrain.enabled, oldVisibility = DynamicActivationofBrain.visibilityAware;
        boolean oldWater = DynamicActivationofBrain.dontEnableIfInWater;
        boolean oldType = EntityType.ZOMBIE.dabEnabled;
        try {
            DynamicActivationofBrain.enabled = true;
            DynamicActivationofBrain.visibilityAware = false;
            DynamicActivationofBrain.dontEnableIfInWater = false;
            Mob mob = mock(Mob.class, CALLS_REAL_METHODS);
            EntityType.ZOMBIE.dabEnabled = true;
            doReturn(EntityType.ZOMBIE).when(mob).getType();
            when(mob.level()).thenReturn(mock(ServerLevel.class));
            when(mob.getId()).thenReturn(0);
            mob.aware = true;
            mob.activatedPriority = 4;
            GoalSelector goals = new GoalSelector();
            CountingGoal ordinary = new CountingGoal(false, Goal.Flag.LOOK);
            CountingGoal urgent = new CountingGoal(true, Goal.Flag.JUMP);
            goals.addGoal(1, ordinary); goals.addGoal(1, urgent);
            set(mob, "goalSelector", goals); set(mob, "targetSelector", new GoalSelector());
            set(mob, "sensing", mock(Sensing.class)); set(mob, "navigation", mock(PathNavigation.class));
            set(mob, "moveControl", mock(MoveControl.class)); set(mob, "lookControl", mock(LookControl.class));
            set(mob, "jumpControl", mock(JumpControl.class));
            goals.tick();
            Method step = Mob.class.getDeclaredMethod("serverAiStep"); step.setAccessible(true);
            for (int tick = 1; tick <= 20; ++tick) { mob.tickCount = tick; step.invoke(mob); }
            assertEquals(6, ordinary.ticks);
            assertEquals(21, urgent.ticks);
            DynamicActivationofBrain.enabled = false;
            for (int tick = 21; tick <= 40; ++tick) { mob.tickCount = tick; step.invoke(mob); }
            assertEquals(1, mob.activatedPriority);
            assertEquals(16, ordinary.ticks);
            assertEquals(41, urgent.ticks);
        } finally {
            DynamicActivationofBrain.enabled = oldEnabled;
            DynamicActivationofBrain.visibilityAware = oldVisibility;
            DynamicActivationofBrain.dontEnableIfInWater = oldWater;
            EntityType.ZOMBIE.dabEnabled = oldType;
        }
    }

    private static void set(Mob mob, String name, Object value) throws Exception {
        Field field = Mob.class.getDeclaredField(name); field.setAccessible(true); field.set(mob, value);
    }

    private static final class CountingGoal extends Goal {
        private final boolean urgent;
        private int ticks;
        CountingGoal(boolean urgent, Flag flag) { this.urgent = urgent; this.setFlags(EnumSet.of(flag)); }
        @Override public boolean canUse() { return true; }
        @Override public boolean canContinueToUse() { return true; }
        @Override public boolean requiresUpdateEveryTick() { return this.urgent; }
        @Override public void tick() { ++this.ticks; }
    }
}
