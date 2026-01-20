package org.millenaire.common.block;

import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Rosette bars - decorative window bars with rosette patterns.
 * Used for Byzantine and other decorative window styles.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockRosetteBars extends IronBarsBlock {

    public BlockRosetteBars() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    public BlockRosetteBars(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

