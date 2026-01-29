package org.millenaire.common.buildingplan;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockFlowerPot.EnumFlowerType;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.millenaire.client.network.ClientSender;
import org.millenaire.common.block.IBlockPath;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.block.mock.MockBlockAnimalSpawn;
import org.millenaire.common.block.mock.MockBlockDecor;
import org.millenaire.common.block.mock.MockBlockFree;
import org.millenaire.common.block.mock.MockBlockMarker;
import org.millenaire.common.block.mock.MockBlockSoil;
import org.millenaire.common.block.mock.MockBlockSource;
import org.millenaire.common.block.mock.MockBlockTreeSpawn;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.entity.TileEntityImportTable;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.BlockStateUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.BuildingLocation;

public class BuildingImportExport {
   public static String EXPORT_DIR = "exportdir";
   private static HashMap<Integer, PointType> reverseColourPoints = new HashMap<>();

   public static Point adjustForOrientation(int x, int y, int z, int xoffset, int zoffset, int orientation) {
      Point pos = null;
      if (orientation == 0) {
         pos = new Point(x + xoffset, y, z + zoffset);
      } else if (orientation == 1) {
         pos = new Point(x + zoffset, y, z - xoffset);
      } else if (orientation == 2) {
         pos = new Point(x - xoffset, y, z - zoffset - 1);
      } else if (orientation == 3) {
         pos = new Point(x - zoffset - 1, y, z + xoffset);
      }

      return pos;
   }

   private static void copyPlanSetToExportDir(BuildingPlanSet planSet) {
      File exportDir = MillCommonUtilities.getExportDir();
      Path exportPath = exportDir.toPath();
      Path inputPath = planSet.getFirstStartingPlan().getLoadedFromFile().toPath().getParent();

      try {
         for (int exportVariation = 0; exportVariation < planSet.plans.size(); exportVariation++) {
            char exportVariationLetter = (char)(65 + exportVariation);
            String txtFileName = planSet.key + "_" + exportVariationLetter + ".txt";
            Files.copy(inputPath.resolve(txtFileName), exportPath.resolve(txtFileName), StandardCopyOption.REPLACE_EXISTING);

            for (int buildingUpgrade = 0; buildingUpgrade < ((BuildingPlan[])planSet.plans.get(exportVariation)).length; buildingUpgrade++) {
               String pngFileName = planSet.key + "_" + exportVariationLetter + buildingUpgrade + ".png";
               Files.copy(inputPath.resolve(pngFileName), exportPath.resolve(pngFileName), StandardCopyOption.REPLACE_EXISTING);
            }
         }
      } catch (IOException var9) {
         MillLog.printException("Error when copying files to export dir:", var9);
      }
   }

   private static void doubleHeightPlan(EntityPlayer player, BuildingPlanSet existingSet) {
      for (BuildingPlan[] plans : existingSet.plans) {
         for (BuildingPlan plan : plans) {
            PointType[][][] newPlan = new PointType[plan.plan.length * 2][plan.plan[0].length][plan.plan[0][0].length];

            for (int i = 0; i < plan.plan.length; i++) {
               for (int j = 0; j < plan.plan[0].length; j++) {
                  for (int k = 0; k < plan.plan[0][0].length; k++) {
                     newPlan[i * 2][j][k] = plan.plan[i][j][k];
                     newPlan[i * 2 + 1][j][k] = plan.plan[i][j][k];
                  }
               }
            }

            plan.plan = newPlan;
            plan.nbfloors *= 2;
         }
      }

      ServerSender.sendTranslatedSentence(player, 'f', "import.doublevertical");
      MillLog.major(null, "Building height: " + existingSet.plans.get(0)[0].plan.length);
   }

   private static void drawWoolBorders(EntityPlayer player, Point tablePos, int orientatedLength, int orientatedWidth) {
      for (int x = 1; x <= orientatedLength; x++) {
         int meta = 0;
         if ((x - 1) % 10 < 5) {
            meta = 14;
         }

         tablePos.getRelative(x, -1.0, 0.0).setBlock(player.world, Blocks.WOOL, meta, true, false);
         tablePos.getRelative(x, -1.0, orientatedWidth + 1).setBlock(player.world, Blocks.WOOL, meta, true, false);
      }

      for (int z = 1; z <= orientatedWidth; z++) {
         int meta = 0;
         if ((z - 1) % 10 < 5) {
            meta = 14;
         }

         tablePos.getRelative(0.0, -1.0, z).setBlock(player.world, Blocks.WOOL, meta, true, false);
         tablePos.getRelative(orientatedLength + 1, -1.0, z).setBlock(player.world, Blocks.WOOL, meta, true, false);
      }

      tablePos.getRelative(0.0, -1.0, 0.0).setBlock(player.world, Blocks.WOOL, 15, true, false);
      tablePos.getRelative(orientatedLength + 1, -1.0, 0.0).setBlock(player.world, Blocks.WOOL, 15, true, false);
      tablePos.getRelative(0.0, -1.0, orientatedWidth + 1).setBlock(player.world, Blocks.WOOL, 15, true, false);
      tablePos.getRelative(orientatedLength + 1, -1.0, orientatedWidth + 1).setBlock(player.world, Blocks.WOOL, 15, true, false);
   }

   public static int exportBuilding(
      World world,
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
      boolean autoconvertToPreserveGround
   ) throws Exception, IOException, UnsupportedEncodingException, FileNotFoundException {
      loadReverseBuildingPoints(autoconvertToPreserveGround, exportRegularChests);
      File exportDir = new File(MillCommonUtilities.getMillenaireCustomContentDir(), "exports");
      if (!exportDir.exists()) {
         exportDir.mkdirs();
      }

      char variationLetter = 'A';
      variationLetter = (char)(variationLetter + variation);
      File buildingFile = new File(exportDir, planName + "_" + variationLetter + ".txt");
      PointType[][][] existingPoints = (PointType[][][])null;
      int existingMinLevel = 0;
      BuildingPlanSet existingSet = null;
      if (buildingFile.exists()) {
         existingSet = loadPlanSetFromExportDir(planName);
         if (existingSet.plans.get(variation)[0].length != length) {
            Mill.proxy
               .localTranslatedSentence(
                  Mill.proxy.getTheSinglePlayer(), '6', "export.errorlength", "" + length, "" + existingSet.plans.get(variation)[0].length
               );
            return upgradeLevel;
         }

         if (existingSet.plans.get(variation)[0].width != width) {
            Mill.proxy
               .localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), '6', "export.errorwidth", "" + width, "" + existingSet.plans.get(variation)[0].width);
            return upgradeLevel;
         }

         if (upgradeLevel == -1) {
            upgradeLevel = existingSet.plans.get(variation).length;
         }

         if (existingSet.plans.get(variation)[0].parentBuildingPlan != null) {
            String parentBuildingPlanKey = existingSet.plans.get(variation)[0].parentBuildingPlan;
            String parentSuffix = parentBuildingPlanKey.split("_")[parentBuildingPlanKey.split("_").length - 1].toUpperCase();
            int parentVariation = parentSuffix.charAt(0) - 'A';
            int parentLevel = Integer.parseInt(parentSuffix.substring(1, parentSuffix.length()));
            String parentBuildingKey = parentBuildingPlanKey.substring(0, parentBuildingPlanKey.length() - parentSuffix.length() - 1);
            BuildingPlanSet parentSet = loadPlanSetFromExportDir(parentBuildingKey);
            existingPoints = getConsolidatedPlanWithParent(parentSet, parentVariation, parentLevel, existingSet, variation, upgradeLevel - 1);
            existingMinLevel = Math.min(existingSet.getMinLevel(variation, upgradeLevel - 1), parentSet.getMinLevel(parentVariation, parentLevel));
         } else {
            existingPoints = getConsolidatedPlan(existingSet, variation, upgradeLevel - 1);
            existingMinLevel = existingSet.getMinLevel(variation, upgradeLevel - 1);
         }
      } else {
         upgradeLevel = 0;
      }

      List<PointType[][]> export = new ArrayList<>();
      boolean stop = false;
      int orientatedLength = length;
      int orientatedWidth = width;
      if (orientation % 2 == 1) {
         orientatedLength = width;
         orientatedWidth = length;
      }

      Point centrePos = startPoint.getRelative(orientatedLength / 2 + 1, 0.0, orientatedWidth / 2 + 1);
      int x = centrePos.getiX();
      int y = centrePos.getiY();
      int z = centrePos.getiZ();
      int lengthOffset = (int)Math.floor(length * 0.5);
      int widthOffset = (int)Math.floor(width * 0.5);
      int dy = 0;

      while (!stop) {
         PointType[][] level = new PointType[length][width];
         boolean blockFound = false;

         for (int dx = 0; dx < length; dx++) {
            for (int dz = 0; dz < width; dz++) {
               level[dx][dz] = null;
               Point p = adjustForOrientation(x, y + dy + startLevel, z, dx - lengthOffset, dz - widthOffset, orientation);
               Block block = WorldUtilities.getBlock(world, p);
               int meta = WorldUtilities.getBlockMeta(world, p);
               if (block != Blocks.AIR) {
                  blockFound = true;
               }

               if (block instanceof BlockFlowerPot) {
                  meta = ((EnumFlowerType)WorldUtilities.getBlockState(world, p)
                        .getActualState(world, new BlockPos(p.x, p.y, p.z))
                        .getValue(BlockFlowerPot.CONTENTS))
                     .ordinal();
               }

               PointType pt = reverseColourPoints.get(getPointHash(block, meta));
               if (pt != null) {
                  if (exportSnow || pt.getBlock() != Blocks.SNOW_LAYER) {
                     PointType existing = null;
                     if (existingPoints != null && dy + startLevel >= existingMinLevel && dy + startLevel < existingMinLevel + existingPoints.length) {
                        existing = existingPoints[dy + startLevel - existingMinLevel][dx][dz];
                        if (existing == null) {
                           MillLog.major(null, "Existing pixel is null");
                        }
                     }

                     if (existing == null) {
                        if (pt.specialType != null || pt.getBlock() != Blocks.AIR || upgradeLevel != 0) {
                           level[dx][dz] = pt;
                        }
                     } else if (existing != pt && (!existing.isType("empty") || pt.getBlock() != Blocks.AIR)) {
                        level[dx][dz] = pt;
                     }
                  }
               } else if (!(block instanceof BlockBed) && !(block instanceof BlockDoublePlant) && !(block instanceof BlockLiquid)) {
                  Mill.proxy
                     .localTranslatedSentence(
                        Mill.proxy.getTheSinglePlayer(), '6', "export.errorunknownblockid", "" + block + "/" + meta + "/" + getPointHash(block, meta)
                     );
               }
            }
         }

         if (!blockFound && (existingPoints == null || export.size() >= existingPoints.length)) {
            stop = true;
         } else {
            export.add(level);
         }

         if (++dy + startPoint.getiY() + startLevel >= 256) {
            stop = true;
         }
      }

      BufferedImage pict = new BufferedImage(export.size() * width + export.size() - 1, length, 1);
      Graphics2D graphics = pict.createGraphics();
      graphics.setColor(new Color(11730865));
      graphics.fillRect(0, 0, pict.getWidth(), pict.getHeight());

      for (int var45 = 0; var45 < export.size(); var45++) {
         PointType[][] level = export.get(var45);

         for (int i = 0; i < length; i++) {
            for (int k = 0; k < width; k++) {
               int colour = 16777215;
               PointType pt = level[i][k];
               if (pt != null) {
                  colour = pt.colour;
               }

               graphics.setColor(new Color(colour));
               graphics.fillRect(var45 * width + var45 + width - k - 1, i, 1, 1);
            }
         }
      }

      String fileName = planName + "_" + variationLetter + upgradeLevel + ".png";
      ImageIO.write(pict, "png", new File(exportDir, fileName));
      if (upgradeLevel == 0 && existingSet == null) {
         BufferedWriter writer = MillCommonUtilities.getWriter(new File(exportDir, planName + "_" + variationLetter + ".txt"));
         writer.write("building.length=" + length + "\n");
         writer.write("building.width=" + width + "\n");
         writer.write("\n");
         writer.write("initial.startlevel=" + startLevel + "\n");
         writer.write("initial.nativename=" + planName + "\n");
         writer.close();
      } else if (upgradeLevel > existingSet.plans.get(variation).length) {
         BufferedWriter writer = MillCommonUtilities.getAppendWriter(new File(exportDir, planName + "_" + variationLetter + ".txt"));
         writer.write("upgrade" + upgradeLevel + ".startlevel=" + startLevel + "\n");
         writer.close();
      }

      Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), 'f', "export.buildingexported", fileName);
      return upgradeLevel;
   }

   private static PointType[][][] getConsolidatedPlan(BuildingPlanSet planSet, int variation, int upgradeLevel) {
      int minLevel = planSet.getMinLevel(variation, upgradeLevel);
      int maxLevel = planSet.getMaxLevel(variation, upgradeLevel);
      int length = planSet.plans.get(variation)[0].plan[0].length;
      int width = planSet.plans.get(variation)[0].plan[0][0].length;
      PointType[][][] consolidatedPlan = new PointType[maxLevel - minLevel][length][width];

      for (int lid = 0; lid <= upgradeLevel; lid++) {
         BuildingPlan plan = planSet.plans.get(variation)[lid];
         if (MillConfigValues.LogBuildingPlan >= 1) {
            MillLog.major(planSet, "Consolidating plan: adding level " + lid);
         }

         int ioffset = plan.startLevel - minLevel;

         for (int i = 0; i < plan.plan.length; i++) {
            for (int j = 0; j < length; j++) {
               for (int k = 0; k < width; k++) {
                  PointType pt = plan.plan[i][j][k];
                  if (!pt.isType("empty") || lid == 0) {
                     consolidatedPlan[i + ioffset][j][k] = pt;
                  }
               }
            }
         }
      }

      return consolidatedPlan;
   }

   private static PointType[][][] getConsolidatedPlanWithParent(
      BuildingPlanSet parentSet, int parentVariation, int parentUpgradeLevel, BuildingPlanSet planSet, int variation, int upgradeLevel
   ) {
      int minLevel = Math.min(planSet.getMinLevel(variation, upgradeLevel), parentSet.getMinLevel(parentVariation, parentUpgradeLevel));
      int maxLevel = Math.max(planSet.getMaxLevel(variation, upgradeLevel), parentSet.getMaxLevel(parentVariation, parentUpgradeLevel));
      int length = planSet.plans.get(variation)[0].plan[0].length;
      int width = planSet.plans.get(variation)[0].plan[0][0].length;
      PointType[][][] consolidatedPlan = new PointType[maxLevel - minLevel][length][width];

      for (int lid = 0; lid <= parentUpgradeLevel; lid++) {
         BuildingPlan plan = parentSet.plans.get(parentVariation)[lid];
         if (MillConfigValues.LogBuildingPlan >= 1) {
            MillLog.major(parentSet, "Consolidating plan: adding level " + lid);
         }

         int ioffset = plan.startLevel - minLevel;

         for (int i = 0; i < plan.plan.length; i++) {
            for (int j = 0; j < length; j++) {
               for (int k = 0; k < width; k++) {
                  PointType pt = plan.plan[i][j][k];
                  if (!pt.isType("empty") || lid == 0) {
                     consolidatedPlan[i + ioffset][j][k] = pt;
                  }
               }
            }
         }
      }

      PointType airPt = reverseColourPoints.get(getPointHash(Blocks.AIR, 0));
      if (parentSet.getMaxLevel(parentVariation, parentUpgradeLevel) < planSet.getMaxLevel(variation, upgradeLevel)) {
         for (int i = parentSet.getMaxLevel(parentVariation, parentUpgradeLevel); i <= planSet.getMaxLevel(variation, upgradeLevel); i++) {
            for (int j = 0; j < length; j++) {
               for (int kx = 0; kx < width; kx++) {
                  consolidatedPlan[i][j][kx] = airPt;
               }
            }
         }
      }

      for (int lid = 0; lid <= upgradeLevel; lid++) {
         BuildingPlan plan = planSet.plans.get(variation)[lid];
         if (MillConfigValues.LogBuildingPlan >= 1) {
            MillLog.major(planSet, "Consolidating plan: adding level " + lid);
         }

         int ioffset = plan.startLevel - minLevel;

         for (int i = 0; i < plan.plan.length; i++) {
            for (int j = 0; j < length; j++) {
               for (int kx = 0; kx < width; kx++) {
                  PointType pt = plan.plan[i][j][kx];
                  if (!pt.isType("empty")) {
                     consolidatedPlan[i + ioffset][j][kx] = pt;
                  }
               }
            }
         }
      }

      return consolidatedPlan;
   }

   private static int getPointHash(Block b, int meta) {
      return b != null ? (b.getRegistryName() + "_" + meta).hashCode() : ("unknownBlock_" + meta).hashCode();
   }

   private static int getPointHash(IBlockState bs) {
      return bs != null ? (bs.getBlock().getRegistryName() + "_" + bs.getBlock().getMetaFromState(bs)).hashCode() : "unknownBlock".hashCode();
   }

   private static PointType getPointTypeFromBlockState(IBlockState blockState) {
      for (PointType newPt : PointType.colourPoints.values()) {
         if (newPt.getBlockState() != null && newPt.getBlockState().equals(blockState)) {
            return newPt;
         }
      }

      return null;
   }

   public static void importTableCreateNewBuilding(
      EntityPlayer player, TileEntityImportTable importTable, int length, int width, int startLevel, boolean clearGround
   ) {
      File exportDir = MillCommonUtilities.getExportDir();
      VirtualDir exportVirtualDir = new VirtualDir(exportDir);
      int exportNumber = 1;

      while (exportVirtualDir.getChildFile("export" + exportNumber + "_A.txt") != null) {
         exportNumber++;
      }

      if (clearGround) {
         for (int x = importTable.getPos().getX(); x < importTable.getPos().getX() + 2 + length; x++) {
            for (int z = importTable.getPos().getZ(); z < importTable.getPos().getZ() + 2 + width; z++) {
               int startingY = Math.min(0, startLevel);

               for (int y = importTable.getPos().getY() + startingY; y < importTable.getPos().getY(); y++) {
                  player.world
                     .setBlockState(
                        new BlockPos(x, y, z),
                        MillBlocks.MARKER_BLOCK.getDefaultState().withProperty(MockBlockMarker.VARIANT, MockBlockMarker.Type.PRESERVE_GROUND)
                     );
               }

               for (int y = importTable.getPos().getY(); y < importTable.getPos().getY() + 50; y++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  if (!pos.equals(importTable.getPos())) {
                     player.world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState());
                  }
               }
            }
         }
      }

      drawWoolBorders(player, importTable.getPosPoint(), length, width);
      importTable.updatePlan("export" + exportNumber, length, width, 0, 0, startLevel, player);
   }

   public static void importTableExportBuildingPlan(World world, TileEntityImportTable importTable, int level) {
      if (importTable.getOrientation() != 0) {
         Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), '6', "export.northfacingonly");
      } else {
         try {
            int upgradeLevelExported = exportBuilding(
               world,
               new Point(importTable.getPos()),
               importTable.getBuildingKey(),
               importTable.getVariation(),
               importTable.getLength(),
               importTable.getWidth(),
               importTable.getOrientation(),
               level,
               importTable.getStartingLevel(),
               importTable.exportSnow(),
               importTable.exportRegularChests(),
               importTable.autoconvertToPreserveGround()
            );
            if (upgradeLevelExported != level) {
               ClientSender.importTableUpdateSettings(
                  new Point(importTable.getPos()),
                  upgradeLevelExported,
                  importTable.getOrientation(),
                  importTable.getStartingLevel(),
                  importTable.exportSnow(),
                  importTable.importMockBlocks(),
                  importTable.autoconvertToPreserveGround(),
                  importTable.exportRegularChests()
               );
            }
         } catch (Exception var4) {
            MillLog.printException("Error when exporting building:", var4);
         }
      }
   }

   public static void importTableExportPlanCost(String buildingKey) {
      BuildingPlanSet set = loadPlanSetFromExportDir(buildingKey);
      File file = new File(MillCommonUtilities.getExportDir(), set.key + " resources used.txt");

      try {
         BufferedWriter writer = MillCommonUtilities.getWriter(file);
         BuildingDevUtilities.writePlanCostTextStyle(set, writer);
         writer.close();
         Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), 'f', "importtable.costexported", "export/" + file.getName());
      } catch (IOException var4) {
         MillLog.printException(var4);
      }
   }

   public static int importTableHandleImportRequest(
      EntityPlayer player,
      Point tablePos,
      String source,
      String buildingKey,
      boolean importAll,
      int variation,
      int level,
      int orientation,
      boolean importMockBlocks
   ) {
      BuildingPlanSet importSet = null;
      if (source.equals(EXPORT_DIR)) {
         importSet = loadPlanSetFromExportDir(buildingKey);
      } else {
         importSet = Culture.getCultureByName(source).getBuildingPlanSet(buildingKey);
         copyPlanSetToExportDir(importSet);
         if (importSet.getFirstStartingPlan().parentBuildingPlan != null) {
            String parentBuildingPlanKey = importSet.getFirstStartingPlan().parentBuildingPlan;
            String parentSuffix = parentBuildingPlanKey.split("_")[parentBuildingPlanKey.split("_").length - 1].toUpperCase();
            String parentBuildingKey = parentBuildingPlanKey.substring(0, parentBuildingPlanKey.length() - parentSuffix.length() - 1);
            copyPlanSetToExportDir(Culture.getCultureByName(source).getBuildingPlanSet(parentBuildingKey));
         }
      }

      if (importSet == null) {
         return 0;
      } else if (!importAll) {
         importTableImportBuilding(player, tablePos, null, importSet, variation, level, orientation, importMockBlocks);
         return importSet.plans.get(variation)[0].length + 2 + importSet.plans.get(variation)[0].areaToClear;
      } else {
         int xDelta = 0;

         for (int aVariation = 0; aVariation < importSet.plans.size(); aVariation++) {
            BuildingPlan basePlan = importSet.plans.get(aVariation)[0];
            int orientatedLength = basePlan.length;
            int orientatedWidth = basePlan.width;
            int orientedGapLength = basePlan.areaToClearLengthAfter + basePlan.areaToClearLengthBefore;
            int orientedGapWidth = basePlan.areaToClearWidthAfter + basePlan.areaToClearWidthBefore;
            if (orientation % 2 == 1) {
               orientatedLength = basePlan.width;
               orientatedWidth = basePlan.length;
               orientedGapLength = basePlan.areaToClearWidthAfter + basePlan.areaToClearWidthBefore;
               orientedGapWidth = basePlan.areaToClearLengthAfter + basePlan.areaToClearLengthBefore;
            }

            int zDelta = 0;

            for (int aLevel = 0; aLevel < ((BuildingPlan[])importSet.plans.get(aVariation)).length; aLevel++) {
               importTableImportBuilding(
                  player, tablePos.getRelative(xDelta, 0.0, zDelta), tablePos, importSet, aVariation, aLevel, orientation, importMockBlocks
               );
               zDelta += orientatedWidth + 2 + orientedGapWidth;
            }

            xDelta += orientatedLength + 2 + orientedGapLength;
         }

         return xDelta;
      }
   }

   public static void importTableImportBuilding(
      EntityPlayer player,
      Point tablePos,
      Point parentTablePos,
      BuildingPlanSet planSet,
      int variation,
      int maxLevel,
      int orientation,
      boolean importMockBlocks
   ) {
      BuildingPlan basePlan = planSet.getPlan(variation, 0);
      int orientatedLength = basePlan.length;
      int orientatedWidth = basePlan.width;
      if (orientation % 2 == 1) {
         orientatedLength = basePlan.width;
         orientatedWidth = basePlan.length;
      }

      if (basePlan.parentBuildingPlan != null) {
         String parentBuildingPlanKey = basePlan.parentBuildingPlan;
         String parentSuffix = parentBuildingPlanKey.split("_")[parentBuildingPlanKey.split("_").length - 1].toUpperCase();
         int parentVariation = parentSuffix.charAt(0) - 'A';
         int parentLevel = Integer.parseInt(parentSuffix.substring(1, parentSuffix.length()));
         String parentBuildingKey = parentBuildingPlanKey.substring(0, parentBuildingPlanKey.length() - parentSuffix.length() - 1);
         BuildingPlanSet parentBuildingSet = loadPlanSetFromExportDir(parentBuildingKey);
         BuildingLocation parentLocation = new BuildingLocation(
            parentBuildingSet.getPlan(parentVariation, parentLevel), tablePos.getRelative(orientatedLength / 2 + 1, 0.0, orientatedWidth / 2 + 1), orientation
         );

         for (int level = 0; level <= parentLevel; level++) {
            parentLocation.level = level;
            parentBuildingSet.buildLocation(
               Mill.getMillWorld(player.world), null, parentLocation, !importMockBlocks, false, null, true, importMockBlocks, null
            );
         }
      }

      BuildingLocation location = new BuildingLocation(basePlan, tablePos.getRelative(orientatedLength / 2 + 1, 0.0, orientatedWidth / 2 + 1), orientation);

      for (int level = 0; level <= maxLevel; level++) {
         location.level = level;
         planSet.buildLocation(Mill.getMillWorld(player.world), null, location, true, false, null, true, importMockBlocks, null);
      }

      drawWoolBorders(player, tablePos, orientatedLength, orientatedWidth);
      TileEntityImportTable table = tablePos.getImportTable(player.world);
      if (table == null) {
         tablePos.setBlock(player.world, MillBlocks.IMPORT_TABLE, 0, true, false);
         table = tablePos.getImportTable(player.world);
      }

      if (table == null) {
         MillLog.error(null, "Can neither find nor create import table at location: " + tablePos);
      } else {
         BuildingPlan plan = planSet.getPlan(variation, maxLevel);
         table.updatePlan(planSet.key, plan.length, plan.width, variation, maxLevel, plan.startLevel, player);
         table.setParentTablePos(parentTablePos);
      }

      ServerSender.sendTranslatedSentence(player, 'f', "importtable.importedbuildingplan", planSet.getPlan(variation, maxLevel).planName);
   }

   private static BuildingPlanSet loadPlanSetFromExportDir(String parentBuildingKey) {
      File exportDir = MillCommonUtilities.getExportDir();
      VirtualDir exportVirtualDir = new VirtualDir(exportDir);
      File parentBuildingFile = new File(exportDir, parentBuildingKey + "_A.txt");
      BuildingPlanSet parentBuildingSet = new BuildingPlanSet(null, parentBuildingKey, exportVirtualDir, parentBuildingFile);

      try {
         parentBuildingSet.loadPictPlans(true);
      } catch (Exception var6) {
         MillLog.printException("Exception when loading plan:", var6);
      }

      return parentBuildingSet;
   }

   public static void loadReverseBuildingPoints(Boolean exportPreserveGround, Boolean exportRegularChests) {
      reverseColourPoints.clear();

      for (PointType pt : PointType.colourPoints.values()) {
         if (pt.specialType == null) {
            Block block = pt.getBlock();
            if (block == null) {
               MillLog.error(pt, "PointType has neither name nor block.");
            } else {
               reverseColourPoints.put(getPointHash(pt.getBlockState()), pt);
               if (block == Blocks.LEAVES || block == Blocks.LEAVES2) {
                  reverseColourPoints.put(
                     getPointHash(
                        pt.getBlock(),
                        pt.getBlock()
                           .getMetaFromState(pt.getBlockState().withProperty(BlockLeaves.DECAYABLE, true).withProperty(BlockLeaves.CHECK_DECAY, true))
                     ),
                     pt
                  );
                  reverseColourPoints.put(
                     getPointHash(
                        pt.getBlock(),
                        pt.getBlock()
                           .getMetaFromState(pt.getBlockState().withProperty(BlockLeaves.DECAYABLE, true).withProperty(BlockLeaves.CHECK_DECAY, false))
                     ),
                     pt
                  );
                  reverseColourPoints.put(
                     getPointHash(
                        pt.getBlock(),
                        pt.getBlock()
                           .getMetaFromState(pt.getBlockState().withProperty(BlockLeaves.DECAYABLE, false).withProperty(BlockLeaves.CHECK_DECAY, true))
                     ),
                     pt
                  );
                  reverseColourPoints.put(
                     getPointHash(
                        pt.getBlock(),
                        pt.getBlock()
                           .getMetaFromState(pt.getBlockState().withProperty(BlockLeaves.DECAYABLE, false).withProperty(BlockLeaves.CHECK_DECAY, false))
                     ),
                     pt
                  );
               } else if (BlockItemUtilities.isPath(block)) {
                  reverseColourPoints.put(getPointHash(pt.getBlockState().withProperty(IBlockPath.STABLE, false)), pt);
               } else if (pt.getBlock() == MillBlocks.PANEL) {
                  reverseColourPoints.put(getPointHash(Blocks.WALL_SIGN, pt.getMeta()), pt);
               } else if (pt.getBlock() instanceof BlockDoor) {
                  reverseColourPoints.put(getPointHash(pt.getBlockState()), pt);
                  reverseColourPoints.put(getPointHash(pt.getBlockState().withProperty(BlockDoor.OPEN, true)), pt);
               } else if (pt.getBlock() instanceof BlockFenceGate) {
                  reverseColourPoints.put(getPointHash(pt.getBlockState()), pt);
                  reverseColourPoints.put(getPointHash(pt.getBlockState().withProperty(BlockFenceGate.OPEN, true)), pt);
               } else if (pt.getBlock() == Blocks.FURNACE) {
                  reverseColourPoints.put(getPointHash(pt.getBlockState()), pt);
                  int meta = pt.getBlock().getMetaFromState(pt.getBlockState());
                  IBlockState litBlockState = Blocks.LIT_FURNACE.getStateFromMeta(meta);
                  reverseColourPoints.put(getPointHash(litBlockState), pt);
               } else if (pt.getBlock() instanceof BlockFlowerPot) {
                  reverseColourPoints.put(getPointHash(pt.getBlock(), pt.getMeta()), pt);
               }
            }
         }
      }

      for (PointType ptx : PointType.colourPoints.values()) {
         if (ptx.specialType != null) {
            if (ptx.specialType.equals("brewingstand")) {
               for (int i = 0; i < 16; i++) {
                  reverseColourPoints.put(getPointHash(Blocks.BREWING_STAND, i), ptx);
               }
            } else if (ptx.specialType.equals("lockedchestTop")) {
               IBlockState bs = MillBlocks.LOCKED_CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.WEST);
               reverseColourPoints.put(getPointHash(bs), ptx);
               if (!exportRegularChests) {
                  bs = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.WEST);
                  reverseColourPoints.put(getPointHash(bs), ptx);
               }
            } else if (ptx.specialType.equals("lockedchestBottom")) {
               IBlockState bs = MillBlocks.LOCKED_CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.EAST);
               reverseColourPoints.put(getPointHash(bs), ptx);
               if (!exportRegularChests) {
                  bs = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.EAST);
                  reverseColourPoints.put(getPointHash(bs), ptx);
               }
            } else if (ptx.specialType.equals("lockedchestLeft")) {
               IBlockState bs = MillBlocks.LOCKED_CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH);
               reverseColourPoints.put(getPointHash(bs), ptx);
               if (!exportRegularChests) {
                  bs = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.SOUTH);
                  reverseColourPoints.put(getPointHash(bs), ptx);
               }
            } else if (ptx.specialType.equals("lockedchestRight")) {
               IBlockState bs = MillBlocks.LOCKED_CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.NORTH);
               reverseColourPoints.put(getPointHash(bs), ptx);
               if (!exportRegularChests) {
                  bs = Blocks.CHEST.getDefaultState().withProperty(BlockChest.FACING, EnumFacing.NORTH);
                  reverseColourPoints.put(getPointHash(bs), ptx);
               }
            } else if (ptx.specialType.equals("mainchestTop")) {
               IBlockState bs = MillBlocks.MAIN_CHEST.getStateFromMeta(EnumFacing.WEST.getIndex());
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.specialType.equals("mainchestBottom")) {
               IBlockState bs = MillBlocks.MAIN_CHEST.getStateFromMeta(EnumFacing.EAST.getIndex());
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.specialType.equals("mainchestLeft")) {
               IBlockState bs = MillBlocks.MAIN_CHEST.getStateFromMeta(EnumFacing.SOUTH.getIndex());
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.specialType.equals("mainchestRight")) {
               IBlockState bs = MillBlocks.MAIN_CHEST.getStateFromMeta(EnumFacing.NORTH.getIndex());
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.specialType.equals("grass") && !exportPreserveGround) {
               IBlockState bs = Blocks.GRASS.getDefaultState();
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.isSubType("villageBannerWall")) {
               String facing = ptx.specialType.substring(17);
               IBlockState bs = MillBlocks.VILLAGE_BANNER_WALL.getDefaultState().withProperty(BlockBanner.FACING, EnumFacing.byName(facing));
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.isSubType("villageBannerStanding")) {
               int rotation = Integer.parseInt(ptx.specialType.substring(21));
               IBlockState bs = MillBlocks.VILLAGE_BANNER_STANDING.getStateFromMeta(rotation);
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.isSubType("cultureBannerWall")) {
               String facing = ptx.specialType.substring(17);
               IBlockState bs = MillBlocks.CULTURE_BANNER_WALL.getDefaultState().withProperty(BlockBanner.FACING, EnumFacing.byName(facing));
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else if (ptx.isSubType("cultureBannerStanding")) {
               int rotation = Integer.parseInt(ptx.specialType.substring(21));
               IBlockState bs = MillBlocks.CULTURE_BANNER_STANDING.getStateFromMeta(rotation);
               reverseColourPoints.put(getPointHash(bs), ptx);
            } else {
               for (MockBlockMarker.Type type : MockBlockMarker.Type.values()) {
                  if (type.name.equalsIgnoreCase(ptx.specialType)) {
                     IBlockState bs = MillBlocks.MARKER_BLOCK.getStateFromMeta(type.meta);
                     reverseColourPoints.put(getPointHash(bs), ptx);
                  }
               }

               if (exportPreserveGround && "preserveground".equalsIgnoreCase(ptx.specialType)) {
                  reverseColourPoints.put(getPointHash(Blocks.GRASS, 0), ptx);
                  reverseColourPoints.put(getPointHash(Blocks.SAND, 0), ptx);
               }

               for (MockBlockAnimalSpawn.Creature creature : MockBlockAnimalSpawn.Creature.values()) {
                  if (ptx.specialType.equalsIgnoreCase(creature.name + "spawn")) {
                     IBlockState bs = MillBlocks.ANIMAL_SPAWN.getStateFromMeta(creature.meta);
                     reverseColourPoints.put(getPointHash(bs), ptx);
                  }
               }

               for (MockBlockSource.Resource resource : MockBlockSource.Resource.values()) {
                  if (ptx.specialType.equalsIgnoreCase(resource.name + "source")) {
                     IBlockState bs = MillBlocks.SOURCE.getStateFromMeta(resource.meta);
                     reverseColourPoints.put(getPointHash(bs), ptx);
                  }
               }

               for (MockBlockFree.Resource resourcex : MockBlockFree.Resource.values()) {
                  if (ptx.specialType.equalsIgnoreCase("free" + resourcex.name)) {
                     IBlockState bs = MillBlocks.FREE_BLOCK.getStateFromMeta(resourcex.meta);
                     reverseColourPoints.put(getPointHash(bs), ptx);
                  }
               }

               for (MockBlockTreeSpawn.TreeType treeType : MockBlockTreeSpawn.TreeType.values()) {
                  if (ptx.specialType.equalsIgnoreCase(treeType.name + "spawn")) {
                     IBlockState bs = MillBlocks.TREE_SPAWN.getStateFromMeta(treeType.meta);
                     reverseColourPoints.put(getPointHash(bs), ptx);
                  }
               }

               for (MockBlockSoil.CropType cropType : MockBlockSoil.CropType.values()) {
                  if (ptx.specialType.equalsIgnoreCase(cropType.name)) {
                     IBlockState bs = MillBlocks.SOIL_BLOCK.getStateFromMeta(cropType.meta);
                     reverseColourPoints.put(getPointHash(bs), ptx);
                  }
               }

               for (MockBlockDecor.Type decorType : MockBlockDecor.Type.values()) {
                  if (ptx.specialType.equalsIgnoreCase(decorType.name)) {
                     IBlockState bs = MillBlocks.DECOR_BLOCK.getStateFromMeta(decorType.meta);
                     reverseColourPoints.put(getPointHash(bs), ptx);
                  }
               }
            }
         }
      }
   }

   private static IBlockState mirrorBlock(PointType pt, boolean horizontal) {
      Comparable rawFacingValue = BlockStateUtilities.getPropertyValueByName(pt.getBlockState(), "facing");
      if (rawFacingValue != null && rawFacingValue instanceof EnumFacing) {
         EnumFacing facing = (EnumFacing)rawFacingValue;
         if (horizontal) {
            if (facing == EnumFacing.EAST) {
               facing = EnumFacing.WEST;
            } else if (facing == EnumFacing.WEST) {
               facing = EnumFacing.EAST;
            }
         } else if (facing == EnumFacing.NORTH) {
            facing = EnumFacing.SOUTH;
         } else if (facing == EnumFacing.SOUTH) {
            facing = EnumFacing.NORTH;
         }

         return BlockStateUtilities.setPropertyValueByName(pt.getBlockState(), "facing", facing);
      } else {
         return null;
      }
   }

   private static void mirrorPlan(BuildingPlanSet existingSet, boolean horizontalmirror) {
      for (BuildingPlan[] plans : existingSet.plans) {
         for (BuildingPlan plan : plans) {
            int planLength = plan.plan[0].length;
            int planWidth = plan.plan[0][0].length;

            for (int floorPos = 0; floorPos < plan.plan.length; floorPos++) {
               for (int lengthPos = 0; lengthPos < plan.plan[0].length; lengthPos++) {
                  for (int widthPos = 0; widthPos < plan.plan[0][0].length; widthPos++) {
                     int newLengthPos;
                     int newWidthPos;
                     if (horizontalmirror) {
                        newLengthPos = planLength - lengthPos - 1;
                        newWidthPos = widthPos;
                     } else {
                        newLengthPos = lengthPos;
                        newWidthPos = planWidth - widthPos - 1;
                     }

                     if (!plan.plan[floorPos][lengthPos][widthPos].isType("empty")) {
                        if (plan.plan[floorPos][lengthPos][widthPos].specialType != null) {
                           plan.plan[floorPos][newLengthPos][newWidthPos] = plan.plan[floorPos][lengthPos][widthPos];
                        } else {
                           IBlockState blockState = mirrorBlock(plan.plan[floorPos][lengthPos][widthPos], horizontalmirror);
                           if (blockState != null) {
                              PointType newPt = getPointTypeFromBlockState(blockState);
                              if (newPt != null) {
                                 plan.plan[floorPos][lengthPos][widthPos] = newPt;
                              }
                           } else {
                              plan.plan[floorPos][newLengthPos][newWidthPos] = plan.plan[floorPos][lengthPos][widthPos];
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void negationWandExportBuilding(EntityPlayer player, World world, Point startPoint) {
      try {
         TileEntitySign sign = startPoint.getSign(world);
         if (sign == null) {
            return;
         }

         if (sign.signText[0] == null || sign.signText[0].getUnformattedText().length() == 0) {
            Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), '6', "export.errornoname");
            return;
         }

         String planName = sign.signText[0].getUnformattedText().toLowerCase();
         int variation = 0;

         for (int letter = 0; letter < 26; letter++) {
            if (planName.endsWith("_" + (char)(97 + letter))) {
               planName = planName.substring(0, planName.length() - 2);
               variation = letter;
            }
         }

         int upgradeLevel = -1;
         if (sign.signText[1] != null && sign.signText[1].getUnformattedText().length() > 0) {
            try {
               upgradeLevel = Integer.parseInt(sign.signText[1].getUnformattedText());
            } catch (Exception var17) {
               ServerSender.sendTranslatedSentence(player, '6', "export.errorinvalidupgradelevel");
               return;
            }
         }

         int xEnd = startPoint.getiX() + 1;

         boolean found;
         for (found = false; !found && xEnd < startPoint.getiX() + 257; xEnd++) {
            Block block = WorldUtilities.getBlock(world, xEnd, startPoint.getiY(), startPoint.getiZ());
            if (block == Blocks.STANDING_SIGN) {
               found = true;
               break;
            }
         }

         if (!found) {
            Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), '6', "export.errornoendsigneast");
            return;
         }

         int zEnd = startPoint.getiZ() + 1;

         for (found = false; !found && zEnd < startPoint.getiZ() + 257; zEnd++) {
            Block block = WorldUtilities.getBlock(world, startPoint.getiX(), startPoint.getiY(), zEnd);
            if (block == Blocks.STANDING_SIGN) {
               found = true;
               break;
            }
         }

         if (!found) {
            Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), '6', "export.errornoendsignsouth");
            return;
         }

         int startLevel = -1;
         if (sign.signText[2] != null && sign.signText[2].getUnformattedText().length() > 0) {
            try {
               startLevel = Integer.parseInt(sign.signText[2].getUnformattedText());
            } catch (Exception var16) {
               Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), '6', "export.errorstartinglevel");
            }
         } else {
            Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), 'f', "export.defaultstartinglevel");
         }

         boolean exportSnow = false;
         if (sign.signText[3] != null && sign.signText[3].getUnformattedText().equals("snow")) {
            exportSnow = true;
         }

         int length = xEnd - startPoint.getiX() - 1;
         int width = zEnd - startPoint.getiZ() - 1;
         int orientation = 0;
         if (sign.signText[3] != null && sign.signText[3].getUnformattedText().startsWith("or:")) {
            String orientationString = sign.signText[3].getUnformattedText().substring(3, sign.signText[3].getUnformattedText().length());
            orientation = Integer.parseInt(orientationString);
         }

         if (orientation != 0) {
            Mill.proxy.localTranslatedSentence(Mill.proxy.getTheSinglePlayer(), '6', "export.northfacingonly");
            return;
         }

         exportBuilding(world, startPoint, planName, variation, length, width, orientation, upgradeLevel, startLevel, exportSnow, false, true);
      } catch (Exception var18) {
         MillLog.printException("Error when trying to store a building: ", var18);
      }
   }

   private static void replaceWoodType(BuildingPlanSet existingSet, EnumType newWoodType) {
      Block newLogBlock;
      Block newLeaveBlock;
      IProperty newLogBlockVariantProperty;
      IProperty newLeaveBlockVariantProperty;
      if (!newWoodType.equals(EnumType.ACACIA) && !newWoodType.equals(EnumType.DARK_OAK)) {
         newLogBlock = Blocks.LOG;
         newLeaveBlock = Blocks.LEAVES;
         newLogBlockVariantProperty = BlockOldLog.VARIANT;
         newLeaveBlockVariantProperty = BlockOldLeaf.VARIANT;
      } else {
         newLogBlock = Blocks.LOG2;
         newLeaveBlock = Blocks.LEAVES2;
         newLogBlockVariantProperty = BlockNewLog.VARIANT;
         newLeaveBlockVariantProperty = BlockNewLeaf.VARIANT;
      }

      Block[][] blocksToReplace = new Block[][]{
         {Blocks.OAK_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.BIRCH_STAIRS, Blocks.JUNGLE_STAIRS, Blocks.ACACIA_STAIRS, Blocks.DARK_OAK_STAIRS},
         {Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR},
         {Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE, Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE},
         {Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE}
      };

      for (BuildingPlan[] plans : existingSet.plans) {
         for (BuildingPlan plan : plans) {
            for (int floorPos = 0; floorPos < plan.plan.length; floorPos++) {
               for (int lengthPos = 0; lengthPos < plan.plan[0].length; lengthPos++) {
                  for (int widthPos = 0; widthPos < plan.plan[0][0].length; widthPos++) {
                     PointType pt = plan.plan[floorPos][lengthPos][widthPos];
                     if (pt.getBlock() != Blocks.LOG && pt.getBlock() != Blocks.LOG2) {
                        if (pt.getBlock() != Blocks.LEAVES && pt.getBlock() != Blocks.LEAVES2) {
                           if (pt.getBlockState() != null) {
                              Comparable rawWoodTypeValue = BlockStateUtilities.getPropertyValueByName(pt.getBlockState(), "variant");
                              if (rawWoodTypeValue != null && rawWoodTypeValue instanceof EnumType) {
                                 IBlockState adjustedBlockState = BlockStateUtilities.setPropertyValueByName(pt.getBlockState(), "variant", newWoodType);
                                 PointType newPt = getPointTypeFromBlockState(adjustedBlockState);
                                 if (newPt != null) {
                                    plan.plan[floorPos][lengthPos][widthPos] = newPt;
                                 }
                              }

                              for (Block[] blockList : blocksToReplace) {
                                 for (Block block : blockList) {
                                    if (pt.getBlock() == block) {
                                       Block newBlock = blockList[newWoodType.getMetadata()];
                                       IBlockState newBlockState = newBlock.getStateFromMeta(pt.getMeta());
                                       PointType newPt = getPointTypeFromBlockState(newBlockState);
                                       if (newPt != null) {
                                          plan.plan[floorPos][lengthPos][widthPos] = newPt;
                                       }
                                    }
                                 }
                              }
                           }
                        } else {
                           IBlockState newBlockState = newLeaveBlock.getStateFromMeta(pt.getMeta()).withProperty(newLeaveBlockVariantProperty, newWoodType);
                           PointType newPt = getPointTypeFromBlockState(newBlockState);
                           if (newPt != null) {
                              plan.plan[floorPos][lengthPos][widthPos] = newPt;
                           }
                        }
                     } else {
                        IBlockState newBlockState = newLogBlock.getStateFromMeta(pt.getMeta()).withProperty(newLogBlockVariantProperty, newWoodType);
                        PointType newPt = getPointTypeFromBlockState(newBlockState);
                        if (newPt != null) {
                           plan.plan[floorPos][lengthPos][widthPos] = newPt;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void summoningWandImportBuildingPlan(
      EntityPlayer player,
      World world,
      Point startPoint,
      int variation,
      BuildingPlanSet existingSet,
      int upgradeLevel,
      boolean includeSpecialPoints,
      int orientation,
      boolean createSign
   ) {
      BuildingPlan basePlan = existingSet.plans.get(variation)[0];
      BuildingPlan upgradePlan = existingSet.plans.get(variation)[upgradeLevel];
      if (createSign) {
         startPoint.setBlock(world, Blocks.STANDING_SIGN, 0, true, false);
         TileEntitySign sign = startPoint.getSign(world);
         char variationLetter = (char)(65 + variation);
         sign.signText[0] = new TextComponentString(existingSet.key + "_" + variationLetter);
         sign.signText[1] = new TextComponentString("" + upgradeLevel);
         sign.signText[2] = new TextComponentString("" + upgradePlan.startLevel);
         if (!includeSpecialPoints) {
            sign.signText[3] = new TextComponentString("nomock");
         } else if (orientation > 0) {
            sign.signText[3] = new TextComponentString("or:" + orientation);
         } else {
            sign.signText[3] = new TextComponentString("");
         }
      }

      int orientatedLength = basePlan.length;
      int orientatedWidth = basePlan.width;
      if (orientation % 2 == 1) {
         orientatedLength = basePlan.width;
         orientatedWidth = basePlan.length;
      }

      BuildingLocation location = new BuildingLocation(basePlan, startPoint.getRelative(orientatedLength / 2 + 1, 0.0, orientatedWidth / 2 + 1), orientation);

      for (int i = 0; i <= upgradeLevel; i++) {
         if (player != null) {
            ServerSender.sendTranslatedSentence(player, 'f', "import.buildinglevel", "" + i);
         }

         existingSet.buildLocation(Mill.getMillWorld(world), null, location, !includeSpecialPoints, false, null, true, includeSpecialPoints, null);
         location.level++;
      }

      Point eastSign = startPoint.getRelative(orientatedLength + 1, 0.0, 0.0);
      eastSign.setBlock(world, Blocks.STANDING_SIGN, 0, true, false);
      TileEntitySign sign = eastSign.getSign(world);
      sign.signText[0] = new TextComponentString("East End");
      sign.signText[1] = new TextComponentString("(length)");
      sign.signText[2] = new TextComponentString("");
      sign.signText[3] = new TextComponentString("");
      Point southSign = startPoint.getRelative(0.0, 0.0, orientatedWidth + 1);
      southSign.setBlock(world, Blocks.STANDING_SIGN, 0, true, false);
      sign = southSign.getSign(world);
      sign.signText[0] = new TextComponentString("South End");
      sign.signText[1] = new TextComponentString("(width)");
      sign.signText[2] = new TextComponentString("");
      sign.signText[3] = new TextComponentString("");
   }

   public static void summoningWandImportBuildingRequest(EntityPlayer player, World world, Point startPoint) {
      try {
         TileEntitySign sign = startPoint.getSign(world);
         if (sign == null) {
            return;
         }

         if (sign.signText[0] == null || sign.signText[0].getUnformattedText().length() == 0) {
            ServerSender.sendTranslatedSentence(player, '6', "import.errornoname");
            return;
         }

         String buildingKey = sign.signText[0].getUnformattedText().toLowerCase();
         int variation = 0;
         boolean explicitVariation = false;

         for (int letter = 0; letter < 26; letter++) {
            if (buildingKey.endsWith("_" + (char)(97 + letter))) {
               buildingKey = buildingKey.substring(0, buildingKey.length() - 2);
               variation = letter;
               explicitVariation = true;
            }
         }

         char variationLetter = 'A';
         variationLetter = (char)(variationLetter + variation);
         File exportDir = MillCommonUtilities.getExportDir();
         File buildingFile = new File(exportDir, buildingKey + "_" + variationLetter + ".txt");
         if (!buildingFile.exists()) {
            File foundFile = null;
            BuildingPlanSet foundPlanSet = null;

            for (Culture culture : Culture.ListCultures) {
               for (BuildingPlanSet planSet : culture.ListPlanSets) {
                  if (foundFile == null && planSet.key.equals(buildingKey) && planSet.plans.size() > variation) {
                     foundFile = planSet.plans.get(variation)[0].getLoadedFromFile();
                     foundPlanSet = planSet;
                  }
               }
            }

            if (foundFile == null) {
               for (Culture culture : Culture.ListCultures) {
                  for (BuildingPlanSet planSetx : culture.ListPlanSets) {
                     if (foundFile == null
                        && planSetx.plans.size() > variation
                        && planSetx.plans.get(variation)[0].nativeName != null
                        && planSetx.plans.get(variation)[0].nativeName.toLowerCase().equals(buildingKey)) {
                        foundFile = planSetx.plans.get(variation)[0].getLoadedFromFile();
                        foundPlanSet = planSetx;
                        buildingKey = planSetx.key;
                        ITextComponent[] oldSignData = sign.signText;
                        startPoint.setBlock(world, Blocks.STANDING_SIGN, 0, true, false);
                        TileEntitySign newSign = startPoint.getSign(world);
                        newSign.signText[0] = new TextComponentString(buildingKey);
                        newSign.signText[1] = oldSignData[1];
                        newSign.signText[2] = oldSignData[2];
                        newSign.signText[3] = oldSignData[3];
                     }
                  }
               }
            }

            if (foundFile == null) {
               ServerSender.sendTranslatedSentence(player, '6', "import.errornotfound");
               return;
            }

            ServerSender.sendTranslatedSentence(player, '6', "import.copyingfrom", foundFile.getAbsolutePath().replace("\\", "/"));
            Path exportPath = exportDir.toPath();
            Path inputPath = foundFile.toPath().getParent();

            for (int exportVariation = 0; exportVariation < foundPlanSet.plans.size(); exportVariation++) {
               char exportVariationLetter = (char)(65 + exportVariation);
               String txtFileName = foundPlanSet.key + "_" + exportVariationLetter + ".txt";
               Files.copy(inputPath.resolve(txtFileName), exportPath.resolve(txtFileName), StandardCopyOption.REPLACE_EXISTING);

               for (int buildingUpgrade = 0; buildingUpgrade < ((BuildingPlan[])foundPlanSet.plans.get(exportVariation)).length; buildingUpgrade++) {
                  String pngFileName = foundPlanSet.key + "_" + exportVariationLetter + buildingUpgrade + ".png";
                  Files.copy(inputPath.resolve(pngFileName), exportPath.resolve(pngFileName), StandardCopyOption.REPLACE_EXISTING);
               }
            }
         }

         BuildingPlanSet existingSet = loadPlanSetFromExportDir(buildingKey);
         boolean importAll = sign.signText[1] != null && sign.signText[1].getUnformattedText().equalsIgnoreCase("all");
         boolean includeSpecialPoints = sign.signText[3] == null || !sign.signText[3].getUnformattedText().equalsIgnoreCase("nomock");
         int orientation = 0;
         if (sign.signText[3] != null && sign.signText[3].getUnformattedText().startsWith("or:")) {
            String orientationString = sign.signText[3].getUnformattedText().substring(3, sign.signText[3].getUnformattedText().length());
            orientation = Integer.parseInt(orientationString);
         }

         if (!importAll) {
            int upgradeLevel = 0;
            if (sign.signText[1] != null && sign.signText[1].getUnformattedText().length() > 0) {
               try {
                  upgradeLevel = Integer.parseInt(sign.signText[1].getUnformattedText());
                  ServerSender.sendTranslatedSentence(player, 'f', "import.buildingupto", "" + upgradeLevel);
               } catch (Exception var22) {
                  ServerSender.sendTranslatedSentence(player, '6', "import.errorinvalidupgradelevel");
                  return;
               }
            } else {
               ServerSender.sendTranslatedSentence(player, 'f', "import.buildinginitialphase");
            }

            if (upgradeLevel >= existingSet.plans.get(variation).length) {
               ServerSender.sendTranslatedSentence(player, '6', "import.errorupgradeleveltoohigh");
               return;
            }

            if (sign.signText[2] != null) {
               String signLine = sign.signText[2].getUnformattedText();
               if (signLine.equals("x2")) {
                  doubleHeightPlan(player, existingSet);
               } else if (!signLine.equals("hmirror") && !signLine.equals("vmirror")) {
                  if (signLine.startsWith("wood:")) {
                     String woodTypeName = signLine.substring("wood:".length(), signLine.length());
                     EnumType woodType = null;

                     for (EnumType type : EnumType.values()) {
                        if (type.getName().equals(woodTypeName)) {
                           woodType = type;
                        }
                     }

                     if (woodType == null) {
                        ServerSender.sendTranslatedSentence(player, '6', "import.errorunknownwoodtype", woodTypeName);
                     } else {
                        replaceWoodType(existingSet, woodType);
                     }
                  }
               } else {
                  boolean horizontalmirror = signLine.equals("hmirror");
                  mirrorPlan(existingSet, horizontalmirror);
               }
            }

            summoningWandImportBuildingPlan(player, world, startPoint, variation, existingSet, upgradeLevel, includeSpecialPoints, orientation, false);
         } else {
            Point adjustedStartPoint = startPoint.getRelative(1.0, 0.0, 0.0);
            int variationStart = 0;
            int variationEnd = existingSet.plans.size();
            if (explicitVariation) {
               variationStart = variation;
               variationEnd = variation + 1;
            }

            for (int variationPos = variationStart; variationPos < variationEnd; variationPos++) {
               for (int maxLevel = 0; maxLevel < ((BuildingPlan[])existingSet.plans.get(variationPos)).length; maxLevel++) {
                  summoningWandImportBuildingPlan(
                     player, world, adjustedStartPoint, variationPos, existingSet, maxLevel, includeSpecialPoints, orientation, true
                  );
                  adjustedStartPoint = adjustedStartPoint.getRelative(existingSet.plans.get(variationPos)[0].length + 10, 0.0, 0.0);
               }

               adjustedStartPoint = new Point(startPoint.x, startPoint.y, adjustedStartPoint.z + existingSet.plans.get(variationPos)[0].width + 10.0);
            }
         }
      } catch (Exception var23) {
         MillLog.printException("Error when importing a building:", var23);
      }
   }
}
