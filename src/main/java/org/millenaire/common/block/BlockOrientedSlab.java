package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A slab block that can be oriented along different axes.
 * Used for Japanese roof tiles that need directional placement.
 * 
 * Combines the standard slab behavior (top/bottom/double) with horizontal
 * axis orientation (X or Z) for proper roof tile alignment.
 */
public class BlockOrientedSlab extends SlabBlock {

    // Horizontal axis orientation
    public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class,
            Direction.Axis.X, Direction.Axis.Z);

    // VoxelShapes for each configuration
    protected static final VoxelShape BOTTOM_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    protected static final VoxelShape TOP_AABB = Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);
    protected static final VoxelShape FULL_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public BlockOrientedSlab(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TYPE, SlabType.BOTTOM)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState superState = super.getStateForPlacement(context);
        if (superState == null)
            return null;

        // Determine axis from player facing
        Direction facing = context.getHorizontalDirection();
        Direction.Axis axis;

        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            axis = Direction.Axis.Z;
        } else {
            axis = Direction.Axis.X;
        }

        return superState.setValue(AXIS, axis);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        SlabType slabType = state.getValue(TYPE);
        switch (slabType) {
            case DOUBLE:
                return FULL_AABB;
            case TOP:
                return TOP_AABB;
            default:
                return BOTTOM_AABB;
        }
    }

    /**
     * Get the axis orientation of the slab.
     */
    public Direction.Axis getAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    /**
     * Check if this slab is oriented along the X axis.
     */
    public boolean isXAxis(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.X;
    }

    /**
     * Check if this slab is oriented along the Z axis.
     */
    public boolean isZAxis(BlockState state) {
        return state.getValue(AXIS) == Direction.Axis.Z;
    }
}

