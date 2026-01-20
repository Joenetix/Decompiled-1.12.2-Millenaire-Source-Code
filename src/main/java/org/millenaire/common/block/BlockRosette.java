package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Rosette decorative block for Millénaire.
 * Decorative circular/star patterns for building facades.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockRosette extends Block {

    public BlockRosette() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    public BlockRosette(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

