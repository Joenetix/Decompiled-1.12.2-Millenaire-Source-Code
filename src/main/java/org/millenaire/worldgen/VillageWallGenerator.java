package org.millenaire.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.culture.WallType;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.VillageMapInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates walls for villages based on WallType and VillageType.
 */
public class VillageWallGenerator {
    private final Level world;

    public VillageWallGenerator(Level world) {
        this.world = world;
    }

    public List<BuildingLocation> computeWallBuildingLocations(
            VillageType villageType, WallType wallType, int maxWallRadius,
            VillageMapInfo winfo, BlockPos centre) {

        List<WallSegment> wallSegments = new ArrayList<>();

        BuildingPlanSet wallPlanSet = wallType.villageWall;
        BuildingPlanSet towerPlanSet = wallType.villageWallTower;
        BuildingPlanSet gatewayPlanSet = wallType.villageWallGateway;
        BuildingPlanSet cornerPlanSet = wallType.villageWallCorner;

        // Default to tower if corner missing
        if (cornerPlanSet == null && towerPlanSet != null) {
            cornerPlanSet = towerPlanSet;
        }

        // Get plan lengths
        BuildingPlan wallPlan = wallPlanSet != null ? wallPlanSet.getFirstStartingPlan() : null;
        BuildingPlan towerPlan = towerPlanSet != null ? towerPlanSet.getFirstStartingPlan() : null;
        BuildingPlan gatewayPlan = gatewayPlanSet != null ? gatewayPlanSet.getFirstStartingPlan() : null;
        BuildingPlan cornerPlan = cornerPlanSet != null ? cornerPlanSet.getFirstStartingPlan() : null;

        int wallLength = wallPlan != null ? wallPlan.length() : 1;
        int towerLength = towerPlan != null ? towerPlan.length() : 0;
        int cornerLength = cornerPlan != null ? cornerPlan.length() : 0;

        // Calculate wall radius
        int wallRadius = gatewayPlan.length() / 2;
        int buildNb = 0;
        int wallRadiusLimit = maxWallRadius;
        if (maxWallRadius == 0) {
            wallRadiusLimit = villageType.getVillageRadius() - wallLength - cornerLength;
        }

        for (; wallRadius < wallRadiusLimit; buildNb++) {
            if (buildNb % (wallType.villageWallsBetweenTowers + 1) == wallType.villageWallsBetweenTowers) {
                wallRadius += towerLength;
            } else {
                wallRadius += wallLength;
            }
        }

        wallRadius += wallLength;
        wallRadius += cornerLength / 2;

        List<WallSide> sides = new ArrayList<>();
        sides.add(new WallSide(1, 0, 1, 0)); // East
        sides.add(new WallSide(0, 1, -1, 3)); // South
        sides.add(new WallSide(-1, 0, -1, 2)); // West
        sides.add(new WallSide(0, -1, 1, 1)); // North

        for (WallSide side : sides) {
            BlockPos p = centre.offset(wallRadius * side.xMultiplier, 0, wallRadius * side.zMultiplier);

            // Assume flat ground for now or find surface in future
            // int y = computeAverageYLevel(gatewayPlan, side.buildingOrientation, p);
            // Using centre Y for simplicity in initial port
            BlockPos gatewayPos = new BlockPos(p.getX(), centre.getY(), p.getZ());

            List<WallSegment> segmentsForward = new ArrayList<>();
            List<WallSegment> segmentsBackward = new ArrayList<>();
            int pos = gatewayPlan.length() / 2;

            for (int i = 0; pos < wallRadiusLimit; i++) {
                BuildingPlanSet currentPlanSet;
                boolean spawn;
                int segmentLength;

                if (i % (wallType.villageWallsBetweenTowers + 1) == wallType.villageWallsBetweenTowers) {
                    currentPlanSet = towerPlanSet;
                    spawn = wallType.villageWallTowerSpawn;
                    segmentLength = towerLength;
                } else {
                    currentPlanSet = wallPlanSet;
                    spawn = wallType.villageWallSpawn;
                    segmentLength = wallLength;
                }

                if (currentPlanSet != null) {
                    buildNextElements(centre, side, segmentsBackward, segmentsForward, wallRadius, pos, currentPlanSet,
                            spawn, true);
                }
                pos += segmentLength;
            }

            // Add final wall segment before corner
            if (wallPlanSet != null) {
                buildNextElements(centre, side, segmentsBackward, segmentsForward, wallRadius, pos, wallPlanSet,
                        wallType.villageWallSpawn, true);
            }
            pos += wallLength;

            // Add Corner
            if (cornerPlanSet != null) {
                buildNextElements(centre, side, segmentsBackward, segmentsForward, wallRadius, pos, cornerPlanSet,
                        wallType.villageWallCornerSpawn, false);
            }

            Collections.reverse(segmentsBackward);
            wallSegments.addAll(segmentsBackward);

            // Add Gateway
            WallSegment gatewaySegment = new WallSegment(
                    new BuildingLocation(gatewayPlanSet.getFirstStartingPlan(), new Point(gatewayPos),
                            side.buildingOrientation));
            wallSegments.add(gatewaySegment);

            wallSegments.addAll(segmentsForward);
        }

        // Apply wall processing from original 1.12.2 logic
        computeWallConnections(wallSegments);
        smoothWalls(wallSegments, wallType);
        // Note: addSlopes and capWalls require slope plan sets which may not be loaded
        // yet
        // They can be added once WallType is extended with slope plans

        // Convert segments to locations
        List<BuildingLocation> locations = new ArrayList<>();
        for (WallSegment seg : wallSegments) {
            locations.add(seg.location);
        }
        return locations;
    }

    /**
     * Compute connections between adjacent wall segments.
     * From original VillageWallGenerator.computeWallConnections()
     */
    private void computeWallConnections(List<WallSegment> wallSegments) {
        for (int i = 0; i < wallSegments.size(); i++) {
            WallSegment previousSegment = i == 0 ? wallSegments.get(wallSegments.size() - 1) : wallSegments.get(i - 1);
            WallSegment currentSegment = wallSegments.get(i);

            // Check if segments are close enough to be connected
            double distance = Math.sqrt(
                    Math.pow(previousSegment.location.pos.getiX() - currentSegment.location.pos.getiX(), 2) +
                            Math.pow(previousSegment.location.pos.getiZ() - currentSegment.location.pos.getiZ(), 2));
            int maxDistance = (previousSegment.location.length + currentSegment.location.length) / 2
                    + 4;

            boolean connectedToPrevious = distance < maxDistance;
            if (connectedToPrevious) {
                previousSegment.nextSegment = currentSegment;
                currentSegment.previousSegment = previousSegment;
            }
        }
    }

    /**
     * Smooth wall Y-levels for a more natural appearance.
     * From original VillageWallGenerator.smoothWalls()
     */
    private void smoothWalls(List<WallSegment> wallSegments, WallType wallType) {
        if (wallSegments.isEmpty())
            return;

        List<Float> referenceY = new ArrayList<>();
        for (WallSegment segment : wallSegments) {
            referenceY.add((float) segment.location.pos.getiY());
        }

        // Run smoothing passes (default 3 if not configured)
        int nbSmoothRuns = wallType.nbSmoothRuns > 0 ? wallType.nbSmoothRuns : 3;

        for (int run = 0; run < nbSmoothRuns; run++) {
            List<Float> adjustedY = new ArrayList<>();

            for (int i = 0; i < referenceY.size(); i++) {
                int previousId = i == 0 ? referenceY.size() - 1 : i - 1;
                int nextId = i == referenceY.size() - 1 ? 0 : i + 1;
                WallSegment segment = wallSegments.get(i);

                int nbPoint = 1;
                float average = referenceY.get(i);

                if (segment.previousSegment != null) {
                    nbPoint++;
                    average += referenceY.get(previousId);
                }

                if (segment.nextSegment != null) {
                    nbPoint++;
                    average += referenceY.get(nextId);
                }

                average /= nbPoint;
                adjustedY.add(average);
            }

            for (int i = 0; i < referenceY.size(); i++) {
                if (!referenceY.get(i).equals(adjustedY.get(i))) {
                    referenceY.set(i, adjustedY.get(i));
                }
            }
        }

        // Apply final Y-levels
        for (int i = 0; i < referenceY.size(); i++) {
            int finalY = Math.round(referenceY.get(i));
            if (wallSegments.get(i).location.pos.getiY() != finalY) {
                wallSegments.get(i).setYLevel(finalY);
            }
        }
    }

    private void buildNextElements(BlockPos centre, WallSide side,
            List<WallSegment> locationsBackward,
            List<WallSegment> locationsForward,
            int wallRadius, int pos,
            BuildingPlanSet planSet, boolean spawn, boolean buildNegative) {

        int segmentLength = planSet.getFirstStartingPlan().length();

        // Forward Logic
        int deltaXForward = 0;
        int deltaZForward = 0;

        if (side.xMultiplier != 0) {
            deltaZForward = (pos + segmentLength / 2) * side.direction;
            BlockPos p = centre.offset(wallRadius * side.xMultiplier, 0, deltaZForward);
            locationsForward.add(createSegment(planSet, p, side.buildingOrientation));
        } else {
            deltaXForward = (pos + segmentLength / 2) * side.direction;
            BlockPos p = centre.offset(deltaXForward, 0, wallRadius * side.zMultiplier);
            locationsForward.add(createSegment(planSet, p, side.buildingOrientation));
        }

        // Backward Logic
        if (buildNegative) {
            int deltaOffset = (segmentLength % 2 == 1) ? side.direction : 0;

            if (side.xMultiplier != 0) {
                // For Z-axis walls, backward means negative Z direction relative to side
                int db = deltaZForward + deltaOffset;
                // Wait, original logic: p = centre.getRelative(radius*x, 0, -deltaZ)
                // If side.direction is 1, forward is +Z, backward is -Z.
                BlockPos p = centre.offset(wallRadius * side.xMultiplier, 0, -deltaZForward - deltaOffset);
                locationsBackward.add(createSegment(planSet, p, side.buildingOrientation));
            } else {
                BlockPos p = centre.offset(-deltaXForward - deltaOffset, 0, wallRadius * side.zMultiplier);
                locationsBackward.add(createSegment(planSet, p, side.buildingOrientation));
            }
        }
    }

    private WallSegment createSegment(BuildingPlanSet planSet, BlockPos pos, int orientation) {
        BuildingPlan plan = planSet.getRandomStartingPlan();
        // Adjust orientation based on plan's inherent orientation if needed
        // For now, simple standard orientation
        int finalOrientation = (orientation + plan.buildingOrientation()) % 4;
        BuildingLocation loc = new BuildingLocation(plan, new Point(pos), finalOrientation);
        return new WallSegment(loc);
    }

    public static class WallSegment {
        public BuildingLocation location;
        public WallSegment previousSegment = null;
        public WallSegment nextSegment = null;
        public boolean sloppable = false;
        public int yTowardsPrevious;
        public int yTowardsNext;

        public WallSegment(BuildingLocation location) {
            this.location = location;
            this.yTowardsPrevious = location.pos.getiY();
            this.yTowardsNext = location.pos.getiY();
        }

        public void setYLevel(int newY) {
            BuildingLocation newLoc = location.clone();
            newLoc.pos = new Point(location.pos.getiX(), newY, location.pos.getiZ());
            this.location = newLoc;
            this.yTowardsPrevious = newY;
            this.yTowardsNext = newY;
        }
    }

    private static class WallSide {
        public final int xMultiplier;
        public final int zMultiplier;
        public final int direction;
        public final int buildingOrientation;

        public WallSide(int xMultiplier, int zMultiplier, int direction, int buildingOrientation) {
            this.xMultiplier = xMultiplier;
            this.zMultiplier = zMultiplier;
            this.direction = direction;
            this.buildingOrientation = buildingOrientation;
        }
    }
}
