package org.millenaire.common.buildingplan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.item.InvItem;

/**
 * Loads building plans from PNG image files.
 * Each PNG contains floor layouts encoded as pixel colors.
 * Ported from 1.12.2 to 1.20.1.
 */
public class PngPlanLoader {

    /**
     * Load a building plan from PNG files.
     * 
     * @param file           The main PNG file
     * @param buildingKey    The building identifier
     * @param level          The upgrade level
     * @param variation      The building variation (A, B, C, ...)
     * @param previousPlan   Previous upgrade plan (or null for level 0)
     * @param metadataLoader Loader for building metadata
     * @param culture        The culture this building belongs to
     * @param importPlan     Whether this is an import operation
     * @return The loaded BuildingPlan
     */
    public static BuildingPlan loadFromPngs(
            File file,
            String buildingKey,
            int level,
            int variation,
            BuildingPlan previousPlan,
            BuildingMetadataLoader metadataLoader,
            Culture culture,
            boolean importPlan) throws Exception {

        if (!file.exists()) {
            throw new MillLog.MillenaireException("PNG file not found: " + file.getAbsolutePath());
        }

        BuildingPlan plan = new BuildingPlan(buildingKey, level, variation, culture);
        plan.setLoadedFromFile(file);

        // Load the PNG image
        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new MillLog.MillenaireException("Failed to read PNG file: " + file.getAbsolutePath());
        }

        if (image == null) {
            throw new MillLog.MillenaireException("Invalid PNG image: " + file.getAbsolutePath());
        }

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        if (MillConfigValues.LogBuildingPlan >= 2) {
            MillLog.major(plan, "Loading PNG: " + file.getName() + " (" + imageWidth + "x" + imageHeight + ")");
        }

        // Parse dimensions from image
        // Image format: multiple floors side by side horizontally
        // Height = building length (depth)
        // Width / nbFloors = building width

        // First, detect number of floors by looking for floor separator
        // Each floor is separated by a column of specific color or just width
        // calculation
        int nbFloors = detectNumberOfFloors(image);

        if (nbFloors <= 0) {
            throw new MillLog.MillenaireException("Could not detect floors in PNG: " + file.getName());
        }

        plan.nbFloors = nbFloors;
        plan.width = imageWidth / nbFloors;
        plan.length = imageHeight;

        if (MillConfigValues.LogBuildingPlan >= 2) {
            MillLog.major(plan, "Detected " + nbFloors + " floors, width=" + plan.width + ", length=" + plan.length);
        }

        // Initialize the BlockState array [floor][length][width]
        plan.blocks = new BlockState[nbFloors][plan.length][plan.width];

        // Load metadata first (sets various plan fields)
        metadataLoader.loadDataForPlan(plan, previousPlan, importPlan);

        // Initialize resource cost
        plan.resCost = new HashMap<>();

        // Parse each pixel
        for (int floor = 0; floor < nbFloors; floor++) {
            for (int z = 0; z < plan.length; z++) {
                for (int x = 0; x < plan.width; x++) {
                    int imgX = floor * plan.width + x;
                    int imgY = z;

                    if (imgX < imageWidth && imgY < imageHeight) {
                        int rgb = image.getRGB(imgX, imgY) & 0xFFFFFF; // Mask out alpha
                        PointType pt = PointType.colourPoints.get(rgb);

                        if (pt != null) {
                            // Convert PointType to BlockState
                            // If PointType has a method to get BlockState, use it.
                            // If it returns null or is special, we might need a placeholder like AIR or
                            // STRUCTURE_VOID.
                            // Assuming PointType has getBlockState() based on BuildingImportExport code
                            // seen earlier.
                            // Also checking for special types.

                            BlockState state = pt.getBlockState();
                            if (state == null) {
                                // Fallback for special types or unknown blocks - map to air or specific marker
                                state = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                            }

                            plan.blocks[floor][z][x] = state;

                            // Add to cost if this point has an associated block cost
                            if (pt.getCost() != null && !pt.isSpecial()) {
                                plan.addToCost(pt.getCost(), 1);
                            }
                        } else {
                            // Unknown color - map to AIR
                            plan.blocks[floor][z][x] = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();

                            // Unknown color - log if verbose
                            if (rgb != 0xFFFFFF && rgb != 0x000000 && MillConfigValues.LogBuildingPlan >= 3) {
                                MillLog.minor(plan, "Unknown color at (" + x + "," + z + ") floor " + floor +
                                        ": RGB(" + ((rgb >> 16) & 0xFF) + "," + ((rgb >> 8) & 0xFF) + "," + (rgb & 0xFF)
                                        + ")");
                            }
                        }
                    }
                }
            }
        }

        // Calculate length and width offsets
        plan.lengthOffset = plan.length / 2;
        plan.widthOffset = plan.width / 2;

        if (MillConfigValues.LogBuildingPlan >= 1) {
            MillLog.major(plan, "Loaded plan: " + plan + " with " + plan.resCost.size() + " resource types");
        }

        return plan;
    }

    /**
     * Detect the number of floors in the image.
     * Floors are arranged horizontally, each with the same width.
     */
    private static int detectNumberOfFloors(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Heuristic: try to find a floor width that divides evenly
        // Common floor widths: scan for patterns

        // Simple approach: assume floors are square-ish or check for separators
        // For now, assume the image width is divisible by common sizes

        // Try common divisors
        int[] commonWidths = { 16, 20, 24, 28, 32, 36, 40, 48, 64 };

        for (int floorWidth : commonWidths) {
            if (width % floorWidth == 0) {
                int floors = width / floorWidth;
                if (floors > 0 && floors <= 20) { // Reasonable floor count
                    return floors;
                }
            }
        }

        // Fallback: try to match to height (assume roughly square floors)
        if (width >= height && width % height == 0) {
            return width / height;
        }

        // Last resort: single floor
        if (width > 0) {
            return 1;
        }

        return 0;
    }

    /**
     * Compute the resource cost of a building plan.
     * Converts processed items back to raw materials (e.g., planks -> logs, sticks
     * -> logs).
     */
    public static void computeCost(BuildingPlan buildingPlan) throws MillLog.MillenaireException {
        if (buildingPlan.resCost == null) {
            buildingPlan.resCost = new HashMap<>();
        }

        // Scan the plan array for point costs
        if (buildingPlan.plan != null) {
            for (int i = 0; i < buildingPlan.nbFloors; i++) {
                for (int j = 0; j < buildingPlan.length; j++) {
                    for (int k = 0; k < buildingPlan.width; k++) {
                        PointType p = buildingPlan.plan[i][j][k];
                        if (p != null && p.getCostQuantity() > 0) {
                            InvItem costItem = p.getCostInvItem();
                            if (costItem != null && costItem.getItem() != net.minecraft.world.item.Items.AIR) {
                                buildingPlan.addToCost(costItem, p.getCostQuantity());
                            }
                        }
                    }
                }
            }
        }

        // Convert sticks to logs (4 sticks = 2 planks = 0.5 logs, so sticks/8 = logs)
        InvItem stickInvItem = InvItem.createInvItem(net.minecraft.world.item.Items.STICK);
        if (buildingPlan.resCost.containsKey(stickInvItem)) {
            int stickQuantity = buildingPlan.resCost.get(stickInvItem);
            buildingPlan.resCost.remove(stickInvItem);
            // 4 sticks from 2 planks, 4 planks from 1 log -> 1 log = 8 sticks approximately
            buildingPlan.addToCost(Blocks.OAK_LOG, (int) Math.max(Math.ceil(stickQuantity / 8.0), 1.0));
        }

        // Convert planks to logs (4 planks = 1 log)
        convertPlanksToLogs(buildingPlan, Blocks.OAK_PLANKS, Blocks.OAK_LOG);
        convertPlanksToLogs(buildingPlan, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG);
        convertPlanksToLogs(buildingPlan, Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG);
        convertPlanksToLogs(buildingPlan, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG);
        convertPlanksToLogs(buildingPlan, Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG);
        convertPlanksToLogs(buildingPlan, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG);
        convertPlanksToLogs(buildingPlan, Blocks.CHERRY_PLANKS, Blocks.CHERRY_LOG);
        convertPlanksToLogs(buildingPlan, Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_LOG);

        // Convert glass panes to glass blocks (16 panes from 6 glass = 6/16 glass per
        // pane)
        InvItem glassPaneInvItem = InvItem.createInvItem(Blocks.GLASS_PANE);
        if (buildingPlan.resCost.containsKey(glassPaneInvItem)) {
            int paneQuantity = buildingPlan.resCost.get(glassPaneInvItem);
            buildingPlan.addToCost(Blocks.GLASS, (int) Math.max(Math.ceil(paneQuantity * 6.0 / 16.0), 1.0));
            buildingPlan.resCost.remove(glassPaneInvItem);
        }
    }

    /**
     * Helper to convert planks of a specific type to logs.
     */
    private static void convertPlanksToLogs(BuildingPlan plan,
            net.minecraft.world.level.block.Block plankBlock,
            net.minecraft.world.level.block.Block logBlock) {
        InvItem plankInvItem = InvItem.createInvItem(plankBlock);
        if (plan.resCost.containsKey(plankInvItem)) {
            int plankQuantity = plan.resCost.get(plankInvItem);
            plan.addToCost(logBlock, (int) Math.max(Math.ceil(plankQuantity / 4.0), 1.0));
            plan.resCost.remove(plankInvItem);
        }
    }

    /**
     * Validate a building plan for consistency.
     * Checks spawn points have matching tags, chest placement is correct, etc.
     */
    public static void validateBuildingPlan(BuildingPlan buildingPlan) {
        if (buildingPlan.plan == null) {
            return;
        }

        int pigs = 0;
        int sheep = 0;
        int chicken = 0;
        int cow = 0;

        for (int i = 0; i < buildingPlan.nbFloors; i++) {
            for (int j = 0; j < buildingPlan.length; j++) {
                for (int k = 0; k < buildingPlan.width; k++) {
                    PointType pt = buildingPlan.plan[i][j][k];
                    if (pt == null)
                        continue;

                    // Check spawn/tag consistency
                    if (pt.isType("chickenspawn")) {
                        chicken++;
                        if (!buildingPlan.containsTags("chicken")) {
                            MillLog.warning(buildingPlan, "Building has chicken spawn but no chicken tag.");
                        }
                    } else if (pt.isType("cowspawn")) {
                        cow++;
                        if (!buildingPlan.containsTags("cattle")) {
                            MillLog.warning(buildingPlan, "Building has cattle spawn but no cattle tag.");
                        }
                    } else if (pt.isType("sheepspawn")) {
                        sheep++;
                        if (!buildingPlan.containsTags("sheeps")) {
                            MillLog.warning(buildingPlan, "Building has sheeps spawn but no sheeps tag.");
                        }
                    } else if (pt.isType("pigspawn")) {
                        pigs++;
                        if (!buildingPlan.containsTags("pigs")) {
                            MillLog.warning(buildingPlan, "Building has pig spawn but no pig tag.");
                        }
                    } else if (pt.isType("squidspawn")) {
                        if (!buildingPlan.containsTags("squids")) {
                            MillLog.warning(buildingPlan, "Building has squid spawn but no squid tag.");
                        }
                    }

                    // Check chest placement
                    if (pt.isSubType("lockedchest") || pt.isSubType("mainchest")) {
                        if (buildingPlan.isgift || buildingPlan.price > 0) {
                            MillLog.warning(buildingPlan,
                                    "Player building has locked/main chests which should be regular chests.");
                        }
                    } else if (pt.getBlock() == Blocks.CHEST) {
                        if (!buildingPlan.isgift && buildingPlan.price == 0) {
                            MillLog.warning(buildingPlan,
                                    "Non-player building has regular chests instead of locked chests.");
                        }
                    }
                }
            }
        }

        // Check for odd spawn counts (animals spawn in pairs)
        if (chicken % 2 == 1) {
            MillLog.warning(buildingPlan, "Odd number of chicken spawn: " + chicken);
        }
        if (sheep % 2 == 1) {
            MillLog.warning(buildingPlan, "Odd number of sheep spawn: " + sheep);
        }
        if (cow % 2 == 1) {
            MillLog.warning(buildingPlan, "Odd number of cow spawn: " + cow);
        }
        if (pigs % 2 == 1) {
            MillLog.warning(buildingPlan, "Odd number of pigs spawn: " + pigs);
        }
    }
}
