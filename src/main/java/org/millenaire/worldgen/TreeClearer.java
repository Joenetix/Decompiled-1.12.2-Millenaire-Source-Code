package org.millenaire.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.village.BuildingLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Clears trees around building locations before construction.
 * Ported from original 1.12.2 TreeClearer.java
 * 
 * This class intelligently removes trees that would interfere with buildings
 * while preserving trees that are outside the construction zone.
 */
public class TreeClearer {
    private static final int LEAF_CLEARING_Y_END = 30;
    private static final int LEAF_CLEARING_Y_START = -10;
    private static final int LOG_SEARCH_MARGIN = 4;
    private static final int LEAF_CLEAR_MARGIN = 2;
    private static final int NON_DECAY_RANGE = 3;

    private final ServerLevel world;
    private final BuildingLocation location;
    private final BuildingPlan plan;

    private final Set<BlockPos> pointsTested = new HashSet<>();
    private final Set<BlockPos> pointsTree = new HashSet<>();

    public TreeClearer(BuildingPlan plan, BuildingLocation location, ServerLevel world) {
        this.location = location;
        this.world = world;
        this.plan = plan;
    }

    /**
     * Clean up trees around the building location.
     * Finds trees in the construction zone and decays their logs and leaves.
     */
    public void cleanup() {
        findTrees();
        decayLogsAndLeaves();
    }

    /**
     * Decay logs and leaves that are inside the construction zone
     * but preserve leaves near trees that are being kept.
     */
    private void decayLogsAndLeaves() {
        // Build a set of positions near kept trees that should NOT be decayed
        Set<BlockPos> nonDecayPosSet = new HashSet<>();

        for (BlockPos logPos : pointsTree) {
            for (int dx = -NON_DECAY_RANGE; dx <= NON_DECAY_RANGE; dx++) {
                for (int dz = -NON_DECAY_RANGE; dz <= NON_DECAY_RANGE; dz++) {
                    for (int dy = -NON_DECAY_RANGE; dy <= NON_DECAY_RANGE; dy++) {
                        nonDecayPosSet.add(logPos.offset(dx, dy, dz));
                    }
                }
            }
        }

        int x = location.pos.getiX();
        int y = location.pos.getiY();
        int z = location.pos.getiZ();
        int orientation = location.orientation;

        int areaClearBefore = plan.areaToClearLengthBefore();
        int areaClearAfter = plan.areaToClearLengthAfter();
        int widthClearBefore = plan.areaToClearWidthBefore();
        int widthClearAfter = plan.areaToClearWidthAfter();
        int lengthOffset = plan.length() / 2;
        int widthOffset = plan.width() / 2;

        for (int dx = -areaClearBefore - LEAF_CLEAR_MARGIN; dx < plan.length() + areaClearAfter
                + LEAF_CLEAR_MARGIN; dx++) {
            for (int dz = -widthClearBefore - LEAF_CLEAR_MARGIN; dz < plan.width() + widthClearAfter
                    + LEAF_CLEAR_MARGIN; dz++) {
                boolean isXOutsidePlan = dx < 0 || dx > plan.length();
                boolean isZOutsidePlan = dz < 0 || dz > plan.width();

                // Only process positions outside the main building footprint
                if (isXOutsidePlan || isZOutsidePlan) {
                    for (int dy = LEAF_CLEARING_Y_START; dy < LEAF_CLEARING_Y_END; dy++) {
                        BlockPos p = adjustForOrientation(x, y + dy, z,
                                dx - lengthOffset, dz - widthOffset, orientation);

                        BlockState state = world.getBlockState(p);
                        Block block = state.getBlock();

                        if (isLogBlock(block)) {
                            // Decay logs that weren't marked as trees to keep
                            if (!pointsTree.contains(p)) {
                                world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                            }
                        } else if (isLeaveBlock(block)) {
                            // Decay leaves unless near a kept tree
                            if (!nonDecayPosSet.contains(p)) {
                                world.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Find trees around the building that should be preserved.
     * Trees are detected by finding log blocks connected to the ground.
     */
    private void findTrees() {
        int x = location.pos.getiX();
        int y = location.pos.getiY();
        int z = location.pos.getiZ();
        int orientation = location.orientation;

        int areaClearBefore = plan.areaToClearLengthBefore();
        int areaClearAfter = plan.areaToClearLengthAfter();
        int widthClearBefore = plan.areaToClearWidthBefore();
        int widthClearAfter = plan.areaToClearWidthAfter();
        int lengthOffset = plan.length() / 2;
        int widthOffset = plan.width() / 2;

        for (int dx = -areaClearBefore - LOG_SEARCH_MARGIN; dx < plan.length() + areaClearAfter
                + LOG_SEARCH_MARGIN; dx++) {
            for (int dz = -widthClearBefore - LOG_SEARCH_MARGIN; dz < plan.width() + widthClearAfter
                    + LOG_SEARCH_MARGIN; dz++) {
                boolean isXOutsidePlan = dx < 0 || dx > plan.length();
                boolean isZOutsidePlan = dz < 0 || dz > plan.width();

                // Only process positions outside the main building footprint
                if (isXOutsidePlan || isZOutsidePlan) {
                    for (int dy = LEAF_CLEARING_Y_START; dy < LEAF_CLEARING_Y_END; dy++) {
                        BlockPos p = adjustForOrientation(x, y + dy, z,
                                dx - lengthOffset, dz - widthOffset, orientation);

                        if (!pointsTested.contains(p)) {
                            Block block = world.getBlockState(p).getBlock();
                            Block blockBelow = world.getBlockState(p.below()).getBlock();

                            // Start tree detection from logs that are on ground
                            if (isLogBlock(block) && isGroundBlock(blockBelow)) {
                                handleTree(p);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Trace a tree from a starting log position.
     * Uses flood-fill to find all connected log blocks.
     */
    private void handleTree(BlockPos startingPos) {
        List<BlockPos> treePoints = new ArrayList<>();
        List<BlockPos> pointsToTest = new ArrayList<>();
        pointsToTest.add(startingPos);
        boolean abort = false;

        while (!pointsToTest.isEmpty() && !abort) {
            BlockPos p = pointsToTest.remove(pointsToTest.size() - 1);

            if (!pointsTested.contains(p)) {
                pointsTested.add(p);
                Block block = world.getBlockState(p).getBlock();

                if (isLogBlock(block)) {
                    treePoints.add(p);
                    // Add all neighbors to check
                    pointsToTest.add(p.above());
                    pointsToTest.add(p.below());
                    pointsToTest.add(p.north());
                    pointsToTest.add(p.south());
                    pointsToTest.add(p.east());
                    pointsToTest.add(p.west());
                }

                // Abort if tree is too large or too far from starting point
                double distSq = p.distSqr(startingPos);
                abort = treePoints.size() > 100 || distSq > 100;
            }
        }

        pointsTree.addAll(treePoints);
    }

    /**
     * Apply rotation to position based on building orientation.
     */
    private BlockPos adjustForOrientation(int x, int y, int z, int dx, int dz, int orientation) {
        return switch (orientation) {
            case 0 -> new BlockPos(x + dx, y, z + dz); // North
            case 1 -> new BlockPos(x - dz, y, z + dx); // West
            case 2 -> new BlockPos(x - dx, y, z - dz); // South
            case 3 -> new BlockPos(x + dz, y, z - dx); // East
            default -> new BlockPos(x + dx, y, z + dz);
        };
    }

    private boolean isLeaveBlock(Block block) {
        return block instanceof LeavesBlock;
    }

    private boolean isLogBlock(Block block) {
        // In 1.20.1, logs extend RotatedPillarBlock and have "log" in registry name
        if (block instanceof RotatedPillarBlock) {
            String name = block.builtInRegistryHolder().key().location().getPath();
            return name.contains("log") || name.contains("stem") || name.contains("wood");
        }
        return false;
    }

    private boolean isGroundBlock(Block block) {
        return block == Blocks.DIRT ||
                block == Blocks.GRASS_BLOCK ||
                block == Blocks.PODZOL ||
                block == Blocks.COARSE_DIRT ||
                block == Blocks.ROOTED_DIRT ||
                block == Blocks.MUD ||
                block == Blocks.STONE ||
                block == Blocks.GRAVEL ||
                block == Blocks.SAND ||
                block == Blocks.SANDSTONE;
    }
}
