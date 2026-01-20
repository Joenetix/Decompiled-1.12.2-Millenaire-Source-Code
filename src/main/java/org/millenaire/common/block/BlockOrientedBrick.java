package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Oriented brick block for Millénaire.
 * Bricks that can be oriented in different directions.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockOrientedBrick extends Block {

    public BlockOrientedBrick() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_RED)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public BlockOrientedBrick(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

