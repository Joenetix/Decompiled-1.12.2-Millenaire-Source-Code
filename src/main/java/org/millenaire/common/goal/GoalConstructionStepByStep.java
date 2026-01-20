package org.millenaire.common.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.village.Building;
import org.millenaire.core.Village;
import net.minecraft.world.level.block.Blocks;

public class GoalConstructionStepByStep extends Goal {

    private Building targetBuilding;
    private BlockPos targetPos;

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        Village v = MillVillager.getVillage();
        if (v == null)
            return false;

        // Find a building that needs construction
        for (Building b : v.getBuildings()) {
            if (b.isActive) {
                targetPos = b.getNextConstructionPos();
                if (targetPos != null) {
                    targetBuilding = b;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        if (targetBuilding == null || targetPos == null)
            return true;

        // Re-validate target (another builder might have placed it)
        BlockPos currentNext = targetBuilding.getNextConstructionPos();
        if (currentNext == null || !currentNext.equals(targetPos)) {
            // Target changed or finished, abort this step
            return true;
        }

        if (MillVillager.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ()) > 16) {
            MillVillager.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
            return false;
        } else {
            // Place block
            BlockState state = targetBuilding.getNextConstructionState();
            if (state == null)
                return true;

            // Don't replace unbreakable blocks or entities if not careful
            // But we assume the plan is correct.
            MillVillager.level().setBlock(targetPos, state, 3);
            targetBuilding.onConstructionStepComplete();

            // Swing arm
            MillVillager.swing(net.minecraft.world.InteractionHand.MAIN_HAND);

            return true;
        }
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 50; // High priority for builders
    }
}

