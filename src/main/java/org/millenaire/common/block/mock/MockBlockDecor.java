package org.millenaire.common.block.mock;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Mock block for decorative elements in building plans.
 * Indicates decoration placement points.
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockDecor extends Block {

    public static final EnumProperty<DecorType> DECOR = EnumProperty.create("decor", DecorType.class);

    public MockBlockDecor() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(DECOR, DecorType.PICTURE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECOR);
    }

    /**
     * Types of decorations.
     */
    public enum DecorType implements StringRepresentable {
        PICTURE(0, "picture"),
        PLAQUE(1, "plaque"),
        DECORATION(2, "decoration"),
        BANNER(3, "banner"),
        STATUE(4, "statue");

        private final int meta;
        private final String name;

        DecorType(int meta, String name) {
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

        public static DecorType fromMeta(int meta) {
            for (DecorType d : values()) {
                if (d.meta == meta) {
                    return d;
                }
            }
            return PICTURE;
        }
    }
}

