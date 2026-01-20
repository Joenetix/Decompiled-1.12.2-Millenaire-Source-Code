package org.millenaire.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Pre-analyzed terrain information for a village area.
 * Ported from original Millénaire VillageMapInfo.
 * 
 * Scans the entire village radius and stores terrain data in 2D arrays
 * for fast lookup during building placement.
 */
public class VillageMapInfo {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static short[][] shortArrayDeepClone(short[][] source) {
        if (source == null)
            return null;
        short[][] dest = new short[source.length][];
        for (int i = 0; i < source.length; i++) {
            if (source[i] != null)
                dest[i] = source[i].clone();
        }
        return dest;
    }

    private static final int MAP_MARGIN = 5;
    private static final int BUILDING_MARGIN = 5;
    private static final int VALID_HEIGHT_DIFF = 10;

    // Map dimensions
    public int length = 0; // X dimension
    public int width = 0; // Z dimension
    public int chunkStartX = 0;
    public int chunkStartZ = 0;
    public int mapStartX = 0;
    public int mapStartZ = 0;
    public int yBaseline = 0;

    // Terrain data arrays - indexed by [x][z] relative to mapStart
    public short[][] topGround; // Surface height at each cell
    public short[][] spaceAbove; // Vertical clearance
    public boolean[][] danger; // Hazards (lava, cactus, etc.)
    public boolean[][] canBuild; // Is building allowed here?
    public boolean[][] buildingForbidden; // Mod-specific forbidden
    public boolean[][] water; // Water bodies
    public boolean[][] tree; // Tree logs present
    public boolean[][] path; // Existing paths
    public boolean[][] buildTested; // Already tested for building
    public boolean[][] topAdjusted; // Track if topGround was manually adjusted

    // Building collision tracking
    public BuildingLocation[][] buildingLocRef;
    private List<BuildingLocation> buildingLocations = new ArrayList<>();

    public ServerLevel level;

    /**
     * Simple building location record for collision tracking.
     */
    public static class BuildingLocation {
        public final BlockPos pos;
        public final int width;
        public final int length;
        public final int orientation;
        public final String planKey;

        // Bounds with margin
        public int minx, maxx, minz, maxz;
        public int minxMargin, maxxMargin, minzMargin, maxzMargin;

        public BuildingLocation(BlockPos pos, int width, int length, int orientation, String planKey) {
            this.pos = pos;
            this.width = width;
            this.length = length;
            this.orientation = orientation;
            this.planKey = planKey;

            // Calculate bounds based on orientation
            int actualWidth = (orientation == 1 || orientation == 3) ? length : width;
            int actualLength = (orientation == 1 || orientation == 3) ? width : length;

            int halfW = actualWidth / 2;
            int halfL = actualLength / 2;

            this.minx = pos.getX() - halfW;
            this.maxx = pos.getX() + halfW;
            this.minz = pos.getZ() - halfL;
            this.maxz = pos.getZ() + halfL;

            // Add margin for spacing between buildings
            this.minxMargin = minx - BUILDING_MARGIN;
            this.maxxMargin = maxx + BUILDING_MARGIN;
            this.minzMargin = minz - BUILDING_MARGIN;
            this.maxzMargin = maxz + BUILDING_MARGIN;
        }
    }

    /**
     * Register a building location on the map.
     */
    public void addBuildingLocationToMap(BuildingLocation bl) {
        LOGGER.debug("[MAPINFO] Registering building: {} at {}", bl.planKey, bl.pos);

        buildingLocations.add(bl);

        // Mark cells as occupied
        int sx = Math.max(bl.minxMargin - mapStartX, 0);
        int sz = Math.max(bl.minzMargin - mapStartZ, 0);
        int ex = Math.min(bl.maxxMargin - mapStartX, length);
        int ez = Math.min(bl.maxzMargin - mapStartZ, width);

        for (int i = sx; i < ex; i++) {
            for (int j = sz; j < ez; j++) {
                buildingLocRef[i][j] = bl;
            }
        }
    }

    /**
     * Get all registered building locations.
     */
    public List<BuildingLocation> getBuildingLocations() {
        return buildingLocations;
    }

    /**
     * Initialize terrain info for the village area.
     */
    private void createWorldInfo(List<BuildingLocation> locations, int pstartX, int pstartZ, int endX, int endZ) {
        LOGGER.debug("[MAPINFO] Creating world info: {}/{} to {}/{}", pstartX, pstartZ, endX, endZ);

        // Align to chunk boundaries
        chunkStartX = pstartX >> 4;
        chunkStartZ = pstartZ >> 4;
        mapStartX = chunkStartX << 4;
        mapStartZ = chunkStartZ << 4;
        length = ((endX >> 4) + 1 << 4) - mapStartX;
        width = ((endZ >> 4) + 1 << 4) - mapStartZ;

        LOGGER.info("[MAPINFO] Map dimensions: {}x{} starting at {},{}", length, width, mapStartX, mapStartZ);

        // Initialize arrays
        topGround = new short[length][width];
        spaceAbove = new short[length][width];
        danger = new boolean[length][width];
        buildingLocRef = new BuildingLocation[length][width];
        buildingForbidden = new boolean[length][width];
        canBuild = new boolean[length][width];
        buildTested = new boolean[length][width];
        water = new boolean[length][width];
        tree = new boolean[length][width];
        path = new boolean[length][width];
        topAdjusted = new boolean[length][width];
        buildingLocations = new ArrayList<>();

        // Initialize defaults
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                buildingLocRef[i][j] = null;
                canBuild[i][j] = false;
            }
        }

        // Register existing building locations
        for (BuildingLocation location : locations) {
            addBuildingLocationToMap(location);
        }

        // Scan terrain chunk by chunk
        for (int i = 0; i < length; i += 16) {
            for (int j = 0; j < width; j += 16) {
                updateChunk(i, j);
            }
        }
    }

    /**
     * Scan a 16x16 chunk area for terrain data.
     */
    private void updateChunk(int startX, int startZ) {
        LOGGER.debug("[MAPINFO] Updating chunk at local {},{}", startX, startZ);

        for (int i = 0; i < 16 && (startX + i) < length; i++) {
            for (int j = 0; j < 16 && (startZ + j) < width; j++) {
                int mx = i + startX;
                int mz = j + startZ;
                int worldX = mx + mapStartX;
                int worldZ = mz + mapStartZ;

                // Get surface height
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, worldX, worldZ);
                topGround[mx][mz] = (short) y;

                // Check blocks at and below surface
                BlockPos surfacePos = new BlockPos(worldX, y, worldZ);
                BlockPos belowPos = new BlockPos(worldX, y - 1, worldZ);
                BlockState surfaceState = level.getBlockState(surfacePos);
                BlockState belowState = level.getBlockState(belowPos);
                Block surfaceBlock = surfaceState.getBlock();
                Block belowBlock = belowState.getBlock();

                // Reset flags
                canBuild[mx][mz] = false;
                buildingForbidden[mx][mz] = false;
                water[mx][mz] = false;
                tree[mx][mz] = false;
                danger[mx][mz] = false;
                path[mx][mz] = false;
                spaceAbove[mx][mz] = 0;

                // Check for water
                if (surfaceState.getFluidState().isSource() ||
                        belowState.getFluidState().isSource()) {
                    water[mx][mz] = true;
                }

                // Check for trees (logs)
                if (belowState.is(BlockTags.LOGS)) {
                    tree[mx][mz] = true;
                }

                // Check for danger blocks
                if (isDangerousBlock(surfaceBlock) || isDangerousBlock(belowBlock)) {
                    danger[mx][mz] = true;
                }

                // Check for forbidden blocks
                if (isForbiddenBlock(surfaceBlock) || isForbiddenBlock(belowBlock)) {
                    buildingForbidden[mx][mz] = true;
                }

                // Check for paths
                if (isPathBlock(belowBlock)) {
                    path[mx][mz] = true;
                }

                // Calculate space above
                int space = 0;
                for (int dy = 0; dy < 4; dy++) {
                    BlockState aboveState = level.getBlockState(new BlockPos(worldX, y + dy, worldZ));
                    if (aboveState.isAir() || !aboveState.isSolid()) {
                        space++;
                    } else {
                        break;
                    }
                }
                spaceAbove[mx][mz] = (short) space;

                // Determine if buildable
                // Must: not danger, not forbidden, not water, within height range of baseline
                if (!danger[mx][mz] &&
                        !buildingForbidden[mx][mz] &&
                        !water[mx][mz] &&
                        buildingLocRef[mx][mz] == null &&
                        y > yBaseline - VALID_HEIGHT_DIFF &&
                        y < yBaseline + VALID_HEIGHT_DIFF) {
                    canBuild[mx][mz] = true;
                }

                // If already has a building, use building's Y level
                if (buildingLocRef[mx][mz] != null) {
                    topGround[mx][mz] = (short) buildingLocRef[mx][mz].pos.getY();
                    spaceAbove[mx][mz] = 3;
                }
            }
        }
    }

    /**
     * Check if a block is dangerous (damages entities).
     */
    private boolean isDangerousBlock(Block block) {
        return block == Blocks.LAVA ||
                block == Blocks.CACTUS ||
                block == Blocks.FIRE ||
                block == Blocks.SOUL_FIRE ||
                block == Blocks.MAGMA_BLOCK ||
                block == Blocks.SWEET_BERRY_BUSH ||
                block == Blocks.WITHER_ROSE;
    }

    /**
     * Check if a block forbids building.
     */
    private boolean isForbiddenBlock(Block block) {
        return block == Blocks.BEDROCK ||
                block == Blocks.END_PORTAL_FRAME ||
                block == Blocks.END_PORTAL ||
                block == Blocks.NETHER_PORTAL ||
                block == Blocks.SPAWNER;
    }

    /**
     * Check if a block is a path.
     */
    private boolean isPathBlock(Block block) {
        return block == Blocks.DIRT_PATH ||
                block == Blocks.GRAVEL ||
                block == Blocks.COBBLESTONE;
    }

    /**
     * Update or create map info for the given area.
     * 
     * @param level     The server level
     * @param locations Existing building locations
     * @param centre    Village center
     * @param radius    Village radius
     * @return true if map was recreated (size changed)
     */
    public boolean update(ServerLevel level, List<BuildingLocation> locations, BlockPos centre, int radius) {
        this.level = level;
        this.yBaseline = centre.getY();

        // Calculate bounds
        int startX = centre.getX();
        int startZ = centre.getZ();
        int endX = centre.getX();
        int endZ = centre.getZ();

        // Expand bounds to include all building locations
        for (BuildingLocation location : locations) {
            if (location != null) {
                startX = Math.min(startX, location.minx);
                endX = Math.max(endX, location.maxx);
                startZ = Math.min(startZ, location.minz);
                endZ = Math.max(endZ, location.maxz);
            }
        }

        // Expand by radius + margin
        startX = Math.min(startX - MAP_MARGIN, centre.getX() - radius - MAP_MARGIN);
        startZ = Math.min(startZ - MAP_MARGIN, centre.getZ() - radius - MAP_MARGIN);
        endX = Math.max(endX + MAP_MARGIN, centre.getX() + radius + MAP_MARGIN);
        endZ = Math.max(endZ + MAP_MARGIN, centre.getZ() + radius + MAP_MARGIN);

        // Check if we need to resize
        int chunkStartXTemp = startX >> 4;
        int chunkStartZTemp = startZ >> 4;
        int mapStartXTemp = chunkStartXTemp << 4;
        int mapStartZTemp = chunkStartZTemp << 4;
        int lengthTemp = ((endX >> 4) + 1 << 4) - mapStartXTemp;
        int widthTemp = ((endZ >> 4) + 1 << 4) - mapStartZTemp;

        if (lengthTemp != length || widthTemp != width || topGround == null) {
            // Need to recreate
            createWorldInfo(locations, startX, startZ, endX, endZ);
            return true;
        } else {
            // Just update building locations
            buildingLocations = new ArrayList<>(locations);
            return false;
        }
    }

    /**
     * Convert world coordinates to map array indices.
     * 
     * @return int[2] with {mapX, mapZ}, or null if out of bounds
     */
    public int[] worldToMap(int worldX, int worldZ) {
        int mapX = worldX - mapStartX;
        int mapZ = worldZ - mapStartZ;
        if (mapX < 0 || mapX >= length || mapZ < 0 || mapZ >= width) {
            return null;
        }
        return new int[] { mapX, mapZ };
    }

    /**
     * Check if construction is forbidden at a point.
     */
    public boolean isConstructionForbiddenHere(BlockPos p) {
        int[] coords = worldToMap(p.getX(), p.getZ());
        if (coords == null)
            return false;
        return buildingForbidden[coords[0]][coords[1]];
    }

    /**
     * Get surface height at a world position.
     */
    public int getTopGround(int worldX, int worldZ) {
        int[] coords = worldToMap(worldX, worldZ);
        if (coords == null)
            return yBaseline;
        return topGround[coords[0]][coords[1]];
    }

    /**
     * Check if a position can have a building.
     */
    public boolean canBuildAt(int worldX, int worldZ) {
        int[] coords = worldToMap(worldX, worldZ);
        if (coords == null)
            return false;
        return canBuild[coords[0]][coords[1]];
    }

    /**
     * Get building location at a position.
     */
    public BuildingLocation getBuildingAt(int worldX, int worldZ) {
        int[] coords = worldToMap(worldX, worldZ);
        if (coords == null)
            return null;
        return buildingLocRef[coords[0]][coords[1]];
    }
}
