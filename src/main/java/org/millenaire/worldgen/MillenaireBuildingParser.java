package org.millenaire.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.WallTorchBlock;

import org.millenaire.core.MillBlocks;

/**
 * Parser for MillÃƒÂ©naire's PNG-based building plans.
 * 
 * Original MillÃƒÂ©naire uses PNG images where each pixel's RGB color maps to a
 * block
 * or special feature (starting position, chests, signs, etc.).
 * 
 * Buildings are located in src/main/resources/assets/millenaire/buildings/
 * e.g., "norman/townhall_A0.png"
 * 
 * Color mappings extracted from original blocklist.txt (1,551 mappings total)
 */
public class MillenaireBuildingParser {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Map packed RGB int to BlockState
    private static final Map<Integer, BlockState> colorMap = new HashMap<>();

    // Map for special block types (non-block markers)
    private static final Map<Integer, SpecialMarker> specialMarkers = new HashMap<>();

    static {
        // CRITICAL: Load blocklist.txt FIRST for authentic blockstate mappings
        loadBlocklistMappings();
        // Then add manual overrides/additions
        initializeColorMappings();
    }

    /**
     * Parse blocklist.txt to extract RGB Ã¢â€ â€™ BlockState mappings.
     * Format: name;blockId;properties;flag;R/G/B;costItem;costProps;costQty
     * Example: torchTop;minecraft:torch;facing=west;true;255/255/1;anywood;;1
     */
    private static void loadBlocklistMappings() {
        try (InputStream is = MillenaireBuildingParser.class.getResourceAsStream(
                "/assets/millenaire/blocklist.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            int lineNum = 0;
            int mappedCount = 0;
            int failureCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                // Parse line format:
                // name;blockId;properties;flag;RGB;costItem;costProps;costQty
                String[] parts = line.split(";", -1);
                if (parts.length < 5) {
                    continue; // Skip malformed lines
                }

                String name = parts[0].trim();
                String blockId = parts[1].trim();
                String metadataStr = parts[2].trim(); // CHANGED: metadata field, not properties!
                String rgbString = parts[4].trim();

                if (name.equalsIgnoreCase("pathStartPos")) {
                    int rgbKey = parseRGB(rgbString);
                    if (rgbKey != -1) {
                        specialMarkers.put(rgbKey, SpecialMarker.PATH_START_POS);
                        LOGGER.info("[Special Marker] Mapped pathStartPos to RGB {}", rgbString);
                    }
                    continue;
                }

                // Skip special markers (empty blockId or no colon)
                if (blockId.isEmpty() || blockId.equals("0") || !blockId.contains(":")) {
                    continue;
                }

                // Parse RGB
                int rgbKey = parseRGB(rgbString);
                if (rgbKey == -1) {
                    continue; // Invalid RGB
                }

                // Parse metadata value
                int metadata = parseMetadata(metadataStr);

                // Translate metadata -> BlockState using MetadataTranslator
                BlockState state = MetadataTranslator.translate(blockId, metadata, metadataStr);

                // FIXED: Explicit handling for known "failing" blocks if translator returns
                // null
                if (state == null) {
                    if (blockId.equals("minecraft:air")) {
                        state = Blocks.AIR.defaultBlockState();
                    } else if (blockId.equals("millenaire:byzantineiconmedium")) {
                        state = Blocks.FURNACE.defaultBlockState();
                    }
                }

                if (state != null) {
                    colorMap.put(rgbKey, state);
                    mappedCount++;
                    // DEBUG: Log thatch color specifically
                    if (rgbString.equals("200/200/120")) {
                        LOGGER.info("[THATCH DEBUG] RGB 200/200/120 mapped to: {} (block: {})",
                                state, state.getBlock().getName().getString());
                    }
                } else {
                    // Log first 50 failures to diagnose why so many are missing
                    if (failureCount < 50) {
                        LOGGER.warn("[Mapping Failure] ID: '{}' Meta: {} Str: '{}' RGB: {}",
                                blockId, metadata, metadataStr, rgbString);
                        failureCount++;
                    }
                }
            }

            LOGGER.info("[Metadata Translator] Loaded {} block mappings from blocklist.txt (target: 1551)",
                    mappedCount);

            // =================================================================
            // FALLBACK MAPPINGS - ONLY added if NOT already in blocklist.txt
            // Using putIfAbsent() ensures blocklist.txt mappings take priority!
            // =================================================================

            // Thatch roofing (Millénaire custom block)
            colorMap.putIfAbsent(rgb(255, 194, 0), MillBlocks.WOOD_DECORATION.get().defaultBlockState());

            // CRITICAL FIX: RGB(255,0,0) = AIR (interior empty space, NOT blocks!)
            colorMap.putIfAbsent(rgb(255, 0, 0), Blocks.AIR.defaultBlockState());

            // Fallback vanilla blocks for colors commonly used but possibly missing from
            // blocklist
            colorMap.putIfAbsent(rgb(70, 70, 70), Blocks.GRAVEL.defaultBlockState()); // freegravel
            colorMap.putIfAbsent(rgb(255, 255, 0), Blocks.TORCH.defaultBlockState()); // torchGuess

            // ladderGuess = auto-detect wall and set facing (RGB 89/64/0 from original
            // blocklist)
            // Default to NORTH facing, will be corrected during placement by wall detection
            colorMap.putIfAbsent(rgb(89, 64, 0), Blocks.LADDER.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH));
            // Signs with facing directions - using vanilla wall signs as fallback for
            // millenaire:panel
            // The sign pixel is placed AT the wall block position, sign should face OUTWARD
            // signwallTop = wall is to NORTH (top of PNG) → sign faces SOUTH
            // signwallBottom = wall is to SOUTH → sign faces NORTH
            // signwallLeft = wall is to WEST (left of PNG) → sign faces EAST
            // signwallRight = wall is to EAST → sign faces WEST
            colorMap.putIfAbsent(rgb(0, 0, 219), Blocks.OAK_WALL_SIGN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallSignBlock.FACING, Direction.SOUTH)); // signwallGuess
            colorMap.putIfAbsent(rgb(0, 0, 220), Blocks.OAK_WALL_SIGN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallSignBlock.FACING, Direction.SOUTH)); // signwallTop
            colorMap.putIfAbsent(rgb(0, 0, 221), Blocks.OAK_WALL_SIGN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallSignBlock.FACING, Direction.NORTH)); // signwallBottom
            colorMap.putIfAbsent(rgb(0, 0, 222), Blocks.OAK_WALL_SIGN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallSignBlock.FACING, Direction.EAST)); // signwallLeft
            colorMap.putIfAbsent(rgb(0, 0, 223), Blocks.OAK_WALL_SIGN.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallSignBlock.FACING, Direction.WEST)); // signwallRight
            colorMap.putIfAbsent(rgb(100, 10, 62), Blocks.BRICKS.defaultBlockState()); // decorated brick fallback
            colorMap.putIfAbsent(rgb(70, 0, 160), Blocks.PURPLE_WOOL.defaultBlockState()); // purple color fallback

            // === NEW: Missing colors discovered during multi-culture testing ===

            // RGB(25,25,25) = furnaceGuess OR byzantineiconmedium
            // Japanese buildings use this for furnace placement; map to furnace
            colorMap.putIfAbsent(rgb(25, 25, 25), Blocks.FURNACE.defaultBlockState());

            // RGB(161,0,X) = bedThatch with different facings (millenaire:bed_straw)
            // Norman/Byzantine buildings use straw beds; map to vanilla red bed for now
            // Beds are 2-block structures: HEAD + FOOT. We place the HEAD with correct
            // facing.
            // Note: The HEAD points TOWARD the pillow, FACING is the direction you look
            // when lying down
            colorMap.putIfAbsent(rgb(161, 0, 0), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.HEAD)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.NORTH));
            colorMap.putIfAbsent(rgb(161, 0, 1), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.HEAD)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.SOUTH));
            colorMap.putIfAbsent(rgb(161, 0, 2), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.HEAD)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.WEST));
            colorMap.putIfAbsent(rgb(161, 0, 3), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.HEAD)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.EAST));
            // Bed FOOT parts - placed at -1 offset from HEAD in facing direction
            colorMap.putIfAbsent(rgb(161, 1, 0), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.FOOT)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.NORTH));
            colorMap.putIfAbsent(rgb(161, 1, 1), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.FOOT)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.SOUTH));
            colorMap.putIfAbsent(rgb(161, 1, 2), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.FOOT)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.WEST));
            colorMap.putIfAbsent(rgb(161, 1, 3), Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.FOOT)
                    .setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING, Direction.EAST));

            // RGB(0,0,120) = fishingspot (special marker, not a real block)
            // Byzantine fishfarm uses this; map to water for visual approximation
            colorMap.putIfAbsent(rgb(0, 0, 120), Blocks.WATER.defaultBlockState());

            // Village and culture banners (wall-mounted) - special markers
            colorMap.putIfAbsent(rgb(0, 0, 226), createWallBanner(Direction.EAST));
            colorMap.putIfAbsent(rgb(0, 0, 227), createWallBanner(Direction.SOUTH));
            colorMap.putIfAbsent(rgb(0, 0, 228), createWallBanner(Direction.WEST));
            colorMap.putIfAbsent(rgb(0, 0, 229), createWallBanner(Direction.NORTH));

            colorMap.putIfAbsent(rgb(0, 1, 226), createWallBanner(Direction.EAST));
            colorMap.putIfAbsent(rgb(0, 1, 227), createWallBanner(Direction.SOUTH));
            colorMap.putIfAbsent(rgb(0, 1, 228), createWallBanner(Direction.WEST));
            colorMap.putIfAbsent(rgb(0, 1, 229), createWallBanner(Direction.NORTH));

            // Standing banners (16 rotations each for village and culture)
            for (int rotation = 0; rotation <= 15; rotation++) {
                int colorB = 230 + rotation;
                colorMap.putIfAbsent(rgb(0, 0, colorB), createStandingBanner(rotation));
                colorMap.putIfAbsent(rgb(0, 1, colorB), createStandingBanner(rotation));
            }

            // Total mappings from blocklist.txt loaded above
            LOGGER.info("[Color Mapping] Using {} blocklist.txt entries as the authoritative source", mappedCount);

        } catch (IOException | NullPointerException e) {
            LOGGER.warn("[BlockState Parser] Could not load blocklist.txt, using manual mappings only", e);
        }
    }

    /**
     * Create a wall banner with the specified facing direction
     */
    private static BlockState createWallBanner(Direction facing) {
        return Blocks.WHITE_WALL_BANNER.defaultBlockState()
                .setValue(WallBannerBlock.FACING, facing);
    }

    /**
     * Create a standing banner with the specified rotation (0-15)
     */
    private static BlockState createStandingBanner(int rotation) {
        return Blocks.WHITE_BANNER.defaultBlockState()
                .setValue(BannerBlock.ROTATION, rotation);
    }

    /**
     * Parse metadata string to integer.
     * Returns -1 if properties instead of numeric metadata.
     */
    private static int parseMetadata(String metaStr) {
        try {
            return Integer.parseInt(metaStr);
        } catch (NumberFormatException e) {
            return -1; // Property string - use property parsing
        }
    }

    /**
     * Parse RGB string "R/G/B" to packed integer
     */
    private static int parseRGB(String rgbString) {
        try {
            String[] rgb = rgbString.split("/");
            if (rgb.length != 3)
                return -1;

            int r = Integer.parseInt(rgb[0].trim());
            int g = Integer.parseInt(rgb[1].trim());
            int b = Integer.parseInt(rgb[2].trim());

            return (r << 16) | (g << 8) | b;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Parse BlockState from blockId and properties string.
     * Example: "minecraft:torch" + "facing=west" Ã¢â€ â€™ Torch facing west
     */
    private static BlockState parseBlockState(String blockId, String propertiesStr) {
        try {
            // Get block from registry
            ResourceLocation blockRes = new ResourceLocation(blockId);
            Block block = ForgeRegistries.BLOCKS.getValue(blockRes);

            if (block == null || block == Blocks.AIR) {
                return null; // Block not found, will use fallback
            }

            BlockState state = block.defaultBlockState();

            // Parse and apply properties
            if (propertiesStr != null && !propertiesStr.isEmpty() && !propertiesStr.equals("0")) {
                String[] props = propertiesStr.split(",");
                for (String prop : props) {
                    String[] kv = prop.split("=", 2);
                    if (kv.length == 2) {
                        state = applyProperty(state, kv[0].trim(), kv[1].trim());
                    }
                }
            }

            return state;

        } catch (Exception e) {
            // Silently skip blocks that fail to parse (likely MillÃƒÂ©naire custom blocks)
            return null;
        }
    }

    /**
     * Apply a single property to a BlockState.
     */
    private static BlockState applyProperty(BlockState state, String key, String value) {
        try {
            switch (key.toLowerCase()) {
                case "facing":
                    Direction dir = parseDirection(value);
                    if (dir != null && state.hasProperty(BlockStateProperties.FACING)) {
                        return state.setValue(BlockStateProperties.FACING, dir);
                    } else if (dir != null && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir);
                    }
                    break;

                case "half":
                    if (value.equalsIgnoreCase("top") && state.hasProperty(BlockStateProperties.HALF)) {
                        return state.setValue(BlockStateProperties.HALF, Half.TOP);
                    } else if (value.equalsIgnoreCase("bottom") && state.hasProperty(BlockStateProperties.HALF)) {
                        return state.setValue(BlockStateProperties.HALF, Half.BOTTOM);
                    } else if (value.equalsIgnoreCase("upper")
                            && state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                        return state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                    } else if (value.equalsIgnoreCase("lower")
                            && state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                        return state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
                    }
                    break;

                case "part":
                    if (value.equalsIgnoreCase("head") && state.hasProperty(BedBlock.PART)) {
                        return state.setValue(BedBlock.PART, BedPart.HEAD);
                    } else if (value.equalsIgnoreCase("foot") && state.hasProperty(BedBlock.PART)) {
                        return state.setValue(BedBlock.PART, BedPart.FOOT);
                    }
                    break;

                case "open":
                    if (state.hasProperty(BlockStateProperties.OPEN)) {
                        return state.setValue(BlockStateProperties.OPEN, Boolean.parseBoolean(value));
                    }
                    break;

                case "hinge":
                    if (value.equalsIgnoreCase("left") && state.hasProperty(BlockStateProperties.DOOR_HINGE)) {
                        return state.setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT);
                    } else if (value.equalsIgnoreCase("right") && state.hasProperty(BlockStateProperties.DOOR_HINGE)) {
                        return state.setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.RIGHT);
                    }
                    break;

                case "powered":
                    if (state.hasProperty(BlockStateProperties.POWERED)) {
                        return state.setValue(BlockStateProperties.POWERED, Boolean.parseBoolean(value));
                    }
                    break;
            }
        } catch (Exception e) {
            // Skip property if it fails
        }
        return state;
    }

    /**
     * Parse direction from string (north, south, east, west, up, down)
     */
    private static Direction parseDirection(String dir) {
        switch (dir.toLowerCase()) {
            case "north":
                return Direction.NORTH;
            case "south":
                return Direction.SOUTH;
            case "east":
                return Direction.EAST;
            case "west":
                return Direction.WEST;
            case "up":
                return Direction.UP;
            case "down":
                return Direction.DOWN;
            default:
                return null;
        }
    }

    private static void initializeColorMappings() {
        // Special markers first (non-block features)
        addSpecial(255, 255, 255, SpecialMarker.EMPTY); // Air/empty
        addSpecial(0, 200, 0, SpecialMarker.PRESERVE_GROUND);
        addSpecial(150, 255, 150, SpecialMarker.ALL_BUT_TREES);
        addSpecial(0, 128, 0, SpecialMarker.GRASS);
        addSpecial(0, 128, 255, SpecialMarker.SLEEPING_POS);
        addSpecial(200, 0, 0, SpecialMarker.SELLING_POS);
        addSpecial(200, 125, 0, SpecialMarker.CRAFTING_POS);
        addSpecial(200, 150, 0, SpecialMarker.DEFENDING_POS);
        addSpecial(200, 175, 0, SpecialMarker.SHELTER_POS);
        addSpecial(200, 250, 0, SpecialMarker.PATH_START_POS);
        addSpecial(50, 50, 250, SpecialMarker.LEISURE_POS);

        // Main chests (with orientations)
        addSpecial(0, 0, 255, SpecialMarker.MAIN_CHEST_GUESS);
        addSpecial(0, 1, 255, SpecialMarker.MAIN_CHEST_TOP);
        addSpecial(0, 2, 255, SpecialMarker.MAIN_CHEST_BOTTOM);
        addSpecial(0, 3, 255, SpecialMarker.MAIN_CHEST_LEFT);
        addSpecial(0, 4, 255, SpecialMarker.MAIN_CHEST_RIGHT);

        // Locked chests
        addSpecial(135, 30, 0, SpecialMarker.LOCKED_CHEST_GUESS);
        addSpecial(135, 30, 1, SpecialMarker.LOCKED_CHEST_TOP);
        addSpecial(135, 30, 2, SpecialMarker.LOCKED_CHEST_BOTTOM);
        addSpecial(135, 30, 3, SpecialMarker.LOCKED_CHEST_LEFT);
        addSpecial(135, 30, 4, SpecialMarker.LOCKED_CHEST_RIGHT);

        // Regular Minecraft chests (for player houses)
        addChest(128, 32, 0, "east");
        addChest(128, 33, 0, "west");
        addChest(128, 34, 0, "north");
        addChest(128, 35, 0, "south");

        // Farm soils and spawn markers
        addSpecial(64, 128, 0, SpecialMarker.SOIL);
        addSpecial(100, 10, 10, SpecialMarker.COW_SPAWN);
        addSpecial(120, 10, 10, SpecialMarker.PIG_SPAWN);
        addSpecial(140, 10, 10, SpecialMarker.SHEEP_SPAWN);
        addSpecial(160, 10, 10, SpecialMarker.CHICKEN_SPAWN);

        // ========================================================================
        // VANILLA BLOCKS NOW LOADED FROM BLOCKLIST.TXT
        // All vanilla blocks (stairs, doors, torches, beds, slabs, etc.) are now
        // loaded from blocklist.txt with full blockstate orientation data.
        // DO NOT add manual addBlock() calls here as they will override the
        // blockstate mappings and break orientations!
        // ========================================================================

        // Millénaire custom blocks - TEMPORARY vanilla stand-ins until proper
        // multi-variant blocks are implemented. The original wood_deco had 16 variants
        // but currently only renders with thatch texture.

        // Timber frame (colombages) - use vanilla blocks as stand-ins
        // TODO: Implement BlockDecorativeWood with proper variants
        colorMap.putIfAbsent(rgb(55, 32, 10), Blocks.STRIPPED_OAK_LOG.defaultBlockState()); // colombages plain
        colorMap.putIfAbsent(rgb(36, 21, 6), Blocks.OAK_PLANKS.defaultBlockState()); // colombages cross

        // Thatch - this IS the wood_deco texture, so keep it
        addMillBlock(200, 200, 120, "millenaire:wood_deco", 2); // thatch

        // Stone deco variants
        addMillBlock(166, 156, 136, "millenaire:stone_deco", 0); // mud brick

        // Paper wall - Japanese
        colorMap.putIfAbsent(rgb(220, 251, 243), Blocks.WHITE_CONCRETE.defaultBlockState()); // paper wall stand-in

        // Painted bricks - use vanilla terracotta as stand-ins
        colorMap.putIfAbsent(rgb(219, 216, 207), Blocks.WHITE_TERRACOTTA.defaultBlockState());
        colorMap.putIfAbsent(rgb(219, 216, 208), Blocks.ORANGE_TERRACOTTA.defaultBlockState());
        colorMap.putIfAbsent(rgb(219, 216, 218), Blocks.BLUE_TERRACOTTA.defaultBlockState());

        // Byzantine tiles
        addMillBlock(189, 176, 145, "millenaire:byzantine_tiles", 0);
        colorMap.putIfAbsent(rgb(14, 43, 216), Blocks.RED_TERRACOTTA.defaultBlockState()); // Byzantine mosaic red
        colorMap.putIfAbsent(rgb(14, 43, 205), Blocks.BLUE_TERRACOTTA.defaultBlockState()); // Byzantine mosaic blue

        // Mayan blocks
        colorMap.putIfAbsent(rgb(255, 213, 1), Blocks.GOLD_BLOCK.defaultBlockState()); // Mayan gold

        // Missing Mappings from Logs
        colorMap.putIfAbsent(rgb(255, 0, 0), Blocks.AIR.defaultBlockState()); // Marker/Empty
        colorMap.putIfAbsent(rgb(104, 78, 28), Blocks.OAK_PRESSURE_PLATE.defaultBlockState()); // Wooden plate
        colorMap.putIfAbsent(rgb(58, 177, 188), Blocks.SNOW.defaultBlockState()); // Snow layer
        colorMap.putIfAbsent(rgb(232, 57, 16), Blocks.MAGMA_BLOCK.defaultBlockState()); // Lava/Magma fallback

        // Farm soil markers - these are placeholder positions for where crops are
        // planted
        // Map them to appropriate ground blocks until proper crop placement is
        // implemented
        colorMap.putIfAbsent(rgb(64, 128, 0), Blocks.FARMLAND.defaultBlockState()); // soil (general farming)
        colorMap.putIfAbsent(rgb(64, 128, 10), Blocks.FARMLAND.defaultBlockState()); // ricesoil - flooded paddy
                                                                                     // (villagers plant rice here)
        colorMap.putIfAbsent(rgb(64, 128, 20), Blocks.FARMLAND.defaultBlockState()); // turmericsoil
        colorMap.putIfAbsent(rgb(64, 128, 30), Blocks.DIRT.defaultBlockState()); // sugarcanesoil
        colorMap.putIfAbsent(rgb(64, 128, 40), Blocks.FARMLAND.defaultBlockState()); // maizesoil
        colorMap.putIfAbsent(rgb(64, 128, 50), Blocks.FARMLAND.defaultBlockState()); // carrotsoil
        colorMap.putIfAbsent(rgb(64, 128, 55), Blocks.GRASS_BLOCK.defaultBlockState()); // flowersoil
        colorMap.putIfAbsent(rgb(64, 128, 60), Blocks.FARMLAND.defaultBlockState()); // cottonsoil
        colorMap.putIfAbsent(rgb(64, 128, 100), Blocks.FARMLAND.defaultBlockState()); // potatosoil
        colorMap.putIfAbsent(rgb(100, 10, 62), Blocks.SOUL_SAND.defaultBlockState()); // netherwartsoil
        colorMap.putIfAbsent(rgb(0, 256, 256), Blocks.GRASS_BLOCK.defaultBlockState()); // vinesoil
        colorMap.putIfAbsent(rgb(58, 23, 9), Blocks.JUNGLE_LOG.defaultBlockState()); // cacaospot

        // Tree saplings/grove markers
        colorMap.putIfAbsent(rgb(34, 53, 34), Blocks.OAK_SAPLING.defaultBlockState()); // oak tree spot
        colorMap.putIfAbsent(rgb(0, 100, 0), Blocks.DARK_OAK_SAPLING.defaultBlockState()); // dark oak tree spot

        // Note: Full color map initialization continues...
        // This is a subset showing the pattern. Complete implementation will include
        // all 1,551 color mappings from blocklist.txt
    }

    private static void addBlock(int r, int g, int b, Block block) {
        // Use putIfAbsent so blocklist.txt mappings take priority
        colorMap.putIfAbsent(rgb(r, g, b), block.defaultBlockState());
    }

    private static void addMillBlock(int r, int g, int b, String blockId, int meta) {
        // Lookup Millenaire block from registry
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockId));
        if (block != null) {
            // Use putIfAbsent so blocklist.txt mappings take priority
            colorMap.putIfAbsent(rgb(r, g, b), block.defaultBlockState());
        } else {
            // Fallback to stone if block not found, but only if no existing mapping
            LOGGER.warn("Millenaire block not found: {}", blockId);
            colorMap.putIfAbsent(rgb(r, g, b), Blocks.STONE.defaultBlockState());
        }
    }

    private static void addChest(int r, int g, int b, String facing) {
        // TODO: Add proper chest with facing direction
        colorMap.put(rgb(r, g, b), Blocks.CHEST.defaultBlockState());
    }

    private static void addSpecial(int r, int g, int b, SpecialMarker marker) {
        specialMarkers.put(rgb(r, g, b), marker);
    }

    private static int rgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Loads multiple level files (A0, A1, A2...) and combines their floors into a
     * single plan.
     * Each level file may contain multiple floors which are all stacked together.
     */
    private static BuildingPlan loadMultiLevelPlan(List<String> levelPaths) {
        List<BuildingPlan> levelPlans = new ArrayList<>();

        // Load each level file as a separate plan
        for (String path : levelPaths) {
            try {
                BuildingPlan levelPlan = loadSinglePlan(path);
                if (levelPlan != null) {
                    levelPlans.add(levelPlan);
                    LOGGER.info("[DEBUG] Loaded level {} with {} floors", path, levelPlan.nbFloors());
                }
            } catch (Exception e) {
                LOGGER.error("[ERROR] Failed to load level file: {} - {}", path, e.getMessage());
            }
        }

        if (levelPlans.isEmpty()) {
            throw new RuntimeException("Failed to load any level files for multi-level building");
        }

        // Calculate total floors and verify dimensions match
        int totalFloors = 0;
        int width = levelPlans.get(0).width();
        int length = levelPlans.get(0).length();

        for (BuildingPlan plan : levelPlans) {
            totalFloors += plan.nbFloors();
            // Use the largest dimensions found
            width = Math.max(width, plan.width());
            length = Math.max(length, plan.length());
        }

        LOGGER.info("[DEBUG] Combining {} level files into {} total floors ({}x{})",
                levelPlans.size(), totalFloors, width, length);

        // Create combined blocks array
        BlockState[][][] combinedBlocks = new BlockState[totalFloors][length][width];

        // Copy floors from each level plan
        int currentFloor = 0;
        for (BuildingPlan plan : levelPlans) {
            for (int f = 0; f < plan.nbFloors(); f++) {
                for (int z = 0; z < plan.length(); z++) {
                    for (int x = 0; x < plan.width(); x++) {
                        combinedBlocks[currentFloor][z][x] = plan.blocks()[f][z][x];
                    }
                }
                currentFloor++;
            }
        }

        int nativeLevel = levelPlans.isEmpty() ? 0 : levelPlans.get(0).nativeLevel();
        BlockPos combinedPathStart = levelPlans.isEmpty() ? null : levelPlans.get(0).pathStartPos();

        return new BuildingPlan(combinedBlocks, width, length, totalFloors, null,
                levelPaths.get(0).replace("_A0.png", "") + " (combined)", nativeLevel, 0, -1, combinedPathStart);
    }

    /**
     * Loads a single PNG building plan file.
     */
    private static BuildingPlan loadSinglePlan(String path) throws IOException {
        String fullPath = "assets/millenaire/buildings/" + path;

        try (InputStream is = MillenaireBuildingParser.class.getClassLoader()
                .getResourceAsStream(fullPath)) {

            if (is == null) {
                throw new IOException("Building plan not found: " + path);
            }

            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IOException("Failed to read image: " + path);
            }

            return parseSingleImage(image, path);
        }
    }

    /**
     * Parses a single PNG image into a BuildingPlan (extracted from original
     * loadPlan logic).
     */
    private static BuildingPlan parseSingleImage(BufferedImage image, String sourceFile) {
        // Ensure color mappings are initialized
        initializeColorMappings();

        int width = image.getWidth();
        int height = image.getHeight();

        // Same parsing logic as original loadPlan - detect floors by separator columns
        int buildingWidth = width;
        int floorWidth = width;
        int nbFloors = 1;
        int buildingDepth = height;

        // Scan for vertical separator columns
        List<Integer> separatorColumns = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            int firstPixelRGB = image.getRGB(x, 0) & 0xFFFFFF;
            boolean isSolidColumn = true;
            for (int z = 1; z < height; z++) {
                int rgb = image.getRGB(x, z) & 0xFFFFFF;
                if (rgb != firstPixelRGB) {
                    isSolidColumn = false;
                    break;
                }
            }
            if (isSolidColumn) {
                int r = (firstPixelRGB >> 16) & 0xFF;
                int g = (firstPixelRGB >> 8) & 0xFF;
                int b = firstPixelRGB & 0xFF;
                boolean isPureBlack = (firstPixelRGB == 0);
                boolean isPaleGreen = (r >= 175 && r <= 180) && (g >= 250) && (b >= 175 && b <= 180);
                boolean isCyanGreen = (r >= 100 && r <= 105) && (g >= 250) && (b >= 200 && b <= 210);
                if (isPureBlack || isPaleGreen || isCyanGreen) {
                    separatorColumns.add(x);
                }
            }
        }

        nbFloors = separatorColumns.size() + 1;
        if (separatorColumns.size() > 0) {
            floorWidth = separatorColumns.get(0);
        }

        BlockState[][][] blocks = new BlockState[nbFloors][buildingDepth][floorWidth];
        BlockPos foundPathStart = null;

        // Parse each floor
        int floorStartX = 0;
        for (int floor = 0; floor < nbFloors; floor++) {
            int floorEndX = (floor < separatorColumns.size()) ? separatorColumns.get(floor) : width;
            int actualFloorWidth = floorEndX - floorStartX;

            for (int z = 0; z < height; z++) {
                for (int localX = 0; localX < actualFloorWidth && localX < floorWidth; localX++) {
                    // CRITICAL FIX: Mirror X axis like original PngPlanLoader.java (line 173)
                    // Original: px += buildingPlan.width - widthPos - 1
                    // This reads the PNG from RIGHT to LEFT, not left to right
                    int mirroredX = floorWidth - 1 - localX;
                    int imageX = floorStartX + mirroredX;
                    if (imageX >= width || imageX < 0)
                        continue;

                    int pixelRGB = image.getRGB(imageX, z) & 0xFFFFFF;
                    BlockState state = colorMap.get(pixelRGB);

                    // Skip air and ground markers
                    if (state != null) {
                        // FIX: Allow AIR to be stored in the plan!
                        // This ensures that "white" pixels in the PNG (which map to AIR)
                        // actually clear the ground when placed, creating basements/interiors.
                        blocks[floor][z][localX] = state;
                    }

                    // Check for special markers
                    SpecialMarker marker = specialMarkers.get(pixelRGB);
                    if (marker == SpecialMarker.PATH_START_POS) {
                        foundPathStart = new BlockPos(localX, floor, z);
                        LOGGER.info("[DEBUG] Found pathStartPos at local {},{},{}", localX, floor, z);
                    }
                }
            }

            if (floor < separatorColumns.size()) {
                floorStartX = separatorColumns.get(floor) + 1;
            }
        }

        return new BuildingPlan(blocks, floorWidth, buildingDepth, nbFloors, null, sourceFile, 0, 0, -1,
                foundPathStart);
    }

    /**
     * Loads a building plan from resources.
     * Supports multi-level buildings where A0.png, A1.png, A2.png etc. represent
     * different heights.
     */
    public static BuildingPlan loadPlan(String path) {
        LOGGER.info("[DEBUG] loadPlan() called with path: {}", path);

        // Attempt to read configuration file for native level and orientation
        int nativeLevel = 0;
        int buildingOrientation = 0;
        int fixedOrientation = -1; // -1 means "face town hall like normal"

        // Terrain preparation config - defaults match original Millénaire
        // Original BuildingPlan.java uses annotation defaultValue = "5" for areaToClear
        // and defaultValue = "10" for foundationDepth (lines 86-119)
        int areaToClear = 5;
        int areaToClearLengthBefore = -1; // -1 means use areaToClear
        int areaToClearLengthAfter = -1;
        int areaToClearWidthBefore = -1;
        int areaToClearWidthAfter = -1;
        int foundationDepth = 10; // Original default is 10, not 3
        int altitudeOffset = 0; // Default 0 matches original Millénaire
        float minDistance = 0.0f; // NEW: Min search radius as fraction (0.0 = center)
        float maxDistance = 1.0f; // NEW: Max search radius as fraction (1.0 = edge)
        java.util.Map<String, Integer> farFromTag = new java.util.HashMap<>(); // Must be FAR from these tags
        java.util.Map<String, Integer> closeToTag = new java.util.HashMap<>(); // Must be CLOSE to these tags

        String basePath = path;
        // Strip .png or _A0.png
        if (path.endsWith("0.png")) {
            basePath = path.substring(0, path.lastIndexOf("0.png")); // "name_A"
        } else if (path.endsWith(".png")) {
            basePath = path.substring(0, path.lastIndexOf(".png"));
        }

        String configPath = basePath + ".txt";

        // Try strict replacement for standard Millenaire format "name_A0.png" ->
        // "name_A.txt"
        if (path.matches(".*_[A-Z]0\\.png")) {
            configPath = path.substring(0, path.lastIndexOf("0.png")) + ".txt";
        }

        String fullConfigPath = "/assets/millenaire/buildings/" + configPath;
        LOGGER.info("[DEBUG] Attempting to load config from: {}", fullConfigPath);

        try (InputStream is = MillenaireBuildingParser.class.getResourceAsStream(fullConfigPath)) {
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    // Skip empty lines, comments, and lines starting with digits
                    if (line.isEmpty() || line.startsWith("#") || line.startsWith("//") ||
                            Character.isDigit(line.charAt(0))) {
                        continue;
                    }

                    if (line.startsWith("initial.startlevel") || line.startsWith("native.level")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                nativeLevel = Integer.parseInt(parts[1].trim());
                                LOGGER.info("[DEBUG] Found native level {} for {}", nativeLevel, path);
                            } catch (NumberFormatException e) {
                                LOGGER.warn("[DEBUG] Invalid number for startlevel in {}: {}", configPath, parts[1]);
                            }
                        }
                    } else if (line.startsWith("building.buildingorientation")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                buildingOrientation = Integer.parseInt(parts[1].trim());
                                LOGGER.info("[DEBUG] Found building orientation {} for {}", buildingOrientation, path);
                            } catch (NumberFormatException e) {
                                LOGGER.warn("[DEBUG] Invalid number for orientation in {}: {}", configPath, parts[1]);
                            }
                        }
                    } else if (line.startsWith("building.fixedorientation")
                            || line.startsWith("initial.fixedorientation")) {
                        // Fixed orientation forces building to always face a specific direction
                        // instead of facing the town hall. Used for gates, walls, etc.
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            String value = parts[1].trim().toLowerCase();
                            // Parse direction name or number
                            switch (value) {
                                case "north":
                                    fixedOrientation = 0;
                                    break;
                                case "west":
                                    fixedOrientation = 1;
                                    break;
                                case "south":
                                    fixedOrientation = 2;
                                    break;
                                case "east":
                                    fixedOrientation = 3;
                                    break;
                                default:
                                    try {
                                        fixedOrientation = Integer.parseInt(value);
                                        LOGGER.info("[DEBUG] Found fixed orientation {} for {}", fixedOrientation,
                                                path);
                                    } catch (NumberFormatException e) {
                                        LOGGER.warn("[DEBUG] Invalid fixed orientation in {}: {}", configPath, value);
                                    }
                            }
                        }
                    } else if (line.startsWith("building.areatoclear=")) {
                        // General areaToClear default for all directions
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                areaToClear = Integer.parseInt(parts[1].trim());
                                LOGGER.debug("[CONFIG] Found areaToClear={} for {}", areaToClear, path);
                            } catch (NumberFormatException e) {
                                LOGGER.warn("[CONFIG] Invalid areaToClear value: {}", parts[1]);
                            }
                        }
                    } else if (line.startsWith("building.areatoclearlengthbefore")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                areaToClearLengthBefore = Integer.parseInt(parts[1].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.startsWith("building.areatoclearlengthafter")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                areaToClearLengthAfter = Integer.parseInt(parts[1].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.startsWith("building.areatoclearwidthbefore")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                areaToClearWidthBefore = Integer.parseInt(parts[1].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.startsWith("building.areatoclearwidthafter")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                areaToClearWidthAfter = Integer.parseInt(parts[1].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.startsWith("building.foundationdepth")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                foundationDepth = Integer.parseInt(parts[1].trim());
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.startsWith("building.altitudeoffset")) {
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                altitudeOffset = Integer.parseInt(parts[1].trim());
                                LOGGER.debug("[CONFIG] Found altitudeOffset={} for {}", altitudeOffset, path);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.startsWith("init.mindistance")) {
                        // Per-building min search radius (0.0 = center, 1.0 = edge)
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                minDistance = Float.parseFloat(parts[1].trim());
                                LOGGER.debug("[CONFIG] Found minDistance={} for {}", minDistance, path);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.startsWith("init.maxdistance")) {
                        // Per-building max search radius (0.0 = center, 1.0 = edge)
                        String[] parts = line.split("=");
                        if (parts.length > 1) {
                            try {
                                maxDistance = Float.parseFloat(parts[1].trim());
                                LOGGER.debug("[CONFIG] Found maxDistance={} for {}", maxDistance, path);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    } else if (line.toLowerCase().contains("farfromtag:")) {
                        // Format: farFromTag:tagname,distance - building must be FAR from buildings
                        // with this tag
                        String[] colonParts = line.split(":", 2);
                        if (colonParts.length > 1) {
                            String[] tagParts = colonParts[1].split(",");
                            if (tagParts.length >= 2) {
                                try {
                                    String tag = tagParts[0].trim().toLowerCase();
                                    int distance = Integer.parseInt(tagParts[1].trim());
                                    farFromTag.put(tag, distance);
                                    LOGGER.debug("[CONFIG] Found farFromTag: {}={} for {}", tag, distance, path);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    } else if (line.toLowerCase().contains("closetotag:")) {
                        // Format: closeToTag:tagname,distance - building must be CLOSE to buildings
                        // with this tag
                        String[] colonParts = line.split(":", 2);
                        if (colonParts.length > 1) {
                            String[] tagParts = colonParts[1].split(",");
                            if (tagParts.length >= 2) {
                                try {
                                    String tag = tagParts[0].trim().toLowerCase();
                                    int distance = Integer.parseInt(tagParts[1].trim());
                                    closeToTag.put(tag, distance);
                                    LOGGER.debug("[CONFIG] Found closeToTag: {}={} for {}", tag, distance, path);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    }
                }
            } else {
                LOGGER.warn("[DEBUG] Config file not found at: {}", fullConfigPath);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load config for {}: {}", path, e.getMessage());
        }

        // Single-file building loading via parseSingleImage reuse
        String fullPath = "assets/millenaire/buildings/" + path;
        System.out.println("[DEBUG] Full resource path: " + fullPath);

        try (InputStream is = MillenaireBuildingParser.class.getClassLoader()
                .getResourceAsStream(fullPath)) {

            if (is == null) {
                System.err.println("[ERROR] Building plan NOT FOUND in resources: " + fullPath);
                throw new IOException("Building plan not found: " + path);
            }

            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IOException("Failed to read image: " + path);
            }

            // Parse raw image (Orientation 0)
            BuildingPlan rawPlan = parseSingleImage(image, path);

            // Apply default areaToClear if specific values not set (original logic from
            // initParametersPostHandling)
            int finalLengthBefore = areaToClearLengthBefore == -1 ? areaToClear : areaToClearLengthBefore;
            int finalLengthAfter = areaToClearLengthAfter == -1 ? areaToClear : areaToClearLengthAfter;
            int finalWidthBefore = areaToClearWidthBefore == -1 ? areaToClear : areaToClearWidthBefore;
            int finalWidthAfter = areaToClearWidthAfter == -1 ? areaToClear : areaToClearWidthAfter;

            // Apply configuration overrides (Orientation + Native Level + Terrain Config +
            // Tag Constraints)
            return new BuildingPlan(rawPlan.blocks(), rawPlan.width(), rawPlan.length(), rawPlan.nbFloors(),
                    rawPlan.startPos(), rawPlan.sourceFile(), nativeLevel, buildingOrientation, fixedOrientation,
                    rawPlan.pathStartPos(), finalLengthBefore, finalLengthAfter, finalWidthBefore, finalWidthAfter,
                    foundationDepth, altitudeOffset, minDistance, maxDistance, farFromTag, closeToTag);

        } catch (IOException e) {
            LOGGER.error("[ERROR] IOException loading building: {}", path, e);
            throw new RuntimeException("Failed to load Millénaire building: " + path, e);
        } catch (Exception e) {
            LOGGER.error("[ERROR] Unexpected exception loading building: {}", path, e);
            throw new RuntimeException("Unexpected error loading building: " + path, e);
        }
    }

    /**
     * Creates a new BuildingPlan with rotated block states based on the orientation
     * parameter.
     * Follows Millenaire logic: each orientation increment is a 90-degree rotation.
     * 0 = No rotation
     * 1 = 90 degrees CCW (North -> West)
     * 2 = 180 degrees
     * 3 = 270 degrees CCW (90 degrees CW) (North -> East)
     */
    private static BuildingPlan applyOrientation(BuildingPlan plan, int orientation, int nativeLevel) {
        if (orientation == 0 && nativeLevel == 0) {
            return plan;
        }

        // We create a new blocks array, but we KEEP the dimensions same as the raw plan
        // because correct rendering depends on the PNG structure being placed relative
        // to origin.
        // We only rotate the *BlockStates* themselves so their facing matches the
        // intended world direction.

        BlockState[][][] newBlocks = new BlockState[plan.nbFloors][plan.length][plan.width];

        for (int f = 0; f < plan.nbFloors; f++) {
            for (int z = 0; z < plan.length; z++) {
                for (int x = 0; x < plan.width; x++) {
                    BlockState original = plan.blocks[f][z][x];
                    if (original != null && !original.isAir()) {
                        newBlocks[f][z][x] = rotateState(original, orientation);
                    } else {
                        newBlocks[f][z][x] = original;
                    }
                }
            }
        }

        return new BuildingPlan(newBlocks, plan.width, plan.length, plan.nbFloors,
                plan.startPos, plan.sourceFile, nativeLevel, orientation);
    }

    private static BlockState rotateState(BlockState state, int orientation) {
        if (orientation == 0)
            return state;

        net.minecraft.world.level.block.Rotation rotation = net.minecraft.world.level.block.Rotation.NONE;
        // Map Millenaire orientation to Minecraft Rotation
        // 1 = North->West = CCW 90
        // 2 = North->South = 180
        // 3 = North->East = CW 90

        switch (orientation % 4) {
            case 1:
                rotation = net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;
                break;
            case 2:
                rotation = net.minecraft.world.level.block.Rotation.CLOCKWISE_180;
                break;
            case 3:
                rotation = net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
                break;
        }

        return state.rotate(rotation);
    }

    /**
     * Generates terrain preparation blocks following EXACT original Millénaire
     * algorithm.
     * Ported from BuildingPlan.getBuildingPoints_prepareGround() (lines 1961-2052).
     * 
     * This creates a list of BuildingBlock entries with special types that define
     * how terrain should be modified around and under the building.
     * 
     * @param effectiveOrigin   Building origin (already adjusted for nativeLevel)
     * @param length            Building length
     * @param width             Building width
     * @param nbFloors          Number of floors
     * @param areaToClearBefore Area to clear before building (default 1)
     * @param areaToClearAfter  Area to clear after building (default 1)
     * @param foundationDepth   Depth of foundation below ground (default 3)
     * @param startLevel        Starting level offset (default 0)
     * @param lengthOffset      Center offset for length
     * @param widthOffset       Center offset for width
     * @param rotation          Building rotation
     * @return List of BuildingBlock entries for terrain preparation
     */
    private static java.util.List<BuildingBlock> prepareGround(
            BlockPos effectiveOrigin,
            int length, int width, int nbFloors,
            int areaToClearBefore, int areaToClearAfter,
            int foundationDepth, int startLevel,
            int lengthOffset, int widthOffset,
            net.minecraft.world.level.block.Rotation rotation) {

        java.util.List<BuildingBlock> blocks = new java.util.ArrayList<>();

        int locationX = effectiveOrigin.getX();
        int locationY = effectiveOrigin.getY();
        int locationZ = effectiveOrigin.getZ();

        // PHASE 1: Clear ground ABOVE - iterate from top down (original lines
        // 1973-2013)
        // Height is nbFloors + 50 in original, but trees are rarely > 20 blocks tall
        int maxClearHeight = nbFloors + 50;

        for (int deltaY = maxClearHeight; deltaY > -1; deltaY--) {
            for (int deltaX = -areaToClearBefore; deltaX < length + areaToClearAfter; deltaX++) {
                for (int deltaZ = -areaToClearBefore; deltaZ < width + areaToClearAfter; deltaZ++) {

                    // Calculate offset from building edge (original lines 1974-1991)
                    int offsetX = 0;
                    if (deltaX < 0) {
                        offsetX = -deltaX;
                    } else if (deltaX >= length - 1) {
                        offsetX = deltaX - length + 1;
                    }

                    int offsetZ = 0;
                    if (deltaZ < 0) {
                        offsetZ = -deltaZ;
                    } else if (deltaZ >= width - 1) {
                        offsetZ = deltaZ - width + 1;
                    }

                    int offset = Math.max(offsetX, offsetZ);
                    if (Math.abs(offsetX - offsetZ) < 3) {
                        offset++;
                    }

                    // CRITICAL: Original uses --offset which PERMANENTLY DECREMENTS offset
                    // This affects all subsequent uses of offset in this iteration
                    offset--; // Pre-decrement like --offset in original

                    // Determine special block type based on position (original lines 1993-2012)
                    if (deltaY >= offset - 2) {
                        BlockPos p = adjustPosForRotation(
                                locationX, locationY + deltaY, locationZ,
                                deltaX - lengthOffset, deltaZ - widthOffset,
                                rotation, length, width);

                        if (deltaX >= 0 && deltaZ >= 0 && deltaX <= length && deltaZ <= width) {
                            // Inside building footprint
                            blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUND));
                        } else if (deltaY != offset - 2 && deltaY != 0) {
                            // Outside building but not at stepped edge or ground level
                            if (deltaX != -areaToClearBefore && deltaZ != -areaToClearBefore &&
                                    deltaX != length + areaToClearAfter - 1 && deltaZ != width + areaToClearAfter - 1) {
                                blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUNDOUTSIDEBUILDING));
                            } else {
                                blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUNDBORDER));
                            }
                        } else {
                            blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUNDBORDER));
                        }
                    } else {
                        // Higher up - only clear trees
                        BlockPos p = adjustPosForRotation(
                                locationX, locationY + deltaY, locationZ,
                                deltaX - lengthOffset, deltaZ - widthOffset,
                                rotation, length, width);
                        blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARTREE));
                    }
                }
            }
        }

        // PHASE 2: Preserve/fill ground BELOW (foundation) - original lines 2016-2051
        for (int deltaX = -areaToClearBefore; deltaX < length + areaToClearAfter; deltaX++) {
            for (int deltaZ = -areaToClearBefore; deltaZ < width + areaToClearAfter; deltaZ++) {

                for (int deltaY = -foundationDepth + startLevel; deltaY < 0; deltaY++) {
                    int offsetX = 0;
                    if (deltaX < 0) {
                        offsetX = -deltaX;
                    } else if (deltaX >= length - 1) {
                        offsetX = deltaX - length + 1;
                    }

                    int offsetZ = 0;
                    if (deltaZ < 0) {
                        offsetZ = -deltaZ;
                    } else if (deltaZ >= width - 1) {
                        offsetZ = deltaZ - width + 1;
                    }

                    int offset = Math.max(offsetX, offsetZ);
                    if (Math.abs(offsetX - offsetZ) < 3) {
                        offset++;
                    }

                    BlockPos p = adjustPosForRotation(
                            locationX, locationY + deltaY, locationZ,
                            deltaX - lengthOffset, deltaZ - widthOffset,
                            rotation, length, width);

                    // CRITICAL: Original uses --offsetx which PERMANENTLY DECREMENTS offset
                    offset--; // Pre-decrement like --offsetx in original

                    // Original: if (-deltaY > offsetx) (note: already decremented)
                    if (-deltaY > offset) {
                        // Deep underground - preserve depth
                        blocks.add(new BuildingBlock(p, SpecialBlockType.PRESERVEGROUNDDEPTH));
                    } else if (-deltaY >= offset - 1) {
                        // Near surface - preserve surface (original: if (-deltaY >= offsetx - 1))
                        blocks.add(new BuildingBlock(p, SpecialBlockType.PRESERVEGROUNDSURFACE));
                    } else {
                        // Just clear trees at this level
                        blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARTREE));
                    }
                }
            }
        }

        LOGGER.info("[TERRAIN] Generated {} terrain preparation blocks", blocks.size());
        return blocks;
    }

    /**
     * Prepares terrain for building placement with separate length/width
     * areaToClear values.
     * This is the full implementation matching original Millénaire behavior where
     * each
     * direction (lengthBefore, lengthAfter, widthBefore, widthAfter) can have
     * different values.
     */
    private static java.util.List<BuildingBlock> prepareGroundWithSeparateWidths(
            BlockPos effectiveOrigin,
            int length, int width, int nbFloors,
            int areaToClearLengthBefore, int areaToClearLengthAfter,
            int areaToClearWidthBefore, int areaToClearWidthAfter,
            int foundationDepth, int startLevel,
            int lengthOffset, int widthOffset,
            net.minecraft.world.level.block.Rotation rotation) {

        java.util.List<BuildingBlock> blocks = new java.util.ArrayList<>();

        int locationX = effectiveOrigin.getX();
        int locationY = effectiveOrigin.getY();
        int locationZ = effectiveOrigin.getZ();

        // PHASE 1: Clear ground ABOVE - iterate from top down (original lines
        // 1973-2013)
        // EXACT PORT: Original iterates deltaY from (nbfloors+50) down to 0
        int maxClearHeight = nbFloors + 50;

        for (int deltaY = maxClearHeight; deltaY > -1; deltaY--) {
            for (int deltaX = -areaToClearLengthBefore; deltaX < length + areaToClearLengthAfter; deltaX++) {
                for (int deltaZ = -areaToClearWidthBefore; deltaZ < width + areaToClearWidthAfter; deltaZ++) {

                    // Calculate offset from building edge (original lines 1974-1991)
                    int offsetX = 0;
                    if (deltaX < 0) {
                        offsetX = -deltaX;
                    } else if (deltaX >= length - 1) {
                        offsetX = deltaX - length + 1;
                    }

                    int offsetZ = 0;
                    if (deltaZ < 0) {
                        offsetZ = -deltaZ;
                    } else if (deltaZ >= width - 1) {
                        offsetZ = deltaZ - width + 1;
                    }

                    int offset = Math.max(offsetX, offsetZ);
                    if (Math.abs(offsetX - offsetZ) < 3) {
                        offset++;
                    }

                    // EXACT ORIGINAL: --offset (pre-decrement) then check deltaY >= offset - 2
                    // Original line 1993: if (deltaY >= --offset - 2)
                    offset--; // Pre-decrement like --offset in original

                    if (deltaY >= offset - 2) {
                        // Within stepped clearing threshold
                        BlockPos p = adjustPosForRotation(
                                locationX, locationY + deltaY, locationZ,
                                deltaX - lengthOffset, deltaZ - widthOffset,
                                rotation, length, width);

                        // EXACT ORIGINAL line 1995: uses <= for upper bounds
                        // if (deltaX >= 0 && deltaZ >= 0 && deltaX <= this.length && deltaZ <=
                        // this.width)
                        if (deltaX >= 0 && deltaZ >= 0 && deltaX <= length && deltaZ <= width) {
                            blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUND));
                        } else if (deltaY != offset - 2 && deltaY != 0) {
                            // Not at edge of stepped zone and not at ground level
                            if (deltaX != -areaToClearLengthBefore && deltaZ != -areaToClearWidthBefore &&
                                    deltaX != length + areaToClearLengthAfter - 1
                                    && deltaZ != width + areaToClearWidthAfter - 1) {
                                blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUNDOUTSIDEBUILDING));
                            } else {
                                blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUNDBORDER));
                            }
                        } else {
                            // At edge of stepped zone or at ground level
                            blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARGROUNDBORDER));
                        }
                    } else {
                        // Below stepped threshold - only clear trees (original lines 2009-2012)
                        BlockPos p = adjustPosForRotation(
                                locationX, locationY + deltaY, locationZ,
                                deltaX - lengthOffset, deltaZ - widthOffset,
                                rotation, length, width);
                        blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARTREE));
                    }
                }
            }
        }

        // PHASE 2: Preserve/fill ground BELOW (foundation)
        for (int deltaX = -areaToClearLengthBefore; deltaX < length + areaToClearLengthAfter; deltaX++) {
            for (int deltaZ = -areaToClearWidthBefore; deltaZ < width + areaToClearWidthAfter; deltaZ++) {

                // EXACT ORIGINAL: Uses foundationDepth from building config (default 10)
                // Original line 2020: for (short deltaY = (short)(-this.foundationDepth +
                // this.startLevel); deltaY < 0; deltaY++)
                for (int deltaY = -foundationDepth + startLevel; deltaY < 0; deltaY++) {
                    int offsetX = 0;
                    if (deltaX < 0) {
                        offsetX = -deltaX;
                    } else if (deltaX >= length - 1) {
                        offsetX = deltaX - length + 1;
                    }

                    int offsetZ = 0;
                    if (deltaZ < 0) {
                        offsetZ = -deltaZ;
                    } else if (deltaZ >= width - 1) {
                        offsetZ = deltaZ - width + 1;
                    }

                    int offset = Math.max(offsetX, offsetZ);
                    if (Math.abs(offsetX - offsetZ) < 3) {
                        offset++;
                    }

                    BlockPos p = adjustPosForRotation(
                            locationX, locationY + deltaY, locationZ,
                            deltaX - lengthOffset, deltaZ - widthOffset,
                            rotation, length, width);

                    offset--; // Pre-decrement

                    if (-deltaY > offset) {
                        blocks.add(new BuildingBlock(p, SpecialBlockType.PRESERVEGROUNDDEPTH));
                    } else if (-deltaY >= offset - 1) {
                        blocks.add(new BuildingBlock(p, SpecialBlockType.PRESERVEGROUNDSURFACE));
                    } else {
                        blocks.add(new BuildingBlock(p, SpecialBlockType.CLEARTREE));
                    }
                }
            }
        }

        LOGGER.info("[TERRAIN] Generated {} terrain preparation blocks (separate widths)", blocks.size());
        return blocks;
    }

    /**
     * Adjusts a position for building rotation.
     * Converts local building coordinates to world coordinates with rotation
     * applied.
     */
    private static BlockPos adjustPosForRotation(
            int locationX, int locationY, int locationZ,
            int xOffset, int zOffset,
            net.minecraft.world.level.block.Rotation rotation,
            int length, int width) {

        int worldX, worldZ;
        switch (rotation) {
            case CLOCKWISE_90:
                worldX = locationX - zOffset;
                worldZ = locationZ + xOffset;
                break;
            case CLOCKWISE_180:
                worldX = locationX - xOffset;
                worldZ = locationZ - zOffset;
                break;
            case COUNTERCLOCKWISE_90:
                worldX = locationX + zOffset;
                worldZ = locationZ - xOffset;
                break;
            default: // NONE
                worldX = locationX + xOffset;
                worldZ = locationZ + zOffset;
                break;
        }
        return new BlockPos(worldX, locationY, worldZ);
    }

    /**
     * Places the building in the world, anchored to terrain, with specific
     * rotation.
     */
    public static void placeBuilding(ServerLevel level, BlockPos origin, BuildingPlan plan,
            net.minecraft.world.level.block.Rotation rotation) {
        System.out.println("[DEBUG] placeBuilding() called with ROTATION " + rotation + ":");
        System.out.println("[DEBUG]   - Origin: " + origin);
        System.out.println("[DEBUG]   - Plan: " + plan.sourceFile());

        // Calculate dimensions after rotation
        int width = plan.width();
        int length = plan.length();
        int rotatedWidth = (rotation == net.minecraft.world.level.block.Rotation.CLOCKWISE_90
                || rotation == net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90) ? length : width;
        int rotatedLength = (rotation == net.minecraft.world.level.block.Rotation.CLOCKWISE_90
                || rotation == net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90) ? width : length;

        // FIX: Use the pre-calculated altitude from origin.getY()
        // This Y was calculated by findBuildingLocationWithMapInfo() as an AVERAGE
        // of topGround across the entire building footprint, matching original
        // Millénaire behavior.
        // Previously we were recalculating with a single center-point sample which
        // caused
        // buildings to sink when the center happened to be in a natural depression.
        int terrainY = origin.getY();

        // Apply altitudeOffset to raise building above terrain (original testSpot line
        // 2636)
        int altitudeOffset = plan.altitudeOffset();
        terrainY += altitudeOffset;

        LOGGER.info("[DEBUG] Using terrain height: terrainY={} (from origin + altitudeOffset={})", terrainY,
                altitudeOffset);

        int startY = terrainY + plan.nativeLevel();
        BlockPos effectiveOrigin = new BlockPos(origin.getX(), startY, origin.getZ());

        // EXACT from original: lengthOffset and widthOffset for centering
        // From PngPlanLoader.java line 138: lengthOffset = (int)Math.floor(length *
        // 0.5)
        int lengthOffset = length / 2;
        int widthOffset = width / 2;

        // NEW: Use original Millénaire terrain handling system with special block types
        // Use per-building areaToClear values loaded from config
        int areaToClearLengthBefore = plan.areaToClearLengthBefore();
        int areaToClearLengthAfter = plan.areaToClearLengthAfter();
        int areaToClearWidthBefore = plan.areaToClearWidthBefore();
        int areaToClearWidthAfter = plan.areaToClearWidthAfter();
        int foundationDepth = plan.foundationDepth();
        int startLevel = plan.nativeLevel(); // Use building's startLevel for foundation calculations

        LOGGER.debug("[TERRAIN] Using areaToClear L:{}/{} W:{}/{} depth:{} startLevel:{} for {}",
                areaToClearLengthBefore, areaToClearLengthAfter,
                areaToClearWidthBefore, areaToClearWidthAfter,
                foundationDepth, startLevel, plan.sourceFile());

        // CRITICAL FIX: Original passes TERRAIN SURFACE Y to prepareGround, NOT
        // adjusted origin!
        // Original code (BuildingPlan.java line 1224): int locationY =
        // location.pos.getiY(); (terrain height)
        // Then line 1246: getBuildingPoints_prepareGround(location, locationX,
        // locationY, ...)
        //
        // Our bug: We were passing effectiveOrigin.Y (terrainY + nativeLevel = 58) but
        // should pass terrainY (67)
        // This caused CLEARGROUND at deltaY=0 to be at Y=58 (9 blocks underground)
        // instead of Y=67 (surface)
        BlockPos terrainSurfaceOrigin = new BlockPos(origin.getX(), terrainY, origin.getZ());

        java.util.List<BuildingBlock> terrainBlocks = prepareGroundWithSeparateWidths(
                terrainSurfaceOrigin, length, width, plan.nbFloors(),
                areaToClearLengthBefore, areaToClearLengthAfter,
                areaToClearWidthBefore, areaToClearWidthAfter,
                foundationDepth, startLevel,
                lengthOffset, widthOffset, rotation);

        // Apply terrain preparation blocks first (clear ground, preserve ground, etc.)
        // Apply terrain preparation blocks first (clear ground, preserve ground, etc.)
        int terrainBlocksApplied = 0;
        for (BuildingBlock block : terrainBlocks) {
            if (block.build(level, true)) {
                terrainBlocksApplied++;
            }
        }
        LOGGER.info("[TERRAIN] Applied {} terrain blocks", terrainBlocksApplied);

        // EXTRA TERRAIN PHASES: Steps and Foundations
        // These were missing from the main placement logic!
        // EXTRA TERRAIN PHASES: REMOVED
        // We now rely solely on prepareGroundWithSeparateWidths (ported 1.12.2 logic)
        // to handle foundations and smoothing. The previous "TieredFoundation" methods
        // were creating incorrect vertical walls and overriding the authentic stepped
        // logic.

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int groundY = effectiveOrigin.getY();
        int blocksPlaced = 0;
        int stairDebugCount = 0; // Separate counter for stair debug logging

        // List to defer wall torch placement until all solid blocks are placed
        List<Object[]> deferredTorches = new ArrayList<>();

        // List to defer ladder placement until all solid blocks are placed (for wall
        // detection)
        // Based on original Millénaire autoGuessLaddersDoorsStairs (lines 607-737)
        List<Object[]> deferredLadders = new ArrayList<>();

        LOGGER.info("[ROTATION DEBUG] Building '{}' - rotation={}, length={}, width={}, floors={}",
                plan.sourceFile(), rotation, length, width, plan.nbFloors);

        // Origin point is where the building gets placed - use effectiveOrigin
        int originX = effectiveOrigin.getX();
        int originZ = effectiveOrigin.getZ();

        for (int floor = 0; floor < plan.nbFloors; floor++) {
            int floorYOffset = floor;

            // EXACT loop structure from original (lines 1028-1032):
            // for (int dx = 0; dx < this.length; dx++)
            // for (int dz = 0; dz < this.width; dz++)
            // PointType pt = this.plan[dy][dx][dz];
            // Our blocks array: blocks[floor][z][x] where z=dx (0..length) and x=dz
            // (0..width)
            for (int dx = 0; dx < length; dx++) {
                for (int dz = 0; dz < width; dz++) {
                    BlockState state = plan.blocks[floor][dx][dz];
                    if (state == null || state.isAir())
                        continue;

                    // Rotate the BlockState (for directional blocks like stairs, doors)
                    BlockState rotatedState = state.rotate(rotation);

                    // EXACT formula from original BuildingPlan.adjustForOrientation (lines 308-321)
                    // Uses CENTERED offsets: xoffset = dx - lengthOffset, zoffset = dz -
                    // widthOffset
                    int xoffset = dx - lengthOffset;
                    int zoffset = dz - widthOffset;

                    int worldX, worldZ;
                    switch (rotation) {
                        case NONE:
                            // Orientation 0: pos = new Point(x + xoffset, y, z + zoffset)
                            worldX = originX + xoffset;
                            worldZ = originZ + zoffset;
                            break;
                        case COUNTERCLOCKWISE_90:
                            // Orientation 1: pos = new Point(x + zoffset, y, z - xoffset - 1)
                            worldX = originX + zoffset;
                            worldZ = originZ - xoffset - 1;
                            break;
                        case CLOCKWISE_180:
                            // Orientation 2: pos = new Point(x - xoffset - 1, y, z - zoffset - 1)
                            worldX = originX - xoffset - 1;
                            worldZ = originZ - zoffset - 1;
                            break;
                        case CLOCKWISE_90:
                            // Orientation 3: pos = new Point(x - zoffset - 1, y, z + xoffset)
                            worldX = originX - zoffset - 1;
                            worldZ = originZ + xoffset;
                            break;
                        default:
                            worldX = originX + xoffset;
                            worldZ = originZ + zoffset;
                            break;
                    }

                    // DEBUG: Log stair rotation details (first 15 stairs per building)
                    if (state.getBlock() instanceof StairBlock && stairDebugCount < 15) {
                        Direction origFacing = state.getValue(StairBlock.FACING);
                        Direction newFacing = rotatedState.getValue(StairBlock.FACING);
                        Half origHalf = state.getValue(StairBlock.HALF);
                        Half newHalf = rotatedState.getValue(StairBlock.HALF);
                        LOGGER.info(
                                "[STAIR DEBUG] floor={} local({},{}) -> world({},{},{}) rot={}: facing {} -> {}, half {} -> {}",
                                floor, dx, dz, worldX, groundY + floorYOffset, worldZ,
                                rotation, origFacing, newFacing, origHalf, newHalf);
                        stairDebugCount++;
                    }

                    mutable.set(worldX, groundY + floorYOffset, worldZ);

                    // FOUNDATION LOGIC: Handle PRESERVE_GROUND
                    if (rotatedState.getBlock() == Blocks.STRUCTURE_VOID) {
                        int checkY = mutable.getY() - 1;
                        BlockState groundState = Blocks.DIRT.defaultBlockState();
                        boolean foundGround = false;

                        // Scan down for ground
                        while (checkY > level.getMinBuildHeight()) {
                            BlockState checkState = level
                                    .getBlockState(new BlockPos(mutable.getX(), checkY, mutable.getZ()));
                            // Use isRedstoneConductor as proxy for "solid block"
                            if (checkState.isRedstoneConductor(level,
                                    new BlockPos(mutable.getX(), checkY, mutable.getZ())) && !checkState.isAir()) {
                                if (isValidGroundBlock(checkState)) {
                                    groundState = checkState;
                                }
                                foundGround = true;
                                break;
                            }
                            checkY--;
                        }

                        int startFillY = foundGround ? checkY + 1 : mutable.getY();
                        for (int fillY = startFillY; fillY <= mutable.getY(); fillY++) {
                            level.setBlock(new BlockPos(mutable.getX(), fillY, mutable.getZ()), groundState, 3);
                            blocksPlaced++;
                        }
                        continue;
                    }

                    // Place the block
                    try {
                        // NOTE: Stair orientation is handled by the blueprint data + state.rotate()
                        // Dynamic overrides were removed because they affected corner stairs and
                        // interior stairs incorrectly. The blueprint + rotation should handle it.

                        // WALL TORCH: Defer placement until all solid blocks are placed
                        if (rotatedState.getBlock() instanceof WallTorchBlock) {
                            // Store position and state for later placement
                            deferredTorches.add(new Object[] { mutable.immutable(), rotatedState });
                            continue; // Skip placement for now
                        }

                        // LADDER: Defer placement until all solid blocks are placed (for wall
                        // detection)
                        if (rotatedState.getBlock() instanceof net.minecraft.world.level.block.LadderBlock) {
                            // Store position and state for later placement with wall detection
                            deferredLadders.add(new Object[] { mutable.immutable(), rotatedState });
                            continue; // Skip placement for now
                        }

                        level.setBlock(mutable, rotatedState, 3);
                        blocksPlaced++;

                        // Special handling for beds (HEAD needs FOOT)
                        if (rotatedState.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                            net.minecraft.world.level.block.state.properties.BedPart part = rotatedState
                                    .getValue(net.minecraft.world.level.block.BedBlock.PART);
                            if (part == net.minecraft.world.level.block.state.properties.BedPart.HEAD) {
                                Direction facing = rotatedState
                                        .getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
                                Direction footDir = facing.getOpposite();
                                BlockPos footPos = mutable.relative(footDir);
                                if (!(level.getBlockState(footPos)
                                        .getBlock() instanceof net.minecraft.world.level.block.BedBlock)) {
                                    BlockState footState = rotatedState.setValue(
                                            net.minecraft.world.level.block.BedBlock.PART,
                                            net.minecraft.world.level.block.state.properties.BedPart.FOOT);
                                    level.setBlock(footPos, footState, 3);
                                    blocksPlaced++;
                                }
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.error("Failed to place block at " + mutable, e);
                    }
                }
            }
        }

        // SECOND PASS: Place deferred wall torches now that all solid blocks exist
        for (Object[] torchData : deferredTorches) {
            BlockPos torchPos = (BlockPos) torchData[0];
            BlockState torchState = (BlockState) torchData[1];

            // Find adjacent wall and update facing to point away from it
            boolean foundWall = false;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos wallPos = torchPos.relative(dir.getOpposite());
                BlockState wallState = level.getBlockState(wallPos);
                if (wallState.isSolidRender(level, wallPos)) {
                    torchState = torchState.setValue(WallTorchBlock.FACING, dir);
                    level.setBlock(torchPos, torchState, 3);
                    blocksPlaced++;
                    foundWall = true;
                    break;
                }
            }

            // If no wall found, place as standing torch instead
            if (!foundWall) {
                level.setBlock(torchPos, Blocks.TORCH.defaultBlockState(), 3);
                blocksPlaced++;
            }
        }

        // THIRD PASS: Place deferred ladders with wall detection
        // Based on original Millénaire autoGuessLaddersDoorsStairs (lines 607-737)
        // Ladder FACING points AWAY from the wall (i.e., toward the player climbing)
        for (Object[] ladderData : deferredLadders) {
            BlockPos ladderPos = (BlockPos) ladderData[0];
            BlockState ladderState = (BlockState) ladderData[1];

            // Find adjacent wall and set facing to point AWAY from it
            // Original logic: if wall is NORTH, ladder faces SOUTH (toward player)
            boolean foundWall = false;
            for (Direction checkDir : Direction.Plane.HORIZONTAL) {
                BlockPos wallPos = ladderPos.relative(checkDir);
                BlockState wallState = level.getBlockState(wallPos);
                if (wallState.isSolidRender(level, wallPos)) {
                    // Ladder faces OPPOSITE direction to the wall (away from wall)
                    Direction ladderFacing = checkDir.getOpposite();
                    ladderState = Blocks.LADDER.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.LadderBlock.FACING, ladderFacing);
                    level.setBlock(ladderPos, ladderState, 3);
                    blocksPlaced++;
                    foundWall = true;
                    LOGGER.debug("[LADDER] Placed at {} facing {} (wall at {})", ladderPos, ladderFacing, checkDir);
                    break;
                }
            }

            // If no wall found, place with default facing (might float, but avoid crash)
            if (!foundWall) {
                LOGGER.warn("[LADDER] No wall found at {}, placing with default facing", ladderPos);
                level.setBlock(ladderPos, ladderState, 3);
                blocksPlaced++;
            }
        }

        System.out.println("[DEBUG]   - Blocks actually placed: " + blocksPlaced);

        // Second pass: Force neighbor updates (fences, walls, etc.)
        for (int floor = 0; floor < plan.nbFloors; floor++) {
            int floorYOffset = floor;
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    BlockState state = plan.blocks[floor][z][x];
                    if (state == null || state.isAir())
                        continue;

                    Block block = state.getBlock();
                    if (block instanceof net.minecraft.world.level.block.FenceBlock ||
                            block instanceof net.minecraft.world.level.block.WallBlock ||
                            block instanceof net.minecraft.world.level.block.IronBarsBlock ||
                            block instanceof net.minecraft.world.level.block.FenceGateBlock) {

                        // Re-calculate rotated position
                        int rx = x;
                        int rz = z;
                        switch (rotation) {
                            case CLOCKWISE_90:
                                rx = length - 1 - z;
                                rz = x;
                                break;
                            case CLOCKWISE_180:
                                rx = width - 1 - x;
                                rz = length - 1 - z;
                                break;
                            case COUNTERCLOCKWISE_90:
                                rx = z;
                                rz = width - 1 - x;
                                break;
                            default:
                                break;
                        }

                        mutable.set(effectiveOrigin.getX() + rx, groundY + floorYOffset, effectiveOrigin.getZ() + rz);
                        BlockState currentState = level.getBlockState(mutable);

                        // Update shape
                        BlockState updatedState = currentState.updateShape(Direction.NORTH,
                                level.getBlockState(mutable.north()), level, mutable, mutable.north());
                        updatedState = updatedState.updateShape(Direction.SOUTH, level.getBlockState(mutable.south()),
                                level, mutable, mutable.south());
                        updatedState = updatedState.updateShape(Direction.EAST, level.getBlockState(mutable.east()),
                                level, mutable, mutable.east());
                        updatedState = updatedState.updateShape(Direction.WEST, level.getBlockState(mutable.west()),
                                level, mutable, mutable.west());

                        if (!updatedState.equals(currentState)) {
                            level.setBlock(mutable, updatedState, 2);
                        }
                    }
                }
            }

            // Place starting marker/sign if needed (using rotated position)
            if (plan.startPos != null && floor == 0) {
                int sx = plan.startPos.getX();
                int sz = plan.startPos.getZ();
                int rsx = sx;
                int rsz = sz;
                switch (rotation) {
                    case CLOCKWISE_90:
                        rsx = length - 1 - sz;
                        rsz = sx;
                        break;
                    case CLOCKWISE_180:
                        rsx = width - 1 - sx;
                        rsz = length - 1 - sz;
                        break;
                    case COUNTERCLOCKWISE_90:
                        rsx = sz;
                        rsz = width - 1 - sx;
                        break;
                    default:
                        break;
                }

                BlockPos signPos = effectiveOrigin.offset(rsx, 1, rsz);
                level.setBlock(signPos, Blocks.OAK_SIGN.defaultBlockState(), 3);
            }
        }
    }

    /**
     * Places the building in the world, anchored to terrain (Default Rotation).
     */
    public static void placeBuilding(ServerLevel level, BlockPos origin, BuildingPlan plan) {
        placeBuilding(level, origin, plan, net.minecraft.world.level.block.Rotation.NONE);
    }

    private static boolean isValidGroundBlock(BlockState state) {
        if (state.isAir())
            return false;
        if (state.is(net.minecraft.tags.BlockTags.DIRT) ||
                state.is(net.minecraft.tags.BlockTags.SAND) ||
                state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) ||
                state.getBlock() == Blocks.GRAVEL ||
                state.getBlock() == Blocks.GRASS_BLOCK) {
            return true;
        }
        return false;
    }

    /**
     * Enum for special non-block markers in building plans
     */
    public enum SpecialMarker {
        EMPTY,
        PRESERVE_GROUND,
        ALL_BUT_TREES,
        GRASS,
        SLEEPING_POS,
        SELLING_POS,
        CRAFTING_POS,
        DEFENDING_POS,
        SHELTER_POS,
        PATH_START_POS,
        LEISURE_POS,
        MAIN_CHEST_GUESS,
        MAIN_CHEST_TOP,
        MAIN_CHEST_BOTTOM,
        MAIN_CHEST_LEFT,
        MAIN_CHEST_RIGHT,
        LOCKED_CHEST_GUESS,
        LOCKED_CHEST_TOP,
        LOCKED_CHEST_BOTTOM,
        LOCKED_CHEST_LEFT,
        LOCKED_CHEST_RIGHT,
        SOIL,
        COW_SPAWN,
        PIG_SPAWN,
        SHEEP_SPAWN,
        CHICKEN_SPAWN
    }

    /**
     * Data holder for parsed building plan.
     * 
     * Orientation fields:
     * - buildingOrientation: The intrinsic direction the building blueprint "faces"
     * (default 1 = West = left of PNG)
     * - fixedOrientation: If >= 0, forces building to always face this direction
     * instead of the town hall
     * -1 means "face town hall like normal" (default behavior)
     * - areaToClear*: How much terrain to prepare around the building (defaults to
     * 1)
     * - foundationDepth: How deep to prepare foundation (defaults to 3)
     * - farFromTag: Map of tags to minimum distances (building must be FAR from
     * buildings with these tags)
     * - closeToTag: Map of tags to maximum distances (building must be CLOSE to
     * buildings with these tags)
     */

}
