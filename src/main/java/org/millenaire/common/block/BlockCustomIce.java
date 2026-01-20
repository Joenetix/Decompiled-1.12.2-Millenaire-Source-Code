package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Custom ice block for Millénaire buildings.
 * Features translucent rendering and slippery surface.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockCustomIce extends Block {

    public BlockCustomIce() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.ICE)
                .friction(0.98F)
                .strength(0.5F)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 20)
                .noOcclusion()
                .isViewBlocking((state, level, pos) -> false));
    }

    public BlockCustomIce(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

