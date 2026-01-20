package org.millenaire.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Represents a building block with position, state, and optional special type.
 * Ported from original Millénaire BuildingBlock.java.
 * 
 * The special type determines how this block interacts with terrain during
 * placement.
 */
public class BuildingBlock {
    public final BlockPos pos;
    public final BlockState state;
    public final SpecialBlockType special;

    /**
     * Create a normal building block with a specific state.
     */
    public BuildingBlock(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
        this.special = SpecialBlockType.NONE;
    }

    /**
     * Create a special block for terrain handling (no specific state).
     */
    public BuildingBlock(BlockPos pos, SpecialBlockType special) {
        this.pos = pos;
        this.state = Blocks.AIR.defaultBlockState();
        this.special = special;
    }

    /**
     * Build this block in the world.
     * 
     * @param level      The server level
     * @param isWorldGen True if this is world generation (affects grass vs dirt)
     * @return True if a block was modified
     */
    public boolean build(ServerLevel level, boolean isWorldGen) {
        // Handle grouped special types using helper methods
        if (special.isClearGround()) {
            return buildClearGround(level, isWorldGen);
        }
        if (special.isPreserveGround()) {
            return buildPreserveGround(level, isWorldGen);
        }
        if (special == SpecialBlockType.CLEARTREE) {
            return buildClearTree(level, isWorldGen);
        }
        if (special.isSpawner()) {
            return buildSpawner(level);
        }
        if (special.isTreeSpawn()) {
            return buildTreeSpawn(level, isWorldGen);
        }
        if (special.isPicture()) {
            return buildPicture(level);
        }
        if (special == SpecialBlockType.TORCHGUESS) {
            return buildTorchGuess(level);
        }
        if (special == SpecialBlockType.CHESTGUESS) {
            return buildChestGuess(level);
        }
        if (special == SpecialBlockType.FURNACEGUESS) {
            return buildFurnaceGuess(level);
        }
        if (special == SpecialBlockType.INVERTED_DOOR) {
            return buildInvertedDoor(level);
        }
        if (special == SpecialBlockType.DISPENDERUNKNOWNPOWDER) {
            return buildDispenserWithPowder(level);
        }
        // Default: normal block
        return buildNormalBlock(level);
    }

    /**
     * Clear ground - EXACT port from original BuildingBlock.buildClearGround()
     * (lines 266-318).
     * Clears blocks that obstruct building placement, with special handling for
     * borders near water.
     */
    private boolean buildClearGround(ServerLevel level, boolean isWorldGen) {
        boolean shouldSetBlock = false;
        boolean shouldSetBlockBelow = false;
        BlockState existingState = level.getBlockState(pos);
        Block existingBlock = existingState.getBlock();
        BlockState targetBlockState = null;
        BlockState targetBelowBlockState = null;

        // Skip decorative plants (original: BlockItemUtilities.isBlockDecorativePlant)
        if (isDecorativePlant(existingBlock)) {
            return false;
        }

        // Handle CLEARGROUNDBORDER - special case for borders (original lines 274-296)
        if (special == SpecialBlockType.CLEARGROUNDBORDER &&
                !(existingBlock instanceof LeavesBlock) && !existingState.isAir()) {
            // Check if adjacent to liquid (water)
            if (isAdjacentToLiquid(level, pos)) {
                // Near water - place ground block instead of air to prevent flooding
                BlockState blockStateBelow = level.getBlockState(pos.below());
                targetBlockState = getValidGroundState(blockStateBelow, true);
                if (targetBlockState == null) {
                    targetBlockState = Blocks.DIRT.defaultBlockState();
                }
                if (existingBlock != targetBlockState.getBlock()) {
                    shouldSetBlock = true;
                }
            } else if (!existingState.isAir()) {
                // Not near water - clear to air
                targetBlockState = Blocks.AIR.defaultBlockState();
                shouldSetBlock = true;
            }
        } else if (!existingState.isAir() &&
                (special != SpecialBlockType.CLEARGROUNDOUTSIDEBUILDING &&
                        special != SpecialBlockType.CLEARGROUNDBORDER ||
                        !(existingBlock instanceof LeavesBlock))) {
            // Standard clear - set to air (original lines 292-296)
            targetBlockState = Blocks.AIR.defaultBlockState();
            shouldSetBlock = true;
        }

        // Ensure ground below is proper (original lines 299-307)
        // Only set block below if we're doing worldGen and below is dirt (convert to
        // grass)
        BlockState blockStateBelowx = level.getBlockState(pos.below());
        targetBelowBlockState = getValidGroundState(blockStateBelowx, true);
        if (isWorldGen && targetBelowBlockState != null &&
                targetBelowBlockState.getBlock() == Blocks.DIRT &&
                blockStateBelowx.getBlock() != Blocks.GRASS_BLOCK) {
            // During world gen, convert exposed dirt to grass
            targetBelowBlockState = Blocks.GRASS_BLOCK.defaultBlockState();
            shouldSetBlockBelow = true;
        }
        // REMOVED: The incorrect logic that was setting shouldSetBlock=true
        // unconditionally
        // Original lines 304-307 only affect block below, not the main block

        // Apply the changes
        if (shouldSetBlock && targetBlockState != null) {
            level.setBlock(pos, targetBlockState, 3);
        }

        if (shouldSetBlockBelow && targetBelowBlockState != null) {
            level.setBlock(pos.below(), targetBelowBlockState, 3);
        }

        return shouldSetBlock || shouldSetBlockBelow;
    }

    /**
     * Preserve/place ground - EXACT port from original
     * BuildingBlock.buildPreserveGround() (lines 518-569).
     * Places a single ground block at this position. Does NOT fill gaps downward.
     * Stepped terrain effect comes from Y-distribution of blocks generated by
     * prepareGround.
     */
    private boolean buildPreserveGround(ServerLevel level, boolean isWorldGen) {
        BlockState existingState = level.getBlockState(pos);
        Block existingBlock = existingState.getBlock();
        boolean isSurface = (special == SpecialBlockType.PRESERVEGROUNDSURFACE);

        // For depth blocks, preserve existing solid ground blocks (original lines
        // 521-529)
        // Original: if (!surface && existingBlockState.isNormalCube() &&
        // existingBlockState.isFullCube())
        if (!isSurface && existingState.isSolidRender(level, pos)) {
            // Check if it's a natural material we should preserve
            // Original: Material.ROCK, Material.GROUND, Material.SAND, Material.CLAY
            if (existingBlock == Blocks.STONE || existingBlock == Blocks.DIRT ||
                    existingBlock == Blocks.GRASS_BLOCK || existingBlock == Blocks.SAND ||
                    existingBlock == Blocks.GRAVEL || existingBlock == Blocks.SANDSTONE ||
                    existingBlock == Blocks.TERRACOTTA || existingBlock == Blocks.CLAY) {
                return false;
            }
        }

        // Get valid ground state from existing block (original line 531)
        BlockState targetGroundState = getValidGroundState(existingState, isSurface);

        // If no valid state, search below (original lines 532-543)
        if (targetGroundState == null) {
            for (BlockPos below = pos.below(); targetGroundState == null
                    && below.getY() > level.getMinBuildHeight(); below = below.below()) {
                BlockState belowState = level.getBlockState(below);
                targetGroundState = getValidGroundState(belowState, isSurface);
            }
            // Default to dirt if nothing found
            if (targetGroundState == null) {
                targetGroundState = Blocks.DIRT.defaultBlockState();
            }
        }

        // During world gen, use grass on surface (original lines 545-547)
        if (targetGroundState.getBlock() == Blocks.DIRT && isWorldGen && isSurface) {
            targetGroundState = Blocks.GRASS_BLOCK.defaultBlockState();
        }

        // During non-worldgen, use dirt instead of grass (original lines 549-551)
        if (targetGroundState.getBlock() == Blocks.GRASS_BLOCK && !isWorldGen) {
            targetGroundState = Blocks.DIRT.defaultBlockState();
        }

        // Fallback handling (original lines 553-559)
        if (targetGroundState == null || targetGroundState.getBlock() == Blocks.AIR) {
            if (isWorldGen && isSurface) {
                targetGroundState = Blocks.GRASS_BLOCK.defaultBlockState();
            } else {
                targetGroundState = Blocks.DIRT.defaultBlockState();
            }
        }

        // Skip if already correct (original line 561-562)
        if (targetGroundState.equals(existingState)) {
            return false;
        }

        // Don't replace grass with dirt (original line 563-564)
        if (existingBlock == Blocks.GRASS_BLOCK && targetGroundState.getBlock() == Blocks.DIRT) {
            return false;
        }

        // EXACT ORIGINAL: Place single block at this position (line 566)
        level.setBlock(pos, targetGroundState, 3);
        return true;
    }

    /**
     * Clear tree blocks - removes logs AND leaves.
     * Extended from original BuildingBlock.buildClearTree() to also clear
     * leaves since CLEARTREE blocks are generated at heights where leaves exist.
     */
    private boolean buildClearTree(ServerLevel level, boolean isWorldGen) {
        BlockState existingState = level.getBlockState(pos);
        Block block = existingState.getBlock();

        // Clear if it's a log or leaves
        if (!isLogBlock(block) && !(block instanceof LeavesBlock)) {
            return false;
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

        // Ensure ground below is proper
        BlockState belowState = level.getBlockState(pos.below());
        BlockState targetBelowState = getValidGroundState(belowState, true);
        if (isWorldGen && targetBelowState != null &&
                targetBelowState.getBlock() == Blocks.DIRT) {
            level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 3);
        } else if (targetBelowState != null && !targetBelowState.equals(belowState)) {
            level.setBlock(pos.below(), targetBelowState, 3);
        }

        return true;
    }

    /**
     * Place a normal block.
     */
    private boolean buildNormalBlock(ServerLevel level) {
        if (state == null || state.isAir()) {
            return false;
        }

        BlockState existingState = level.getBlockState(pos);
        if (existingState.equals(state)) {
            return false;
        }

        level.setBlock(pos, state, 3);
        return true;
    }

    /**
     * Build a mob spawner with the appropriate entity type.
     */
    private boolean buildSpawner(ServerLevel level) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 3);

        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof net.minecraft.world.level.block.entity.SpawnerBlockEntity spawner) {
            net.minecraft.world.entity.EntityType<?> entityType = switch (special) {
                case SPAWNERSKELETON -> net.minecraft.world.entity.EntityType.SKELETON;
                case SPAWNERZOMBIE -> net.minecraft.world.entity.EntityType.ZOMBIE;
                case SPAWNERSPIDER -> net.minecraft.world.entity.EntityType.SPIDER;
                case SPAWNERCAVESPIDER -> net.minecraft.world.entity.EntityType.CAVE_SPIDER;
                case SPAWNERCREEPER -> net.minecraft.world.entity.EntityType.CREEPER;
                case SPAWNERBLAZE -> net.minecraft.world.entity.EntityType.BLAZE;
                default -> net.minecraft.world.entity.EntityType.ZOMBIE;
            };
            spawner.getSpawner().setEntityId(entityType, level, level.random, pos);
        }
        return true;
    }

    /**
     * Spawn a tree at this location.
     */
    private boolean buildTreeSpawn(ServerLevel level, boolean isWorldGen) {
        if (!isWorldGen) {
            return false;
        }

        // Place appropriate sapling - actual tree growth happens via game mechanics
        BlockState sapling = switch (special) {
            case OAKSPAWN -> Blocks.OAK_SAPLING.defaultBlockState();
            case PINESPAWN -> Blocks.SPRUCE_SAPLING.defaultBlockState();
            case BIRCHSPAWN -> Blocks.BIRCH_SAPLING.defaultBlockState();
            case JUNGLESPAWN -> Blocks.JUNGLE_SAPLING.defaultBlockState();
            case ACACIASPAWN -> Blocks.ACACIA_SAPLING.defaultBlockState();
            case DARKOAKSPAWN -> Blocks.DARK_OAK_SAPLING.defaultBlockState();
            case CHERRYTREESPAWN -> Blocks.CHERRY_SAPLING.defaultBlockState();
            case SAKURATREESPAWN -> Blocks.CHERRY_SAPLING.defaultBlockState(); // Sakura = Cherry in 1.20
            default -> Blocks.OAK_SAPLING.defaultBlockState();
        };

        // For custom trees (apple, olive, pistachio), use oak sapling as placeholder
        if (special == SpecialBlockType.APPLETREESPAWN ||
                special == SpecialBlockType.OLIVETREESPAWN ||
                special == SpecialBlockType.PISTACHIOTREESPAWN) {
            sapling = Blocks.OAK_SAPLING.defaultBlockState();
        }

        level.setBlock(pos, sapling, 3);
        return true;
    }

    /**
     * Build a picture/wall decoration.
     */
    private boolean buildPicture(ServerLevel level) {
        // TODO: Implement wall decoration entity placement when EntityWallDecoration is
        // ported
        // For now, return false to skip
        return false;
    }

    /**
     * Build a torch with guessed facing based on surrounding blocks.
     */
    private boolean buildTorchGuess(ServerLevel level) {
        // Try wall torch first, then floor torch
        Direction facing = guessFacing(level);
        BlockPos attachPos = pos.relative(facing.getOpposite());

        if (level.getBlockState(attachPos).isSolid()) {
            // Can attach to wall
            BlockState wallTorch = switch (facing) {
                case NORTH -> Blocks.WALL_TORCH.defaultBlockState().setValue(
                        net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.NORTH);
                case SOUTH -> Blocks.WALL_TORCH.defaultBlockState().setValue(
                        net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.SOUTH);
                case EAST -> Blocks.WALL_TORCH.defaultBlockState().setValue(
                        net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.EAST);
                case WEST -> Blocks.WALL_TORCH.defaultBlockState().setValue(
                        net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.WEST);
                default -> Blocks.TORCH.defaultBlockState();
            };
            level.setBlock(pos, wallTorch, 3);
        } else {
            // Floor torch
            level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 3);
        }
        return true;
    }

    /**
     * Build a chest with guessed facing based on surrounding blocks.
     */
    private boolean buildChestGuess(ServerLevel level) {
        Direction facing = guessFacing(level);
        BlockState chest = Blocks.CHEST.defaultBlockState()
                .setValue(net.minecraft.world.level.block.ChestBlock.FACING, facing);
        level.setBlock(pos, chest, 3);
        return true;
    }

    /**
     * Build a furnace with guessed facing based on surrounding blocks.
     */
    private boolean buildFurnaceGuess(ServerLevel level) {
        Direction facing = guessFacing(level);
        BlockState furnace = Blocks.FURNACE.defaultBlockState()
                .setValue(net.minecraft.world.level.block.FurnaceBlock.FACING, facing);
        level.setBlock(pos, furnace, 3);
        return true;
    }

    /**
     * Build an inverted door (hinge on right side).
     */
    private boolean buildInvertedDoor(ServerLevel level) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.DoorBlock) {
            level.setBlock(pos, state, 3);

            if (state.getValue(
                    net.minecraft.world.level.block.DoorBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) {
                BlockState upper = state
                        .setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                                net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER)
                        .setValue(net.minecraft.world.level.block.DoorBlock.HINGE,
                                net.minecraft.world.level.block.state.properties.DoorHingeSide.RIGHT);
                level.setBlock(pos.above(), upper, 3);
            }
        }
        return true;
    }

    /**
     * Build a dispenser containing unknown powder.
     */
    private boolean buildDispenserWithPowder(ServerLevel level) {
        level.setBlock(pos, Blocks.DISPENSER.defaultBlockState(), 3);
        // TODO: Add unknown powder items when MillItems is fully ported
        return true;
    }

    /**
     * Guess the best facing direction for a block based on surrounding blocks.
     */
    private Direction guessFacing(ServerLevel level) {
        BlockState bsNorth = level.getBlockState(pos.north());
        BlockState bsSouth = level.getBlockState(pos.south());
        BlockState bsWest = level.getBlockState(pos.west());
        BlockState bsEast = level.getBlockState(pos.east());

        // Face away from solid blocks
        if (bsNorth.isSolid() && !bsSouth.isSolid())
            return Direction.SOUTH;
        if (bsSouth.isSolid() && !bsNorth.isSolid())
            return Direction.NORTH;
        if (bsWest.isSolid() && !bsEast.isSolid())
            return Direction.EAST;
        if (bsEast.isSolid() && !bsWest.isSolid())
            return Direction.WEST;

        // Default
        if (!bsSouth.isSolid())
            return Direction.SOUTH;
        if (!bsNorth.isSolid())
            return Direction.NORTH;
        if (!bsEast.isSolid())
            return Direction.EAST;
        if (!bsWest.isSolid())
            return Direction.WEST;

        return Direction.NORTH;
    }

    // ========== Helper Methods ==========

    /**
     * Check if a block is a decorative plant that shouldn't be cleared.
     */
    private boolean isDecorativePlant(Block block) {
        return block == Blocks.DANDELION || block == Blocks.POPPY ||
                block == Blocks.BLUE_ORCHID || block == Blocks.ALLIUM ||
                block == Blocks.AZURE_BLUET || block == Blocks.RED_TULIP ||
                block == Blocks.ORANGE_TULIP || block == Blocks.WHITE_TULIP ||
                block == Blocks.PINK_TULIP || block == Blocks.OXEYE_DAISY ||
                block == Blocks.CORNFLOWER || block == Blocks.LILY_OF_THE_VALLEY;
    }

    /**
     * Check if position is adjacent to liquid.
     */
    private boolean isAdjacentToLiquid(ServerLevel level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));
            if (neighbor.getBlock() instanceof LiquidBlock) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get valid ground state based on existing block.
     * EXACT port from original WorldUtilities.getBlockStateValidGround().
     * Returns null for air/non-terrain blocks - caller should search below.
     * 
     * @param existingState The current block state
     * @param surface       True if this is a surface block (affects stone/sandstone
     *                      handling)
     * @return Appropriate ground state or null if not a valid ground block
     */
    private BlockState getValidGroundState(BlockState existingState, boolean surface) {
        Block block = existingState.getBlock();

        // Original: gravel -> dirt
        if (block == Blocks.GRAVEL) {
            return Blocks.DIRT.defaultBlockState();
        }

        // Original: stone on surface -> dirt, underground -> keep stone
        if (block == Blocks.STONE || block == Blocks.DEEPSLATE ||
                block == Blocks.GRANITE || block == Blocks.DIORITE || block == Blocks.ANDESITE) {
            if (surface) {
                return Blocks.DIRT.defaultBlockState();
            } else {
                return existingState;
            }
        }

        // Original: dirt -> keep as is
        if (block == Blocks.DIRT || block == Blocks.COARSE_DIRT) {
            return existingState;
        }

        // Original: grass -> returns dirt (caller will convert to grass if needed)
        if (block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.MYCELIUM) {
            return Blocks.DIRT.defaultBlockState();
        }

        // Original: sand -> keep as is
        if (block == Blocks.SAND || block == Blocks.RED_SAND) {
            return existingState;
        }

        // Original: sandstone on surface -> sand, underground -> keep sandstone
        if (block == Blocks.SANDSTONE || block == Blocks.RED_SANDSTONE) {
            if (surface) {
                return Blocks.SAND.defaultBlockState();
            } else {
                return existingState;
            }
        }

        // Original: terracotta -> keep as is
        if (block == Blocks.TERRACOTTA || block == Blocks.CLAY) {
            return existingState;
        }

        // Original returns null for EVERYTHING ELSE including AIR
        // This causes caller to search below for valid ground type
        return null;
    }

    /**
     * Check if block is vegetation.
     */
    private boolean isVegetation(Block block) {
        return block == Blocks.GRASS || block == Blocks.TALL_GRASS ||
                block == Blocks.FERN || block == Blocks.LARGE_FERN ||
                block instanceof LeavesBlock;
    }

    /**
     * Check if block is a log.
     */
    private boolean isLogBlock(Block block) {
        String name = block.getName().getString().toLowerCase();
        return name.contains("log") || name.contains("stem") ||
                block == Blocks.OAK_LOG || block == Blocks.SPRUCE_LOG ||
                block == Blocks.BIRCH_LOG || block == Blocks.JUNGLE_LOG ||
                block == Blocks.ACACIA_LOG || block == Blocks.DARK_OAK_LOG ||
                block == Blocks.MANGROVE_LOG || block == Blocks.CHERRY_LOG ||
                block == Blocks.CRIMSON_STEM || block == Blocks.WARPED_STEM;
    }

    @Override
    public String toString() {
        return "BuildingBlock{pos=" + pos + ", special=" + special + ", state=" + state + "}";
    }
}
