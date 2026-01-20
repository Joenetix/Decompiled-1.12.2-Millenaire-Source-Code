package org.millenaire.worldgen.trees;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.HashSet;
import java.util.Set;

/**
 * Generates Apple Trees with a distinctive branching pattern.
 * Ported from original 1.12.2 WorldGenAppleTree.java
 */
public class AppleTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_TREE_HEIGHT = 5;

    public AppleTreeFeature() {
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
        if (!groundState.is(Blocks.GRASS_BLOCK) && !groundState.is(Blocks.DIRT) &&
                !groundState.is(Blocks.PODZOL) && !groundState.is(Blocks.COARSE_DIRT)) {
            return false;
        }

        // Build trunk (5 blocks tall)
        BlockState oakLog = Blocks.OAK_LOG.defaultBlockState();
        for (int yPos = 0; yPos < 5; yPos++) {
            BlockPos upN = position.above(yPos);
            BlockState state = world.getBlockState(upN);
            if (state.isAir() || state.getBlock() instanceof LeavesBlock) {
                world.setBlock(upN, oakLog, 3);
            }
        }

        // Generate branches in 4 horizontal directions
        // Using apple leaves (TODO: register custom apple leaves block)
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.DISTANCE, 1);

        Set<Direction> branchFacings = new HashSet<>();
        branchFacings.add(Direction.Plane.HORIZONTAL.getRandomDirection(rand));
        for (int i = 0; i < 3; i++) {
            branchFacings.add(Direction.Plane.HORIZONTAL.getRandomDirection(rand));
        }

        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int branchStartY = 3 + rand.nextInt(1);
            int horizontalSize = 3 - rand.nextInt(2);
            int xPos = position.getX();
            int zPos = position.getZ();
            int yPos = position.getY() + branchStartY;
            int curve = rand.nextBoolean() ? 1 : -1;

            for (int hPos = 0; hPos < horizontalSize; hPos++) {
                if (yPos < position.getY() + treeHeight && rand.nextFloat() < 0.7f) {
                    yPos++;
                }

                if (facing.getStepX() != 0) {
                    xPos += facing.getStepX();
                    if (rand.nextFloat() < 0.15f) {
                        zPos += curve;
                    }
                } else {
                    zPos += facing.getStepZ();
                    if (rand.nextFloat() < 0.15f) {
                        xPos += curve;
                    }
                }

                BlockPos branchPos = new BlockPos(xPos, yPos, zPos);
                BlockState state = world.getBlockState(branchPos);

                if (state.isAir() || state.getBlock() instanceof LeavesBlock) {
                    // Place branch log with correct axis
                    BlockState branchLog = oakLog.setValue(RotatedPillarBlock.AXIS, facing.getAxis());
                    world.setBlock(branchPos, branchLog, 3);

                    // Place leaves around branch
                    for (int dx = -1; dx < 2; dx++) {
                        for (int dz = -1; dz < 2; dz++) {
                            for (int dy = -1; dy < 2; dy++) {
                                BlockPos leafPos = branchPos.offset(dx, dy, dz);
                                BlockState leafState = world.getBlockState(leafPos);
                                if (leafState.isAir() && rand.nextInt(100) < 50) {
                                    world.setBlock(leafPos, leaves, 3);
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean checkSpace(WorldGenLevel world, BlockPos position, int height) {
        for (int j = position.getY(); j <= position.getY() + 1 + height; j++) {
            int radius = 1;
            if (j == position.getY()) {
                radius = 0;
            }
            if (j >= position.getY() + 1 + height - 2) {
                radius = 2;
            }

            BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
            for (int x = position.getX() - radius; x <= position.getX() + radius; x++) {
                for (int z = position.getZ() - radius; z <= position.getZ() + radius; z++) {
                    if (j < 0 || j >= world.getMaxBuildHeight()) {
                        return false;
                    }
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
}
