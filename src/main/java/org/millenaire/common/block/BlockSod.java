package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Sod/turf block for Millénaire - grass building material.
 * Used in Norse and other cultures for roofing.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockSod extends Block {

    public BlockSod() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.GRASS)
                .strength(0.6F)
                .sound(SoundType.GRASS));
    }

    public BlockSod(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

