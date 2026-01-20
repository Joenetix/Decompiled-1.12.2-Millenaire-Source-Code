package org.millenaire.common.utilities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.pathing.atomicstryker.RegionMapper;

public class Point {
    // 1.20.1: Entity fields are x, y, z (accessed via getters or specific fields
    // depending on mapping)
    // Standard mappings: getX(), getY(), getZ() usually round.
    // Entity.position().x, etc.

    public final double x;
    public final double y;
    public final double z;

    public static final Point read(CompoundTag nbttagcompound, String label) {
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
        this.x = ent.getX();
        this.y = ent.getY();
        this.z = ent.getZ();
    }

    public Point(Node pp) {
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

    // ... Distance methods adapted for Mth

    public double distanceTo(double px, double py, double pz) {
        double d = px - this.x;
        double d1 = py - this.y;
        double d2 = pz - this.z;
        return Mth.sqrt((float) (d * d + d1 * d1 + d2 * d2));
    }

    public double distanceTo(Point p) {
        return p == null ? -1.0 : this.distanceTo(p.x, p.y, p.z);
    }

    // Simpler distanceToSquared
    public double distanceToSquared(double px, double py, double pz) {
        double d = px - this.x;
        double d1 = py - this.y;
        double d2 = pz - this.z;
        return d * d + d1 * d1 + d2 * d2;
    }

    public double distanceToSquared(Point p) {
        return this.distanceToSquared(p.x, p.y, p.z);
    }

    public int getiX() {
        return Mth.floor(this.x);
    }

    public int getiY() {
        return Mth.floor(this.y);
    }

    public int getiZ() {
        return Mth.floor(this.z);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(getiX(), getiY(), getiZ());
    }

    public Block getBlock(Level world) {
        return world.getBlockState(getBlockPos()).getBlock();
    }

    public RegionMapper.Point2D getP2D() {
        return new RegionMapper.Point2D(this.getiX(), this.getiZ());
    }

    /**
     * Convert this Point to an IntPoint (floored integer coordinates).
     */
    public IntPoint getIntPoint() {
        return new IntPoint(this.getiX(), this.getiY(), this.getiZ());
    }

    // Stub methods for strings using LanguageUtilities
    public String approximateDistanceLongString(Point p) {
        int dist = (int) this.distanceTo(p);
        return dist + "m";
    }

    public String approximateDistanceShortString(Point p) {
        int dist = (int) this.distanceTo(p);
        return dist + "m";
    }

    @Override
    public String toString() {
        return Math.round(this.x * 100.0) / 100L + "/" + Math.round(this.y * 100.0) / 100L + "/"
                + Math.round(this.z * 100.0) / 100L;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Point)) {
            return false;
        } else {
            Point p = (Point) o;
            return p.x == this.x && p.y == this.y && p.z == this.z;
        }
    }

    @Override
    public int hashCode() {
        return (int) (this.x + ((int) this.y << 8) + ((int) this.z << 16));
    }

    /**
     * Get a point relative to this one by the given offsets.
     */
    public Point getRelative(double dx, double dy, double dz) {
        return new Point(this.x + dx, this.y + dy, this.z + dz);
    }

    /**
     * Get a point relative to this one by the given integer offsets.
     */
    public Point getRelative(int dx, int dy, int dz) {
        return new Point(this.x + dx, this.y + dy, this.z + dz);
    }

    /**
     * Get the point above this one.
     */
    public Point getAbove() {
        return new Point(this.x, this.y + 1, this.z);
    }

    /**
     * Get the point below this one.
     */
    public Point getBelow() {
        return new Point(this.x, this.y - 1, this.z);
    }

    /**
     * Get point to the north (-Z).
     */
    public Point getNorth() {
        return new Point(this.x, this.y, this.z - 1);
    }

    /**
     * Get point to the south (+Z).
     */
    public Point getSouth() {
        return new Point(this.x, this.y, this.z + 1);
    }

    /**
     * Get point to the east (+X).
     */
    public Point getEast() {
        return new Point(this.x + 1, this.y, this.z);
    }

    /**
     * Get point to the west (-X).
     */
    public Point getWest() {
        return new Point(this.x - 1, this.y, this.z);
    }

    /**
     * Get point in a direction.
     */
    public Point getRelative(net.minecraft.core.Direction direction) {
        return new Point(
                this.x + direction.getStepX(),
                this.y + direction.getStepY(),
                this.z + direction.getStepZ());
    }

    /**
     * Get the block entity at this position.
     */
    public net.minecraft.world.level.block.entity.BlockEntity getTileEntity(Level world) {
        return world.getBlockEntity(getBlockPos());
    }

    public void write(CompoundTag nbttagcompound, String label) {
        nbttagcompound.putDouble(label + "_xCoord", this.x);
        nbttagcompound.putDouble(label + "_yCoord", this.y);
        nbttagcompound.putDouble(label + "_zCoord", this.z);
    }

    // Additional Helper methods found in original if needed?
    // horizontalDistanceTo, getNorth, etc?
    // I'll add them if RegionMapper needs them. RegionMapper uses Point
    // extensively.
    // RegionMapper mainly uses Point for conversions and distance check.
    // It manually calculates some distances or uses Point.distanceTo.

    /**
     * Calculate horizontal distance (ignoring Y) to another point.
     */
    public double horizontalDistanceTo(Point p) {
        if (p == null)
            return -1.0;
        double dx = p.x - this.x;
        double dz = p.z - this.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Calculate horizontal distance (ignoring Y) to a BlockPos.
     */
    public double horizontalDistanceTo(BlockPos pos) {
        if (pos == null)
            return -1.0;
        double dx = pos.getX() - this.x;
        double dz = pos.getZ() - this.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Calculate squared horizontal distance (ignoring Y) to a BlockPos.
     * This is the "square radius" distance used for village boundary checks.
     */
    public double squareRadiusDistance(BlockPos pos) {
        if (pos == null)
            return -1.0;
        double dx = pos.getX() - this.x;
        double dz = pos.getZ() - this.z;
        return Math.max(Math.abs(dx), Math.abs(dz));
    }

    /**
     * Calculate squared horizontal distance (ignoring Y) to a Point.
     */
    public double squareRadiusDistance(Point p) {
        if (p == null)
            return -1.0;
        double dx = p.x - this.x;
        double dz = p.z - this.z;
        return Math.max(Math.abs(dx), Math.abs(dz));
    }

    /**
     * Get chunk X coordinate.
     */
    public int getChunkX() {
        return getiX() >> 4;
    }

    /**
     * Get chunk Z coordinate.
     */
    public int getChunkZ() {
        return getiZ() >> 4;
    }

    /**
     * Get direction string to another point.
     */
    public String directionTo(Point p, boolean useCardinals) {
        if (p == null)
            return "";
        double dx = p.x - this.x;
        double dz = p.z - this.z;
        double angle = Math.atan2(dz, dx) * 180 / Math.PI;

        if (useCardinals) {
            if (angle >= -22.5 && angle < 22.5)
                return "E";
            if (angle >= 22.5 && angle < 67.5)
                return "SE";
            if (angle >= 67.5 && angle < 112.5)
                return "S";
            if (angle >= 112.5 && angle < 157.5)
                return "SW";
            if (angle >= 157.5 || angle < -157.5)
                return "W";
            if (angle >= -157.5 && angle < -112.5)
                return "NW";
            if (angle >= -112.5 && angle < -67.5)
                return "N";
            return "NE";
        }
        return String.format("%.0f°", angle);
    }
}
