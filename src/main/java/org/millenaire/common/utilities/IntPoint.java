package org.millenaire.common.utilities;

import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Immutable integer 3D point with neighbor and distance utilities.
 * Exact port from 1.12.2, adapted for 1.20.1 APIs.
 */
public class IntPoint implements Comparable<IntPoint> {
    final int x;
    final int y;
    final int z;

    public IntPoint(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public IntPoint(Point p) {
        this.x = p.getiX();
        this.y = p.getiY();
        this.z = p.getiZ();
    }

    @Override
    public int compareTo(IntPoint p) {
        return p.hashCode() - this.hashCode();
    }

    public int distanceToSquared(int px, int py, int pz) {
        int d = px - this.x;
        int d1 = py - this.y;
        int d2 = pz - this.z;
        return d * d + d1 * d1 + d2 * d2;
    }

    public int distanceToSquared(IntPoint p) {
        return this.distanceToSquared(p.x, p.y, p.z);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof IntPoint) {
            IntPoint p = (IntPoint) o;
            return this.x == p.x && this.y == p.y && this.z == p.z;
        } else {
            return false;
        }
    }

    public IntPoint getAbove() {
        return new IntPoint(this.x, this.y + 1, this.z);
    }

    public List<IntPoint> getAllNeightbours() {
        return Arrays.asList(
                this.getAbove(),
                this.getBelow(),
                this.getNorth(),
                this.getEast(),
                this.getSouth(),
                this.getWest(),
                this.getRelative(1, 1, 0),
                this.getRelative(1, -1, 0),
                this.getRelative(-1, 1, 0),
                this.getRelative(-1, -1, 0),
                this.getRelative(1, 0, 1),
                this.getRelative(1, 0, -1),
                this.getRelative(-1, 0, 1),
                this.getRelative(-1, 0, -1),
                this.getRelative(0, 1, 1),
                this.getRelative(0, -1, 1),
                this.getRelative(0, 1, -1),
                this.getRelative(0, -1, -1),
                this.getRelative(1, 1, 1),
                this.getRelative(1, 1, -1),
                this.getRelative(1, -1, 1),
                this.getRelative(1, -1, -1),
                this.getRelative(-1, 1, 1),
                this.getRelative(-1, 1, -1),
                this.getRelative(-1, -1, 1),
                this.getRelative(-1, -1, -1));
    }

    public IntPoint getBelow() {
        return new IntPoint(this.x, this.y - 1, this.z);
    }

    public Block getBlock(Level level) {
        return level.getBlockState(this.getBlockPos()).getBlock();
    }

    public BlockPos getBlockPos() {
        return new BlockPos(this.x, this.y, this.z);
    }

    public IntPoint getEast() {
        return new IntPoint(this.x + 1, this.y, this.z);
    }

    public List<IntPoint> getNeightboursWithDiagonals() {
        return Arrays.asList(
                this.getAbove(),
                this.getBelow(),
                this.getNorth(),
                this.getEast(),
                this.getSouth(),
                this.getWest(),
                this.getRelative(1, 1, 0),
                this.getRelative(1, -1, 0),
                this.getRelative(-1, 1, 0),
                this.getRelative(-1, -1, 0),
                this.getRelative(1, 0, 1),
                this.getRelative(1, 0, -1),
                this.getRelative(-1, 0, 1),
                this.getRelative(-1, 0, -1),
                this.getRelative(0, 1, 0),
                this.getRelative(0, -1, 1),
                this.getRelative(0, 1, -1),
                this.getRelative(0, -1, -1));
    }

    public IntPoint getNorth() {
        return new IntPoint(this.x, this.y, this.z - 1);
    }

    public Point getPoint() {
        return new Point(this.x, this.y, this.z);
    }

    public IntPoint getRelative(int dx, int dy, int dz) {
        return new IntPoint(this.x + dx, this.y + dy, this.z + dz);
    }

    public IntPoint getSouth() {
        return new IntPoint(this.x, this.y, this.z + 1);
    }

    public IntPoint getWest() {
        return new IntPoint(this.x - 1, this.y, this.z);
    }

    @Override
    public int hashCode() {
        return this.x + (this.y << 8) + (this.z << 16);
    }

    public int horizontalDistanceToSquared(int px, int pz) {
        int d = px - this.x;
        int d2 = pz - this.z;
        return d * d + d2 * d2;
    }

    public int horizontalDistanceToSquared(IntPoint p) {
        return this.horizontalDistanceToSquared(p.x, p.z);
    }

    public void setBlockState(Level level, BlockState state) {
        level.setBlockAndUpdate(this.getBlockPos(), state);
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }
}
