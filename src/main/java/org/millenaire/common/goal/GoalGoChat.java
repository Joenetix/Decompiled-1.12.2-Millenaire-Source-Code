package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

import java.util.List;

/**
 * Chat goal - villager finds another villager to chat with.
 */
public class GoalGoChat extends Goal {

    public GoalGoChat() {
        this.leasure = true;
        this.travelBookShow = false;
    }

    @Override
    public GoalInformation getDestination(MillVillager villager) throws Exception {
        // Find a nearby villager to chat with
        List<MillVillager> villagers = villager.getTownHall().getKnownVillagers();

        MillVillager closest = null;
        double closestDist = Double.MAX_VALUE;

        for (MillVillager v : villagers) {
            if (v != villager) {
                double dist = villager.getPos().distanceTo(v.getPos());
                if (dist < closestDist && dist < 20) {
                    closestDist = dist;
                    closest = v;
                }
            }
        }

        if (closest != null) {
            return packDest(closest.getPos(), null, closest);
        }

        return packDest(villager.getPos());
    }

    @Override
    public boolean performAction(MillVillager villager) throws Exception {
        // Chat complete
        return true;
    }

    @Override
    public int priority(MillVillager villager) throws Exception {
        return 8 + villager.getRandom().nextInt(12);
    }

    @Override
    public int actionDuration(MillVillager villager) throws Exception {
        return 1500 + villager.getRandom().nextInt(2500);
    }

    @Override
    public boolean canBeDoneAtNight() {
        return false;
    }

    @Override
    public boolean lookAtPlayer() {
        return true;
    }

    @Override
    public int range(MillVillager villager) {
        return 5;
    }
}
