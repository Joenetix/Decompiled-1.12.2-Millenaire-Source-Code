package org.millenaire.common.goal;

import org.millenaire.common.entity.MillVillager;

/**
 * Goal for villagers to defend the village from threats.
 * Attacks hostile mobs and raiders.
 */
public class GoalDefendVillage extends Goal {

    @Override
    public boolean isPossible(MillVillager MillVillager) {
        // Check if there are threats nearby
        return MillVillager.getVillage() != null;
    }

    @Override
    public boolean performAction(MillVillager MillVillager) {
        // TODO: Implement combat logic
        // 1. Find nearest threat
        // 2. Equip weapon
        // 3. Attack target
        return false;
    }

    @Override
    public int priority(MillVillager MillVillager) {
        return 90; // Very high priority for defense
    }

    @Override
    public int range(MillVillager MillVillager) {
        return 2; // Melee range
    }
}

