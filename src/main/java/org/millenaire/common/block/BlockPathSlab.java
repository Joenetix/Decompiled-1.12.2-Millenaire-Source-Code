package org.millenaire.common.block;

import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.Block;

/**
 * Path slab block for Millénaire villages.
 * Half-block version of paths.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockPathSlab extends SlabBlock implements IBlockPath {

    public static final BooleanProperty STABLE = IBlockPath.STABLE;

    private BlockPath doubleSlab;

    public BlockPathSlab() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, net.minecraft.world.level.block.state.properties.SlabType.BOTTOM)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(STABLE, Boolean.TRUE));
    }

    public BlockPathSlab(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, net.minecraft.world.level.block.state.properties.SlabType.BOTTOM)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(STABLE, Boolean.TRUE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STABLE);
    }

    public void setDoubleSlab(BlockPath path) {
        this.doubleSlab = path;
    }

    @Override
    public BlockPath getDoubleSlab() {
        return this.doubleSlab;
    }

    @Override
    public BlockPathSlab getSingleSlab() {
        return this;
    }

    @Override
    public boolean isFullPath() {
        return false;
    }
}
