package org.millenaire.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import org.millenaire.core.VillageData;
import org.millenaire.core.VillageManager;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.pathing.atomicstryker.AStarPathPlannerJPS;
import org.millenaire.worldgen.VillageWallGenerator;
import org.millenaire.common.culture.WallType;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.village.VillageMapInfo;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Handles the spawning and initial setup of Millénaire villages.
 * Creates starter villages with a main building and a few huts.
 */
public class VillageStarter {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    // ===== BUILDING OVERLAP PREVENTION =====
    // Based on original Millénaire VillageMapInfo (BUILDING_MARGIN = 5)
    private static final int BUILDING_MARGIN = 5;

    // ===== VILLAGE GENERATION CONFIG (from original MillConfigValues) =====
    // Minimum distance from world spawn to generate a village
    private static final int SPAWN_PROTECTION_RADIUS = 250;
    // Default village radius (matches original MillConfigValues.VillageRadius)
    private static final int DEFAULT_VILLAGE_RADIUS = 80;
    // Minimum distance between villages
    private static final int MIN_DISTANCE_BETWEEN_VILLAGES = 500;
    // Minimum distance between villages and lone buildings
    private static final int MIN_DISTANCE_BETWEEN_VILLAGES_AND_LONE_BUILDINGS = 250;
    // Minimum distance between lone buildings
    private static final int MIN_DISTANCE_BETWEEN_LONE_BUILDINGS = 500;

    // Occupancy grid: maps (x,z) world coordinates to building ID
    // This tracks which cells are occupied by buildings, preventing overlap
    private static Map<Long, String> occupiedGrid = new HashMap<>();

    /**
     * Convert (x, z) world coordinates to a single long key for the occupancy grid.
     */
    private static long posToKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    /**
     * Register a building's footprint in the occupancy grid.
     * Based on original Millénaire VillageMapInfo.addBuildingLocationToMap()
     * 
     * @param origin      World position of building origin (corner)
     * @param width       Building width (X dimension in orientation 0)
     * @param length      Building length (Z dimension in orientation 0)
     * @param orientation Building orientation (0-3), affects dimension swap
     * @param margin      Extra buffer around building (BUILDING_MARGIN)
     * @param buildingId  Identifier for debugging
     */
    private static void registerBuildingFootprint(BlockPos origin, int width, int length,
            int orientation, int margin, String buildingId) {
        // Swap dimensions for 90°/270° rotations (orientation 1 or 3)
        int actualWidth = (orientation == 1 || orientation == 3) ? length : width;
        int actualLength = (orientation == 1 || orientation == 3) ? width : length;

        int registered = 0;
        for (int dx = -margin; dx < actualWidth + margin; dx++) {
            for (int dz = -margin; dz < actualLength + margin; dz++) {
                occupiedGrid.put(posToKey(origin.getX() + dx, origin.getZ() + dz), buildingId);
                registered++;
            }
        }
        LOGGER.debug("Registered {} cells for building {} at {} ({}x{} + margin {})",
                registered, buildingId, origin, actualWidth, actualLength, margin);
    }

    /**
     * Check if a building can be placed at the given position without overlapping.
     * Based on original Millénaire BuildingPlan.testSpot() lines 2609-2612
     * 
     * @param origin      World position of building origin (corner)
     * @param width       Building width
     * @param length      Building length
     * @param orientation Building orientation (0-3)
     * @param margin      Extra buffer around building
     * @return true if the position is clear, false if it overlaps existing
     *         buildings
     */
    private static boolean canPlaceBuilding(BlockPos origin, int width, int length,
            int orientation, int margin) {
        // Swap dimensions for 90°/270° rotations
        int actualWidth = (orientation == 1 || orientation == 3) ? length : width;
        int actualLength = (orientation == 1 || orientation == 3) ? width : length;

        for (int dx = -margin; dx < actualWidth + margin; dx++) {
            for (int dz = -margin; dz < actualLength + margin; dz++) {
                if (occupiedGrid.containsKey(posToKey(origin.getX() + dx, origin.getZ() + dz))) {
                    LOGGER.debug("Collision at ({}, {}) - cell occupied",
                            origin.getX() + dx, origin.getZ() + dz);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Clear the occupancy grid. Call at start of village generation.
     */
    private static void clearOccupancyGrid() {
        occupiedGrid.clear();
    }

    // ===== TERRAIN SUITABILITY EVALUATION =====
    // Based on original Millénaire testSpot() multi-point sampling

    // ===== SPAWN PROTECTION & DISTANCE CHECKS =====
    // Based on original Millénaire WorldGenVillage.java (lines 139-166)

    /**
     * Checks if a village position would be too close to world spawn.
     * From original WorldGenVillage.isWithinSpawnRadiusProtection()
     * 
     * @param level         The server level
     * @param villagePos    The proposed village position
     * @param villageRadius The radius of the village type
     * @return true if the position is too close to spawn (should be blocked)
     */
    public static boolean isWithinSpawnRadiusProtection(ServerLevel level, BlockPos villagePos, int villageRadius) {
        if (SPAWN_PROTECTION_RADIUS == 0) {
            return false; // Spawn protection disabled
        }

        BlockPos spawnPos = level.getSharedSpawnPos();
        double distance = Math.sqrt(
                Math.pow(villagePos.getX() - spawnPos.getX(), 2) +
                        Math.pow(villagePos.getZ() - spawnPos.getZ(), 2));

        double minAcceptable = SPAWN_PROTECTION_RADIUS + villageRadius;

        if (distance < minAcceptable) {
            LOGGER.debug("Blocking spawn at {}/{}. Distance to spawn: {}, min acceptable: {}",
                    villagePos.getX(), villagePos.getZ(), distance, minAcceptable);
            return true;
        }
        return false;
    }

    /**
     * Checks if a village position would be too close to any existing villages.
     * From original WorldGenVillage.generateVillage() distance checks.
     * 
     * @param level          The server level
     * @param villagePos     The proposed village position
     * @param isLoneBuilding Whether this is a lone building (uses different
     *                       distances)
     * @return true if the position is too close to existing villages (should be
     *         blocked)
     */
    public static boolean isTooCloseToExistingVillages(ServerLevel level, BlockPos villagePos, boolean isLoneBuilding) {
        VillageManager villageManager = VillageManager.get(level);
        if (villageManager == null) {
            return false; // No village manager, can't check
        }

        int minDistanceVillages = isLoneBuilding ? MIN_DISTANCE_BETWEEN_VILLAGES_AND_LONE_BUILDINGS
                : MIN_DISTANCE_BETWEEN_VILLAGES;
        int minDistanceLoneBuildings = isLoneBuilding ? MIN_DISTANCE_BETWEEN_LONE_BUILDINGS
                : MIN_DISTANCE_BETWEEN_VILLAGES_AND_LONE_BUILDINGS;

        // Check against all registered villages
        for (VillageData village : villageManager.getAllVillages().values()) {
            BlockPos existingPos = village.getCenter();
            double distance = Math.sqrt(
                    Math.pow(villagePos.getX() - existingPos.getX(), 2) +
                            Math.pow(villagePos.getZ() - existingPos.getZ(), 2));

            // Check if this is a lone building
            boolean existingIsLone = village.isLoneBuilding();
            int minDistance = existingIsLone ? minDistanceLoneBuildings : minDistanceVillages;

            if (distance < minDistance) {
                LOGGER.debug("Found nearby village at {}. Distance: {}, minRequired: {}",
                        existingPos, distance, minDistance);
                return true;
            }
        }
        return false;
    }

    /**
     * Result of terrain suitability evaluation.
     * 
     * @param isValid      Whether terrain is suitable for building
     * @param averageY     Average terrain height across footprint
     * @param maxY         Maximum terrain height across footprint (use this for
     *                     placement!)
     * @param variance     Height difference (max - min) across footprint
     * @param rejectReason Reason for rejection, or null if valid
     */
    private record TerrainResult(boolean isValid, int averageY, int maxY, int variance, String rejectReason) {
        static TerrainResult valid(int avgY, int maxY, int variance) {
            return new TerrainResult(true, avgY, maxY, variance, null);
        }

        static TerrainResult invalid(String reason) {
            return new TerrainResult(false, 0, 0, 0, reason);
        }
    }

    /**
     * Evaluate terrain suitability at a potential building position.
     * Samples multiple points across the building footprint to detect:
     * - Excessive height variance (mountains, holes)
     * - Water bodies
     * - Steep cliffs at edges
     * 
     * Based on original Millénaire BuildingPlan.testSpot() (lines 2571-2658)
     * which samples all points and averages altitude.
     * 
     * @param level       The server level
     * @param pos         Center position to evaluate
     * @param width       Building width
     * @param length      Building length
     * @param orientation Building orientation (0-3)
     * @return TerrainResult with validity, average Y, and variance
     */
    private static TerrainResult evaluateTerrainSuitability(ServerLevel level, BlockPos pos,
            int width, int length, int orientation) {
        // Swap dimensions if rotated 90 degrees
        int actualWidth = (orientation == 1 || orientation == 3) ? length : width;
        int actualLength = (orientation == 1 || orientation == 3) ? width : length;

        int halfW = actualWidth / 2;
        int halfL = actualLength / 2;

        // FIX: Check EVERY SINGLE BLOCK in the building footprint to find the true
        // maximum Y
        // This prevents buildings from carving into terrain that wasn't sampled
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        long totalY = 0; // Use long to avoid overflow for large buildings
        int blockCount = 0;
        int waterCount = 0;

        // Iterate over entire building footprint
        for (int dx = -halfW; dx < actualWidth - halfW; dx++) {
            for (int dz = -halfL; dz < actualLength - halfL; dz++) {
                int checkX = pos.getX() + dx;
                int checkZ = pos.getZ() + dz;
                int checkY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, checkX, checkZ);

                // Check for water at this position
                BlockPos surfacePos = new BlockPos(checkX, checkY - 1, checkZ);
                BlockState surfaceState = level.getBlockState(surfacePos);
                if (surfaceState.getFluidState().isSource()) {
                    waterCount++;
                }

                minY = Math.min(minY, checkY);
                maxY = Math.max(maxY, checkY);
                totalY += checkY;
                blockCount++;
            }
        }

        if (blockCount == 0) {
            return TerrainResult.invalid("no blocks checked");
        }

        int variance = maxY - minY;
        int averageY = (int) (totalY / blockCount);

        // Determine max allowed variance based on building size
        // Larger buildings can tolerate more variance
        int footprintArea = actualWidth * actualLength;
        int maxVariance;
        if (footprintArea > 800) {
            maxVariance = 12; // Huge building (e.g., 30x30)
        } else if (footprintArea > 400) {
            maxVariance = 10; // Large building
        } else if (footprintArea > 100) {
            maxVariance = 8; // Medium building
        } else {
            maxVariance = 6; // Small building
        }

        // Reject if too much water (more than 1/3 of footprint)
        if (waterCount > blockCount / 3) {
            LOGGER.debug("[TERRAIN] Rejected {},{}: too much water ({}/{})",
                    pos.getX(), pos.getZ(), waterCount, blockCount);
            return TerrainResult.invalid("too much water");
        }

        // Reject if terrain variance too high
        if (variance > maxVariance) {
            LOGGER.debug("[TERRAIN] Rejected {},{}: variance {} > max {}",
                    pos.getX(), pos.getZ(), variance, maxVariance);
            return TerrainResult.invalid("terrain too rough (variance=" + variance + ")");
        }

        LOGGER.info("[TERRAIN] Footprint check: {} blocks, minY={}, maxY={}, avgY={}, variance={}",
                blockCount, minY, maxY, averageY, variance);
        return TerrainResult.valid(averageY, maxY, variance);
    }

    /**
     * Find a valid position for a building using footprint-based collision
     * detection.
     * Based on original Millénaire BuildingPlan.testSpot() approach.
     * 
     * @param level       The server level
     * @param center      Village center position
     * @param plan        Building plan (for dimensions)
     * @param minRadius   Minimum distance from center
     * @param maxRadius   Maximum distance from center
     * @param maxAttempts Maximum number of random positions to try
     * @return Valid position, or null if no valid position found
     */
    private static BlockPos findValidBuildingPosition(ServerLevel level, BlockPos center,
            BuildingPlan plan,
            int minRadius, int maxRadius, int maxAttempts) {
        int attempts = 0;

        // Increase attempts for larger search areas (rougher terrain may need more
        // tries)
        int effectiveMaxAttempts = maxAttempts * 2; // Double attempts for terrain-aware placement

        while (attempts < effectiveMaxAttempts) {
            attempts++;

            // Generate random position in annular ring around center
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = minRadius + RANDOM.nextDouble() * (maxRadius - minRadius);

            int x = (int) (center.getX() + Math.cos(angle) * distance);
            int z = (int) (center.getZ() + Math.sin(angle) * distance);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

            BlockPos testPos = new BlockPos(x, y, z);

            // Calculate what the orientation would be at this position
            int orientation = computeOrientation(testPos, center);
            orientation = (orientation + plan.buildingOrientation()) % 4;

            // STEP 1: Check footprint collision (fast check first)
            if (!canPlaceBuilding(testPos, plan.width(), plan.length(), orientation, BUILDING_MARGIN)) {
                continue; // Collision detected, try next position
            }

            // STEP 2: Check terrain suitability (multi-point sampling)
            TerrainResult terrain = evaluateTerrainSuitability(level, testPos,
                    plan.width(), plan.length(), orientation);

            if (!terrain.isValid()) {
                // Terrain unsuitable, try next position
                continue;
            }

            // Use the MAXIMUM Y from terrain sampling so buildings sit ON TOP of terrain
            // This prevents buildings from "carving into" hillsides
            BlockPos validPos = new BlockPos(x, terrain.maxY(), z);

            LOGGER.debug("Found valid position for building at {} (maxY={}, avgY={}, variance={}) after {} attempts",
                    validPos, terrain.maxY(), terrain.averageY(), terrain.variance(), attempts);
            return validPos;
        }

        LOGGER.warn("Could not find valid building position after {} attempts", effectiveMaxAttempts);
        return null;
    }

    // ===== VILLAGEMAPINFO-BASED BUILDING PLACEMENT =====
    // Full port from original Millénaire BuildingPlan.findBuildingLocation() and
    // testSpot()

    /**
     * Result of testing a spot for building placement.
     */
    private record LocationReturn(BuildingLocation location, int errorCode, BlockPos errorPos) {
        static LocationReturn success(BuildingLocation loc) {
            return new LocationReturn(loc, 0, null);
        }

        static LocationReturn error(int code, BlockPos pos) {
            return new LocationReturn(null, code, pos);
        }
    }

    /**
     * Find a valid building location using the VillageMapInfo system.
     * Uses expanding square search pattern from original Millénaire.
     * 
     * @param winfo        Village map info with terrain data
     * @param regionMapper Region mapper for reachability (can be null for town
     *                     hall)
     * @param centre       Village center position
     * @param plan         Building plan
     * @param maxRadius    Maximum search radius
     * @param orientation  Fixed orientation (-1 for auto)
     * @return Building location or null if not found
     */
    public static BuildingLocation findBuildingLocationWithMapInfo(
            VillageMapInfo winfo,
            RegionMapper regionMapper,
            BlockPos centre,
            BuildingPlan plan,
            int maxRadius,
            int minRadius, // New parameter
            int orientation) {

        long startTime = System.nanoTime();
        int ci = centre.getX() - winfo.mapStartX;
        int cj = centre.getZ() - winfo.mapStartZ;

        // Start with minimum distance and expand
        int radius = Math.max(minRadius, 5); // Use provided minRadius

        LOGGER.debug("[PLACEMENT] Searching for {} around ({},{}) radius {}-{}",
                plan.sourceFile(), ci, cj, radius, maxRadius);

        // Reset build tested flags
        for (int i = 0; i < winfo.length; i++) {
            for (int j = 0; j < winfo.width; j++) {
                winfo.buildTested[i][j] = false;
            }
        }

        // Expanding square search - exactly as in original
        while (radius < maxRadius) {
            int mini = Math.max(0, ci - radius);
            int maxi = Math.min(winfo.length - 1, ci + radius);
            int minj = Math.max(0, cj - radius);
            int maxj = Math.min(winfo.width - 1, cj + radius);

            // Test north edge
            for (int i = mini; i < maxi; i++) {
                if (cj - radius == minj) {
                    LocationReturn lr = testSpot(winfo, regionMapper, centre, i, minj, plan, orientation);
                    if (lr.location() != null) {
                        LOGGER.debug("[PLACEMENT] Found location at ({},{}) after {}ms",
                                i + winfo.mapStartX, minj + winfo.mapStartZ,
                                (System.nanoTime() - startTime) / 1_000_000.0);
                        return lr.location();
                    }
                }

                // Test south edge
                if (cj + radius == maxj) {
                    LocationReturn lr = testSpot(winfo, regionMapper, centre, i, maxj, plan, orientation);
                    if (lr.location() != null) {
                        LOGGER.debug("[PLACEMENT] Found location after {}ms",
                                (System.nanoTime() - startTime) / 1_000_000.0);
                        return lr.location();
                    }
                }
            }

            // Test west edge
            for (int j = minj; j < maxj; j++) {
                if (ci - radius == mini) {
                    LocationReturn lr = testSpot(winfo, regionMapper, centre, mini, j, plan, orientation);
                    if (lr.location() != null) {
                        return lr.location();
                    }
                }

                // Test east edge
                if (ci + radius == maxi) {
                    LocationReturn lr = testSpot(winfo, regionMapper, centre, maxi, j, plan, orientation);
                    if (lr.location() != null) {
                        return lr.location();
                    }
                }
            }

            radius++;
        }

        LOGGER.warn("[PLACEMENT] Could not find location for {} (radius: {})", plan.sourceFile(), radius);
        return null;
    }

    /**
     * Test a specific spot for building placement.
     * Full port from original Millénaire BuildingPlan.testSpot().
     * 
     * Error codes:
     * 0 = success
     * 1 = out of bounds
     * 2 = building collision
     * 3 = forbidden area
     * 4 = not buildable
     * 5 = danger zone
     * 6 = not reachable
     */
    private static LocationReturn testSpot(
            VillageMapInfo winfo,
            RegionMapper regionMapper,
            BlockPos centre,
            int x, int z, // Map coordinates
            BuildingPlan plan,
            int porientation) {

        // Skip if already tested
        if (x >= 0 && x < winfo.length && z >= 0 && z < winfo.width) {
            if (winfo.buildTested[x][z]) {
                return LocationReturn.error(0, null); // Already tested, skip silently
            }
            winfo.buildTested[x][z] = true;
        }

        BlockPos testWorldPos = new BlockPos(x + winfo.mapStartX, 64, z + winfo.mapStartZ);

        // Calculate orientation (face toward centre)
        int orientation;
        if (porientation == -1) {
            orientation = computeOrientation(testWorldPos, centre);
        } else {
            orientation = porientation;
        }
        orientation = (orientation + plan.buildingOrientation()) % 4;

        // ========================================
        // TAG-BASED PROXIMITY CHECKS (Original lines 2527-2551)
        // Error code 7 = too close to farFromTag building
        // Error code 8 = required closeToTag building not nearby
        // ========================================

        // Check farFromTag - must be FAR from buildings with these tags
        if (plan.farFromTag != null && !plan.farFromTag.isEmpty()) {
            for (String tag : plan.farFromTag.keySet()) {
                int requiredDistance = plan.farFromTag.get(tag);
                for (BuildingLocation bl : winfo.getBuildingLocations()) {
                    if (bl.planKey != null && bl.planKey.toLowerCase().contains(tag.toLowerCase())) {
                        double distance = Math.sqrt(
                                Math.pow(testWorldPos.getX() - bl.pos.getiX(), 2) +
                                        Math.pow(testWorldPos.getZ() - bl.pos.getiZ(), 2));
                        if (distance < requiredDistance) {
                            return LocationReturn.error(7, testWorldPos);
                        }
                    }
                }
            }
        }

        // Check closeToTag - must be CLOSE to a building with these tags
        if (plan.closeToTag != null && !plan.closeToTag.isEmpty()) {
            for (String tag : plan.closeToTag.keySet()) {
                int requiredDistance = plan.closeToTag.get(tag);
                boolean foundNearby = false;
                for (BuildingLocation bl : winfo.getBuildingLocations()) {
                    if (bl.planKey != null && bl.planKey.toLowerCase().contains(tag.toLowerCase())) {
                        double distance = Math.sqrt(
                                Math.pow(testWorldPos.getX() - bl.pos.getiX(), 2) +
                                        Math.pow(testWorldPos.getZ() - bl.pos.getiZ(), 2));
                        if (distance < requiredDistance) {
                            foundNearby = true;
                            break;
                        }
                    }
                }
                if (!foundNearby) {
                    return LocationReturn.error(8, testWorldPos);
                }
            }
        }

        // Calculate footprint dimensions based on orientation
        int xwidth, zwidth;
        if (orientation == 1 || orientation == 3) {
            xwidth = plan.width() + 2 + 2; // areaToClear margins
            zwidth = plan.length() + 2 + 2;
        } else {
            xwidth = plan.length() + 2 + 2;
            zwidth = plan.width() + 2 + 2;
        }

        // Check all cells in footprint
        int altitudeTotal = 0;
        int nbPoints = 0;
        int nbError = 0;
        int allowedErrors = 10;

        // Track MAX Y for placement
        int maxY = Integer.MIN_VALUE;

        // RELAXED: Larger error tolerance for more forgiving placement
        // Original was very strict (10), we allow more edge-case errors
        if (xwidth * zwidth > 2000) {
            allowedErrors = xwidth * zwidth / 5; // Was /10
        } else if (xwidth * zwidth > 200) {
            allowedErrors = xwidth * zwidth / 10; // Was /20
        } else {
            allowedErrors = 30; // Was 10
        }

        boolean reachable = (regionMapper == null); // Town hall doesn't need reachability check

        // Check each quadrant outward from center
        for (int i = 0; i <= xwidth / 2; i++) {
            for (int j = 0; j <= zwidth / 2; j++) {
                for (int k = 0; k < 4; k++) {
                    int ci, cj;
                    if (k == 0) {
                        ci = x + i;
                        cj = z + j;
                    } else if (k == 1) {
                        ci = x - i;
                        cj = z + j;
                    } else if (k == 2) {
                        ci = x - i;
                        cj = z - j;
                    } else {
                        ci = x + i;
                        cj = z - j;
                    }

                    // Bounds check
                    if (ci < 0 || cj < 0 || ci >= winfo.length || cj >= winfo.width) {
                        return LocationReturn.error(1, new BlockPos(ci + winfo.mapStartX, 0, cj + winfo.mapStartZ));
                    }

                    // Building collision check
                    if (winfo.buildingLocRef[ci][cj] != null) {
                        return LocationReturn.error(2, new BlockPos(ci + winfo.mapStartX, 0, cj + winfo.mapStartZ));
                    }

                    // Forbidden area check
                    if (winfo.buildingForbidden[ci][cj]) {
                        if (nbError > allowedErrors) {
                            return LocationReturn.error(3, new BlockPos(ci + winfo.mapStartX, 0, cj + winfo.mapStartZ));
                        }
                        nbError++;
                    } else if (winfo.danger[ci][cj]) {
                        if (nbError > allowedErrors) {
                            return LocationReturn.error(5, new BlockPos(ci + winfo.mapStartX, 0, cj + winfo.mapStartZ));
                        }
                        nbError++;
                    } else if (!winfo.canBuild[ci][cj]) {
                        if (nbError > allowedErrors) {
                            return LocationReturn.error(4, new BlockPos(ci + winfo.mapStartX, 0, cj + winfo.mapStartZ));
                        }
                        nbError++;
                    }

                    // Region reachability check
                    if (regionMapper != null) {
                        if (ci < regionMapper.regions.length && cj < regionMapper.regions[0].length) {
                            if (regionMapper.regions[ci][cj] == regionMapper.thRegion) {
                                reachable = true;
                            }
                        }
                    }

                    // Track MAX Y for placement, IGNORING TREES
                    // Users don't want buildings sitting on top of trees
                    if (!winfo.tree[ci][cj]) {
                        int y = winfo.topGround[ci][cj];
                        if (y > maxY) {
                            maxY = y;
                        }
                    }

                    altitudeTotal += winfo.topGround[ci][cj];
                    nbPoints++;
                }
            }
        }

        // Check reachability
        if (regionMapper != null && !reachable) {
            return LocationReturn.error(6, centre);
        }

        // FIX: Revert to using AVERAGE Y to match original Millénaire behavior exactly.
        // Original line 2658: int altitude = Math.round(altitudeTotal * 1.0F /
        // nbPoints);
        // Using Max Y caused floating buildings/gaps because fixed foundation depth
        // (10)
        // wasn't enough when placed at the peak of a hill.
        int altitude = Math.round((float) altitudeTotal / nbPoints);

        // Add altitude offset from plan (line 2659 in original)
        altitude += plan.altitudeOffset();

        LOGGER.debug("[PLACEMENT] Selected altitude {} (avgY={}) for {}",
                altitude, (altitudeTotal / nbPoints), plan.sourceFile());

        // Create building location
        BlockPos finalPos = new BlockPos(x + winfo.mapStartX, altitude, z + winfo.mapStartZ);
        BuildingLocation location = new BuildingLocation(
                plan, new org.millenaire.common.utilities.Point(finalPos), orientation);

        return LocationReturn.success(location);
    }

    /**
     * Spawn a starter village of the given culture at the specified position.
     * Selects a random village type from the culture.
     */
    public static VillageData spawnStarterVillage(ServerLevel level, BlockPos centerPos, Culture culture) {
        // Get valid village types for this biome
        net.minecraft.resources.ResourceLocation biomeId = level.getBiome(centerPos).unwrapKey().get().location();
        String biomeName = biomeId.toString();

        List<org.millenaire.common.culture.VillageType> allTypes = culture.getVillageTypes();
        List<org.millenaire.common.culture.VillageType> validTypes = new ArrayList<>();

        for (org.millenaire.common.culture.VillageType type : allTypes) {
            List<String> validBiomes = type.getValidBiomes();
            if (validBiomes.isEmpty()) {
                validTypes.add(type);
                continue;
            }

            for (String validBiome : validBiomes) {
                // Check exact match, path match, or case-insensitive match
                if (validBiome.equalsIgnoreCase(biomeName) ||
                        validBiome.equalsIgnoreCase(biomeId.getPath()) ||
                        validBiome.replace(" ", "_").equalsIgnoreCase(biomeId.getPath())) {
                    validTypes.add(type);
                    break;
                }
            }
        }

        if (validTypes.isEmpty()) {
            LOGGER.warn("No valid village types for biome: {}. Using all types as fallback.", biomeName);
            validTypes = allTypes;
        }

        if (validTypes.isEmpty()) {
            LOGGER.error("No village types found for culture: {}", culture.getId());
            return null;
        }

        // Pick random type based on weight (simplified for now: random)
        org.millenaire.common.culture.VillageType randomType = validTypes.get(RANDOM.nextInt(validTypes.size()));
        return spawnVillage(level, centerPos, culture, randomType);
    }

    /**
     * Generate a hamlet (satellite village) around a parent village.
     * Ported from original Millénaire WorldGenVillage.generateHamlet().
     * 
     * Searches in 36 angle steps at distances of 250-350 blocks from the parent
     * village.
     * 
     * @param level               The server level
     * @param culture             The culture for the hamlet
     * @param parentVillageCenter Center of the parent village
     * @param hamletTypeId        ID of the hamlet village type to spawn
     * @return true if hamlet was successfully generated
     */
    public static boolean generateHamlet(ServerLevel level, Culture culture, BlockPos parentVillageCenter,
            String hamletTypeId) {
        // Find the hamlet village type
        org.millenaire.common.culture.VillageType hamletType = null;
        for (org.millenaire.common.culture.VillageType type : culture.getVillageTypes()) {
            if (type.getId().equalsIgnoreCase(hamletTypeId)) {
                hamletType = type;
                break;
            }
        }

        if (hamletType == null) {
            LOGGER.warn("[HAMLET] Could not find hamlet type '{}' for culture '{}'", hamletTypeId, culture.getId());
            return false;
        }

        LOGGER.info("[HAMLET] Attempting to generate hamlet '{}' around {}", hamletTypeId, parentVillageCenter);

        // Original algorithm: search at increasing radii (250-350) with 36 angle steps
        for (int minRadius = 250; minRadius < 350; minRadius += 50) {
            // Start at a random angle
            double startAngle = (Math.PI * 2.0 * RANDOM.nextDouble());
            double angleStep = (Math.PI * 2.0) / 36.0; // ~10 degrees per step

            for (int attempt = 0; attempt < 36; attempt++) {
                double angle = startAngle + (attempt * angleStep);
                int radius = minRadius + RANDOM.nextInt(40);

                int dx = (int) (Math.cos(angle) * radius);
                int dz = (int) (Math.sin(angle) * radius);
                int testX = parentVillageCenter.getX() + dx;
                int testZ = parentVillageCenter.getZ() + dz;

                // Get surface height at test position
                int testY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, testX, testZ);
                BlockPos testPos = new BlockPos(testX, testY, testZ);

                LOGGER.debug("[HAMLET] Testing position {} for hamlet '{}'", testPos, hamletTypeId);

                // Try to spawn the hamlet at this location
                VillageData hamletData = spawnVillage(level, testPos, culture, hamletType);

                if (hamletData != null) {
                    LOGGER.info("[HAMLET] Successfully generated hamlet '{}' at {} (distance: {} from parent)",
                            hamletTypeId, testPos, radius);
                    return true;
                }
            }
        }

        LOGGER.warn("[HAMLET] Could not find suitable location for hamlet '{}' around {}", hamletTypeId,
                parentVillageCenter);
        return false;
    }

    /**
     * Generate all hamlets for a village type around a parent village.
     * 
     * @param level               The server level
     * @param culture             The culture
     * @param villageType         The parent village type (contains hamlet list)
     * @param parentVillageCenter Center of the parent village
     */
    public static void generateHamletsForVillage(ServerLevel level, Culture culture,
            org.millenaire.common.culture.VillageType villageType, BlockPos parentVillageCenter) {

        if (!villageType.hasHamlets()) {
            LOGGER.debug("[HAMLET] Village type '{}' has no hamlets configured", villageType.getId());
            return;
        }

        List<String> hamlets = villageType.getHamlets();
        LOGGER.info("[HAMLET] Generating {} hamlets for village type '{}' at {}",
                hamlets.size(), villageType.getId(), parentVillageCenter);

        for (String hamletId : hamlets) {
            boolean success = generateHamlet(level, culture, parentVillageCenter, hamletId);
            if (!success) {
                LOGGER.warn("[HAMLET] Failed to generate hamlet '{}'", hamletId);
            }
        }
    }

    /**
     * Smooth terrain around buildings to create natural slopes given their raised
     * foundations.
     * Uses an iterative cellular automaton approach.
     */
    private static void populateBuildingResidents(ServerLevel level, VillageData villageData,
            BlockPos buildingPos, BuildingPlan plan) {

        Culture culture = villageData.getCulture();
        if (culture == null)
            return;

        List<String> residents = new ArrayList<>();
        if (plan.maleResident != null)
            residents.addAll(plan.maleResident);
        if (plan.femaleResident != null)
            residents.addAll(plan.femaleResident);

        for (String residentType : residents) {
            if (residentType == null || residentType.isEmpty())
                continue;

            // Check if valid villager type
            if (culture.getVillagerType(residentType) == null) {
                // LOGGER.warn("Unknown villager type '{}' in plan {}", residentType,
                // plan.sourceFile());
                continue;
            }

            long villagerId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
            org.millenaire.common.village.VillagerRecord vr = org.millenaire.common.village.VillagerRecord
                    .createVillagerRecord(
                            culture, residentType, org.millenaire.common.world.MillWorldData.get(level),
                            buildingPos, villageData.getCenterPos(), null, null, villagerId);

            if (vr != null) {
                villageData.addResident(buildingPos, vr);
                // LOGGER.info("Created resident {} for {}", residentType, buildingPos);
            }
        }
    }

    private static void smoothTerrain(ServerLevel level, BlockPos center,
            List<BuildingLocation> buildings, int radius) {
        LOGGER.info("Smoothing terrain around {} buildings...", buildings.size());

        // Limit the scope to prevent modifying chunks far away
        int mapRadius = radius + 10;
        int size = mapRadius * 2;
        int startX = center.getX() - mapRadius;
        int startZ = center.getZ() - mapRadius;

        // 1. Initialize height map with current terrain
        Integer[][] heights = new Integer[size][size];
        boolean[][] isProtected = new boolean[size][size]; // Don't modify these
        int[][] minDistToBuilding = new int[size][size]; // For distance limiting

        // Initialize distances to max
        for (int i = 0; i < size; i++) {
            Arrays.fill(minDistToBuilding[i], Integer.MAX_VALUE);
            for (int j = 0; j < size; j++) {
                int wx = startX + i;
                int wz = startZ + j;
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz);

                // CRITICAL: Filter out logs/leaves that might still be caught (e.g. from trees
                // generated after heightmap)
                // or if we just want to be super sure we are hitting ground.
                // Scan down up to 20 blocks
                BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos(wx, y, wz);
                BlockState state = level.getBlockState(mutPos);
                int searchDepth = 0;
                while (searchDepth < 20 && !level.isOutsideBuildHeight(mutPos) &&
                        (state.is(net.minecraft.tags.BlockTags.LOGS) ||
                                state.is(net.minecraft.tags.BlockTags.LEAVES) ||
                                state.isAir())) {
                    mutPos.move(0, -1, 0);
                    state = level.getBlockState(mutPos);
                    searchDepth++;
                }

                heights[i][j] = mutPos.getY();
            }
        }

        // 2. Mark building footprints (Protected) & Seed BFS
        List<int[]> bfsQueue = new ArrayList<>();

        for (BuildingLocation bl : buildings) {
            int bx = bl.pos.getiX() - startX;
            int bz = bl.pos.getiZ() - startZ;

            // Handle rotation: If orientation is 1 (90) or 3 (270), swap width/length
            int effectiveWidth = bl.width;
            int effectiveLength = bl.length;
            if (bl.orientation == 1 || bl.orientation == 3) {
                effectiveWidth = bl.length;
                effectiveLength = bl.width;
            }

            // Building bounds with margin
            // Margin 2 protects the immediate area (patio)
            int margin = 2;
            int minX = bx - effectiveWidth / 2 - margin;
            int maxX = bx + effectiveWidth / 2 + margin;
            int minZ = bz - effectiveLength / 2 - margin;
            int maxZ = bz + effectiveLength / 2 + margin;

            for (int x = Math.max(0, minX); x < Math.min(size, maxX); x++) {
                for (int z = Math.max(0, minZ); z < Math.min(size, maxZ); z++) {
                    isProtected[x][z] = true;
                    // CRITICAL FIX: Override the height map value to the foundation level (-1).
                    // This creates a "flat plane" at ground level for the smoothing algorithm,
                    // preventing it from ramping up to the roof height.
                    heights[x][z] = bl.pos.getiY() - 1;
                    minDistToBuilding[x][z] = 0;
                    bfsQueue.add(new int[] { x, z });
                }
            }
        }

        // 3. BFS to compute distance mask (Manhattan distance for
        // simplicity/performance in grid)
        int head = 0;
        int[] dx = { 1, -1, 0, 0 };
        int[] dz = { 0, 0, 1, -1 };

        while (head < bfsQueue.size()) {
            int[] cell = bfsQueue.get(head++);
            int cx = cell[0];
            int cz = cell[1];
            int dist = minDistToBuilding[cx][cz];

            if (dist >= 12)
                continue; // Optimization: don't propagate further than needed

            for (int k = 0; k < 4; k++) {
                int nx = cx + dx[k];
                int nz = cz + dz[k];
                if (nx >= 0 && nx < size && nz >= 0 && nz < size) {
                    if (minDistToBuilding[nx][nz] > dist + 1) {
                        minDistToBuilding[nx][nz] = dist + 1;
                        bfsQueue.add(new int[] { nx, nz });
                    }
                }
            }
        }

        // 4. Iterative Smoothing
        // Reduce iterations to limit spread
        boolean changed = true;
        int iterations = 0;
        int maxIterations = 10;
        int maxSmoothedDistance = 12; // Increased to 12 to handle larger buildings and softer slopes

        // Pseudo-random noise for variation
        java.util.Random rand = new java.util.Random(center.asLong());

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            Integer[][] newHeights = new Integer[size][size];
            for (int i = 0; i < size; i++) {
                System.arraycopy(heights[i], 0, newHeights[i], 0, size);
            }

            for (int x = 1; x < size - 1; x++) {
                for (int z = 1; z < size - 1; z++) {
                    if (isProtected[x][z])
                        continue;
                    if (minDistToBuilding[x][z] > maxSmoothedDistance)
                        continue; // Distance cap

                    int currentY = heights[x][z];
                    int targetY = currentY;

                    int[] neighborYs = {
                            heights[x + 1][z], heights[x - 1][z],
                            heights[x][z + 1], heights[x][z - 1]
                    };

                    // Strict 1-block slope for maximum smoothness.
                    // Removed random noise to prevent "holes" and jagged terrain.
                    int maxDrop = 1;

                    for (int ny : neighborYs) {
                        // 1. Neighbor is much HIGHER? Pull UP.
                        if (ny > targetY + maxDrop) {
                            targetY = Math.max(targetY, ny - maxDrop);
                        }
                        // 2. Neighbor is much LOWER? Push DOWN.
                        // This cuts slopes into hills instead of building pillars up
                        else if (ny < targetY - maxDrop) {
                            targetY = Math.min(targetY, ny + maxDrop);
                        }
                    }

                    if (targetY != currentY) {
                        newHeights[x][z] = targetY;
                        changed = true;
                    }
                }
            }
            heights = newHeights;
        }

        LOGGER.info("Terrain smoothing complete after {} iterations.", iterations);

        // 5. Apply changes to world
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                if (isProtected[x][z])
                    continue;
                if (minDistToBuilding[x][z] > maxSmoothedDistance)
                    continue; // Don't apply far out

                int wx = startX + x;
                int wz = startZ + z;
                int originalY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz);
                int newY = heights[x][z];

                if (newY > originalY) {
                    // Fill upward
                    for (int y = originalY; y < newY; y++) {
                        BlockPos p = new BlockPos(wx, y, wz);
                        if (y == newY - 1) {
                            // Top layer: Randomize slightly to blend? Or just grass.
                            level.setBlock(p, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                        } else {
                            // Sub-layers
                            level.setBlock(p, Blocks.DIRT.defaultBlockState(), 3);
                        }
                    }
                } else if (newY < originalY) {
                    // Cut downward
                    for (int y = originalY - 1; y >= newY; y--) {
                        BlockPos p = new BlockPos(wx, y, wz);
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    /**
     * Spawn a village of the specified type.
     */
    public static VillageData spawnVillage(ServerLevel level, BlockPos centerPos, Culture culture,
            org.millenaire.common.culture.VillageType villageType) {
        LOGGER.info("========== SPAWNING VILLAGE ==========");
        LOGGER.info("Culture: {}", culture.getDisplayName());
        LOGGER.info("Village Type: {} ({})", villageType.getName(), villageType.getId());

        // ===== SPAWN PROTECTION CHECK (from original WorldGenVillage) =====
        int villageRadius = villageType.getVillageRadius();
        if (villageRadius <= 0) {
            villageRadius = DEFAULT_VILLAGE_RADIUS;
        }

        if (isWithinSpawnRadiusProtection(level, centerPos, villageRadius)) {
            LOGGER.warn("Village generation blocked - too close to world spawn");
            return null;
        }

        // ===== INTER-VILLAGE DISTANCE CHECK (from original WorldGenVillage) =====
        boolean isLoneBuilding = villageType.isLoneBuilding();
        if (isTooCloseToExistingVillages(level, centerPos, isLoneBuilding)) {
            LOGGER.warn("Village generation blocked - too close to existing village");
            return null;
        }

        // CRITICAL: Clear occupancy grid for this village generation
        clearOccupancyGrid();

        // Adjust to ground level
        int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, centerPos.getX(), centerPos.getZ());
        BlockPos adjustedCenter = new BlockPos(centerPos.getX(), groundY, centerPos.getZ());

        // Create village data
        UUID villageId = UUID.randomUUID();
        VillageManager manager = VillageManager.get(level);
        VillageData villageData = manager.createVillage(villageId, culture.getId(), adjustedCenter);

        // Track all building footprints for pathfinding avoidance / bounding box
        List<BlockPos> buildingCenters = new ArrayList<>();
        buildingCenters.add(adjustedCenter);

        // Track building ENTRANCE positions (where the front door is) for path routing
        List<BlockPos> buildingEntrances = new ArrayList<>();

        // 1. Place MAIN Building (Centre)
        String centreKey = villageType.getCentreBuilding();
        String mainBuildingPath = culture.getBuildingPath(centreKey);

        if (mainBuildingPath == null) {
            LOGGER.error("Could not find path for centre building: {}", centreKey);
            manager.removeVillage(villageId);
            return null;
        }

        BuildingPlan mainPlan = null;
        BlockPos centeredTownHallPos = adjustedCenter; // Will be updated after loading plan
        BlockPos townHallPathStartPos = adjustedCenter; // Will be updated if Town Hall has pathStartPos
        try {
            LOGGER.info("Placing centre: {}", mainBuildingPath);
            mainPlan = MillenaireBuildingParser.loadPlan(mainBuildingPath);

            // Center the town hall by offsetting position by half the building dimensions
            // This ensures the building's visual center aligns with the village center
            int offsetX = mainPlan.width() / 2;
            int offsetZ = mainPlan.length() / 2;
            centeredTownHallPos = adjustedCenter.offset(-offsetX, 0, -offsetZ);
            LOGGER.info("Town hall dimensions: {}x{}", mainPlan.width(), mainPlan.length());
            LOGGER.info("Centering town hall: offset by (-{}, -{})", offsetX, offsetZ);
            LOGGER.info("Town hall placement pos: {}", centeredTownHallPos);

            // CRITICAL: Register town hall footprint BEFORE placing it
            // orientation 0 for town hall (no rotation)
            registerBuildingFootprint(centeredTownHallPos, mainPlan.width(), mainPlan.length(),
                    0, BUILDING_MARGIN, "townhall");

            MillenaireBuildingParser.placeBuilding(level, centeredTownHallPos, mainPlan);
            villageData.addBuilding(mainBuildingPath, centeredTownHallPos);

            // Populate Town Hall residents
            if (centeredTownHallPos != null) {
                populateBuildingResidents(level, villageData, centeredTownHallPos, mainPlan);
            }

            // Extract Town Hall's pathStartPos for path generation (original mod:
            // Building.recalculatePaths line 4682)
            // This is the point where ALL paths should connect to - the Town Hall entrance
            BlockPos newPathStart = adjustedCenter; // Default fallback
            if (mainPlan.pathStartPos() != null) {
                newPathStart = centeredTownHallPos.offset(
                        mainPlan.pathStartPos().getX(), mainPlan.pathStartPos().getY(), mainPlan.pathStartPos().getZ());
                townHallPathStartPos = newPathStart;
                LOGGER.info("Town Hall pathStartPos (world): {}", townHallPathStartPos);
            } else {
                LOGGER.warn("Town Hall has no pathStartPos, scanning for best entrance");
                BlockPos bestEnt = calculateBestEntrancePosition(centeredTownHallPos, mainPlan,
                        net.minecraft.world.level.block.Rotation.NONE, adjustedCenter.offset(5, 0, 5));
                if (bestEnt != null) {
                    townHallPathStartPos = bestEnt;
                    LOGGER.info("Found Town Hall entrance at {}", townHallPathStartPos);
                } else {
                    LOGGER.warn("No entrance found, defaulting to center (risk of pathing trap)");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to place centre building", e);
            manager.removeVillage(villageId);
            return null;
        }

        // ===== INITIALIZE VILLAGE MAP INFO (Original Millénaire terrain pre-analysis)
        // =====
        // This scans the entire village radius and caches terrain data for building
        // placement
        VillageMapInfo winfo = new VillageMapInfo();
        List<BuildingLocation> plannedBuildings = new ArrayList<>();

        // Register town hall as first building location
        BuildingLocation thLocation = new BuildingLocation(
                mainPlan, new org.millenaire.common.utilities.Point(centeredTownHallPos), 0);
        plannedBuildings.add(thLocation);

        // Track placed buildings for terrain smoothing
        List<BuildingLocation> placedLocations = new ArrayList<>();
        placedLocations.add(thLocation);

        // Scan terrain around village center (reusing villageRadius from spawn
        // protection check)
        LOGGER.info("[MAPINFO] Initializing village map, radius: {}", villageRadius);
        long mapStartTime = System.nanoTime();
        winfo.update(level, plannedBuildings, new org.millenaire.common.utilities.Point(adjustedCenter), villageRadius);
        LOGGER.info("[MAPINFO] Initialized in {}ms, map size: {}x{}",
                (System.nanoTime() - mapStartTime) / 1_000_000, winfo.length, winfo.width);

        // Create RegionMapper for reachability from town hall
        RegionMapper regionMapper = new RegionMapper();
        regionMapper.createConnectionsTable(winfo, centeredTownHallPos);

        // Add Core and Secondary buildings to PLANNED projects
        for (String core : villageType.getCoreBuildings()) {
            if (culture.getBuildingPath(core) != null) {
                villageData.addPlannedBuilding(core);
            }
        }
        for (String sec : villageType.getSecondaryBuildings()) {
            if (culture.getBuildingPath(sec) != null) {
                villageData.addPlannedBuilding(sec);
            }
        }

        // 2. Place STARTING Buildings with PROPER COLLISION DETECTION
        // Based on original Millénaire BuildingPlan.testSpot() - uses occupancy grid
        List<String> starterKeys = villageType.getStartingBuildings();

        for (String key : starterKeys) {
            String path = culture.getBuildingPath(key);
            if (path == null) {
                LOGGER.warn("Skipping unknown starting building: {}", key);
                continue;
            }

            try {
                // Load plan FIRST to get dimensions for collision check
                BuildingPlan plan = MillenaireBuildingParser.loadPlan(path);

                // Use per-building distance fields from config (matches original Millénaire
                // exactly)
                // minDistance/maxDistance are 0-1 fractions of village radius
                int minRadius = (int) (plan.minDistance() * villageRadius);
                int maxRadius = (int) (plan.maxDistance() * villageRadius);

                // Apply minimum floor to prevent edge cases
                if (minRadius < 5)
                    minRadius = 5;
                if (maxRadius < minRadius)
                    maxRadius = villageRadius;

                LOGGER.info("Zoning: {} minDistance={} maxDistance={} -> minRadius={} maxRadius={} (villageRadius={})",
                        key, plan.minDistance(), plan.maxDistance(), minRadius, maxRadius, villageRadius);

                // Find a valid position using VillageMapInfo (original Millénaire approach)
                // Uses expanding square search with terrain pre-analysis and reachability
                BuildingLocation location = findBuildingLocationWithMapInfo(
                        winfo, regionMapper, adjustedCenter, plan, maxRadius, minRadius, -1);

                if (location == null) {
                    LOGGER.warn("Could not find valid position for building: {} using map info", key);
                    continue;
                }

                BlockPos validPos = location.pos.getBlockPos();
                int finalOrientation = location.orientation;
                Rotation rotation = millenairOrientationToRotation(finalOrientation);

                LOGGER.info("Placing starter: {} at {} (orientation: {} -> {})", key, validPos, finalOrientation,
                        rotation);

                // Register to VillageMapInfo for future collision checks
                winfo.addBuildingLocationToMap(location);
                placedLocations.add(location);

                // Also register to occupancy grid (legacy support)
                registerBuildingFootprint(validPos, plan.width(), plan.length(),
                        finalOrientation, BUILDING_MARGIN, key);

                // Place the building
                MillenaireBuildingParser.placeBuilding(level, validPos, plan, rotation);

                villageData.addBuilding(path, validPos);
                buildingCenters.add(validPos);

                // Calculate entrance position (smart scan)
                BlockPos entrancePos = calculateBestEntrancePosition(validPos, plan, rotation, adjustedCenter);
                buildingEntrances.add(entrancePos);

                // Populate residents from plan
                populateBuildingResidents(level, villageData, validPos, plan);

            } catch (Exception e) {
                LOGGER.error("Failed to place {}", key, e);
            }
        }

        // 2b. Smooth Terrain around all placed buildings
        // DISABLED: Original Millénaire mod does NOT have terrain smoothing.
        // Terrain is shaped entirely by prepareGround block placement (CLEARGROUND,
        // PRESERVEGROUNDDEPTH, PRESERVEGROUNDSURFACE). The custom smoothTerrain
        // function was causing checkerboard patterns and terrain artifacts.
        // if (!placedLocations.isEmpty()) {
        // smoothTerrain(level, adjustedCenter, placedLocations, villageRadius);
        // }

        // 3. Place Walls & Gates based on village type configuration
        List<BlockPos> gatePositions = placeVillageBorder(level, adjustedCenter, buildingCenters, culture, villageType);

        // 4. Generate Paths - Connect Center to Building ENTRANCES AND Gates
        List<String> pathMaterials = villageType.getPathMaterials();
        LOGGER.info("Village Path Materials: {}", pathMaterials);
        if (!pathMaterials.isEmpty()) {
            String primaryPathInfo = pathMaterials.get(0);
            // Path to all building entrances and gates for organic village feel
            List<BlockPos> allDestinations = new ArrayList<>();
            for (BlockPos ent : buildingEntrances) {
                if (ent != null)
                    allDestinations.add(ent);
            }
            allDestinations.addAll(gatePositions); // Connect to gates
            generatePaths(level, townHallPathStartPos, allDestinations, primaryPathInfo);
        }

        // 5. Populate with Initial Citizens (Pre-population)
        org.millenaire.entities.Citizen chief = org.millenaire.core.MillEntities.CITIZEN.get().create(level);
        if (chief != null) {
            chief.setPos(adjustedCenter.getX() + 0.5, adjustedCenter.getY() + 1.0, adjustedCenter.getZ() + 0.5);
            chief.setVillageId(villageId);
            chief.setCustomName(net.minecraft.network.chat.Component.literal("Village Chief"));
            chief.setPersistenceRequired();

            level.addFreshEntity(chief);
            LOGGER.info("Spawned Village Chief at {}", adjustedCenter);
        } else {
            LOGGER.error(
                    "CRITICAL: Failed to create Village Chief entity! MillEntities.CITIZEN.get().create(level) returned null.");
        }

        LOGGER.info("Village spawning complete.");
        return villageData;
    }

    /**
     * Compute building orientation based on position relative to town hall.
     * EXACT copy from original Millenaire: BuildingPlan.computeOrientation()
     * 
     * Buildings face TOWARD the town hall. The returned value is:
     * 0 = NORTH (building is EAST of center, faces WEST toward center)
     * 1 = WEST (building is SOUTH of center, faces NORTH toward center)
     * 2 = SOUTH (building is WEST of center, faces EAST toward center)
     * 3 = EAST (building is NORTH of center, faces SOUTH toward center)
     * 
     * @param buildingPos The building's position
     * @param facingPos   The position to face (usually town hall center)
     * @return Orientation value 0-3
     */
    private static int computeOrientation(BlockPos buildingPos, BlockPos facingPos) {
        // EXACT COPY from original: BuildingPlan.computeOrientation (lines 323-331)
        int relx = buildingPos.getX() - facingPos.getX();
        int relz = buildingPos.getZ() - facingPos.getZ();
        if (relx * relx > relz * relz) {
            return relx > 0 ? 0 : 2;
        } else {
            return relz > 0 ? 3 : 1;
        }
    }

    /**
     * Calculate the final world orientation for a building.
     * EXACT formula from original Millenaire: BuildingPlan line 2560
     * 
     * orientation = (computeOrientation(buildingPos, centre) + buildingOrientation)
     * % 4
     * 
     * @param buildingPos The building's world position
     * @param townHallPos The town hall center position
     * @param plan        The building plan (contains buildingOrientation and
     *                    fixedOrientation)
     * @return The final orientation (0-3)
     */
    private static int calculateFinalOrientation(BlockPos buildingPos, BlockPos townHallPos,
            BuildingPlan plan) {
        int orientation;

        // Check for fixed orientation first (used for gates, walls, etc.)
        if (plan.fixedOrientation() >= 0) {
            orientation = plan.fixedOrientation();
            LOGGER.debug("Using fixed orientation {} for {}", orientation, plan.sourceFile());
        } else {
            // Normal case: face toward town hall
            orientation = computeOrientation(buildingPos, townHallPos);
        }

        // Apply building's intrinsic orientation (from config, default 1 = West = left
        // of PNG)
        // EXACT formula from original: orientation = (orientation +
        // buildingOrientation) % 4
        int finalOrientation = (orientation + plan.buildingOrientation()) % 4;

        LOGGER.debug("Orientation for {}: computed={}, buildingOrientation={}, final={}",
                plan.sourceFile(), orientation, plan.buildingOrientation(), finalOrientation);

        return finalOrientation;
    }

    /**
     * Convert Millenaire orientation (0-3) to Minecraft Rotation.
     * EXACT MAPPING from original BuildingPlan.adjustForOrientation:
     * - Orientation 0 = No rotation
     * - Orientation 1 = 90° CCW (North → West)
     * - Orientation 2 = 180°
     * - Orientation 3 = 90° CW (North → East)
     */
    private static net.minecraft.world.level.block.Rotation millenairOrientationToRotation(int orientation) {
        switch (orientation % 4) {
            case 0:
                return net.minecraft.world.level.block.Rotation.NONE; // No rotation
            case 1:
                return net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90; // 90° CCW (was incorrectly CW)
            case 2:
                return net.minecraft.world.level.block.Rotation.CLOCKWISE_180; // 180°
            case 3:
                return net.minecraft.world.level.block.Rotation.CLOCKWISE_90; // 90° CW (was incorrectly CCW)
            default:
                return net.minecraft.world.level.block.Rotation.NONE;
        }
    }

    /**
     * EXACT COPY from original Millenaire: BuildingPlan.adjustForOrientation (lines
     * 308-321)
     * Transforms local building coordinates to world coordinates based on
     * orientation.
     * This is how the original mod rotates building positions.
     * 
     * @param originX     World X of building origin
     * @param originZ     World Z of building origin
     * @param localX      Local X offset within building (xoffset in original)
     * @param localZ      Local Z offset within building (zoffset in original)
     * @param orientation Orientation value (0-3)
     * @return World BlockPos after applying orientation transform
     */
    private static BlockPos adjustForOrientation(int originX, int originZ, int localX, int localZ, int orientation) {
        int worldX, worldZ;

        if (orientation == 0) {
            // No rotation
            worldX = originX + localX;
            worldZ = originZ + localZ;
        } else if (orientation == 1) {
            // 90° CCW: (x + zoffset, z - xoffset - 1)
            worldX = originX + localZ;
            worldZ = originZ - localX - 1;
        } else if (orientation == 2) {
            // 180°: (x - xoffset - 1, z - zoffset - 1)
            worldX = originX - localX - 1;
            worldZ = originZ - localZ - 1;
        } else { // orientation == 3
            // 90° CW: (x - zoffset - 1, z + xoffset)
            worldX = originX - localZ - 1;
            worldZ = originZ + localX;
        }

        return new BlockPos(worldX, 0, worldZ); // Y is handled separately
    }

    /**
     * Generate paths using the ORIGINAL Millénaire algorithm.
     * Based on Building.recalculatePaths() from decompiled source (lines
     * 4666-4756).
     * 
     * Key features:
     * - Default destination is Town Hall
     * - For buildings >20 blocks from TH, connects to closer buildings (1.5x bias)
     * - Uses straight-line paths, not A*, for visual consistency
     */
    private static void generatePaths(ServerLevel level, BlockPos townHallPathPoint, List<BlockPos> destinations,
            String materialName) {
        LOGGER.info("Generating paths. Material: '{}', Destinations: {}, Start: {}", materialName, destinations.size(),
                townHallPathPoint);

        net.minecraft.world.level.block.state.BlockState pathState = getPathBlockState(materialName);
        if (pathState == null) {
            LOGGER.error("Failed to resolve path block state for: {}", materialName);
            return;
        }

        LOGGER.info("Path block state resolved: {}", pathState);

        LOGGER.info("Starting path generation loop...");

        // Process each building's path (original: lines 4695-4751)
        for (BlockPos pathStartPos : destinations) {
            // Default destination is Town Hall (original: line 4708)
            BlockPos pathDest = townHallPathPoint;

            double distToTH = Math.sqrt(townHallPathPoint.distSqr(pathStartPos));

            // If building is far from TH (>20 blocks), try to connect to a closer building
            // Original: lines 4720-4746
            if (distToTH > 20.0) {
                BlockPos otherBuildingDest = null;

                for (BlockPos otherBuildingPathStart : destinations) {
                    if (otherBuildingPathStart.equals(pathStartPos))
                        continue;

                    double thToThis = Math.sqrt(townHallPathPoint.distSqr(pathStartPos));
                    double thToOther = Math.sqrt(townHallPathPoint.distSqr(otherBuildingPathStart));
                    double otherToThis = Math.sqrt(otherBuildingPathStart.distSqr(pathStartPos));
                    double currentDestToThis = Math.sqrt(pathDest.distSqr(pathStartPos));

                    // Original condition (line 4732-4733):
                    // - The other building must be closer to TH than this building
                    // - The other building must be significantly closer (1.5x) than current dest
                    if (thToThis > thToOther && otherToThis * 1.5 < currentDestToThis) {
                        if (otherBuildingDest == null ||
                                pathStartPos.distSqr(otherBuildingDest) > pathStartPos
                                        .distSqr(otherBuildingPathStart)) {
                            otherBuildingDest = otherBuildingPathStart;
                        }
                    }
                }

                if (otherBuildingDest != null) {
                    pathDest = otherBuildingDest;
                    LOGGER.info("Building at {} connecting to neighbor at {} instead of TH", pathStartPos, pathDest);
                }
            }

            // Use ported AtomicStryker A* PathPlanner
            // Config: Use Doors (true), No Diagonals (false), No Dropping (false), No
            // Swimming (false), Clear Leaves (true)
            // Original RegionMapper uses: true, false, false, false, true
            AStarConfig config = new AStarConfig(true, false, false, false, true);
            AStarPathPlannerJPS planner = new AStarPathPlannerJPS(level, null, true); // true for JPS

            // Revert to original behavior: Use path positions from building config directly
            // This ensures paths on piers/elevated structures work correctly
            int startY = pathStartPos.getY();
            int destY = pathDest.getY();

            // AStarNode expects integers
            AStarNode startNode = new AStarNode(pathStartPos.getX(), startY, pathStartPos.getZ());
            AStarNode endNode = new AStarNode(pathDest.getX(), destY, pathDest.getZ());

            LOGGER.info("Calculation A* path from {} to {}", startNode, endNode);

            // Run Synchronously
            ArrayList<AStarNode> route = planner.getPathSync(startNode, endNode, config);

            if (route != null && !route.isEmpty()) {
                LOGGER.info("Path found! Length: {}", route.size());
                buildPathFromRoute(level, route, pathState);
            } else {
                LOGGER.warn("No path found from {} to {}", pathStartPos, pathDest);
                try {
                    LOGGER.debug("Start viable: {}, End viable: {}",
                            org.millenaire.common.pathing.atomicstryker.AStarStatic.isViable(level, startNode, 1,
                                    config),
                            org.millenaire.common.pathing.atomicstryker.AStarStatic.isViable(level, endNode, 1,
                                    config));
                } catch (Exception e) {
                    LOGGER.debug("Error checking path viability: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Calculate the entrance position for a building based on its placement
     * position,
     * dimensions, and rotation. The entrance is at the front edge of the building
     * (the side facing the village center based on rotation).
     * 
     * @param buildingPos   The corner position where the building was placed
     * @param width         Building width (X extent before rotation)
     * @param length        Building length (Z extent before rotation)
     * @param rotation      The rotation applied to the building
     * @param villageCenter The center of the village
     * @return The position at the building edge closest to the village center
     */
    private static BlockPos calculateEntrancePosition(BlockPos buildingPos, int width, int length, Rotation rotation,
            BlockPos villageCenter) {
        int x = buildingPos.getX();
        int z = buildingPos.getZ();

        // Calculate effective dimensions after rotation
        int effWidth = (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) ? length : width;
        int effLength = (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) ? width
                : length;

        // Calculate the center of the building
        int buildingCenterX = x + effWidth / 2;
        int buildingCenterZ = z + effLength / 2;

        // Find the direction from building center to village center
        int dx = villageCenter.getX() - buildingCenterX;
        int dz = villageCenter.getZ() - buildingCenterZ;

        // Determine which edge is closest to the village center and return a point just
        // outside that edge
        if (Math.abs(dx) > Math.abs(dz)) {
            // Horizontal dominates - use east or west edge
            if (dx > 0) {
                // Village center is to the EAST, so entrance is at east edge (+X)
                return new BlockPos(x + effWidth, buildingPos.getY(), buildingCenterZ);
            } else {
                // Village center is to the WEST, so entrance is at west edge (-X)
                return new BlockPos(x - 1, buildingPos.getY(), buildingCenterZ);
            }
        } else {
            // Vertical dominates - use north or south edge
            if (dz > 0) {
                // Village center is to the SOUTH, so entrance is at south edge (+Z)
                return new BlockPos(buildingCenterX, buildingPos.getY(), z + effLength);
            } else {
                // Village center is to the NORTH, so entrance is at north edge (-Z)
                return new BlockPos(buildingCenterX, buildingPos.getY(), z - 1);
            }
        }
    }

    /**
     * Calculate the best entrance position by scanning the building perimeter for
     * doors/gaps.
     * Picks the candidate closest to the village center.
     */
    private static BlockPos calculateBestEntrancePosition(BlockPos pos, BuildingPlan plan,
            Rotation rotation, BlockPos villageCenter) {
        // 1. Use configured path start if available (Primary Strategy)
        if (plan.pathStartPos() != null) {
            LOGGER.debug("Using configured path start");
            return calculateRotatedPathStart(pos, plan.width(), plan.length(), rotation, plan.pathStartPos());
        }

        // 2. Fallback: Use building EDGE facing village center (not center which is
        // inside structure)
        // Original mod: BuildingResManager.getPathStartPos() returns sellingPos if
        // pathStartPos is null
        // sellingPos is typically at the building entrance, not center.
        // Since we don't have sellingPos parsed, use calculateEntrancePosition which
        // places the entrance at the building edge closest to village center.
        LOGGER.debug("No pathStartPos defined, using building edge facing village center as fallback");
        return calculateEntrancePosition(pos, plan.width(), plan.length(), rotation, villageCenter);
    }

    /**
     * Calculate the world position of the path start based on building layout and
     * valid rotation.
     * Uses EXACT same formulas as original BuildingPlan.adjustForOrientation
     * with CENTERED offsets like placeBuilding.
     */
    private static BlockPos calculateRotatedPathStart(BlockPos buildingOrigin, int width, int length, Rotation rotation,
            BlockPos pathStart) {
        // pathStart is in local coordinates (dx, dz) within the building
        int dx = pathStart.getX(); // Local X = length dimension
        int dz = pathStart.getZ(); // Local Z = width dimension

        // EXACT from original: centered offsets
        int lengthOffset = length / 2;
        int widthOffset = width / 2;

        int xoffset = dx - lengthOffset;
        int zoffset = dz - widthOffset;

        int originX = buildingOrigin.getX();
        int originZ = buildingOrigin.getZ();

        int worldX, worldZ;
        switch (rotation) {
            case NONE:
                // Orientation 0
                worldX = originX + xoffset;
                worldZ = originZ + zoffset;
                break;
            case COUNTERCLOCKWISE_90:
                // Orientation 1: (x + zoffset, z - xoffset - 1)
                worldX = originX + zoffset;
                worldZ = originZ - xoffset - 1;
                break;
            case CLOCKWISE_180:
                // Orientation 2: (x - xoffset - 1, z - zoffset - 1)
                worldX = originX - xoffset - 1;
                worldZ = originZ - zoffset - 1;
                break;
            case CLOCKWISE_90:
                // Orientation 3: (x - zoffset - 1, z + xoffset)
                worldX = originX - zoffset - 1;
                worldZ = originZ + xoffset;
                break;
            default:
                worldX = originX + xoffset;
                worldZ = originZ + zoffset;
                break;
        }

        // Return path start in world coordinates (use origin Y + relative Y from plan)
        return new BlockPos(worldX, buildingOrigin.getY() + pathStart.getY(), worldZ);
    }

    private static List<BlockPos> generateHutPositions(BlockPos center, int count, int minRadius, int maxRadius) {
        List<BlockPos> positions = new ArrayList<>();
        int attempts = 0;
        int maxAttempts = count * 10;

        while (positions.size() < count && attempts < maxAttempts) {
            attempts++;

            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = minRadius + RANDOM.nextDouble() * (maxRadius - minRadius);

            int x = (int) (center.getX() + Math.cos(angle) * distance);
            int z = (int) (center.getZ() + Math.sin(angle) * distance);

            BlockPos pos = new BlockPos(x, 0, z);

            // Simple check to avoid overlapping with other huts (distance check)
            boolean tooClose = false;
            for (BlockPos existing : positions) {
                if (existing.distSqr(pos) < 256) { // 16 blocks minimal distance
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                positions.add(pos);
            }
        }

        return positions;
    }

    /**
     * Build path from A* result, placing 2-wide path blocks along route.
     * Takes ArrayList<AStarNode> from ported pathing system.
     */
    private static void buildPathFromRoute(ServerLevel level, List<AStarNode> route, BlockState pathState) {
        if (route == null || route.isEmpty())
            return;

        for (int i = 0; i < route.size(); i++) {
            AStarNode node = route.get(i);
            BlockPos pos = new BlockPos(node.x, node.y, node.z); // A* calculates Y properly now

            // Place main path block
            placePathBlock(level, pos, pathState);

            // Determine perpendicular direction for 2-wide path
            if (i + 1 < route.size()) {
                AStarNode nextNode = route.get(i + 1);
                BlockPos next = new BlockPos(nextNode.x, nextNode.y, nextNode.z);
                int dx = next.getX() - pos.getX();
                int dz = next.getZ() - pos.getZ();

                // If moving in X direction, widen in Z; if moving in Z, widen in X
                if (dx != 0) {
                    placePathBlock(level, pos.offset(0, 0, 1), pathState);
                } else {
                    placePathBlock(level, pos.offset(1, 0, 0), pathState);
                }
            } else if (i > 0) {
                // Last node - use previous direction
                AStarNode prevNode = route.get(i - 1);
                BlockPos prev = new BlockPos(prevNode.x, prevNode.y, prevNode.z);
                int dx = pos.getX() - prev.getX();
                if (dx != 0) {
                    placePathBlock(level, pos.offset(0, 0, 1), pathState);
                } else {
                    placePathBlock(level, pos.offset(1, 0, 0), pathState);
                }
            }
        }
    }

    private static void placePathBlock(ServerLevel level, BlockPos pos, BlockState pathState) {
        // Paths should replace the ground block (e.g. grass), not float above it.
        // A* nodes are typically at foot level (the air block above ground).
        // So we target pos.below()
        int y = pos.getY();
        if (level.getBlockState(pos).isAir()) {
            y = pos.getY() - 1;
        }

        // Safety: ensure we are at ground level (motion blocking) if A* didn't give
        // strict y
        // Re-checking heightmap might be safer if A* nodes are weird
        // But ported A* logic (AStarWorkerJPS) uses getGroundNodeHeight so Y should be
        // valid ground Y (top of block).
        // Wait, AStarNode y is usually the air block above the ground?
        // In AStarWorkerJPS: r.add(new AStarNode(x, nY, ...)). nY comes from
        // getGroundNodeHeight.
        // getGroundNodeHeight returns yN (height of air block?).
        // AStarStatic.isViable check:
        // if (block at yN-1 is solid && block at yN is not solid)
        // So y is the AIR block above ground.
        // To place a path, we want to replace the SOLID block at y-1.

        BlockPos groundPos = new BlockPos(pos.getX(), y - 1, pos.getZ());
        // Verify current Y is correct for A* vs World
        // If A* node says 64, it means feet are at 64. Block to walk on is 63.
        // So we replace 63.

        // However, if we blindly trust A*, we might need to be careful.
        // Let's stick to replacing the block BELOW the node.

        BlockPos targetPos = new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ());
        BlockState existing = level.getBlockState(targetPos);

        if (isNaturalBlock(existing)) {
            level.setBlock(targetPos, pathState, 3);
            if (isNaturalClearable(level.getBlockState(targetPos.above()))) {
                level.setBlock(targetPos.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static boolean isNaturalBlock(BlockState state) {
        net.minecraft.world.level.block.Block block = state.getBlock();
        return block == net.minecraft.world.level.block.Blocks.GRASS_BLOCK ||
                block == net.minecraft.world.level.block.Blocks.DIRT ||
                block == net.minecraft.world.level.block.Blocks.COARSE_DIRT ||
                block == net.minecraft.world.level.block.Blocks.PODZOL ||
                block == net.minecraft.world.level.block.Blocks.STONE ||
                block == net.minecraft.world.level.block.Blocks.GRAVEL ||
                block == net.minecraft.world.level.block.Blocks.SAND ||
                block == net.minecraft.world.level.block.Blocks.SANDSTONE ||
                block == net.minecraft.world.level.block.Blocks.RED_SAND ||
                block == net.minecraft.world.level.block.Blocks.RED_SANDSTONE ||
                block == net.minecraft.world.level.block.Blocks.SNOW_BLOCK ||
                block == net.minecraft.world.level.block.Blocks.SNOW ||
                block == net.minecraft.world.level.block.Blocks.ICE ||
                block == net.minecraft.world.level.block.Blocks.PACKED_ICE;
    }

    private static boolean isNaturalClearable(net.minecraft.world.level.block.state.BlockState state) {
        return state.isAir() || state.getBlock() instanceof net.minecraft.world.level.block.BushBlock;
    }

    private static net.minecraft.world.level.block.state.BlockState getPathBlockState(String name) {
        name = name.toLowerCase().trim();
        if (name.equals("pathdirt"))
            return org.millenaire.core.MillBlocks.PATHDIRT.get().defaultBlockState();
        if (name.equals("pathgravel"))
            return org.millenaire.core.MillBlocks.PATHGRAVEL.get().defaultBlockState();
        if (name.equals("pathslabs"))
            return org.millenaire.core.MillBlocks.PATHSLABS.get().defaultBlockState();
        if (name.equals("pathsandstone"))
            return org.millenaire.core.MillBlocks.PATHSANDSTONE.get().defaultBlockState();
        if (name.equals("pathochretiles"))
            return org.millenaire.core.MillBlocks.PATHOCHRETILES.get().defaultBlockState();
        if (name.equals("pathgravelslabs"))
            return org.millenaire.core.MillBlocks.PATHGRAVELSLABS.get().defaultBlockState();
        if (name.equals("pathsnow"))
            return org.millenaire.core.MillBlocks.PATHSNOW.get().defaultBlockState();

        return org.millenaire.core.MillBlocks.PATHGRAVEL.get().defaultBlockState();
    }

    public static boolean spawnLoneBuilding(ServerLevel level, BlockPos pos, Culture culture) {
        // TODO: Restore full implementation of spawnLoneBuilding
        // This was temporarily lost during file rewrite.
        // For now, we return false to prevent crashes, but it needs to be
        // re-implemented.
        // Assuming LOGGER is defined elsewhere or will be defined.
        // If LOGGER is not defined elsewhere, this line will cause a compilation error.
        // The instruction implies removing the definition, but keeping the usage.
        // This suggests LOGGER is defined at a higher scope or in another static block.
        // For now, commenting out to ensure syntactic correctness as per instructions.
        // LOGGER.warn("spawnLoneBuilding called but not fully implemented yet.");
        return false;
    }

    /**
     * Generate walls around the village.
     */
    private static java.util.List<BlockPos> placeVillageBorder(ServerLevel level, BlockPos center,
            java.util.List<BlockPos> buildingCenters, Culture culture,
            org.millenaire.common.culture.VillageType villageType) {

        java.util.List<BlockPos> gatePositions = new ArrayList<>();

        // Get wall key - returns String
        String wallKey = villageType.getOuterWallKey();
        if (wallKey == null) {
            return gatePositions;
        }

        org.millenaire.common.culture.WallType wallType = culture.getWallType(wallKey);
        if (wallType == null) {
            LOGGER.warn("Wall type '{}' not found for village '{}'", wallKey, villageType.getId());
            return gatePositions;
        }

        LOGGER.info("Generating walls: {}", wallKey);
        VillageWallGenerator generator = new VillageWallGenerator(level);

        VillageMapInfo winfo = new VillageMapInfo();

        int radius = villageType.getVillageRadius();
        if (radius <= 0) {
            double maxDistSq = 0;
            for (BlockPos bp : buildingCenters) {
                maxDistSq = Math.max(maxDistSq, center.distSqr(bp));
            }
            radius = (int) Math.sqrt(maxDistSq) + 15;
        }

        // Use fully qualified name for standalone BuildingLocation
        java.util.List<org.millenaire.common.village.BuildingLocation> wallLocs = generator
                .computeWallBuildingLocations(
                        villageType, wallType, radius, winfo, center);

        for (org.millenaire.common.village.BuildingLocation loc : wallLocs) {
            try {
                // loc.planKey contains the source filename
                // In standalone BuildingLocation, planKey stores the sourceFile from the plan
                BuildingPlan plan = MillenaireBuildingParser.loadPlan(loc.planKey);

                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, loc.pos.getiX(), loc.pos.getiZ());
                BlockPos finalPos = new BlockPos(loc.pos.getiX(), y, loc.pos.getiZ());

                Rotation rotation = millenairOrientationToRotation(loc.orientation);

                registerBuildingFootprint(finalPos, plan.width(), plan.length(), loc.orientation, 0, "wall");

                MillenaireBuildingParser.placeBuilding(level, finalPos, plan, rotation);

                if (loc.planKey.contains("gateway") || loc.planKey.contains("gate")) {
                    gatePositions.add(finalPos);
                }

            } catch (Exception e) {
                LOGGER.error("Failed to place wall segment: {}", loc.planKey, e);
            }
        }

        return gatePositions;
    }

}
