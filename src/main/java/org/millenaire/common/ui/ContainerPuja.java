package org.millenaire.common.ui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import org.millenaire.core.MillItems;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

import javax.annotation.Nullable;

/**
 * Container for Puja/Sacrifice shrine UI.
 * Handles offerings, money, and tool slots.
 */
public class ContainerPuja extends AbstractContainerMenu {
    PujaSacrifice shrine;
    ToolSlot slotTool;

    public ContainerPuja(@Nullable MenuType<?> menuType, int containerId, Player player, Building temple) {
        super(menuType, containerId);
        try {
            this.shrine = temple.pujas;
            this.slotTool = new ToolSlot(temple.pujas, 4, 86, 37);

            // Offering slot
            this.addSlot(new OfferingSlot(temple.pujas, 0, 26, 19));

            // Money slots
            this.addSlot(new MoneySlot(temple.pujas, 1, 8, 55));
            this.addSlot(new MoneySlot(temple.pujas, 2, 26, 55));
            this.addSlot(new MoneySlot(temple.pujas, 3, 44, 55));

            // Tool slot
            this.addSlot(this.slotTool);

            // Player inventory
            for (int i = 0; i < 3; i++) {
                for (int k = 0; k < 9; k++) {
                    this.addSlot(new Slot(player.getInventory(), k + i * 9 + 9, 8 + k * 18, 106 + i * 18));
                }
            }

            // Hotbar
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(player.getInventory(), j, 8 + j * 18, 164));
            }
        } catch (Exception e) {
            MillLog.printException("Exception in ContainerPuja(): ", e);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int stackID) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(stackID);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (stackID == 4) {
                // From tool slot to player inventory
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (stackID > 4) {
                // From player inventory
                Item item = itemstack1.getItem();

                if (item != MillItems.DENIER.get() &&
                        item != MillItems.DENIER_ARGENT.get() &&
                        item != MillItems.DENIER_OR.get()) {

                    if (this.shrine.getOfferingValue(itemstack1) > 0) {
                        // To offering slot
                        if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        // Check if valid for tool slot
                        if (!this.slotTool.mayPlace(itemstack1)) {
                            return ItemStack.EMPTY;
                        }
                        if (!this.moveItemStackTo(itemstack1, 4, 5, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                } else {
                    // Money to money slots
                    if (!this.moveItemStackTo(itemstack1, 1, 4, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                // From shrine slots to player inventory
                if (!this.moveItemStackTo(itemstack1, 5, 41, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.getCount() == 0) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    /**
     * Slot for money items (denier, denier argent, denier or).
     */
    public static class MoneySlot extends Slot {
        PujaSacrifice shrine;

        public MoneySlot(PujaSacrifice shrine, int slot, int x, int y) {
            super(shrine, slot, x, y);
            this.shrine = shrine;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            Item item = stack.getItem();
            return item == MillItems.DENIER.get() ||
                    item == MillItems.DENIER_OR.get() ||
                    item == MillItems.DENIER_ARGENT.get();
        }

        @Override
        public void setChanged() {
            if (this.shrine.temple != null && this.shrine.temple.world != null &&
                    !this.shrine.temple.world.isClientSide()) {
                this.shrine.temple.getTownHall().requestSave("Puja money slot changed");
            }
            super.setChanged();
        }
    }

    /**
     * Slot for offering items.
     */
    public static class OfferingSlot extends Slot {
        PujaSacrifice shrine;

        public OfferingSlot(PujaSacrifice shrine, int slot, int x, int y) {
            super(shrine, slot, x, y);
            this.shrine = shrine;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.shrine.getOfferingValue(stack) > 0;
        }

        @Override
        public void setChanged() {
            if (this.shrine.temple != null && this.shrine.temple.world != null &&
                    !this.shrine.temple.world.isClientSide()) {
                this.shrine.temple.getTownHall().requestSave("Puja offering slot changed");
            }
            super.setChanged();
        }
    }

    /**
     * Slot for tool/weapon/armor to be enchanted.
     */
    public static class ToolSlot extends Slot {
        PujaSacrifice shrine;

        public ToolSlot(PujaSacrifice shrine, int slot, int x, int y) {
            super(shrine, slot, x, y);
            this.shrine = shrine;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            Item item = stack.getItem();
            if (this.shrine.type == PujaSacrifice.MAYAN) {
                return item instanceof SwordItem || item instanceof ArmorItem ||
                        item instanceof BowItem || item instanceof AxeItem;
            } else {
                return item instanceof ShovelItem || item instanceof AxeItem ||
                        item instanceof PickaxeItem;
            }
        }

        @Override
        public void setChanged() {
            this.shrine.calculateOfferingsNeeded();
            if (this.shrine.temple != null && this.shrine.temple.world != null &&
                    !this.shrine.temple.world.isClientSide()) {
                this.shrine.temple.getTownHall().requestSave("Puja tool slot changed");
            }
            super.setChanged();
        }
    }
}
