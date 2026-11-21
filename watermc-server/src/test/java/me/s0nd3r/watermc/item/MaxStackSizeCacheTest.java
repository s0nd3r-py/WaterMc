package me.s0nd3r.watermc.item;

import java.util.List;
import java.util.Random;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Кэш максимального размера стака всегда совпадает с прямым чтением компонента, включая пустые стаки. */
class MaxStackSizeCacheTest {

    @BeforeAll
    static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
    }

    private static int direct(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    @Test
    void cacheFollowsEveryMutationPath() {
        Random random = new Random(11);
        List<ItemStack> stacks = List.of(new ItemStack(Items.STONE), new ItemStack(Items.DIAMOND_SWORD), new ItemStack(Items.ENDER_PEARL, 3), new ItemStack(Items.APPLE, 10));
        for (ItemStack stack : stacks) {
            for (int i = 0; i < 2000; i++) {
                switch (random.nextInt(6)) {
                    case 0 -> stack.set(DataComponents.MAX_STACK_SIZE, 1 + random.nextInt(99));
                    case 1 -> stack.remove(DataComponents.MAX_STACK_SIZE);
                    case 2 -> stack.applyComponents(DataComponentPatch.builder().set(DataComponents.MAX_STACK_SIZE, 1 + random.nextInt(99)).build());
                    case 3 -> stack.applyComponents(DataComponentPatch.builder().remove(DataComponents.MAX_STACK_SIZE).build());
                    case 4 -> stack.restorePatch(DataComponentPatch.EMPTY);
                    default -> stack.set(DataComponents.REPAIR_COST, random.nextInt(10));
                }
                assertEquals(direct(stack), stack.getMaxStackSize());
                ItemStack copy = stack.copy();
                assertEquals(direct(copy), copy.getMaxStackSize(), "copies compute their own cache");
            }
        }
    }

    @Test
    void emptiedPatchStaysWritableAndCopiesStayIndependent() {
        ItemStack stack = new ItemStack(Items.APPLE);
        stack.set(DataComponents.REPAIR_COST, 3);
        stack.set(DataComponents.REPAIR_COST, new ItemStack(Items.APPLE).get(DataComponents.REPAIR_COST)); // back to the prototype value: the patch empties
        assertTrue(stack.getComponentsPatch().isEmpty());
        assertTrue(ItemStack.isSameItemSameComponents(stack, new ItemStack(Items.APPLE)));
        ItemStack copy = stack.copy();
        stack.set(DataComponents.REPAIR_COST, 5);
        copy.set(DataComponents.REPAIR_COST, 7);
        assertEquals(5, stack.get(DataComponents.REPAIR_COST));
        assertEquals(7, copy.get(DataComponents.REPAIR_COST));
    }

    @Test
    void emptyStacksKeepTheVanillaAnswer() {
        assertEquals(1, ItemStack.EMPTY.getMaxStackSize());
        ItemStack stone = new ItemStack(Items.STONE, 5);
        stone.setCount(0);
        assertTrue(stone.isEmpty());
        assertEquals(direct(stone), stone.getMaxStackSize());
        assertEquals(1, stone.getMaxStackSize());
    }
}
