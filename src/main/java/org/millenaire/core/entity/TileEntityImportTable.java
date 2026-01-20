package org.millenaire.core.entity;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.millenaire.core.MillBlockEntities;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 * TileEntityImportTable - Building plan import/export table for modders.
 * Ported exactly from original 1.12.2 TileEntityImportTable.java
 * 
 * Used for creating and editing building plans.
 */
public class TileEntityImportTable extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    private String buildingKey = null;
    private int variation = 0;
    private int upgradeLevel = 0;
    private int length;
    private int width;
    private int startingLevel = -1;
    private int orientation = 0;
    private boolean exportSnow = false;
    private boolean importMockBlocks = true;
    private boolean autoconvertToPreserveGround = true;
    private boolean exportRegularChests = false;
    private BlockPos parentTablePos = null;

    public TileEntityImportTable(BlockPos pos, BlockState state) {
        super(MillBlockEntities.IMPORT_TABLE.get(), pos, state);
    }

    public void activate(Player player) {
        // TODO: Implement activation logic
        // Original checked for SUMMONING_WAND and NEGATION_WAND items
        // For now, just send updates
        if (!player.level().isClientSide) {
            this.sendUpdates();
            // TODO: ServerSender.displayImportTableGUI(player, this.worldPosition);
        }
    }

    public boolean autoconvertToPreserveGround() {
        return this.autoconvertToPreserveGround;
    }

    public boolean exportRegularChests() {
        return this.exportRegularChests;
    }

    public boolean exportSnow() {
        return this.exportSnow;
    }

    public String getBuildingKey() {
        return this.buildingKey;
    }

    public int getLength() {
        return this.length;
    }

    public int getOrientation() {
        return this.orientation;
    }

    @Nullable
    public BlockPos getParentTablePos() {
        return this.parentTablePos;
    }

    public BlockPos getPosPoint() {
        return this.worldPosition;
    }

    public int getStartingLevel() {
        return this.startingLevel;
    }

    private BlockState getState() {
        return this.level.getBlockState(this.worldPosition);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public int getUpgradeLevel() {
        return this.upgradeLevel;
    }

    public int getVariation() {
        return this.variation;
    }

    public int getWidth() {
        return this.width;
    }

    public boolean importMockBlocks() {
        return this.importMockBlocks;
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        this.buildingKey = compound.getString("buildingKey");
        if (this.buildingKey.isEmpty())
            this.buildingKey = null;
        this.variation = compound.getInt("variation");
        this.length = compound.getInt("length");
        this.width = compound.getInt("width");
        this.upgradeLevel = compound.getInt("upgradeLevel");
        this.startingLevel = compound.getInt("startingLevel");
        this.orientation = compound.getInt("orientation");
        this.exportSnow = compound.getBoolean("exportSnow");
        this.importMockBlocks = compound.getBoolean("importMockBlocks");
        this.autoconvertToPreserveGround = compound.getBoolean("autoconvertToPreserveGround");
        this.exportRegularChests = compound.getBoolean("exportRegularChests");
        if (compound.contains("parentTablePosX")) {
            this.parentTablePos = new BlockPos(
                    compound.getInt("parentTablePosX"),
                    compound.getInt("parentTablePosY"),
                    compound.getInt("parentTablePosZ"));
        }
    }

    private void sendUpdates() {
        if (this.level != null) {
            this.level.blockEntityChanged(this.worldPosition);
            BlockState state = getState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
            this.setChanged();
        }
    }

    public void setAutoconvertToPreserveGround(boolean autoconvertToPreserveGround) {
        this.autoconvertToPreserveGround = autoconvertToPreserveGround;
    }

    public void setBuildingKey(String buildingKey) {
        this.buildingKey = buildingKey;
    }

    public void setExportRegularChests(boolean exportRegularChests) {
        this.exportRegularChests = exportRegularChests;
    }

    public void setExportSnow(boolean exportSnow) {
        this.exportSnow = exportSnow;
    }

    public void setImportMockBlocks(boolean importMockBlocks) {
        this.importMockBlocks = importMockBlocks;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public void setParentTablePos(BlockPos parentTablePos) {
        this.parentTablePos = parentTablePos;
    }

    public void setStartingLevel(int startingLevel) {
        this.startingLevel = startingLevel;
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = upgradeLevel;
    }

    public void updatePlan(String buildingKey, int length, int width, int variation, int level, int startLevel,
            Player player) {
        this.buildingKey = buildingKey;
        LOGGER.debug("updatePlan : Updating buildingKey to: {}", buildingKey);
        this.length = length;
        this.width = width;
        this.variation = variation;
        this.upgradeLevel = level;
        this.startingLevel = startLevel;
        // TODO: updateAttachedSign();
        if (player != null) {
            this.sendUpdates();
        }
    }

    public void updateSettings(
            int upgradeLevel,
            int orientation,
            int startingLevel,
            boolean exportSnow,
            boolean importMockBlocks,
            boolean autoconvertToPreserveGround,
            boolean exportRegularChests,
            Player player) {
        this.upgradeLevel = upgradeLevel;
        this.orientation = orientation;
        this.startingLevel = startingLevel;
        this.exportSnow = exportSnow;
        this.importMockBlocks = importMockBlocks;
        this.autoconvertToPreserveGround = autoconvertToPreserveGround;
        this.exportRegularChests = exportRegularChests;
        // TODO: updateAttachedSign();
        if (player != null) {
            this.sendUpdates();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (this.buildingKey != null) {
            compound.putString("buildingKey", this.buildingKey);
        }
        compound.putInt("variation", this.variation);
        compound.putInt("length", this.length);
        compound.putInt("width", this.width);
        compound.putInt("upgradeLevel", this.upgradeLevel);
        compound.putInt("startingLevel", this.startingLevel);
        compound.putInt("orientation", this.orientation);
        compound.putBoolean("exportSnow", this.exportSnow);
        compound.putBoolean("importMockBlocks", this.importMockBlocks);
        compound.putBoolean("autoconvertToPreserveGround", this.autoconvertToPreserveGround);
        compound.putBoolean("exportRegularChests", this.exportRegularChests);
        if (this.parentTablePos != null) {
            compound.putInt("parentTablePosX", this.parentTablePos.getX());
            compound.putInt("parentTablePosY", this.parentTablePos.getY());
            compound.putInt("parentTablePosZ", this.parentTablePos.getZ());
        }
    }
}
