package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.millenaire.core.MillBlockEntities;
import org.millenaire.core.entity.TileEntityImportTable;

import javax.annotation.Nullable;

/**
 * BlockImportTable - Block that provides the TileEntityImportTable.
 * Ported exactly from original 1.12.2 BlockImportTable.java
 */
public class BlockImportTable extends BaseEntityBlock {

    public BlockImportTable(BlockBehaviour.Properties properties) {
        super(properties.strength(1.0F).sound(SoundType.WOOD));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityImportTable(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityImportTable importTable) {
            importTable.activate(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

