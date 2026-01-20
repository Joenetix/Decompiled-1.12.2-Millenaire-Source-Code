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
 * Generates Olive Trees with gnarled trunks.
 * Ported from original 1.12.2 WorldGenOliveTree.java
 * Uses oak wood with custom olive leaves.
 */
public class OliveTreeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_TREE_HEIGHT = 4;

    public OliveTreeFeature() {
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

        // Olive trees use oak logs (gray-ish bark)
        BlockState logState = Blocks.OAK_LOG.defaultBlockState();
        // Use oak leaves for now (TODO: custom olive leaves)
        BlockState leavesState = Blocks.OAK_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.DISTANCE, 1);

        // Generate rounded canopy
        int canopyStart = position.getY() + treeHeight - 2;
        int canopyEnd = position.getY() + treeHeight + 2;

        for (int yPos = canopyStart; yPos <= canopyEnd; yPos++) {
            int radius = 2;
            if (yPos == canopyStart || yPos == canopyEnd) {
                radius = 1;
            }

            for (int xPos = position.getX() - radius; xPos <= position.getX() + radius; xPos++) {
                for (int zPos = position.getZ() - radius; zPos <= position.getZ() + radius; zPos++) {
                    int distX = Math.abs(xPos - position.getX());
                    int distZ = Math.abs(zPos - position.getZ());

                    // Skip corners for rounded shape
                    if (distX == radius && distZ == radius)
                        continue;

                    if (rand.nextInt(100) < 85) {
                        BlockPos leafPos = new BlockPos(xPos, yPos, zPos);
                        BlockState state = world.getBlockState(leafPos);
                        if (state.isAir() || state.getBlock() instanceof LeavesBlock) {
                            world.setBlock(leafPos, leavesState, 3);
                        }
                    }
                }
            }
        }

        // Generate slightly twisted trunk
        int xOffset = 0;
        int zOffset = 0;
        for (int yPos = 0; yPos < treeHeight; yPos++) {
            // Add slight twist to trunk
            if (yPos > 1 && rand.nextInt(4) == 0) {
                Direction twist = Direction.Plane.HORIZONTAL.getRandomDirection(rand);
                xOffset += twist.getStepX();
                zOffset += twist.getStepZ();
                // Limit twist
                xOffset = Math.max(-1, Math.min(1, xOffset));
                zOffset = Math.max(-1, Math.min(1, zOffset));
            }

            BlockPos logPos = position.offset(xOffset, yPos, zOffset);
            world.setBlock(logPos, logState, 3);
        }

        return true;
    }

    private boolean checkSpace(WorldGenLevel world, BlockPos position, int height) {
        for (int j = position.getY(); j <= position.getY() + height + 2; j++) {
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
