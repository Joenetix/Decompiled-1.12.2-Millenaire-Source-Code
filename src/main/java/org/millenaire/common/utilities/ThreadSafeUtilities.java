package org.millenaire.common.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ThreadSafeUtilities {

    public static class ChunkAccessException extends Exception {
        private static final long serialVersionUID = 1L;
    }

    public static BlockState getBlockState(Level world, int x, int y, int z) throws ChunkAccessException {
        // In 1.20.1, we assume main thread access or safe read.
        // If atomicstryker needs async safety, we might need more checks,
        // but for now we wrap standard access.
        try {
            if (world == null)
                return null;
            return world.getBlockState(new BlockPos(x, y, z));
        } catch (Exception e) {
            throw new ChunkAccessException();
        }
    }

    public static Block getBlock(Level world, int x, int y, int z) throws ChunkAccessException {
        BlockState state = getBlockState(world, x, y, z);
        return state == null ? null : state.getBlock();
    }
}
