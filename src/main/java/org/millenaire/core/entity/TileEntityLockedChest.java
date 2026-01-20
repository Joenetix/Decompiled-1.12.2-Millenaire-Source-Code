package org.millenaire.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;
import org.millenaire.common.world.MillWorldData;

public class TileEntityLockedChest extends ChestBlockEntity {

    public Point buildingPos = null;
    public boolean loaded = false;
    public boolean serverDevMode = false;
    private Point posPoint;

    public TileEntityLockedChest(BlockPos pos, BlockState state) {
        super(pos, state);
        this.posPoint = new Point(pos);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("buildingPos")) {
            buildingPos = Point.read(tag, "buildingPos");
        }
        if (tag.contains("serverDevMode")) {
            serverDevMode = tag.getBoolean("serverDevMode");
        }
        loaded = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (buildingPos != null) {
            buildingPos.write(tag, "buildingPos");
        }
        tag.putBoolean("serverDevMode", serverDevMode);
    }

    @Override
    public boolean canOpen(Player player) {
        return !isLockedFor(player) && super.canOpen(player);
    }

    public boolean isLockedFor(Player player) {
        if (player == null) {
            return true;
        }

        if (level != null && level.isClientSide && !loaded) {
            return true;
        }

        if (buildingPos == null) {
            return false;
        }

        // Dev mode check for permission omitted for now as Config is not fully ported
        // or I'd assume false
        if (serverDevMode) {
            return false;
        }

        if (level != null && !level.isClientSide) {
            MillWorldData mw = MillWorldData.get(level);
            if (mw == null) {
                return true;
            }
            // TODO: Use correct method to get building when ported
            // Building building = mw.getBuilding(buildingPos);
            // return building != null && building.lockedForPlayer(player);
            return false; // Stub until Building.lockedForPlayer is available
        }

        return false;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        // TODO: Use ContainerLockedChest when ported
        // Return vanilla 3-row chest menu for backward compatibility if unlocked
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.chest");
    }
}
