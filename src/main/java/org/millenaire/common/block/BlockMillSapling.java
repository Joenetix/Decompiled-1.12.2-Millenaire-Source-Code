package org.millenaire.common.block;

import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Custom sapling block for Millénaire trees.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillSapling extends SaplingBlock {

    public BlockMillSapling(AbstractTreeGrower grower) {
        super(grower, BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS));
    }

    public BlockMillSapling(AbstractTreeGrower grower, BlockBehaviour.Properties properties) {
        super(grower, properties);
    }
}

