package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;

import java.util.function.Supplier;

/**
 * Custom crop block with Millénaire-specific growth mechanics.
 * Supports irrigation requirements and variable growth speed.
 */
public class BlockMillCrops extends BushBlock implements BonemealableBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final int MAX_AGE = 7;

    // Bounding boxes for each growth stage
    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[] {
            Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), // Stage 0
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), // Stage 1
            Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), // Stage 2
            Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), // Stage 3
            Block.box(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), // Stage 4
            Block.box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), // Stage 5
            Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), // Stage 6
            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 16.0) // Stage 7 (max)
    };

    private final boolean requiresIrrigation;
    private final boolean slowGrowth;
    private final Supplier<Item> seedItem;

    public BlockMillCrops(Properties properties, boolean requiresIrrigation, boolean slowGrowth,
            Supplier<Item> seedItem) {
        super(properties);
        this.requiresIrrigation = requiresIrrigation;
        this.slowGrowth = slowGrowth;
        this.seedItem = seedItem;
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    public BlockMillCrops(Properties properties) {
        this(properties, false, false, () -> null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[getAge(state)];
    }

    public int getAge(BlockState state) {
        return state.getValue(AGE);
    }

    public boolean isMaxAge(BlockState state) {
        return getAge(state) >= MAX_AGE;
    }

    public BlockState withAge(int age) {
        return this.defaultBlockState().setValue(AGE, Math.min(age, MAX_AGE));
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return this.mayPlaceOn(level.getBlockState(below), level, below);
    }

    /**
     * Get growth chance based on irrigation and growth speed settings.
     */
    protected float getGrowthChance(Level level, BlockPos pos) {
        if (requiresIrrigation) {
            // Check if farmland below is irrigated (moisture level > 0)
            BlockState below = level.getBlockState(pos.below());
            if (below.is(Blocks.FARMLAND)) {
                // Farmland moisture is stored in the block state
                int moisture = below.getValue(net.minecraft.world.level.block.FarmBlock.MOISTURE);
                if (moisture == 0) {
                    return 0.0f; // No growth without irrigation
                }
            }
        }
        return slowGrowth ? 4.0f : 8.0f;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !isMaxAge(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1))
            return;

        if (level.getRawBrightness(pos, 0) >= 9) {
            int age = getAge(state);
            if (age < MAX_AGE) {
                float growthChance = getGrowthChance(level, pos);
                if (growthChance > 0.0f) {
                    int threshold = (int) (25.0f / growthChance);
                    if (ForgeHooks.onCropsGrowPre(level, pos, state, random.nextInt(threshold + 1) == 0)) {
                        level.setBlock(pos, withAge(age + 1), 2);
                        ForgeHooks.onCropsGrowPost(level, pos, state);
                    }
                }
            }
        }
    }

    // Bonemeal support
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return !isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int newAge = Math.min(getAge(state) + random.nextIntBetweenInclusive(2, 5), MAX_AGE);
        level.setBlock(pos, withAge(newAge), 2);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (seedItem.get() != null) {
            return new ItemStack(seedItem.get());
        }
        return super.getCloneItemStack(level, pos, state);
    }

    // Factory methods for specific crop types
    public static BlockMillCrops rice(Supplier<Item> seed) {
        return new BlockMillCrops(
                BlockBehaviour.Properties.of().noCollission().instabreak().randomTicks(),
                true, // Requires irrigation
                false,
                seed);
    }

    public static BlockMillCrops turmeric(Supplier<Item> seed) {
        return new BlockMillCrops(
                BlockBehaviour.Properties.of().noCollission().instabreak().randomTicks(),
                true, // Requires irrigation
                true, // Slow growth
                seed);
    }

    public static BlockMillCrops maize(Supplier<Item> seed) {
        return new BlockMillCrops(
                BlockBehaviour.Properties.of().noCollission().instabreak().randomTicks(),
                false,
                false,
                seed);
    }

    public static BlockMillCrops cotton(Supplier<Item> seed) {
        return new BlockMillCrops(
                BlockBehaviour.Properties.of().noCollission().instabreak().randomTicks(),
                false,
                true, // Slow growth
                seed);
    }

    public static BlockMillCrops vine(Supplier<Item> seed) {
        return new BlockMillCrops(
                BlockBehaviour.Properties.of().noCollission().instabreak().randomTicks(),
                true, // Requires irrigation (grapes need water)
                true, // Slow growth
                seed);
    }
}

