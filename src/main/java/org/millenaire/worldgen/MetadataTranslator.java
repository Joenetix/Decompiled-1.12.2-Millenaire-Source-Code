package org.millenaire.worldgen;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraftforge.registries.ForgeRegistries;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.millenaire.core.MillBlocks;
import org.millenaire.common.block.BlockDecorativeWood;
import org.millenaire.common.block.WoodDecoVariant;

/**
 * Translates 1.12.2 metadata values to 1.20.1 BlockStates.
 * 
 * The original Millénaire mod used Minecraft 1.12.2 which stored block variants
 * as numeric metadata (0-15). Minecraft 1.13+ uses blockstate properties
 * instead.
 * This class provides the translation layer between the two systems.
 */
public class MetadataTranslator {

    private static final Logger LOGGER = LogUtils.getLogger();

    static {
        LOGGER.info("[MetadataTranslator] v3 Loaded - Includes ALL fallbacks for painted_brick, sandstone, etc.");
    }

    /**
     * Translate a 1.12.2 block ID + metadata to a 1.20.1 BlockState.
     * 
     * @param blockId     The original block ID (e.g., "minecraft:stone")
     * @param metadata    The numeric metadata value (0-15), or -1 if property-based
     * @param metadataStr The original string from blocklist.txt (for property
     *                    parsing)
     * @return The translated BlockState, or null if translation fails
     */
    public static BlockState translate(String blockId, int metadata, String metadataStr) {
        // Handle property-based entries
        if (metadata == -1 && metadataStr != null && metadataStr.contains("=")) {
            BlockState result = parsePropertiesFormat(blockId, metadataStr);
            if (result != null) {
                if (metadataStr.contains("facing") || metadataStr.contains("half")) {
                    LOGGER.info("[Props] {} with '{}' -> {}", blockId, metadataStr, result);
                }
                return result;
            }
            // If result is null (e.g. invalid block), fall through to metadata/ID/fallback
            // logic
        }
        // CRITICAL: Try metadata-based translation FIRST
        // Old IDs like "minecraft:planks" don't exist in 1.20.1 registry!
        BlockState metadataResult = translateByMetadata(blockId, metadata);
        if (metadataResult != null) {
            return metadataResult;
        }

        // For blocks without metadata, try ID translation + registry
        String modernId = translateBlockId(blockId);

        // FIX: Handle legacy double_stone_slab explicitly as it doesn't exist in 1.20
        // registry
        // Original mod mapped meta 0 to Stone Slab (Double) which looked like smooth
        // stone
        if (blockId.equals("minecraft:double_stone_slab")) {
            if (metadata == 0 || metadata == 8) {
                return Blocks.SMOOTH_STONE.defaultBlockState();
            } else if (metadata == 1 || metadata == 9) { // Sandstone
                return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            }
            // Behave as regular smooth stone for other variants for now
            return Blocks.SMOOTH_STONE.defaultBlockState();
        }

        // Explicitly allow AIR (which is otherwise filtered out by the null/AIR check
        // below)
        if (modernId.equals("minecraft:air")) {
            return Blocks.AIR.defaultBlockState();
        }

        ResourceLocation blockRes = new ResourceLocation(modernId);
        Block block = ForgeRegistries.BLOCKS.getValue(blockRes);

        if (block != null && block != Blocks.AIR) {
            return block.defaultBlockState();
        }

        // --- FALLBACK FOR MISSING MILLENAIRE BLOCKS ---
        // Prevents crashes when Millénaire content (stairs, slabs, walls) is not yet
        // registered
        if (blockId.startsWith("millenaire:")) {
            // Specific missing items/blocks
            if (blockId.equals("millenaire:byzantineiconmedium")) {
                return Blocks.FURNACE.defaultBlockState(); // Fallback to furnace to match hardcoded color map
            }

            // Sandstone variants (carved, etc.)
            if (blockId.contains("sandstone")) {
                if (blockId.contains("slab"))
                    return Blocks.SANDSTONE_SLAB.defaultBlockState();
                if (blockId.contains("stairs"))
                    return Blocks.SANDSTONE_STAIRS.defaultBlockState();
                if (blockId.contains("wall"))
                    return Blocks.SANDSTONE_WALL.defaultBlockState();
                return Blocks.SANDSTONE.defaultBlockState();
            }
            // Painted bricks - map to terracotta
            if (blockId.contains("painted_brick")) {
                if (blockId.contains("stairs"))
                    return Blocks.BRICK_STAIRS.defaultBlockState(); // Use brick stairs for shape
                if (blockId.contains("slab"))
                    return Blocks.BRICK_SLAB.defaultBlockState();
                return Blocks.TERRACOTTA.defaultBlockState();
            }
            // Wooden bars / lattices
            if (blockId.contains("wooden_bars") || blockId.contains("rosette")) {
                return Blocks.OAK_FENCE.defaultBlockState();
            }
            // Tiles (Byzantine, etc.)
            if (blockId.contains("tiles")) {
                if (blockId.contains("stairs"))
                    return Blocks.STONE_BRICK_STAIRS.defaultBlockState();
                if (blockId.contains("slab"))
                    return Blocks.STONE_BRICK_SLAB.defaultBlockState();
                return Blocks.STONE_BRICKS.defaultBlockState();
            }
            // Earth / Mud
            if (blockId.contains("earth") || blockId.contains("mud")) {
                return Blocks.PACKED_MUD.defaultBlockState();
            }
            // Thatch blocks - use oak for better roof appearance
            if (blockId.contains("thatch")) {
                if (blockId.contains("stairs"))
                    return Blocks.OAK_STAIRS.defaultBlockState();
                if (blockId.contains("slab"))
                    return Blocks.OAK_SLAB.defaultBlockState();
                return Blocks.HAY_BLOCK.defaultBlockState();
            }
        }

        return null;
    }

    /**
     * Apply a single property to a BlockState.
     * Handles common properties like facing, half, part, axis, etc.
     */
    private static BlockState applyProperty(BlockState state, String key, String value) {
        try {
            Block block = state.getBlock();

            switch (key.toLowerCase()) {
                case "facing" -> {
                    Direction dir = parseDirectionValue(value);
                    if (dir != null) {
                        // Try horizontal facing first (most common)
                        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                            if (dir.getAxis().isHorizontal()) {
                                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
                            }
                        } else if (state.hasProperty(BlockStateProperties.FACING)) {
                            state = state.setValue(BlockStateProperties.FACING, dir);
                        }
                    }
                }
                case "half" -> {
                    if (state.hasProperty(BlockStateProperties.HALF)) {
                        Half half = "top".equalsIgnoreCase(value) ? Half.TOP : Half.BOTTOM;
                        state = state.setValue(BlockStateProperties.HALF, half);
                    } else if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                        DoubleBlockHalf dbh = "upper".equalsIgnoreCase(value) ? DoubleBlockHalf.UPPER
                                : DoubleBlockHalf.LOWER;
                        state = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, dbh);
                    }
                }
                case "part" -> {
                    if (state.hasProperty(BedBlock.PART)) {
                        net.minecraft.world.level.block.state.properties.BedPart part = "head".equalsIgnoreCase(value)
                                ? net.minecraft.world.level.block.state.properties.BedPart.HEAD
                                : net.minecraft.world.level.block.state.properties.BedPart.FOOT;
                        state = state.setValue(BedBlock.PART, part);
                    }
                }
                case "axis" -> {
                    if (state.hasProperty(BlockStateProperties.AXIS)) {
                        Direction.Axis axis = switch (value.toLowerCase()) {
                            case "x" -> Direction.Axis.X;
                            case "z" -> Direction.Axis.Z;
                            default -> Direction.Axis.Y;
                        };
                        state = state.setValue(BlockStateProperties.AXIS, axis);
                    }
                }
                case "shape" -> {
                    if (state.hasProperty(BlockStateProperties.STAIRS_SHAPE)) {
                        StairsShape shape = switch (value.toLowerCase()) {
                            case "inner_left" -> StairsShape.INNER_LEFT;
                            case "inner_right" -> StairsShape.INNER_RIGHT;
                            case "outer_left" -> StairsShape.OUTER_LEFT;
                            case "outer_right" -> StairsShape.OUTER_RIGHT;
                            default -> StairsShape.STRAIGHT;
                        };
                        state = state.setValue(BlockStateProperties.STAIRS_SHAPE, shape);
                    }
                }
                case "type" -> {
                    if (state.hasProperty(BlockStateProperties.SLAB_TYPE)) {
                        SlabType slabType = switch (value.toLowerCase()) {
                            case "top" -> SlabType.TOP;
                            case "double" -> SlabType.DOUBLE;
                            default -> SlabType.BOTTOM;
                        };
                        state = state.setValue(BlockStateProperties.SLAB_TYPE, slabType);
                    }
                }
                case "open" -> {
                    if (state.hasProperty(BlockStateProperties.OPEN)) {
                        boolean open = "true".equalsIgnoreCase(value);
                        state = state.setValue(BlockStateProperties.OPEN, open);
                    }
                }
                case "lit" -> {
                    if (state.hasProperty(BlockStateProperties.LIT)) {
                        boolean lit = "true".equalsIgnoreCase(value);
                        state = state.setValue(BlockStateProperties.LIT, lit);
                    }
                }
                case "powered" -> {
                    if (state.hasProperty(BlockStateProperties.POWERED)) {
                        boolean powered = "true".equalsIgnoreCase(value);
                        state = state.setValue(BlockStateProperties.POWERED, powered);
                    }
                }
                case "hinge" -> {
                    if (state.hasProperty(DoorBlock.HINGE)) {
                        DoorHingeSide hinge = "right".equalsIgnoreCase(value)
                                ? DoorHingeSide.RIGHT
                                : DoorHingeSide.LEFT;
                        state = state.setValue(DoorBlock.HINGE, hinge);
                    }
                }
                // Ignore unknown properties silently
                default -> {
                }
            }
        } catch (Exception e) {
            // If property application fails, return state unchanged
            LOGGER.debug("Failed to apply property {}={} to {}: {}", key, value, state.getBlock(), e.getMessage());
        }
        return state;
    }

    /**
     * Parse direction from string value.
     */
    private static Direction parseDirectionValue(String value) {
        return switch (value.toLowerCase()) {
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            default -> null;
        };
    }

    /**
     * Translate blocks using 1.12.2 metadata.
     * Returns null if no metadata translation exists for this block.
     */
    private static BlockState translateByMetadata(String blockId, int metadata) {
        try {
            return switch (blockId) {
                // Stone variants
                case "minecraft:stone" -> translateStone(metadata);
                case "minecraft:stonebrick" -> translateStoneBrick(metadata);
                case "minecraft:sandstone" -> translateSandstone(metadata);
                case "minecraft:red_sandstone" -> translateRedSandstone(metadata);
                // Wood types - CRITICAL: these old IDs don't exist in 1.20.1!
                case "minecraft:planks" -> translatePlanks(metadata);
                case "minecraft:log" -> translateLog(null, metadata, false);
                case "minecraft:log2" -> translateLog(null, metadata, true);
                case "minecraft:sapling" -> translateSapling(metadata);
                case "minecraft:leaves" -> translateLeaves(metadata, false);
                case "minecraft:leaves2" -> translateLeaves(metadata, true);
                // Simple blocks with metadata 0
                case "minecraft:gravel" -> Blocks.GRAVEL.defaultBlockState();
                // Stairs
                case "minecraft:oak_stairs", "minecraft:stone_stairs",
                        "minecraft:brick_stairs", "minecraft:stone_brick_stairs",
                        "minecraft:nether_brick_stairs", "minecraft:sandstone_stairs",
                        "minecraft:spruce_stairs", "minecraft:birch_stairs",
                        "minecraft:jungle_stairs", "minecraft:quartz_stairs",
                        "minecraft:acacia_stairs", "minecraft:dark_oak_stairs",
                        "minecraft:red_sandstone_stairs", "minecraft:purpur_stairs" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    yield (block != null && block != Blocks.AIR) ? translateStairs(block, metadata) : null;
                }
                // Slabs - special handling needed
                case "minecraft:stone_slab", "minecraft:wooden_slab",
                        "minecraft:stone_slab2", "minecraft:purpur_slab" ->
                    translateSlab(blockId, null, metadata);
                // Doors
                case "minecraft:wooden_door", "minecraft:iron_door",
                        "minecraft:spruce_door", "minecraft:birch_door",
                        "minecraft:jungle_door", "minecraft:acacia_door",
                        "minecraft:dark_oak_door", "minecraft:oak_door" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    yield (block != null && block != Blocks.AIR) ? translateDoor(block, metadata) : null;
                }
                // Torches
                case "minecraft:torch" -> translateTorch(metadata);

                // Other attachments needing registry lookup
                case "minecraft:lever", "minecraft:ladder", "minecraft:vine" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    if (block == null || block == Blocks.AIR)
                        yield null;
                    yield switch (blockId) {
                        case "minecraft:lever" -> translateLever(block, metadata);
                        case "minecraft:ladder" -> translateLadder(block, metadata);
                        case "minecraft:vine" -> translateVine(block, metadata);
                        default -> null;
                    };
                }
                // Beds
                case "minecraft:bed" -> translateBed(metadata);
                // Colored blocks - CRITICAL: these old IDs don't exist!
                case "minecraft:wool" -> translateWool(metadata);
                case "minecraft:stained_hardened_clay" -> translateStainedClay(metadata);
                case "minecraft:stained_glass" -> translateStainedGlass(metadata);
                case "minecraft:stained_glass_pane" -> translateStainedGlassPane(metadata);
                case "minecraft:carpet" -> translateCarpet(metadata);
                case "minecraft:concrete" -> translateConcrete(metadata);
                case "minecraft:concrete_powder" -> translateConcretePowder(metadata);
                // Fences
                case "minecraft:fence", "minecraft:nether_brick_fence" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    yield (block != null && block != Blocks.AIR) ? block.defaultBlockState() : null;
                }
                // Directional blocks - NOTE: Modern pumpkin has no facing (only carved_pumpkin
                // does)
                case "minecraft:pumpkin" -> Blocks.PUMPKIN.defaultBlockState();
                case "minecraft:lit_pumpkin" -> {
                    // Jack o'lantern still has facing in 1.20.1
                    Direction facing = Direction.from2DDataValue(metadata & 0x3);
                    yield Blocks.JACK_O_LANTERN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                            facing);
                }
                case "minecraft:furnace", "minecraft:lit_furnace" -> {
                    String modernId = translateBlockId(blockId);
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modernId));
                    if (block == null || block == Blocks.AIR)
                        yield null;
                    yield translateHorizontalFacing(block, metadata);
                }
                case "minecraft:dispenser", "minecraft:dropper" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    if (block == null || block == Blocks.AIR)
                        yield null;
                    yield translateDispenser(block, metadata);
                }
                // Tall plants (double_plant) - use GRASS for decorative grass
                case "minecraft:double_plant" -> Blocks.GRASS.defaultBlockState(); // Single-block grass
                // Redstone
                case "minecraft:powered_repeater", "minecraft:unpowered_repeater",
                        "minecraft:powered_comparator", "minecraft:unpowered_comparator" -> {
                    String modernId = translateBlockId(blockId);
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modernId));
                    if (block == null || block == Blocks.AIR)
                        yield null;
                    yield blockId.contains("repeater") ? translateRepeater(block, metadata)
                            : translateComparator(block, metadata);
                }
                // Flowers (red_flower has 9 variants in 1.12.2, split into separate blocks in
                // 1.20.1)
                case "minecraft:red_flower" -> switch (metadata) {
                    case 0 -> Blocks.POPPY.defaultBlockState(); // Poppy (red)
                    case 1 -> Blocks.BLUE_ORCHID.defaultBlockState(); // Blue Orchid
                    case 2 -> Blocks.ALLIUM.defaultBlockState(); // Allium (purple)
                    case 3 -> Blocks.AZURE_BLUET.defaultBlockState(); // Azure Bluet (white) - this is RGB(255,50,53)!
                    case 4 -> Blocks.RED_TULIP.defaultBlockState(); // Red Tulip
                    case 5 -> Blocks.ORANGE_TULIP.defaultBlockState(); // Orange Tulip
                    case 6 -> Blocks.WHITE_TULIP.defaultBlockState(); // White Tulip
                    case 7 -> Blocks.PINK_TULIP.defaultBlockState(); // Pink Tulip
                    case 8 -> Blocks.OXEYE_DAISY.defaultBlockState(); // Oxeye Daisy
                    default -> null;
                };
                // Crops - wheat/carrots/potatoes use AGE_7, beetroots uses AGE_3, nether_wart
                // uses AGE_3
                case "minecraft:wheat", "minecraft:carrots", "minecraft:potatoes" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    yield (block != null && block != Blocks.AIR) ? translateCrop(block, metadata) : null;
                }
                case "minecraft:beetroots" -> {
                    int age = Math.min(metadata & 0x3, 3); // Beetroots has 4 growth stages (0-3)
                    yield Blocks.BEETROOTS.defaultBlockState().setValue(BlockStateProperties.AGE_3, age);
                }
                case "minecraft:nether_wart" -> {
                    int age = Math.min(metadata & 0x3, 3); // Nether wart has 4 growth stages (0-3)
                    yield Blocks.NETHER_WART.defaultBlockState().setValue(BlockStateProperties.AGE_3, age);
                }
                // Anvil
                case "minecraft:anvil" -> translateAnvil(metadata);
                // Cauldron - FIXED: use WATER_CAULDRON for filled states
                case "minecraft:cauldron" -> {
                    if (metadata == 0)
                        yield Blocks.CAULDRON.defaultBlockState();
                    yield Blocks.WATER_CAULDRON.defaultBlockState()
                            .setValue(LayeredCauldronBlock.LEVEL, Math.min(metadata, 3));
                }
                // Farmland
                case "minecraft:farmland" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    yield (block != null && block != Blocks.AIR) ? translateFarmland(block, metadata) : null;
                }
                // Cake
                case "minecraft:cake" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
                    yield (block != null && block != Blocks.AIR) ? translateCake(block, metadata) : null;
                }
                case "minecraft:rail", "minecraft:golden_rail", "minecraft:detector_rail",
                        "minecraft:activator_rail" -> {
                    String modernId = translateBlockId(blockId);
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(modernId));
                    yield (block != null && block != Blocks.AIR) ? translateRail(block, metadata) : null;
                }

                // === ADDED: Missing 1.12.2 block translations ===

                // Double stone slabs - convert to full blocks (smooth stone, sandstone, etc.)
                case "minecraft:double_stone_slab" -> translateDoubleStoneSlab(metadata);

                // Simple ID renames with no metadata
                case "minecraft:brick_block" -> Blocks.BRICKS.defaultBlockState();
                case "minecraft:hardened_clay" -> Blocks.TERRACOTTA.defaultBlockState();
                case "minecraft:melon_block" -> Blocks.MELON.defaultBlockState();
                case "minecraft:noteblock" -> Blocks.NOTE_BLOCK.defaultBlockState();
                case "minecraft:waterlily" -> Blocks.LILY_PAD.defaultBlockState();
                case "minecraft:fence_gate" -> Blocks.OAK_FENCE_GATE.defaultBlockState();
                case "minecraft:grass" -> Blocks.GRASS_BLOCK.defaultBlockState();
                case "minecraft:grass_path" -> Blocks.DIRT_PATH.defaultBlockState();

                // Tall grass / flowers - metadata controls type
                case "minecraft:tallgrass" -> translateTallGrass(metadata);
                case "minecraft:yellow_flower" -> Blocks.DANDELION.defaultBlockState();

                // Lit redstone lamp (1.20.1 uses blockstate property)
                case "minecraft:lit_redstone_lamp" -> Blocks.REDSTONE_LAMP.defaultBlockState();

                // End rod with facing
                case "minecraft:end_rod" -> {
                    Direction facing = switch (metadata) {
                        case 0 -> Direction.DOWN;
                        case 1 -> Direction.UP;
                        case 2 -> Direction.NORTH;
                        case 3 -> Direction.SOUTH;
                        case 4 -> Direction.WEST;
                        case 5 -> Direction.EAST;
                        default -> Direction.UP;
                    };
                    yield Blocks.END_ROD.defaultBlockState().setValue(EndRodBlock.FACING, facing);
                }

                // Hay block / bone block with axis
                case "minecraft:hay_block", "minecraft:bone_block" -> {
                    Block block = blockId.equals("minecraft:hay_block") ? Blocks.HAY_BLOCK : Blocks.BONE_BLOCK;
                    Direction.Axis axis = switch (metadata >> 2) {
                        case 0 -> Direction.Axis.Y;
                        case 1 -> Direction.Axis.X;
                        case 2 -> Direction.Axis.Z;
                        default -> Direction.Axis.Y;
                    };
                    yield block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
                }

                // Trapdoor with properties
                case "minecraft:trapdoor" -> translateTrapdoor(metadata);

                // Standing sign with rotation
                case "minecraft:standing_sign" -> Blocks.OAK_SIGN.defaultBlockState()
                        .setValue(StandingSignBlock.ROTATION, metadata & 15);

                // === ADDED: Missing blocks from blocklist analysis ===

                // Sand variants
                case "minecraft:sand" -> switch (metadata) {
                    case 0 -> Blocks.SAND.defaultBlockState();
                    case 1 -> Blocks.RED_SAND.defaultBlockState();
                    default -> Blocks.SAND.defaultBlockState();
                };

                // Dirt variants (dirt:0 = dirt, dirt:1 = coarse_dirt, dirt:2 = podzol)
                case "minecraft:dirt" -> switch (metadata) {
                    case 0 -> Blocks.DIRT.defaultBlockState();
                    case 1 -> Blocks.COARSE_DIRT.defaultBlockState();
                    case 2 -> Blocks.PODZOL.defaultBlockState();
                    default -> Blocks.DIRT.defaultBlockState();
                };

                // Wood fences - these exist with same ID in 1.20.1, just need default state
                case "minecraft:spruce_fence" -> Blocks.SPRUCE_FENCE.defaultBlockState();
                case "minecraft:birch_fence" -> Blocks.BIRCH_FENCE.defaultBlockState();
                case "minecraft:jungle_fence" -> Blocks.JUNGLE_FENCE.defaultBlockState();
                case "minecraft:acacia_fence" -> Blocks.ACACIA_FENCE.defaultBlockState();
                case "minecraft:dark_oak_fence" -> Blocks.DARK_OAK_FENCE.defaultBlockState();

                // Simple blocks that exist in 1.20.1 with same/similar ID
                case "minecraft:nether_brick" -> Blocks.NETHER_BRICKS.defaultBlockState();
                case "minecraft:purpur_block" -> Blocks.PURPUR_BLOCK.defaultBlockState();
                case "minecraft:sea_lantern" -> Blocks.SEA_LANTERN.defaultBlockState();
                case "minecraft:redstone_block" -> Blocks.REDSTONE_BLOCK.defaultBlockState();
                case "minecraft:blue_terracotta" -> Blocks.BLUE_TERRACOTTA.defaultBlockState();

                // Flower pot - metadata controls content (complex, map to empty for now)
                case "minecraft:flower_pot" -> Blocks.FLOWER_POT.defaultBlockState();

                // =========================================================
                // MILLENAIRE CUSTOM BLOCKS - translate metadata to variants
                // =========================================================

                // Wood decoration (timber frame, thatch, etc.)
                case "millenaire:wood_deco" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("millenaire:wood_deco"));
                    if (block instanceof org.millenaire.common.block.BlockDecorativeWood woodBlock) {
                        yield woodBlock.getStateFromMeta(metadata);
                    }
                    yield (block != null) ? block.defaultBlockState() : null;
                }

                // Stone decoration (mud brick, cooked brick, etc.)
                case "millenaire:stone_deco" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("millenaire:stone_deco"));
                    if (block instanceof org.millenaire.common.block.BlockDecorativeStone stoneBlock) {
                        yield stoneBlock.getStateFromMeta(metadata);
                    }
                    yield (block != null) ? block.defaultBlockState() : null;
                }

                // Panel block - handle 3D rotation metadata
                case "millenaire:panel" -> {
                    Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("millenaire:panel"));
                    if (block == null || block == Blocks.AIR)
                        yield null;

                    // Millenaire panels use 0-5 for 3D direction
                    // 0=Down, 1=Up, 2=North, 3=South, 4=West, 5=East
                    // Vertical axis (0, 1) defaults to NORTH in original code
                    Direction facing = Direction.from3DDataValue(metadata);
                    if (facing.getAxis().isVertical()) {
                        facing = Direction.NORTH;
                    }

                    yield block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
                }

                // Millenaire custom blocks - catch-all for other culture-specific blocks
                default -> {
                    if (blockId.startsWith("millenaire:")) {
                        // Lookup custom block in registry
                        ResourceLocation res = new ResourceLocation(blockId);
                        Block block = ForgeRegistries.BLOCKS.getValue(res);
                        yield (block != null && block != Blocks.AIR) ? block.defaultBlockState() : null;
                    }
                    // Not a millenaire block and no metadata translation exists
                    yield null;
                }
            };
        } catch (Exception e) {
            LOGGER.warn("[Metadata Translator] Failed to translate {},{}: {}",
                    blockId, metadata, e.getMessage());
            return null;
        }
    }

    /**
     * Parse properties from format like "variant=oak" or "facing=north,half=top"
     */
    private static BlockState parsePropertiesFormat(String blockId, String propertiesStr) {
        // EARLY FALLBACK: Handle thatch blocks before property parsing fails
        // Check BOTH variant=thatch properties AND millenaire:stairs_thatch block ID
        if (propertiesStr.contains("variant=thatch") || blockId.equals("millenaire:stairs_thatch")) {
            if (blockId.equals("millenaire:stairs_thatch") || blockId.contains("stairs")) {
                // Return oak stairs with preserved facing/half properties
                if (propertiesStr.contains("half=top")) {
                    if (propertiesStr.contains("facing=north"))
                        return Blocks.OAK_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING,
                                        net.minecraft.core.Direction.NORTH)
                                .setValue(net.minecraft.world.level.block.StairBlock.HALF,
                                        net.minecraft.world.level.block.state.properties.Half.TOP);
                    if (propertiesStr.contains("facing=south"))
                        return Blocks.OAK_STAIRS.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING,
                                        net.minecraft.core.Direction.SOUTH)
                                .setValue(net.minecraft.world.level.block.StairBlock.HALF,
                                        net.minecraft.world.level.block.state.properties.Half.TOP);
                    if (propertiesStr.contains("facing=east"))
                        return MillBlocks.STAIRS_THATCH.get().defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING,
                                        net.minecraft.core.Direction.EAST)
                                .setValue(net.minecraft.world.level.block.StairBlock.HALF,
                                        net.minecraft.world.level.block.state.properties.Half.TOP);
                    if (propertiesStr.contains("facing=west"))
                        return MillBlocks.STAIRS_THATCH.get().defaultBlockState()
                                .setValue(net.minecraft.world.level.block.StairBlock.FACING,
                                        net.minecraft.core.Direction.WEST)
                                .setValue(net.minecraft.world.level.block.StairBlock.HALF,
                                        net.minecraft.world.level.block.state.properties.Half.TOP);
                } else {
                    if (propertiesStr.contains("facing=north"))
                        return MillBlocks.STAIRS_THATCH.get().defaultBlockState().setValue(
                                net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.NORTH);
                    if (propertiesStr.contains("facing=south"))
                        return MillBlocks.STAIRS_THATCH.get().defaultBlockState().setValue(
                                net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.SOUTH);
                    if (propertiesStr.contains("facing=east"))
                        return MillBlocks.STAIRS_THATCH.get().defaultBlockState().setValue(
                                net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.EAST);
                    if (propertiesStr.contains("facing=west"))
                        return MillBlocks.STAIRS_THATCH.get().defaultBlockState().setValue(
                                net.minecraft.world.level.block.StairBlock.FACING, net.minecraft.core.Direction.WEST);
                }
                return MillBlocks.STAIRS_THATCH.get().defaultBlockState();
            }
            if (blockId.equals("millenaire:slab_wood_deco") || blockId.contains("slab")) {
                // Return oak slabs with preserved half property
                if (propertiesStr.contains("half=top")) {
                    return Blocks.OAK_SLAB.defaultBlockState().setValue(net.minecraft.world.level.block.SlabBlock.TYPE,
                            net.minecraft.world.level.block.state.properties.SlabType.TOP);
                }
                return Blocks.OAK_SLAB.defaultBlockState(); // half=bottom by default
            }
            // Generic thatch block fallback - use actual Millénaire thatch block
            return MillBlocks.WOOD_DECORATION.get().defaultBlockState()
                    .setValue(BlockDecorativeWood.VARIANT, WoodDecoVariant.THATCH);
        }

        // Parse properties into a map
        java.util.Map<String, String> properties = new java.util.HashMap<>();
        String[] props = propertiesStr.split(",");
        for (String prop : props) {
            String[] kv = prop.split("=", 2);
            if (kv.length == 2) {
                properties.put(kv[0].trim(), kv[1].trim());
            }
        }

        // CRITICAL: Torch translation (must be BEFORE variant!)
        // In 1.12.2: minecraft:torch with facing property
        // In 1.20.1: minecraft:wall_torch (facing=n/s/e/w) or minecraft:torch
        // (standing, no facing)
        if (blockId.equals("minecraft:torch") && properties.containsKey("facing")) {
            String facing = properties.get("facing");
            if (facing.equals("up")) {
                // Standing torch - keep minecraft:torch but remove facing property
                properties.remove("facing");
            } else {
                // Wall torch - change block ID
                blockId = "minecraft:wall_torch";
            }
        }

        // CRITICAL: Panel facing rotation
        // Old Millenaire stored the wall attachment direction (e.g., facing=east means
        // attached to east wall)
        // But 1.20.1 FACING means the direction the panel front faces (out from the
        // wall)
        // We need to rotate 90 degrees clockwise (like we did for signs)
        if (blockId.equals("millenaire:panel") && properties.containsKey("facing")) {
            String facing = properties.get("facing");
            String rotatedFacing = switch (facing) {
                case "north" -> "east";
                case "east" -> "south";
                case "south" -> "west";
                case "west" -> "north";
                default -> facing;
            };
            properties.put("facing", rotatedFacing);
        }

        // CRITICAL: Translate old "variant" property to modern block ID
        if (properties.containsKey("variant")) {
            String variant = properties.get("variant");
            blockId = translateVariantToBlockId(blockId, variant);
            properties.remove("variant"); // Variant is now in the block ID
        } else if (properties.containsKey("type") && blockId.equals("minecraft:sapling")) {
            // Handle "type=oak" etc for saplings (blocklist.txt uses type instead of
            // variant)
            String variant = properties.get("type");
            blockId = translateVariantToBlockId(blockId, variant);
            properties.remove("type");
        }

        // Get the (possibly translated) block
        ResourceLocation blockRes = new ResourceLocation(blockId);
        Block block = ForgeRegistries.BLOCKS.getValue(blockRes);
        if (block == null || block == Blocks.AIR) {
            return null;
        }
        BlockState state = block.defaultBlockState();

        // Apply remaining properties (facing, axis, half, etc.)
        for (java.util.Map.Entry<String, String> entry : properties.entrySet()) {
            state = applyProperty(state, entry.getKey(), entry.getValue());
        }
        return state;
    }

    /**
     * Translate old 1.12.2 variant property to modern 1.20.1 block ID.
     * Example: minecraft:log + variant=spruce → minecraft:spruce_log
     */
    private static String translateVariantToBlockId(String oldBlockId, String variant) {
        return switch (oldBlockId) {
            case "minecraft:log" -> switch (variant) {
                case "oak" -> "minecraft:oak_log";
                case "spruce" -> "minecraft:spruce_log";
                case "birch" -> "minecraft:birch_log";
                case "jungle" -> "minecraft:jungle_log";
                default -> oldBlockId;
            };
            case "minecraft:log2" -> switch (variant) {
                case "acacia" -> "minecraft:acacia_log";
                case "dark_oak" -> "minecraft:dark_oak_log";
                default -> oldBlockId;
            };
            case "minecraft:planks" -> switch (variant) {
                case "oak" -> "minecraft:oak_planks";
                case "spruce" -> "minecraft:spruce_planks";
                case "birch" -> "minecraft:birch_planks";
                case "jungle" -> "minecraft:jungle_planks";
                case "acacia" -> "minecraft:acacia_planks";
                case "dark_oak" -> "minecraft:dark_oak_planks";
                default -> oldBlockId;
            };
            case "minecraft:leaves" -> switch (variant) {
                case "oak" -> "minecraft:oak_leaves";
                case "spruce" -> "minecraft:spruce_leaves";
                case "birch" -> "minecraft:birch_leaves";
                case "jungle" -> "minecraft:jungle_leaves";
                default -> oldBlockId;
            };
            case "minecraft:leaves2" -> switch (variant) {
                case "acacia" -> "minecraft:acacia_leaves";
                case "dark_oak" -> "minecraft:dark_oak_leaves";
                default -> oldBlockId;
            };
            case "minecraft:sapling" -> switch (variant) {
                case "oak" -> "minecraft:oak_sapling";
                case "spruce" -> "minecraft:spruce_sapling";
                case "birch" -> "minecraft:birch_sapling";
                case "jungle" -> "minecraft:jungle_sapling";
                case "acacia" -> "minecraft:acacia_sapling";
                case "dark_oak" -> "minecraft:dark_oak_sapling";
                default -> oldBlockId;
            };
            case "minecraft:stone_slab" -> switch (variant) {
                case "stone" -> "minecraft:stone_slab";
                case "sandstone" -> "minecraft:sandstone_slab";
                case "cobblestone" -> "minecraft:cobblestone_slab";
                case "brick" -> "minecraft:brick_slab";
                case "stone_brick" -> "minecraft:stone_brick_slab";
                case "nether_brick" -> "minecraft:nether_brick_slab";
                case "quartz" -> "minecraft:quartz_slab";
                default -> oldBlockId;
            };
            case "minecraft:wooden_slab" -> switch (variant) {
                case "oak" -> "minecraft:oak_slab";
                case "spruce" -> "minecraft:spruce_slab";
                case "birch" -> "minecraft:birch_slab";
                case "jungle" -> "minecraft:jungle_slab";
                case "acacia" -> "minecraft:acacia_slab";
                case "dark_oak" -> "minecraft:dark_oak_slab";
                default -> oldBlockId;
            };
            case "minecraft:double_plant" -> switch (variant) {
                case "double_grass" -> "minecraft:tall_grass"; // Tall grass (2-block plant)
                case "double_fern" -> "minecraft:large_fern";
                case "double_rose" -> "minecraft:rose_bush";
                case "sunflower" -> "minecraft:sunflower";
                case "syringa" -> "minecraft:lilac";
                case "paeonia" -> "minecraft:peony";
                default -> oldBlockId;
            };
            default -> oldBlockId;
        };
    }

    /**
     * Generic helper to set a property value on a BlockState.
     * Works with any property type (Direction, Half, Boolean, Integer, etc.)
     */
    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState setValueHelper(
            BlockState state, Property<T> property, String valueStr) {
        // Try to parse the value using the property's own parser
        java.util.Optional<T> parsedValue = property.getValue(valueStr);
        if (parsedValue.isPresent()) {
            return state.setValue(property, parsedValue.get());
        }

        // Fallback: try common conversions
        if (property instanceof DirectionProperty && valueStr.length() > 0) {
            Direction dir = parseDirection(valueStr);
            if (dir != null && property.getPossibleValues().contains((T) dir)) {
                return state.setValue(property, (T) dir);
            }
        }

        return state; // Couldn't parse, return unchanged
    }

    /**
     * Translate 1.12.2 block IDs to 1.20.1 equivalents.
     * Many blocks were renamed in the 1.13 flattening.
     */
    private static String translateBlockId(String oldId) {
        return switch (oldId) {
            // Stone variants (no underscore → with underscore)
            case "minecraft:stonebrick" -> "minecraft:stone_bricks";
            case "minecraft:double_stone_slab" -> "minecraft:smooth_stone";

            // Wood blocks (generic → specific type)
            case "minecraft:planks" -> "minecraft:oak_planks"; // Default to oak
            case "minecraft:log" -> "minecraft:oak_log"; // Will be overridden by metadata translator
            case "minecraft:log2" -> "minecraft:acacia_log"; // Will be overridden by metadata translator
            case "minecraft:fence" -> "minecraft:oak_fence"; // CRITICAL: minecraft:fence doesn't exist in 1.20.1!
            case "minecraft:fence_gate" -> "minecraft:oak_fence_gate"; // CRITICAL: minecraft:fence_gate doesn't exist
                                                                       // in 1.20.1!

            // Doors (wooden_door → oak_door)
            case "minecraft:wooden_door" -> "minecraft:oak_door";

            // Slabs (keep old ID for metadata-based type selection)
            case "minecraft:stone_slab", "minecraft:wooden_slab", "minecraft:stone_slab2" -> oldId; // Special handling
                                                                                                    // in
                                                                                                    // translateSlab()

            // Pumpkins
            case "minecraft:lit_pumpkin" -> "minecraft:jack_o_lantern";

            // Furnaces
            case "minecraft:lit_furnace" -> "minecraft:furnace"; // Lit state is blockstate property now

            // Repeaters/Comparators
            case "minecraft:unpowered_repeater", "minecraft:powered_repeater" -> "minecraft:repeater";
            case "minecraft:unpowered_comparator", "minecraft:powered_comparator" -> "minecraft:comparator";

            // Colored blocks
            case "minecraft:stained_hardened_clay" -> "minecraft:terracotta"; // Will select color via metadata
            case "minecraft:stained_glass" -> "minecraft:white_stained_glass"; // Will select color via metadata
            case "minecraft:stained_glass_pane" -> "minecraft:white_stained_glass_pane"; // Will select color via
                                                                                         // metadata

            // Rails
            case "minecraft:golden_rail" -> "minecraft:powered_rail";

            // ===== ADDED: Missing blocks from log analysis =====

            // Pressure plates (wooden_pressure_plate → oak_pressure_plate)
            case "minecraft:wooden_pressure_plate" -> "minecraft:oak_pressure_plate";

            // Snow (snow_layer → snow)
            case "minecraft:snow_layer" -> "minecraft:snow";

            // Silver glazed terracotta → light_gray_glazed_terracotta
            case "minecraft:silver_glazed_terracotta" -> "minecraft:light_gray_glazed_terracotta";

            // Portal blocks
            case "minecraft:portal" -> "minecraft:nether_portal";

            // Flowing liquids (flowing_lava/water → lava/water with properties)
            case "minecraft:flowing_lava" -> "minecraft:lava";
            case "minecraft:flowing_water" -> "minecraft:water";

            // Mob spawner
            case "minecraft:mob_spawner" -> "minecraft:spawner";

            // Nether wart
            case "minecraft:nether_wart" -> "minecraft:nether_wart";

            // Boat (entity, not block, but included for completeness)
            case "minecraft:boat" -> "minecraft:oak_boat";

            // Grass/fern
            case "minecraft:tallgrass" -> "minecraft:grass"; // Will be overridden by metadata for fern

            // Red flower (many variants via metadata)
            case "minecraft:red_flower" -> "minecraft:poppy"; // Will be overridden by metadata

            // Default: no translation needed
            default -> oldId;
        };
    }

    private static Direction parseDirection(String dir) {
        return switch (dir.toLowerCase()) {
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east" -> Direction.EAST;
            case "west" -> Direction.WEST;
            case "up" -> Direction.UP;
            case "down" -> Direction.DOWN;
            default -> null;
        };
    }

    // ========== STONE VARIANTS ==========

    private static BlockState translateStone(int meta) {
        return switch (meta) {
            case 0 -> Blocks.STONE.defaultBlockState();
            case 1 -> Blocks.GRANITE.defaultBlockState();
            case 2 -> Blocks.POLISHED_GRANITE.defaultBlockState();
            case 3 -> Blocks.DIORITE.defaultBlockState();
            case 4 -> Blocks.POLISHED_DIORITE.defaultBlockState();
            case 5 -> Blocks.ANDESITE.defaultBlockState();
            case 6 -> Blocks.POLISHED_ANDESITE.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private static BlockState translateStoneBrick(int meta) {
        return switch (meta) {
            case 0 -> Blocks.STONE_BRICKS.defaultBlockState();
            case 1 -> Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            case 2 -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            case 3 -> Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
            default -> Blocks.STONE_BRICKS.defaultBlockState();
        };
    }

    private static BlockState translateSandstone(int meta) {
        return switch (meta) {
            case 0 -> Blocks.SANDSTONE.defaultBlockState();
            case 1 -> Blocks.CHISELED_SANDSTONE.defaultBlockState();
            case 2 -> Blocks.CUT_SANDSTONE.defaultBlockState();
            default -> Blocks.SANDSTONE.defaultBlockState();
        };
    }

    private static BlockState translateRedSandstone(int meta) {
        return switch (meta) {
            case 0 -> Blocks.RED_SANDSTONE.defaultBlockState();
            case 1 -> Blocks.CHISELED_RED_SANDSTONE.defaultBlockState();
            case 2 -> Blocks.CUT_RED_SANDSTONE.defaultBlockState();
            default -> Blocks.RED_SANDSTONE.defaultBlockState();
        };
    }

    // ========== WOOD TYPES ==========

    private static BlockState translatePlanks(int meta) {
        return switch (meta) {
            case 0 -> Blocks.OAK_PLANKS.defaultBlockState();
            case 1 -> Blocks.SPRUCE_PLANKS.defaultBlockState();
            case 2 -> Blocks.BIRCH_PLANKS.defaultBlockState();
            case 3 -> Blocks.JUNGLE_PLANKS.defaultBlockState();
            case 4 -> Blocks.ACACIA_PLANKS.defaultBlockState();
            case 5 -> Blocks.DARK_OAK_PLANKS.defaultBlockState();
            default -> Blocks.OAK_PLANKS.defaultBlockState();
        };
    }

    private static BlockState translateLog(Block block, int meta, boolean isLog2) {
        // Bits 0-1: wood type (for log2: 0=acacia, 1=dark_oak)
        // Bits 2-3: axis (0=y, 1=x, 2=z, 3=only bark)
        int woodType = meta & 0x3;
        int axisValue = (meta >> 2) & 0x3;

        Block logBlock = isLog2 ? switch (woodType) {
            case 0 -> Blocks.ACACIA_LOG;
            case 1 -> Blocks.DARK_OAK_LOG;
            default -> Blocks.ACACIA_LOG;
        } : switch (woodType) {
            case 0 -> Blocks.OAK_LOG;
            case 1 -> Blocks.SPRUCE_LOG;
            case 2 -> Blocks.BIRCH_LOG;
            case 3 -> Blocks.JUNGLE_LOG;
            default -> Blocks.OAK_LOG;
        };

        Direction.Axis axis = switch (axisValue) {
            case 0 -> Direction.Axis.Y;
            case 1 -> Direction.Axis.X;
            case 2 -> Direction.Axis.Z;
            case 3 -> Direction.Axis.Y; // All bark - use wood blocks in production
            default -> Direction.Axis.Y;
        };

        return logBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState translateSapling(int meta) {
        return switch (meta) {
            case 0 -> Blocks.OAK_SAPLING.defaultBlockState();
            case 1 -> Blocks.SPRUCE_SAPLING.defaultBlockState();
            case 2 -> Blocks.BIRCH_SAPLING.defaultBlockState();
            case 3 -> Blocks.JUNGLE_SAPLING.defaultBlockState();
            case 4 -> Blocks.ACACIA_SAPLING.defaultBlockState();
            case 5 -> Blocks.DARK_OAK_SAPLING.defaultBlockState();
            default -> Blocks.OAK_SAPLING.defaultBlockState();
        };
    }

    private static BlockState translateLeaves(int meta, boolean isLeaves2) {
        int leafType = meta & 0x3;

        Block leaves = isLeaves2 ? switch (leafType) {
            case 0 -> Blocks.ACACIA_LEAVES;
            case 1 -> Blocks.DARK_OAK_LEAVES;
            default -> Blocks.ACACIA_LEAVES;
        } : switch (leafType) {
            case 0 -> Blocks.OAK_LEAVES;
            case 1 -> Blocks.SPRUCE_LEAVES;
            case 2 -> Blocks.BIRCH_LEAVES;
            case 3 -> Blocks.JUNGLE_LEAVES;
            default -> Blocks.OAK_LEAVES;
        };

        return leaves.defaultBlockState();
    }

    // ========== STAIRS (CRITICAL FOR ORIENTATION!) ==========

    private static BlockState translateStairs(Block stairBlock, int meta) {
        // Bits 0-1: facing (0=EAST, 1=WEST, 2=SOUTH, 3=NORTH)
        // Bit 2: half (0=BOTTOM, 1=TOP/upside-down)
        // Bit 3: shape (we'll use default outer_right for now)

        int facingIndex = meta & 0x3;
        boolean isTop = (meta & 0x4) != 0;

        // Vanilla 1.12.2 stair metadata: bits 0-1 = facing
        // 0=EAST, 1=WEST, 2=SOUTH, 3=NORTH
        Direction facing = switch (facingIndex) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            case 3 -> Direction.NORTH;
            default -> Direction.NORTH;
        };

        Half half = isTop ? Half.TOP : Half.BOTTOM;

        return stairBlock.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half);
    }

    // ========== SLABS ==========

    private static BlockState translateSlab(String blockId, Block block, int meta) {
        // For 1.12.2 slabs: bits 0-2 = type, bit 3 = top/bottom
        boolean isTop = (meta & 0x8) != 0;
        int slabType = meta & 0x7;

        // Get the correct slab block based on type
        Block slabBlock = switch (blockId) {
            case "minecraft:stone_slab" -> switch (slabType) {
                case 0 -> Blocks.SMOOTH_STONE_SLAB;
                case 1 -> Blocks.SANDSTONE_SLAB;
                case 2 -> Blocks.OAK_SLAB; // Was petrified oak
                case 3 -> Blocks.COBBLESTONE_SLAB;
                case 4 -> Blocks.BRICK_SLAB;
                case 5 -> Blocks.STONE_BRICK_SLAB;
                case 6 -> Blocks.NETHER_BRICK_SLAB;
                case 7 -> Blocks.QUARTZ_SLAB;
                default -> Blocks.STONE_SLAB;
            };
            case "minecraft:wooden_slab" -> switch (slabType) {
                case 0 -> Blocks.OAK_SLAB;
                case 1 -> Blocks.SPRUCE_SLAB;
                case 2 -> Blocks.BIRCH_SLAB;
                case 3 -> Blocks.JUNGLE_SLAB;
                case 4 -> Blocks.ACACIA_SLAB;
                case 5 -> Blocks.DARK_OAK_SLAB;
                default -> Blocks.OAK_SLAB;
            };
            case "minecraft:stone_slab2" -> switch (slabType) {
                case 0 -> Blocks.RED_SANDSTONE_SLAB;
                default -> Blocks.RED_SANDSTONE_SLAB;
            };
            case "minecraft:purpur_slab" -> Blocks.PURPUR_SLAB;
            default -> block;
        };

        SlabType type = isTop ? SlabType.TOP : SlabType.BOTTOM;

        return slabBlock.defaultBlockState().setValue(SlabBlock.TYPE, type);
    }

    // ========== DOORS ==========

    private static BlockState translateDoor(Block doorBlock, int meta) {
        // Lower half (bit 3 = 0): bits 0-1 = facing, bit 2 = open
        // Upper half (bit 3 = 1): bit 0 = hinge side, bit 1 = powered

        boolean isUpperHalf = (meta & 0x8) != 0;

        if (isUpperHalf) {
            // Upper half
            DoorHingeSide hinge = (meta & 0x1) != 0 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT;
            return doorBlock.defaultBlockState()
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER)
                    .setValue(DoorBlock.HINGE, hinge);
        } else {
            // Lower half
            int facingIndex = meta & 0x3;
            boolean isOpen = (meta & 0x4) != 0;

            Direction facing = Direction.from2DDataValue(facingIndex);

            return doorBlock.defaultBlockState()
                    .setValue(DoorBlock.FACING, facing)
                    .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                    .setValue(DoorBlock.OPEN, isOpen);
        }
    }

    // ========== TORCHES ==========

    private static BlockState translateTorch(int meta) {
        // 1 = pointing east (attached to west wall)
        // 2 = pointing west (attached to east wall)
        // 3 = pointing south (attached to north wall)
        // 4 = pointing north (attached to south wall)
        // 5 or 0 = standing

        return switch (meta) {
            case 1 -> Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(WallTorchBlock.FACING, Direction.EAST);
            case 2 -> Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(WallTorchBlock.FACING, Direction.WEST);
            case 3 -> Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(WallTorchBlock.FACING, Direction.SOUTH);
            case 4 -> Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(WallTorchBlock.FACING, Direction.NORTH);
            case 5, 0 -> Blocks.TORCH.defaultBlockState(); // Standing
            default -> Blocks.TORCH.defaultBlockState();
        };
    }

    private static BlockState translateLever(Block lever, int meta) {
        // Similar to torch but with lever-specific attachment
        int facing = meta & 0x7;

        Direction dir = switch (facing) {
            case 0, 7 -> Direction.DOWN;
            case 1 -> Direction.EAST;
            case 2 -> Direction.WEST;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.NORTH;
            case 5, 6 -> Direction.UP;
            default -> Direction.DOWN;
        };

        return lever.defaultBlockState().setValue(LeverBlock.FACING, dir);
    }

    private static BlockState translateLadder(Block ladder, int meta) {
        Direction facing = switch (meta) {
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.NORTH;
        };

        return ladder.defaultBlockState().setValue(LadderBlock.FACING, facing);
    }

    private static BlockState translateVine(Block vine, int meta) {
        BlockState state = vine.defaultBlockState();

        // Vines use bit flags for each direction
        if ((meta & 0x1) != 0)
            state = state.setValue(VineBlock.SOUTH, true);
        if ((meta & 0x2) != 0)
            state = state.setValue(VineBlock.WEST, true);
        if ((meta & 0x4) != 0)
            state = state.setValue(VineBlock.NORTH, true);
        if ((meta & 0x8) != 0)
            state = state.setValue(VineBlock.EAST, true);

        return state;
    }

    // ========== BEDS ==========

    private static BlockState translateBed(int meta) {
        // Bits 0-1: facing (0=SOUTH, 1=WEST, 2=NORTH, 3=EAST)
        // Bit 2: occupied (ignored for building plans)
        // Bit 3: part (0=FOOT, 1=HEAD)

        int facingIndex = meta & 0x3;
        boolean isHead = (meta & 0x8) != 0;

        Direction facing = Direction.from2DDataValue(facingIndex);
        BedPart part = isHead ? BedPart.HEAD : BedPart.FOOT;

        // TODO: Support different bed colors based on additional data
        return Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, part);
    }

    // ========== WOOL AND COLORED BLOCKS ==========

    private static BlockState translateWool(int meta) {
        return switch (meta) {
            case 0 -> Blocks.WHITE_WOOL.defaultBlockState();
            case 1 -> Blocks.ORANGE_WOOL.defaultBlockState();
            case 2 -> Blocks.MAGENTA_WOOL.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_WOOL.defaultBlockState();
            case 4 -> Blocks.YELLOW_WOOL.defaultBlockState();
            case 5 -> Blocks.LIME_WOOL.defaultBlockState();
            case 6 -> Blocks.PINK_WOOL.defaultBlockState();
            case 7 -> Blocks.GRAY_WOOL.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_WOOL.defaultBlockState();
            case 9 -> Blocks.CYAN_WOOL.defaultBlockState();
            case 10 -> Blocks.PURPLE_WOOL.defaultBlockState();
            case 11 -> Blocks.BLUE_WOOL.defaultBlockState();
            case 12 -> Blocks.BROWN_WOOL.defaultBlockState();
            case 13 -> Blocks.GREEN_WOOL.defaultBlockState();
            case 14 -> Blocks.RED_WOOL.defaultBlockState();
            case 15 -> Blocks.BLACK_WOOL.defaultBlockState();
            default -> Blocks.WHITE_WOOL.defaultBlockState();
        };
    }

    private static BlockState translateStainedClay(int meta) {
        return switch (meta) {
            case 0 -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
            case 1 -> Blocks.ORANGE_TERRACOTTA.defaultBlockState();
            case 2 -> Blocks.MAGENTA_TERRACOTTA.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_TERRACOTTA.defaultBlockState();
            case 4 -> Blocks.YELLOW_TERRACOTTA.defaultBlockState();
            case 5 -> Blocks.LIME_TERRACOTTA.defaultBlockState();
            case 6 -> Blocks.PINK_TERRACOTTA.defaultBlockState();
            case 7 -> Blocks.GRAY_TERRACOTTA.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_TERRACOTTA.defaultBlockState();
            case 9 -> Blocks.CYAN_TERRACOTTA.defaultBlockState();
            case 10 -> Blocks.PURPLE_TERRACOTTA.defaultBlockState();
            case 11 -> Blocks.BLUE_TERRACOTTA.defaultBlockState();
            case 12 -> Blocks.BROWN_TERRACOTTA.defaultBlockState();
            case 13 -> Blocks.GREEN_TERRACOTTA.defaultBlockState();
            case 14 -> Blocks.RED_TERRACOTTA.defaultBlockState();
            case 15 -> Blocks.BLACK_TERRACOTTA.defaultBlockState();
            default -> Blocks.WHITE_TERRACOTTA.defaultBlockState();
        };
    }

    private static BlockState translateStainedGlass(int meta) {
        return switch (meta) {
            case 0 -> Blocks.WHITE_STAINED_GLASS.defaultBlockState();
            case 1 -> Blocks.ORANGE_STAINED_GLASS.defaultBlockState();
            case 2 -> Blocks.MAGENTA_STAINED_GLASS.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_STAINED_GLASS.defaultBlockState();
            case 4 -> Blocks.YELLOW_STAINED_GLASS.defaultBlockState();
            case 5 -> Blocks.LIME_STAINED_GLASS.defaultBlockState();
            case 6 -> Blocks.PINK_STAINED_GLASS.defaultBlockState();
            case 7 -> Blocks.GRAY_STAINED_GLASS.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_STAINED_GLASS.defaultBlockState();
            case 9 -> Blocks.CYAN_STAINED_GLASS.defaultBlockState();
            case 10 -> Blocks.PURPLE_STAINED_GLASS.defaultBlockState();
            case 11 -> Blocks.BLUE_STAINED_GLASS.defaultBlockState();
            case 12 -> Blocks.BROWN_STAINED_GLASS.defaultBlockState();
            case 13 -> Blocks.GREEN_STAINED_GLASS.defaultBlockState();
            case 14 -> Blocks.RED_STAINED_GLASS.defaultBlockState();
            case 15 -> Blocks.BLACK_STAINED_GLASS.defaultBlockState();
            default -> Blocks.WHITE_STAINED_GLASS.defaultBlockState();
        };
    }

    private static BlockState translateStainedGlassPane(int meta) {
        return switch (meta) {
            case 0 -> Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState();
            case 1 -> Blocks.ORANGE_STAINED_GLASS_PANE.defaultBlockState();
            case 2 -> Blocks.MAGENTA_STAINED_GLASS_PANE.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_STAINED_GLASS_PANE.defaultBlockState();
            case 4 -> Blocks.YELLOW_STAINED_GLASS_PANE.defaultBlockState();
            case 5 -> Blocks.LIME_STAINED_GLASS_PANE.defaultBlockState();
            case 6 -> Blocks.PINK_STAINED_GLASS_PANE.defaultBlockState();
            case 7 -> Blocks.GRAY_STAINED_GLASS_PANE.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_STAINED_GLASS_PANE.defaultBlockState();
            case 9 -> Blocks.CYAN_STAINED_GLASS_PANE.defaultBlockState();
            case 10 -> Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState();
            case 11 -> Blocks.BLUE_STAINED_GLASS_PANE.defaultBlockState();
            case 12 -> Blocks.BROWN_STAINED_GLASS_PANE.defaultBlockState();
            case 13 -> Blocks.GREEN_STAINED_GLASS_PANE.defaultBlockState();
            case 14 -> Blocks.RED_STAINED_GLASS_PANE.defaultBlockState();
            case 15 -> Blocks.BLACK_STAINED_GLASS_PANE.defaultBlockState();
            default -> Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState();
        };
    }

    private static BlockState translateCarpet(int meta) {
        return switch (meta) {
            case 0 -> Blocks.WHITE_CARPET.defaultBlockState();
            case 1 -> Blocks.ORANGE_CARPET.defaultBlockState();
            case 2 -> Blocks.MAGENTA_CARPET.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_CARPET.defaultBlockState();
            case 4 -> Blocks.YELLOW_CARPET.defaultBlockState();
            case 5 -> Blocks.LIME_CARPET.defaultBlockState();
            case 6 -> Blocks.PINK_CARPET.defaultBlockState();
            case 7 -> Blocks.GRAY_CARPET.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_CARPET.defaultBlockState();
            case 9 -> Blocks.CYAN_CARPET.defaultBlockState();
            case 10 -> Blocks.PURPLE_CARPET.defaultBlockState();
            case 11 -> Blocks.BLUE_CARPET.defaultBlockState();
            case 12 -> Blocks.BROWN_CARPET.defaultBlockState();
            case 13 -> Blocks.GREEN_CARPET.defaultBlockState();
            case 14 -> Blocks.RED_CARPET.defaultBlockState();
            case 15 -> Blocks.BLACK_CARPET.defaultBlockState();
            default -> Blocks.WHITE_CARPET.defaultBlockState();
        };
    }

    private static BlockState translateConcrete(int meta) {
        return switch (meta) {
            case 0 -> Blocks.WHITE_CONCRETE.defaultBlockState();
            case 1 -> Blocks.ORANGE_CONCRETE.defaultBlockState();
            case 2 -> Blocks.MAGENTA_CONCRETE.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_CONCRETE.defaultBlockState();
            case 4 -> Blocks.YELLOW_CONCRETE.defaultBlockState();
            case 5 -> Blocks.LIME_CONCRETE.defaultBlockState();
            case 6 -> Blocks.PINK_CONCRETE.defaultBlockState();
            case 7 -> Blocks.GRAY_CONCRETE.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
            case 9 -> Blocks.CYAN_CONCRETE.defaultBlockState();
            case 10 -> Blocks.PURPLE_CONCRETE.defaultBlockState();
            case 11 -> Blocks.BLUE_CONCRETE.defaultBlockState();
            case 12 -> Blocks.BROWN_CONCRETE.defaultBlockState();
            case 13 -> Blocks.GREEN_CONCRETE.defaultBlockState();
            case 14 -> Blocks.RED_CONCRETE.defaultBlockState();
            case 15 -> Blocks.BLACK_CONCRETE.defaultBlockState();
            default -> Blocks.WHITE_CONCRETE.defaultBlockState();
        };
    }

    private static BlockState translateConcretePowder(int meta) {
        return switch (meta) {
            case 0 -> Blocks.WHITE_CONCRETE_POWDER.defaultBlockState();
            case 1 -> Blocks.ORANGE_CONCRETE_POWDER.defaultBlockState();
            case 2 -> Blocks.MAGENTA_CONCRETE_POWDER.defaultBlockState();
            case 3 -> Blocks.LIGHT_BLUE_CONCRETE_POWDER.defaultBlockState();
            case 4 -> Blocks.YELLOW_CONCRETE_POWDER.defaultBlockState();
            case 5 -> Blocks.LIME_CONCRETE_POWDER.defaultBlockState();
            case 6 -> Blocks.PINK_CONCRETE_POWDER.defaultBlockState();
            case 7 -> Blocks.GRAY_CONCRETE_POWDER.defaultBlockState();
            case 8 -> Blocks.LIGHT_GRAY_CONCRETE_POWDER.defaultBlockState();
            case 9 -> Blocks.CYAN_CONCRETE_POWDER.defaultBlockState();
            case 10 -> Blocks.PURPLE_CONCRETE_POWDER.defaultBlockState();
            case 11 -> Blocks.BLUE_CONCRETE_POWDER.defaultBlockState();
            case 12 -> Blocks.BROWN_CONCRETE_POWDER.defaultBlockState();
            case 13 -> Blocks.GREEN_CONCRETE_POWDER.defaultBlockState();
            case 14 -> Blocks.RED_CONCRETE_POWDER.defaultBlockState();
            case 15 -> Blocks.BLACK_CONCRETE_POWDER.defaultBlockState();
            default -> Blocks.WHITE_CONCRETE_POWDER.defaultBlockState();
        };
    }

    // ========== MISC BLOCKS ==========

    private static BlockState translateHorizontalFacing(Block block, int meta) {
        Direction facing = Direction.from2DDataValue(meta & 0x3);
        return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
    }

    private static BlockState translateDispenser(Block block, int meta) {
        Direction facing = Direction.from3DDataValue(meta & 0x7);
        return block.defaultBlockState().setValue(BlockStateProperties.FACING, facing);
    }

    private static BlockState translateRepeater(Block block, int meta) {
        int facingIndex = meta & 0x3;
        int delay = ((meta >> 2) & 0x3) + 1; // 0-3 → 1-4

        Direction facing = Direction.from2DDataValue(facingIndex);

        return block.defaultBlockState()
                .setValue(RepeaterBlock.FACING, facing)
                .setValue(RepeaterBlock.DELAY, delay);
    }

    private static BlockState translateComparator(Block block, int meta) {
        int facingIndex = meta & 0x3;
        boolean subtract = (meta & 0x4) != 0;

        Direction facing = Direction.from2DDataValue(facingIndex);

        return block.defaultBlockState()
                .setValue(ComparatorBlock.FACING, facing)
                .setValue(ComparatorBlock.MODE, subtract ? ComparatorMode.SUBTRACT : ComparatorMode.COMPARE);
    }

    private static BlockState translateCrop(Block block, int meta) {
        int age = meta & 0x7; // 0-7 growth stages
        return block.defaultBlockState().setValue(BlockStateProperties.AGE_7, age);
    }

    private static BlockState translateAnvil(int meta) {
        int damage = meta >> 2; // High 2 bits = damage level
        int facing = meta & 0x3; // Low 2 bits = facing

        Block anvil = switch (damage) {
            case 0 -> Blocks.ANVIL;
            case 1 -> Blocks.CHIPPED_ANVIL;
            case 2 -> Blocks.DAMAGED_ANVIL;
            default -> Blocks.ANVIL;
        };

        Direction dir = Direction.from2DDataValue(facing);
        return anvil.defaultBlockState().setValue(AnvilBlock.FACING, dir);
    }

    private static BlockState translateCauldron(Block block, int meta) {
        int level = meta & 0x3; // 0-3 fill level
        return block.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, level);
    }

    private static BlockState translateFarmland(Block block, int meta) {
        int moisture = meta & 0x7; // 0-7 moisture level
        return block.defaultBlockState().setValue(FarmBlock.MOISTURE, moisture);
    }

    private static BlockState translateCake(Block block, int meta) {
        int bites = meta & 0x7; // 0-6 bites taken
        return block.defaultBlockState().setValue(CakeBlock.BITES, Math.min(bites, 6));
    }

    private static BlockState translateRail(Block block, int meta) {
        if (block instanceof PoweredRailBlock) {
            int shape = meta & 0x7;
            boolean powered = (meta & 0x8) != 0;

            RailShape railShape = switch (shape) {
                case 0 -> RailShape.NORTH_SOUTH;
                case 1 -> RailShape.EAST_WEST;
                case 2 -> RailShape.ASCENDING_EAST;
                case 3 -> RailShape.ASCENDING_WEST;
                case 4 -> RailShape.ASCENDING_NORTH;
                case 5 -> RailShape.ASCENDING_SOUTH;
                default -> RailShape.NORTH_SOUTH;
            };

            return block.defaultBlockState()
                    .setValue(PoweredRailBlock.SHAPE, railShape)
                    .setValue(PoweredRailBlock.POWERED, powered);
        } else {
            // Regular rail with more shapes
            RailShape shape = switch (meta) {
                case 0 -> RailShape.NORTH_SOUTH;
                case 1 -> RailShape.EAST_WEST;
                case 2 -> RailShape.ASCENDING_EAST;
                case 3 -> RailShape.ASCENDING_WEST;
                case 4 -> RailShape.ASCENDING_NORTH;
                case 5 -> RailShape.ASCENDING_SOUTH;
                case 6 -> RailShape.SOUTH_EAST;
                case 7 -> RailShape.SOUTH_WEST;
                case 8 -> RailShape.NORTH_WEST;
                case 9 -> RailShape.NORTH_EAST;
                default -> RailShape.NORTH_SOUTH;
            };

            return block.defaultBlockState().setValue(RailBlock.SHAPE, shape);
        }
    }

    // === NEW HELPER METHODS for 1.12.2→1.20.1 translation ===

    /**
     * Translate double_stone_slab metadata to full block equivalents.
     * In 1.12.2, double slabs were combined blocks; in 1.20.1 they're separate.
     */
    private static BlockState translateDoubleStoneSlab(int meta) {
        return switch (meta & 7) {
            case 0 -> Blocks.SMOOTH_STONE.defaultBlockState(); // stone double slab
            case 1 -> Blocks.SANDSTONE.defaultBlockState(); // sandstone double slab
            case 2 -> Blocks.OAK_PLANKS.defaultBlockState(); // petrified oak (legacy)
            case 3 -> Blocks.COBBLESTONE.defaultBlockState(); // cobblestone
            case 4 -> Blocks.BRICKS.defaultBlockState(); // brick
            case 5 -> Blocks.STONE_BRICKS.defaultBlockState(); // stone brick
            case 6 -> Blocks.NETHER_BRICKS.defaultBlockState();// nether brick
            case 7 -> Blocks.QUARTZ_BLOCK.defaultBlockState(); // quartz
            default -> Blocks.SMOOTH_STONE.defaultBlockState();
        };
    }

    /**
     * Translate wooden_slab metadata to wood-specific slabs.
     */
    private static BlockState translateWoodenSlab(int meta) {
        boolean top = (meta & 8) != 0;
        Block slab = switch (meta & 7) {
            case 0 -> Blocks.OAK_SLAB;
            case 1 -> Blocks.SPRUCE_SLAB;
            case 2 -> Blocks.BIRCH_SLAB;
            case 3 -> Blocks.JUNGLE_SLAB;
            case 4 -> Blocks.ACACIA_SLAB;
            case 5 -> Blocks.DARK_OAK_SLAB;
            default -> Blocks.OAK_SLAB;
        };
        BlockState state = slab.defaultBlockState();
        if (top) {
            state = state.setValue(SlabBlock.TYPE, SlabType.TOP);
        }
        return state;
    }

    /**
     * Translate tallgrass metadata to appropriate grass/fern blocks.
     */
    private static BlockState translateTallGrass(int meta) {
        return switch (meta) {
            case 0 -> Blocks.DEAD_BUSH.defaultBlockState(); // shrub/dead bush
            case 1 -> Blocks.GRASS.defaultBlockState(); // grass (renamed from SHORT_GRASS in 1.20.1)
            case 2 -> Blocks.FERN.defaultBlockState(); // fern
            default -> Blocks.GRASS.defaultBlockState();
        };
    }

    /**
     * Translate trapdoor metadata to facing/half/open properties.
     */
    private static BlockState translateTrapdoor(int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.SOUTH;
            case 1 -> Direction.NORTH;
            case 2 -> Direction.EAST;
            case 3 -> Direction.WEST;
            default -> Direction.NORTH;
        };
        boolean open = (meta & 4) != 0;
        boolean top = (meta & 8) != 0;

        return Blocks.OAK_TRAPDOOR.defaultBlockState()
                .setValue(TrapDoorBlock.FACING, facing)
                .setValue(TrapDoorBlock.OPEN, open)
                .setValue(TrapDoorBlock.HALF, top ? Half.TOP : Half.BOTTOM);
    }
}
