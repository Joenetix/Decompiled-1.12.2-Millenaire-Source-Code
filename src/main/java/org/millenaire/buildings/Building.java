package org.millenaire.buildings;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import org.millenaire.core.Village;

/**
 * Represents a building in a village.
 * 
 * Buildings can be upgraded through multiple levels,
 * provide jobs, and produce resources.
 */
public class Building {
    private final String type;
    private final BlockPos position;
    private final Village village;

    private int level;
    private boolean underConstruction;

    public Building(String type, BlockPos position, Village village) {
        this.type = type;
        this.position = position;
        this.village = village;
        this.level = 0;
        this.underConstruction = false;
    }

    /**
     * Check if this building can be upgraded
     */
    public boolean canUpgrade() {
        // TODO: Check resources, prerequisites, etc.
        return level < getMaxLevel();
    }

    /**
     * Upgrade to next level
     */
    public void upgrade() {
        if (canUpgrade()) {
            level++;
            underConstruction = true;
        }
    }

    /**
     * Mark construction as complete
     */
    public void finishConstruction() {
        underConstruction = false;
    }

    /**
     * Get maximum level for this building type
     */
    public int getMaxLevel() {
        // TODO: Load from config based on building type
        return 5;
    }

    // Getters
    public String getType() {
        return type;
    }

    public BlockPos getPosition() {
        return position;
    }

    public Village getVillage() {
        return village;
    }

    public int getLevel() {
        return level;
    }

    public boolean isUnderConstruction() {
        return underConstruction;
    }

    /**
     * Save building data to NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type);
        tag.putLong("Position", position.asLong());
        tag.putInt("Level", level);
        tag.putBoolean("UnderConstruction", underConstruction);
        return tag;
    }

    /**
     * Load building from NBT
     */
    public static Building load(CompoundTag tag, Village village) {
        String type = tag.getString("Type");
        BlockPos position = BlockPos.of(tag.getLong("Position"));

        Building building = new Building(type, position, village);
        building.level = tag.getInt("Level");
        building.underConstruction = tag.getBoolean("UnderConstruction");

        return building;
    }
}
