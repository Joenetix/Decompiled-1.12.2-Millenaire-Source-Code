package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Bars block (iron bars style) for Millénaire.
 * Used for wooden bars, windows, and decorative elements.
 * Connects to walls and other bar blocks.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockBars extends IronBarsBlock {

    public BlockBars() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(5.0F, 10.0F)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    public BlockBars(BlockBehaviour.Properties properties) {
        super(properties);
    }

    /**
     * Check if bars can connect to adjacent block.
     * Extends vanilla logic to include BlockMillWall.
     */
    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        if (adjacentState.getBlock() instanceof BlockMillWall) {
            return false; // Don't skip rendering - we want to show connection
        }
        return super.skipRendering(state, adjacentState, direction);
    }

    /**
     * Custom connection check that includes Millénaire walls.
     */
    public boolean canConnectTo(BlockGetter level, BlockPos pos, Direction direction) {
        BlockPos adjacentPos = pos.relative(direction);
        BlockState adjacentState = level.getBlockState(adjacentPos);
        Block adjacentBlock = adjacentState.getBlock();

        // Connect to Millénaire walls
        if (adjacentBlock instanceof BlockMillWall) {
            return true;
        }

        // Default pane connection logic
        return adjacentState.isFaceSturdy(level, adjacentPos, direction.getOpposite());
    }
}
