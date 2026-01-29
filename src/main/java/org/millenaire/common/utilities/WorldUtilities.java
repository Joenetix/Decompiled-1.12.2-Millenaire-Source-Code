package org.millenaire.common.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WorldUtilities {

    public static int findSurfaceBlock(Level world, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, world.getMaxBuildHeight() - 1, z);

        // Go down until we hit something not air/leaves/foliage?
        // Original: !BlockItemUtilities.isBlockGround(...) && !Liquid

        // Scan down from top
        for (int y = world.getMaxBuildHeight() - 1; y > world.getMinBuildHeight(); y--) {
            pos.setY(y);
            BlockState state = world.getBlockState(pos);
            if (!state.isAir() && state.getFluidState().isEmpty()) { // Simple check for now
                // In original: BlockItemUtilities.isBlockGround check
                // For now, if it's solid, we assume it's surface
                return y + 1;
            }
        }
        return world.getMinBuildHeight() + 1;
    }
}
