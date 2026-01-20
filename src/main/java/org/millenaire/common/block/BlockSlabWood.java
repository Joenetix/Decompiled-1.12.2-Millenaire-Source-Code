package org.millenaire.common.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Wood slab block for Millénaire.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockSlabWood extends SlabBlock {

    public BlockSlabWood() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0F, 3.0F)
                .sound(SoundType.WOOD));
    }

    public BlockSlabWood(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

