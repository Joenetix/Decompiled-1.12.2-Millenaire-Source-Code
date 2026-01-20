package org.millenaire.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.level.Level;

/**
 * EntityTargetedGhast - Ghast that tracks to a specific target point.
 * Ported exactly from original 1.12.2 EntityTargetedGhast.java
 * Used in certain quests.
 */
public class EntityTargetedGhast extends Ghast {

    public BlockPos target = null;

    public EntityTargetedGhast(EntityType<? extends Ghast> type, Level world) {
        super(type, world);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public void tick() {
        if (this.target != null) {
            double distance = Math.sqrt(
                    Math.pow(target.getX() - this.getX(), 2) +
                            Math.pow(target.getY() - this.getY(), 2) +
                            Math.pow(target.getZ() - this.getZ(), 2));

            if (distance > 20.0) {
                // Move towards target
                this.getMoveControl().setWantedPosition(
                        target.getX(),
                        target.getY(),
                        target.getZ(),
                        this.getSpeed());
            } else if (distance < 10.0) {
                // Move randomly around target
                this.getMoveControl().setWantedPosition(
                        target.getX() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F,
                        target.getY() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F,
                        target.getZ() + (this.random.nextFloat() * 2.0F - 1.0F) * 16.0F,
                        this.getSpeed());
            }
        }

        super.tick();
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
