package org.millenaire.common.ui.firepit;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * Slot for fuel items in the fire pit.
 */
public class SlotFirePitFuel extends SlotItemHandler {

    public SlotFirePitFuel(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public int getMaxStackSize(@Nonnull ItemStack stack) {
        // Buckets can only stack to 1
        return isBucket(stack) ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return AbstractFurnaceBlockEntity.isFuel(stack) || isBucket(stack);
    }

    /**
     * Check if the stack is a bucket (used for lava bucket fuel).
     */
    private static boolean isBucket(ItemStack stack) {
        return stack.getItem() == net.minecraft.world.item.Items.BUCKET ||
                stack.getItem() == net.minecraft.world.item.Items.LAVA_BUCKET;
    }
}
