package org.millenaire.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Multi-variant decorative stone block for Millénaire buildings.
 * 
 * Supports multiple textures via variant property:
 * - mudbrick (Norman villages)
 * - cookedbrick (fired brick)
 * - mayagold (Mayan gold blocks)
 * - byzantine_mosaic_red/blue (Byzantine culture)
 * - stuccobricks, chiseledstucco
 */
public class BlockDecorativeStone extends Block {

    public static final EnumProperty<StoneDecoVariant> VARIANT = EnumProperty.create("variant", StoneDecoVariant.class);

    public BlockDecorativeStone(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(VARIANT, StoneDecoVariant.MUDBRICK));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    /**
     * Get blockstate for a specific variant.
     */
    public BlockState getStateForVariant(StoneDecoVariant variant) {
        return this.defaultBlockState().setValue(VARIANT, variant);
    }

    /**
     * Get blockstate from legacy 1.12.2 metadata value.
     */
    public BlockState getStateFromMeta(int meta) {
        return getStateForVariant(StoneDecoVariant.fromMeta(meta));
    }
}

