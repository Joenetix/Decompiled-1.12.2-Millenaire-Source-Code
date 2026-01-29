package org.millenaire.common.pathing.atomicstryker;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.Vec3d;

public class AS_PathEntity extends Path {
   private long timeLastPathIncrement = 0L;
   public final PathPoint[] pointsCopy;
   private int pathIndexCopy;

   public AS_PathEntity(PathPoint[] points) {
      super(points);
      this.timeLastPathIncrement = System.currentTimeMillis();
      this.pointsCopy = points;
      this.pathIndexCopy = 0;
   }

   public void advancePathIndex() {
      this.timeLastPathIncrement = System.currentTimeMillis();
      this.pathIndexCopy++;
      this.setCurrentPathIndex(this.pathIndexCopy);
   }

   public PathPoint getCurrentTargetPathPoint() {
      return this.isFinished() ? null : this.pointsCopy[this.getCurrentPathIndex()];
   }

   public PathPoint getFuturePathPoint(int jump) {
      return this.getCurrentPathIndex() >= this.pointsCopy.length - jump ? null : this.pointsCopy[this.getCurrentPathIndex() + jump];
   }

   public PathPoint getNextTargetPathPoint() {
      return this.getCurrentPathIndex() >= this.pointsCopy.length - 1 ? null : this.pointsCopy[this.getCurrentPathIndex() + 1];
   }

   public PathPoint getPastTargetPathPoint(int jump) {
      return this.getCurrentPathIndex() >= jump && this.pointsCopy.length != 0 ? this.pointsCopy[this.getCurrentPathIndex() - jump] : null;
   }

   public Vec3d getPosition(Entity var1) {
      return super.isFinished() ? null : super.getPosition(var1);
   }

   public PathPoint getPreviousTargetPathPoint() {
      return this.getCurrentPathIndex() >= 1 && this.pointsCopy.length != 0 ? this.pointsCopy[this.getCurrentPathIndex() - 1] : null;
   }

   public long getTimeSinceLastPathIncrement() {
      return System.currentTimeMillis() - this.timeLastPathIncrement;
   }

   public void setCurrentPathIndex(int par1) {
      this.timeLastPathIncrement = System.currentTimeMillis();
      this.pathIndexCopy = par1;
      super.setCurrentPathIndex(par1);
   }
}
