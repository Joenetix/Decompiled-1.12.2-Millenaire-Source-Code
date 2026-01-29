package org.millenaire.common.culture;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.entity.player.Player;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.annotedparameters.ParametersManager;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.buildingplan.BuildingPlanSet;

/**
 * Represents a specific type of village within a culture (e.g. "Agricole",
 * "Militaire").
 * Loaded from text files in the culture's "villages" directory.
 * Ported from 1.12.2 with full ConfigField annotations.
 */
public class VillageType implements MillCommonUtilities.WeightedChoice {

    // === Constants ===
    private static final String VILLAGE_TYPE_HAMEAU = "hameau";
    private static final String VILLAGE_TYPE_MARVEL = "marvel";
    private static final float MINIMUM_VALID_BIOME_PERC = 0.6F;

    // === Core Fields ===
    public String key = null;
    public Culture culture;
    public boolean lonebuilding = false;

    // === Display Name ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Name of the village in the culture's language.")
    public String name = null;

    // === Icon ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
    @ConfigAnnotations.FieldDocumentation(explanation = "Name of a good whose icon represents this village.")
    private InvItem icon = null;

    // === Banner Configuration ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_basecolor")
    @ConfigAnnotations.FieldDocumentation(explanation = "A color the village's banner can have as its base color.")
    public List<String> banner_baseColors = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_patterncolor")
    @ConfigAnnotations.FieldDocumentation(explanation = "A color the village's banner can have as its pattern color.")
    public List<String> banner_patternsColors = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_chargecolor")
    @ConfigAnnotations.FieldDocumentation(explanation = "A color the village's banner can have as its charge color.")
    public List<String> banner_chargeColors = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_pattern")
    @ConfigAnnotations.FieldDocumentation(explanation = "A pattern for the banner. Uses one of the patterncolors.")
    public List<String> banner_Patterns = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_chargepattern")
    @ConfigAnnotations.FieldDocumentation(explanation = "A charge pattern for the banner. Uses one of the chargecolors.")
    public List<String> banner_chargePatterns = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_json")
    @ConfigAnnotations.FieldDocumentation(explanation = "A JSON object that specifies the banner's appearance.")
    public List<String> banner_JSONs = new ArrayList<>();

    // === Travel Book Display ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "travelbook_display", defaultValue = "true")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether to display this village type in the Travel Book.")
    public boolean travelBookDisplay = true;

    // === World Generation ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER)
    @ConfigAnnotations.FieldDocumentation(explanation = "Generation weight. Higher = more likely to be picked.", explanationCategory = "World Generation")
    public int weight = 100;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "biome")
    @ConfigAnnotations.FieldDocumentation(explanation = "A biome the village can spawn in.", explanationCategory = "World Generation")
    public List<String> biomes = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "-1")
    @ConfigAnnotations.FieldDocumentation(explanation = "Maximum number of this village type in a world. -1 for no limit.", explanationCategory = "World Generation")
    public int max = -1;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.FLOAT, defaultValue = "0.6")
    @ConfigAnnotations.FieldDocumentation(explanation = "% of village that must be in the appropriate biome.", explanationCategory = "World Generation")
    private float minimumBiomeValidity = MINIMUM_VALID_BIOME_PERC;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "true")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether this village type can be generated on an MP server.", explanationCategory = "World Generation")
    public boolean generateOnServer = true;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "generateforplayer", defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether this village type is generated for a specific player.", explanationCategory = "World Generation")
    public boolean generatedForPlayer = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "-1")
    @ConfigAnnotations.FieldDocumentation(explanation = "Minimum distance from spawn point. -1 for no limits.", explanationCategory = "World Generation")
    public int minDistanceFromSpawn = -1;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "requiredtag")
    @ConfigAnnotations.FieldDocumentation(explanation = "A global tag required for this village type to generate.", explanationCategory = "World Generation")
    public List<String> requiredTags = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "forbiddentag")
    @ConfigAnnotations.FieldDocumentation(explanation = "A global tag that stops the village from generating.", explanationCategory = "World Generation")
    public List<String> forbiddenTags = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Key lone buildings have priority in generation.", explanationCategory = "World Generation")
    public boolean keyLonebuilding = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
    @ConfigAnnotations.FieldDocumentation(explanation = "Player tag that activates higher generation chance.", explanationCategory = "World Generation")
    public String keyLoneBuildingGenerateTag = null;

    // === Village Type ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Player-controlled village, always spawned with a wand.", explanationCategory = "Village type")
    public boolean playerControlled = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "hameau")
    @ConfigAnnotations.FieldDocumentation(explanation = "Hamlet type generated around this village.", explanationCategory = "Village type")
    public List<String> hamlets = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING, paramName = "type")
    @ConfigAnnotations.FieldDocumentation(explanation = "Special type of village (e.g., 'hamlet', 'marvel').", explanationCategory = "Village type")
    private String specialType = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN)
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether this village type can be spawned with a wand.", explanationCategory = "Village type")
    public boolean spawnable = true;

    // === Village Buildings ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "centre")
    @ConfigAnnotations.FieldDocumentation(explanation = "The building at the centre of the village.", explanationCategory = "Village Buildings")
    public BuildingPlanSet centreBuilding = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "start")
    @ConfigAnnotations.FieldDocumentation(explanation = "A starting building.", explanationCategory = "Village Buildings")
    public List<BuildingPlanSet> startBuildings = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "player")
    @ConfigAnnotations.FieldDocumentation(explanation = "A player-purchasable building.", explanationCategory = "Village Buildings")
    public List<BuildingPlanSet> playerBuildings = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "core")
    @ConfigAnnotations.FieldDocumentation(explanation = "A core building, built with high priority.", explanationCategory = "Village Buildings")
    public List<BuildingPlanSet> coreBuildings = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "secondary")
    @ConfigAnnotations.FieldDocumentation(explanation = "A secondary building, built with reduced priority.", explanationCategory = "Village Buildings")
    public List<BuildingPlanSet> secondaryBuildings = new ArrayList<>();

    public List<BuildingPlanSet> extraBuildings = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "never")
    @ConfigAnnotations.FieldDocumentation(explanation = "A building this village will never build.", explanationCategory = "Village Buildings")
    public List<BuildingPlanSet> excludedBuildings = new ArrayList<>();

    // === Village Behaviour ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER)
    @ConfigAnnotations.FieldDocumentation(explanation = "Radius of the village.", explanationCategory = "Village Behaviour")
    public int radius = MillConfigValues.VillageRadius;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.WALL_TYPE)
    @ConfigAnnotations.FieldDocumentation(explanation = "Type of outer village walls.", explanationCategory = "Village Behaviour")
    public WallType outerWallType = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.WALL_TYPE)
    @ConfigAnnotations.FieldDocumentation(explanation = "Type of inner village walls.", explanationCategory = "Village Behaviour")
    public WallType innerWallType = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "50")
    @ConfigAnnotations.FieldDocumentation(explanation = "Radius of the inner village walls.", explanationCategory = "Village Behaviour")
    public int innerWallRadius = 50;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "1")
    @ConfigAnnotations.FieldDocumentation(explanation = "Max builders working simultaneously.", explanationCategory = "Village Behaviour")
    public int maxSimultaneousConstructions = 1;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "0")
    @ConfigAnnotations.FieldDocumentation(explanation = "Max builders on wall buildings.", explanationCategory = "Village Behaviour")
    public int maxSimultaneousWallConstructions = 0;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether this village type carries out raids.", explanationCategory = "Village Behaviour")
    public boolean carriesRaid = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_ADD)
    @ConfigAnnotations.FieldDocumentation(explanation = "Block to use as path material.", explanationCategory = "Village Behaviour")
    public List<InvItem> pathMaterial = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_PRICE_ADD, paramName = "sellingPrice")
    @ConfigAnnotations.FieldDocumentation(explanation = "Custom selling price override.", explanationCategory = "Village Behaviour")
    public HashMap<InvItem, Integer> sellingPrices = new HashMap<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_PRICE_ADD, paramName = "buyingPrice")
    @ConfigAnnotations.FieldDocumentation(explanation = "Custom buying price override.", explanationCategory = "Village Behaviour")
    public HashMap<InvItem, Integer> buyingPrices = new HashMap<>();

    // === Village Name ===
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
    @ConfigAnnotations.FieldDocumentation(explanation = "Name list for this village. 'villages' by default.", explanationCategory = "Village Name")
    public String nameList = "villages";

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_CASE_SENSITIVE_ADD, paramName = "qualifier")
    @ConfigAnnotations.FieldDocumentation(explanation = "Village qualifier applicable without conditions.", explanationCategory = "Village Name")
    public List<String> qualifiers = new ArrayList<>();

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for villages near hills.", explanationCategory = "Village Name")
    public String hillQualifier = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for villages near mountains.", explanationCategory = "Village Name")
    public String mountainQualifier = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for villages near deserts.", explanationCategory = "Village Name")
    public String desertQualifier = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for villages near forests.", explanationCategory = "Village Name")
    public String forestQualifier = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for villages near lava.", explanationCategory = "Village Name")
    public String lavaQualifier = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for villages near lakes.", explanationCategory = "Village Name")
    public String lakeQualifier = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
    @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for villages near seas.", explanationCategory = "Village Name")
    public String oceanQualifier = null;

    // === Brick Colour Themes ===
    public List<BrickColourTheme> brickColourThemes = new ArrayList<>();

    // === Constructors ===

    public VillageType(Culture c, String key, boolean lone) {
        this.key = key;
        this.culture = c;
        this.lonebuilding = lone;
        this.spawnable = !this.lonebuilding;
        this.nameList = this.lonebuilding ? null : "villages";
    }

    /**
     * Simple constructor for compatibility with existing code.
     */
    public VillageType(String id, String name, int weight) {
        this.key = id;
        this.name = name;
        this.weight = weight;
    }

    // === Static Loading Methods ===

    /**
     * Load all lone building types from a culture's lonebuildings directory.
     */
    public static List<VillageType> loadLoneBuildings(VirtualDir cultureVirtualDir, Culture culture) {
        VirtualDir lonebuildingsVirtualDir = cultureVirtualDir.getChildDirectory("lonebuildings");
        List<VillageType> v = new ArrayList<>();

        for (File file : lonebuildingsVirtualDir.listFilesRecursive(new MillCommonUtilities.ExtFileFilter("txt"))) {
            try {
                if (MillConfigValues.LogVillage >= 1) {
                    MillLog.major(file, "Loading lone building: " + file.getAbsolutePath());
                }

                VillageType village = loadVillageType(file, culture, true);
                if (village != null) {
                    v.remove(village);
                    v.add(village);
                }
            } catch (Exception e) {
                MillLog.printException(e);
            }
        }

        return v;
    }

    /**
     * Load all village types from a culture's villages directory.
     */
    public static List<VillageType> loadVillages(VirtualDir cultureVirtualDir, Culture culture) {
        VirtualDir villagesVirtualDir = cultureVirtualDir.getChildDirectory("villages");
        List<VillageType> villages = new ArrayList<>();

        for (File file : villagesVirtualDir.listFilesRecursive(new MillCommonUtilities.ExtFileFilter("txt"))) {
            try {
                if (MillConfigValues.LogVillage >= 1) {
                    MillLog.major(file, "Loading village: " + file.getAbsolutePath());
                }

                VillageType village = loadVillageType(file, culture, false);
                if (village != null) {
                    villages.remove(village);
                    villages.add(village);
                }
            } catch (Exception e) {
                MillLog.printException(e);
            }
        }

        return villages;
    }

    /**
     * Load a single village type from a file.
     */
    public static VillageType loadVillageType(File file, Culture c, boolean lonebuilding) {
        VillageType villageType = new VillageType(c, file.getName().split("\\.")[0], lonebuilding);

        try {
            ParametersManager.loadAnnotedParameterData(file, villageType, null, "village type", c);

            if (villageType.name == null) {
                throw new MillLog.MillenaireException("No name found for village: " + villageType.key);
            }
            if (villageType.centreBuilding == null) {
                throw new MillLog.MillenaireException("No central building found for village: " + villageType.key);
            }

            // Default path material if none specified
            if (villageType.pathMaterial.isEmpty()) {
                // TODO: Add default path material once InvItem is fully ported
            }

            if (MillConfigValues.LogVillage >= 1) {
                MillLog.major(villageType,
                        "Loaded village type " + villageType.name + ". NameList: " + villageType.nameList);
            }

            return villageType;
        } catch (Exception e) {
            MillLog.printException(e);
            return null;
        }
    }

    /**
     * Get list of spawnable villages for player.
     */
    public static List<VillageType> spawnableVillages(Player player) {
        List<VillageType> villages = new ArrayList<>();

        for (Culture culture : Culture.ListCultures) {
            for (VillageType village : culture.getVillageTypes()) {
                if (village.spawnable && village.playerControlled) {
                    villages.add(village);
                }
            }

            for (VillageType village : culture.getVillageTypes()) {
                if (village.spawnable && !village.playerControlled) {
                    villages.add(village);
                }
            }
        }

        return villages;
    }

    // === Instance Methods ===

    @Override
    public int getChoiceWeight(Player player) {
        return this.keyLonebuilding ? 10000 : this.weight;
    }

    public float getMinimumBiomeValidity() {
        return this.minimumBiomeValidity;
    }

    public String getNameNative() {
        return this.name;
    }

    public String getNameTranslated() {
        String translated = culture.getCultureString("village." + this.key);
        return translated.equals("village." + this.key) ? null : translated;
    }

    public String getNameNativeAndTranslated() {
        String fullName = getNameNative();
        String translated = getNameTranslated();
        if (translated != null && !translated.isEmpty()) {
            fullName = fullName + " (" + translated + ")";
        }
        return fullName;
    }

    public boolean isHamlet() {
        return VILLAGE_TYPE_HAMEAU.equals(this.specialType);
    }

    public boolean isMarvel() {
        return VILLAGE_TYPE_MARVEL.equals(this.specialType);
    }

    public boolean isRegularVillage() {
        return this.specialType == null && !this.lonebuilding;
    }

    public boolean isKeyLoneBuildingForGeneration(Player player) {
        if (this.keyLonebuilding) {
            return true;
        }
        // TODO: Check player tags when UserProfile is available
        return false;
    }

    // === Accessors for Compatibility ===

    public String getId() {
        return key;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public List<String> getValidBiomes() {
        return biomes;
    }

    public String getCentreBuilding() {
        if (centreBuilding != null && centreBuilding.key != null) {
            return centreBuilding.key;
        }
        return centreBuildingKey;
    }

    public int getVillageRadius() {
        return radius;
    }

    public WallType getOuterWallType() {
        return outerWallType;
    }

    public WallType getInnerWallType() {
        return innerWallType;
    }

    public int getInnerWallRadius() {
        return innerWallRadius;
    }

    public boolean hasInnerWalls() {
        return innerWallType != null;
    }

    public boolean hasOuterWalls() {
        return outerWallType != null;
    }

    public List<String> getHamlets() {
        return hamlets;
    }

    public boolean hasHamlets() {
        return !hamlets.isEmpty();
    }

    public int getMinDistanceFromSpawn() {
        return minDistanceFromSpawn;
    }

    public int getMaxSimultaneousConstructions() {
        return maxSimultaneousConstructions;
    }

    public boolean getCarriesRaid() {
        return carriesRaid;
    }

    public String getNameList() {
        return nameList;
    }

    public List<String> getQualifiers() {
        return qualifiers;
    }

    public boolean isLoneBuilding() {
        return lonebuilding;
    }

    // === Compatibility Setters for VillageConfigLoader ===

    public void addBiome(String biome) {
        biomes.add(biome);
    }

    public void addPathMaterial(String material) {
        // For compatibility - store as InvItem once fully ported
        // For now just track keys
    }

    public void setCentreBuilding(String building) {
        // Will be resolved later to BuildingPlanSet
        this.centreBuildingKey = building;
    }

    public void addStartingBuilding(String building) {
        startingBuildingKeys.add(building);
    }

    public void addCoreBuilding(String building) {
        coreBuildingKeys.add(building);
    }

    public void addSecondaryBuilding(String building) {
        secondaryBuildingKeys.add(building);
    }

    public void addForbiddenBuilding(String building) {
        excludedBuildingKeys.add(building);
    }

    public void setOuterWallType(String type) {
        this.outerWallKey = type;
    }

    public void setInnerWallType(String type) {
        this.innerWallKey = type;
    }

    public void setInnerWallRadius(int radius) {
        this.innerWallRadius = radius;
    }

    public void setVillageRadius(int radius) {
        this.radius = radius;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    // === Additional Getters for Compatibility ===

    /**
     * Get core building keys as strings (for compatibility).
     */
    public List<String> getCoreBuildings() {
        return coreBuildingKeys;
    }

    /**
     * Get secondary building keys as strings (for compatibility).
     */
    public List<String> getSecondaryBuildings() {
        return secondaryBuildingKeys;
    }

    /**
     * Get starting building keys as strings (for compatibility).
     */
    public List<String> getStartingBuildings() {
        return startingBuildingKeys;
    }

    /**
     * Get path materials as strings (for compatibility).
     */
    public List<String> getPathMaterials() {
        List<String> result = new ArrayList<>();
        for (InvItem item : pathMaterial) {
            if (item != null) {
                result.add(item.toString());
            }
        }
        // Default if empty
        if (result.isEmpty()) {
            result.add("pathgravel");
        }
        return result;
    }

    /**
     * Get selling prices as Map<String, Float> (for compatibility).
     */
    public Map<String, Float> getSellingPrices() {
        Map<String, Float> result = new HashMap<>();
        for (Map.Entry<InvItem, Integer> entry : sellingPrices.entrySet()) {
            if (entry.getKey() != null) {
                result.put(entry.getKey().toString(), (float) entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get buying prices as Map<String, Float> (for compatibility).
     */
    public Map<String, Float> getBuyingPrices() {
        Map<String, Float> result = new HashMap<>();
        for (Map.Entry<InvItem, Integer> entry : buyingPrices.entrySet()) {
            if (entry.getKey() != null) {
                result.put(entry.getKey().toString(), (float) entry.getValue());
            }
        }
        return result;
    }

    /**
     * Get outer wall key as string (for compatibility).
     */
    public String getOuterWallKey() {
        if (outerWallType != null) {
            return outerWallType.key;
        }
        return outerWallKey;
    }

    /**
     * Get inner wall key as string (for compatibility).
     */
    public String getInnerWallKey() {
        if (innerWallType != null) {
            return innerWallType.key;
        }
        return innerWallKey;
    }

    // === String-based key storage for deferred resolution ===
    private String centreBuildingKey = null;
    private String outerWallKey = null;
    private String innerWallKey = null;
    private List<String> startingBuildingKeys = new ArrayList<>();
    private List<String> coreBuildingKeys = new ArrayList<>();
    private List<String> secondaryBuildingKeys = new ArrayList<>();
    private List<String> excludedBuildingKeys = new ArrayList<>();

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof VillageType))
            return false;
        VillageType v = (VillageType) obj;
        return v.culture == this.culture && v.key.equals(this.key);
    }

    @Override
    public int hashCode() {
        return culture.hashCode() + key.hashCode();
    }

    @Override
    public String toString() {
        return "VillageType{" +
                "id='" + key + '\'' +
                ", name='" + name + '\'' +
                ", centre='" + getCentreBuilding() + '\'' +
                '}';
    }

    // === Inner Classes ===

    /**
     * Brick colour theme for Indian-style villages.
     */
    public static class BrickColourTheme implements MillCommonUtilities.WeightedChoice {
        public final String key;
        public final int weight;
        public final Map<DyeColor, Map<DyeColor, Integer>> colours;

        public BrickColourTheme(String key, int weight, Map<DyeColor, Map<DyeColor, Integer>> colours) {
            this.key = key;
            this.weight = weight;
            this.colours = colours;
        }

        @Override
        public int getChoiceWeight(Player player) {
            return this.weight;
        }

        public DyeColor getRandomDyeColour(DyeColor colour) {
            if (!colours.containsKey(colour)) {
                return DyeColor.WHITE;
            }

            Map<DyeColor, Integer> colourMap = colours.get(colour);
            int totalWeight = 0;

            for (Integer w : colourMap.values()) {
                totalWeight += w;
            }

            int pickedValue = MillCommonUtilities.randomInt(totalWeight);
            int currentWeightTotal = 0;

            for (Map.Entry<DyeColor, Integer> entry : colourMap.entrySet()) {
                currentWeightTotal += entry.getValue();
                if (pickedValue < currentWeightTotal) {
                    return entry.getKey();
                }
            }

            return DyeColor.WHITE;
        }

        @Override
        public String toString() {
            return "theme: " + this.key;
        }
    }
}
