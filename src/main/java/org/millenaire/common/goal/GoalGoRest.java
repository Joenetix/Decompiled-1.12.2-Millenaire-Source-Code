package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.utilities.Point;

/**
 * Rest goal - villager takes a break and rests in place.
 */
public class GoalGoRest extends Goal {

    public GoalGoRest() {
        this.leasure = true;
        this.travelBookShow = false;
    }

    @Override
    public GoalInformation getDestination(MillVillager villager) throws Exception {
        // Rest at current position
        return packDest(villager.getPos());
    }

    @Override
    public boolean performAction(MillVillager villager) throws Exception {
        // Just rest - no specific action
        return true;
    }

    @Override
    public int priority(MillVillager villager) throws Exception {
        return 5 + villager.getRandom().nextInt(10);
    }

    @Override
    public int actionDuration(MillVillager villager) throws Exception {
        return 3000 + villager.getRandom().nextInt(2000);
    }

    @Override
    public boolean canBeDoneAtNight() {
        return true;
    }
}
