package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/**
 * Goal for trading villagers to sell goods to players.
 * Waits at shop and handles transactions.
 */
public class GoalBeSeller extends Goal {

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        // Check if villager is assigned to a shop building
        return MillVillager.getVillage() != null;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        // TODO: Implement seller logic
        // 1. Go to shop position
        // 2. Wait for player interaction
        // 3. Handle trade
        return false;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 30; // Low priority, idle task
    }
}

