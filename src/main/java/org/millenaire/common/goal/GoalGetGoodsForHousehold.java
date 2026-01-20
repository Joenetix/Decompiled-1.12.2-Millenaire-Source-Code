package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/** Goal for getting goods for household. */
public class GoalGetGoodsForHousehold extends Goal {
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
        return 45;
    }
}

