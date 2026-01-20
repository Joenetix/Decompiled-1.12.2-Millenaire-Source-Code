package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Path/road block for Millénaire villages.
 * Full block version of paths.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockPath extends Block implements IBlockPath {

    public static final BooleanProperty STABLE = IBlockPath.STABLE;

    private BlockPathSlab singleSlab;

    public BlockPath() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(1.5F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(STABLE, false));
    }

    public BlockPath(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(STABLE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STABLE);
    }

    public void setSingleSlab(BlockPathSlab slab) {
        this.singleSlab = slab;
    }

    @Override
    public BlockPath getDoubleSlab() {
        return this;
    }

    @Override
    public BlockPathSlab getSingleSlab() {
        return this.singleSlab;
    }

    @Override
    public boolean isFullPath() {
        return true;
    }
}

