package org.millenaire.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.millenaire.common.village.VillageMapInfo;

/**
 * Region mapper for determining reachability from the town hall.
 * Full port from original Millénaire RegionMapper.
 * 
 * Uses visibility graph with corner nodes to determine connected regions.
 * Buildings should only be placed in cells reachable from the town hall.
 */
public class RegionMapper {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int MIN_SIZE_FOR_REGION_BRIDGING = 200;

    // Reference to the village map info
    public VillageMapInfo winfo;

    // Connection flags for each cell - indicates if you can walk in that direction
    public boolean[][] top; // Can move to x-1 (north)
    public boolean[][] bottom; // Can move to x+1 (south)
    public boolean[][] left; // Can move to z-1 (west)
    public boolean[][] right; // Can move to z+1 (east)

    // Ground height copy for pathing
    public short[][] topGround;

    // Region IDs for each cell
    public short[][] regions;

    // The region ID that contains the town hall
    public short thRegion;

    // Visibility graph nodes
    public List<Node> nodes;

    /**
     * 2D point for node positions.
     */
    public static class Point2D {
        public int x;
        public int z;

        public Point2D(int px, int pz) {
            this.x = px;
            this.z = pz;
        }

        public int distanceTo(Point2D p) {
            int d = p.x - this.x;
            int d1 = p.z - this.z;
            return (int) Math.sqrt(d * d + d1 * d1);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point2D)) {
                return false;
            }
            Point2D p = (Point2D) obj;
            return this.x == p.x && this.z == p.z;
        }

        @Override
        public int hashCode() {
            return (this.x << 16) ^ this.z;
        }

        @Override
        public String toString() {
            return this.x + "/" + this.z;
        }
    }

    /**
     * Visibility graph node at a corner.
     */
    private static class Node {
        Point2D pos;
        List<Node> neighbours;
        HashMap<Node, Integer> costs;
        int id;
        int cornerSide;
        int region = 0;

        public Node(Point2D p, int pid, int cornerSide) {
            this.pos = p;
            this.id = pid;
            this.cornerSide = cornerSide;
            this.neighbours = new ArrayList<>();
            this.costs = new HashMap<>();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            Node n = (Node) obj;
            return n.hashCode() == this.hashCode();
        }

        @Override
        public int hashCode() {
            return this.pos.x + (this.pos.z << 16);
        }

        @Override
        public String toString() {
            return "Node " + this.id + ": " + this.pos + " group: " + this.region +
                    " neighbours: " + this.neighbours.size();
        }
    }

    /**
     * Convert 4 booleans to a number for display.
     */
    private int boolDisplay(boolean a, boolean b, boolean c, boolean d) {
        int i = a ? 1 : 0;
        i += b ? 2 : 0;
        i += c ? 4 : 0;
        return i + (d ? 8 : 0);
    }

    /**
     * Build visibility graph nodes at corners.
     * Ported from original Millénaire.
     */
    private void buildNodes() {
        for (int i = 0; i < winfo.length; i++) {
            for (int j = 0; j < winfo.width; j++) {
                boolean isNode = false;
                int cornerSide = 0;

                // Check for corner at top-left
                if (i > 0 && j > 0 && top[i][j] && left[i][j] &&
                        (!left[i - 1][j] || !top[i][j - 1])) {
                    isNode = true;
                    cornerSide |= 1;
                }

                // Check for corner at bottom-left
                if (i < winfo.length - 1 && j > 0 && bottom[i][j] && left[i][j] &&
                        (!left[i + 1][j] || !bottom[i][j - 1])) {
                    isNode = true;
                    cornerSide |= 2;
                }

                // Check for corner at top-right
                if (i > 0 && j < winfo.width - 1 && top[i][j] && right[i][j] &&
                        (!right[i - 1][j] || !top[i][j + 1])) {
                    isNode = true;
                    cornerSide |= 4;
                }

                // Check for corner at bottom-right
                if (i < winfo.length - 1 && j < winfo.width - 1 && bottom[i][j] && right[i][j] &&
                        (!right[i + 1][j] || !bottom[i][j + 1])) {
                    isNode = true;
                    cornerSide |= 8;
                }

                if (isNode) {
                    nodes.add(new Node(new Point2D(i, j), nodes.size(), cornerSide));
                }
            }
        }

        // Adjust node positions to move away from obstacles (original logic)
        for (Node n : nodes) {
            if (n.cornerSide == 1 && n.pos.x < winfo.length - 1 && n.pos.z < winfo.width - 1 &&
                    bottom[n.pos.x][n.pos.z] && right[n.pos.x][n.pos.z] &&
                    bottom[n.pos.x][n.pos.z + 1] && right[n.pos.x + 1][n.pos.z]) {
                int tx = n.pos.x + 1;
                int tz = n.pos.z + 1;
                if (tx < winfo.length - 1 && tz < winfo.width - 1 && bottom[tx][tz] && right[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }

            if (n.cornerSide == 2 && n.pos.x > 0 && n.pos.z < winfo.width - 1 &&
                    top[n.pos.x][n.pos.z] && right[n.pos.x][n.pos.z] &&
                    top[n.pos.x][n.pos.z + 1] && right[n.pos.x - 1][n.pos.z]) {
                int tx = n.pos.x - 1;
                int tz = n.pos.z + 1;
                if (tx > 0 && tz < winfo.width - 1 && top[tx][tz] && right[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }

            if (n.cornerSide == 4 && n.pos.x < winfo.length - 1 && n.pos.z > 0 &&
                    bottom[n.pos.x][n.pos.z] && left[n.pos.x][n.pos.z] &&
                    bottom[n.pos.x][n.pos.z - 1] && left[n.pos.x + 1][n.pos.z]) {
                int tx = n.pos.x + 1;
                int tz = n.pos.z - 1;
                if (tx < winfo.length - 1 && tz > 0 && bottom[tx][tz] && left[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }

            if (n.cornerSide == 8 && n.pos.x > 0 && n.pos.z > 0 &&
                    top[n.pos.x][n.pos.z] && left[n.pos.x][n.pos.z] &&
                    top[n.pos.x][n.pos.z - 1] && left[n.pos.x - 1][n.pos.z]) {
                int tx = n.pos.x - 1;
                int tz = n.pos.z - 1;
                if (tx > 0 && tz > 0 && top[tx][tz] && left[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }

            if (n.cornerSide == 3 && n.pos.z < winfo.width - 1 && right[n.pos.x][n.pos.z]) {
                int tx = n.pos.x;
                int tz = n.pos.z + 1;
                if (tz < winfo.width - 1 && bottom[tx][tz] && right[tx][tz] && top[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }

            if (n.cornerSide == 5 && n.pos.x < winfo.length - 1 && bottom[n.pos.x][n.pos.z]) {
                int tx = n.pos.x + 1;
                int tz = n.pos.z;
                if (tx < winfo.length - 1 && bottom[tx][tz] && right[tx][tz] && left[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }

            if (n.cornerSide == 10 && n.pos.x > 0 && top[n.pos.x][n.pos.z]) {
                int tx = n.pos.x - 1;
                int tz = n.pos.z;
                if (tx > 0 && top[tx][tz] && right[tx][tz] && left[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }

            if (n.cornerSide == 12 && n.pos.z > 0 && left[n.pos.x][n.pos.z]) {
                int tx = n.pos.x;
                int tz = n.pos.z - 1;
                if (tx > 0 && top[tx][tz] && bottom[tx][tz] && left[tx][tz]) {
                    n.pos.x = tx;
                    n.pos.z = tz;
                }
            }
        }

        // Remove duplicate nodes
        for (int i = nodes.size() - 1; i > -1; i--) {
            for (int j = i - 1; j > -1; j--) {
                if (nodes.get(i).equals(nodes.get(j))) {
                    nodes.remove(i);
                    break;
                }
            }
        }
    }

    /**
     * Check if two points can see each other (no obstacles between).
     * Ported from original Millénaire.
     */
    public boolean canSee(Point2D p1, Point2D p2) {
        int xdist = p2.x - p1.x;
        int zdist = p2.z - p1.z;

        if (xdist == 0 && zdist == 0) {
            return true;
        }

        int xsign = (xdist < 0) ? -1 : 1;
        int zsign = (zdist < 0) ? -1 : 1;

        int x = p1.x;
        int z = p1.z;
        int xdone = 0;
        int zdone = 0;

        while (x != p2.x || z != p2.z) {
            int nx, nz;

            if (xdist == 0 || (zdist != 0 && Math.abs(xdone * 1000 / xdist) > Math.abs(zdone * 1000 / zdist))) {
                nz = z + zsign;
                nx = x;
                zdone += zsign;

                if (zsign == 1 && !right[x][z]) {
                    return false;
                }
                if (zsign == -1 && !left[x][z]) {
                    return false;
                }
            } else {
                nx = x + xsign;
                nz = z;
                xdone += xsign;

                if (xsign == 1 && !bottom[x][z]) {
                    return false;
                }
                if (xsign == -1 && !top[x][z]) {
                    return false;
                }
            }

            x = nx;
            z = nz;
        }

        return true;
    }

    /**
     * Create the connections table by analyzing terrain walkability.
     * Full port from original Millénaire.
     * 
     * @param winfo Village map info with terrain data
     * @param thPos Town hall position
     * @return true if successful
     */
    public boolean createConnectionsTable(VillageMapInfo winfo, BlockPos thPos) {
        long startTime = System.nanoTime();
        this.winfo = winfo;

        // Initialize arrays
        top = new boolean[winfo.length][winfo.width];
        bottom = new boolean[winfo.length][winfo.width];
        left = new boolean[winfo.length][winfo.width];
        right = new boolean[winfo.length][winfo.width];
        regions = new short[winfo.length][winfo.width];
        topGround = shortArrayDeepClone(winfo.topGround);
        nodes = new ArrayList<>();

        // Build connection flags based on terrain walkability
        for (int i = 0; i < winfo.length; i++) {
            for (int j = 0; j < winfo.width; j++) {
                int y = winfo.topGround[i][j];
                int space = winfo.spaceAbove[i][j];

                if (!winfo.danger[i][j] && !winfo.water[i][j] && space > 1) {
                    // Check connection to cell at x-1
                    if (i > 0) {
                        int ny = winfo.topGround[i - 1][j];
                        int nspace = winfo.spaceAbove[i - 1][j];
                        boolean connected = false;

                        if (ny == y && nspace > 1) {
                            connected = true;
                        } else if (ny == y - 1 && nspace > 2) {
                            connected = true;
                        } else if (ny == y + 1 && nspace > 1 && space > 2) {
                            connected = true;
                        }

                        if (connected) {
                            top[i][j] = true;
                            bottom[i - 1][j] = true;
                        }
                    }

                    // Check connection to cell at z-1
                    if (j > 0) {
                        int ny = winfo.topGround[i][j - 1];
                        int nspace = winfo.spaceAbove[i][j - 1];
                        boolean connected = false;

                        if (ny == y && nspace > 1) {
                            connected = true;
                        } else if (ny == y - 1 && nspace > 2) {
                            connected = true;
                        } else if (ny == y + 1 && nspace > 1 && space > 2) {
                            connected = true;
                        }

                        if (connected) {
                            left[i][j] = true;
                            right[i][j - 1] = true;
                        }
                    }
                }
            }
        }

        LOGGER.debug("[REGION] Connection building: {}ms", (System.nanoTime() - startTime) / 1_000_000.0);

        // Build visibility graph nodes
        startTime = System.nanoTime();
        buildNodes();
        LOGGER.debug("[REGION] Node finding: {}ms, {} nodes", (System.nanoTime() - startTime) / 1_000_000.0,
                nodes.size());

        // Link nodes that can see each other
        startTime = System.nanoTime();
        for (Node n : nodes) {
            for (Node n2 : nodes) {
                if (n.id < n2.id && canSee(n.pos, n2.pos)) {
                    Integer distance = n.pos.distanceTo(n2.pos);
                    n.costs.put(n2, distance);
                    n.neighbours.add(n2);
                    n2.costs.put(n, distance);
                    n2.neighbours.add(n);
                }
            }
        }
        LOGGER.debug("[REGION] Node linking: {}ms", (System.nanoTime() - startTime) / 1_000_000.0);

        // Find regions
        startTime = System.nanoTime();
        findRegions(thPos);
        LOGGER.debug("[REGION] Group finding: {}ms", (System.nanoTime() - startTime) / 1_000_000.0);

        LOGGER.info("[REGION] Complete. {} nodes, TH region: {}", nodes.size(), thRegion);
        return true;
    }

    /**
     * Deep clone a short array.
     */
    public static short[][] shortArrayDeepClone(short[][] source) {
        short[][] target = new short[source.length][];
        for (int i = 0; i < source.length; i++) {
            target[i] = source[i].clone();
        }
        return target;
    }

    /**
     * Find regions using flood-fill from nodes.
     * Ported from original Millénaire.
     * 
     * BUGFIX: When there are no corner nodes (flat terrain), we flood-fill
     * the entire walkable area as region 1 starting from the town hall.
     */
    private void findRegions(BlockPos thPos) {
        int thMapX = thPos.getX() - winfo.mapStartX;
        int thMapZ = thPos.getZ() - winfo.mapStartZ;

        // Initialize regions array to -1 (unassigned)
        for (int i = 0; i < winfo.length; i++) {
            for (int j = 0; j < winfo.width; j++) {
                regions[i][j] = -1;
            }
        }

        // BUGFIX: If no nodes exist (flat terrain), flood-fill walkable area from TH
        if (nodes.isEmpty()) {
            LOGGER.info("[REGION] No corner nodes found (flat terrain), using flood-fill from TH");
            thRegion = 1;

            if (thMapX >= 0 && thMapX < winfo.length && thMapZ >= 0 && thMapZ < winfo.width) {
                floodFillRegion(thMapX, thMapZ, (short) 1);
            } else {
                LOGGER.warn("[REGION] Town hall outside map bounds, marking all walkable as region 1");
                // Mark all walkable cells as region 1
                for (int i = 0; i < winfo.length; i++) {
                    for (int j = 0; j < winfo.width; j++) {
                        if (!winfo.danger[i][j] && !winfo.water[i][j] && winfo.spaceAbove[i][j] > 1) {
                            regions[i][j] = 1;
                        }
                    }
                }
            }

            LOGGER.debug("[REGION] Flood-fill complete, TH in region {}", thRegion);
            return;
        }

        int nodesMarked = 0;
        int nodeGroup = 0;

        // Mark nodes with region IDs
        while (nodesMarked < nodes.size()) {
            nodeGroup++;
            List<Node> toVisit = new ArrayList<>();
            Node fn = null;

            // Find first unmarked node
            for (int i = 0; fn == null && i < nodes.size(); i++) {
                if (nodes.get(i).region == 0) {
                    fn = nodes.get(i);
                }
            }

            if (fn == null)
                break;

            fn.region = nodeGroup;
            nodesMarked++;
            toVisit.add(fn);

            // Flood fill through node neighbours
            while (!toVisit.isEmpty()) {
                Node current = toVisit.remove(0);
                for (Node n : current.neighbours) {
                    if (n.region == 0) {
                        n.region = nodeGroup;
                        toVisit.add(n);
                        nodesMarked++;
                    }
                }
            }
        }

        // Mark node positions with their region
        for (Node n : nodes) {
            regions[n.pos.x][n.pos.z] = (short) n.region;
        }

        // Spread regions to connected cells
        boolean spreadDone = true;
        while (spreadDone) {
            spreadDone = false;

            for (int i = 0; i < winfo.length; i++) {
                for (int j = 0; j < winfo.width; j++) {
                    if (regions[i][j] > 0) {
                        short regionId = regions[i][j];

                        // Spread north
                        for (int x = i; x > 1 && top[x][j] && regions[x - 1][j] == -1;) {
                            regions[--x][j] = regionId;
                            spreadDone = true;
                        }

                        // Spread south
                        for (int x = i; x < winfo.length - 1 && bottom[x][j] && regions[x + 1][j] == -1;) {
                            regions[++x][j] = regionId;
                            spreadDone = true;
                        }

                        // Spread west
                        for (int z = j; z > 1 && left[i][z] && regions[i][z - 1] == -1;) {
                            regions[i][--z] = regionId;
                            spreadDone = true;
                        }

                        // Spread east
                        for (int z = j; z < winfo.width - 1 && right[i][z] && regions[i][z + 1] == -1;) {
                            regions[i][++z] = regionId;
                            spreadDone = true;
                        }
                    }
                }
            }
        }

        // Get town hall's region
        if (thMapX >= 0 && thMapX < winfo.length && thMapZ >= 0 && thMapZ < winfo.width) {
            thRegion = regions[thMapX][thMapZ];
            if (thRegion <= 0) {
                // Try to find nearest region
                thRegion = findNearestRegion(thMapX, thMapZ);
            }
        } else {
            LOGGER.warn("[REGION] Town hall outside map bounds");
            thRegion = 1;
        }

        LOGGER.debug("[REGION] Found {} groups, TH in region {}", nodeGroup, thRegion);
    }

    /**
     * Flood-fill a region from a starting point using walkability connections.
     * Used when no corner nodes exist (flat terrain).
     */
    private void floodFillRegion(int startX, int startZ, short regionId) {
        List<int[]> toVisit = new ArrayList<>();
        toVisit.add(new int[] { startX, startZ });
        regions[startX][startZ] = regionId;

        while (!toVisit.isEmpty()) {
            int[] current = toVisit.remove(0);
            int x = current[0];
            int z = current[1];

            // Check north (x-1)
            if (x > 0 && top[x][z] && regions[x - 1][z] == -1) {
                regions[x - 1][z] = regionId;
                toVisit.add(new int[] { x - 1, z });
            }

            // Check south (x+1)
            if (x < winfo.length - 1 && bottom[x][z] && regions[x + 1][z] == -1) {
                regions[x + 1][z] = regionId;
                toVisit.add(new int[] { x + 1, z });
            }

            // Check west (z-1)
            if (z > 0 && left[x][z] && regions[x][z - 1] == -1) {
                regions[x][z - 1] = regionId;
                toVisit.add(new int[] { x, z - 1 });
            }

            // Check east (z+1)
            if (z < winfo.width - 1 && right[x][z] && regions[x][z + 1] == -1) {
                regions[x][z + 1] = regionId;
                toVisit.add(new int[] { x, z + 1 });
            }
        }
    }

    /**
     * Find the nearest valid region to a point.
     */
    private short findNearestRegion(int x, int z) {
        for (int r = 1; r <= 20; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int nx = x + dx;
                    int nz = z + dz;
                    if (nx >= 0 && nx < winfo.length && nz >= 0 && nz < winfo.width) {
                        if (regions[nx][nz] > 0) {
                            return regions[nx][nz];
                        }
                    }
                }
            }
        }
        return 1;
    }

    /**
     * Check if a world position is in the town hall's reachable region.
     */
    public boolean isInTHRegion(int worldX, int worldZ) {
        int mapX = worldX - winfo.mapStartX;
        int mapZ = worldZ - winfo.mapStartZ;

        if (mapX < 0 || mapX >= winfo.length || mapZ < 0 || mapZ >= winfo.width) {
            return false;
        }

        return regions[mapX][mapZ] == thRegion;
    }

    /**
     * Check if a world position is walkable.
     */
    public boolean isWalkable(int worldX, int worldZ) {
        int mapX = worldX - winfo.mapStartX;
        int mapZ = worldZ - winfo.mapStartZ;

        if (mapX < 0 || mapX >= winfo.length || mapZ < 0 || mapZ >= winfo.width) {
            return false;
        }

        return regions[mapX][mapZ] > 0;
    }

    /**
     * Get region ID at world position.
     */
    public short getRegion(int worldX, int worldZ) {
        int mapX = worldX - winfo.mapStartX;
        int mapZ = worldZ - winfo.mapStartZ;

        if (mapX < 0 || mapX >= winfo.length || mapZ < 0 || mapZ >= winfo.width) {
            return -1;
        }

        return regions[mapX][mapZ];
    }

    /**
     * Check if a point is within the mapped area.
     */
    public boolean isInArea(BlockPos p) {
        return p.getX() >= winfo.mapStartX &&
                p.getX() < winfo.mapStartX + winfo.length &&
                p.getZ() >= winfo.mapStartZ &&
                p.getZ() < winfo.mapStartZ + winfo.width;
    }
}
