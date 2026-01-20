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
 * Mock block for free resources in building plans.
 * Indicates that any block of the specified type can be used.
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockFree extends Block {

    public static final EnumProperty<FreeResource> RESOURCE = EnumProperty.create("resource", FreeResource.class);

    public MockBlockFree() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(RESOURCE, FreeResource.STONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RESOURCE);
    }

    /**
     * Types of free resources.
     */
    public enum FreeResource implements StringRepresentable {
        STONE(0, "stone"),
        SAND(1, "sand"),
        GRAVEL(2, "gravel"),
        SANDSTONE(3, "sandstone"),
        WOOL(4, "wool"),
        COBBLESTONE(5, "cobblestone"),
        STONEBRICK(6, "stonebrick"),
        PAINTEDBRICK(7, "paintedbrick"),
        GRASS_BLOCK(8, "grass_block");

        private final int meta;
        private final String name;

        FreeResource(int meta, String name) {
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

        public static FreeResource fromMeta(int meta) {
            for (FreeResource r : values()) {
                if (r.meta == meta) {
                    return r;
                }
            }
            return STONE;
        }
    }
}

