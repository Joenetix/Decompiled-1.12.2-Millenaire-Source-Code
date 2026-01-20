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
 * Mock block for tree spawn points in building plans.
 * Indicates where trees should be spawned during building construction.
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockTreeSpawn extends Block {

    public static final EnumProperty<TreeType> TREE = EnumProperty.create("tree", TreeType.class);

    public MockBlockTreeSpawn() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(TREE, TreeType.OAK));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TREE);
    }

    /**
     * Types of trees.
     */
    public enum TreeType implements StringRepresentable {
        OAK(0, "oak"),
        SPRUCE(1, "spruce"),
        BIRCH(2, "birch"),
        JUNGLE(3, "jungle"),
        ACACIA(4, "acacia"),
        DARK_OAK(5, "dark_oak"),
        CHERRY(6, "cherry");

        private final int meta;
        private final String name;

        TreeType(int meta, String name) {
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

        public static TreeType fromMeta(int meta) {
            for (TreeType t : values()) {
                if (t.meta == meta) {
                    return t;
                }
            }
            return OAK;
        }
    }
}

