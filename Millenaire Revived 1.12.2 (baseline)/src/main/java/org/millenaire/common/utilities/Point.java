package org.millenaire.common.utilities;

import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.millenaire.common.entity.TileEntityFirePit;
import org.millenaire.common.entity.TileEntityImportTable;
import org.millenaire.common.entity.TileEntityLockedChest;
import org.millenaire.common.entity.TileEntityPanel;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.pathing.atomicstryker.RegionMapper;

public class Point {
   public final double x;
   public final double y;
   public final double z;

   public static final Point read(NBTTagCompound nbttagcompound, String label) {
      double x = nbttagcompound.getDouble(label + "_xCoord");
      double y = nbttagcompound.getDouble(label + "_yCoord");
      double z = nbttagcompound.getDouble(label + "_zCoord");
      return x == 0.0 && y == 0.0 && z == 0.0 ? null : new Point(x, y, z);
   }

   public Point(AStarNode node) {
      this.x = node.x;
      this.y = node.y;
      this.z = node.z;
   }

   public Point(BlockPos pos) {
      this.x = pos.getX();
      this.y = pos.getY();
      this.z = pos.getZ();
   }

   public Point(double i, double j, double k) {
      this.x = i;
      this.y = j;
      this.z = k;
   }

   public Point(Entity ent) {
      this.x = ent.posX;
      this.y = ent.posY;
      this.z = ent.posZ;
   }

   public Point(PathPoint pp) {
      this.x = pp.x;
      this.y = pp.y;
      this.z = pp.z;
   }

   public Point(String s) {
      String[] scoord = s.split("/");
      this.x = Double.parseDouble(scoord[0]);
      this.y = Double.parseDouble(scoord[1]);
      this.z = Double.parseDouble(scoord[2]);
   }

   public String approximateDistanceLongString(Point p) {
      int dist = (int)this.distanceTo(p);
      if (dist < 950) {
         return dist / 100 * 100 + " " + LanguageUtilities.string("other.metre");
      } else {
         dist = Math.round((float)(dist / 500));
         return dist % 2 == 0
            ? dist / 2 + " " + LanguageUtilities.string("other.kilometre")
            : (dist - 1) / 2 + LanguageUtilities.string("other.andhalf") + " " + LanguageUtilities.string("other.kilometre");
      }
   }

   public String approximateDistanceShortString(Point p) {
      int dist = (int)this.distanceTo(p);
      if (dist < 950) {
         return dist / 100 * 100 + "m";
      } else {
         dist /= 500;
         return dist % 2 == 0 ? dist / 2 + "km" : (dist - 1) / 2 + ".5 km";
      }
   }

   public String directionTo(Point p) {
      return this.directionTo(p, false);
   }

   public String directionTo(Point p, boolean prefixed) {
      String prefix;
      if (prefixed) {
         prefix = "other.tothe";
      } else {
         prefix = "other.";
      }

      int xdist = MathHelper.floor(p.x - this.x);
      int zdist = MathHelper.floor(p.z - this.z);
      String direction;
      if (Math.abs(xdist) > Math.abs(zdist) * 0.6 && Math.abs(xdist) < Math.abs(zdist) * 1.4
         || Math.abs(zdist) > Math.abs(xdist) * 0.6 && Math.abs(zdist) < Math.abs(xdist) * 1.4) {
         if (zdist > 0) {
            direction = prefix + "south-";
         } else {
            direction = prefix + "north-";
         }

         if (xdist > 0) {
            direction = direction + "east";
         } else {
            direction = direction + "west";
         }
      } else if (Math.abs(xdist) > Math.abs(zdist)) {
         if (xdist > 0) {
            direction = prefix + "east";
         } else {
            direction = prefix + "west";
         }
      } else if (zdist > 0) {
         direction = prefix + "south";
      } else {
         direction = prefix + "north";
      }

      return direction;
   }

   public String directionToShort(Point p) {
      int xdist = MathHelper.floor(p.x - this.x);
      int zdist = MathHelper.floor(p.z - this.z);
      String direction;
      if (Math.abs(xdist) > Math.abs(zdist) * 0.6 && Math.abs(xdist) < Math.abs(zdist) * 1.4
         || Math.abs(zdist) > Math.abs(xdist) * 0.6 && Math.abs(zdist) < Math.abs(xdist) * 1.4) {
         if (zdist > 0) {
            direction = LanguageUtilities.string("other.south_short");
         } else {
            direction = LanguageUtilities.string("other.north_short");
         }

         if (xdist > 0) {
            direction = direction + LanguageUtilities.string("other.east_short");
         } else {
            direction = direction + LanguageUtilities.string("other.west_short");
         }
      } else if (Math.abs(xdist) > Math.abs(zdist)) {
         if (xdist > 0) {
            direction = LanguageUtilities.string("other.east_short");
         } else {
            direction = LanguageUtilities.string("other.west_short");
         }
      } else if (zdist > 0) {
         direction = LanguageUtilities.string("other.south_short");
      } else {
         direction = LanguageUtilities.string("other.north_short");
      }

      return direction;
   }

   public String distanceDirectionShort(Point p) {
      return LanguageUtilities.string("other.directionshort", this.directionToShort(p), "" + (int)this.horizontalDistanceTo(p) + "m");
   }

   public double distanceTo(double px, double py, double pz) {
      double d = px - this.x;
      double d1 = py - this.y;
      double d2 = pz - this.z;
      return MathHelper.sqrt(d * d + d1 * d1 + d2 * d2);
   }

   public double distanceTo(Entity e) {
      return this.distanceTo(e.posX, e.posY, e.posZ);
   }

   public double distanceTo(Point p) {
      return p == null ? -1.0 : this.distanceTo(p.x, p.y, p.z);
   }

   public double distanceToSquared(double px, double py, double pz) {
      double d = px - this.x;
      double d1 = py - this.y;
      double d2 = pz - this.z;
      return d * d + d1 * d1 + d2 * d2;
   }

   public double distanceToSquared(Entity e) {
      return this.distanceToSquared(e.posX, e.posY, e.posZ);
   }

   public double distanceToSquared(PathPoint pp) {
      return this.distanceToSquared(pp.x, pp.y, pp.z);
   }

   public double distanceToSquared(Point p) {
      return this.distanceToSquared(p.x, p.y, p.z);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Point)) {
         return false;
      } else {
         Point p = (Point)o;
         return p.x == this.x && p.y == this.y && p.z == this.z;
      }
   }

   public Point getAbove() {
      return new Point(this.x, this.y + 1.0, this.z);
   }

   public Point getBelow() {
      return new Point(this.x, this.y - 1.0, this.z);
   }

   public Block getBlock(World world) {
      return world.getBlockState(this.getBlockPos()).getBlock();
   }

   public IBlockState getBlockActualState(World world) {
      Block block = this.getBlock(world);
      BlockPos pos = this.getBlockPos();
      IBlockState state = world.getBlockState(pos);
      return block.getActualState(state, world, pos);
   }

   public BlockPos getBlockPos() {
      return new BlockPos(this.x, this.y, this.z);
   }

   public TileEntityBrewingStand getBrewingStand(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntityBrewingStand ? (TileEntityBrewingStand)ent : null;
   }

   public String getChunkString() {
      return this.getChunkX() + "/" + this.getChunkZ();
   }

   public int getChunkX() {
      return this.getiX() >> 4;
   }

   public int getChunkZ() {
      return this.getiZ() >> 4;
   }

   public TileEntityDispenser getDispenser(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntityDispenser ? (TileEntityDispenser)ent : null;
   }

   public Point getEast() {
      return new Point(this.x + 1.0, this.y, this.z);
   }

   public TileEntityFirePit getFirePit(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntityFirePit ? (TileEntityFirePit)ent : null;
   }

   public TileEntityFurnace getFurnace(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntityFurnace ? (TileEntityFurnace)ent : null;
   }

   public TileEntityImportTable getImportTable(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntityImportTable ? (TileEntityImportTable)ent : null;
   }

   public IntPoint getIntPoint() {
      return new IntPoint(this);
   }

   public String getIntString() {
      return this.getiX() + "/" + this.getiY() + "/" + this.getiZ();
   }

   public int getiX() {
      return MathHelper.floor(this.x);
   }

   public int getiY() {
      return MathHelper.floor(this.y);
   }

   public int getiZ() {
      return MathHelper.floor(this.z);
   }

   public int getMeta(World world) {
      IBlockState state = world.getBlockState(this.getBlockPos());
      return state.getBlock().getMetaFromState(state);
   }

   public TileEntityLockedChest getMillChest(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntityLockedChest ? (TileEntityLockedChest)ent : null;
   }

   public List<Point> getNeightbours() {
      return Arrays.asList(this.getAbove(), this.getBelow(), this.getNorth(), this.getEast(), this.getSouth(), this.getWest());
   }

   public Point getNorth() {
      return new Point(this.x, this.y, this.z - 1.0);
   }

   public RegionMapper.Point2D getP2D() {
      return new RegionMapper.Point2D(this.getiX(), this.getiZ());
   }

   public TileEntityPanel getPanel(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntityPanel ? (TileEntityPanel)ent : null;
   }

   public PathPoint getPathPoint() {
      return new PathPoint((int)this.x, (int)this.y, (int)this.z);
   }

   public String getPathString() {
      return this.getiX() + "_" + this.getiY() + "_" + this.getiZ();
   }

   public Point getRelative(double dx, double dy, double dz) {
      return new Point(this.x + dx, this.y + dy, this.z + dz);
   }

   public TileEntitySign getSign(World world) {
      TileEntity ent = world.getTileEntity(this.getBlockPos());
      return ent != null && ent instanceof TileEntitySign ? (TileEntitySign)ent : null;
   }

   public Point getSouth() {
      return new Point(this.x, this.y, this.z + 1.0);
   }

   public TileEntity getTileEntity(World world) {
      return world.getTileEntity(this.getBlockPos());
   }

   public Point getWest() {
      return new Point(this.x - 1.0, this.y, this.z);
   }

   @Override
   public int hashCode() {
      return (int)(this.x + ((int)this.y << 8) + ((int)this.z << 16));
   }

   public double horizontalDistanceTo(BlockPos bp) {
      return this.horizontalDistanceTo(bp.getX(), bp.getZ());
   }

   public double horizontalDistanceTo(double px, double pz) {
      double d = px - this.x;
      double d2 = pz - this.z;
      return MathHelper.sqrt(d * d + d2 * d2);
   }

   public double horizontalDistanceTo(Entity e) {
      return this.horizontalDistanceTo(e.posX, e.posZ);
   }

   public double horizontalDistanceTo(PathPoint p) {
      return p == null ? 0.0 : this.horizontalDistanceTo(p.x, p.z);
   }

   public double horizontalDistanceTo(Point p) {
      return p == null ? 0.0 : this.horizontalDistanceTo(p.x, p.z);
   }

   public double horizontalDistanceToSquared(double px, double pz) {
      double d = px - this.x;
      double d2 = pz - this.z;
      return d * d + d2 * d2;
   }

   public double horizontalDistanceToSquared(Entity e) {
      return this.horizontalDistanceToSquared(e.posX, e.posZ);
   }

   public double horizontalDistanceToSquared(Point p) {
      return this.horizontalDistanceToSquared(p.x, p.z);
   }

   public boolean isBlockPassable(World world) {
      return !this.getBlock(world).getDefaultState().getMaterial().isSolid();
   }

   public boolean sameBlock(PathPoint p) {
      return p == null ? false : this.getiX() == p.x && this.getiY() == p.y && this.getiZ() == p.z;
   }

   public boolean sameBlock(Point p) {
      return p == null ? false : this.getiX() == p.getiX() && this.getiY() == p.getiY() && this.getiZ() == p.getiZ();
   }

   public void setBlock(World world, Block block, int meta, boolean notify, boolean sound) {
      WorldUtilities.setBlockAndMetadata(world, this, block, meta, notify, sound);
   }

   public void setBlockState(World world, IBlockState state) {
      world.setBlockState(this.getBlockPos(), state);
   }

   public int squareRadiusDistance(Point p) {
      return (int)Math.max(Math.abs(this.x - p.x), Math.abs(this.z - p.z));
   }

   @Override
   public String toString() {
      return Math.round(this.x * 100.0) / 100L + "/" + Math.round(this.y * 100.0) / 100L + "/" + Math.round(this.z * 100.0) / 100L;
   }

   public void write(NBTTagCompound nbttagcompound, String label) {
      nbttagcompound.setDouble(label + "_xCoord", this.x);
      nbttagcompound.setDouble(label + "_yCoord", this.y);
      nbttagcompound.setDouble(label + "_zCoord", this.z);
   }
}
