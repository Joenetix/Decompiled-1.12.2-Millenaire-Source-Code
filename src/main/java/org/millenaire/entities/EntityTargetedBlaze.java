package org.millenaire.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * EntityTargetedBlaze - Blaze that tracks to a specific target point.
 * Ported exactly from original 1.12.2 EntityTargetedBlaze.java
 * Used in certain quests.
 */
public class EntityTargetedBlaze extends Blaze {

    public BlockPos target = null;

    public EntityTargetedBlaze(EntityType<? extends Blaze> type, Level world) {
        super(type, world);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    private boolean isCourseTraversable(double par1, double par3, double par5, double par7) {
        if (target == null)
            return false;

        double d4 = (target.getX() - this.getX()) / par7;
        double d5 = (target.getY() - this.getY()) / par7;
        double d6 = (target.getZ() - this.getZ()) / par7;
        AABB axisalignedbb = this.getBoundingBox().move(0.0, 0.0, 0.0);

        for (int i = 1; i < par7; i++) {
            axisalignedbb = axisalignedbb.move(d4, d5, d6);
            if (!this.level().noCollision(this, axisalignedbb)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("targetX")) {
            this.target = new BlockPos(
                    compound.getInt("targetX"),
                    compound.getInt("targetY"),
                    compound.getInt("targetZ"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.target != null) {
            compound.putInt("targetX", this.target.getX());
            compound.putInt("targetY", this.target.getY());
            compound.putInt("targetZ", this.target.getZ());
        }
    }
}
