package org.millenaire.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * PathNavigateSimple - Simple path navigation for villagers.
 * Ported exactly from original 1.12.2 PathNavigateSimple.java
 * 
 * Custom navigation that works well for villager NPCs.
 */
public class PathNavigateSimple extends GroundPathNavigation {

    public PathNavigateSimple(Mob entity, Level world) {
        super(entity, world);
    }

    @Override
    protected boolean canUpdatePath() {
        return true;
    }

    private boolean getCanSwim() {
        return true;
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.getPathablePosY(), this.mob.getZ());
    }

    private int getPathablePosY() {
        if (this.mob.isInWater() && this.getCanSwim()) {
            int y = (int) this.mob.getBoundingBox().minY;
            Block block = this.level.getBlockState(
                    new BlockPos(
                            Mth.floor(this.mob.getX()),
                            y,
                            Mth.floor(this.mob.getZ())))
                    .getBlock();

            int attempts = 0;
            while (block == Blocks.WATER) {
                y++;
                block = this.level.getBlockState(
                        new BlockPos(
                                Mth.floor(this.mob.getX()),
                                y,
                                Mth.floor(this.mob.getZ())))
                        .getBlock();

                if (++attempts > 16) {
                    return (int) this.mob.getBoundingBox().minY;
                }
            }
            return y;
        } else {
            return (int) (this.mob.getBoundingBox().minY + 0.5);
        }
    }

    @Override
    protected boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32) {
        int i = Mth.floor(posVec31.x);
        int j = Mth.floor(posVec31.z);
        double d0 = posVec32.x - posVec31.x;
        double d1 = posVec32.z - posVec31.z;
        double d2 = d0 * d0 + d1 * d1;

        if (d2 < 1.0E-8) {
            return false;
        }

        double d3 = 1.0 / Math.sqrt(d2);
        d0 *= d3;
        d1 *= d3;

        int sizeX = Mth.ceil(this.mob.getBbWidth());
        int sizeY = Mth.ceil(this.mob.getBbHeight());
        int sizeZ = sizeX;

        sizeX += 2;
        sizeZ += 2;

        if (!this.isSafeToStandAt(i, (int) posVec31.y, j, sizeX, sizeY, sizeZ, posVec31, d0, d1)) {
            return false;
        }

        sizeX -= 2;
        sizeZ -= 2;

        double d4 = 1.0 / Math.abs(d0);
        double d5 = 1.0 / Math.abs(d1);
        double d6 = i - posVec31.x;
        double d7 = j - posVec31.z;

        if (d0 >= 0.0) {
            d6++;
        }
        if (d1 >= 0.0) {
            d7++;
        }

        d6 /= d0;
        d7 /= d1;

        int k = d0 < 0.0 ? -1 : 1;
        int l = d1 < 0.0 ? -1 : 1;
        int i1 = Mth.floor(posVec32.x);
        int j1 = Mth.floor(posVec32.z);
        int k1 = i1 - i;
        int l1 = j1 - j;

        while (k1 * k > 0 || l1 * l > 0) {
            if (d6 < d7) {
                d6 += d4;
                i += k;
                k1 = i1 - i;
            } else {
                d7 += d5;
                j += l;
                l1 = j1 - j;
            }

            if (!this.isSafeToStandAt(i, (int) posVec31.y, j, sizeX, sizeY, sizeZ, posVec31, d0, d1)) {
                return false;
            }
        }

        return true;
    }

    private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 origin, double dx,
            double dz) {
        for (BlockPos blockpos : BlockPos.betweenClosed(
                new BlockPos(x, y, z),
                new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {

            double d0 = blockpos.getX() + 0.5 - origin.x;
            double d1 = blockpos.getZ() + 0.5 - origin.z;

            if (d0 * dx + d1 * dz >= 0.0) {
                if (!this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos,
                        net.minecraft.world.level.pathfinder.PathComputationType.LAND)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 vec31, double dx,
            double dz) {
        int i = x - sizeX / 2;
        int j = z - sizeZ / 2;

        if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, vec31, dx, dz)) {
            return false;
        }

        for (int k = i; k < i + sizeX; k++) {
            for (int l = j; l < j + sizeZ; l++) {
                double d0 = k + 0.5 - vec31.x;
                double d1 = l + 0.5 - vec31.z;

                if (d0 * dx + d1 * dz >= 0.0) {
                    BlockPathTypes pathnodetype = this.nodeEvaluator.getBlockPathType(
                            this.level, k, y - 1, l, this.mob);

                    if (pathnodetype == BlockPathTypes.WATER) {
                        return false;
                    }
                    if (pathnodetype == BlockPathTypes.LAVA) {
                        return false;
                    }
                    if (pathnodetype == BlockPathTypes.OPEN) {
                        return false;
                    }

                    pathnodetype = this.nodeEvaluator.getBlockPathType(
                            this.level, k, y, l, this.mob);

                    float f = this.mob.getPathfindingMalus(pathnodetype);
                    if (f < 0.0F || f >= 8.0F) {
                        return false;
                    }

                    if (pathnodetype == BlockPathTypes.DAMAGE_FIRE ||
                            pathnodetype == BlockPathTypes.DANGER_FIRE ||
                            pathnodetype == BlockPathTypes.DAMAGE_OTHER) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    protected void followThePath() {
        Vec3 vec3d = this.getTempMobPos();
        Path path = this.path;

        if (path == null)
            return;

        int pathLen = path.getNodeCount();

        for (int j = path.getNextNodeIndex(); j < pathLen; j++) {
            if (path.getNode(j).y != Math.floor(vec3d.y)) {
                pathLen = j;
                break;
            }
        }

        this.maxDistanceToWaypoint = this.mob.getBbWidth() > 0.75F
                ? this.mob.getBbWidth() / 2.0F
                : 0.75F - this.mob.getBbWidth() / 2.0F;

        Vec3 target = path.getNextEntityPos(this.mob);

        if (Math.abs(this.mob.getX() - (target.x + 0.5)) < this.maxDistanceToWaypoint
                && Math.abs(this.mob.getZ() - (target.z + 0.5)) < this.maxDistanceToWaypoint
                && Math.abs(this.mob.getY() - target.y) < 1.0) {
            path.advance();
        }

        int k = Mth.ceil(this.mob.getBbWidth());
        int l = Mth.ceil(this.mob.getBbHeight());
        int i1 = k;

        for (int j1 = pathLen - 1; j1 >= path.getNextNodeIndex(); j1--) {
            if (this.canMoveDirectly(vec3d, path.getEntityPosAtNode(this.mob, j1))) {
                path.setNextNodeIndex(j1);
                break;
            }
        }
    }
}
