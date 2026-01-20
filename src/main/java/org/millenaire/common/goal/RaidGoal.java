package org.millenaire.common.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import org.millenaire.common.entity.MillVillager;

import java.util.EnumSet;

public class RaidGoal extends Goal {
    private final MillVillager villager;
    private BlockPos target;

    public RaidGoal(MillVillager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!villager.isRaider())
            return false;
        // Assuming target is BlockPos in RaidGoal
        target = villager.getRaidTarget();
        return target != null && villager.distanceToSqr(target.getX(), target.getY(), target.getZ()) > 100; // Move
                                                                                                            // if >
        // 10 blocks
        // away
    }

    @Override
    public void start() {
        if (target != null) {
            villager.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0D);
        }
    }

    @Override
    public void tick() {
        if (target != null && villager.getNavigation().isDone()
                && villager.distanceToSqr(target.getX(), target.getY(), target.getZ()) > 100) {
            villager.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.0D);
        }
    }
}
