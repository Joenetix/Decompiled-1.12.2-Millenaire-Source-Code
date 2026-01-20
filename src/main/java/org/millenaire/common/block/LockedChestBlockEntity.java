package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.millenaire.core.MillBlockEntities;
import org.millenaire.common.world.MillWorldData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import org.millenaire.common.utilities.Point;

/**
 * BlockEntity for Locked Chests used by villages.
 * Can be locked to specific villages/buildings and tracks ownership.
 */
public class LockedChestBlockEntity extends BlockEntity implements Container, MenuProvider {

    private static final int INVENTORY_SIZE = 27; // Same as vanilla chest

    // Inventory storage
    private final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> inventoryCapability = LazyOptional.of(() -> inventory);

    // Ownership
    private BlockPos ownerVillagePos = null;
    private String ownerBuildingKey = null;
    private boolean locked = false;

    // Display
    private Component customName = null;

    public LockedChestBlockEntity(BlockPos pos, BlockState state) {
        super(MillBlockEntities.LOCKED_CHEST.get(), pos, state);
    }

    // Container implementation
    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return inventory.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        if (locked && !isOwner(player)) {
            return false;
        }
        return level != null && level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return customName != null ? customName : Component.translatable("container.millenaire.locked_chest");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return ChestMenu.threeRows(containerId, playerInventory, this);
    }

    // Ownership methods
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        setChanged();
    }

    public void setOwner(BlockPos villagePos, String buildingKey) {
        this.ownerVillagePos = villagePos;
        this.ownerBuildingKey = buildingKey;
        setChanged();
    }

    public BlockPos getOwnerVillagePos() {
        return ownerVillagePos;
    }

    public String getOwnerBuildingKey() {
        return ownerBuildingKey;
    }

    public boolean isOwner(Player player) {
        // Access permissions based on reputation or exact ownership
        if (ownerVillagePos != null) {
            MillWorldData mw = MillWorldData.get(level);
            if (mw != null) {
                org.millenaire.common.world.UserProfile profile = mw.getProfile(player.getUUID(),
                        player.getScoreboardName());
                // Check if reputation >= FRIEND (assume 640 = 10 Denier Argent for now)
                // TODO: Define constants for reputation levels
                if (profile.getReputation(new Point(ownerVillagePos)) >= 640) { // Friend status
                    return true;
                }
            }
        }

        // Creative players or unlocked chests always accessible
        if (player.isCreative() || !locked) {
            return true;
        }

        return false;
    }

    public void setCustomName(Component name) {
        this.customName = name;
        setChanged();
    }

    // Capability handling
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return inventoryCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inventoryCapability.invalidate();
    }

    // NBT serialization
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putBoolean("Locked", locked);
        if (ownerVillagePos != null) {
            tag.putLong("OwnerVillagePos", ownerVillagePos.asLong());
        }
        if (ownerBuildingKey != null) {
            tag.putString("OwnerBuilding", ownerBuildingKey);
        }
        if (customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(customName));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        locked = tag.getBoolean("Locked");
        if (tag.contains("OwnerVillagePos")) {
            ownerVillagePos = BlockPos.of(tag.getLong("OwnerVillagePos"));
        }
        if (tag.contains("OwnerBuilding")) {
            ownerBuildingKey = tag.getString("OwnerBuilding");
        }
        if (tag.contains("CustomName")) {
            customName = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
    }
}
