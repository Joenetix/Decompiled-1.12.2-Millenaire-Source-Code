package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/**
 * Goal for villagers to bring resources back to their home building.
 */
public class GoalBringBackResourcesHome extends Goal {

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        // Check if carrying resources
        return MillVillager.getVillage() != null;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        // TODO: Implement return logic
        return false;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 55; // Higher priority to empty inventory
    }
}

