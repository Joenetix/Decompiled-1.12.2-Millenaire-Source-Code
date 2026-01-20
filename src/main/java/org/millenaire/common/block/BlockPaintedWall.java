package org.millenaire.common.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Painted wall block for Millénaire.
 * Walls with dye colors for decorative building.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockPaintedWall extends WallBlock implements IPaintedBlock {

    private final DyeColor color;

    public BlockPaintedWall(DyeColor color) {
        super(BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
        this.color = color;
    }

    @Override
    public String getBlockType() {
        return "wall";
    }

    @Override
    public DyeColor getDyeColour() {
        return this.color;
    }
}

