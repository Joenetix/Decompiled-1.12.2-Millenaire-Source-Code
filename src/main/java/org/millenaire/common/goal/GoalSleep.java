package org.millenaire.common.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.entity.MillVillager;

public class GoalSleep extends Goal {

    public GoalSleep() {
        super();
        this.key = "sleep";
    }

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        if (MillVillager.level().isDay())
            return false;

        // Check if home exists and has a bed
        if (MillVillager.getHouse() == null)
            return false;
        if (MillVillager.getHouse().getSleepingPos() == null)
            return false;

        return true;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 100; // High priority at night
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        BlockPos bedPos = MillVillager.getHouse() != null ? MillVillager.getHouse().getSleepingPos() : null;

        if (bedPos == null)
            return true; // Should not happen if isPossible checked

        double distSq = MillVillager.distanceToSqr(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5);

        if (distSq > 4.0) {
            // Move to bed
            MillVillager.getNavigation().moveTo(bedPos.getX() + 0.5, bedPos.getY(), bedPos.getZ() + 0.5, 1.0);
        } else {
            // Sleep
            if (!MillVillager.isSleeping()) {
                MillVillager.startSleeping(bedPos);
            }
        }

        return true; // Goal continues until day
    }
}

