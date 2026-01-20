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
 * Generates Sakura (Japanese Flowering Cherry) Trees.
 * Ported from original 1.12.2 WorldGenSakura.java
 * Similar to cherry but with different proportions.
 */
public class SakuraTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_TREE_HEIGHT = 5;

    public SakuraTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();

        int treeHeight = rand.nextInt(3) + MIN_TREE_HEIGHT;

        if (position.getY() < 1 || position.getY() + treeHeight + 1 > world.getMaxBuildHeight()) {
            return false;
        }

        if (!checkSpace(world, position, treeHeight)) {
            return false;
        }

        BlockState groundState = world.getBlockState(position.below());
        if (!canSustainTree(groundState)) {
            return false;
        }

        // Use cherry blocks from 1.20 for sakura
        BlockState logState = Blocks.CHERRY_LOG != null ? Blocks.CHERRY_LOG.defaultBlockState()
                : Blocks.BIRCH_LOG.defaultBlockState();
        BlockState leavesState = Blocks.CHERRY_LEAVES != null
                ? Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, false)
                : Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, false);

        // Sakura has a wider, lower canopy
        for (int yPos = position.getY() + 2; yPos <= position.getY() + treeHeight; yPos++) {
            int radius = 3;
            if (yPos < position.getY() + 3) {
                radius = 2;
            } else if (yPos > position.getY() + treeHeight - 1) {
                radius = 2;
            }

            for (int xPos = position.getX() - radius; xPos <= position.getX() + radius; xPos++) {
                int distX = Math.abs(xPos - position.getX());
                for (int zPos = position.getZ() - radius; zPos <= position.getZ() + radius; zPos++) {
                    int distZ = Math.abs(zPos - position.getZ());

                    int chance = 90;
                    if (distX == radius && distZ == radius) {
                        chance = 0;
                    } else if (distX == radius || distZ == radius) {
                        chance = 70;
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
        for (int yPos = 0; yPos < treeHeight - 1; yPos++) {
            BlockPos logPos = position.above(yPos);
            world.setBlock(logPos, logState, 3);
        }

        // Sakura has drooping branches
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (rand.nextInt(100) < 70) {
                int branchY = position.getY() + treeHeight - 2;
                int branchLength = 2 + rand.nextInt(2);

                for (int i = 1; i <= branchLength; i++) {
                    int xPos = position.getX() + facing.getStepX() * i;
                    int zPos = position.getZ() + facing.getStepZ() * i;
                    int yPos = branchY - (i > 1 ? 1 : 0); // Droop

                    BlockPos branchPos = new BlockPos(xPos, yPos, zPos);
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
        for (int j = position.getY(); j <= position.getY() + height; j++) {
            int radius = 3;
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
                state.is(Blocks.PODZOL);
    }
}
