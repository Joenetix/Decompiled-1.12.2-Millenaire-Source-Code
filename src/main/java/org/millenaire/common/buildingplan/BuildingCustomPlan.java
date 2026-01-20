package org.millenaire.common.buildingplan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.core.MillBlocks;

/**
 * Custom building plans created by players using in-game blocks.
 * Scans a radius around a central point for specific block types (chests,
 * signs, etc.)
 * to define building functions.
 * Ported from 1.12.2 to 1.20.1.
 * 
 * NOTE: Full functionality requires complete Building and Culture class
 * implementations.
 * Some methods are stubbed pending full port.
 */
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
    public Map<TypeRes, Integer> minResources = new HashMap<>();
    public Map<TypeRes, Integer> maxResources = new HashMap<>();

    /**
     * Load all custom buildings from a culture's custombuildings directory.
     */
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
            } catch (Exception e) {
                MillLog.printException("Error when loading " + file.getAbsolutePath(), e);
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
        reader.close();

        if (MillConfigValues.LogBuildingPlan >= 1) {
            MillLog.major(this, "Loaded custom building " + this.buildingKey + " " + this.nativeName
                    + " pop: " + this.maleResident + "/" + this.femaleResident);
        }

        if (!this.minResources.containsKey(TypeRes.SIGN)) {
            MillLog.error(this, "No signs in custom building.");
        }
    }

    /**
     * Find resources (chests, signs, etc.) within radius of the given position.
     */
    public Map<TypeRes, List<Point>> findResources(Level world, Point pos) {
        Map<TypeRes, List<Point>> resources = new HashMap<>();

        for (int currentRadius = 0; currentRadius < this.radius; currentRadius++) {
            for (int y = pos.getiY() - this.heightRadius + 1; y < pos.getiY() + this.heightRadius + 1; y++) {
                // Scan the perimeter of a square at this radius
                for (int x = pos.getiX() - currentRadius; x <= pos.getiX() + currentRadius; x++) {
                    handlePoint(x, y, pos.getiZ() - currentRadius, world, resources);
                    handlePoint(x, y, pos.getiZ() + currentRadius, world, resources);
                }
                for (int z = pos.getiZ() - currentRadius + 1; z <= pos.getiZ() + currentRadius - 1; z++) {
                    handlePoint(pos.getiX() - currentRadius, y, z, world, resources);
                    handlePoint(pos.getiX() + currentRadius, y, z, world, resources);
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
        String translated = getNameTranslated();
        if (translated != null && !translated.isEmpty()) {
            name = name + " (" + translated + ")";
        }
        return name;
    }

    @Override
    public List<String> getMaleResident() {
        return this.maleResident;
    }

    @Override
    public String getNameTranslated() {
        // TODO: Implement when Culture.canReadBuildingNames() is available
        return "";
    }

    @Override
    public String getNativeName() {
        return this.nativeName;
    }

    @Override
    public List<String> getVisitors() {
        return this.visitors;
    }

    /**
     * Handle a point during resource scanning.
     */
    private void handlePoint(int x, int y, int z, Level world, Map<TypeRes, List<Point>> resources) {
        Point p = new Point(x, y, z);
        TypeRes res = identifyRes(world, p);

        if (res != null && maxResources.containsKey(res)) {
            if (resources.containsKey(res)) {
                if (resources.get(res).size() < maxResources.get(res)) {
                    resources.get(res).add(p);
                }
            } else {
                List<Point> points = new ArrayList<>();
                points.add(p);
                resources.put(res, points);
            }
        }
    }

    /**
     * Identify what type of resource is at the given point.
     */
    private TypeRes identifyRes(Level world, Point p) {
        Block b = p.getBlock(world);

        if (b == Blocks.CHEST || b == MillBlocks.LOCKED_CHEST.get()) {
            return TypeRes.CHEST;
        } else if (b == Blocks.CRAFTING_TABLE) {
            return TypeRes.CRAFT;
        } else if (b == Blocks.OAK_WALL_SIGN || b == Blocks.OAK_SIGN ||
                b == MillBlocks.PANEL.get()) {
            return TypeRes.SIGN;
        } else if (b == Blocks.FARMLAND) {
            return TypeRes.FIELD;
        } else if (b == Blocks.HAY_BLOCK) {
            return TypeRes.SPAWN;
        } else if (isSapling(b)) {
            return TypeRes.SAPLING;
        } else if (b == Blocks.FURNACE) {
            return TypeRes.FURNACE;
        } else if (b == MillBlocks.FIRE_PIT.get()) {
            return TypeRes.FIRE_PIT;
        } else if (b == MillBlocks.WET_BRICK.get()) {
            return TypeRes.MUDBRICK;
        } else if (b == Blocks.SUGAR_CANE) {
            return TypeRes.SUGAR;
        } else if (b == Blocks.COCOA) {
            return TypeRes.CACAO;
        }

        return null;
    }

    private boolean isSapling(Block b) {
        return b == Blocks.OAK_SAPLING || b == Blocks.SPRUCE_SAPLING ||
                b == Blocks.BIRCH_SAPLING || b == Blocks.JUNGLE_SAPLING ||
                b == Blocks.ACACIA_SAPLING || b == Blocks.DARK_OAK_SAPLING ||
                b == Blocks.CHERRY_SAPLING;
    }

    /**
     * Parse a config line from the custom building file.
     */
    private void readConfigLine(String line) {
        if (line == null)
            return;

        String[] configs = line.split(";", -1);

        for (String config : configs) {
            String[] parts = config.split(":");
            if (parts.length == 2) {
                String key = parts[0].toLowerCase();
                String value = parts[1];

                switch (key) {
                    case "moveinpriority" -> this.priorityMoveIn = Integer.parseInt(value);
                    case "radius" -> this.radius = Integer.parseInt(value);
                    case "heightradius" -> this.heightRadius = Integer.parseInt(value);
                    case "native" -> this.nativeName = value;
                    case "gamenamekey" -> this.gameNameKey = value;
                    case "croptype" -> this.cropType = new ResourceLocation(value);
                    case "spawntype" -> this.spawnType = new ResourceLocation(value);
                    case "male" -> this.maleResident.add(value.toLowerCase());
                    case "female" -> this.femaleResident.add(value.toLowerCase());
                    case "visitor" -> this.visitors.add(value.toLowerCase());
                    case "shop" -> this.shop = value;
                    case "tag" -> this.tags.add(value.toLowerCase());
                    default -> {
                        if (key.startsWith("name_")) {
                            this.names.put(key, value);
                        } else {
                            // Try to parse as TypeRes
                            for (TypeRes typeRes : TypeRes.values()) {
                                if (typeRes.key.equals(key)) {
                                    try {
                                        if (value.contains("-")) {
                                            this.minResources.put(typeRes, Integer.parseInt(value.split("-")[0]));
                                            this.maxResources.put(typeRes, Integer.parseInt(value.split("-")[1]));
                                        } else {
                                            int val = Integer.parseInt(value);
                                            this.minResources.put(typeRes, val);
                                            this.maxResources.put(typeRes, val);
                                        }
                                    } catch (Exception e) {
                                        MillLog.printException("Exception parsing res " + typeRes.key, e);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Register resources for a custom building.
     * Full port from 1.12.2 registerResources().
     */
    public void registerResources(org.millenaire.common.village.Building building,
            BuildingLocation location) {
        if (building == null || location == null || building.getResManager() == null)
            return;

        Map<TypeRes, List<Point>> resources = findResources(building.world, location.pos);
        org.millenaire.common.village.BuildingResManager resManager = building.getResManager();

        // Set sleeping position
        resManager.setSleepingPos(location.pos.getAbove().getBlockPos());
        location.sleepingPos = location.pos.getAbove();

        // Chests
        if (resources.containsKey(TypeRes.CHEST)) {
            resManager.chests.clear();
            for (Point chestP : resources.get(TypeRes.CHEST)) {
                // Convert vanilla chest to locked chest
                if (chestP.getBlock(building.world) == Blocks.CHEST) {
                    org.millenaire.utilities.WorldUtilities.setBlockState(building.world, chestP,
                            MillBlocks.LOCKED_CHEST.get().defaultBlockState(), true, false);
                }
                resManager.chests.add(chestP.getBlockPos());
            }
        }

        // Crafting table
        if (resources.containsKey(TypeRes.CRAFT) && resources.get(TypeRes.CRAFT).size() > 0) {
            location.craftingPos = resources.get(TypeRes.CRAFT).get(0);
            resManager.setCraftingPos(resources.get(TypeRes.CRAFT).get(0).getBlockPos());
        }

        // Signs - register them
        if (resources.containsKey(TypeRes.SIGN)) {
            resManager.signs.clear();
            for (Point signP : resources.get(TypeRes.SIGN)) {
                resManager.signs.add(signP.getBlockPos());
                // Convert vanilla sign to panel
                Block b = signP.getBlock(building.world);
                if (b == Blocks.OAK_WALL_SIGN || b == Blocks.OAK_SIGN) {
                    org.millenaire.utilities.WorldUtilities.setBlockState(building.world, signP,
                            MillBlocks.PANEL.get().defaultBlockState(), true, false);
                }
            }
        }

        // Crop fields
        if (cropType != null && resources.containsKey(TypeRes.FIELD)) {
            resManager.soils.clear();
            resManager.soilTypes.clear();
            for (Point p : resources.get(TypeRes.FIELD)) {
                resManager.addSoilPoint(cropType, p.getBlockPos());
            }
        }

        // Animal spawns
        if (spawnType != null && resources.containsKey(TypeRes.SPAWN)) {
            resManager.spawns.clear();
            resManager.spawnTypes.clear();
            for (Point p : resources.get(TypeRes.SPAWN)) {
                // Clear the hay block
                org.millenaire.utilities.WorldUtilities.setBlockState(building.world, p, Blocks.AIR.defaultBlockState(),
                        true, false);
                resManager.addSpawnPoint(spawnType, p.getBlockPos());
            }
        }

        // Tree saplings
        if (resources.containsKey(TypeRes.SAPLING)) {
            resManager.woodspawn.clear();
            resManager.woodspawnTypes.clear();
            for (Point p : resources.get(TypeRes.SAPLING)) {
                resManager.woodspawn.add(p.getBlockPos());
                Block b = p.getBlock(building.world);
                if (b == Blocks.OAK_SAPLING)
                    resManager.woodspawnTypes.put(p.getBlockPos(), "oak");
                else if (b == Blocks.SPRUCE_SAPLING)
                    resManager.woodspawnTypes.put(p.getBlockPos(), "spruce");
                else if (b == Blocks.BIRCH_SAPLING)
                    resManager.woodspawnTypes.put(p.getBlockPos(), "birch");
                else if (b == Blocks.JUNGLE_SAPLING)
                    resManager.woodspawnTypes.put(p.getBlockPos(), "jungle");
                else if (b == Blocks.ACACIA_SAPLING)
                    resManager.woodspawnTypes.put(p.getBlockPos(), "acacia");
                else if (b == Blocks.DARK_OAK_SAPLING)
                    resManager.woodspawnTypes.put(p.getBlockPos(), "dark_oak");
                else if (b == Blocks.CHERRY_SAPLING)
                    resManager.woodspawnTypes.put(p.getBlockPos(), "cherry");
            }
        }

        // Stalls
        if (resources.containsKey(TypeRes.STALL)) {
            resManager.stalls.clear();
            for (Point p : resources.get(TypeRes.STALL)) {
                org.millenaire.utilities.WorldUtilities.setBlockState(building.world, p, Blocks.AIR.defaultBlockState(),
                        true, false);
                resManager.stalls.add(p.getBlockPos());
            }
        }

        // Mining sources
        if (resources.containsKey(TypeRes.MINING)) {
            resManager.sources.clear();
            resManager.sourceTypes.clear();
            for (Point p : resources.get(TypeRes.MINING)) {
                resManager.addSourcePoint(org.millenaire.utilities.WorldUtilities.getBlockState(building.world, p),
                        p.getBlockPos());
            }
        }

        // Furnaces
        if (resources.containsKey(TypeRes.FURNACE)) {
            resManager.furnaces.clear();
            for (Point p : resources.get(TypeRes.FURNACE)) {
                resManager.furnaces.add(p.getBlockPos());
            }
        }

        // Fire pits
        if (resources.containsKey(TypeRes.FIRE_PIT)) {
            resManager.firepits.clear();
            for (Point p : resources.get(TypeRes.FIRE_PIT)) {
                resManager.firepits.add(p.getBlockPos());
            }
        }

        // Mud bricks
        if (resources.containsKey(TypeRes.MUDBRICK)) {
            resManager.brickspot.clear();
            for (Point p : resources.get(TypeRes.MUDBRICK)) {
                resManager.brickspot.add(p.getBlockPos());
            }
        }

        // Sugar cane
        if (resources.containsKey(TypeRes.SUGAR)) {
            resManager.sugarcanesoils.clear();
            for (Point p : resources.get(TypeRes.SUGAR)) {
                resManager.sugarcanesoils.add(p.getBlockPos());
            }
        }

        // Fishing
        if (resources.containsKey(TypeRes.FISHING)) {
            resManager.fishingspots.clear();
            for (Point p : resources.get(TypeRes.FISHING)) {
                org.millenaire.utilities.WorldUtilities.setBlockState(building.world, p, Blocks.AIR.defaultBlockState(),
                        true, false);
                resManager.fishingspots.add(p.getBlockPos());
            }
        }

        // Cacao
        if (resources.containsKey(TypeRes.CACAO)) {
            for (Point p : resources.get(TypeRes.CACAO)) {
                resManager.addSoilPoint(new ResourceLocation("minecraft", "cocoa_beans"), p.getBlockPos());
            }
        }

        updateTags(building);
    }

    /**
     * Update tags on building.
     */
    private void updateTags(org.millenaire.common.village.Building building) {
        if (building == null || tags.isEmpty())
            return;

        building.addTags(tags, buildingKey + ": registering new tags");
        if (MillConfigValues.LogBuildingPlan >= 2) {
            MillLog.minor(this, "Applying tags: " + String.join(", ", tags)
                    + ", result: " + String.join(", ", building.getTags()));
        }
    }

    @Override
    public String toString() {
        return "custom:" + this.buildingKey + ":" + (this.culture != null ? this.culture.getId() : "null");
    }

    /**
     * Resource types that can be found in custom buildings.
     */
    public enum TypeRes {
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

        TypeRes(String key) {
            this.key = key;
        }
    }
}
