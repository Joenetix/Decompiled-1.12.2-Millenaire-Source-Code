package org.millenaire.worldgen;

/**
 * Special block types for terrain and special block handling during building
 * placement.
 * Ported from original Millénaire BuildingBlock.java special flags.
 * 
 * These types define how specific blocks should be placed or how terrain should
 * be modified.
 */
public enum SpecialBlockType {
    /** Normal block - no special handling */
    NONE(0),

    /** Tapestry wall decoration */
    TAPESTRY(1),

    /** Spawn oak tree */
    OAKSPAWN(2),

    /** Spawn pine/spruce tree */
    PINESPAWN(3),

    /** Spawn birch tree */
    BIRCHSPAWN(4),

    /** Indian statue decoration */
    INDIANSTATUE(5),

    /** Preserve or place ground at depth (dirt) */
    PRESERVEGROUNDDEPTH(6),

    /** Clear tree blocks (logs and leaves) */
    CLEARTREE(7),

    /** Mayan statue decoration */
    MAYANSTATUE(8),

    /** Mob spawner - skeleton */
    SPAWNERSKELETON(9),

    /** Mob spawner - zombie */
    SPAWNERZOMBIE(10),

    /** Mob spawner - spider */
    SPAWNERSPIDER(11),

    /** Mob spawner - cave spider */
    SPAWNERCAVESPIDER(12),

    /** Mob spawner - creeper */
    SPAWNERCREEPER(13),

    /** Dispenser with unknown powder */
    DISPENDERUNKNOWNPOWDER(14),

    /** Spawn jungle tree */
    JUNGLESPAWN(15),

    /** Door with inverted hinge (right side) */
    INVERTED_DOOR(16),

    /** Clear blocks inside building footprint - replaces with air */
    CLEARGROUND(17),

    /** Byzantine icon small */
    BYZANTINEICONSMALL(18),

    /** Byzantine icon medium */
    BYZANTINEICONMEDIUM(19),

    /** Byzantine icon large */
    BYZANTINEICONLARGE(20),

    /** Preserve or place ground at surface level (grass_block) */
    PRESERVEGROUNDSURFACE(21),

    /** Mob spawner - blaze */
    SPAWNERBLAZE(22),

    /** Spawn acacia tree */
    ACACIASPAWN(23),

    /** Spawn dark oak tree */
    DARKOAKSPAWN(24),

    /** Torch with guessed facing */
    TORCHGUESS(25),

    /** Chest with guessed facing */
    CHESTGUESS(26),

    /** Furnace with guessed facing */
    FURNACEGUESS(27),

    /** Clear blocks outside building (preserves leaves) */
    CLEARGROUNDOUTSIDEBUILDING(28),

    /** Hide wall hanging decoration */
    HIDEHANGING(29),

    /** Spawn apple tree */
    APPLETREESPAWN(30),

    /** Border clearing - handles edges near water specially */
    CLEARGROUNDBORDER(31),

    /** Spawn olive tree */
    OLIVETREESPAWN(32),

    /** Spawn pistachio tree */
    PISTACHIOTREESPAWN(33),

    /** Wall carpet small */
    WALLCARPETSMALL(40),

    /** Wall carpet medium */
    WALLCARPETMEDIUM(41),

    /** Wall carpet large */
    WALLCARPETLARGE(42),

    /** Spawn cherry tree */
    CHERRYTREESPAWN(43),

    /** Spawn sakura tree */
    SAKURATREESPAWN(44);

    public final int value;

    SpecialBlockType(int value) {
        this.value = value;
    }

    /**
     * Get SpecialBlockType from int value.
     */
    public static SpecialBlockType fromValue(int value) {
        for (SpecialBlockType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return NONE;
    }

    /**
     * Check if this is a spawner type.
     */
    public boolean isSpawner() {
        return this == SPAWNERSKELETON || this == SPAWNERZOMBIE
                || this == SPAWNERSPIDER || this == SPAWNERCAVESPIDER
                || this == SPAWNERCREEPER || this == SPAWNERBLAZE;
    }

    /**
     * Check if this is a tree spawn type.
     */
    public boolean isTreeSpawn() {
        return this == OAKSPAWN || this == PINESPAWN || this == BIRCHSPAWN
                || this == JUNGLESPAWN || this == ACACIASPAWN || this == DARKOAKSPAWN
                || this == APPLETREESPAWN || this == OLIVETREESPAWN || this == PISTACHIOTREESPAWN
                || this == CHERRYTREESPAWN || this == SAKURATREESPAWN;
    }

    /**
     * Check if this is a picture/decoration type.
     */
    public boolean isPicture() {
        return this == TAPESTRY || this == INDIANSTATUE || this == MAYANSTATUE
                || this == BYZANTINEICONSMALL || this == BYZANTINEICONMEDIUM || this == BYZANTINEICONLARGE
                || this == HIDEHANGING || this == WALLCARPETSMALL || this == WALLCARPETMEDIUM
                || this == WALLCARPETLARGE;
    }

    /**
     * Check if this is a ground clearing type.
     */
    public boolean isClearGround() {
        return this == CLEARGROUND || this == CLEARGROUNDOUTSIDEBUILDING || this == CLEARGROUNDBORDER;
    }

    /**
     * Check if this is a ground preservation type.
     */
    public boolean isPreserveGround() {
        return this == PRESERVEGROUNDSURFACE || this == PRESERVEGROUNDDEPTH;
    }
}
