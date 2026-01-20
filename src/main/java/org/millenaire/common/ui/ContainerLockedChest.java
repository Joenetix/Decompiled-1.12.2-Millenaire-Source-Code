package org.millenaire.common.ui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.millenaire.common.village.Building;

import javax.annotation.Nullable;

/**
 * Container for locked chests in Millenaire buildings.
 * Can be in locked mode (view only) or unlocked mode (full access).
 */
public class ContainerLockedChest extends AbstractContainerMenu {
    private final Container lowerChestInventory;
    private final int numRows;
    private final boolean locked;

    public ContainerLockedChest(@Nullable MenuType<?> menuType, int containerId, Container playerInventory,
            Container chestInventory, Player player, Building building, boolean locked) {
        super(menuType, containerId);
        this.locked = locked;
        this.lowerChestInventory = chestInventory;
        this.numRows = chestInventory.getContainerSize() / 9;
        chestInventory.startOpen(player);
        int i = (this.numRows - 4) * 18;

        // Add chest slots
        for (int j = 0; j < this.numRows; j++) {
            for (int k = 0; k < 9; k++) {
                if (locked) {
                    this.addSlot(new LockedSlot(chestInventory, k + j * 9, 8 + k * 18, 18 + j * 18));
                } else {
                    this.addSlot(new CachedSlot(chestInventory, k + j * 9, 8 + k * 18, 18 + j * 18, building));
                }
            }
        }

        // Add player inventory
        for (int l = 0; l < 3; l++) {
            for (int j1 = 0; j1 < 9; j1++) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
            }
        }

        // Add hotbar
        for (int i1 = 0; i1 < 9; i1++) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.lowerChestInventory.stillValid(player);
    }

    public Container getLowerChestInventory() {
        return this.lowerChestInventory;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.lowerChestInventory.stopOpen(player);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot instanceof LockedSlot && this.locked) {
                return; // Prevent interaction with locked slots
            }
        }
        super.clicked(slotId, dragType, clickTypeIn, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < this.numRows * 9) {
                // From chest to player inventory
                if (!this.moveItemStackTo(itemstack1, this.numRows * 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to chest
                if (!this.moveItemStackTo(itemstack1, 0, this.numRows * 9, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    /**
     * A slot that invalidates the building's inventory cache when changed.
     */
    public static class CachedSlot extends Slot {
        final Building building;

        public CachedSlot(Container inventory, int index, int xPosition, int yPosition, Building building) {
            super(inventory, index, xPosition, yPosition);
            this.building = building;
        }

        @Override
        public void setChanged() {
            super.setChanged();
            if (this.building != null) {
                this.building.invalidateInventoryCache();
            }
        }
    }

    /**
     * A slot that cannot be modified by the player.
     */
    public static class LockedSlot extends Slot {
        public LockedSlot(Container inventory, int index, int xPosition, int yPosition) {
            super(inventory, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
