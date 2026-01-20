package org.millenaire.common.block;

import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.DyeColor;

/**
 * Stained glass block for Millénaire.
 * Used in church windows and decorative elements.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillStainedGlass extends StainedGlassBlock {

    public BlockMillStainedGlass(DyeColor color) {
        super(color, BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isRedstoneConductor((state, level, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
    }

    public BlockMillStainedGlass(DyeColor color, BlockBehaviour.Properties properties) {
        super(color, properties);
    }
}

