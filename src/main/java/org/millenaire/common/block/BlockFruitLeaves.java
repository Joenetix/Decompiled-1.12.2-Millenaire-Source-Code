package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Fruit-bearing leaves block for Millénaire.
 * Apple and other fruit trees with growth cycle and harvesting.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockFruitLeaves extends LeavesBlock implements BonemealableBlock {

    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
    public static final EnumProperty<FruitType> FRUIT = EnumProperty.create("fruit", FruitType.class);

    public BlockFruitLeaves() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .strength(0.2F)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false));
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(FRUIT, FruitType.APPLE)
                .setValue(DISTANCE, 7)
                .setValue(PERSISTENT, false)
                .setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AGE, FRUIT);
    }

    /**
     * Returns the maximum age for fruit ripeness.
     */
    public int getMaxAge() {
        return 3;
    }

    /**
     * Returns true if fruit is fully ripe.
     */
    public boolean isMaxAge(BlockState state) {
        return state.getValue(AGE) >= getMaxAge();
    }

    /**
     * Right-click to harvest ripe fruit.
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (isMaxAge(state)) {
            if (!level.isClientSide) {
                // Drop fruit item
                ItemStack fruit = getFruitDrop(state.getValue(FRUIT));
                Block.popResource(level, pos.below(), fruit);

                // Reset age to 0
                level.setBlock(pos, state.setValue(AGE, 0), 2);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    /**
     * Get the item to drop when harvesting.
     */
    private ItemStack getFruitDrop(FruitType type) {
        return switch (type) {
            case APPLE -> new ItemStack(Items.APPLE, 1);
            case CIDER_APPLE -> new ItemStack(Items.APPLE, 1); // TODO: Replace with cider apple item
            default -> ItemStack.EMPTY;
        };
    }

    /**
     * Time-based growth tick - fruit ripens based on world time.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);

        // Calculate target age based on world time
        long worldTime = level.getDayTime() % 24000L;
        int targetAge = 0;
        if (worldTime > 3000L && worldTime < 5000L) {
            targetAge = 1;
        } else if (worldTime > 5000L && worldTime < 6000L) {
            targetAge = 2;
        } else if (worldTime > 6000L && worldTime < 10000L) {
            targetAge = 3;
        }

        int currentAge = state.getValue(AGE);
        int validPreviousAge = targetAge - 1;
        if (validPreviousAge < 0) {
            validPreviousAge = getMaxAge();
        }

        // Progress to next age if at the right stage
        if (currentAge == validPreviousAge && random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(AGE, targetAge), 2);
        }
    }

    // BonemealableBlock implementation
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
        level.setBlock(pos, state.setValue(AGE, getMaxAge()), 2);
    }

    /**
     * Fruit types.
     */
    public enum FruitType implements StringRepresentable {
        NONE(0, "none"),
        APPLE(1, "apple"),
        CIDER_APPLE(2, "cider_apple");

        private final int meta;
        private final String name;

        FruitType(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMeta() {
            return this.meta;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static FruitType fromMeta(int meta) {
            for (FruitType f : values()) {
                if (f.meta == meta) {
                    return f;
                }
            }
            return NONE;
        }
    }
}
