package org.millenaire.common.buildingplan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingLocation;

public class BuildingCustomPlan implements IBuildingPlan {
   public final Culture culture;
   public String nativeName;
   public String shop = null;
   public String buildingKey;
   public String gameNameKey = null;
   public final Map<String, String> names = new HashMap<>();
   public List<String> maleResident = new ArrayList<>();
   public List<String> femaleResident = new ArrayList<>();
   public List<String> visitors = new ArrayList<>();
   public int priorityMoveIn = 1;
   public int radius = 6;
   public int heightRadius = 4;
   public List<String> tags = new ArrayList<>();
   public ResourceLocation cropType = null;
   public ResourceLocation spawnType = null;
   public Map<BuildingCustomPlan.TypeRes, Integer> minResources = new HashMap<>();
   public Map<BuildingCustomPlan.TypeRes, Integer> maxResources = new HashMap<>();

   public static Map<String, BuildingCustomPlan> loadCustomBuildings(VirtualDir cultureVirtualDir, Culture culture) {
      Map<String, BuildingCustomPlan> buildingCustoms = new HashMap<>();
      VirtualDir customBuildingsVirtualDir = cultureVirtualDir.getChildDirectory("custombuildings");
      BuildingFileFiler textFiles = new BuildingFileFiler(".txt");

      for (File file : customBuildingsVirtualDir.listFilesRecursive(textFiles)) {
         try {
            if (MillConfigValues.LogBuildingPlan >= 1) {
               MillLog.major(file, "Loaded custom building");
            }

            BuildingCustomPlan buildingCustom = new BuildingCustomPlan(file, culture);
            buildingCustoms.put(buildingCustom.buildingKey, buildingCustom);
         } catch (Exception var8) {
            MillLog.printException("Error when loading " + file.getAbsolutePath(), var8);
         }
      }

      return buildingCustoms;
   }

   public BuildingCustomPlan(Culture culture, String key) {
      this.culture = culture;
      this.buildingKey = key;
   }

   public BuildingCustomPlan(File file, Culture culture) throws IOException {
      this.culture = culture;
      this.buildingKey = file.getName().split("\\.")[0];
      BufferedReader reader = MillCommonUtilities.getReader(file);
      String line = reader.readLine();
      this.readConfigLine(line);
      if (MillConfigValues.LogBuildingPlan >= 1) {
         MillLog.major(this, "Loaded custom building " + this.buildingKey + this.nativeName + " pop: " + this.maleResident + "/" + this.femaleResident);
      }

      if (!this.minResources.containsKey(BuildingCustomPlan.TypeRes.SIGN)) {
         MillLog.error(this, "No signs in custom building.");
      }
   }

   private void adjustLocationSize(BuildingLocation location, Map<BuildingCustomPlan.TypeRes, List<Point>> resources) {
      int startX = location.pos.getiX();
      int startY = location.pos.getiY();
      int startZ = location.pos.getiZ();
      int endX = location.pos.getiX();
      int endY = location.pos.getiY();
      int endZ = location.pos.getiZ();

      for (BuildingCustomPlan.TypeRes type : resources.keySet()) {
         for (Point p : resources.get(type)) {
            if (startX >= p.getiX()) {
               startX = p.getiX();
            }

            if (startY >= p.getiY()) {
               startY = p.getiY();
            }

            if (startZ >= p.getiZ()) {
               startZ = p.getiZ();
            }

            if (endX <= p.getiX()) {
               endX = p.getiX();
            }

            if (endY <= p.getiY()) {
               endY = p.getiY();
            }

            if (endZ <= p.getiZ()) {
               endZ = p.getiZ();
            }
         }
      }

      location.minx = startX - 1;
      location.maxx = endX + 1;
      location.miny = startY - 1;
      location.maxy = endY + 1;
      location.minz = startZ - 1;
      location.maxz = endZ + 1;
      location.length = location.maxx - location.minx + 1;
      location.width = location.maxz - location.minz + 1;
      location.computeMargins();
   }

   public Map<BuildingCustomPlan.TypeRes, List<Point>> findResources(World world, Point pos, Building townHall, BuildingLocation currentLocation) {
      Map<BuildingCustomPlan.TypeRes, List<Point>> resources = new HashMap<>();

      for (int currentRadius = 0; currentRadius < this.radius; currentRadius++) {
         for (int y = pos.getiY() - this.heightRadius + 1; y < pos.getiY() + this.heightRadius + 1; y++) {
            int z = pos.getiZ() - currentRadius;

            for (int x = pos.getiX() - currentRadius; x <= pos.getiX() + currentRadius; x++) {
               this.handlePoint(x, y, z, world, resources, townHall, currentLocation);
            }

            int var10 = pos.getiX() - currentRadius;

            for (int var13 = pos.getiZ() - currentRadius + 1; var13 <= pos.getiZ() + currentRadius - 1; var13++) {
               this.handlePoint(var10, y, var13, world, resources, townHall, currentLocation);
            }

            z = pos.getiZ() + currentRadius;

            for (int var11 = pos.getiX() - currentRadius; var11 <= pos.getiX() + currentRadius; var11++) {
               this.handlePoint(var11, y, z, world, resources, townHall, currentLocation);
            }

            var10 = pos.getiX() + currentRadius;

            for (int var15 = pos.getiZ() - currentRadius + 1; var15 <= pos.getiZ() + currentRadius - 1; var15++) {
               this.handlePoint(var10, y, var15, world, resources, townHall, currentLocation);
            }
         }
      }

      return resources;
   }

   @Override
   public Culture getCulture() {
      return this.culture;
   }

   @Override
   public List<String> getFemaleResident() {
      return this.femaleResident;
   }

   public String getFullDisplayName() {
      String name = this.nativeName;
      if (this.getNameTranslated() != null && this.getNameTranslated().length() > 0) {
         name = name + " (" + this.getNameTranslated() + ")";
      }

      return name;
   }

   @Override
   public List<String> getMaleResident() {
      return this.maleResident;
   }

   @Override
   public String getNameTranslated() {
      return this.culture.canReadBuildingNames() ? this.culture.getCustomBuildingGameName(this) : "";
   }

   @Override
   public String getNativeName() {
      return this.nativeName;
   }

   @Override
   public List<String> getVisitors() {
      return this.visitors;
   }

   private void handlePoint(
      int x, int y, int z, World world, Map<BuildingCustomPlan.TypeRes, List<Point>> resources, Building townHall, BuildingLocation currentLocation
   ) {
      Point p = new Point(x, y, z);
      if (townHall != null) {
         BuildingLocation locationAtPos = townHall.getLocationAtCoord(p);
         if (locationAtPos == null || !locationAtPos.equals(currentLocation)) {
            for (BuildingLocation bl : townHall.getLocations()) {
               if ((currentLocation == null || !currentLocation.isSameLocation(bl)) && bl.isInsideZone(p)) {
                  return;
               }
            }
         }
      }

      BuildingCustomPlan.TypeRes res = this.identifyRes(world, p);
      if (res != null && this.maxResources.containsKey(res)) {
         if (resources.containsKey(res) && resources.get(res).size() < this.maxResources.get(res)) {
            resources.get(res).add(p);
         } else if (!resources.containsKey(res)) {
            List<Point> points = new ArrayList<>();
            points.add(p);
            resources.put(res, points);
         }
      }
   }

   private BuildingCustomPlan.TypeRes identifyRes(World world, Point p) {
      Block b = p.getBlock(world);
      int meta = p.getMeta(world);
      if (b.equals(Blocks.CHEST) || b.equals(MillBlocks.LOCKED_CHEST)) {
         return BuildingCustomPlan.TypeRes.CHEST;
      } else if (b.equals(Blocks.CRAFTING_TABLE)) {
         return BuildingCustomPlan.TypeRes.CRAFT;
      } else if (b.equals(Blocks.WALL_SIGN) || b.equals(MillBlocks.PANEL)) {
         return BuildingCustomPlan.TypeRes.SIGN;
      } else if (b.equals(Blocks.FARMLAND)) {
         return BuildingCustomPlan.TypeRes.FIELD;
      } else if (b.equals(Blocks.HAY_BLOCK)) {
         return BuildingCustomPlan.TypeRes.SPAWN;
      } else if (!b.equals(Blocks.SAPLING)
         && (!b.equals(Blocks.LOG) && !b.equals(Blocks.LOG2) || !p.getBelow().getBlock(world).equals(Blocks.DIRT))) {
         if (b.equals(Blocks.WOOL) && p.getMeta(world) == 4) {
            return BuildingCustomPlan.TypeRes.STALL;
         } else if (!b.equals(Blocks.STONE)
               && !b.equals(Blocks.SANDSTONE)
               && !b.equals(Blocks.SAND)
               && !b.equals(Blocks.GRAVEL)
               && !b.equals(Blocks.CLAY)
            || !p.getAbove().getBlock(world).equals(Blocks.AIR)
               && !p.getRelative(1.0, 0.0, 0.0).getBlock(world).equals(Blocks.AIR)
               && !p.getRelative(-1.0, 0.0, 0.0).getBlock(world).equals(Blocks.AIR)
               && !p.getRelative(0.0, 0.0, 1.0).getBlock(world).equals(Blocks.AIR)
               && !p.getRelative(0.0, 0.0, -1.0).getBlock(world).equals(Blocks.AIR)) {
            if (b.equals(Blocks.FURNACE)) {
               return BuildingCustomPlan.TypeRes.FURNACE;
            } else if (b.equals(MillBlocks.FIRE_PIT)) {
               return BuildingCustomPlan.TypeRes.FIRE_PIT;
            } else if (b.equals(MillBlocks.WET_BRICK) && meta == 0) {
               return BuildingCustomPlan.TypeRes.MUDBRICK;
            } else if (b.equals(Blocks.REEDS) && !p.getBelow().getBlock(world).equals(Blocks.REEDS)) {
               return BuildingCustomPlan.TypeRes.SUGAR;
            } else if (b.equals(Blocks.WOOL) && p.getMeta(world) == 3) {
               return BuildingCustomPlan.TypeRes.FISHING;
            } else if (b.equals(Blocks.WOOL) && p.getMeta(world) == 0) {
               return BuildingCustomPlan.TypeRes.SILK;
            } else {
               if (b.equals(Blocks.WOOL) && p.getMeta(world) == 11) {
                  Point[] neighbours = new Point[]{
                     p.getRelative(1.0, 0.0, 0.0), p.getRelative(-1.0, 0.0, 0.0), p.getRelative(0.0, 0.0, 1.0), p.getRelative(0.0, 0.0, -1.0)
                  };
                  boolean waterAround = true;

                  for (Point p2 : neighbours) {
                     if (!p2.getBlock(world).equals(Blocks.WATER)) {
                        waterAround = false;
                     }
                  }

                  if (waterAround) {
                     return BuildingCustomPlan.TypeRes.SQUID;
                  }
               }

               return b.equals(Blocks.COCOA) ? BuildingCustomPlan.TypeRes.CACAO : null;
            }
         } else {
            return BuildingCustomPlan.TypeRes.MINING;
         }
      } else {
         return BuildingCustomPlan.TypeRes.SAPLING;
      }
   }

   private void readConfigLine(String line) {
      String[] configs = line.split(";", -1);

      for (String config : configs) {
         if (config.split(":").length == 2) {
            String key = config.split(":")[0].toLowerCase();
            String value = config.split(":")[1];
            if (key.equalsIgnoreCase("moveinpriority")) {
               this.priorityMoveIn = Integer.parseInt(value);
            } else if (key.equalsIgnoreCase("radius")) {
               this.radius = Integer.parseInt(value);
            } else if (key.equalsIgnoreCase("heightradius")) {
               this.heightRadius = Integer.parseInt(value);
            } else if (key.equalsIgnoreCase("native")) {
               this.nativeName = value;
            } else if (key.equalsIgnoreCase("gameNameKey")) {
               this.gameNameKey = value;
            } else if (key.equalsIgnoreCase("cropType")) {
               this.cropType = new ResourceLocation(value);
            } else if (key.equalsIgnoreCase("spawnType")) {
               this.spawnType = new ResourceLocation(value);
            } else if (key.startsWith("name_")) {
               this.names.put(key, value);
            } else if (key.equalsIgnoreCase("male")) {
               if (this.culture.villagerTypes.containsKey(value.toLowerCase())) {
                  this.maleResident.add(value.toLowerCase());
               } else {
                  MillLog.error(this, "Attempted to load unknown male villager: " + value);
               }
            } else if (key.equalsIgnoreCase("female")) {
               if (this.culture.villagerTypes.containsKey(value.toLowerCase())) {
                  this.femaleResident.add(value.toLowerCase());
               } else {
                  MillLog.error(this, "Attempted to load unknown female villager: " + value);
               }
            } else if (key.equalsIgnoreCase("visitor")) {
               if (this.culture.villagerTypes.containsKey(value.toLowerCase())) {
                  this.visitors.add(value.toLowerCase());
               } else {
                  MillLog.error(this, "Attempted to load unknown visitor: " + value);
               }
            } else if (key.equalsIgnoreCase("shop")) {
               if (!this.culture.shopBuys.containsKey(value) && !this.culture.shopSells.containsKey(value) && !this.culture.shopBuysOptional.containsKey(value)
                  )
                {
                  MillLog.error(this, "Undefined shop type: " + value);
               } else {
                  this.shop = value;
               }
            } else if (key.equalsIgnoreCase("tag")) {
               this.tags.add(value.toLowerCase());
            } else {
               boolean found = false;

               for (BuildingCustomPlan.TypeRes typeRes : BuildingCustomPlan.TypeRes.values()) {
                  if (typeRes.key.equals(key)) {
                     try {
                        found = true;
                        if (value.contains("-")) {
                           this.minResources.put(typeRes, Integer.parseInt(value.split("-")[0]));
                           this.maxResources.put(typeRes, Integer.parseInt(value.split("-")[1]));
                        } else {
                           this.minResources.put(typeRes, Integer.parseInt(value));
                           this.maxResources.put(typeRes, Integer.parseInt(value));
                        }
                     } catch (Exception var15) {
                        MillLog.printException(
                           "Exception while parsing res " + typeRes.key + " in custom file " + this.buildingKey + " of culture " + this.culture.key + ":",
                           var15
                        );
                     }
                  }
               }

               if (!found) {
                  MillLog.error(this, "Could not recognise key on line: " + config);
               }
            }
         }
      }
   }

   public void registerResources(Building building, BuildingLocation location) {
      Map<BuildingCustomPlan.TypeRes, List<Point>> resources = this.findResources(building.world, location.pos, building.getTownHall(), location);
      this.adjustLocationSize(location, resources);
      building.getResManager().setSleepingPos(location.pos.getAbove());
      location.sleepingPos = location.pos.getAbove();
      if (resources.containsKey(BuildingCustomPlan.TypeRes.CHEST)) {
         building.getResManager().chests.clear();

         for (Point chestP : resources.get(BuildingCustomPlan.TypeRes.CHEST)) {
            if (chestP.getBlock(building.world).equals(Blocks.CHEST)) {
               int meta = chestP.getMeta(building.world);
               chestP.setBlock(building.world, MillBlocks.LOCKED_CHEST, meta, false, false);
            }

            building.getResManager().chests.add(chestP);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.CRAFT) && resources.get(BuildingCustomPlan.TypeRes.CRAFT).size() > 0) {
         location.craftingPos = resources.get(BuildingCustomPlan.TypeRes.CRAFT).get(0);
         building.getResManager().setCraftingPos(resources.get(BuildingCustomPlan.TypeRes.CRAFT).get(0));
      }

      this.registerSigns(building, resources);
      if (this.cropType != null && resources.containsKey(BuildingCustomPlan.TypeRes.FIELD)) {
         building.getResManager().soils.clear();
         building.getResManager().soilTypes.clear();

         for (Point p : resources.get(BuildingCustomPlan.TypeRes.FIELD)) {
            building.getResManager().addSoilPoint(this.cropType, p);
         }
      }

      if (this.spawnType != null && resources.containsKey(BuildingCustomPlan.TypeRes.SPAWN)) {
         building.getResManager().spawns.clear();
         building.getResManager().spawnTypes.clear();

         for (Point p : resources.get(BuildingCustomPlan.TypeRes.SPAWN)) {
            p.setBlock(building.world, Blocks.AIR, 0, true, false);
            building.getResManager().addSpawnPoint(this.spawnType, p);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.SAPLING)) {
         building.getResManager().woodspawn.clear();

         for (Point p : resources.get(BuildingCustomPlan.TypeRes.SAPLING)) {
            building.getResManager().woodspawn.add(p);
            IBlockState bs = building.world.getBlockState(p.getBlockPos());
            if (bs.getBlock() == Blocks.SAPLING) {
               if (bs.getValue(BlockSapling.TYPE) == EnumType.OAK) {
                  building.getResManager().woodspawnTypes.put(p, "oakspawn");
               } else if (bs.getValue(BlockSapling.TYPE) == EnumType.SPRUCE) {
                  building.getResManager().woodspawnTypes.put(p, "pinespawn");
               } else if (bs.getValue(BlockSapling.TYPE) == EnumType.BIRCH) {
                  building.getResManager().woodspawnTypes.put(p, "birchspawn");
               } else if (bs.getValue(BlockSapling.TYPE) == EnumType.JUNGLE) {
                  building.getResManager().woodspawnTypes.put(p, "junglespawn");
               } else if (bs.getValue(BlockSapling.TYPE) == EnumType.ACACIA) {
                  building.getResManager().woodspawnTypes.put(p, "acaciaspawn");
               } else if (bs.getValue(BlockSapling.TYPE) == EnumType.DARK_OAK) {
                  building.getResManager().woodspawnTypes.put(p, "darkoakspawn");
               }
            } else if (bs.getBlock() == MillBlocks.SAPLING_APPLETREE) {
               building.getResManager().woodspawnTypes.put(p, "appletreespawn");
            } else if (bs.getBlock() == MillBlocks.SAPLING_OLIVETREE) {
               building.getResManager().woodspawnTypes.put(p, "olivetreespawn");
            } else if (bs.getBlock() == MillBlocks.SAPLING_PISTACHIO) {
               building.getResManager().woodspawnTypes.put(p, "pistachiotreespawn");
            } else if (bs.getBlock() == MillBlocks.SAPLING_CHERRY) {
               building.getResManager().woodspawnTypes.put(p, "cherrytreespawn");
            } else if (bs.getBlock() == MillBlocks.SAPLING_SAKURA) {
               building.getResManager().woodspawnTypes.put(p, "sakuratreespawn");
            }
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.STALL)) {
         building.getResManager().stalls.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.STALL)) {
            px.setBlock(building.world, Blocks.AIR, 0, true, false);
            building.getResManager().stalls.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.MINING)) {
         building.getResManager().sources.clear();
         building.getResManager().sourceTypes.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.MINING)) {
            building.getResManager().addSourcePoint(px.getBlockActualState(building.world), px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.FURNACE)) {
         building.getResManager().furnaces.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.FURNACE)) {
            building.getResManager().furnaces.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.FIRE_PIT)) {
         building.getResManager().firepits.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.FIRE_PIT)) {
            building.getResManager().firepits.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.MUDBRICK)) {
         building.getResManager().brickspot.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.MUDBRICK)) {
            building.getResManager().brickspot.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.SUGAR)) {
         building.getResManager().sugarcanesoils.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.SUGAR)) {
            building.getResManager().sugarcanesoils.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.FISHING)) {
         building.getResManager().fishingspots.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.FISHING)) {
            px.setBlock(building.world, Blocks.AIR, 0, true, false);
            building.getResManager().fishingspots.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.SILK)) {
         building.getResManager().silkwormblock.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.SILK)) {
            px.setBlock(building.world, MillBlocks.SILK_WORM, 0, true, false);
            building.getResManager().silkwormblock.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.SNAILS)) {
         building.getResManager().snailsoilblock.clear();

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.SNAILS)) {
            px.setBlock(building.world, MillBlocks.SNAIL_SOIL, 0, true, false);
            building.getResManager().snailsoilblock.add(px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.SQUID)) {
         int squidSpawnPos = -1;

         for (int i = 0; i < building.getResManager().spawnTypes.size(); i++) {
            if (building.getResManager().spawnTypes.get(i).equals(new ResourceLocation("Squid"))) {
               squidSpawnPos = i;
            }
         }

         if (squidSpawnPos > -1) {
            building.getResManager().spawns.get(squidSpawnPos).clear();
         }

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.SQUID)) {
            px.setBlock(building.world, Blocks.WATER, 0, true, false);
            building.getResManager().addSpawnPoint(new ResourceLocation("Squid"), px);
         }
      }

      if (resources.containsKey(BuildingCustomPlan.TypeRes.CACAO)) {
         int cocoaSoilPos = -1;

         for (int ix = 0; ix < building.getResManager().soilTypes.size(); ix++) {
            if (building.getResManager().soilTypes.get(ix).equals(Mill.CROP_CACAO)) {
               cocoaSoilPos = ix;
            }
         }

         if (cocoaSoilPos > -1) {
            building.getResManager().soils.get(cocoaSoilPos).clear();
         }

         for (Point px : resources.get(BuildingCustomPlan.TypeRes.CACAO)) {
            building.getResManager().addSoilPoint(Mill.CROP_CACAO, px);
         }
      }

      this.updateTags(building);
   }

   private void registerSigns(Building building, Map<BuildingCustomPlan.TypeRes, List<Point>> resources) {
      building.getResManager().signs.clear();
      Map<Integer, Point> signsWithPos = new HashMap<>();
      List<Point> otherSigns = new ArrayList<>();
      if (resources.containsKey(BuildingCustomPlan.TypeRes.SIGN)) {
         for (Point signP : resources.get(BuildingCustomPlan.TypeRes.SIGN)) {
            TileEntitySign signEntity = signP.getSign(building.world);
            int signPos = -1;
            if (signEntity != null) {
               try {
                  signPos = Integer.parseInt(signEntity.signText[0].getUnformattedText()) - 1;
               } catch (Exception var10) {
               }
            }

            if (signPos > -1 && !signsWithPos.containsKey(signPos)) {
               signsWithPos.put(signPos, signP);
            } else {
               otherSigns.add(signP);
            }

            if (signP.getBlock(building.world).equals(Blocks.WALL_SIGN)) {
               int meta = signP.getMeta(building.world);
               signP.setBlock(building.world, MillBlocks.PANEL, meta, true, false);
            }
         }
      }

      int signNumber = signsWithPos.size() + otherSigns.size();

      for (int i = 0; i < signNumber; i++) {
         building.getResManager().signs.add(null);
      }

      for (Integer pos : signsWithPos.keySet()) {
         if (pos < signNumber) {
            building.getResManager().signs.set(pos, signsWithPos.get(pos));
         } else {
            otherSigns.add(signsWithPos.get(pos));
         }
      }

      int posInOthers = 0;

      for (int i = 0; i < signNumber; i++) {
         if (building.getResManager().signs.get(i) == null) {
            building.getResManager().signs.set(i, otherSigns.get(posInOthers));
            posInOthers++;
         }
      }
   }

   @Override
   public String toString() {
      return "custom:" + this.buildingKey + ":" + this.culture.key;
   }

   private void updateTags(Building building) {
      if (!this.tags.isEmpty()) {
         building.addTags(this.tags, this.buildingKey + ": registering new tags");
         if (MillConfigValues.LogTags >= 2) {
            MillLog.minor(
               this,
               "Applying tags: "
                  + this.tags.stream().collect(Collectors.joining(", "))
                  + ", result: "
                  + building.getTags().stream().collect(Collectors.joining(", "))
            );
         }
      }
   }

   public static enum TypeRes {
      CHEST("chest"),
      CRAFT("craft"),
      SIGN("sign"),
      FIELD("field"),
      SPAWN("spawn"),
      SAPLING("sapling"),
      STALL("stall"),
      MINING("mining"),
      FURNACE("furnace"),
      FIRE_PIT("fire_pit"),
      MUDBRICK("mudbrick"),
      SUGAR("sugar"),
      FISHING("fishing"),
      SILK("silk"),
      SNAILS("snails"),
      SQUID("squid"),
      CACAO("cacao");

      public final String key;

      private TypeRes(String key) {
         this.key = key;
      }
   }
}
