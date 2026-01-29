package org.millenaire.common.block;

import net.minecraft.util.StringRepresentable;

public enum SnailSoilProgress implements StringRepresentable {
    SNAIL_SOIL_EMPTY("snail_soil_empty"),
    SNAIL_SOIL_IP1("snail_soil_ip1"),
    SNAIL_SOIL_IP2("snail_soil_ip2"),
    SNAIL_SOIL_FULL("snail_soil_full");

    private final String name;

    SnailSoilProgress(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
