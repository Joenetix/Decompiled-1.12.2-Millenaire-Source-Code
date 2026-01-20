package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A chest that can be locked by villages/NPCs.
 * Players need permission (alliance, trading) to access.
 * Supports double chest detection and cat blocking like vanilla.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockLockedChest extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Slightly smaller than full block for visual distinction
    private static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 15.0);

    // Bounding boxes for double chests (adjusted based on adjacent chest direction)
    protected static final VoxelShape NORTH_CHEST_AABB = Block.box(1.0, 0.0, 0.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape SOUTH_CHEST_AABB = Block.box(1.0, 0.0, 1.0, 15.0, 14.0, 16.0);
    protected static final VoxelShape WEST_CHEST_AABB = Block.box(0.0, 0.0, 1.0, 15.0, 14.0, 15.0);
    protected static final VoxelShape EAST_CHEST_AABB = Block.box(1.0, 0.0, 1.0, 16.0, 14.0, 15.0);

    public BlockLockedChest(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Adjust shape for double chests
        if (level.getBlockState(pos.north()).getBlock() == this) {
            return NORTH_CHEST_AABB;
        } else if (level.getBlockState(pos.south()).getBlock() == this) {
            return SOUTH_CHEST_AABB;
        } else if (level.getBlockState(pos.west()).getBlock() == this) {
            return WEST_CHEST_AABB;
        } else if (level.getBlockState(pos.east()).getBlock() == this) {
            return EAST_CHEST_AABB;
        }
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LockedChestBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            // Check if chest is blocked by cat or solid block above
            if (isBlocked(level, pos)) {
                return InteractionResult.FAIL;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LockedChestBlockEntity chest) {
                if (chest.isLocked() && !chest.isOwner(player)) {
                    player.displayClientMessage(Component.translatable("message.millenaire.chest_locked"), true);
                    return InteractionResult.FAIL;
                }

                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, chest, pos);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LockedChestBlockEntity chest) {
                chest.setCustomName(stack.getHoverName());
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof LockedChestBlockEntity chest) {
                // Drop inventory contents
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);
                    if (!stack.isEmpty()) {
                        Block.popResource(level, pos, stack);
                    }
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof LockedChestBlockEntity chest) {
            return net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(chest);
        }
        return 0;
    }

    /**
     * Check if this position is part of a double chest.
     */
    public boolean isDoubleChest(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != this) {
            return false;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(direction)).getBlock() == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if chest can be placed - limit to max 2 adjacent chests.
     */
    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        int adjacentCount = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacentPos = pos.relative(direction);
            if (level.getBlockState(adjacentPos).getBlock() == this) {
                if (isDoubleChest((Level) level, adjacentPos)) {
                    return false; // Can't place next to already-double chest
                }
                adjacentCount++;
            }
        }
        return adjacentCount <= 1;
    }

    /**
     * Check if chest is blocked by solid block above or cat sitting on it.
     */
    private boolean isBlocked(Level level, BlockPos pos) {
        return isBelowSolidBlock(level, pos) || isCatSittingOnChest(level, pos);
    }

    /**
     * Check if there's a solid block directly above.
     */
    private boolean isBelowSolidBlock(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isSolidRender(level, above);
    }

    /**
     * Check if a cat is sitting on this chest.
     */
    private boolean isCatSittingOnChest(Level level, BlockPos pos) {
        List<Cat> cats = level.getEntitiesOfClass(Cat.class,
                new AABB(pos.getX(), pos.getY() + 1, pos.getZ(),
                        pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1));
        for (Cat cat : cats) {
            if (cat.isInSittingPose()) {
                return true;
            }
        }
        return false;
    }
}
