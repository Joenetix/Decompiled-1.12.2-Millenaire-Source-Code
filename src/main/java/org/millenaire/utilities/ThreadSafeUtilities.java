package org.millenaire.utilities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class ThreadSafeUtilities {
    public static Block getBlock(Level world, int x, int y, int z) throws ThreadSafeUtilities.ChunkAccessException {
        validateCoords(world, x, z);
        return world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static BlockState getBlockState(Level world, int x, int y, int z)
            throws ThreadSafeUtilities.ChunkAccessException {
        validateCoords(world, x, z);
        return world.getBlockState(new BlockPos(x, y, z));
    }

    public static boolean isBlockPassable(Block block, Level world, int x, int y, int z)
            throws ThreadSafeUtilities.ChunkAccessException {
        validateCoords(world, x, z);
        // Adapted for 1.20.1: Check if the block state has no collision shape (e.g.
        // air, flowers)
        return world.getBlockState(new BlockPos(x, y, z)).getCollisionShape(world, new BlockPos(x, y, z)).isEmpty();
    }

    public static boolean isChunkAtGenerated(Level world, int x, int z) {
        // world.hasChunk checks if chunk is loaded or generated?
        // In 1.20.1 hasChunk usually checks if it exists.
        return world.getChunk(x >> 4, z >> 4, net.minecraft.world.level.chunk.ChunkStatus.FULL, false) != null;
    }

    public static boolean isChunkAtLoaded(Level world, int x, int z) {
        if (world instanceof ServerLevel) {
            return ((ServerLevel) world).getChunkSource().hasChunk(x >> 4, z >> 4);
        } else {
            return world instanceof ClientLevel ? ((ClientLevel) world).getChunkSource().hasChunk(x >> 4, z >> 4)
                    : false;
        }
    }

    private static void validateCoords(Level world, int x, int z) throws ThreadSafeUtilities.ChunkAccessException {
        if (world instanceof ServerLevel) {
            if (!((ServerLevel) world).getChunkSource().hasChunk(x >> 4, z >> 4)) {
                throw new ThreadSafeUtilities.ChunkAccessException(
                        "Attempting to access a coordinate in an unloaded chunk within a thread in a server world at "
                                + x + "/" + z + ".",
                        x, z);
            }
        } else if (world instanceof ClientLevel && !((ClientLevel) world).getChunkSource().hasChunk(x >> 4, z >> 4)) {
            throw new ThreadSafeUtilities.ChunkAccessException(
                    "Attempting to access a coordinate in an unloaded chunk within a thread in a client world at " + x
                            + "/" + z + ".",
                    x, z);
        }
    }

    public static class ChunkAccessException extends Exception {
        private static final long serialVersionUID = -7650231135028039490L;
        public final int x;
        public final int z;

        public ChunkAccessException(String message, int x, int z) {
            super(message);
            this.x = x;
            this.z = z;
        }
    }
}
