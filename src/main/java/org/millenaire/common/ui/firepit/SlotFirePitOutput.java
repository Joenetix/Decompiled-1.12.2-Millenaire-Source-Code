package org.millenaire.common.ui.firepit;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

/**
 * Slot for output items in the fire pit (cooked items).
 * Handles experience orb spawning when items are taken.
 */
public class SlotFirePitOutput extends SlotItemHandler {
    private final Player player;
    private int removeCount;

    public SlotFirePitOutput(Player player, IItemHandler handler, int slotIndex, int xPosition, int yPosition) {
        super(handler, slotIndex, xPosition, yPosition);
        this.player = player;
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }
        return super.remove(amount);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return false; // Output slots don't accept items
    }

    /**
     * Called when items are removed from the slot.
     */
    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(this.player.level(), this.player, this.removeCount);

        if (!this.player.level().isClientSide() && this.player.level() instanceof ServerLevel serverLevel) {
            // Calculate experience from smelting
            float xpAmount = getExperienceForStack(stack);
            int xpInt = Mth.floor(xpAmount);
            float xpFrac = Mth.frac(xpAmount);

            if (xpFrac != 0.0F && Math.random() < xpFrac) {
                xpInt++;
            }

            ExperienceOrb.award(serverLevel, this.player.position(), xpInt);
        }

        this.removeCount = 0;
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        this.removeCount += amount;
        this.checkTakeAchievements(stack);
    }

    @Override
    public void onTake(Player thePlayer, @Nonnull ItemStack stack) {
        this.checkTakeAchievements(stack);
        super.onTake(thePlayer, stack);
    }

    /**
     * Get experience value for a smelted item.
     * Fire pit uses simplified experience values.
     */
    private float getExperienceForStack(ItemStack stack) {
        // Simple experience calculation - 0.1 XP per item cooked
        return this.removeCount * 0.1f;
    }
}
