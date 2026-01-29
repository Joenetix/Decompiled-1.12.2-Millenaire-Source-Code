package org.millenaire.core;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.VineBlock; // Added by user instruction
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.millenaire.MillenaireRevived;
import org.millenaire.core.block.PanelBlock; // Added by user instruction

/**
 * Complete block registry for Millénaire 8.1.2 content.
 * 
 * Registers ALL custom blocks from the original mod organized by culture:
 * - Core decorative blocks (wood_deco, stone_deco, earth_deco) - foundation of
 * all buildings
 * - Norman: Timber frames, mud brick, thatch, cooked brick
 * - Indian: Sandstone variants, silk worms, wet brick
 * - Japanese: Gray/green/red tiles, paper walls, cherry/sakura trees
 * - Mayan: Maize, cotton crops
 * - Byzantine: Byzantine tiles, olive/pistachio trees, grape vines
 * - Shared: Paths, painted bricks, crops, functional blocks
 * 
 * Phase 1: Registration only - blocks are visible but non-functional
 * Phase 2+: Building schematics will use these blocks
 */
public class MillBlocks {

        public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
                        ForgeRegistries.BLOCKS,
                        MillenaireRevived.MODID);

        // =============================================================================
        // CORE DECORATIVE BLOCKS - Foundation of all cultural buildings
        // =============================================================================

        // TODO: Implement BlockDecorativeWood with 16 variants (TIMBERFRAMEPLAIN,
        // TIMBERFRAMECROSS, DIRTWALL, THATCH, etc.)
        // IMPLEMENTED: Now using BlockDecorativeWood with EnumProperty<WoodDecoVariant>
        public static final RegistryObject<Block> WOOD_DECORATION = BLOCKS.register("wood_deco",
                        () -> new org.millenaire.common.block.BlockDecorativeWood(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(2.0F, 3.0F)
                                        .sound(SoundType.WOOD)));

        // IMPLEMENTED: Now using BlockDecorativeStone with
        // EnumProperty<StoneDecoVariant>
        public static final RegistryObject<Block> STONE_DECORATION = BLOCKS.register("stone_deco",
                        () -> new org.millenaire.common.block.BlockDecorativeStone(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        // TODO: Implement BlockDecorativeEarth with 8 variants
        // IMPLEMENTED: Now using BlockDecorativeEarth with EarthVariant
        public static final RegistryObject<Block> EARTH_DECORATION = BLOCKS.register("earth_deco",
                        () -> new org.millenaire.common.block.BlockDecorativeEarth());

        // =============================================================================
        // NORMAN CULTURE BLOCKS
        // =============================================================================

        // Extended mud brick variants
        public static final RegistryObject<Block> EXTENDED_MUD_BRICK = BLOCKS.register("extended_mud_brick",
                        () -> new org.millenaire.common.block.BlockExtendedMudBrick(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        // Walls
        public static final RegistryObject<Block> WALL_MUD_BRICK = BLOCKS.register("wall_mud_brick",
                        () -> new WallBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        // Stairs - Norman (using vanilla StairBlock with custom appearance)
        public static final RegistryObject<Block> STAIRS_TIMBERFRAME = BLOCKS.register("stairs_timberframe",
                        () -> new StairBlock(() -> Blocks.OAK_PLANKS.defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.WOOD)
                                                        .strength(2.0F, 3.0F)
                                                        .sound(SoundType.WOOD)));

        public static final RegistryObject<Block> STAIRS_MUDBRICK = BLOCKS.register("stairs_mudbrick",
                        () -> new StairBlock(() -> Blocks.BRICKS.defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.DIRT)
                                                        .strength(2.0F, 10.0F)
                                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> STAIRS_COOKEDBRICK = BLOCKS.register("stairs_cookedbrick",
                        () -> new StairBlock(() -> Blocks.BRICKS.defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.COLOR_RED)
                                                        .strength(2.0F, 10.0F)
                                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> STAIRS_THATCH = BLOCKS.register("stairs_thatch",
                        () -> new StairBlock(() -> Blocks.HAY_BLOCK.defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.COLOR_YELLOW)
                                                        .strength(0.5F)
                                                        .sound(SoundType.GRASS)));

        // Slabs - Wood and Stone decoration slabs
        public static final RegistryObject<Block> SLAB_WOOD_DECO = BLOCKS.register("slab_wood_deco",
                        () -> new SlabBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(2.0F, 3.0F)
                                        .sound(SoundType.WOOD)));

        public static final RegistryObject<Block> SLAB_STONE_DECO = BLOCKS.register("slab_stone_deco",
                        () -> new SlabBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        // Norman trees - Apple
        // TODO: Implement BlockMillSapling and BlockFruitLeaves
        // public static final RegistryObject<Block> SAPLING_APPLETREE =
        // BLOCKS.register("sapling_appletree", ...);
        // public static final RegistryObject<Block> LEAVES_APPLETREE =
        // BLOCKS.register("leaves_appletree", ...);

        // Bed - Straw (Norman) - Uses vanilla bed mechanics with custom texture
        public static final RegistryObject<Block> BED_STRAW = BLOCKS.register("bed_straw",
                        () -> new net.minecraft.world.level.block.BedBlock(
                                        net.minecraft.world.item.DyeColor.YELLOW,
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.COLOR_YELLOW)
                                                        .sound(SoundType.WOOD)
                                                        .strength(0.2F)
                                                        .noOcclusion()));

        // =============================================================================
        // INDIAN CULTURE BLOCKS
        // =============================================================================

        // Sandstone variants - Indian/Byzantine
        public static final RegistryObject<Block> SANDSTONE_CARVED = BLOCKS.register("sandstone_carved",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SAND)
                                        .strength(0.8F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> SANDSTONE_RED_CARVED = BLOCKS.register("sandstone_red_carved",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_ORANGE)
                                        .strength(0.8F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> SANDSTONE_OCHRE_CARVED = BLOCKS.register("sandstone_ochre_carved",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_YELLOW)
                                        .strength(0.8F)
                                        .sound(SoundType.STONE)));

        // Sandstone stairs (TODO: implement proper stairs)
        // STAIRS_SANDSTONE_CARVED, STAIRS_SANDSTONE_RED_CARVED,
        // STAIRS_SANDSTONE_OCHRE_CARVED

        // Sandstone slabs (TODO: implement proper slabs)
        // SLAB_SANDSTONE_CARVED, SLAB_SANDSTONE_RED_CARVED, SLAB_SANDSTONE_OCHRE_CARVED

        // Sandstone walls (TODO: implement proper walls)
        // WALL_SANDSTONE_CARVED, WALL_SANDSTONE_RED_CARVED, WALL_SANDSTONE_OCHRE_CARVED

        // Special Indian blocks
        public static final RegistryObject<Block> WET_BRICK = BLOCKS.register("wet_brick",
                        () -> new org.millenaire.common.block.BlockWetBrick(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .strength(0.5F)
                                        .sound(SoundType.GRAVEL)));

        public static final RegistryObject<Block> SILK_WORM = BLOCKS.register("silk_worm",
                        () -> new org.millenaire.common.block.BlockSilkworm(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(0.5F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOD)));

        public static final RegistryObject<Block> SNAIL_SOIL = BLOCKS.register("snail_soil",
                        () -> new org.millenaire.common.block.BlockSnailSoil(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .strength(0.5F)
                                        .sound(SoundType.SAND)));

        // Bed - Charpoy (Indian) - Uses vanilla bed mechanics with custom texture
        public static final RegistryObject<Block> BED_CHARPOY = BLOCKS.register("bed_charpoy",
                        () -> new net.minecraft.world.level.block.BedBlock(
                                        net.minecraft.world.item.DyeColor.BROWN,
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.COLOR_BROWN)
                                                        .sound(SoundType.WOOD)
                                                        .strength(0.2F)
                                                        .noOcclusion()));

        // =============================================================================
        // JAPANESE CULTURE BLOCKS
        // =============================================================================

        // Japanese tiles - TODO: Implement BlockOrientedSlab for proper oriented tile
        // blocks
        public static final RegistryObject<Block> GRAY_TILES = BLOCKS.register("gray_tiles",
                        () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> GREEN_TILES = BLOCKS.register("green_tiles",
                        () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_GREEN)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> RED_TILES = BLOCKS.register("red_tiles",
                        () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_RED)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        // Japanese tile slabs - Using BlockOrientedSlab for directional placement
        public static final RegistryObject<Block> GRAY_TILES_SLAB = BLOCKS.register("gray_tiles_slab",
                        () -> new org.millenaire.common.block.BlockOrientedSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> GREEN_TILES_SLAB = BLOCKS.register("green_tiles_slab",
                        () -> new org.millenaire.common.block.BlockOrientedSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_GREEN)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> RED_TILES_SLAB = BLOCKS.register("red_tiles_slab",
                        () -> new org.millenaire.common.block.BlockOrientedSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_RED)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        // Japanese tile stairs
        public static final RegistryObject<Block> STAIRS_GRAY_TILES = BLOCKS.register("stairs_gray_tiles",
                        () -> new StairBlock(() -> Blocks.STONE.defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.STONE)
                                                        .strength(2.0F, 10.0F)
                                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> STAIRS_GREEN_TILES = BLOCKS.register("stairs_green_tiles",
                        () -> new StairBlock(() -> Blocks.STONE.defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.COLOR_GREEN)
                                                        .strength(2.0F, 10.0F)
                                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> STAIRS_RED_TILES = BLOCKS.register("stairs_red_tiles",
                        () -> new StairBlock(() -> Blocks.STONE.defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.COLOR_RED)
                                                        .strength(2.0F, 10.0F)
                                                        .sound(SoundType.STONE)));

        // Japanese paper wall - Translucent pane-like block for sliding doors
        public static final RegistryObject<Block> PAPER_WALL = BLOCKS.register("paper_wall",
                        () -> new IronBarsBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SNOW)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOL)));

        // Japanese bars
        // Wooden bars - use IronBarsBlock to render as panes with transparent centers
        public static final RegistryObject<Block> WOODEN_BARS = BLOCKS.register("wooden_bars",
                        () -> new IronBarsBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOD)));

        public static final RegistryObject<Block> WOODEN_BARS_DARK = BLOCKS.register("wooden_bars_dark",
                        () -> new IronBarsBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_BROWN)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOD)));

        // Japanese trees - Cherry and Sakura - TODO: Implement custom sapling/leaves
        // SAPLING_CHERRY, CHERRY_LEAVES, SAPLING_SAKURA, SAKURA_LEAVES

        // =============================================================================
        // MAYAN CULTURE BLOCKS
        // =============================================================================

        // Mayan blocks are mostly painted brick variants (registered below)
        // Crops are in shared crops section

        // =============================================================================
        // BYZANTINE CULTURE BLOCKS
        // =============================================================================

        // Byzantine tiles
        public static final RegistryObject<Block> BYZANTINE_TILES = BLOCKS.register("byzantine_tiles",
                        () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_RED)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> BYZANTINE_TILES_SLAB = BLOCKS.register("byzantine_tiles_slab",
                        () -> new org.millenaire.common.block.BlockOrientedSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_RED)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> STAIRS_BYZ_TILES = BLOCKS.register("stairs_byz_tiles",
                        () -> new StairBlock(() -> BYZANTINE_TILES.get().defaultBlockState(),
                                        BlockBehaviour.Properties.of()
                                                        .mapColor(MapColor.COLOR_RED)
                                                        .strength(2.0F, 10.0F)
                                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> BYZANTINE_STONE_TILES = BLOCKS.register("byzantine_stone_tiles",
                        () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(2.0F, 6.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> BYZANTINE_SANDSTONE_TILES = BLOCKS.register(
                        "byzantine_sandstone_tiles",
                        () -> new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SAND)
                                        .strength(2.0F, 6.0F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> BYZANTINE_STONE_ORNAMENT = BLOCKS.register("byzantine_stone_ornament",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(1.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> BYZANTINE_SANDSTONE_ORNAMENT = BLOCKS.register(
                        "byzantine_sandstone_ornament",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SAND)
                                        .strength(1.5F)
                                        .sound(SoundType.STONE)));

        // Byzantine bars
        public static final RegistryObject<Block> WOODEN_BARS_INDIAN = BLOCKS.register("wooden_bars_indian",
                        () -> new IronBarsBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOD)));

        public static final RegistryObject<Block> WOODEN_BARS_ROSETTE = BLOCKS.register("wooden_bars_rosette",
                        () -> new IronBarsBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOD)));

        // Byzantine trees - Olive and Pistachio
        public static final RegistryObject<Block> LEAVES_OLIVETREE = BLOCKS.register("leaves_olivetree",
                        () -> new org.millenaire.common.block.BlockOliveLeaves(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.PLANT)
                                        .strength(0.2F)
                                        .sound(SoundType.GRASS)
                                        .noOcclusion()));

        public static final RegistryObject<Block> SAPLING_OLIVETREE = BLOCKS.register("sapling_olivetree",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.PLANT)
                                        .noCollission()
                                        .instabreak()
                                        .sound(SoundType.GRASS)));

        public static final RegistryObject<Block> LEAVES_PISTACHIO = BLOCKS.register("leaves_pistachio",
                        () -> new org.millenaire.common.block.BlockOliveLeaves(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.PLANT)
                                        .strength(0.2F)
                                        .sound(SoundType.GRASS)
                                        .noOcclusion()));

        public static final RegistryObject<Block> SAPLING_PISTACHIO = BLOCKS.register("sapling_pistachio",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.PLANT)
                                        .noCollission()
                                        .instabreak()
                                        .sound(SoundType.GRASS)));

        // =============================================================================
        // INUIT CULTURE BLOCKS - Arctic/Arctic structures
        // =============================================================================

        // Ice and snow building blocks
        public static final RegistryObject<Block> ICEBRICK = BLOCKS.register("icebrick",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.ICE)
                                        .strength(0.5F)
                                        .friction(0.98F)
                                        .sound(SoundType.GLASS)));

        public static final RegistryObject<Block> SNOWBRICK = BLOCKS.register("snowbrick",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SNOW)
                                        .strength(0.4F)
                                        .sound(SoundType.SNOW)));

        public static final RegistryObject<Block> SNOWWALL = BLOCKS.register("snowwall",
                        () -> new WallBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SNOW)
                                        .strength(0.3F)
                                        .sound(SoundType.SNOW)));

        // Inuit decorative blocks
        public static final RegistryObject<Block> INUITCARVING = BLOCKS.register("inuitcarving",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.TERRACOTTA_WHITE)
                                        .strength(1.0F)
                                        .sound(SoundType.BONE_BLOCK)));

        // Fire pit - functional block for outdoor cooking in villages
        public static final RegistryObject<Block> FIRE_PIT = BLOCKS.register("fire_pit",
                        () -> new org.millenaire.common.block.BlockFirePit(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(2.0F)
                                        .noOcclusion()
                                        .sound(SoundType.STONE)));

        // Hide hanging decoration
        public static final RegistryObject<Block> HIDEHANGING = BLOCKS.register("hidehanging",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_BROWN)
                                        .strength(0.5F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOL)));

        // =============================================================================
        // SHARED CROPS - Using BlockMillCrops with proper growth mechanics
        // =============================================================================

        public static final RegistryObject<Block> CROP_RICE = BLOCKS.register("crop_rice",
                        () -> org.millenaire.common.block.BlockMillCrops.rice(() -> MillItems.RICE.get()));

        public static final RegistryObject<Block> CROP_TURMERIC = BLOCKS.register("crop_turmeric",
                        () -> org.millenaire.common.block.BlockMillCrops.turmeric(() -> MillItems.TURMERIC.get()));

        public static final RegistryObject<Block> CROP_MAIZE = BLOCKS.register("crop_maize",
                        () -> org.millenaire.common.block.BlockMillCrops.maize(() -> MillItems.MAIZE.get()));

        public static final RegistryObject<Block> CROP_COTTON = BLOCKS.register("crop_cotton",
                        () -> org.millenaire.common.block.BlockMillCrops.cotton(() -> MillItems.COTTON.get()));

        public static final RegistryObject<Block> CROP_VINE = BLOCKS.register("crop_vine",
                        () -> org.millenaire.common.block.BlockMillCrops.vine(() -> MillItems.GRAPES.get()));

        // =============================================================================
        // SHARED DECORATIVE
        // =============================================================================

        // Stained glass - uses StainedGlassPaneBlock for proper translucent pane
        // rendering
        public static final RegistryObject<Block> STAINED_GLASS = BLOCKS.register("stained_glass",
                        () -> new StainedGlassPaneBlock(DyeColor.WHITE, BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.NONE)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .isViewBlocking((state, world, pos) -> false)
                                        .isSuffocating((state, world, pos) -> false)
                                        .sound(SoundType.GLASS)));

        // Rosette
        public static final RegistryObject<Block> ROSETTE = BLOCKS.register("rosette",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOD)));

        // Panel
        // Panel
        public static final RegistryObject<Block> PANEL = BLOCKS.register("panel",
                        () -> new PanelBlock(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(0.3F)
                                        .noOcclusion()
                                        .sound(SoundType.WOOD)));
        // ...);

        // =============================================================================
        // PATHS - 7 types + slabs (14 blocks total)
        // =============================================================================

        // Path blocks - used for village paths and building floors
        public static final RegistryObject<Block> PATHDIRT = BLOCKS.register("pathdirt",
                        () -> new org.millenaire.common.block.BlockPath(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .strength(0.5F)
                                        .sound(SoundType.GRAVEL)));

        public static final RegistryObject<Block> PATHGRAVEL = BLOCKS.register("pathgravel",
                        () -> new org.millenaire.common.block.BlockPath(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(0.5F)
                                        .sound(SoundType.GRAVEL)));

        public static final RegistryObject<Block> PATHSLABS = BLOCKS.register("pathslabs",
                        () -> new org.millenaire.common.block.BlockPath(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHSANDSTONE = BLOCKS.register("pathsandstone",
                        () -> new org.millenaire.common.block.BlockPath(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SAND)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHOCHRETILES = BLOCKS.register("pathochretiles",
                        () -> new org.millenaire.common.block.BlockPath(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.TERRACOTTA_ORANGE)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHGRAVELSLABS = BLOCKS.register("pathgravelslabs",
                        () -> new org.millenaire.common.block.BlockPath(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHSNOW = BLOCKS.register("pathsnow",
                        () -> new org.millenaire.common.block.BlockPath(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SNOW)
                                        .strength(0.2F)
                                        .sound(SoundType.SNOW)));

        // Path slabs - half-height variants
        // Path slabs - half-height variants
        public static final RegistryObject<Block> PATHDIRT_SLAB = BLOCKS.register("pathdirt_slab",
                        () -> new org.millenaire.common.block.BlockPathSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.DIRT)
                                        .strength(0.5F)
                                        .sound(SoundType.GRAVEL)));

        public static final RegistryObject<Block> PATHGRAVEL_SLAB = BLOCKS.register("pathgravel_slab",
                        () -> new org.millenaire.common.block.BlockPathSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(0.5F)
                                        .sound(SoundType.GRAVEL)));

        public static final RegistryObject<Block> PATHSLABS_SLAB = BLOCKS.register("pathslabs_slab",
                        () -> new org.millenaire.common.block.BlockPathSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHSANDSTONE_SLAB = BLOCKS.register("pathsandstone_slab",
                        () -> new org.millenaire.common.block.BlockPathSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SAND)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHOCHRETILES_SLAB = BLOCKS.register("pathochretiles_slab",
                        () -> new org.millenaire.common.block.BlockPathSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.TERRACOTTA_ORANGE)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHGRAVELSLABS_SLAB = BLOCKS.register("pathgravelslabs_slab",
                        () -> new org.millenaire.common.block.BlockPathSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.STONE)
                                        .strength(0.5F)
                                        .sound(SoundType.STONE)));

        public static final RegistryObject<Block> PATHSNOW_SLAB = BLOCKS.register("pathsnow_slab",
                        () -> new org.millenaire.common.block.BlockPathSlab(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.SNOW)
                                        .strength(0.2F)
                                        .sound(SoundType.SNOW)));

        // =============================================================================
        // FUNCTIONAL BLOCKS WITH TILE ENTITIES
        // =============================================================================

        // Locked Chest - village resource storage with ownership
        public static final RegistryObject<Block> LOCKED_CHEST = BLOCKS.register("locked_chest",
                        () -> new org.millenaire.common.block.BlockLockedChest(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(2.5F)
                                        .sound(SoundType.WOOD)));

        // Import Table - for modders to import/export building plans
        public static final RegistryObject<Block> IMPORT_TABLE = BLOCKS.register("import_table",
                        () -> new org.millenaire.common.block.BlockImportTable(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.WOOD)
                                        .strength(2.5F)
                                        .sound(SoundType.WOOD)));

        // =============================================================================
        // MOCK BLOCKS - For building system (Phase 3+)
        // =============================================================================

        // TODO: Implement mock block types
        // MARKER_BLOCK, MAIN_CHEST, ANIMAL_SPAWN, SOURCE, FREE_BLOCK, TREE_SPAWN,
        // SOIL_BLOCK, DECOR_BLOCK
        // VILLAGE_BANNER_WALL, VILLAGE_BANNER_STANDING, CULTURE_BANNER_WALL,
        // CULTURE_BANNER_STANDING

        // =============================================================================
        // SPECIAL BLOCKS
        // =============================================================================

        // Alchemist explosive
        public static final RegistryObject<Block> ALCHEMIST_EXPLOSIVE = BLOCKS.register("alchemistexplosive",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.COLOR_RED)
                                        .strength(2.0F, 10.0F)
                                        .sound(SoundType.STONE)));

        // Sod (shared/decorative)
        public static final RegistryObject<Block> SOD = BLOCKS.register("sod",
                        () -> new Block(BlockBehaviour.Properties.of()
                                        .mapColor(MapColor.GRASS)
                                        .strength(0.6F)
                                        .sound(SoundType.GRASS)));

        // =============================================================================
        // PAINTED BRICKS - 80 blocks total (16 colors × 5 types)
        // =============================================================================

        // TODO: Implement painted brick system
        // Loop through DyeColor.values() and register:
        // - painted_brick_<color>
        // - painted_brick_decorated_<color>
        // - stairs_painted_brick_<color>
        // - slab_painted_brick_<color>
        // - wall_painted_brick_<color>
}
