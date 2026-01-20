package org.millenaire.common.culture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.worldgen.MillenaireBuildingParser;

/**
 * Represents a Millénaire culture with its buildings, villagers, and unique
 * characteristics.
 * 
 * Cultures define:
 * - Building styles and plans
 * - Villager types and appearance
 * - Trade goods and economy
 * - Cultural aesthetics
 * 
 * Ported from 1.12.2 with enhanced language and loading support.
 */
public class Culture {
    // === Language Level Constants ===
    public static final int LANGUAGE_FLUENT = 500;
    public static final int LANGUAGE_MODERATE = 200;
    public static final int LANGUAGE_BEGINNER = 100;

    // Static registry for culture lookup
    private static final Map<String, Culture> REGISTRY = new HashMap<>();

    // Static list of all cultures (for iteration)
    public static final List<Culture> ListCultures = new ArrayList<>();

    public final String key;
    private final String id;
    private final String displayName;
    private final List<String> buildingPaths;
    private final Map<String, VillagerType> villagerTypes;
    private final List<String> tradeGoods;
    private final List<VillageType> villageTypes;
    private final Map<String, List<String>> nameLists;
    private final Map<String, WallType> wallTypes = new HashMap<>();

    // Shop configuration
    public Map<String, List<TradeGood>> shopSells = new HashMap<>();
    public Map<String, List<TradeGood>> shopBuys = new HashMap<>();
    public Map<String, List<TradeGood>> shopBuysOptional = new HashMap<>();

    // Trade goods by key and by item
    private final Map<String, TradeGood> tradeGoodsByKey = new HashMap<>();

    // Plan Cache
    private final Map<String, BuildingPlanSet> loadedPlans = new HashMap<>();

    // Language support
    private CultureLanguage mainLanguage;
    private CultureLanguage fallbackLanguage;
    private final Map<String, CultureLanguage> loadedLanguages = new HashMap<>();

    public Culture(String id, String displayName) {
        this.key = id;
        this.id = id;
        this.displayName = displayName;
        this.buildingPaths = new ArrayList<>();
        this.villagerTypes = new HashMap<>();
        this.tradeGoods = new ArrayList<>();
        this.villageTypes = new ArrayList<>();
        this.nameLists = new HashMap<>();
        REGISTRY.put(id, this);
        ListCultures.add(this);
    }

    // Static registry methods
    public static Culture getCultureByName(String name) {
        return REGISTRY.get(name);
    }

    public static Map<String, Culture> getAllCultures() {
        return REGISTRY;
    }

    // VillageType lookup
    public VillageType getVillageType(String key) {
        for (VillageType vt : villageTypes) {
            if (vt.getId().equals(key)) {
                return vt;
            }
        }
        return null;
    }

    public BuildingPlanSet getBuildingPlanSet(String key) {
        if (loadedPlans.containsKey(key)) {
            return loadedPlans.get(key);
        }

        BuildingPlanSet set = new BuildingPlanSet(this, key, null, null);
        // Try to load variations and levels
        // Heuristic: Try vars A-C, levels 0-5
        char[] variations = { 'A', 'B', 'C' };

        boolean foundAny = false;

        for (int v = 0; v < variations.length; v++) {
            for (int l = 0; l < 6; l++) {
                // Construct path: cultures/norman/buildings/key_A0.png
                // Wait, path is relative to assets/millenaire/buildings/
                // key probably includes subfolder like "core/townhall"
                // So "norman/core/townhall_A0.png"
                String suffix = "_" + variations[v] + l;
                // We need to find the full path from buildingPaths list to be sure

                // Using getBuildingPath to find the base path matching key
                // The key passed here is typically "core/townhall" or just "townhall" depending
                // on usage.
                // Let's assume key is relative to building root for this culture.

                String pathToCheck = id + "/" + key + suffix + ".png";
                // Or try with .txt if no png?

                try {
                    // We should verify if resource exists.
                    // MillenaireBuildingParser.loadPlan checks resource existence indirectly?
                    // Let's assume we can try to load it.
                    // Let's assume we can try to load it.
                    BuildingPlan plan = MillenaireBuildingParser.loadPlan(pathToCheck);

                    if (plan != null) {
                        set.addPlan(plan, v, l);
                        foundAny = true;
                    }
                } catch (Exception e) {
                    // Expected if file missing
                }
            }
        }

        if (foundAny) {
            loadedPlans.put(key, set);
            return set;
        }
        return null;
    }

    /**
     * Inner class for villager archetypes used in culture configuration.
     */
    public static class VillagerArchetype {
        public final String name;
        public final String role;
        public final boolean isMale;

        public VillagerArchetype(String name, String role, boolean isMale) {
            this.name = name;
            this.role = role;
            this.isMale = isMale;
        }
    }

    // Language methods
    public void setMainLanguage(CultureLanguage lang) {
        this.mainLanguage = lang;
        loadedLanguages.put(lang.getLanguageCode(), lang);
    }

    public void setFallbackLanguage(CultureLanguage lang) {
        this.fallbackLanguage = lang;
        loadedLanguages.put(lang.getLanguageCode(), lang);
    }

    public String getCultureString(String key) {
        key = key.toLowerCase();
        if (mainLanguage != null && mainLanguage.strings.containsKey(key)) {
            return mainLanguage.strings.get(key);
        }
        if (fallbackLanguage != null && fallbackLanguage.strings.containsKey(key)) {
            return fallbackLanguage.strings.get(key);
        }
        return key;
    }

    public String getBuildingGameName(String planName) {
        if (mainLanguage != null) {
            String name = mainLanguage.getBuildingName(planName);
            if (name != null)
                return name;
        }
        if (fallbackLanguage != null) {
            String name = fallbackLanguage.getBuildingName(planName);
            if (name != null)
                return name;
        }
        return planName;
    }

    // Trade good methods
    public TradeGood getTradeGoodByKey(String key) {
        return tradeGoodsByKey.get(key);
    }

    public void registerTradeGood(String key, TradeGood good) {
        tradeGoodsByKey.put(key, good);
    }

    public void addVillageType(VillageType type) {
        villageTypes.add(type);
    }

    public List<VillageType> getVillageTypes() {
        return villageTypes;
    }

    public Culture addBuilding(String category, String buildingName) {
        buildingPaths.add(id + "/" + category + "/" + buildingName + "_A0.png");
        return this;
    }

    // Custom buildings registry
    private final Map<String, org.millenaire.common.buildingplan.BuildingCustomPlan> customBuildings = new HashMap<>();

    public org.millenaire.common.buildingplan.BuildingCustomPlan getBuildingCustom(String key) {
        return customBuildings.get(key);
    }

    public void registerCustomBuilding(org.millenaire.common.buildingplan.BuildingCustomPlan plan) {
        customBuildings.put(plan.buildingKey, plan);
    }

    public Culture addWallType(WallType wallType) {
        wallTypes.put(wallType.key, wallType);
        return this;
    }

    public WallType getWallType(String key) {
        return wallTypes.get(key);
    }

    // Goods and crops lists for economy/farming
    private final List<TradeGood> listGoods = new ArrayList<>();
    private final List<CropDefinition> listCrops = new ArrayList<>();

    public Culture addGood(TradeGood good) {
        listGoods.add(good);
        return this;
    }

    public List<TradeGood> getListGoods() {
        return listGoods;
    }

    public Culture addCrop(CropDefinition crop) {
        listCrops.add(crop);
        return this;
    }

    public List<CropDefinition> getListCrops() {
        return listCrops;
    }

    public Culture addVillagerType(String key, VillagerType type) {
        villagerTypes.put(key, type);
        return this;
    }

    public Culture addVillagerType(String key, VillagerArchetype archetype) {
        // Create a VillagerType from the archetype
        VillagerType type = new VillagerType(key, archetype.name, archetype.isMale, archetype.role);
        villagerTypes.put(key, type);
        return this;
    }

    public Culture addTradeGood(String itemId) {
        tradeGoods.add(itemId);
        return this;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getBuildingPaths() {
        return buildingPaths;
    }

    public String getBuildingPath(String key) {
        for (String path : buildingPaths) {
            if (path.contains("/" + key + "_A0.png") || path.contains("/" + key + ".png")) {
                return path;
            }
        }
        return null;
    }

    public Map<String, VillagerType> getVillagerTypes() {
        return villagerTypes;
    }

    public VillagerType getVillagerType(String key) {
        return villagerTypes.get(key);
    }

    public List<String> getTradeGoods() {
        return tradeGoods;
    }

    public void addNameList(String key, List<String> names) {
        nameLists.put(key, names);
    }

    public String getRandomNameFromList(String listName) {
        if (!nameLists.containsKey(listName))
            return "Unknown";
        List<String> list = nameLists.get(listName);
        if (list.isEmpty())
            return "Unknown";
        return list.get((int) (Math.random() * list.size()));
    }

    public String getRandomNameFromList(String listName, java.util.Set<String> taken) {
        if (!nameLists.containsKey(listName))
            return "Unknown";
        List<String> list = new ArrayList<>(nameLists.get(listName));
        list.removeAll(taken);
        if (list.isEmpty())
            return getRandomNameFromList(listName);
        return list.get((int) (Math.random() * list.size()));
    }

    /**
     * Get a trade good by item.
     */
    public TradeGood getTradeGood(InvItem item) {
        if (item == null)
            return null;
        for (TradeGood good : tradeGoodsByKey.values()) {
            if (good.item != null && good.item.equals(item)) {
                return good;
            }
        }
        return null;
    }

    /**
     * Get the label for a reputation level.
     */
    public String getReputationLevelLabel(int reputation) {
        // TODO: Localize these strings
        if (reputation >= 40960)
            return "natural leader";
        if (reputation >= 640)
            return "friend";
        if (reputation >= 0)
            return "neutral";
        return "stranger";
    }
}
