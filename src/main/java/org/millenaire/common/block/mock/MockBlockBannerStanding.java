package org.millenaire.common.block.mock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Mock block for standing banner in building plans.
 * Indicates a standing banner placement.
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockBannerStanding extends Block {

    public MockBlockBannerStanding() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
    }
}

