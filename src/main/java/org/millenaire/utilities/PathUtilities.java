package org.millenaire.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Path building utility methods.
 * Ported from original 1.12.2 PathUtilities.java
 * 
 * Handles path construction between buildings in villages.
 */
public class PathUtilities {

    // Path construction modes
    private static final boolean PATH_RAISE = false;
    private static final boolean PATH_DROP = true;

    /**
     * Check if a path can be built at the given block state.
     * Returns true if the block can be replaced by path blocks.
     */
    public static boolean canPathBeBuiltHere(BlockState blockState) {
        Block block = blockState.getBlock();

        // Already a path block - check if it's stable (permanent)
        if (isPathBlock(block)) {
            // Non-stable paths can be rebuilt
            return true; // Simplified - original checked STABLE property
        }

        // Can replace path-replaceable blocks and decorative plants
        return BlockItemUtilities.isBlockPathReplaceable(block) ||
                BlockItemUtilities.isBlockDecorativePlant(block);
    }

    /**
     * Check if a position is on a stable (permanent) path.
     */
    public static boolean isPointOnStablePath(Level world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        if (isPathBlock(block)) {
            return true; // Simplified - original checked STABLE property
        }

        // Check block below
        Block belowBlock = world.getBlockState(pos.below()).getBlock();
        if (isPathBlock(belowBlock)) {
            return true;
        }

        return false;
    }

    /**
     * Clear a path block and replace with ground.
     */
    public static void clearPathBlock(Level world, BlockPos pos) {
        BlockState bs = world.getBlockState(pos);

        if (isPathBlock(bs.getBlock())) {
            BlockState blockStateBelow = world.getBlockState(pos.below());
            BlockState replacement = WorldUtilities.getBlockStateValidGround(blockStateBelow, true);

            if (replacement != null) {
                world.setBlock(pos, replacement, 3);
            } else {
                world.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Check if a block is a path block.
     */
    public static boolean isPathBlock(Block block) {
        // Check for vanilla dirt path and gravel (common path materials)
        return block == Blocks.DIRT_PATH ||
                block == Blocks.GRAVEL ||
                block == Blocks.COBBLESTONE ||
                block == Blocks.STONE_BRICKS;
    }

    /**
     * Check if a position contains stairs, slab, or chest.
     * These blocks cannot be replaced by paths.
     */
    public static boolean isStairsOrSlabOrChest(Level world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        return block instanceof StairBlock ||
                block instanceof SlabBlock ||
                block instanceof ChestBlock;
    }

    /**
     * Get the appropriate path block based on culture/biome.
     * Can be expanded to support different path materials.
     */
    public static BlockState getDefaultPathBlock() {
        return Blocks.DIRT_PATH.defaultBlockState();
    }

    /**
     * Get a gravel path block.
     */
    public static BlockState getGravelPathBlock() {
        return Blocks.GRAVEL.defaultBlockState();
    }

    /**
     * Get a cobblestone path block.
     */
    public static BlockState getCobblestonePathBlock() {
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    /**
     * Calculate if path should go up or down between two points.
     * 
     * @param startY Starting Y level
     * @param endY   Ending Y level
     * @return true if path should drop, false if should raise
     */
    public static boolean shouldPathDrop(int startY, int endY) {
        return endY < startY;
    }

    /**
     * Calculate the path direction between two points.
     * 
     * @param from Starting position
     * @param to   Ending position
     * @return Direction as 0-3 (N, E, S, W)
     */
    public static int getPathDirection(BlockPos from, BlockPos to) {
        int dx = to.getX() - from.getX();
        int dz = to.getZ() - from.getZ();

        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? 1 : 3; // East or West
        } else {
            return dz > 0 ? 2 : 0; // South or North
        }
    }

    /**
     * Check if a position is valid for path construction.
     * Position must be on solid ground with air above.
     */
    public static boolean isValidPathPosition(Level world, BlockPos pos) {
        Block below = world.getBlockState(pos.below()).getBlock();
        Block at = world.getBlockState(pos).getBlock();
        Block above = world.getBlockState(pos.above()).getBlock();

        return BlockItemUtilities.isBlockSolid(below) &&
                (at == Blocks.AIR || canPathBeBuiltHere(world.getBlockState(pos))) &&
                !BlockItemUtilities.isBlockSolid(above);
    }

    /**
     * Find a valid path level at the given X,Z coordinates.
     * Searches vertically to find suitable ground.
     */
    public static int findPathLevel(Level world, int x, int z, int referenceY) {
        // Search up and down from reference Y
        for (int offset = 0; offset <= 5; offset++) {
            // Check above
            BlockPos posUp = new BlockPos(x, referenceY + offset, z);
            if (isValidPathPosition(world, posUp)) {
                return referenceY + offset;
            }

            // Check below
            if (offset > 0) {
                BlockPos posDown = new BlockPos(x, referenceY - offset, z);
                if (isValidPathPosition(world, posDown)) {
                    return referenceY - offset;
                }
            }
        }

        // Fallback to reference Y
        return referenceY;
    }
}
