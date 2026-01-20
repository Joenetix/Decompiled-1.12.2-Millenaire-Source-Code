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
 * Mock block for resource sources in building plans.
 * Indicates block that provides resources (chests, furnaces, etc.)
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockSource extends Block {

    public static final EnumProperty<SourceType> SOURCE = EnumProperty.create("source", SourceType.class);

    public MockBlockSource() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(SOURCE, SourceType.CHEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOURCE);
    }

    /**
     * Types of resource sources.
     */
    public enum SourceType implements StringRepresentable {
        CHEST(0, "chest"),
        FURNACE(1, "furnace"),
        DISPENSER(2, "dispenser"),
        BREWING_STAND(3, "brewingstand"),
        CRAFTING_TABLE(4, "craftingtable");

        private final int meta;
        private final String name;

        SourceType(int meta, String name) {
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

        public static SourceType fromMeta(int meta) {
            for (SourceType s : values()) {
                if (s.meta == meta) {
                    return s;
                }
            }
            return CHEST;
        }
    }
}

