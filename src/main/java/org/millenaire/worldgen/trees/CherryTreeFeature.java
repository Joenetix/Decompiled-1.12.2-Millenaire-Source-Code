package org.millenaire.worldgen.trees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generates Cherry Trees with pink blossoms.
 * Ported from original 1.12.2 WorldGenCherry.java
 */
public class CherryTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_TREE_HEIGHT = 6;

    public CherryTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();

        int treeHeight = rand.nextInt(2) + MIN_TREE_HEIGHT;

        // Check world height bounds
        if (position.getY() < 1 || position.getY() + treeHeight + 1 > world.getMaxBuildHeight()) {
            return false;
        }

        // Check for obstacles
        if (!checkSpace(world, position, treeHeight)) {
            return false;
        }

        // Check if ground can sustain plant
        BlockState groundState = world.getBlockState(position.below());
        if (!canSustainTree(groundState)) {
            return false;
        }

        // Use Minecraft 1.20's native cherry wood if available, else spruce
        BlockState logState = Blocks.CHERRY_LOG != null ? Blocks.CHERRY_LOG.defaultBlockState()
                : Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState leavesState = Blocks.CHERRY_LEAVES != null
                ? Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, false)
                : Blocks.PINK_PETALS.defaultBlockState(); // Fallback

        // Generate leaves canopy first
        for (int yPos = position.getY() + 2; yPos <= position.getY() + treeHeight + 1; yPos++) {
            int leavesRadius = 3;
            if (yPos < position.getY() + 4) {
                leavesRadius -= position.getY() + 4 - yPos;
            } else if (yPos > position.getY() + treeHeight - 2) {
                leavesRadius -= yPos - (position.getY() + treeHeight - 2);
            }

            for (int xPos = position.getX() - leavesRadius; xPos <= position.getX() + leavesRadius; xPos++) {
                int distX = Math.abs(xPos - position.getX());
                for (int zPos = position.getZ() - leavesRadius; zPos <= position.getZ() + leavesRadius; zPos++) {
                    int distZ = Math.abs(zPos - position.getZ());

                    int chance = 95;
                    if (distX == leavesRadius && distZ == leavesRadius) {
                        chance = 0; // No leaves at corners
                    } else if (distX == leavesRadius || distZ == leavesRadius) {
                        chance = 80; // Reduced chance at edges
                    }

                    if (rand.nextInt(100) < chance) {
                        BlockPos leafPos = new BlockPos(xPos, yPos, zPos);
                        BlockState state = world.getBlockState(leafPos);
                        if (state.isAir() || state.getBlock() instanceof LeavesBlock) {
                            world.setBlock(leafPos, leavesState, 3);
                        }
                    }
                }
            }
        }

        // Generate trunk
        for (int yPos = 0; yPos < treeHeight; yPos++) {
            BlockPos logPos = position.above(yPos);
            BlockState state = world.getBlockState(logPos);
            if (state.isAir() || state.getBlock() instanceof LeavesBlock) {
                world.setBlock(logPos, logState, 3);
            }
        }

        // Generate branches (60% chance per direction)
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (rand.nextInt(100) < 60) {
                int branchMaxY = treeHeight - rand.nextInt(2);
                int branchMinY = 3 + rand.nextInt(2);
                int horizontalOffset = 2 - rand.nextInt(2);
                int xPos = position.getX();
                int zPos = position.getZ();

                for (int yPos = 0; yPos < branchMaxY; yPos++) {
                    int yWorld = position.getY() + yPos;
                    if (yPos >= branchMinY && horizontalOffset > 0) {
                        xPos += facing.getStepX();
                        zPos += facing.getStepZ();
                        horizontalOffset--;
                    }

                    BlockPos branchPos = new BlockPos(xPos, yWorld, zPos);
                    BlockState state = world.getBlockState(branchPos);
                    if (state.isAir() || state.getBlock() instanceof LeavesBlock) {
                        world.setBlock(branchPos, logState, 3);
                    }
                }
            }
        }

        return true;
    }

    private boolean checkSpace(WorldGenLevel world, BlockPos position, int height) {
        for (int j = position.getY(); j <= position.getY() + 1 + height; j++) {
            int radius = 1;
            if (j == position.getY())
                radius = 0;
            if (j >= position.getY() + 1 + height - 2)
                radius = 2;

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int x = position.getX() - radius; x <= position.getX() + radius; x++) {
                for (int z = position.getZ() - radius; z <= position.getZ() + radius; z++) {
                    mutablePos.set(x, j, z);
                    BlockState state = world.getBlockState(mutablePos);
                    if (!state.isAir() && !(state.getBlock() instanceof LeavesBlock)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canSustainTree(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) ||
                state.is(Blocks.PODZOL) || state.is(Blocks.COARSE_DIRT);
    }
}
