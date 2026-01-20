package org.millenaire.pathing;

import net.minecraft.world.level.Level;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.pathing.atomicstryker.AStarPathPlannerJPS;
import org.millenaire.common.pathing.atomicstryker.IAStarPathedEntity;

/**
 * Handles creation and management of path finders for entities.
 */
public class PathingHandler {

    /**
     * Creates a new JPS path planner for an entity.
     * 
     * @param world  The world level
     * @param entity The entity requiring pathing
     * @return AStarPathPlannerJPS instance
     */
    public static AStarPathPlannerJPS createPlanner(Level world, IAStarPathedEntity entity) {
        return new AStarPathPlannerJPS(world, entity, true);
    }

    /**
     * Get default config for a citizen.
     */
    public static AStarConfig getDefaultConfig() {
        // AStarConfig(boolean allowDropping, boolean canTakeDiagonals, boolean
        // canSwimming, boolean canUseDoors, boolean canClearLeaves, int
        // toleranceHorizontal, int toleranceVertical)
        return new AStarConfig(true, true, false, true, false, 1, 1);
    }
}
