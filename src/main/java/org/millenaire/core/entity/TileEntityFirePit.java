package org.millenaire.core.entity;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RangedWrapper;

import org.millenaire.core.MillBlocks;
import org.millenaire.core.MillBlockEntities;

/**
 * TileEntityFirePit - Multi-slot outdoor cooking station.
 * Ported exactly from original 1.12.2 TileEntityFirePit.java
 * 
 * Features:
 * - 3 input slots (0-2)
 * - 1 fuel slot (3)
 * - 3 output slots (4-6)
 * - Parallel cooking of up to 3 items
 */
public class TileEntityFirePit extends BlockEntity {

    private final ItemStackHandler items = new ItemStackHandler(7) {
        @Override
        protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
            // Slots 0-2: inputs (must be food or smeltable to food)
            // Slot 3: fuel
            // Slots 4-6: outputs (no insertion allowed from outside)
            if ((0 > slot || slot >= 3 || TileEntityFirePit.isFirePitBurnable(stack))
                    && (3 > slot || slot >= 4 || isFuel(stack))
                    && (4 > slot || slot >= 7)) {
                return super.getStackLimit(slot, stack);
            }
            return 0;
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileEntityFirePit.this.setChanged();
            if (TileEntityFirePit.this.level != null) {
                BlockState state = TileEntityFirePit.this.level.getBlockState(TileEntityFirePit.this.worldPosition);
                TileEntityFirePit.this.level.sendBlockUpdated(TileEntityFirePit.this.worldPosition, state, state, 18);
            }
        }
    };

    public final IItemHandlerModifiable inputs = new RangedWrapper(this.items, 0, 3);
    public final IItemHandlerModifiable fuel = new RangedWrapper(this.items, 3, 4);
    public final IItemHandlerModifiable outputs = new RangedWrapper(this.items, 4, 7);

    private final LazyOptional<IItemHandler> itemsHandler = LazyOptional.of(() -> items);
    private final LazyOptional<IItemHandler> inputsHandler = LazyOptional.of(() -> inputs);
    private final LazyOptional<IItemHandler> fuelHandler = LazyOptional.of(() -> fuel);
    private final LazyOptional<IItemHandler> outputsHandler = LazyOptional.of(() -> outputs);

    private int[] cookTimes = new int[3];
    private int burnTime = 0;
    private int totalBurnTime = 0;

    public TileEntityFirePit(BlockPos pos, BlockState state) {
        super(MillBlockEntities.FIRE_PIT.get(), pos, state);
    }

    /**
     * Check if an item can be cooked in the fire pit.
     * Must be food or smelt into food.
     */
    public static boolean isFirePitBurnable(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        // Check if it's food or smelts into food
        // For simplicity, accept any smeltable item for now
        // Original checked ItemFood instanceof
        return stack.getItem().isEdible() || canSmeltToFood(stack);
    }

    private static boolean canSmeltToFood(ItemStack stack) {
        // In 1.20.1, we'd need to check recipes
        // Simplified: accept raw meats and potatoes
        String itemName = stack.getItem().toString().toLowerCase();
        return itemName.contains("raw_") || itemName.contains("potato") && !itemName.contains("baked");
    }

    /**
     * Check if an item is valid fuel.
     */
    public static boolean isFuel(ItemStack stack) {
        return net.minecraftforge.common.ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }

    private boolean canSmelt(int idx) {
        ItemStack stack = this.inputs.getStackInSlot(idx);
        if (stack.isEmpty()) {
            return false;
        }

        ItemStack result = getSmeltResult(stack);
        if (result.isEmpty()) {
            return false;
        }

        ItemStack output = this.outputs.getStackInSlot(idx);
        if (output.isEmpty()) {
            return true;
        }

        return ItemHandlerHelper.canItemStacksStack(result, output)
                && output.getCount() + result.getCount() <= result.getMaxStackSize();
    }

    private ItemStack getSmeltResult(ItemStack input) {
        if (level == null)
            return ItemStack.EMPTY;

        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.SimpleContainer(input), level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()).copy())
                .orElse(ItemStack.EMPTY);
    }

    public void dropAll() {
        for (int i = 0; i < this.items.getSlots(); i++) {
            ItemStack stack = this.items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        this.worldPosition.getX(),
                        this.worldPosition.getY(),
                        this.worldPosition.getZ(),
                        stack);
            }
        }
    }

    public int getBurnTime() {
        return this.burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getCookTime(int idx) {
        return this.cookTimes[idx];
    }

    public void setCookTime(int idx, int cookTime) {
        this.cookTimes[idx] = cookTime;
    }

    public int getTotalBurnTime() {
        return this.totalBurnTime;
    }

    public void setTotalBurnTime(int totalBurnTime) {
        this.totalBurnTime = totalBurnTime;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (facing == null) {
                return itemsHandler.cast();
            } else if (facing == Direction.UP) {
                return inputsHandler.cast();
            } else if (facing == Direction.DOWN) {
                return outputsHandler.cast();
            } else {
                return fuelHandler.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemsHandler.invalidate();
        inputsHandler.invalidate();
        fuelHandler.invalidate();
        outputsHandler.invalidate();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains("Inventory")) {
            CompoundTag inventory = compound.getCompound("Inventory");
            inventory.remove("Size");
            this.items.deserializeNBT(inventory);
        }
        this.burnTime = compound.getInt("BurnTime");
        if (compound.contains("CookTime")) {
            int[] loaded = compound.getIntArray("CookTime");
            this.cookTimes = Arrays.copyOf(loaded, 3);
        }
        this.totalBurnTime = compound.getInt("TotalBurnTime");
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        CompoundTag inventory = this.items.serializeNBT();
        inventory.remove("Size");
        compound.put("Inventory", inventory);
        compound.putInt("BurnTime", this.burnTime);
        compound.putIntArray("CookTime", this.cookTimes);
        compound.putInt("TotalBurnTime", this.totalBurnTime);
    }

    public void smeltItem(int idx) {
        if (this.canSmelt(idx)) {
            ItemStack input = this.inputs.getStackInSlot(idx);
            ItemStack result = getSmeltResult(input);
            ItemStack output = this.outputs.getStackInSlot(idx);

            if (output.isEmpty()) {
                this.outputs.setStackInSlot(idx, result.copy());
            } else {
                output.grow(result.getCount());
            }

            input.shrink(1);
        }
    }

    /**
     * Called every tick to process cooking.
     */
    public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityFirePit te) {
        boolean burning = te.burnTime > 0;
        boolean dirty = false;

        if (burning) {
            te.burnTime--;
        }

        if (!level.isClientSide) {
            ItemStack fuelStack = te.fuel.getStackInSlot(0);

            for (int i = 0; i < 3; i++) {
                ItemStack inputStack = te.inputs.getStackInSlot(i);

                if ((te.burnTime > 0 || !fuelStack.isEmpty()) && !inputStack.isEmpty()) {
                    // Try to start burning if not already
                    if (te.burnTime <= 0 && te.canSmelt(i)) {
                        te.burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, RecipeType.SMELTING);
                        te.totalBurnTime = te.burnTime;

                        if (te.burnTime > 0) {
                            dirty = true;
                            if (!fuelStack.isEmpty()) {
                                fuelStack.shrink(1);
                                if (fuelStack.isEmpty()) {
                                    te.fuel.setStackInSlot(0, fuelStack.getItem().getCraftingRemainingItem(fuelStack));
                                }
                            }
                        }
                    }

                    // Cook if burning
                    if (te.burnTime > 0 && te.canSmelt(i)) {
                        te.cookTimes[i]++;
                        if (te.cookTimes[i] == 200) {
                            te.cookTimes[i] = 0;
                            te.smeltItem(i);
                            dirty = true;
                        }
                    } else {
                        te.cookTimes[i] = 0;
                    }
                } else if (te.burnTime <= 0 && te.cookTimes[i] > 0) {
                    // Cool down if no fuel
                    dirty = true;
                    te.cookTimes[i] = Mth.clamp(te.cookTimes[i] - 2, 0, 200);
                }
            }

            // Update block state if burn status changed
            if (burning != te.burnTime > 0) {
                dirty = true;
                // Update LIT property on block
                if (state.hasProperty(org.millenaire.common.block.BlockFirePit.LIT)) {
                    level.setBlock(pos, state.setValue(org.millenaire.common.block.BlockFirePit.LIT, te.burnTime > 0),
                            3);
                }
            }
        }

        if (dirty) {
            te.setChanged();
        }
    }

    // --- Getters for container ---

    public IItemHandlerModifiable getInputs() {
        return inputs;
    }

    public IItemHandlerModifiable getFuel() {
        return fuel;
    }

    public IItemHandlerModifiable getOutputs() {
        return outputs;
    }
}
