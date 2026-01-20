package org.millenaire.common.block;

import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Custom wall block for Millénaire.
 * Used for various culture-specific wall designs.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillWall extends WallBlock {

    public BlockMillWall() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public BlockMillWall(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

