package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Basic sandstone block for Millénaire.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillSandstone extends Block {

    public BlockMillSandstone() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.SAND)
                .strength(0.8F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public BlockMillSandstone(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

