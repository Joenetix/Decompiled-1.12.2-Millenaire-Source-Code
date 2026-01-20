package org.millenaire.common.block;

import net.minecraft.util.StringRepresentable;

/**
 * Enum for stone decoration block variants.
 * Used by BlockDecorativeStone to determine texture/model.
 * 
 * Based on original Millénaire 1.12.2 stone_deco metadata values.
 */
public enum StoneDecoVariant implements StringRepresentable {
    MUDBRICK("mudbrick", 0),
    COOKEDBRICK("cookedbrick", 1),
    MAYAGOLD("mayagold", 2),
    BYZANTINE_MOSAIC_RED("byzantine_mosaic_red", 3),
    BYZANTINE_MOSAIC_BLUE("byzantine_mosaic_blue", 4),
    STUCCOBRICKS("stuccobricks", 5),
    CHISELEDSTUCCO("chiseledstucco", 6),
    MUDBRICK_SMOOTH("mudbrick_smooth", 7);

    private final String name;
    private final int legacyMeta;

    StoneDecoVariant(String name, int legacyMeta) {
        this.name = name;
        this.legacyMeta = legacyMeta;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int getLegacyMeta() {
        return legacyMeta;
    }

    /**
     * Get variant from legacy 1.12.2 metadata value.
     */
    public static StoneDecoVariant fromMeta(int meta) {
        for (StoneDecoVariant variant : values()) {
            if (variant.legacyMeta == meta) {
                return variant;
            }
        }
        return MUDBRICK; // Default fallback
    }
}

