package org.millenaire.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.HashSet;
import java.util.Set;

/**
 * Block and Item utility methods.
 * Ported from original 1.12.2 BlockItemUtilities.java
 */
public class BlockItemUtilities {

    // Block categories (populated at init)
    private static final Set<Block> GROUND_BLOCKS = new HashSet<>();
    private static final Set<Block> DANGER_BLOCKS = new HashSet<>();

    static {
        // Initialize ground blocks
        GROUND_BLOCKS.add(Blocks.DIRT);
        GROUND_BLOCKS.add(Blocks.GRASS_BLOCK);
        GROUND_BLOCKS.add(Blocks.PODZOL);
        GROUND_BLOCKS.add(Blocks.COARSE_DIRT);
        GROUND_BLOCKS.add(Blocks.MYCELIUM);
        GROUND_BLOCKS.add(Blocks.ROOTED_DIRT);
        GROUND_BLOCKS.add(Blocks.MUD);
        GROUND_BLOCKS.add(Blocks.STONE);
        GROUND_BLOCKS.add(Blocks.COBBLESTONE);
        GROUND_BLOCKS.add(Blocks.GRAVEL);
        GROUND_BLOCKS.add(Blocks.SAND);
        GROUND_BLOCKS.add(Blocks.RED_SAND);
        GROUND_BLOCKS.add(Blocks.SANDSTONE);
        GROUND_BLOCKS.add(Blocks.RED_SANDSTONE);
        GROUND_BLOCKS.add(Blocks.CLAY);

        // Initialize danger blocks
        DANGER_BLOCKS.add(Blocks.LAVA);
        DANGER_BLOCKS.add(Blocks.FIRE);
        DANGER_BLOCKS.add(Blocks.SOUL_FIRE);
        DANGER_BLOCKS.add(Blocks.CACTUS);
        DANGER_BLOCKS.add(Blocks.SWEET_BERRY_BUSH);
        DANGER_BLOCKS.add(Blocks.WITHER_ROSE);
        DANGER_BLOCKS.add(Blocks.MAGMA_BLOCK);
    }

    /**
     * Check if a block is considered "ground" for terrain generation.
     */
    public static boolean isBlockGround(Block b) {
        if (b == null || b == Blocks.AIR || b == Blocks.CAVE_AIR || b == Blocks.VOID_AIR) {
            return false;
        }
        return GROUND_BLOCKS.contains(b) || b.defaultBlockState().is(BlockTags.DIRT);
    }

    /**
     * Check if a block is solid for pathfinding purposes.
     */
    public static boolean isBlockSolid(Block b) {
        if (b == null) {
            return false;
        }
        BlockState state = b.defaultBlockState();

        // Check basic solidity
        if (state.isSolidRender(null, BlockPos.ZERO)) {
            return true;
        }

        // Additional solid blocks
        return b == Blocks.GLASS ||
                b == Blocks.ICE ||
                b == Blocks.PACKED_ICE ||
                b instanceof SlabBlock ||
                b instanceof StairBlock ||
                b instanceof FenceBlock ||
                b instanceof FarmBlock;
    }

    /**
     * Check if a block is walkable (can walk on top of it).
     */
    public static boolean isBlockWalkable(Block b) {
        if (b == null) {
            return false;
        }
        BlockState state = b.defaultBlockState();

        // Check basic walkability
        if (state.isSolidRender(null, BlockPos.ZERO)) {
            return true;
        }

        // Additional walkable blocks
        return b == Blocks.GLASS ||
                b == Blocks.ICE ||
                b instanceof SlabBlock ||
                b instanceof StairBlock ||
                b instanceof FarmBlock;
    }

    /**
     * Check if a block is dangerous to walk through.
     */
    public static boolean isBlockDangerous(Block b) {
        if (b == null || b == Blocks.AIR) {
            return false;
        }
        return DANGER_BLOCKS.contains(b);
    }

    /**
     * Check if a block is a liquid.
     */
    public static boolean isBlockLiquid(Level level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        return !fluidState.isEmpty();
    }

    /**
     * Check if a block is water.
     */
    public static boolean isBlockWater(Level level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        return fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER;
    }

    /**
     * Check if a block is a fence.
     */
    public static boolean isFence(Block block) {
        return block instanceof FenceBlock || block.defaultBlockState().is(BlockTags.FENCES);
    }

    /**
     * Check if a block is a fence gate.
     */
    public static boolean isFenceGate(Block block) {
        return block instanceof FenceGateBlock || block.defaultBlockState().is(BlockTags.FENCE_GATES);
    }

    /**
     * Check if a block is a wooden door.
     */
    public static boolean isWoodenDoor(Block block) {
        return block instanceof DoorBlock && block.defaultBlockState().is(BlockTags.WOODEN_DOORS);
    }

    /**
     * Check if a block is a decorative plant.
     */
    public static boolean isBlockDecorativePlant(Block block) {
        return block instanceof FlowerBlock ||
                block instanceof TallFlowerBlock ||
                block instanceof TallGrassBlock ||
                block.defaultBlockState().is(BlockTags.FLOWERS) ||
                block.defaultBlockState().is(BlockTags.SMALL_FLOWERS);
    }

    /**
     * Check if a block can be replaced when building paths.
     */
    public static boolean isBlockPathReplaceable(Block b) {
        if (b == null || b == Blocks.AIR) {
            return true;
        }

        return b instanceof TallGrassBlock ||
                b instanceof DeadBushBlock ||
                b instanceof FlowerBlock ||
                b instanceof SnowLayerBlock ||
                b.defaultBlockState().canBeReplaced();
    }

    /**
     * Check if a block is considered forbidden for building.
     */
    public static boolean isBlockForbidden(Block b) {
        if (b == null) {
            return false;
        }

        return b == Blocks.BEDROCK ||
                b == Blocks.END_PORTAL_FRAME ||
                b == Blocks.END_PORTAL ||
                b == Blocks.NETHER_PORTAL ||
                b == Blocks.SPAWNER ||
                b == Blocks.COMMAND_BLOCK ||
                b == Blocks.CHAIN_COMMAND_BLOCK ||
                b == Blocks.REPEATING_COMMAND_BLOCK ||
                b == Blocks.STRUCTURE_BLOCK ||
                b == Blocks.BARRIER;
    }

    /**
     * Check if a block is opaque (fully solid cube).
     */
    public static boolean isBlockOpaqueCube(Block block) {
        if (block == null) {
            return false;
        }
        return block.defaultBlockState().isCollisionShapeFullBlock(null, BlockPos.ZERO);
    }

    /**
     * Get the canonical name of a block.
     */
    public static String getBlockCanonicalName(Block block) {
        if (block == null) {
            return "null";
        }
        return block.builtInRegistryHolder().key().location().toString();
    }

    /**
     * Check if a block is a log (wood trunk).
     */
    public static boolean isLog(Block block) {
        if (block == null) {
            return false;
        }
        return block.defaultBlockState().is(BlockTags.LOGS);
    }

    /**
     * Check if a block is a natural block (stone, dirt, etc.) for preservation.
     */
    public static boolean isNaturalBlock(Block block) {
        if (block == null) {
            return false;
        }
        return block == Blocks.STONE ||
                block == Blocks.GRANITE ||
                block == Blocks.DIORITE ||
                block == Blocks.ANDESITE ||
                block == Blocks.DEEPSLATE ||
                block == Blocks.DIRT ||
                block == Blocks.GRASS_BLOCK ||
                block == Blocks.GRAVEL ||
                block == Blocks.SAND ||
                block == Blocks.RED_SAND ||
                block == Blocks.CLAY ||
                block == Blocks.SANDSTONE ||
                block == Blocks.RED_SANDSTONE ||
                block.defaultBlockState().is(BlockTags.DIRT) ||
                block.defaultBlockState().is(BlockTags.BASE_STONE_OVERWORLD);
    }
}
