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
 * Generates Pistachio Trees for Mediterranean cultures.
 * Ported from original 1.12.2 WorldGenPistachio.java
 * Similar structure to cherry trees but smaller.
 */
public class PistachioTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_TREE_HEIGHT = 4;

    public PistachioTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel world = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();

        int treeHeight = rand.nextInt(2) + MIN_TREE_HEIGHT;

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

        // Pistachio uses acacia-like wood
        BlockState logState = Blocks.ACACIA_LOG.defaultBlockState();
        // Green leaves (TODO: custom pistachio leaves)
        BlockState leavesState = Blocks.ACACIA_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false);

        // Generate compact canopy
        for (int yPos = position.getY() + 2; yPos <= position.getY() + treeHeight + 1; yPos++) {
            int radius = 2;
            if (yPos < position.getY() + 3) {
                radius = 1;
            } else if (yPos > position.getY() + treeHeight) {
                radius = 1;
            }

            for (int xPos = position.getX() - radius; xPos <= position.getX() + radius; xPos++) {
                int distX = Math.abs(xPos - position.getX());
                for (int zPos = position.getZ() - radius; zPos <= position.getZ() + radius; zPos++) {
                    int distZ = Math.abs(zPos - position.getZ());

                    int chance = 90;
                    if (distX == radius && distZ == radius) {
                        chance = 30;
                    } else if (distX == radius || distZ == radius) {
                        chance = 75;
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

        // Small branches (40% chance per direction)
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (rand.nextInt(100) < 40) {
                int branchY = position.getY() + treeHeight - 1;
                BlockPos branchPos = position.offset(facing.getStepX(), branchY - position.getY(), facing.getStepZ());
                BlockState state = world.getBlockState(branchPos);
                if (state.isAir() || state.getBlock() instanceof LeavesBlock) {
                    world.setBlock(branchPos, logState, 3);
                }
            }
        }

        return true;
    }

    private boolean checkSpace(WorldGenLevel world, BlockPos position, int height) {
        for (int j = position.getY(); j <= position.getY() + height + 1; j++) {
            int radius = 2;
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
                state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
    }
}
