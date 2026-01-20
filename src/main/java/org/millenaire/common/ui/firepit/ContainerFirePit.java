package org.millenaire.common.ui.firepit;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.millenaire.core.entity.TileEntityFirePit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Container/Menu for the Fire Pit block.
 * Handles 3 input slots, 1 fuel slot, and 3 output slots.
 */
public class ContainerFirePit extends AbstractContainerMenu {
    private static final int[][] INPUT_POSITIONS = { { 56, 8 }, { 44, 28 }, { 56, 48 } };
    private static final int[][] OUTPUT_POSITIONS = { { 104, 8 }, { 116, 28 }, { 104, 48 } };
    private static final int[] FUEL_POSITION = { 80, 70 };
    private static final int[] INV_POSITION = { 8, 93 };

    private final int inputStart;
    private final int inputEnd;
    private final int fuelStart;
    private final int fuelEnd;
    private final int outputStart;
    private final int outputEnd;
    private final int inventoryStart;
    private final int inventoryEnd;
    private final int hotbarStart;
    private final int hotbarEnd;

    private final TileEntityFirePit firePit;
    private final ContainerData data;

    public ContainerFirePit(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory,
            TileEntityFirePit firePit, ContainerData data) {
        super(menuType, containerId);
        this.firePit = firePit;
        this.data = data;

        // Add data slots for syncing cook time and burn time
        this.addDataSlots(data);

        // Input slots
        this.inputStart = this.slots.size();
        for (int i = 0; i < 3; i++) {
            this.addSlot(new SlotFirePitInput(firePit.getInputs(), i, INPUT_POSITIONS[i][0], INPUT_POSITIONS[i][1]));
        }
        this.inputEnd = this.slots.size();

        // Fuel slot
        this.fuelStart = this.slots.size();
        this.addSlot(new SlotFirePitFuel(firePit.getFuel(), 0, FUEL_POSITION[0], FUEL_POSITION[1]));
        this.fuelEnd = this.slots.size();

        // Output slots
        this.outputStart = this.slots.size();
        for (int i = 0; i < 3; i++) {
            this.addSlot(new SlotFirePitOutput(playerInventory.player, firePit.getOutputs(), i, OUTPUT_POSITIONS[i][0],
                    OUTPUT_POSITIONS[i][1]));
        }
        this.outputEnd = this.slots.size();

        // Player inventory
        this.inventoryStart = this.slots.size();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, INV_POSITION[0] + col * 18,
                        INV_POSITION[1] + row * 18));
            }
        }
        this.inventoryEnd = this.slots.size();

        // Hotbar
        this.hotbarStart = this.slots.size();
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, INV_POSITION[0] + i * 18, INV_POSITION[1] + 54 + 4));
        }
        this.hotbarEnd = this.slots.size();
    }

    private static boolean inRange(int index, int start, int end) {
        return start <= index && index < end;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            original = stackInSlot.copy();

            if (inRange(index, this.outputStart, this.outputEnd)) {
                // From output to player inventory
                if (!this.moveItemStackTo(stackInSlot, this.inventoryStart, this.hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stackInSlot, original);
            } else if (inRange(index, this.inventoryStart, this.hotbarEnd)) {
                // From player inventory
                if (TileEntityFirePit.isFirePitBurnable(stackInSlot)) {
                    // To input slots
                    if (!this.moveItemStackTo(stackInSlot, this.inputStart, this.inputEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (AbstractFurnaceBlockEntity.isFuel(stackInSlot)) {
                    // To fuel slot
                    if (!this.moveItemStackTo(stackInSlot, this.fuelStart, this.fuelEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (inRange(index, this.inventoryStart, this.inventoryEnd)) {
                    // From main inventory to hotbar
                    if (!this.moveItemStackTo(stackInSlot, this.hotbarStart, this.hotbarEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (inRange(index, this.hotbarStart, this.hotbarEnd)) {
                    // From hotbar to main inventory
                    if (!this.moveItemStackTo(stackInSlot, this.inventoryStart, this.inventoryEnd, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(stackInSlot, this.inventoryStart, this.hotbarEnd, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == original.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stackInSlot);
        }

        return original;
    }

    /**
     * Get cook progress for a slot (0-3).
     */
    public int getCookProgress(int slot) {
        if (slot >= 0 && slot < 3) {
            return this.data.get(slot);
        }
        return 0;
    }

    /**
     * Get remaining burn time.
     */
    public int getBurnTime() {
        return this.data.get(3);
    }

    /**
     * Get total burn time for current fuel.
     */
    public int getTotalBurnTime() {
        return this.data.get(4);
    }

    /**
     * Check if the fire pit is currently burning.
     */
    public boolean isBurning() {
        return getBurnTime() > 0;
    }

    /**
     * Get the fire pit block entity.
     */
    public TileEntityFirePit getFirePit() {
        return this.firePit;
    }
}
