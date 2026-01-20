package org.millenaire.common.ui.firepit;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.millenaire.core.entity.TileEntityFirePit;

import javax.annotation.Nonnull;

/**
 * Slot for input items in the fire pit (items to be cooked).
 */
public class SlotFirePitInput extends SlotItemHandler {

    public SlotFirePitInput(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return TileEntityFirePit.isFirePitBurnable(stack);
    }
}
