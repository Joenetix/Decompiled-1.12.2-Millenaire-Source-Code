package org.millenaire.common.buildingplan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.core.MillBlocks;

/**
 * Building import/export utilities.
 * Handles exporting in-game structures to PNG building plans,
 * and importing building plans to place in the world.
 * Ported from 1.12.2 to 1.20.1.
 * 
 * NOTE: Full functionality requires complete block mapping and
 * TileEntityImportTable.
 * Some methods are stubbed pending full port.
 */
public class BuildingImportExport {
    public static String EXPORT_DIR = "exportdir";
    private static Map<Integer, PointType> reverseColourPoints = new HashMap<>();

    /**
     * Adjust a position based on building orientation (0-3).
     * Orientation 0 = North, 1 = East, 2 = South, 3 = West
     */
    public static Point adjustForOrientation(int x, int y, int z, int xoffset, int zoffset, int orientation) {
        return switch (orientation) {
            case 0 -> new Point(x + xoffset, y, z + zoffset);
            case 1 -> new Point(x + zoffset, y, z - xoffset);
            case 2 -> new Point(x - xoffset, y, z - zoffset - 1);
            case 3 -> new Point(x - zoffset - 1, y, z + xoffset);
            default -> new Point(x + xoffset, y, z + zoffset);
        };
    }

    /**
     * Copy a building plan set to the export directory.
     */
    private static void copyPlanSetToExportDir(BuildingPlanSet planSet) {
        File exportDir = MillCommonUtilities.getExportDir();
        Path exportPath = exportDir.toPath();

        BuildingPlan firstPlan = planSet.getFirstStartingPlan();
        if (firstPlan == null || firstPlan.getLoadedFromFile() == null) {
            MillLog.error(planSet, "Cannot copy plan set: no source file");
            return;
        }

        Path inputPath = firstPlan.getLoadedFromFile().toPath().getParent();

        try {
            for (int exportVariation = 0; exportVariation < planSet.plans.size(); exportVariation++) {
                char exportVariationLetter = (char) ('A' + exportVariation);
                String txtFileName = planSet.key + "_" + exportVariationLetter + ".txt";
                Files.copy(inputPath.resolve(txtFileName), exportPath.resolve(txtFileName),
                        StandardCopyOption.REPLACE_EXISTING);

                BuildingPlan[] variationPlans = planSet.plans.get(exportVariation);
                for (int buildingUpgrade = 0; buildingUpgrade < variationPlans.length; buildingUpgrade++) {
                    String pngFileName = planSet.key + "_" + exportVariationLetter + buildingUpgrade + ".png";
                    Files.copy(inputPath.resolve(pngFileName), exportPath.resolve(pngFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            MillLog.printException("Error when copying files to export dir:", e);
        }
    }

    /**
     * Export a building from the world to a PNG building plan.
     * 
     * @param level                       The world level
     * @param startPoint                  Starting position (typically import table
     *                                    location)
     * @param planName                    Name for the exported plan
     * @param variation                   Which variation (A=0, B=1, etc.)
     * @param length                      Building length
     * @param width                       Building width
     * @param orientation                 Building orientation (0-3)
     * @param upgradeLevel                Which upgrade level to export
     * @param startLevel                  Y-offset for the building base
     * @param exportSnow                  Whether to include snow blocks
     * @param exportRegularChests         Whether to export regular chests (vs
     *                                    locked chests)
     * @param autoconvertToPreserveGround Whether to auto-convert grass to
     *                                    preserveGround
     * @return The upgrade level that was actually exported
     */
    public static int exportBuilding(
            ServerLevel level,
            Point startPoint,
            String planName,
            int variation,
            int length,
            int width,
            int orientation,
            int upgradeLevel,
            int startLevel,
            boolean exportSnow,
            boolean exportRegularChests,
            boolean autoconvertToPreserveGround) throws Exception {
        loadReverseBuildingPoints(autoconvertToPreserveGround, exportRegularChests);

        File exportDir = new File(MillCommonUtilities.getMillenaireCustomContentDir(), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }

        char variationLetter = (char) ('A' + variation);

        // Scan the world for blocks
        List<PointType[][]> export = new ArrayList<>();
        int orientatedLength = (orientation % 2 == 1) ? width : length;
        int orientatedWidth = (orientation % 2 == 1) ? length : width;

        Point centrePos = startPoint.getRelative(orientatedLength / 2 + 1, 0, orientatedWidth / 2 + 1);
        int x = centrePos.getiX();
        int y = centrePos.getiY();
        int z = centrePos.getiZ();
        int lengthOffset = (int) Math.floor(length * 0.5);
        int widthOffset = (int) Math.floor(width * 0.5);

        boolean stop = false;
        int dy = 0;

        while (!stop) {
            PointType[][] levelData = new PointType[length][width];
            boolean blockFound = false;

            for (int dx = 0; dx < length; dx++) {
                for (int dz = 0; dz < width; dz++) {
                    levelData[dx][dz] = null;
                    Point p = adjustForOrientation(x, y + dy + startLevel, z, dx - lengthOffset, dz - widthOffset,
                            orientation);
                    BlockState blockState = level.getBlockState(p.getBlockPos());
                    Block block = blockState.getBlock();

                    if (!blockState.isAir()) {
                        blockFound = true;
                    }

                    PointType pt = reverseColourPoints.get(getPointHash(blockState));
                    if (pt != null) {
                        if (exportSnow || block != Blocks.SNOW) {
                            levelData[dx][dz] = pt;
                        }
                    }
                }
            }

            if (!blockFound && export.size() > 0) {
                stop = true;
            } else {
                export.add(levelData);
            }

            if (++dy + startPoint.getiY() + startLevel >= 320) {
                stop = true;
            }
        }

        // Generate the PNG image
        BufferedImage pict = new BufferedImage(export.size() * width + export.size() - 1, length,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = pict.createGraphics();
        graphics.setColor(new Color(0xB32F11)); // Brown separator color
        graphics.fillRect(0, 0, pict.getWidth(), pict.getHeight());

        for (int floorIndex = 0; floorIndex < export.size(); floorIndex++) {
            PointType[][] levelData = export.get(floorIndex);

            for (int i = 0; i < length; i++) {
                for (int k = 0; k < width; k++) {
                    int colour = 0xFFFFFF; // White = empty
                    PointType pt = levelData[i][k];
                    if (pt != null) {
                        colour = pt.colour;
                    }

                    graphics.setColor(new Color(colour));
                    graphics.fillRect(floorIndex * width + floorIndex + width - k - 1, i, 1, 1);
                }
            }
        }

        // Save the PNG
        String fileName = planName + "_" + variationLetter + upgradeLevel + ".png";
        ImageIO.write(pict, "png", new File(exportDir, fileName));

        // Create/update the txt file if this is level 0
        if (upgradeLevel == 0) {
            BufferedWriter writer = MillCommonUtilities
                    .getWriter(new File(exportDir, planName + "_" + variationLetter + ".txt"));
            writer.write("building.length=" + length + "\n");
            writer.write("building.width=" + width + "\n");
            writer.write("\n");
            writer.write("initial.startlevel=" + startLevel + "\n");
            writer.write("initial.nativename=" + planName + "\n");
            writer.close();
        }

        MillLog.major(null, "Exported building plan: " + fileName);
        return upgradeLevel;
    }

    /**
     * Get a hash for a block state to look up its PointType.
     */
    private static int getPointHash(BlockState blockState) {
        if (blockState == null) {
            return "unknownBlock".hashCode();
        }
        // Use registry name + state string for unique identification
        return blockState.toString().hashCode();
    }

    /**
     * Load the reverse lookup map from block states to PointTypes.
     */
    public static void loadReverseBuildingPoints(boolean exportPreserveGround, boolean exportRegularChests) {
        reverseColourPoints.clear();

        if (PointType.colourPoints == null) {
            return;
        }

        for (PointType pt : PointType.colourPoints.values()) {
            if (pt.specialType == null && pt.getBlockState() != null) {
                reverseColourPoints.put(getPointHash(pt.getBlockState()), pt);
            }
        }

        // Add special mappings for common blocks
        // TODO: Add more block state variations when needed
    }

    /**
     * Load a building plan set from the export directory.
     */
    public static BuildingPlanSet loadPlanSetFromExportDir(String buildingKey) {
        File exportDir = MillCommonUtilities.getExportDir();
        VirtualDir exportVirtualDir = new VirtualDir(exportDir);
        File buildingFile = new File(exportDir, buildingKey + "_A.txt");

        BuildingPlanSet planSet = new BuildingPlanSet(null, buildingKey, exportVirtualDir, buildingFile);
        try {
            planSet.loadPictPlans(true);
        } catch (Exception e) {
            MillLog.printException("Exception when loading plan from export dir:", e);
        }

        return planSet;
    }

    /**
     * Import a building plan and place it in the world.
     * 
     * @param player           The player performing the import
     * @param tablePos         Position of the import table
     * @param planSet          The building plan set to import
     * @param variation        Which variation to import
     * @param maxLevel         Maximum upgrade level to import
     * @param orientation      Building orientation
     * @param importMockBlocks Whether to use mock blocks instead of real blocks
     */
    public static void importBuilding(
            Player player,
            Point tablePos,
            BuildingPlanSet planSet,
            int variation,
            int maxLevel,
            int orientation,
            boolean importMockBlocks) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            MillLog.error(null, "Cannot import building: not on server level");
            return;
        }

        BuildingPlan basePlan = planSet.getPlan(variation, 0);
        if (basePlan == null) {
            MillLog.error(planSet, "Cannot import: no base plan for variation " + variation);
            return;
        }

        int orientatedLength = (orientation % 2 == 1) ? basePlan.width : basePlan.length;
        int orientatedWidth = (orientation % 2 == 1) ? basePlan.length : basePlan.width;

        Point centerPos = tablePos.getRelative(orientatedLength / 2 + 1, 0, orientatedWidth / 2 + 1);

        // Create location with simplified constructor
        BuildingLocation location = new BuildingLocation(centerPos);
        location.planKey = planSet.key;
        location.orientation = orientation;

        // TODO: Implement actual building placement when
        // BuildingPlanSet.buildLocation() is ported
        // For now, just log what would be placed
        for (int level = 0; level <= maxLevel; level++) {
            location.level = level;
            MillLog.major(null, "Would place building: " + planSet.key +
                    " variation " + (char) ('A' + variation) +
                    " level " + level +
                    " at " + centerPos);
        }

        MillLog.major(null, "Imported building plan: " + planSet.key + " variation " + (char) ('A' + variation)
                + " level " + maxLevel);
    }

    /**
     * Get consolidated plan combining multiple upgrade levels.
     */
    /**
     * Get consolidated plan combining multiple upgrade levels.
     */
    public static BlockState[][][] getConsolidatedPlan(BuildingPlanSet planSet, int variation, int upgradeLevel) {
        int minLevel = planSet.getMinLevel(variation, upgradeLevel);
        int maxLevel = planSet.getMaxLevel(variation, upgradeLevel);

        BuildingPlan[] plans = planSet.plans.get(variation);
        if (plans == null || plans.length == 0) {
            return new BlockState[0][0][0];
        }

        int length = plans[0].blocks[0].length;
        int width = plans[0].blocks[0][0].length;
        BlockState[][][] consolidatedPlan = new BlockState[maxLevel - minLevel][length][width];

        for (int lid = 0; lid <= upgradeLevel && lid < plans.length; lid++) {
            BuildingPlan plan = plans[lid];
            if (MillConfigValues.LogBuildingPlan >= 1) {
                MillLog.major(planSet, "Consolidating plan: adding level " + lid);
            }

            int ioffset = plan.startLevel - minLevel;

            for (int i = 0; i < plan.blocks.length && i + ioffset < consolidatedPlan.length; i++) {
                for (int j = 0; j < length && j < plan.blocks[i].length; j++) {
                    for (int k = 0; k < width && k < plan.blocks[i][j].length; k++) {
                        BlockState pt = plan.blocks[i][j][k];
                        // If not air/null, overwrite
                        if (pt != null && !pt.isAir() || lid == 0) {
                            consolidatedPlan[i + ioffset][j][k] = pt;
                        }
                    }
                }
            }
        }

        return consolidatedPlan;
    }
}
