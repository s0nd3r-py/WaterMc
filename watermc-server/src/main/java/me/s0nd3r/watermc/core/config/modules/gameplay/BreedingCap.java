package me.s0nd3r.watermc.core.config.modules.gameplay;

import me.s0nd3r.watermc.core.config.ConfigModules;
import me.s0nd3r.watermc.core.config.EnumConfigCategory;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;

/** Breeding cap; idea from ServerCore (Wesley1808, MIT), reimplemented. */
public class BreedingCap extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.GAMEPLAY.getBaseKeyName() + ".breeding-cap";
    }

    public static boolean enabled = false;
    public static int limit = 32;
    public static int range = 64;

    @Override
    public void onLoaded() {
        final String base = getBasePath();
        enabled = config.getBoolean(base + ".enabled", enabled, config.pickStringRegionBased("""
            Animals and villagers stop breeding when this many of their kind are already within range, so
            automatic breeding farms cannot pile up thousands of mobs. Blocked parents get the normal cooldown.""", """
            范围内同种生物达到上限时禁止繁殖."""));
        limit = Math.max(1, config.getInt(base + ".limit", limit, "Mobs of the same type within range that block breeding."));
        range = Math.clamp(config.getInt(base + ".range", range, "Horizontal and vertical radius in blocks, 1-128."), 1, 128);
    }

    /** True (and both parents get the post-breeding cooldown) when the parent's kind is at the cap around it. */
    public static boolean blocks(final AgeableMob parent, final AgeableMob partner) {
        if (!enabled) {
            return false;
        }
        final EntityType<?> type = parent.getType();
        final AABB area = parent.getBoundingBox().inflate(range);
        final int count = type == EntityType.FROG
            ? parent.level().getEntities((Entity) null, area, e -> e.getType() == EntityType.FROG || e.getType() == EntityType.TADPOLE).size()
            : parent.level().getEntities(type, area, EntitySelector.NO_SPECTATORS).size();
        if (count < limit) {
            return false;
        }
        parent.setAge(6000);
        partner.setAge(6000);
        return true;
    }
}
