package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Snail soil block for Millénaire.
 * Snails grow under water with air above.
 * Used for escargot production (purple dye).
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockSnailSoil extends Block {

    public static final EnumProperty<ProgressType> PROGRESS = EnumProperty.create("progress", ProgressType.class);

    public BlockSnailSoil() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.5F)
                .sound(SoundType.GRAVEL)
                .randomTicks());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PROGRESS, ProgressType.EMPTY));
    }

    public BlockSnailSoil(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PROGRESS, ProgressType.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROGRESS);
    }

    /**
     * Growth tick - snails progress when underwater with air above.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentProgress = state.getValue(PROGRESS).getMeta();

        if (currentProgress < 3) {
            // Check for water directly above and air above the water
            BlockState above = level.getBlockState(pos.above());
            BlockState twoAbove = level.getBlockState(pos.above().above());

            boolean waterAbove = above.is(Blocks.WATER);
            boolean airAboveWater = twoAbove.isAir();

            if (waterAbove && airAboveWater && random.nextInt(2) == 0) {
                ProgressType newProgress = ProgressType.byMeta(currentProgress + 1);
                level.setBlock(pos, state.setValue(PROGRESS, newProgress), 2);
            }
        }
    }

    /**
     * Check if snails are ready to harvest.
     */
    public boolean isReadyToHarvest(BlockState state) {
        return state.getValue(PROGRESS) == ProgressType.FULL;
    }

    /**
     * Snail progress stages enum.
     */
    public enum ProgressType implements StringRepresentable {
        EMPTY(0, "snail_soil_empty"),
        IP1(1, "snail_soil_ip1"),
        IP2(2, "snail_soil_ip2"),
        FULL(3, "snail_soil_full");

        private static final ProgressType[] BY_META = new ProgressType[values().length];
        private final int meta;
        private final String name;

        ProgressType(int meta, String name) {
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

        public static ProgressType byMeta(int meta) {
            if (meta < 0 || meta >= BY_META.length) {
                meta = 0;
            }
            return BY_META[meta];
        }

        static {
            for (ProgressType type : values()) {
                BY_META[type.meta] = type;
            }
        }
    }
}
