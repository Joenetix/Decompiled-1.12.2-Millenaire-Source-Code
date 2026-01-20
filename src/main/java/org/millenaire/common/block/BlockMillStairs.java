package org.millenaire.common.block;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Custom stairs block for Millénaire.
 * Used for various culture-specific stair designs.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillStairs extends StairBlock {

    public BlockMillStairs(Supplier<BlockState> baseState, BlockBehaviour.Properties properties) {
        super(baseState, properties);
    }

    public BlockMillStairs(BlockState baseState, BlockBehaviour.Properties properties) {
        super(() -> baseState, properties);
    }
}

