package org.millenaire.common.pathing.atomicstryker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.util.math.MathHelper;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.ThreadSafeUtilities;
import org.millenaire.common.village.VillageMapInfo;

public class RegionMapper {
   private static final int MIN_SIZE_FOR_REGION_BRIDGING = 200;
   private static final AStarConfig JPS_CONFIG = new AStarConfig(true, false, false, false, true);
   public VillageMapInfo winfo;
   public boolean[][] top;
   public boolean[][] bottom;
   public boolean[][] left;
   public boolean[][] right;
   public short[][] topGround;
   public short[][] regions;
   public short thRegion;
   public List<RegionMapper.Node> nodes;

   private int boolDisplay(boolean a, boolean b, boolean c, boolean d) {
      int i = a ? 1 : 0;
      i += b ? 2 : 0;
      i += c ? 4 : 0;
      return i + (d ? 8 : 0);
   }

   private void buildNodes() {
      for (int i = 0; i < this.winfo.length; i++) {
         for (int j = 0; j < this.winfo.width; j++) {
            boolean isNode = false;
            int cornerSide = 0;
            if (i > 0 && j > 0 && this.top[i][j] && this.left[i][j] && (!this.left[i - 1][j] || !this.top[i][j - 1])) {
               isNode = true;
               cornerSide |= 1;
            }

            if (i < this.winfo.length - 1 && j > 0 && this.bottom[i][j] && this.left[i][j] && (!this.left[i + 1][j] || !this.bottom[i][j - 1])) {
               isNode = true;
               cornerSide += 2;
               cornerSide |= 2;
            }

            if (i > 0 && j < this.winfo.width - 1 && this.top[i][j] && this.right[i][j] && (!this.right[i - 1][j] || !this.top[i][j + 1])) {
               isNode = true;
               cornerSide |= 4;
            }

            if (i < this.winfo.length - 1
               && j < this.winfo.width - 1
               && this.bottom[i][j]
               && this.right[i][j]
               && (!this.right[i + 1][j] || !this.bottom[i][j + 1])) {
               isNode = true;
               cornerSide |= 8;
            }

            if (isNode) {
               this.nodes.add(new RegionMapper.Node(new RegionMapper.Point2D(i, j), this.nodes.size(), cornerSide, false));
            }
         }
      }

      for (RegionMapper.Node n : this.nodes) {
         if (n.cornerSide == 1
            && n.pos.x < this.winfo.length - 1
            && n.pos.z < this.winfo.width - 1
            && this.bottom[n.pos.x][n.pos.z]
            && this.right[n.pos.x][n.pos.z]
            && this.bottom[n.pos.x][n.pos.z + 1]
            && this.right[n.pos.x + 1][n.pos.z]) {
            int tx = n.pos.x + 1;
            int tz = n.pos.z + 1;
            if (tx < this.winfo.length - 1 && tz < this.winfo.width - 1 && this.bottom[tx][tz] && this.right[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }

         if (n.cornerSide == 2
            && n.pos.x > 0
            && n.pos.z < this.winfo.width - 1
            && this.top[n.pos.x][n.pos.z]
            && this.right[n.pos.x][n.pos.z]
            && this.top[n.pos.x][n.pos.z + 1]
            && this.right[n.pos.x - 1][n.pos.z]) {
            int tx = n.pos.x - 1;
            int tz = n.pos.z + 1;
            if (tx > 0 && tz < this.winfo.width - 1 && this.top[tx][tz] && this.right[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }

         if (n.cornerSide == 4
            && n.pos.x < this.winfo.length - 1
            && n.pos.z > 0
            && this.bottom[n.pos.x][n.pos.z]
            && this.left[n.pos.x][n.pos.z]
            && this.bottom[n.pos.x][n.pos.z - 1]
            && this.left[n.pos.x + 1][n.pos.z]) {
            int tx = n.pos.x + 1;
            int tz = n.pos.z - 1;
            if (tx < this.winfo.length - 1 && tz > 0 && this.bottom[tx][tz] && this.left[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }

         if (n.cornerSide == 8
            && n.pos.x > 0
            && n.pos.z > 0
            && this.top[n.pos.x][n.pos.z]
            && this.left[n.pos.x][n.pos.z]
            && this.top[n.pos.x][n.pos.z - 1]
            && this.left[n.pos.x - 1][n.pos.z]) {
            int tx = n.pos.x - 1;
            int tz = n.pos.z - 1;
            if (tx > 0 && tz > 0 && this.top[tx][tz] && this.left[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }

         if (n.cornerSide == 3 && n.pos.z < this.winfo.width - 1 && this.right[n.pos.x][n.pos.z]) {
            int tx = n.pos.x;
            int tz = n.pos.z + 1;
            if (tz < this.winfo.width - 1 && this.bottom[tx][tz] && this.right[tx][tz] && this.top[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }

         if (n.cornerSide == 5 && n.pos.x < this.winfo.length - 1 && this.bottom[n.pos.x][n.pos.z]) {
            int tx = n.pos.x + 1;
            int tz = n.pos.z;
            if (tx < this.winfo.length - 1 && this.bottom[tx][tz] && this.right[tx][tz] && this.left[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }

         if (n.cornerSide == 10 && n.pos.x > 0 && this.top[n.pos.x][n.pos.z]) {
            int tx = n.pos.x - 1;
            int tz = n.pos.z;
            if (tx > 0 && this.top[tx][tz] && this.right[tx][tz] && this.left[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }

         if (n.cornerSide == 12 && n.pos.z > 0 && this.left[n.pos.x][n.pos.z]) {
            int tx = n.pos.x;
            int tz = n.pos.z - 1;
            if (tx > 0 && this.top[tx][tz] && this.bottom[tx][tz] && this.left[tx][tz]) {
               n.pos.x = tx;
               n.pos.z = tz;
            }
         }
      }

      for (int i = this.nodes.size() - 1; i > -1; i--) {
         for (int j = i - 1; j > -1; j--) {
            if (this.nodes.get(i).equals(this.nodes.get(j))) {
               this.nodes.remove(i);
               break;
            }
         }
      }
   }

   public boolean canSee(RegionMapper.Point2D p1, RegionMapper.Point2D p2) {
      int xdist = p2.x - p1.x;
      int zdist = p2.z - p1.z;
      if (xdist == 0 && zdist == 0) {
         return true;
      } else {
         int xsign = 1;
         int zsign = 1;
         if (xdist < 0) {
            xsign = -1;
         }

         if (zdist < 0) {
            zsign = -1;
         }

         int x = p1.x;
         int z = p1.z;
         int xdone = 0;
         int zdone = 0;

         while (x != p2.x || z != p2.z) {
            int nx;
            int nz;
            if (xdist == 0 || zdist != 0 && xdone * 1000 / xdist > zdone * 1000 / zdist) {
               nz = z + zsign;
               nx = x;
               zdone += zsign;
               if (zsign == 1 && !this.right[x][z]) {
                  return false;
               }

               if (zsign == -1 && !this.left[x][z]) {
                  return false;
               }
            } else {
               nx = x + xsign;
               nz = z;
               xdone += xsign;
               if (xsign == 1 && !this.bottom[x][z]) {
                  return false;
               }

               if (xsign == -1 && !this.top[x][z]) {
                  return false;
               }
            }

            x = nx;
            z = nz;
         }

         return true;
      }
   }

   public boolean createConnectionsTable(VillageMapInfo winfo, Point thStanding) throws MillLog.MillenaireException {
      long startTime = System.nanoTime();
      this.winfo = winfo;
      this.top = new boolean[winfo.length][winfo.width];
      this.bottom = new boolean[winfo.length][winfo.width];
      this.left = new boolean[winfo.length][winfo.width];
      this.right = new boolean[winfo.length][winfo.width];
      this.regions = new short[winfo.length][winfo.width];
      this.topGround = VillageMapInfo.shortArrayDeepClone(winfo.topGround);
      this.nodes = new ArrayList<>();

      for (int i = 0; i < winfo.length; i++) {
         for (int j = 0; j < winfo.width; j++) {
            int y = winfo.topGround[i][j];
            int space = winfo.spaceAbove[i][j];
            if (!winfo.danger[i][j] && !winfo.water[i][j] && space > 1) {
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
                     this.top[i][j] = true;
                     this.bottom[i - 1][j] = true;
                  }
               }

               if (j > 0) {
                  int nyx = winfo.topGround[i][j - 1];
                  int nspacex = winfo.spaceAbove[i][j - 1];
                  boolean connectedx = false;
                  if (nyx == y && nspacex > 1) {
                     connectedx = true;
                  } else if (nyx == y - 1 && nspacex > 2) {
                     connectedx = true;
                  } else if (nyx == y + 1 && nspacex > 1 && space > 2) {
                     connectedx = true;
                  }

                  if (connectedx) {
                     this.left[i][j] = true;
                     this.right[i][j - 1] = true;
                  }
               }
            }
         }
      }

      if (MillConfigValues.LogConnections >= 2) {
         MillLog.minor(this, "Time taken for connection building: " + (System.nanoTime() - startTime) / 1000000.0);
      }

      startTime = System.nanoTime();
      this.buildNodes();
      if (MillConfigValues.LogConnections >= 2) {
         MillLog.minor(this, "Time taken for nodes finding: " + (System.nanoTime() - startTime) / 1000000.0);
      }

      startTime = System.nanoTime();

      for (RegionMapper.Node n : this.nodes) {
         for (RegionMapper.Node n2 : this.nodes) {
            if (n.id < n2.id && this.canSee(n.pos, n2.pos)) {
               Integer distance = n.pos.distanceTo(n2.pos);
               n.costs.put(n2, distance);
               n.neighbours.add(n2);
               n2.costs.put(n, distance);
               n2.neighbours.add(n);
            }
         }
      }

      if (MillConfigValues.LogConnections >= 2) {
         MillLog.minor(this, "Time taken for nodes linking: " + (System.nanoTime() - startTime) / 1000000.0);
      }

      startTime = System.nanoTime();
      this.findRegions(thStanding);
      if (MillConfigValues.LogConnections >= 2) {
         MillLog.minor(this, "Time taken for group finding: " + (System.nanoTime() - startTime) / 1000000.0);
      }

      if (MillConfigValues.LogConnections >= 1) {
         MillLog.major(this, "Node graph complete. Size: " + this.nodes.size() + " Time taken: " + (System.nanoTime() - startTime) / 1000000.0);
      }

      if (MillConfigValues.LogConnections >= 3 && MillConfigValues.DEV) {
         MillLog.major(this, "Calling displayConnectionsLog");
         this.displayConnectionsLog();
      }

      return true;
   }

   private void displayConnectionsLog() {
      long startTime = System.nanoTime();
      MillLog.minor(this, "Connections:");
      String s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + MathHelper.floor(j / 10) % 10;
      }

      MillLog.minor(this, s);
      s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + j % 10;
      }

      MillLog.minor(this, s);

      for (int i = 0; i < this.winfo.length; i++) {
         if (i < 10) {
            s = i + "   ";
         } else if (i < 100) {
            s = i + "  ";
         } else {
            s = i + " ";
         }

         for (int j = 0; j < this.winfo.width; j++) {
            s = s + Integer.toHexString(this.boolDisplay(this.top[i][j], this.left[i][j], this.bottom[i][j], this.right[i][j]));
         }

         if (i < 10) {
            s = s + "   " + i;
         } else if (i < 100) {
            s = s + "  " + i;
         } else {
            s = s + " " + i;
         }

         MillLog.minor(this, s);
      }

      MillLog.minor(this, "spaceAbove:");
      s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + MathHelper.floor(j / 10) % 10;
      }

      MillLog.minor(this, s);
      s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + j % 10;
      }

      MillLog.minor(this, s);

      for (int i = 0; i < this.winfo.length; i++) {
         if (i < 10) {
            s = i + "   ";
         } else if (i < 100) {
            s = i + "  ";
         } else {
            s = i + " ";
         }

         for (int j = 0; j < this.winfo.width; j++) {
            s = s + this.winfo.spaceAbove[i][j];
         }

         if (i < 10) {
            s = s + "   " + i;
         } else if (i < 100) {
            s = s + "  " + i;
         } else {
            s = s + " " + i;
         }

         MillLog.minor(this, s);
      }

      MillLog.minor(this, "Y pos:");
      s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + MathHelper.floor(j / 10) % 10;
      }

      MillLog.minor(this, s);
      s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + j % 10;
      }

      MillLog.minor(this, s);

      for (int i = 0; i < this.winfo.length; i++) {
         if (i < 10) {
            s = i + "   ";
         } else if (i < 100) {
            s = i + "  ";
         } else {
            s = i + " ";
         }

         for (int j = 0; j < this.winfo.width; j++) {
            s = s + this.winfo.topGround[i][j] % 10;
         }

         if (i < 10) {
            s = s + "   " + i;
         } else if (i < 100) {
            s = s + "  " + i;
         } else {
            s = s + " " + i;
         }

         MillLog.minor(this, s);
      }

      MillLog.minor(this, "Nodes:");
      s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + MathHelper.floor(j / 10) % 10;
      }

      MillLog.minor(this, s);
      s = "    ";

      for (int j = 0; j < this.winfo.width; j++) {
         s = s + j % 10;
      }

      MillLog.minor(this, s);

      for (int i = 0; i < this.winfo.length; i++) {
         if (i < 10) {
            s = i + "   ";
         } else if (i < 100) {
            s = i + "  ";
         } else {
            s = i + " ";
         }

         for (int j = 0; j < this.winfo.width; j++) {
            boolean found = false;

            for (RegionMapper.Node n : this.nodes) {
               if (n.pos.x == i && n.pos.z == j) {
                  s = s + Integer.toHexString(n.id % 10);
                  found = true;
               }
            }

            if (!found) {
               if (!this.top[i][j] && !this.bottom[i][j] && !this.left[i][j] && !this.right[i][j]) {
                  s = s + "#";
               } else if (this.top[i][j] && this.bottom[i][j] && this.left[i][j] && this.right[i][j]) {
                  s = s + " ";
               } else {
                  s = s + ".";
               }
            }
         }

         if (i < 10) {
            s = s + "   " + i;
         } else if (i < 100) {
            s = s + "  " + i;
         } else {
            s = s + " " + i;
         }

         MillLog.minor(this, s);
      }

      MillLog.minor(this, "Displaying connections finished. Time taken: " + (System.nanoTime() - startTime) / 1000000.0);
   }

   private void findRegions(Point thStanding) throws MillLog.MillenaireException {
      int nodesMarked = 0;
      int nodeGroup = 0;

      while (nodesMarked < this.nodes.size()) {
         nodeGroup++;
         List<RegionMapper.Node> toVisit = new ArrayList<>();
         RegionMapper.Node fn = null;

         for (int i = 0; fn == null; i++) {
            if (this.nodes.get(i).region == 0) {
               fn = this.nodes.get(i);
            }
         }

         fn.region = nodeGroup;
         nodesMarked++;
         toVisit.add(fn);

         while (toVisit.size() > 0) {
            for (RegionMapper.Node n : toVisit.get(0).neighbours) {
               if (n.region == 0) {
                  n.region = nodeGroup;
                  toVisit.add(n);
                  nodesMarked++;
               } else if (n.region != nodeGroup) {
                  throw new MillLog.MillenaireException("Node belongs to group " + n.region + " but reached from " + nodeGroup);
               }
            }

            toVisit.remove(0);
         }
      }

      for (int ix = 0; ix < this.winfo.length; ix++) {
         for (int j = 0; j < this.winfo.width; j++) {
            this.regions[ix][j] = -1;
         }
      }

      for (RegionMapper.Node nx : this.nodes) {
         this.regions[nx.pos.x][nx.pos.z] = (short)nx.region;
      }

      boolean spreaddone = true;

      while (spreaddone) {
         spreaddone = false;

         for (int ix = 0; ix < this.winfo.length; ix++) {
            for (int j = 0; j < this.winfo.width; j++) {
               if (this.regions[ix][j] > 0) {
                  short regionid = this.regions[ix][j];

                  for (int x = ix; x > 1 && this.top[x][j] && this.regions[x - 1][j] == -1; spreaddone = true) {
                     this.regions[--x][j] = regionid;
                  }

                  for (int var27 = ix; var27 < this.winfo.length - 1 && this.bottom[var27][j] && this.regions[var27 + 1][j] == -1; spreaddone = true) {
                     this.regions[++var27][j] = regionid;
                  }

                  for (int var28 = j; var28 > 1 && this.left[ix][var28] && this.regions[ix][var28 - 1] == -1; spreaddone = true) {
                     this.regions[ix][--var28] = regionid;
                  }

                  for (int var29 = j; var29 < this.winfo.width - 1 && this.right[ix][var29] && this.regions[ix][var29 + 1] == -1; spreaddone = true) {
                     this.regions[ix][++var29] = regionid;
                  }
               }
            }
         }
      }

      this.thRegion = this.regions[thStanding.getiX() - this.winfo.mapStartX][thStanding.getiZ() - this.winfo.mapStartZ];
      long startTime = System.nanoTime();
      int maxRegionId = -1;

      for (RegionMapper.Node nx : this.nodes) {
         if (nx.region > maxRegionId) {
            maxRegionId = nx.region;
         }
      }

      int[] regionsSize = new int[maxRegionId + 1];
      RegionMapper.Point2D[] pointsInRegion = new RegionMapper.Point2D[maxRegionId + 1];

      for (int ix = 0; ix <= maxRegionId; ix++) {
         regionsSize[ix] = 0;
      }

      for (int ix = 0; ix < this.winfo.length; ix++) {
         for (int jx = 0; jx < this.winfo.width; jx++) {
            if (this.regions[ix][jx] > -1) {
               regionsSize[this.regions[ix][jx]]++;
            }
         }
      }

      for (RegionMapper.Node nxx : this.nodes) {
         pointsInRegion[nxx.region] = nxx.pos;
      }

      for (int ix = 0; ix <= maxRegionId; ix++) {
         if (regionsSize[ix] > 200 && ix != this.thRegion) {
            try {
               Point targetPoint = new Point(
                  pointsInRegion[ix].x + this.winfo.mapStartX,
                  this.winfo.topGround[pointsInRegion[ix].x][pointsInRegion[ix].z] - 1,
                  pointsInRegion[ix].z + this.winfo.mapStartZ
               );
               ArrayList<AStarNode> path = this.getPath(
                  thStanding.getiX(), thStanding.getiY(), thStanding.getiZ(), targetPoint.getiX(), targetPoint.getiY() + 1, targetPoint.getiZ()
               );
               if (path != null) {
                  for (int x = 0; x < this.winfo.length; x++) {
                     for (int z = 0; z < this.winfo.width; z++) {
                        if (this.regions[x][z] == ix) {
                           this.regions[x][z] = this.thRegion;
                        }
                     }
                  }
               }
            } catch (ThreadSafeUtilities.ChunkAccessException var15) {
               if (MillConfigValues.LogChunkLoader >= 1) {
                  MillLog.major(this, var15.getMessage());
               }
            }
         }
      }

      if (MillConfigValues.LogConnections >= 2) {
         MillLog.minor(this, "Time taken for region bridging: " + (System.nanoTime() - startTime) / 1000000.0);
      }

      if (MillConfigValues.LogConnections >= 2) {
         MillLog.minor(this, nodeGroup + " node groups found.");
      }
   }

   private ArrayList<AStarNode> getPath(int startx, int starty, int startz, int destx, int desty, int destz) throws ThreadSafeUtilities.ChunkAccessException {
      if (!AStarStatic.isViable(this.winfo.world, startx, starty, startz, 0, JPS_CONFIG)) {
         starty--;
      }

      if (!AStarStatic.isViable(this.winfo.world, startx, starty, startz, 0, JPS_CONFIG)) {
         starty += 2;
      }

      if (!AStarStatic.isViable(this.winfo.world, startx, starty, startz, 0, JPS_CONFIG)) {
         starty--;
      }

      AStarNode starter = new AStarNode(startx, starty, startz, 0, null);
      AStarNode finish = new AStarNode(destx, desty, destz, -1, null);
      AStarWorker pathWorker = new AStarWorker();
      pathWorker.setup(this.winfo.world, starter, finish, JPS_CONFIG);
      return pathWorker.runSync();
   }

   public boolean isInArea(Point p) {
      return !(p.x < this.winfo.mapStartX)
         && !(p.x >= this.winfo.mapStartX + this.winfo.length)
         && !(p.z < this.winfo.mapStartZ)
         && !(p.z >= this.winfo.mapStartZ + this.winfo.width);
   }

   public boolean isValidPoint(Point p) {
      return !this.isInArea(p) ? false : this.winfo.spaceAbove[p.getiX() - this.winfo.mapStartX][p.getiZ() - this.winfo.mapStartZ] > 1;
   }

   private static class Node {
      RegionMapper.Point2D pos;
      List<RegionMapper.Node> neighbours;
      HashMap<RegionMapper.Node, Integer> costs;
      int id;
      int fromDist;
      int toDist;
      int cornerSide;
      int region = 0;

      public Node(RegionMapper.Point2D p, int pid, int cornerSide, boolean ptemp) {
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
         } else {
            RegionMapper.Node n = (RegionMapper.Node)obj;
            return n.hashCode() == this.hashCode();
         }
      }

      @Override
      public int hashCode() {
         return this.pos.x + (this.pos.z << 16);
      }

      @Override
      public String toString() {
         return "Node "
            + this.id
            + ": "
            + this.pos
            + " group: "
            + this.region
            + " neighbours: "
            + this.neighbours.size()
            + "(fromDist: "
            + this.fromDist
            + ", toDist: "
            + this.toDist
            + ")";
      }
   }

   public static class Point2D {
      int x;
      int z;

      public Point2D(int px, int pz) {
         this.x = px;
         this.z = pz;
      }

      public int distanceTo(RegionMapper.Point2D p) {
         int d = p.x - this.x;
         int d1 = p.z - this.z;
         return (int)Math.sqrt(d * d + d1 * d1);
      }

      @Override
      public boolean equals(Object obj) {
         if (!(obj instanceof RegionMapper.Point2D)) {
            return false;
         } else {
            RegionMapper.Point2D p = (RegionMapper.Point2D)obj;
            return this.x == p.x && this.z == p.z;
         }
      }

      @Override
      public int hashCode() {
         return this.x << 16 & this.z;
      }

      @Override
      public String toString() {
         return this.x + "/" + this.z;
      }
   }
}
