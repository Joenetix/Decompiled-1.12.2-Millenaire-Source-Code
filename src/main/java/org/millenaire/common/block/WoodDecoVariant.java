package org.millenaire.common.block;

import net.minecraft.util.StringRepresentable;

/**
 * Enum for wood decoration block variants.
 * Used by BlockDecorativeWood to determine texture/model.
 * 
 * Based on original Millénaire 1.12.2 wood_deco metadata values.
 */
public enum WoodDecoVariant implements StringRepresentable {
    TIMBERFRAME_PLAIN("timberframeplain", 0),
    TIMBERFRAME_CROSS("timberframecross", 1),
    THATCH("thatch", 2),
    BEEHIVE("beehive", 3),
    DIRTWALL("dirtwall", 4),
    HALFTIMBER("halftimber", 5),
    HALFTIMBER_2("halftimber2", 6),
    HALFTIMBER_3("halftimber3", 7);

    private final String name;
    private final int legacyMeta;

    WoodDecoVariant(String name, int legacyMeta) {
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
    public static WoodDecoVariant fromMeta(int meta) {
        for (WoodDecoVariant variant : values()) {
            if (variant.legacyMeta == meta) {
                return variant;
            }
        }
        return THATCH; // Default fallback
    }
}

