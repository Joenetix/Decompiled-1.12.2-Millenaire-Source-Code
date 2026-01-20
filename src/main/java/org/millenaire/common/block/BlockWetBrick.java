package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Wet brick block for Millénaire - drying bricks.
 * Bricks that dry over time in sunlight.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockWetBrick extends Block {

    public BlockWetBrick() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.CLAY)
                .strength(0.6F)
                .sound(SoundType.GRAVEL)
                .randomTicks());
    }

    public BlockWetBrick(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Drying logic - can convert to dry brick over time
        // TODO: Implement when dry brick block is available
    }
}

