package org.millenaire.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Silkworm block for Millénaire.
 * Used for silk farming in various cultures.
 * Progresses through lifecycle stages in low light.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockSilkWorm extends Block {

    public static final EnumProperty<ProgressType> PROGRESS = EnumProperty.create("progress", ProgressType.class);

    public BlockSilkWorm() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOL)
                .strength(2.0F, 5.0F)
                .sound(SoundType.WOOD)
                .randomTicks()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PROGRESS, ProgressType.EMPTY));
    }

    public BlockSilkWorm(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(PROGRESS, ProgressType.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PROGRESS);
    }

    /**
     * Growth tick - silkworms progress in low light conditions.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int currentProgress = state.getValue(PROGRESS).getMeta();

        // Silkworms grow in dark/dim conditions (light level < 7)
        if (currentProgress < 3 && level.getRawBrightness(pos.above(), 0) < 7 && random.nextInt(2) == 0) {
            ProgressType newProgress = ProgressType.byMeta(currentProgress + 1);
            level.setBlock(pos, state.setValue(PROGRESS, newProgress), 2);
        }
    }

    /**
     * Check if silkworms are ready to harvest.
     */
    public boolean isReadyToHarvest(BlockState state) {
        return state.getValue(PROGRESS) == ProgressType.FULL;
    }

    /**
     * Silkworm progress stages enum.
     */
    public enum ProgressType implements StringRepresentable {
        EMPTY(0, "silkwormempty"),
        IP1(1, "silkwormip1"),
        IP2(2, "silkwormip2"),
        FULL(3, "silkwormfull");

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
