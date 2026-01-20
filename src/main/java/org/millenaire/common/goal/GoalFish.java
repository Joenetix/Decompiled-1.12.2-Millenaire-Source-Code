package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/**
 * Goal for fishing villagers to catch fish.
 */
public class GoalFish extends Goal {

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        // Check if near water and has fishing rod
        return MillVillager.getVillage() != null;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        // TODO: Implement fishing logic
        return false;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 45;
    }
}

