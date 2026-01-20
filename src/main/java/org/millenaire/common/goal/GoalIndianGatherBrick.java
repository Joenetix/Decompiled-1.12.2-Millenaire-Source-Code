package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/** Goal for Indian villagers to gather dried bricks. */
public class GoalIndianGatherBrick extends Goal {
    @Override
    public boolean isPossible(MillVillager MillVillager) {
        return MillVillager.getVillage() != null;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        return false;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 50;
    }
}

