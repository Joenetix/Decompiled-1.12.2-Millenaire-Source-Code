package org.millenaire.common.culture;

import net.minecraft.world.level.block.Block;

/**
 * Defines a farmable crop for a culture.
 */
public record CropDefinition(
        String key,
        String seedItem,
        Block cropBlock,
        Block soilBlock,
        int growthStages,
        String harvestGoal,
        String plantGoal) {
    public CropDefinition(String key, String seedItem, Block cropBlock) {
        this(key, seedItem, cropBlock, null, 7, null, null);
    }
}
