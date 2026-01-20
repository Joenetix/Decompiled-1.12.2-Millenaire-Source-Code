package org.millenaire.common.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Half slab block for Millénaire.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockHalfSlab extends SlabBlock {

    public BlockHalfSlab() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public BlockHalfSlab(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

