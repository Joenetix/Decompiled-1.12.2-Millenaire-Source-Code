package org.millenaire.common.buildingplan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.IntPoint;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.BuildingLocation;

/**
 * Clears trees from around building locations by decaying logs and leaves.
 * Ported from 1.12.2 to 1.20.1.
 */
public class TreeClearer {
    private static final int LEAF_CLEARING_Y_END = 30;
    private static final int LEAF_CLEARING_Y_START = -10;
    private static final int LOG_SEARCH_MARGIN = 4;
    private static final int LEAF_CLEAR_MARGIN = 2;
    private static final int NON_DECAY_RANGE = 3;

    public static long cumulatedTimeTreeFinding = 0L;
    public static long cumulatedTimeLeaveDecay = 0L;

    private final Level world;
    private final BuildingLocation location;
    private final BuildingPlan plan;
    final Set<IntPoint> pointsTested = new HashSet<>();
    final Set<IntPoint> pointsTree = new HashSet<>();

    BlockState decayingLeaves = Blocks.AIR.defaultBlockState();
    BlockState decayedTree = Blocks.AIR.defaultBlockState();
    BlockState nonDecayingLeaves = null;
    BlockState nonDecayedTree = null;
    boolean testMode = false;

    public TreeClearer(BuildingPlan plan, BuildingLocation location, Level world) {
        this.location = location;
        this.world = world;
        this.plan = plan;
        if (this.testMode) {
            this.decayingLeaves = Blocks.GLASS.defaultBlockState();
            this.decayedTree = Blocks.GOLD_BLOCK.defaultBlockState();
            this.nonDecayingLeaves = Blocks.BLACK_WOOL.defaultBlockState();
            this.nonDecayedTree = Blocks.DIAMOND_BLOCK.defaultBlockState();
        }
    }

    /**
     * Clean up trees around the building location.
     */
    public void cleanup() {
        long startTime = System.nanoTime();
        this.findTrees();
        cumulatedTimeTreeFinding = cumulatedTimeTreeFinding + (System.nanoTime() - startTime);
        startTime = System.nanoTime();
        this.decayLogsAndLeaves();
        cumulatedTimeLeaveDecay = cumulatedTimeLeaveDecay + (System.nanoTime() - startTime);
    }

    private void decayLogsAndLeaves() {
        long startTime = System.nanoTime();
        Set<IntPoint> nonDecayPosSet = new HashSet<>();

        // Mark positions near trees as non-decay
        for (IntPoint logPos : this.pointsTree) {
            for (int dx = -NON_DECAY_RANGE; dx <= NON_DECAY_RANGE; dx++) {
                for (int dz = -NON_DECAY_RANGE; dz <= NON_DECAY_RANGE; dz++) {
                    for (int dy = -NON_DECAY_RANGE; dy <= NON_DECAY_RANGE; dy++) {
                        IntPoint nonDecayPoint = logPos.getRelative(dx, dy, dz);
                        nonDecayPosSet.add(nonDecayPoint);
                        if (this.testMode && nonDecayingLeaves != null) {
                            Block block = nonDecayPoint.getBlock(this.world);
                            if (!this.isLogBlock(block) && block != nonDecayedTree.getBlock()) {
                                nonDecayPoint.setBlockState(this.world, Blocks.WHITE_WOOL.defaultBlockState());
                            }
                        }
                    }
                }
            }

            if (this.nonDecayedTree != null) {
                logPos.setBlockState(this.world, this.nonDecayedTree);
            }
        }

        int nbLeavesDecayed = 0;
        int nbLeavesSpared = 0;
        int x = this.location.pos.getiX();
        int z = this.location.pos.getiZ();
        int orientation = this.location.orientation;
        int randomWoolColour = MillCommonUtilities.randomInt(16);

        // Decay leaves and logs in the clearing area
        for (int dx = -this.plan.areaToClearLengthBefore - LEAF_CLEAR_MARGIN; dx < this.plan.length
                + this.plan.areaToClearLengthAfter + LEAF_CLEAR_MARGIN; dx++) {
            for (int dz = -this.plan.areaToClearWidthBefore - LEAF_CLEAR_MARGIN; dz < this.plan.width
                    + this.plan.areaToClearWidthAfter + LEAF_CLEAR_MARGIN; dz++) {

                boolean isXOutsidePlan = dx < 0 || dx > this.plan.length;
                boolean isZOutsidePlan = dz < 0 || dz > this.plan.width;

                if (isXOutsidePlan || isZOutsidePlan) {
                    for (int y = this.location.pos.getiY() + LEAF_CLEARING_Y_START; y < this.location.pos.getiY()
                            + LEAF_CLEARING_Y_END; y++) {
                        IntPoint p = BuildingPlan.adjustForOrientation(
                                x, y, z, dx - this.plan.lengthOffset, dz - this.plan.widthOffset, orientation)
                                .getIntPoint();

                        Block block = p.getBlock(this.world);
                        if (this.isLogBlock(block)) {
                            if (!this.pointsTree.contains(p)) {
                                p.setBlockState(this.world, this.decayedTree);
                            }
                        } else if (this.isLeaveBlock(block)) {
                            if (!nonDecayPosSet.contains(p)) {
                                nbLeavesDecayed++;
                                p.setBlockState(this.world, this.decayingLeaves);
                            } else {
                                nbLeavesSpared++;
                                if (this.nonDecayingLeaves != null) {
                                    p.setBlockState(this.world, this.nonDecayingLeaves);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (MillConfigValues.LogWorldGeneration >= 1) {
            MillLog.debug(this, "Finished decaying " + nbLeavesDecayed + " leaves. Spared " + nbLeavesSpared +
                    ". Total time in ns: " + (System.nanoTime() - startTime));
        }
    }

    private void findTrees() {
        int x = this.location.pos.getiX();
        int z = this.location.pos.getiZ();
        int orientation = this.location.orientation;

        for (int dx = -this.plan.areaToClearLengthBefore - LOG_SEARCH_MARGIN; dx < this.plan.length
                + this.plan.areaToClearLengthAfter + LOG_SEARCH_MARGIN; dx++) {
            for (int dz = -this.plan.areaToClearWidthBefore - LOG_SEARCH_MARGIN; dz < this.plan.width
                    + this.plan.areaToClearWidthAfter + LOG_SEARCH_MARGIN; dz++) {

                boolean isXOutsidePlan = dx < 0 || dx > this.plan.length;
                boolean isZOutsidePlan = dz < 0 || dz > this.plan.width;

                if (isXOutsidePlan || isZOutsidePlan) {
                    for (int y = this.location.pos.getiY() + LEAF_CLEARING_Y_START; y < this.location.pos.getiY()
                            + LEAF_CLEARING_Y_END; y++) {
                        IntPoint p = BuildingPlan.adjustForOrientation(
                                x, y, z, dx - this.plan.lengthOffset, dz - this.plan.widthOffset, orientation)
                                .getIntPoint();

                        if (!this.pointsTested.contains(p)
                                && this.isLogBlock(p.getBlock(this.world))
                                && BlockItemUtilities.isBlockGround(p.getBelow().getBlock(this.world))) {
                            this.handleTree(p);
                        }
                    }
                }
            }
        }
    }

    private void handleTree(IntPoint startingPos) {
        List<IntPoint> treePoints = new ArrayList<>();
        List<IntPoint> pointsToTest = new ArrayList<>();
        pointsToTest.add(startingPos);
        boolean abort = false;

        while (!pointsToTest.isEmpty() && !abort) {
            IntPoint p = pointsToTest.get(pointsToTest.size() - 1);
            if (!this.pointsTested.contains(p)) {
                this.pointsTested.add(p);
                Block block = p.getBlock(this.world);
                if (this.isLogBlock(block)) {
                    treePoints.add(p);
                    pointsToTest.addAll(p.getAllNeightbours());
                }

                abort = treePoints.size() > 100 || p.horizontalDistanceToSquared(startingPos) > 100;
            }

            pointsToTest.remove(p);
        }

        this.pointsTree.addAll(treePoints);
    }

    /**
     * Check if block is a leave block using 1.20.1 block tags.
     */
    private boolean isLeaveBlock(Block block) {
        return block.defaultBlockState().is(BlockTags.LEAVES);
    }

    /**
     * Check if block is a log block using 1.20.1 block tags.
     */
    private boolean isLogBlock(Block block) {
        return block.defaultBlockState().is(BlockTags.LOGS);
    }
}
