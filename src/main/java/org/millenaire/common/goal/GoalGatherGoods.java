package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/**
 * Goal for villagers to gather goods from village storage.
 */
public class GoalGatherGoods extends Goal {

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        return MillVillager.getVillage() != null;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        // TODO: Implement gathering logic
        return false;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 35;
    }
}

