package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Grape vine block for Millénaire.
 * A double-height crop used for wine production.
 * Has upper and lower halves that grow together.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockGrapeVine extends Block implements BonemealableBlock {

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 7);
    public static final EnumProperty<DoubleBlockHalf> HALF = EnumProperty.create("half", DoubleBlockHalf.class);

    protected static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public BlockGrapeVine() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.0F)
                .sound(SoundType.CROP)
                .randomTicks()
                .noCollission()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    public BlockGrapeVine(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    public int getMaxAge() {
        return 7;
    }

    public int getAge(BlockState state) {
        return state.getValue(AGE);
    }

    public BlockState withAge(int age) {
        return this.defaultBlockState().setValue(AGE, age);
    }

    /**
     * Check if vine can survive - requires matching other half and valid soil.
     */
    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        } else {
            BlockState above = level.getBlockState(pos.above());
            BlockState soil = level.getBlockState(pos.below());
            boolean hasUpperHalf = above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER;
            boolean hasValidSoil = soil.is(Blocks.FARMLAND) || soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK);
            return hasUpperHalf && hasValidSoil;
        }
    }

    /**
     * Place upper half when lower half is placed.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        level.setBlock(pos.above(), this.defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    /**
     * Growth tick - only lower half grows, updates both halves.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) {
            if (level.getRawBrightness(pos.above(), 0) >= 9) {
                int age = getAge(state);
                if (age < getMaxAge()) {
                    if (random.nextInt(10) == 0) {
                        int newAge = age + 1;
                        level.setBlock(pos, withAge(newAge), 2);
                        level.setBlock(pos.above(), withAge(newAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
                    }
                }
            }
        }
    }

    // BonemealableBlock implementation
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return getAge(state) < getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(getAge(state) + random.nextInt(2) + 1, getMaxAge());

        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos lowerPos = pos.below();
            level.setBlock(lowerPos, withAge(newAge), 2);
            level.setBlock(pos, withAge(newAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
        } else {
            level.setBlock(pos, withAge(newAge), 2);
            level.setBlock(pos.above(), withAge(newAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
        }
    }

    /**
     * Double block half enum.
     */
    public enum DoubleBlockHalf implements StringRepresentable {
        UPPER("upper"),
        LOWER("lower");

        private final String name;

        DoubleBlockHalf(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
