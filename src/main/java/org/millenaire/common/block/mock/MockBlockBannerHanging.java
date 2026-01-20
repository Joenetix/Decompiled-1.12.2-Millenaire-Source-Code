package org.millenaire.common.block.mock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Mock block for hanging banner in building plans.
 * Indicates a wall-mounted banner placement.
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockBannerHanging extends Block {

    public MockBlockBannerHanging() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
    }
}

