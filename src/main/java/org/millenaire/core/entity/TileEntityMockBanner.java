package org.millenaire.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityMockBanner extends BannerBlockEntity {
    public TileEntityMockBanner(BlockPos pos, BlockState state) {
        super(pos, state);
    }
}
