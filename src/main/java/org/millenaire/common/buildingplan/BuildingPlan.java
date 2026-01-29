package org.millenaire.common.buildingplan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.pathing.atomicstryker.RegionMapper;
import org.millenaire.common.village.VillageMapInfo;

/**
 * Represents a building plan loaded from PNG files.
 * This is the core class for the building system.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BuildingPlan implements IBuildingPlan, MillCommonUtilities.WeightedChoice {
    // Constants
    private static final int LARGE_BUILDING_FLOOR_SIZE = 2500;
    public static final int NORTH_FACING = 0;
    public static final int WEST_FACING = 1;
    public static final int SOUTH_FACING = 2;
    public static final int EAST_FACING = 3;
    public static final String[] FACING_KEYS = new String[] { "north", "west", "south", "east" };
    private static final short PART_SIZE = 8;

    // Geometry fields
    public int length;
    public int width;
    public int areaToClear = 5;
    public int areaToClearLengthBefore = -1;
    public int areaToClearLengthAfter = -1;
    public int areaToClearWidthBefore = -1;
    public int areaToClearWidthAfter = -1;
    public int buildingOrientation = 1;
    public int altitudeOffset = 0;
    public int foundationDepth = 10;
    public int fixedOrientation = -1;

    // Display fields
    private InvItem icon = null;
    public String travelBookCategory = null;
    public boolean travelBookDisplay = true;
    public String nativeName = null;
    public HashMap<String, String> translatedNames = new HashMap<>();
    public boolean showTownHallSigns = true;

    // Sub-building fields
    public List<String> startingSubBuildings = new ArrayList<>();
    public boolean isSubBuilding = false;
    public String parentBuildingPlan = null;
    public List<String> subBuildings = new ArrayList<>();

    // Building configuration
    public int version = 0;
    public int max = 1;
    public float minDistance = 0.0f;
    public float maxDistance = 1.0f;
    public Map<String, Integer> farFromTag = new HashMap<>();
    public Map<String, Integer> closeToTag = new HashMap<>();
    public int reputation = 0;
    public int price = 0;
    public boolean isgift = false;
    public boolean isWallSegment = false;
    public boolean isBorderBuilding = false;
    public int weight = 1;

    // Random brick colours (DyeColor -> (DyeColor -> weight))
    public Map<DyeColor, Map<DyeColor, Integer>> randomBrickColours = new HashMap<>();

    // Upgrade fields
    public int startLevel = 0;
    public int priority = 1;
    public List<String> maleResident = new ArrayList<>();
    public List<String> femaleResident = new ArrayList<>();
    public List<String> visitors = new ArrayList<>();
    public int priorityMoveIn = 10;
    public int pathLevel = 0;
    public int pathWidth = 2;
    public boolean rebuildPath = false;
    public boolean noPathsToBuilding = false;

    // Tags
    public String exploreTag = null;
    public String requiredGlobalTag = null;
    public List<String> tags = new ArrayList<>();
    public List<String> clearTags = new ArrayList<>();
    public List<String> forbiddenTagsInVillage = new ArrayList<>();
    public List<String> requiredTags = new ArrayList<>();
    public List<String> villageTags = new ArrayList<>();
    public List<String> requiredVillageTags = new ArrayList<>();
    public List<String> parentTags = new ArrayList<>();
    public List<String> requiredParentTags = new ArrayList<>();

    // Economy
    public int irrigation = 0;
    public int extraSimultaneousConstructions = 0;
    public int extraSimultaneousWallConstructions = 0;
    public String shop = null;
    public int[] signOrder = new int[] { 0 };
    public Map<InvItem, Integer> abstractedProduction = new HashMap<>();
    public List<StartingGood> startingGoods = new ArrayList<>();

    // Runtime fields
    public int nbFloors;
    public int lengthOffset;
    public int widthOffset;
    public boolean isUpdate = false;
    public int level;
    public PointType[][][] plan = null; // Re-enabled for special point type scanning
    public BlockState[][][] blocks;
    public String planName = "";
    public String buildingKey;
    public HashMap<InvItem, Integer> resCost = new HashMap<>();
    public int variation;
    public Culture culture;
    public BuildingPlan parent;
    private File loadedFromFile = null;

    // Fields from Record
    public BlockPos startPos;
    public BlockPos pathStartPos;
    public String sourceFile;
    public int nativeLevel;

    // Record-style accessors for compatibility
    public BlockState[][][] blocks() {
        return blocks;
    }

    public int width() {
        return width;
    }

    public int length() {
        return length;
    }

    public int nbFloors() {
        return nbFloors;
    }

    public BlockPos startPos() {
        return startPos;
    }

    public String sourceFile() {
        return sourceFile;
    }

    public int nativeLevel() {
        return nativeLevel;
    }

    public int buildingOrientation() {
        return buildingOrientation;
    }

    public int fixedOrientation() {
        return fixedOrientation;
    }

    public BlockPos pathStartPos() {
        return pathStartPos;
    }

    public int areaToClearLengthBefore() {
        return areaToClearLengthBefore;
    }

    public int areaToClearLengthAfter() {
        return areaToClearLengthAfter;
    }

    public int areaToClearWidthBefore() {
        return areaToClearWidthBefore;
    }

    public int areaToClearWidthAfter() {
        return areaToClearWidthAfter;
    }

    public int foundationDepth() {
        return foundationDepth;
    }

    public int altitudeOffset() {
        return altitudeOffset;
    }

    public float minDistance() {
        return minDistance;
    }

    public float maxDistance() {
        return maxDistance;
    }

    // Constructors from Record
    // Constructor used by loadPlan (20 args)
    public BuildingPlan(BlockState[][][] blocks, int width, int length, int nbFloors, BlockPos startPos,
            String sourceFile, int nativeLevel, int buildingOrientation, int fixedOrientation,
            BlockPos pathStartPos,
            int areaToClearLengthBefore, int areaToClearLengthAfter, int areaToClearWidthBefore,
            int areaToClearWidthAfter,
            int foundationDepth, int altitudeOffset, float minDistance, float maxDistance,
            java.util.Map<String, Integer> farFromTag, java.util.Map<String, Integer> closeToTag) {
        this.blocks = blocks;
        this.width = width;
        this.length = length;
        this.nbFloors = nbFloors;
        this.startPos = startPos;
        this.sourceFile = sourceFile;
        this.nativeLevel = nativeLevel;
        this.buildingOrientation = buildingOrientation;
        this.fixedOrientation = fixedOrientation;
        this.pathStartPos = pathStartPos;
        this.areaToClearLengthBefore = areaToClearLengthBefore;
        this.areaToClearLengthAfter = areaToClearLengthAfter;
        this.areaToClearWidthBefore = areaToClearWidthBefore;
        this.areaToClearWidthAfter = areaToClearWidthAfter;
        this.foundationDepth = foundationDepth;
        this.altitudeOffset = altitudeOffset;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        if (farFromTag != null)
            this.farFromTag.putAll(farFromTag);
        if (closeToTag != null)
            this.closeToTag.putAll(closeToTag);
    }

    // Constructor used by loadMultiLevelPlan and parseSingleImage (10 args)
    public BuildingPlan(BlockState[][][] blocks, int width, int length, int nbFloors, BlockPos startPos,
            String sourceFile, int nativeLevel, int buildingOrientation, int fixedOrientation,
            BlockPos pathStartPos) {
        this(blocks, width, length, nbFloors, startPos, sourceFile, nativeLevel, buildingOrientation,
                fixedOrientation, pathStartPos,
                5, 5, 5, 5, 10, 0, 0.0f, 1.0f,
                null, null);
    }

    // Basic constructor with defaults for all new fields
    public BuildingPlan(BlockState[][][] blocks, int width, int length, int nbFloors, BlockPos startPos,
            String sourceFile) {
        this(blocks, width, length, nbFloors, startPos, sourceFile, 0, 0, -1, null);
    }

    // Legacy parser constructor
    public BuildingPlan(BlockState[][][] blocks, int width, int length, int nbFloors, BlockPos startPos,
            String sourceFile, int nativeLevel, int buildingOrientation) {
        this(blocks, width, length, nbFloors, startPos, sourceFile, nativeLevel, buildingOrientation, -1, null);
    }

    /**
     * Starting good for building chest loot.
     */
    public static class StartingGood {
        public final InvItem item;
        public final double chance;
        public final int minCount;
        public final int maxCount;

        public StartingGood(InvItem item, double chance, int minCount, int maxCount) {
            this.item = item;
            this.chance = chance;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }

    /**
     * Pair of location and building plan.
     */
    public static class LocationBuildingPair {
        public final BuildingLocation location;
        public final BuildingPlan plan;

        public LocationBuildingPair(BuildingLocation location, BuildingPlan plan) {
            this.location = location;
            this.plan = plan;
        }
    }

    // Constructors
    public BuildingPlan() {
    }

    public BuildingPlan(String buildingKey, int level, int variation, Culture c) {
        this.buildingKey = buildingKey;
        this.isUpdate = level > 0;
        this.level = level;
        this.variation = variation;
        this.culture = c;
    }

    // Static methods
    public static Point adjustForOrientation(int x, int y, int z, int xoffset, int zoffset, int orientation) {
        Point pos = null;
        if (orientation == 0) {
            pos = new Point(x + xoffset, y, z + zoffset);
        } else if (orientation == 1) {
            pos = new Point(x + zoffset, y, z - xoffset - 1);
        } else if (orientation == 2) {
            pos = new Point(x - xoffset - 1, y, z - zoffset - 1);
        } else if (orientation == 3) {
            pos = new Point(x - zoffset - 1, y, z + xoffset);
        }
        return pos;
    }

    public static int computeOrientation(Point buildingPos, Point facingPos) {
        int relx = buildingPos.getiX() - facingPos.getiX();
        int relz = buildingPos.getiZ() - facingPos.getiZ();
        if (relx * relx > relz * relz) {
            return relx > 0 ? 0 : 2;
        } else {
            return relz > 0 ? 3 : 1;
        }
    }

    static String getColourString(int colour) {
        return ((colour & 0xFF0000) >> 16) + "/" + ((colour & 0xFF00) >> 8) + "/" + ((colour & 0xFF) >> 0) + "/"
                + Integer.toHexString(colour);
    }

    /**
     * Load building points from blocklist.txt files.
     */
    public static boolean loadBuildingPoints() {
        // TODO: Implement loading from Mill.loadingDirs
        return false;
    }

    /**
     * Load plans for a culture.
     */
    public static HashMap<String, BuildingPlanSet> loadPlans(VirtualDir cultureVirtualDir, Culture culture) {
        HashMap<String, BuildingPlanSet> plans = new HashMap<>();

        if (cultureVirtualDir == null) {
            return plans;
        }

        VirtualDir buildingsVirtualDir = cultureVirtualDir.getChildDirectory("buildings");
        if (buildingsVirtualDir == null) {
            return plans;
        }

        BuildingFileFiler pictPlans = new BuildingFileFiler("_A.txt");

        for (File file : buildingsVirtualDir.listFilesRecursive(pictPlans)) {
            try {
                if (MillConfigValues.LogBuildingPlan >= 1) {
                    MillLog.major(file, "Loading pict building: " + file.getAbsolutePath());
                }

                BuildingPlanSet set = new BuildingPlanSet(
                        culture,
                        file.getName().substring(0, file.getName().length() - 6),
                        buildingsVirtualDir,
                        file);
                set.loadPictPlans(false);

                if (file.getParentFile().getName().startsWith("lone")) {
                    set.max = 0;
                }

                plans.put(set.key, set);
            } catch (Exception e) {
                MillLog.printException(
                        "Exception when loading " + file.getName() + " plan set in culture " + culture.key + ":", e);
            }
        }

        validatePlans(plans);
        return plans;
    }

    /**
     * Validate loaded plans for consistency.
     */
    private static void validatePlans(HashMap<String, BuildingPlanSet> plans) {
        for (BuildingPlanSet planSet : plans.values()) {
            BuildingPlan firstPlan = planSet.getFirstStartingPlan();
            if (firstPlan == null)
                continue;

            if (firstPlan.isSubBuilding) {
                if (firstPlan.parentBuildingPlan == null) {
                    MillLog.warning(planSet,
                            "This plan is a sub-building but has no referenced parent plan.");
                } else {
                    // Validate parent exists
                    String parentKey = firstPlan.parentBuildingPlan;
                    String[] parts = parentKey.split("_");
                    if (parts.length >= 2) {
                        String suffix = parts[parts.length - 1].toUpperCase();
                        int parentVariation = suffix.charAt(0) - 'A';
                        int parentLevel = 0;
                        try {
                            parentLevel = Integer.parseInt(suffix.substring(1));
                        } catch (NumberFormatException e) {
                            // Ignore
                        }
                        String parentBuildingKey = parentKey.substring(0, parentKey.length() - suffix.length() - 1);

                        if (!plans.containsKey(parentBuildingKey)) {
                            MillLog.warning(planSet, "Unknown parent building plan: " + parentKey);
                        } else {
                            BuildingPlan parentPlan = plans.get(parentBuildingKey).getPlan(parentVariation,
                                    parentLevel);
                            if (parentPlan == null) {
                                MillLog.warning(planSet, "Unknown level or upgrade for parent: " + parentKey);
                            } else if (parentPlan.length != firstPlan.length || parentPlan.width != firstPlan.width) {
                                MillLog.warning(planSet, "Dimensions of parent don't match sub-building.");
                            }
                        }
                    }
                }
            }

            // Validate sub-buildings exist
            for (int variation = 0; variation < planSet.plans.size(); variation++) {
                Set<String> invalid = new HashSet<>();
                BuildingPlan[] varPlans = planSet.plans.get(variation);

                for (BuildingPlan plan : varPlans) {
                    for (String subKey : plan.subBuildings) {
                        if (!plans.containsKey(subKey)) {
                            invalid.add(subKey);
                        }
                    }
                    for (String subKey : plan.startingSubBuildings) {
                        if (!plans.containsKey(subKey)) {
                            invalid.add(subKey);
                        }
                    }
                }

                for (String key : invalid) {
                    MillLog.error(planSet, "Unknown sub-building: " + key);
                }

                if (!invalid.isEmpty()) {
                    for (BuildingPlan plan : varPlans) {
                        plan.subBuildings.removeAll(invalid);
                        plan.startingSubBuildings.removeAll(invalid);
                    }
                }
            }
        }
    }

    // Cost calculation methods
    public void addToAnyWoodCost(int nb) {
        addToCost(Blocks.OAK_LOG, -1, nb);
    }

    public void addToCost(Block block, int nb) {
        addToCost(block, 0, nb);
    }

    public void addToCost(Block block, int meta, int nb) {
        try {
            InvItem key = InvItem.createInvItem(block.defaultBlockState());
            if (this.resCost.containsKey(key)) {
                nb += this.resCost.get(key);
            }
            this.resCost.put(key, nb);
        } catch (Exception e) {
            MillLog.printException("Exception when calculating cost of: " + this, e);
        }
    }

    public void addToCost(BlockState blockState, int nb) {
        addToCost(blockState.getBlock(), 0, nb);
    }

    public void addToCost(InvItem invitem, int nb) {
        try {
            if (this.resCost.containsKey(invitem)) {
                nb += this.resCost.get(invitem);
            }
            this.resCost.put(invitem, nb);
        } catch (Exception e) {
            MillLog.printException("Exception when calculating cost of: " + this, e);
        }
    }

    public void addToCost(Item item, int nb) {
        try {
            InvItem key = InvItem.createInvItem(item, 0);
            if (this.resCost.containsKey(key)) {
                nb += this.resCost.get(key);
            }
            this.resCost.put(key, nb);
        } catch (Exception e) {
            MillLog.printException("Exception when calculating cost of: " + this, e);
        }
    }

    // Direction adjustment for orientation
    public Direction adjustFacingForOrientation(Direction facing, int orientation) {
        if (facing == Direction.DOWN || facing == Direction.UP) {
            return facing;
        }
        for (int i = 0; i < orientation; i++) {
            facing = facing.getClockWise();
        }
        return facing;
    }

    /**
     * Build this plan at the given location.
     * Full port from 1.12.2 BuildingPlan.build()
     * 
     * @param mw                   The world data
     * @param villageType          The village type
     * @param location             The building location
     * @param villageGeneration    Whether this is during world generation
     * @param isBuildingTownHall   Whether this is the town hall
     * @param townHall             The town hall building (can be null)
     * @param wandimport           Whether this is an import from wand
     * @param includeSpecialPoints Whether to include special points
     * @param owner                The player owner
     * @param rushBuilding         Whether to rush building
     * @return List of location-building pairs created
     */
    public List<LocationBuildingPair> build(
            org.millenaire.common.world.MillWorldData mw,
            org.millenaire.common.culture.VillageType villageType,
            BuildingLocation location,
            boolean villageGeneration,
            boolean isBuildingTownHall,
            org.millenaire.common.village.Building townHall,
            boolean wandimport,
            boolean includeSpecialPoints,
            Player owner,
            boolean rushBuilding) {

        if (!isBuildingTownHall && townHall == null && !wandimport) {
            MillLog.error(this, "Building is not TH and does not have TH's position.");
        }

        net.minecraft.world.level.Level world = mw.world;
        List<LocationBuildingPair> buildings = new ArrayList<>();
        boolean[][] laySnow = null;

        if (villageGeneration || wandimport) {
            laySnow = this.checkForSnow(world, location);
        }

        boolean initialBuild = location.level == 0 && !location.isSubBuildingLocation;
        boolean isLargeBuilding = initialBuild && this.width * this.length > LARGE_BUILDING_FLOOR_SIZE;

        if (isLargeBuilding) {
            org.millenaire.common.network.ServerSender.sendTranslatedSentenceInRange(
                    world, location.pos, Integer.MAX_VALUE, '4', "other.largebuildinggeneration", this.nativeName);
        }

        BuildingBlock[] bblocks = this.getBuildingPoints(world, location, villageGeneration, includeSpecialPoints,
                false);
        boolean measureTime = MillConfigValues.DEV && isLargeBuilding;
        long startTime = System.currentTimeMillis();

        if (measureTime) {
            MillLog.temp(this, "Starting build. Free memory: " + Runtime.getRuntime().freeMemory() / 1024L / 1024L);
        }

        for (BuildingBlock bblock : bblocks) {
            bblock.build(world, townHall, villageGeneration, wandimport);
        }

        if (measureTime) {
            MillLog.temp(this, "Finished building. Time passed: "
                    + Math.round((float) (System.currentTimeMillis() - startTime))
                    + "ms. Free memory: " + Runtime.getRuntime().freeMemory() / 1024L / 1024L);
        }

        if (this.containsTags("hof")) {
            this.fillHoFSigns(location, world);
        }

        if (laySnow != null) {
            this.setSnow(world, location, laySnow);
        }

        Point townHallPos = null;
        if (townHall != null) {
            net.minecraft.core.BlockPos thPos = townHall.getPos();
            townHallPos = new Point(thPos.getX(), thPos.getY(), thPos.getZ());
        }

        if (bblocks.length > 0 && !wandimport) {
            if (location.level == 0) {
                org.millenaire.common.village.Building building = new org.millenaire.common.village.Building(
                        mw, this.culture, villageType, location, isBuildingTownHall, villageGeneration, townHallPos);

                if (MillConfigValues.LogWorldGeneration >= 2) {
                    MillLog.minor(this, "Building " + this.planName + " at " + location);
                }

                this.referenceBuildingPoints(building);
                building.initialise(owner, villageGeneration || rushBuilding);
                building.fillStartingGoods();
                buildings.add(new LocationBuildingPair(location, this));
                building.updateBanners();

                if (isBuildingTownHall) {
                    net.minecraft.core.BlockPos bPos = building.getPos();
                    townHallPos = new Point(bPos.getX(), bPos.getY(), bPos.getZ());
                    townHall = building;
                }

                this.updateTags(building);
            } else {
                org.millenaire.common.village.Building buildingx = location.getBuilding(world);
                if (buildingx != null) {
                    this.updateBuildingForPlan(buildingx);
                }
                townHall = buildingx;
            }
        }

        if (bblocks.length > 0 && wandimport && location.level == 0) {
            this.displayPanelNumbers(world, location);
        }

        if (this.culture != null && !wandimport && location.level == 0) {
            for (String sb : this.startingSubBuildings) {
                boolean validSubBuilding = true;
                if (townHall != null) {
                    validSubBuilding = townHall.isValidProject(
                            new BuildingProject(this.culture.getBuildingPlanSet(sb), this));
                }

                if (validSubBuilding) {
                    BuildingPlan plan = this.culture.getBuildingPlanSet(sb).getRandomStartingPlan();
                    BuildingLocation l = location.createLocationForStartingSubBuilding(sb);
                    List<LocationBuildingPair> vb = plan.build(
                            mw, villageType, l, villageGeneration, false, townHall, false, false, owner, rushBuilding);
                    location.subBuildings.add(sb);

                    buildings.addAll(vb);
                } else {
                    MillLog.temp(this, "Cannot create starting subbuilding " + sb + " as it is invalid.");
                }
            }
        }

        if (villageGeneration || wandimport) {
            this.build_cleanup(location, world);
        }

        // Mark blocks for update in the building area
        world.blockUpdated(
                new BlockPos(
                        location.pos.getiX() - this.length / 2 - 5,
                        location.pos.getiY() - this.nbFloors - 5,
                        location.pos.getiZ() - this.width / 2 - 5),
                Blocks.AIR);

        return buildings;
    }

    /**
     * Cleanup after building - remove dropped items and animals stuck in walls.
     */
    private void build_cleanup(BuildingLocation location, net.minecraft.world.level.Level world) {
        int radius = Math.max(this.width / 2, this.length / 2) + 5;

        // Remove dropped items
        for (net.minecraft.world.entity.Entity entity : org.millenaire.utilities.WorldUtilities.getEntitiesWithinAABB(
                world, net.minecraft.world.entity.item.ItemEntity.class,
                location.pos.getBlockPos(), radius, 10)) {
            entity.discard();
        }

        // Remove stuck animals
        for (net.minecraft.world.entity.Entity entity : org.millenaire.utilities.WorldUtilities.getEntitiesWithinAABB(
                world, net.minecraft.world.entity.animal.Animal.class,
                location.pos.getBlockPos(), radius, 10)) {
            net.minecraft.world.entity.animal.Animal animal = (net.minecraft.world.entity.animal.Animal) entity;
            if (animal.isInWall() && !animal.isPersistenceRequired()) {
                entity.discard();
            }
        }

        // Remove stuck hostile mobs
        for (net.minecraft.world.entity.Entity entity : org.millenaire.utilities.WorldUtilities.getEntitiesWithinAABB(
                world, net.minecraft.world.entity.monster.Monster.class,
                location.pos.getBlockPos(), radius, 10)) {
            net.minecraft.world.entity.monster.Monster mob = (net.minecraft.world.entity.monster.Monster) entity;
            if (mob.isInWall() && !mob.isPersistenceRequired()) {
                entity.discard();
            }
        }

        // TODO: TreeClearer.cleanup() when TreeClearer class is ported
    }

    /**
     * Check for snow in the building area.
     */
    private boolean[][] checkForSnow(net.minecraft.world.level.Level world, BuildingLocation location) {
        int clearLengthBefore = areaToClearLengthBefore >= 0 ? areaToClearLengthBefore : areaToClear;
        int clearLengthAfter = areaToClearLengthAfter >= 0 ? areaToClearLengthAfter : areaToClear;
        int clearWidthBefore = areaToClearWidthBefore >= 0 ? areaToClearWidthBefore : areaToClear;
        int clearWidthAfter = areaToClearWidthAfter >= 0 ? areaToClearWidthAfter : areaToClear;

        boolean[][] snow = new boolean[this.length + clearLengthBefore + clearLengthAfter][this.width + clearWidthBefore
                + clearWidthAfter];

        int x = location.pos.getiX();
        int z = location.pos.getiZ();
        int orientation = location.orientation;

        for (int dx = -clearLengthBefore; dx < this.length + clearLengthAfter; dx++) {
            for (int dz = -clearWidthBefore; dz < this.width + clearWidthAfter; dz++) {
                Point p = adjustForOrientation(x, 256, z, dx - this.lengthOffset, dz - this.widthOffset, orientation);
                boolean stop = false;
                boolean isSnow = false;

                while (!stop && p.y > 0.0) {
                    Block block = p.getBlock(world);
                    if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) {
                        stop = true;
                        isSnow = true;
                    } else if (org.millenaire.utilities.WorldUtilities.getBlockState(world, p).canOcclude()) {
                        stop = true;
                    } else {
                        p = p.getBelow();
                    }
                }

                snow[dx + clearLengthBefore][dz + clearWidthBefore] = isSnow;
            }
        }

        return snow;
    }

    /**
     * Set snow on the building area.
     */
    private void setSnow(net.minecraft.world.level.Level world, BuildingLocation location, boolean[][] laySnow) {
        int clearLengthBefore = areaToClearLengthBefore >= 0 ? areaToClearLengthBefore : areaToClear;
        int clearLengthAfter = areaToClearLengthAfter >= 0 ? areaToClearLengthAfter : areaToClear;
        int clearWidthBefore = areaToClearWidthBefore >= 0 ? areaToClearWidthBefore : areaToClear;
        int clearWidthAfter = areaToClearWidthAfter >= 0 ? areaToClearWidthAfter : areaToClear;

        int x = location.pos.getiX();
        int z = location.pos.getiZ();
        int orientation = location.orientation;

        for (int dx = -clearLengthBefore; dx < this.length + clearLengthAfter; dx++) {
            for (int dz = -clearWidthBefore; dz < this.width + clearWidthAfter; dz++) {
                if (laySnow[dx + clearLengthBefore][dz + clearWidthBefore]) {
                    Point p = adjustForOrientation(x, 256, z, dx - this.lengthOffset, dz - this.widthOffset,
                            orientation);

                    while (p.y > 0.0 && !org.millenaire.utilities.WorldUtilities.getBlockState(world, p).canOcclude()) {
                        p = p.getBelow();
                    }

                    Point above = p.getAbove();
                    if (above.getBlock(world) == Blocks.AIR) {
                        org.millenaire.utilities.WorldUtilities.setBlockState(world, above,
                                Blocks.SNOW.defaultBlockState(), true, false);
                    }
                }
            }
        }
    }

    /**
     * Check if this plan contains a specific tag.
     */
    public boolean containsTags(String tag) {
        return this.tags.contains(tag.toLowerCase()) && !this.clearTags.contains(tag.toLowerCase());
    }

    /**
     * Fill Hall of Fame signs.
     */
    private void fillHoFSigns(BuildingLocation location, net.minecraft.world.level.Level world) {
        // TODO: Implement HoF sign filling when sign handling is ported
    }

    /**
     * Finds a valid location for this building plan.
     * Stub implementation for compilation.
     */
    public BuildingLocation findBuildingLocation(VillageMapInfo winfo, RegionMapper regionMapper, Point targetPos,
            int radius, java.util.Random random, int maxTries) {
        // TODO: Implement full terrain scanning and location finding logic
        // For now, returning null to satisfy compilation.
        return null;
    }

    /**
     * Reference building points.
     * Links special points from the plan to the building's resource manager.
     * Full port from 1.12.2 referenceBuildingPoints().
     */
    private void referenceBuildingPoints(org.millenaire.common.village.Building building) {
        if (building == null || building.location == null)
            return;

        BuildingLocation loc = building.location;
        org.millenaire.common.village.BuildingResManager resManager = building.getResManager();
        if (resManager == null)
            return;

        // Copy special positions from location (legacy compatibility)
        if (loc.sleepingPos != null)
            resManager.setSleepingPos(loc.sleepingPos);
        if (loc.sellingPos != null)
            resManager.setSellingPos(loc.sellingPos);
        if (loc.craftingPos != null)
            resManager.setCraftingPos(loc.craftingPos);
        if (loc.defendingPos != null)
            resManager.setDefendingPos(loc.defendingPos);
        if (loc.chestPos != null)
            resManager.addChest(loc.chestPos);

        // If plan array is null, we can't scan for special points
        if (this.plan == null) {
            if (MillConfigValues.LogBuildingPlan >= 2) {
                MillLog.minor(this, "Plan array is null for " + this.buildingKey + ", using location-only points");
            }
            return;
        }

        // Scan plan array for special point types
        int locationX = loc.pos.getiX();
        int locationY = loc.pos.getiY();
        int locationZ = loc.pos.getiZ();
        int orientation = loc.orientation;

        for (int i = 0; i < this.plan.length; i++) {
            for (int j = 0; j < this.plan[i].length; j++) {
                for (int k = 0; k < this.plan[i][j].length; k++) {
                    PointType pt = this.plan[i][j][k];
                    if (pt == null)
                        continue;

                    String specialType = pt.getSpecialType();
                    if (specialType == null)
                        continue;

                    // Calculate world position with orientation
                    Point p = adjustForOrientation(locationX, locationY + j, locationZ,
                            i - this.lengthOffset, k - this.widthOffset, orientation);
                    net.minecraft.core.BlockPos blockPos = new net.minecraft.core.BlockPos(
                            p.getiX(), p.getiY(), p.getiZ());

                    // Register point based on type
                    registerSpecialPoint(resManager, specialType, p, blockPos, pt);
                }
            }
        }

        if (MillConfigValues.LogBuildingPlan >= 2) {
            MillLog.minor(this, "Referenced building points for " + this.buildingKey +
                    ": " + resManager.chests.size() + " chests, " +
                    resManager.furnaces.size() + " furnaces, " +
                    resManager.soils.size() + " soil types");
        }
    }

    /**
     * Register a special point with the resource manager.
     */
    private void registerSpecialPoint(org.millenaire.common.village.BuildingResManager resManager,
            String specialType, Point p, net.minecraft.core.BlockPos blockPos, PointType pt) {

        // Soil types (crops)
        if (specialType.equals(SpecialPointTypeList.bsoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("minecraft", "wheat"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bricesoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("millenaire", "rice"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bturmericsoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("millenaire", "turmeric"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bmaizesoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("millenaire", "maize"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bcarrotsoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("minecraft", "carrot"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bpotatosoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("minecraft", "potato"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bsugarcanesoil)) {
            resManager.sugarcanesoils.add(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bnetherwartsoil)) {
            resManager.netherwartsoils.add(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bvinesoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("millenaire", "grape"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bcottonsoil)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("millenaire", "cotton"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bsilkwormblock)) {
            resManager.silkwormblock.add(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bsnailsoilblock)) {
            resManager.snailsoilblock.add(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bcacaospot)) {
            resManager.addSoilPoint(new net.minecraft.resources.ResourceLocation("minecraft", "cocoa_beans"), blockPos);
        }
        // Chests
        else if (pt.isSubType("lockedchest") || pt.isSubType("mainchest")) {
            resManager.addChest(blockPos);
        }
        // Positions
        else if (specialType.equals(SpecialPointTypeList.bsleepingPos)) {
            resManager.setSleepingPos(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bsellingPos)) {
            resManager.setSellingPos(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bcraftingPos)) {
            resManager.setCraftingPos(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bdefendingPos)) {
            resManager.setDefendingPos(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bshelterPos)) {
            resManager.setShelterPos(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bpathStartPos)) {
            resManager.setPathStartPos(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bleasurePos)) {
            resManager.setLeasurePos(blockPos);
        }
        // Furnaces and brewing
        else if (specialType.equals(SpecialPointTypeList.bfurnaceGuess)) {
            resManager.furnaces.add(blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bbrewingstand)) {
            resManager.brewingStands.add(blockPos);
        }
        // Animal spawns
        else if (specialType.equals(SpecialPointTypeList.bcowspawn)) {
            resManager.addSpawnPoint(new net.minecraft.resources.ResourceLocation("minecraft", "cow"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bsheepspawn)) {
            resManager.addSpawnPoint(new net.minecraft.resources.ResourceLocation("minecraft", "sheep"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bchickenspawn)) {
            resManager.addSpawnPoint(new net.minecraft.resources.ResourceLocation("minecraft", "chicken"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bpigspawn)) {
            resManager.addSpawnPoint(new net.minecraft.resources.ResourceLocation("minecraft", "pig"), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bwolfspawn)) {
            resManager.addSpawnPoint(new net.minecraft.resources.ResourceLocation("minecraft", "wolf"), blockPos);
        }
        // Tree spawns (wood sources)
        else if (specialType.equals(SpecialPointTypeList.boakspawn)) {
            resManager.woodspawn.add(blockPos);
            resManager.woodspawnTypes.put(blockPos, "oak");
        } else if (specialType.equals(SpecialPointTypeList.bpinespawn)) {
            resManager.woodspawn.add(blockPos);
            resManager.woodspawnTypes.put(blockPos, "spruce");
        } else if (specialType.equals(SpecialPointTypeList.bbirchspawn)) {
            resManager.woodspawn.add(blockPos);
            resManager.woodspawnTypes.put(blockPos, "birch");
        } else if (specialType.equals(SpecialPointTypeList.bjunglespawn)) {
            resManager.woodspawn.add(blockPos);
            resManager.woodspawnTypes.put(blockPos, "jungle");
        } else if (specialType.equals(SpecialPointTypeList.bdarkoakspawn)) {
            resManager.woodspawn.add(blockPos);
            resManager.woodspawnTypes.put(blockPos, "dark_oak");
        } else if (specialType.equals(SpecialPointTypeList.bacaciaspawn)) {
            resManager.woodspawn.add(blockPos);
            resManager.woodspawnTypes.put(blockPos, "acacia");
        }
        // Sources (infinite blocks for villagers to harvest)
        else if (specialType.equals(SpecialPointTypeList.bstonesource)) {
            resManager.addSourcePoint(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bsandsource)) {
            resManager.addSourcePoint(net.minecraft.world.level.block.Blocks.SAND.defaultBlockState(), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bgravelsource)) {
            resManager.addSourcePoint(net.minecraft.world.level.block.Blocks.GRAVEL.defaultBlockState(), blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bclaysource)) {
            resManager.addSourcePoint(net.minecraft.world.level.block.Blocks.CLAY.defaultBlockState(), blockPos);
        }
        // Mob spawners
        else if (specialType.equals(SpecialPointTypeList.bspawnerskeleton)) {
            resManager.addMobSpawnerPoint(new net.minecraft.resources.ResourceLocation("minecraft", "skeleton"),
                    blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bspawnerzombie)) {
            resManager.addMobSpawnerPoint(new net.minecraft.resources.ResourceLocation("minecraft", "zombie"),
                    blockPos);
        } else if (specialType.equals(SpecialPointTypeList.bspawnerspider)) {
            resManager.addMobSpawnerPoint(new net.minecraft.resources.ResourceLocation("minecraft", "spider"),
                    blockPos);
        }
        // Brick spots
        else if (specialType.equals(SpecialPointTypeList.bbrickspot)) {
            resManager.brickspot.add(blockPos);
        }
        // Fishing spots
        else if (specialType.equals(SpecialPointTypeList.bfishingspot)) {
            resManager.fishingspots.add(blockPos);
        }
        // Healing spots
        else if (specialType.equals(SpecialPointTypeList.bhealingspot)) {
            resManager.healingspots.add(blockPos);
        }
        // Stalls
        else if (specialType.equals(SpecialPointTypeList.bstall)) {
            resManager.stalls.add(blockPos);
        }
        // Signs
        else if (pt.isSubType("sign") || specialType.equals(SpecialPointTypeList.bplainSignGuess)) {
            resManager.signs.add(blockPos);
        }
        // Banners
        else if (pt.isSubType("villageBanner")) {
            resManager.banners.add(blockPos);
        } else if (pt.isSubType("cultureBanner")) {
            resManager.cultureBanners.add(blockPos);
        }
    }

    /**
     * Test if a building can be placed at the given map position.
     * Full port from 1.12.2 testSpot().
     * 
     * @param winfo                  Village map terrain info
     * @param centre                 Village center point
     * @param x                      Map-relative X coordinate
     * @param z                      Map-relative Z coordinate
     * @param random                 Random source
     * @param porientation           Forced orientation (-1 for auto-calculate)
     * @param ignoreExtraConstraints Skip farFromTag/closeToTag checks
     * @return LocationReturn with valid location or error code
     */
    public LocationReturn testSpot(VillageMapInfo winfo,
            Point centre, int x, int z, java.util.Random random,
            int porientation, boolean ignoreExtraConstraints) {

        Point testPosHorizontal = new Point(x + winfo.mapStartX, 64.0, z + winfo.mapStartZ);

        // Bounds check
        if (x < 0 || x >= winfo.length || z < 0 || z >= winfo.width) {
            return new LocationReturn(LocationReturn.OUTSIDE_RADIUS, testPosHorizontal);
        }

        winfo.buildTested[x][z] = true;

        if (MillConfigValues.LogWorldGeneration >= 3) {
            MillLog.debug(this, "Testing: " + x + "/" + z);
        }

        // Check farFromTag constraints
        if (!ignoreExtraConstraints && this.farFromTag != null) {
            for (String tag : this.farFromTag.keySet()) {
                for (BuildingLocation location : winfo.getBuildingLocations()) {
                    if (location.planKey != null && location.planKey.contains(tag)) {
                        double dist = testPosHorizontal.horizontalDistanceTo(
                                new Point(location.pos.getiX(), 0, location.pos.getiZ()));
                        if (dist < this.farFromTag.get(tag).intValue()) {
                            return new LocationReturn(LocationReturn.TOO_CLOSE_TO_TAG, testPosHorizontal);
                        }
                    }
                }
            }
        }

        // Check closeToTag constraints
        if (!ignoreExtraConstraints && this.closeToTag != null && !this.closeToTag.isEmpty()) {
            for (String tag : this.closeToTag.keySet()) {
                boolean foundNearbyBuilding = false;
                for (BuildingLocation location : winfo.getBuildingLocations()) {
                    if (location.planKey != null && location.planKey.contains(tag)) {
                        double dist = testPosHorizontal.horizontalDistanceTo(
                                new Point(location.pos.getiX(), 0, location.pos.getiZ()));
                        if (dist < this.closeToTag.get(tag).intValue()) {
                            foundNearbyBuilding = true;
                            break;
                        }
                    }
                }
                if (!foundNearbyBuilding) {
                    return new LocationReturn(LocationReturn.TOO_FAR_FROM_TAG, testPosHorizontal);
                }
            }
        }

        // Calculate orientation
        int orientation;
        if (porientation == -1) {
            orientation = computeOrientation(new Point(x + winfo.mapStartX, 0, z + winfo.mapStartZ), centre);
        } else {
            orientation = porientation;
        }
        orientation = (orientation + this.buildingOrientation) % 4;

        // Calculate footprint dimensions
        int xwidth, zwidth;
        if (orientation == 1 || orientation == 3) {
            xwidth = this.width + this.areaToClearWidthBefore + this.areaToClearWidthAfter + 2;
            zwidth = this.length + this.areaToClearLengthBefore + this.areaToClearLengthAfter + 2;
        } else {
            xwidth = this.length + this.areaToClearLengthBefore + this.areaToClearLengthAfter + 2;
            zwidth = this.width + this.areaToClearWidthBefore + this.areaToClearWidthAfter + 2;
        }

        // Scan footprint for errors
        int altitudeTotal = 0;
        int nbPoints = 0;
        int nbError = 0;
        int allowedErrors = (xwidth * zwidth > 200) ? xwidth * zwidth / 20 : 10;

        for (int i = -xwidth / 2; i <= xwidth / 2; i++) {
            for (int j = -zwidth / 2; j <= zwidth / 2; j++) {
                int ci = x + i;
                int cj = z + j;

                // Bounds check
                if (ci < 0 || cj < 0 || ci >= winfo.length || cj >= winfo.width) {
                    return new LocationReturn(LocationReturn.OUTSIDE_RADIUS,
                            new Point(ci + winfo.mapStartX, 64, cj + winfo.mapStartZ));
                }

                // Location clash check
                if (winfo.buildingLocRef[ci][cj] != null) {
                    return new LocationReturn(LocationReturn.LOCATION_CLASH,
                            new Point(ci + winfo.mapStartX, 64, cj + winfo.mapStartZ));
                }

                // Forbidden area check
                if (winfo.buildingForbidden[ci][cj]) {
                    if (nbError > allowedErrors) {
                        return new LocationReturn(LocationReturn.CONSTRUCTION_FORBIDDEN,
                                new Point(ci + winfo.mapStartX, 64, cj + winfo.mapStartZ));
                    }
                    nbError++;
                } else if (winfo.danger[ci][cj]) {
                    if (nbError > allowedErrors) {
                        return new LocationReturn(LocationReturn.DANGER,
                                new Point(ci + winfo.mapStartX, 64, cj + winfo.mapStartZ));
                    }
                    nbError++;
                } else if (!winfo.canBuild[ci][cj]) {
                    if (nbError > allowedErrors) {
                        return new LocationReturn(LocationReturn.WRONG_ALTITUDE,
                                new Point(ci + winfo.mapStartX, 64, cj + winfo.mapStartZ));
                    }
                    nbError++;
                }

                altitudeTotal += winfo.topGround[ci][cj];
                nbPoints++;
            }
        }

        // Calculate average altitude
        int altitude = Math.round(altitudeTotal * 1.0F / nbPoints);
        altitude += this.altitudeOffset;

        // Create valid location
        BuildingLocation l = new BuildingLocation(this,
                new Point(x + winfo.mapStartX, altitude, z + winfo.mapStartZ),
                orientation);
        return new LocationReturn(l);
    }

    /**
     * Update tags on building.
     * Copies plan tags to the building for condition checks.
     * Full port from 1.12.2 updateTags().
     */
    private void updateTags(org.millenaire.common.village.Building building) {
        if (building == null)
            return;

        // Add tags from the plan
        if (this.tags != null && !this.tags.isEmpty()) {
            building.addTags(this.tags, this.buildingKey + ": registering new tags");
            if (MillConfigValues.LogBuildingPlan >= 2) {
                MillLog.minor(this, "Applying tags: " + String.join(", ", this.tags)
                        + ", result: " + String.join(", ", building.getTags()));
            }
        }

        // Clear tags specified by this plan
        if (this.clearTags != null && !this.clearTags.isEmpty()) {
            for (String tag : this.clearTags) {
                building.getTags().remove(tag);
            }
            if (MillConfigValues.LogBuildingPlan >= 2) {
                MillLog.minor(this, "Clearing tags: " + String.join(", ", this.clearTags)
                        + ", result: " + String.join(", ", building.getTags()));
            }
        }

        // NOTE: Parent tags commented out - Building class needs parentBuilding field
        // Add tags to parent building (if parentBuilding field exists)
        // if (this.parentTags != null && !this.parentTags.isEmpty()) {
        // org.millenaire.common.village.Building parent = building.parentBuilding;
        // if (parent != null) {
        // parent.addTags(this.parentTags, this.buildingKey + ": registering new parent
        // tags");
        // }
        // }

        // Add tags to village (town hall)
        if (this.villageTags != null && !this.villageTags.isEmpty()) {
            org.millenaire.common.village.Building townHall = building.getTownHall();
            if (townHall != null) {
                townHall.addTags(this.villageTags, this.buildingKey + ": registering new village tags");
                if (MillConfigValues.LogBuildingPlan >= 2) {
                    MillLog.minor(this, "Applying village tags: " + String.join(", ", this.villageTags)
                            + ", result: " + String.join(", ", townHall.getTags()));
                }
            }
        }
    }

    /**
     * Update building for plan.
     * Called when upgrading an existing building to a new level.
     */
    private void updateBuildingForPlan(org.millenaire.common.village.Building building) {
        if (building == null || building.location == null)
            return;

        // Update the location's level to match this plan
        building.location.level = this.level;

        // Reference the new building points
        referenceBuildingPoints(building);

        // Update tags
        updateTags(building);
    }

    /**
     * Display panel numbers during wand import.
     */
    private void displayPanelNumbers(net.minecraft.world.level.Level world, BuildingLocation location) {
        // TODO: Implement panel number display
    }

    /**
     * Get building points for construction.
     * Ported from 1.12.2 getBuildingPoints().
     */
    public BuildingBlock[] getBuildingPoints(
            net.minecraft.world.level.Level world,
            BuildingLocation location,
            boolean villageGeneration,
            boolean includeSpecialPoints,
            boolean deletionLogs) {

        int locationX = location.pos.getiX();
        int locationY = location.pos.getiY();
        int locationZ = location.pos.getiZ();
        int orientation = location.orientation;

        if (MillConfigValues.LogWorldGeneration >= 2) {
            MillLog.minor(this, "Getting blocks for " + this.planName + " at "
                    + locationX + "/" + locationY + "/" + locationZ + "/" + orientation);
        }

        List<BuildingBlock> bblocks = new ArrayList<>();

        if (this.blocks == null) {
            MillLog.error(this, "No block data loaded for " + this.buildingKey);
            return new BuildingBlock[0];
        }

        // Iterate through all floors and positions
        for (int deltaY = 0; deltaY < this.nbFloors; deltaY++) {
            for (int deltaX = 0; deltaX < this.length; deltaX++) {
                for (int deltaZ = 0; deltaZ < this.width; deltaZ++) {
                    BlockState bs = this.blocks[deltaY][deltaX][deltaZ];
                    if (bs == null) {
                        continue;
                    }

                    Point p = adjustForOrientation(
                            locationX,
                            locationY + deltaY + this.startLevel,
                            locationZ,
                            deltaX - this.lengthOffset,
                            deltaZ - this.widthOffset,
                            orientation);

                    if (p == null) {
                        continue;
                    }

                    // Rotate block state if needed
                    BlockState rotatedState = rotateBlockStateForOrientation(bs, orientation);
                    bblocks.add(new BuildingBlock(p, rotatedState));
                }
            }
        }

        // Call autoGuessLaddersDoorsStairs for auto-orientation
        this.autoGuessLaddersDoorsStairs(bblocks);

        return bblocks.toArray(new BuildingBlock[0]);
    }

    /**
     * Rotate a block state for the given orientation.
     */
    private BlockState rotateBlockStateForOrientation(BlockState bs, int orientation) {
        if (orientation == 0) {
            return bs;
        }

        net.minecraft.world.level.block.Rotation rotation;
        switch (orientation) {
            case 1:
                rotation = net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
                break;
            case 2:
                rotation = net.minecraft.world.level.block.Rotation.CLOCKWISE_180;
                break;
            case 3:
                rotation = net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;
                break;
            default:
                return bs;
        }

        return bs.rotate(rotation);
    }

    /**
     * Auto-guess orientations for ladders, doors, and stairs.
     * Ported from 1.12.2 autoGuessLaddersDoorsStairs().
     */
    public void autoGuessLaddersDoorsStairs(List<BuildingBlock> bblocks) {
        List<BuildingBlock> stairs = new ArrayList<>();
        List<BuildingBlock> ladders = new ArrayList<>();
        List<BuildingBlock> doors = new ArrayList<>();
        HashMap<Point, BuildingBlock> map = new HashMap<>();

        for (BuildingBlock block : bblocks) {
            map.put(block.p, block);
            if (block.block == Blocks.LADDER && block.getMeta() == -1) {
                ladders.add(block);
            } else if (block.block == Blocks.OAK_DOOR) {
                doors.add(block);
            } else if ((block.block == Blocks.STONE_STAIRS || block.block == Blocks.OAK_STAIRS)
                    && block.getMeta() == -1) {
                block.setMeta((byte) -1);
                stairs.add(block);
            }
        }

        // Ladder orientation guessing
        boolean[] northValid = new boolean[ladders.size()];
        boolean[] southValid = new boolean[ladders.size()];
        boolean[] westValid = new boolean[ladders.size()];
        boolean[] eastValid = new boolean[ladders.size()];
        int i = 0;

        for (BuildingBlock ladder : ladders) {
            northValid[i] = mapIsOpaqueBlock(map, ladder.p.getNorth());
            southValid[i] = mapIsOpaqueBlock(map, ladder.p.getSouth());
            westValid[i] = mapIsOpaqueBlock(map, ladder.p.getWest());
            eastValid[i] = mapIsOpaqueBlock(map, ladder.p.getEast());

            if (northValid[i] && !southValid[i] && !westValid[i] && !eastValid[i]) {
                ladder.setMeta((byte) Direction.SOUTH.get3DDataValue());
            } else if (!northValid[i] && southValid[i] && !westValid[i] && !eastValid[i]) {
                ladder.setMeta((byte) Direction.NORTH.get3DDataValue());
            } else if (!northValid[i] && !southValid[i] && westValid[i] && !eastValid[i]) {
                ladder.setMeta((byte) Direction.EAST.get3DDataValue());
            } else if (!northValid[i] && !southValid[i] && !westValid[i] && eastValid[i]) {
                ladder.setMeta((byte) Direction.WEST.get3DDataValue());
            }

            i++;
        }

        // Stair orientation guessing
        i = 0;
        northValid = new boolean[stairs.size()];
        southValid = new boolean[stairs.size()];
        westValid = new boolean[stairs.size()];
        eastValid = new boolean[stairs.size()];

        for (BuildingBlock stair : stairs) {
            northValid[i] = !mapIsOpaqueBlock(map, stair.p.getSouth());
            southValid[i] = !mapIsOpaqueBlock(map, stair.p.getNorth());
            westValid[i] = !mapIsOpaqueBlock(map, stair.p.getEast());
            eastValid[i] = !mapIsOpaqueBlock(map, stair.p.getWest());

            if (northValid[i]) {
                stair.setMeta((byte) 1);
            } else if (southValid[i]) {
                stair.setMeta((byte) 0);
            } else if (westValid[i]) {
                stair.setMeta((byte) 3);
            } else if (eastValid[i]) {
                stair.setMeta((byte) 2);
            } else {
                stair.setMeta((byte) 0);
            }

            i++;
        }

        // Door inversion checking
        for (BuildingBlock door : doors) {
            BlockState bs = door.getBlockState();
            if (bs.hasProperty(
                    net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
                Direction facing = bs.getValue(
                        net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                boolean invert = false;

                if (facing == Direction.NORTH) {
                    if ((!map.containsKey(door.p.getWest()) || map.get(door.p.getWest()).block == Blocks.AIR
                            || map.get(door.p.getWest()).block == Blocks.OAK_DOOR)
                            && map.containsKey(door.p.getEast())) {
                        invert = true;
                    }
                } else if (facing == Direction.EAST) {
                    if ((!map.containsKey(door.p.getNorth()) || map.get(door.p.getNorth()).block == Blocks.AIR
                            || map.get(door.p.getNorth()).block == Blocks.OAK_DOOR)
                            && map.containsKey(door.p.getSouth())) {
                        invert = true;
                    }
                } else if (facing == Direction.SOUTH) {
                    if ((!map.containsKey(door.p.getEast()) || map.get(door.p.getEast()).block == Blocks.AIR
                            || map.get(door.p.getEast()).block == Blocks.OAK_DOOR)
                            && map.containsKey(door.p.getWest())) {
                        invert = true;
                    }
                } else if (facing == Direction.WEST) {
                    if ((!map.containsKey(door.p.getSouth()) || map.get(door.p.getSouth()).block == Blocks.AIR
                            || map.get(door.p.getSouth()).block == Blocks.OAK_DOOR)
                            && map.containsKey(door.p.getNorth())) {
                        invert = true;
                    }
                }

                if (invert) {
                    door.special = BuildingBlock.INVERTED_DOOR;
                }
            }
        }
    }

    /**
     * Check if a block in the map is opaque.
     */
    private boolean mapIsOpaqueBlock(HashMap<Point, BuildingBlock> map, Point p) {
        if (!map.containsKey(p)) {
            return false;
        }
        BuildingBlock bb = map.get(p);
        return bb.block != null && bb.block != Blocks.AIR && bb.getBlockState().canOcclude();
    }

    // IBuildingPlan implementation
    @Override
    public Culture getCulture() {
        return this.culture;
    }

    @Override
    public List<String> getMaleResident() {
        return this.maleResident;
    }

    @Override
    public List<String> getFemaleResident() {
        return this.femaleResident;
    }

    @Override
    public List<String> getVisitors() {
        return this.visitors;
    }

    @Override
    public String getNativeName() {
        return this.nativeName;
    }

    @Override
    public String getNameTranslated() {
        // TODO: Implement language lookup
        String lang = "en";
        if (translatedNames.containsKey(lang)) {
            return translatedNames.get(lang);
        }
        return nativeName != null ? nativeName : buildingKey;
    }

    public String getNameNativeAndTranslated() {
        String translated = getNameTranslated();
        if (nativeName != null && !nativeName.equals(translated)) {
            return nativeName + " (" + translated + ")";
        }
        return translated;
    }

    // WeightedChoice implementation
    @Override
    public int getChoiceWeight(Player player) {
        return this.weight;
    }

    // Getters
    public ItemStack getIcon() {
        if (icon != null) {
            return icon.toItemStack(1);
        }
        return ItemStack.EMPTY;
    }

    public File getLoadedFromFile() {
        return loadedFromFile;
    }

    public void setLoadedFromFile(File file) {
        this.loadedFromFile = file;
    }

    @Override
    public String toString() {
        return buildingKey + "_" + (char) ('A' + variation) + level +
                (nativeName != null ? " (" + nativeName + ")" : "");
    }

    // =========================================================================
    // Inner Classes
    // =========================================================================

    /**
     * Return value for building location search.
     * Contains either a valid location or an error code with position.
     */
    public static class LocationReturn {
        public static final int OUTSIDE_RADIUS = 1;
        public static final int LOCATION_CLASH = 2;
        public static final int CONSTRUCTION_FORBIDDEN = 3;
        public static final int WRONG_ALTITUDE = 4;
        public static final int DANGER = 5;
        public static final int NOT_REACHABLE = 6;
        public static final int TOO_CLOSE_TO_TAG = 7;
        public static final int TOO_FAR_FROM_TAG = 8;

        public BuildingLocation location;
        public int errorCode;
        public Point errorPos;

        public LocationReturn(BuildingLocation l) {
            this.location = l;
            this.errorCode = 0;
            this.errorPos = null;
        }

        public LocationReturn(int error, Point p) {
            this.location = null;
            this.errorCode = error;
            this.errorPos = p;
        }

        public boolean isValid() {
            return location != null && errorCode == 0;
        }

        public String getErrorMessage() {
            switch (errorCode) {
                case OUTSIDE_RADIUS:
                    return "Outside village radius";
                case LOCATION_CLASH:
                    return "Location clashes with existing building";
                case CONSTRUCTION_FORBIDDEN:
                    return "Construction forbidden at location";
                case WRONG_ALTITUDE:
                    return "Wrong altitude";
                case DANGER:
                    return "Dangerous location";
                case NOT_REACHABLE:
                    return "Location not reachable from town hall";
                case TOO_CLOSE_TO_TAG:
                    return "Too close to required distant building";
                case TOO_FAR_FROM_TAG:
                    return "Too far from required nearby building";
                default:
                    return "Unknown error: " + errorCode;
            }
        }
    }
}
