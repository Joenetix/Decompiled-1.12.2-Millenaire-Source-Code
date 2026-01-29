package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Decorative earth block for Millénaire - dirt walls and similar.
 * Ported from 1.12.2 to 1.20.1.
 * 
 * NOTE: In 1.12.2, this block had metadata variants, but in modern Minecraft
 * each variant would be its own block. For now, this is just the "dirtwall"
 * block.
 * Additional earth variants can be added as separate blocks if needed.
 */
public class BlockDecorativeEarth extends Block {

    public BlockDecorativeEarth() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.8F)
                .sound(SoundType.GRAVEL));
    }
}
