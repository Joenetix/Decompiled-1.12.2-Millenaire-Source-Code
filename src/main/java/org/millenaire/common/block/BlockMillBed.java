package org.millenaire.common.block;

import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.item.DyeColor;

/**
 * Custom bed block for Millénaire villagers.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillBed extends BedBlock {

    public BlockMillBed(DyeColor color) {
        super(color, BlockBehaviour.Properties.of()
                .mapColor(state -> state.getValue(BedBlock.PART) == BedPart.FOOT ? color.getMapColor() : MapColor.WOOL)
                .strength(0.2F)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    public BlockMillBed(DyeColor color, BlockBehaviour.Properties properties) {
        super(color, properties);
    }
}

