package org.millenaire.common.block;

import net.minecraft.util.StringRepresentable;

public enum MudBrickVariant implements StringRepresentable {
    MUDBRICK_SMOOTH("mudbrick_smooth"),
    MUDBRICK_SELJUK_ORNAMENTED("mudbrick_seljuk_ornamented"),
    MUDBRICK_SELJUK_DECORATED("mudbrick_seljuk_decorated");

    private final String name;

    MudBrickVariant(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
