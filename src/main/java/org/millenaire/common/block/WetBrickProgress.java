package org.millenaire.common.block;

import net.minecraft.util.StringRepresentable;

public enum WetBrickProgress implements StringRepresentable {
    WETBRICK0("wetbrick0"),
    WETBRICK1("wetbrick1"),
    WETBRICK2("wetbrick2");

    private final String name;

    WetBrickProgress(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
