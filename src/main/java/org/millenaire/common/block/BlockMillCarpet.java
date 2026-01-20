package org.millenaire.common.block;

import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.DyeColor;

/**
 * Custom carpet block for Millénaire.
 * Supports various culture-specific designs.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillCarpet extends WoolCarpetBlock {

    public BlockMillCarpet(DyeColor color) {
        super(color, BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(0.1F)
                .sound(SoundType.WOOL));
    }

    public BlockMillCarpet(DyeColor color, BlockBehaviour.Properties properties) {
        super(color, properties);
    }
}

