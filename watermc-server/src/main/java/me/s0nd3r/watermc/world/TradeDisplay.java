package me.s0nd3r.watermc.world;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/**
 * Item a villager shows in its hand while a player holds trade items nearby ({@code ShowTradesToPlayer}).
 *
 * <p>It is a copy of the trade result, placed in the main hand with drop chance 0 so it never drops on death.
 * Since 1.21.6 shears remove shearable equipment (saddles, horse armour, carpets) from any mob regardless of
 * drop chance, and conversion into a zombie villager keeps equipment, so the display copy could be taken for
 * free and repeatedly. The display is recognised exactly as vanilla creates it: a non-empty main hand with drop
 * chance 0.</p>
 */
public final class TradeDisplay {
    private TradeDisplay() {}

    public static boolean isShowing(Mob mob) {
        return !mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()
            && mob.getDropChances().byEquipment(EquipmentSlot.MAINHAND) <= 0.0F;
    }

    /** Removes the display copy; real items (drop chance above 0) are left alone. */
    public static void clear(Mob mob) {
        if (isShowing(mob)) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }
}
