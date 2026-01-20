package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/** Goal for foreign merchants keeping their stall. */
public class GoalForeignMerchantKeepStall extends Goal {
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
        return 30;
    }
}

