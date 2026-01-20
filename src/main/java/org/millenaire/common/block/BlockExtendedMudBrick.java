package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Extended mud brick block for Millénaire.
 * Used in various Indian and other culture buildings.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockExtendedMudBrick extends Block {

    public BlockExtendedMudBrick() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_ORANGE)
                .strength(1.5F, 4.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    public BlockExtendedMudBrick(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

