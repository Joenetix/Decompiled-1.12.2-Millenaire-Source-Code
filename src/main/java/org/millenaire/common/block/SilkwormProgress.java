package org.millenaire.common.block;

import net.minecraft.util.StringRepresentable;

public enum SilkwormProgress implements StringRepresentable {
    SILKWORMEMPTY("silkwormempty"),
    SILKWORMIP1("silkwormip1"),
    SILKWORMIP2("silkwormip2"),
    SILKWORMFULL("silkwormfull");

    private final String name;

    SilkwormProgress(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
