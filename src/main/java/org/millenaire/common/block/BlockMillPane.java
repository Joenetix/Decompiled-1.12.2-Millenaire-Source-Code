package org.millenaire.common.block;

import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Glass pane block for Millénaire.
 * Used for windows in various cultures.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockMillPane extends IronBarsBlock {

    public BlockMillPane() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0.3F)
                .sound(SoundType.GLASS)
                .noOcclusion());
    }

    public BlockMillPane(BlockBehaviour.Properties properties) {
        super(properties);
    }
}

