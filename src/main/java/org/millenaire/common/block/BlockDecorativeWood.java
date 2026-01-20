package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Multi-variant decorative wood block for Millénaire buildings.
 * 
 * Supports multiple textures via variant property:
 * - timberframeplain (colombages plain)
 * - timberframecross (colombages with X pattern)
 * - thatch (straw roofing)
 * - beehive
 * - dirtwall
 * - halftimber variants
 */
public class BlockDecorativeWood extends Block {

    public static final EnumProperty<WoodDecoVariant> VARIANT = EnumProperty.create("variant", WoodDecoVariant.class);

    public BlockDecorativeWood(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VARIANT, WoodDecoVariant.THATCH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    /**
     * Get blockstate for a specific variant.
     */
    public BlockState getStateForVariant(WoodDecoVariant variant) {
        return this.defaultBlockState().setValue(VARIANT, variant);
    }

    /**
     * Get blockstate from legacy 1.12.2 metadata value.
     */
    public BlockState getStateFromMeta(int meta) {
        return getStateForVariant(WoodDecoVariant.fromMeta(meta));
    }
}

