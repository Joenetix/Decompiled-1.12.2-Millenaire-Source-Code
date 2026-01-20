package org.millenaire.common.block.mock;

import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import org.joml.Vector3f;

/**
 * Marker block for building plans - indicates special positions.
 * Used by content creators to define sleeping, selling, crafting positions,
 * etc.
 * Ported from 1.12.2 to 1.20.1.
 */
public class MockBlockMarker extends Block {

    public static final EnumProperty<MarkerType> VARIANT = EnumProperty.create("variant", MarkerType.class);

    public MockBlockMarker() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(-1.0F, 3600000.0F)
                .noLootTable()
                .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(VARIANT, MarkerType.PRESERVE_GROUND));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        MarkerType type = state.getValue(VARIANT);
        if (type == MarkerType.PRESERVE_GROUND) {
            return;
        }

        int color = type.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        level.addParticle(
                new DustParticleOptions(new Vector3f(r, g, b), 1.0F),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                0, 0, 0);
    }

    /**
     * Types of marker positions.
     */
    public enum MarkerType implements StringRepresentable {
        PRESERVE_GROUND(0, "preserveground", 0x000000),
        SLEEPING_POS(1, "sleepingpos", 0xDFFF94),
        SELLING_POS(2, "sellingpos", 0x00FF9C),
        CRAFTING_POS(3, "craftingpos", 0x11AC00),
        DEFENDING_POS(4, "defendingpos", 0xFF0000),
        SHELTER_POS(5, "shelterpos", 0x7F0097),
        PATH_START_POS(6, "pathstartpos", 0x0B0056),
        LEISURE_POS(7, "leisurepos", 0xF0A500),
        STALL(8, "stall", 0x96A200),
        BRICK_SPOT(9, "brickspot", 0x870C00),
        HEALING_SPOT(10, "healingspot", 0x00D200),
        FISHING_SPOT(11, "fishingspot", 0x000078);

        private final int meta;
        private final String name;
        private final int color;

        MarkerType(int meta, String name, int color) {
            this.meta = meta;
            this.name = name;
            this.color = color;
        }

        public int getMeta() {
            return this.meta;
        }

        public int getColor() {
            return this.color;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static MarkerType fromMeta(int meta) {
            for (MarkerType type : values()) {
                if (type.meta == meta) {
                    return type;
                }
            }
            return PRESERVE_GROUND;
        }

        public static int getMetaFromName(String name) {
            for (MarkerType type : values()) {
                if (type.name.equalsIgnoreCase(name)) {
                    return type.meta;
                }
            }
            return -1;
        }
    }
}

