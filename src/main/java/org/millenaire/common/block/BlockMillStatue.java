package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Statue block for Millénaire - decorative statues.
 * Used for culture-specific monuments and decorations.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillStatue extends Block {

    public BlockMillStatue() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    public BlockMillStatue(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

