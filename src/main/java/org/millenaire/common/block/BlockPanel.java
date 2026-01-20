package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Panel block for Millénaire - decorative wall panels.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockPanel extends Block {

    public BlockPanel() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(1.5F, 2.0F)
                .sound(SoundType.WOOD));
    }

    public BlockPanel(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

