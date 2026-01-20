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
 * Mock block for soil and terrain in building plans.
 * Indicates terrain type requirements.
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockSoil extends Block {

    public static final EnumProperty<SoilType> SOIL = EnumProperty.create("soil", SoilType.class);

    public MockBlockSoil() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(SOIL, SoilType.DIRT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOIL);
    }

    /**
     * Types of soil.
     */
    public enum SoilType implements StringRepresentable {
        DIRT(0, "dirt"),
        FARMLAND(1, "farmland"),
        GRASS(2, "grass"),
        SAND(3, "sand"),
        GRAVEL(4, "gravel"),
        CLAY(5, "clay");

        private final int meta;
        private final String name;

        SoilType(int meta, String name) {
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

        public static SoilType fromMeta(int meta) {
            for (SoilType s : values()) {
                if (s.meta == meta) {
                    return s;
                }
            }
            return DIRT;
        }
    }
}

