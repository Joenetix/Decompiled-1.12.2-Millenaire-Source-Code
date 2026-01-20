package org.millenaire.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.millenaire.core.MillBlockEntities;
import org.millenaire.core.entity.TileEntityFirePit;

/**
 * BlockFirePit - Multi-slot outdoor cooking station.
 * Has X/Z alignment based on player facing direction.
 * Emits particles and sounds when lit.
 * Ported from original 1.12.2 BlockFirePit.java
 */
public class BlockFirePit extends BaseEntityBlock {

    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final EnumProperty<EnumAlignment> ALIGNMENT = EnumProperty.create("alignment", EnumAlignment.class);

    // Bounding box: centered, half-height block
    protected static final VoxelShape FIRE_PIT_SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 8.0, 13.0);

    public BlockFirePit(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LIT, false)
                .setValue(ALIGNMENT, EnumAlignment.Z));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, ALIGNMENT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return FIRE_PIT_SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityFirePit(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(type, MillBlockEntities.FIRE_PIT.get(), TileEntityFirePit::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityFirePit firePit) {
                firePit.dropAll();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    /**
     * Set alignment based on player facing when placed.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        if (placer != null) {
            Direction.Axis axis = placer.getDirection().getAxis();
            EnumAlignment alignment = EnumAlignment.fromAxis(axis);
            level.setBlock(pos, state.setValue(ALIGNMENT, alignment), 3);
        }
    }

    /**
     * Open GUI when right-clicked.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TileEntityFirePit firePit) {
                // TODO: Open fire pit GUI - needs NetworkHooks.openScreen or similar
                // For now, just return success to indicate interaction happened
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /**
     * Spawn particles and play sound when lit.
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            // Play fire crackling sound occasionally
            if (random.nextInt(24) == 0) {
                level.playLocalSound(
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS,
                        1.0F + random.nextFloat(), random.nextFloat() * 0.7F + 0.3F,
                        false);
            }

            // Spawn smoke particles
            for (int i = 0; i < 3; i++) {
                double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
                double y = pos.getY() + 0.5 + random.nextDouble() * 0.3;
                double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
                level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.05, 0.0);
            }

            // Occasionally spawn large smoke
            if (random.nextInt(8) == 0) {
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.8;
                double z = pos.getZ() + 0.5;
                level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.1, 0.0);
            }
        }
    }

    /**
     * Alignment enum for fire pit orientation.
     */
    public enum EnumAlignment implements StringRepresentable {
        X("x", 0, 90.0),
        Z("z", 1, 0.0);

        private final String name;
        private final int meta;
        public final double angle;

        EnumAlignment(String name, int meta, double angle) {
            this.name = name;
            this.meta = meta;
            this.angle = angle;
        }

        public static EnumAlignment fromAxis(Direction.Axis axis) {
            if (axis == Direction.Axis.X) {
                return Z; // Rotate 90 degrees when facing X
            } else if (axis == Direction.Axis.Z) {
                return X;
            }
            return Z; // Default for Y axis
        }

        public static EnumAlignment fromMeta(int meta) {
            return meta != 0 ? X : Z;
        }

        public int getMeta() {
            return this.meta;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
