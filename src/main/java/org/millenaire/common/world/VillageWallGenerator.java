package org.millenaire.common.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.World;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.culture.WallType;
import org.millenaire.common.pathing.atomicstryker.RegionMapper;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.village.VillageMapInfo;

public class VillageWallGenerator {
   World world;

   public VillageWallGenerator(World world) {
      this.world = world;
   }

   private void addSlopes(List<VillageWallGenerator.WallSegment> wallSegments, WallType wallType) {
      BuildingPlanSet[] leftSlopes = new BuildingPlanSet[]{
         wallType.villageWallSlope1Left,
         wallType.villageWallSlope2Left,
         wallType.villageWallSlope3Left,
         wallType.villageWallSlope4Left,
         wallType.villageWallSlope5Left,
         wallType.villageWallSlope6Left
      };
      BuildingPlanSet[] rightSlopes = new BuildingPlanSet[]{
         wallType.villageWallSlope1Right,
         wallType.villageWallSlope2Right,
         wallType.villageWallSlope3Right,
         wallType.villageWallSlope4Right,
         wallType.villageWallSlope5Right,
         wallType.villageWallSlope6Right
      };

      for (VillageWallGenerator.WallSegment segment : wallSegments) {
         segment.sloppable = segment.location.getPlan().buildingKey.equals(wallType.villageWall.key);
      }

      for (VillageWallGenerator.WallSegment segment : wallSegments) {
         int y = segment.location.pos.getiY();
         if (segment.previousSegment != null && segment.nextSegment != null) {
            if (segment.previousSegment.location.pos.y < y && segment.nextSegment.location.pos.y < y) {
               segment.setYLevel((int)Math.max(segment.previousSegment.location.pos.y, segment.nextSegment.location.pos.y));
               y = segment.location.pos.getiY();
            } else if (segment.previousSegment.location.pos.y > y && segment.nextSegment.location.pos.y > y) {
               segment.setYLevel((int)Math.min(segment.previousSegment.location.pos.y, segment.nextSegment.location.pos.y));
               y = segment.location.pos.getiY();
            }
         }

         if (segment.sloppable) {
            if (segment.previousSegment != null && !segment.previousSegment.sloppable && segment.previousSegment.location.pos.y < y) {
               segment.setYLevel(segment.previousSegment.location.pos.getiY());
               y = segment.location.pos.getiY();
            } else if (segment.nextSegment != null && !segment.nextSegment.sloppable && segment.nextSegment.location.pos.y < y) {
               segment.setYLevel(segment.nextSegment.location.pos.getiY());
               y = segment.location.pos.getiY();
            }

            String slopeKey = null;
            if (segment.nextSegment != null && segment.nextSegment.yTowardsPrevious > y) {
               int deltaY = segment.nextSegment.yTowardsPrevious - y;
               boolean slopeSwapped = false;

               for (int slopeDelta = leftSlopes.length; slopeDelta != 0 && !slopeSwapped; slopeDelta--) {
                  if (deltaY >= slopeDelta && leftSlopes[slopeDelta - 1] != null) {
                     slopeKey = leftSlopes[slopeDelta - 1].key;
                     segment.yTowardsNext += slopeDelta;
                     slopeSwapped = true;
                  }
               }
            } else if (segment.previousSegment != null && segment.previousSegment.yTowardsNext > y) {
               int deltaY = segment.previousSegment.yTowardsNext - y;
               boolean slopeSwapped = false;

               for (int slopeDeltax = rightSlopes.length; slopeDeltax != 0 && !slopeSwapped; slopeDeltax--) {
                  if (deltaY >= slopeDeltax && rightSlopes[slopeDeltax - 1] != null) {
                     slopeKey = rightSlopes[slopeDeltax - 1].key;
                     segment.yTowardsPrevious += slopeDeltax;
                     slopeSwapped = true;
                  }
               }
            }

            if (slopeKey != null) {
               int locationLevel = segment.location.level;
               segment.location = segment.location.createLocationForAlternateBuilding(slopeKey);
               segment.location.level = locationLevel;
               MillLog.temp(
                  null,
                  "Placed slope " + segment.location.planKey + ", yTowardsPrevious: " + segment.yTowardsPrevious + ", yTowardsNext:" + segment.yTowardsNext
               );
            }
         }
      }
   }

   private void buildNextElements(
      RegionMapper regionMapper,
      Point centre,
      VillageType villageType,
      WallType wallType,
      List<VillageWallGenerator.WallSegment> locationsBackward,
      List<VillageWallGenerator.WallSegment> locationsForward,
      VillageMapInfo winfo,
      int wallRadius,
      VillageWallGenerator.WallSide side,
      int pos,
      BuildingPlanSet planSet,
      boolean spawn,
      boolean buildNegative
   ) throws MillLog.MillenaireException {
      int segmentLength = planSet.getFirstStartingPlan().length;
      if (side.xMultiplier != 0) {
         int deltaZ = (pos + segmentLength / 2) * side.direction;
         Point p = centre.getRelative(wallRadius * side.xMultiplier, 0.0, deltaZ);
         VillageWallGenerator.WallSegment segment = this.computeWallElementLocation(
            regionMapper, centre, villageType, wallType, winfo, planSet, p, side.buildingOrientation, spawn
         );
         if (segment != null) {
            locationsForward.add(segment);
         }

         if (buildNegative) {
            if (segmentLength % 2 == 1) {
               deltaZ += side.direction;
            }

            p = centre.getRelative(wallRadius * side.xMultiplier, 0.0, -deltaZ);
            segment = this.computeWallElementLocation(regionMapper, centre, villageType, wallType, winfo, planSet, p, side.buildingOrientation, spawn);
            if (segment != null) {
               locationsBackward.add(segment);
            }
         }
      } else {
         int deltaX = (pos + segmentLength / 2) * side.direction;
         Point px = centre.getRelative(deltaX, 0.0, wallRadius * side.zMultiplier);
         VillageWallGenerator.WallSegment segmentx = this.computeWallElementLocation(
            regionMapper, centre, villageType, wallType, winfo, planSet, px, side.buildingOrientation, spawn
         );
         if (segmentx != null) {
            locationsForward.add(segmentx);
         }

         if (buildNegative) {
            if (segmentLength % 2 == 1) {
               deltaX += side.direction;
            }

            px = centre.getRelative(-deltaX, 0.0, wallRadius * side.zMultiplier);
            segmentx = this.computeWallElementLocation(regionMapper, centre, villageType, wallType, winfo, planSet, px, side.buildingOrientation, spawn);
            if (segmentx != null) {
               locationsBackward.add(segmentx);
            }
         }
      }
   }

   private void capWalls(
      Point villageCentre,
      List<VillageWallGenerator.WallSegment> wallSegments,
      BuildingPlan wallPlan,
      BuildingPlan capPlanRight,
      BuildingPlan capPlanLeft,
      BuildingPlan capPlanBoth
   ) {
      for (VillageWallGenerator.WallSegment segment : wallSegments) {
         if (segment.location.getPlan() == wallPlan) {
            boolean capRight = segment.previousSegment == null;
            boolean capLeft = segment.nextSegment == null;
            BuildingPlan target = null;
            if (capRight && capLeft) {
               target = capPlanBoth;
            } else if (capRight) {
               target = capPlanRight;
            } else if (capLeft) {
               target = capPlanLeft;
            }

            if (target != null && target != wallPlan) {
               BuildingLocation newLocation = new BuildingLocation(target, segment.location.pos, segment.location.orientation);
               newLocation.level = segment.location.level;
               segment.location = newLocation;
            }
         }
      }
   }

   private int computeAverageYLevel(BuildingPlan plan, int orientation, Point pos) {
      orientation = (orientation + plan.buildingOrientation) % 4;
      int orientatedLength = plan.length;
      int orientatedWidth = plan.width;
      if (orientation % 2 == 1) {
         orientatedLength = plan.width;
         orientatedWidth = plan.length;
      }

      List<Point> corners = new ArrayList<>();
      corners.add(pos.getRelative(orientatedLength / 2, 0.0, orientatedWidth / 2));
      corners.add(pos.getRelative(orientatedLength / 2, 0.0, -orientatedWidth / 2));
      corners.add(pos.getRelative(-orientatedLength / 2, 0.0, orientatedWidth / 2));
      corners.add(pos.getRelative(-orientatedLength / 2, 0.0, -orientatedWidth / 2));
      int ySurfaceLevel = 2;

      for (Point corner : corners) {
         ySurfaceLevel += WorldUtilities.findSurfaceBlock(this.world, corner.getiX(), corner.getiZ());
      }

      return ySurfaceLevel / corners.size();
   }

   public List<BuildingLocation> computeWallBuildingLocations(
      VillageType villageType, WallType wallType, int maxWallRadius, RegionMapper regionMapper, Point centre, VillageMapInfo winfo
   ) throws MillLog.MillenaireException {
      List<VillageWallGenerator.WallSegment> wallSegments = new ArrayList<>();
      BuildingPlanSet wallPlanSet = wallType.villageWall;
      BuildingPlanSet towerPlanSet = wallType.villageWallTower;
      BuildingPlanSet gatewayPlanSet = wallType.villageWallGateway;
      BuildingPlanSet cornerPlanSet = wallType.villageWallCorner;
      BuildingPlanSet capPlanSetRight = wallType.villageWallCapRight;
      BuildingPlanSet capPlanSetLeft = wallType.villageWallCapLeft;
      BuildingPlanSet capPlanSetBoth = wallType.villageWallCapBoth;
      if (cornerPlanSet == null && towerPlanSet != null) {
         cornerPlanSet = towerPlanSet;
      }

      if (wallPlanSet != null) {
         if (capPlanSetRight == null) {
            capPlanSetRight = wallPlanSet;
         }

         if (capPlanSetLeft == null) {
            capPlanSetLeft = wallPlanSet;
         }

         if (capPlanSetBoth == null) {
            capPlanSetBoth = wallPlanSet;
         }
      }

      BuildingPlan wallPlan = wallPlanSet != null ? wallPlanSet.getFirstStartingPlan() : null;
      BuildingPlan towerPlan = towerPlanSet != null ? towerPlanSet.getFirstStartingPlan() : null;
      BuildingPlan gatewayPlan = gatewayPlanSet != null ? gatewayPlanSet.getFirstStartingPlan() : null;
      BuildingPlan cornerPlan = cornerPlanSet != null ? cornerPlanSet.getFirstStartingPlan() : null;
      BuildingPlan capPlanRight = capPlanSetRight != null ? capPlanSetRight.getFirstStartingPlan() : null;
      BuildingPlan capPlanLeft = capPlanSetLeft != null ? capPlanSetLeft.getFirstStartingPlan() : null;
      BuildingPlan capPlanBoth = capPlanSetBoth != null ? capPlanSetBoth.getFirstStartingPlan() : null;
      int wallLength = wallPlan != null ? wallPlan.length : 1;
      int towerLength = towerPlan != null ? towerPlan.length : 0;
      int cornerLength = cornerPlan != null ? cornerPlan.length : 0;
      int wallRadius = gatewayPlan.length / 2;
      int buildNb = 0;
      int wallRadiusLimit = maxWallRadius;
      if (maxWallRadius == 0) {
         wallRadiusLimit = villageType.radius - wallLength - cornerLength;
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
      List<VillageWallGenerator.WallSide> sides = new ArrayList<>();
      sides.add(new VillageWallGenerator.WallSide(1, 0, 1, 0));
      sides.add(new VillageWallGenerator.WallSide(0, 1, -1, 3));
      sides.add(new VillageWallGenerator.WallSide(-1, 0, -1, 2));
      sides.add(new VillageWallGenerator.WallSide(0, -1, 1, 1));

      for (VillageWallGenerator.WallSide side : sides) {
         Point p = centre.getRelative(wallRadius * side.xMultiplier, 0.0, wallRadius * side.zMultiplier);
         int y = this.computeAverageYLevel(gatewayPlan, side.buildingOrientation, p);
         Point gatewayPos = new Point(p.getiX(), y, p.getiZ());
         List<VillageWallGenerator.WallSegment> segmentsForward = new ArrayList<>();
         List<VillageWallGenerator.WallSegment> segmentsBackward = new ArrayList<>();
         int pos = gatewayPlan.length / 2;

         for (int var42 = 0; pos < wallRadiusLimit; var42++) {
            BuildingPlanSet currentPlanSet;
            boolean spawn;
            int segmentLength;
            if (var42 % (wallType.villageWallsBetweenTowers + 1) == wallType.villageWallsBetweenTowers) {
               currentPlanSet = towerPlanSet;
               spawn = wallType.villageWallTowerSpawn;
               segmentLength = towerLength;
            } else {
               currentPlanSet = wallPlanSet;
               spawn = wallType.villageWallSpawn;
               segmentLength = wallLength;
            }

            if (currentPlanSet != null) {
               this.buildNextElements(
                  regionMapper, centre, villageType, wallType, segmentsBackward, segmentsForward, winfo, wallRadius, side, pos, currentPlanSet, spawn, true
               );
            }

            pos += segmentLength;
         }

         if (wallPlanSet != null) {
            this.buildNextElements(
               regionMapper,
               centre,
               villageType,
               wallType,
               segmentsBackward,
               segmentsForward,
               winfo,
               wallRadius,
               side,
               pos,
               wallPlanSet,
               wallType.villageWallSpawn,
               true
            );
         }

         pos += wallLength;
         if (cornerPlanSet != null) {
            this.buildNextElements(
               regionMapper,
               centre,
               villageType,
               wallType,
               segmentsBackward,
               segmentsForward,
               winfo,
               wallRadius,
               side,
               pos,
               cornerPlanSet,
               wallType.villageWallCornerSpawn,
               false
            );
         }

         Collections.reverse(segmentsBackward);
         wallSegments.addAll(segmentsBackward);
         VillageWallGenerator.WallSegment gatewaySegment = this.computeWallElementLocation(
            regionMapper, centre, villageType, wallType, winfo, gatewayPlanSet, gatewayPos, side.buildingOrientation, wallType.villageWallGatewaySpawn
         );
         if (gatewaySegment != null) {
            wallSegments.add(gatewaySegment);
         }

         wallSegments.addAll(segmentsForward);
      }

      this.computeWallConnections(wallSegments);
      this.smoothWalls(wallSegments, wallType);
      if (wallPlan != null) {
         this.capWalls(centre, wallSegments, wallPlan, capPlanRight, capPlanLeft, capPlanBoth);
         if (wallType.villageWallSlope1Left != null && wallType.villageWallSlope1Right != null) {
            this.addSlopes(wallSegments, wallType);
         }
      }

      List<BuildingLocation> locations = new ArrayList<>();

      for (VillageWallGenerator.WallSegment segment : wallSegments) {
         locations.add(segment.location);
      }

      return locations;
   }

   private void computeWallConnections(List<VillageWallGenerator.WallSegment> wallSegments) {
      for (int i = 0; i < wallSegments.size(); i++) {
         VillageWallGenerator.WallSegment previousSegment = i == 0 ? wallSegments.get(wallSegments.size() - 1) : wallSegments.get(i - 1);
         VillageWallGenerator.WallSegment currentSegment = wallSegments.get(i);
         boolean connectedToPrevious = previousSegment.location.pos.horizontalDistanceTo(currentSegment.location.pos)
            < (previousSegment.location.getPlan().length + currentSegment.location.getPlan().length) / 2 + 4;
         if (connectedToPrevious) {
            previousSegment.nextSegment = currentSegment;
            currentSegment.previousSegment = previousSegment;
         }
      }
   }

   private VillageWallGenerator.WallSegment computeWallElementLocation(
      RegionMapper regionMapper,
      Point centre,
      VillageType villageType,
      WallType wallType,
      VillageMapInfo winfo,
      BuildingPlanSet planSet,
      Point pos,
      int orientation,
      boolean spawn
   ) throws MillLog.MillenaireException {
      orientation = (orientation + planSet.getFirstStartingPlan().buildingOrientation) % 4;
      BuildingPlan plan = planSet.getRandomStartingPlan();
      int orientatedLength = plan.length;
      int orientatedWidth = plan.width;
      if (orientation % 2 == 1) {
         orientatedLength = plan.width;
         orientatedWidth = plan.length;
      }

      int y = this.computeAverageYLevel(plan, orientation, pos);
      if (y > centre.y + wallType.maxYDelta) {
         return null;
      } else {
         Point buildingPos = new Point(pos.getiX(), y, pos.getiZ());
         List<Point> testPoints = new ArrayList<>();
         testPoints.add(buildingPos);
         testPoints.add(buildingPos.getRelative(orientatedLength / 2, 0.0, orientatedWidth / 2));
         testPoints.add(buildingPos.getRelative(orientatedLength / 2, 0.0, -orientatedWidth / 2));
         testPoints.add(buildingPos.getRelative(-orientatedLength / 2, 0.0, orientatedWidth / 2));
         testPoints.add(buildingPos.getRelative(-orientatedLength / 2, 0.0, -orientatedWidth / 2));
         if (regionMapper != null) {
            boolean reachable = false;

            for (Point p : testPoints) {
               if (this.isReachablePos(regionMapper, winfo, p)) {
                  reachable = true;
               }
            }

            if (!reachable) {
               return null;
            }
         }

         BuildingLocation location = new BuildingLocation(plan, buildingPos, orientation);
         if (!spawn) {
            location.level = -1;
         }

         return new VillageWallGenerator.WallSegment(location);
      }
   }

   private boolean isReachablePos(RegionMapper regionMapper, VillageMapInfo winfo, Point pos) {
      int relativeX = pos.getiX() - winfo.mapStartX;
      int relativeZ = pos.getiZ() - winfo.mapStartZ;
      return relativeX >= 0 && relativeX < regionMapper.regions.length && relativeZ >= 0 && relativeZ < regionMapper.regions[0].length
         ? regionMapper.regions[relativeX][relativeZ] == regionMapper.thRegion
         : false;
   }

   private void smoothWalls(List<VillageWallGenerator.WallSegment> wallSegments, WallType wallType) {
      List<Float> referenceY = new ArrayList<>();

      for (VillageWallGenerator.WallSegment segment : wallSegments) {
         referenceY.add((float)segment.location.pos.getiY());
      }

      for (int run = 0; run < wallType.nbSmoothRuns; run++) {
         List<Float> adjustedY = new ArrayList<>();

         for (int i = 0; i < referenceY.size(); i++) {
            int previousId = i == 0 ? referenceY.size() - 1 : i - 1;
            int nextId = i == referenceY.size() - 1 ? 0 : i + 1;
            VillageWallGenerator.WallSegment segment = wallSegments.get(i);
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
            if (referenceY.get(i) != adjustedY.get(i)) {
               referenceY.set(i, adjustedY.get(i));
            }
         }
      }

      for (int ix = 0; ix < referenceY.size(); ix++) {
         int finalY = Math.round(referenceY.get(ix));
         if (wallSegments.get(ix).location.pos.getiY() != finalY) {
            wallSegments.get(ix).setYLevel(finalY);
         }
      }
   }

   private static class WallSegment {
      public BuildingLocation location;
      public VillageWallGenerator.WallSegment previousSegment = null;
      public VillageWallGenerator.WallSegment nextSegment = null;
      public boolean sloppable = false;
      public int yTowardsPrevious;
      public int yTowardsNext;

      public WallSegment(BuildingLocation location) {
         this.location = location;
         this.yTowardsPrevious = location.pos.getiY();
         this.yTowardsNext = location.pos.getiY();
      }

      public void setYLevel(int newY) {
         int deltaY = (int)(newY - this.location.pos.y);
         this.location.pos = this.location.pos.getRelative(0.0, deltaY, 0.0);
         this.yTowardsPrevious += deltaY;
         this.yTowardsNext += deltaY;
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
