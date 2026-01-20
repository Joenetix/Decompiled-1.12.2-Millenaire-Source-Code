package org.millenaire.common.block;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Interface for path blocks (roads, walkways).
 * Ported from 1.12.2 to 1.20.1.
 */
public interface IBlockPath {
    /**
     * Property indicating if the path is stable (cannot collapse).
     */
    BooleanProperty STABLE = BooleanProperty.create("stable");

    /**
     * Get the double (full) slab version of this path.
     */
    BlockPath getDoubleSlab();

    /**
     * Get the single (half) slab version of this path.
     */
    BlockPathSlab getSingleSlab();

    /**
     * Check if this is a full path block (not a slab).
     */
    boolean isFullPath();
}

