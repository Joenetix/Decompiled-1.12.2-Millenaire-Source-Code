package org.millenaire.common.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory class for creating painted brick blocks in all 16 dye colors.
 * Each color has 5 block types: block, decorated, stairs, slab, and wall.
 */
public class PaintedBrickBlocks {

    // Maps for all painted brick variants by color
    public static final Map<DyeColor, Supplier<Block>> PAINTED_BRICKS = new HashMap<>();
    public static final Map<DyeColor, Supplier<Block>> PAINTED_BRICKS_DECORATED = new HashMap<>();
    public static final Map<DyeColor, Supplier<Block>> PAINTED_BRICK_STAIRS = new HashMap<>();
    public static final Map<DyeColor, Supplier<Block>> PAINTED_BRICK_SLABS = new HashMap<>();
    public static final Map<DyeColor, Supplier<Block>> PAINTED_BRICK_WALLS = new HashMap<>();

    /**
     * Get the MapColor for a given DyeColor.
     */
    public static MapColor getMapColor(DyeColor color) {
        return switch (color) {
            case WHITE -> MapColor.SNOW;
            case ORANGE -> MapColor.COLOR_ORANGE;
            case MAGENTA -> MapColor.COLOR_MAGENTA;
            case LIGHT_BLUE -> MapColor.COLOR_LIGHT_BLUE;
            case YELLOW -> MapColor.COLOR_YELLOW;
            case LIME -> MapColor.COLOR_LIGHT_GREEN;
            case PINK -> MapColor.COLOR_PINK;
            case GRAY -> MapColor.COLOR_GRAY;
            case LIGHT_GRAY -> MapColor.COLOR_LIGHT_GRAY;
            case CYAN -> MapColor.COLOR_CYAN;
            case PURPLE -> MapColor.COLOR_PURPLE;
            case BLUE -> MapColor.COLOR_BLUE;
            case BROWN -> MapColor.COLOR_BROWN;
            case GREEN -> MapColor.COLOR_GREEN;
            case RED -> MapColor.COLOR_RED;
            case BLACK -> MapColor.COLOR_BLACK;
        };
    }

    /**
     * Create a basic painted brick block for a color.
     */
    public static Block createPaintedBrick(DyeColor color) {
        return new Block(BlockBehaviour.Properties.of()
                .mapColor(getMapColor(color))
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    /**
     * Create a decorated painted brick block (has pattern overlay).
     */
    public static Block createDecoratedPaintedBrick(DyeColor color) {
        return new Block(BlockBehaviour.Properties.of()
                .mapColor(getMapColor(color))
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    /**
     * Create painted brick stairs.
     */
    public static StairBlock createPaintedBrickStairs(DyeColor color, Supplier<Block> baseBlock) {
        return new StairBlock(
                () -> baseBlock.get().defaultBlockState(),
                BlockBehaviour.Properties.of()
                        .mapColor(getMapColor(color))
                        .strength(2.0F, 6.0F)
                        .sound(SoundType.STONE)
                        .requiresCorrectToolForDrops());
    }

    /**
     * Create painted brick slabs.
     */
    public static SlabBlock createPaintedBrickSlab(DyeColor color) {
        return new SlabBlock(BlockBehaviour.Properties.of()
                .mapColor(getMapColor(color))
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    /**
     * Create painted brick walls.
     */
    public static WallBlock createPaintedBrickWall(DyeColor color) {
        return new WallBlock(BlockBehaviour.Properties.of()
                .mapColor(getMapColor(color))
                .strength(2.0F, 6.0F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops());
    }

    /**
     * Get registry name for a painted brick block.
     */
    public static String getRegistryName(DyeColor color, String suffix) {
        return "painted_brick_" + color.getName() + (suffix.isEmpty() ? "" : "_" + suffix);
    }

    /**
     * Get all colors as an array for iteration.
     */
    public static DyeColor[] getAllColors() {
        return DyeColor.values();
    }
}

