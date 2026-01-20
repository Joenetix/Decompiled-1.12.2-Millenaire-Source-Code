package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

import java.util.List;

/**
 * Socialise goal - villager moves to socialize with other villagers.
 */
public class GoalGoSocialise extends Goal {

    public GoalGoSocialise() {
        this.leasure = true;
        this.travelBookShow = false;
    }

    @Override
    public GoalInformation getDestination(MillVillager villager) throws Exception {
        // Try to find another villager to socialize with
        List<MillVillager> villagers = villager.getTownHall().getKnownVillagers();

        if (!villagers.isEmpty()) {
            MillVillager target = villagers.get(villager.getRandom().nextInt(villagers.size()));
            if (target != villager) {
                return packDest(target.getPos());
            }
        }

        // Fallback to house
        Building house = villager.getHouse();
        if (house != null && house.getSleepingPos() != null) {
            return packDest(new Point(house.getSleepingPos()));
        }

        return packDest(villager.getPos());
    }

    @Override
    public boolean performAction(MillVillager villager) throws Exception {
        // Socializing complete
        return true;
    }

    @Override
    public int priority(MillVillager villager) throws Exception {
        return 10 + villager.getRandom().nextInt(15);
    }

    @Override
    public int actionDuration(MillVillager villager) throws Exception {
        return 2000 + villager.getRandom().nextInt(3000);
    }

    @Override
    public boolean canBeDoneAtNight() {
        return false;
    }

    @Override
    public boolean lookAtPlayer() {
        return true;
    }
}
