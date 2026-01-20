package org.millenaire.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * World utility methods.
 * Ported from original 1.12.2 WorldUtilities.java
 */
public class WorldUtilities {

    /**
     * Find the top soil/ground block at the given X,Z coordinates.
     * Returns Y coordinate where buildings should be placed.
     */
    public static int findTopSoilBlock(Level world, int x, int z) {
        int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        while (y > world.getMinBuildHeight() && !BlockItemUtilities.isBlockGround(getBlock(world, x, y - 1, z))) {
            y--;
        }

        if (y > world.getMaxBuildHeight() - 2) {
            y = world.getMaxBuildHeight() - 2;
        }

        return y;
    }

    /**
     * Find the surface block, including liquids.
     * Returns Y coordinate of the surface.
     */
    public static int findSurfaceBlock(Level world, int x, int z) {
        int y = world.getMaxBuildHeight();

        while (y > world.getMinBuildHeight() &&
                !BlockItemUtilities.isBlockGround(getBlock(world, x, y, z)) &&
                !(getBlock(world, x, y, z) instanceof LiquidBlock)) {
            y--;
        }

        if (y > world.getMaxBuildHeight() - 2) {
            y = world.getMaxBuildHeight() - 2;
        }

        return y + 1;
    }

    /**
     * Get block at the given coordinates.
     */
    public static Block getBlock(Level world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    /**
     * Get block at the given position.
     */
    public static Block getBlock(Level world, BlockPos pos) {
        return world.getBlockState(pos).getBlock();
    }

    /**
     * Get block state at the given coordinates.
     */
    public static BlockState getBlockState(Level world, int x, int y, int z) {
        return world.getBlockState(new BlockPos(x, y, z));
    }

    /**
     * Get block state at the given position.
     */
    public static BlockState getBlockState(Level world, BlockPos pos) {
        return world.getBlockState(pos);
    }

    /**
     * Set block at the given position.
     */
    public static void setBlock(Level world, BlockPos pos, BlockState state) {
        world.setBlock(pos, state, 3);
    }

    /**
     * Set block at the given coordinates.
     */
    public static void setBlock(Level world, int x, int y, int z, BlockState state) {
        world.setBlock(new BlockPos(x, y, z), state, 3);
    }

    /**
     * Check if chunks are generated in the given area.
     */
    public static boolean checkChunksGenerated(ServerLevel world, int startX, int startZ, int endX, int endZ) {
        for (int x = startX >> 4; x <= endX >> 4; x++) {
            for (int z = startZ >> 4; z <= endZ >> 4; z++) {
                if (!world.hasChunk(x, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Get entities within a bounding box.
     */
    public static <T extends Entity> List<T> getEntitiesWithinAABB(Level world, Class<T> type,
            BlockPos center, int horizontalRadius, int verticalRadius) {
        AABB box = new AABB(
                center.getX() - horizontalRadius,
                center.getY() - verticalRadius,
                center.getZ() - horizontalRadius,
                center.getX() + horizontalRadius,
                center.getY() + verticalRadius,
                center.getZ() + horizontalRadius);
        return world.getEntitiesOfClass(type, box);
    }

    /**
     * Find entity by UUID.
     */
    public static Entity getEntityByUUID(ServerLevel world, UUID uuid) {
        return world.getEntity(uuid);
    }

    /**
     * Find a random standing position around a point.
     */
    public static BlockPos findRandomStandingPosAround(Level world, BlockPos center, int radius) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int dx = world.random.nextInt(radius * 2 + 1) - radius;
            int dz = world.random.nextInt(radius * 2 + 1) - radius;
            int y = findTopSoilBlock(world, center.getX() + dx, center.getZ() + dz);

            BlockPos pos = new BlockPos(center.getX() + dx, y, center.getZ() + dz);
            if (isValidStandingPos(world, pos)) {
                return pos;
            }
        }
        return center;
    }

    /**
     * Check if a position is valid for standing.
     */
    public static boolean isValidStandingPos(Level world, BlockPos pos) {
        Block below = getBlock(world, pos.below());
        Block at = getBlock(world, pos);
        Block above = getBlock(world, pos.above());

        return BlockItemUtilities.isBlockSolid(below) &&
                !BlockItemUtilities.isBlockSolid(at) &&
                !BlockItemUtilities.isBlockSolid(above);
    }

    /**
     * Count blocks of a specific type around a position.
     */
    public static int countBlocksAround(Level world, BlockPos center, Block target,
            int radiusX, int radiusY, int radiusZ) {
        int count = 0;
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    if (getBlock(world, center.offset(x, y, z)) == target) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Get a valid ground block state for terrain generation.
     * Converts unsupported blocks to appropriate alternatives.
     */
    public static BlockState getBlockStateValidGround(BlockState currentState, boolean surface) {
        Block block = currentState.getBlock();

        // Already valid
        if (BlockItemUtilities.isBlockGround(block)) {
            return currentState;
        }

        // Convert to appropriate ground type
        if (surface) {
            return Blocks.GRASS_BLOCK.defaultBlockState();
        } else {
            return Blocks.DIRT.defaultBlockState();
        }
    }

    /**
     * Play block place sound at a position.
     */
    public static void playBlockSound(Level world, BlockPos pos, BlockState state) {
        world.playSound(
                null,
                pos,
                state.getSoundType().getPlaceSound(),
                net.minecraft.sounds.SoundSource.BLOCKS,
                1.0f,
                1.0f);
    }

    // ========== Point-based overloads ==========

    /**
     * Get block at Point position.
     */
    public static Block getBlock(Level world, org.millenaire.common.utilities.Point p) {
        return getBlock(world, p.getBlockPos());
    }

    /**
     * Get block state at Point position.
     */
    public static BlockState getBlockState(Level world, org.millenaire.common.utilities.Point p) {
        return getBlockState(world, p.getBlockPos());
    }

    /**
     * Set block at Point position.
     */
    public static void setBlock(Level world, org.millenaire.common.utilities.Point p, Block block, boolean notifyBlocks,
            boolean playSound) {
        BlockState state = block.defaultBlockState();
        int flags = notifyBlocks ? 3 : 2;
        world.setBlock(p.getBlockPos(), state, flags);
        if (playSound) {
            playBlockSound(world, p.getBlockPos(), state);
        }
    }

    /**
     * Set block state at Point position.
     */
    public static void setBlockState(Level world, org.millenaire.common.utilities.Point p, BlockState state,
            boolean notifyBlocks, boolean playSound) {
        int flags = notifyBlocks ? 3 : 2;
        world.setBlock(p.getBlockPos(), state, flags);
        if (playSound) {
            playBlockSound(world, p.getBlockPos(), state);
        }
    }

    /**
     * Check if position is a liquid.
     */
    public static boolean isLiquid(Level world, org.millenaire.common.utilities.Point p) {
        return BlockItemUtilities.isBlockLiquid(world, p.getBlockPos());
    }
}
