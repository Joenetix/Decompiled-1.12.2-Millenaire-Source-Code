package org.millenaire.common.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Painted stairs block for Millénaire.
 * Stairs with dye colors for decorative building.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockPaintedStairs extends StairBlock implements IPaintedBlock {

    private final DyeColor color;

    public BlockPaintedStairs(DyeColor color, Supplier<BlockState> baseState) {
        super(baseState, BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
        this.color = color;
    }

    @Override
    public String getBlockType() {
        return "stairs";
    }

    @Override
    public DyeColor getDyeColour() {
        return this.color;
    }
}

