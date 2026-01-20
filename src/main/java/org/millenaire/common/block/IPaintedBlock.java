package org.millenaire.common.block;

import net.minecraft.world.item.DyeColor;

/**
 * Interface for painted blocks that have a dye color.
 * Ported from 1.12.2 to 1.20.1.
 */
public interface IPaintedBlock {
    /**
     * Get the type of this painted block (e.g., "brick", "wall").
     */
    String getBlockType();

    /**
     * Get the dye color of this painted block.
     */
    DyeColor getDyeColour();
}

