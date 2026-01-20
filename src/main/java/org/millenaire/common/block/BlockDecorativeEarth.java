package org.millenaire.common.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Decorative earth block for Millénaire - dirt walls and similar.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BlockDecorativeEarth extends Block {

    public static final EnumProperty<EarthVariant> VARIANT = EnumProperty.create("variant", EarthVariant.class);

    public BlockDecorativeEarth() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.8F)
                .sound(SoundType.GRAVEL));
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, EarthVariant.DIRTWALL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    /**
     * Earth block variants.
     */
    public enum EarthVariant implements StringRepresentable {
        DIRTWALL(0, "dirtwall");

        private final int meta;
        private final String name;

        EarthVariant(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMetadata() {
            return this.meta;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static EarthVariant byMetadata(int meta) {
            EarthVariant[] values = values();
            if (meta < 0 || meta >= values.length) {
                meta = 0;
            }
            return values[meta];
        }
    }
}

