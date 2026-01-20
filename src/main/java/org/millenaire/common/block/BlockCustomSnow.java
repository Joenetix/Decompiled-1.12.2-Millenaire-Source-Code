package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Custom snow block for Millénaire buildings.
 * Unlike vanilla snow, this doesn't melt and always drops itself.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockCustomSnow extends Block {

    public BlockCustomSnow() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SNOW)
                .strength(0.2F)
                .sound(SoundType.SNOW)
                .requiresCorrectToolForDrops());
    }

    public BlockCustomSnow(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

