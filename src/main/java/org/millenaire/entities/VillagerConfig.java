package org.millenaire.entities;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.mojang.logging.LogUtils;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.village.Building;
import org.millenaire.core.MillItems;
import org.slf4j.Logger;

/**
 * Configuration for villager tool/weapon/armor preferences.
 * Ported exactly from original 1.12.2 VillagerConfig.java
 */
public class VillagerConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String DEFAULT = "default";
    public static Map<String, VillagerConfig> villagerConfigs = new HashMap<>();
    public static VillagerConfig DEFAULT_CONFIG;

    public static String CATEGORY_WEAPONSHANDTOHAND = "weaponshandtohand";
    public static String CATEGORY_WEAPONSRANGED = "weaponsranged";
    public static String CATEGORY_ARMOURSHELMET = "armourshelmet";
    public static String CATEGORY_ARMOURSCHESTPLATE = "armourschestplate";
    public static String CATEGORY_ARMOURSLEGGINGS = "armoursleggings";
    public static String CATEGORY_ARMOURSBOOTS = "armoursboots";
    public static String CATEGORY_TOOLSSWORD = "toolssword";
    public static String CATEGORY_TOOLSPICKAXE = "toolspickaxe";
    public static String CATEGORY_TOOLSAXE = "toolsaxe";
    public static String CATEGORY_TOOLSHOE = "toolshoe";
    public static String CATEGORY_TOOLSSHOVEL = "toolsshovel";

    public final String key;

    public Map<InvItem, Integer> weapons = new HashMap<>();
    public Map<InvItem, Integer> weaponsHandToHand = new HashMap<>();
    public Map<InvItem, Integer> weaponsRanged = new HashMap<>();
    public Map<InvItem, Integer> armoursHelmet = new HashMap<>();
    public Map<InvItem, Integer> armoursChestplate = new HashMap<>();
    public Map<InvItem, Integer> armoursLeggings = new HashMap<>();
    public Map<InvItem, Integer> armoursBoots = new HashMap<>();
    public Map<InvItem, Integer> toolsSword = new HashMap<>();
    public Map<InvItem, Integer> toolsPickaxe = new HashMap<>();
    public Map<InvItem, Integer> toolsAxe = new HashMap<>();
    public Map<InvItem, Integer> toolsHoe = new HashMap<>();
    public Map<InvItem, Integer> toolsShovel = new HashMap<>();
    public Map<InvItem, Integer> foodsGrowth = new HashMap<>();
    public Map<InvItem, Integer> foodsConception = new HashMap<>();

    public List<InvItem> weaponsHandToHandSorted;
    public List<InvItem> weaponsRangedSorted;
    public List<InvItem> weaponsSorted;
    public List<InvItem> armoursHelmetSorted;
    public List<InvItem> armoursChestplateSorted;
    public List<InvItem> armoursLeggingsSorted;
    public List<InvItem> armoursBootsSorted;
    public List<InvItem> toolsSwordSorted;
    public List<InvItem> toolsPickaxeSorted;
    public List<InvItem> toolsAxeSorted;
    public List<InvItem> toolsHoeSorted;
    public List<InvItem> toolsShovelSorted;
    public List<InvItem> foodsGrowthSorted;
    public List<InvItem> foodsConceptionSorted;

    public Map<String, List<InvItem>> categories = new HashMap<>();

    private static VillagerConfig copyDefault(String key) {
        VillagerConfig newConfig = new VillagerConfig(key);

        for (Field field : VillagerConfig.class.getFields()) {
            try {
                if (field.getType() == Map.class) {
                    @SuppressWarnings("unchecked")
                    Map<InvItem, Integer> map = (Map<InvItem, Integer>) field.get(DEFAULT_CONFIG);
                    field.set(newConfig, new HashMap<>(map));
                }
            } catch (Exception e) {
                LOGGER.error("Exception when duplicating maps: " + field, e);
            }
        }

        return newConfig;
    }

    public static List<File> getVillagerConfigFiles() {
        // TODO: Implement virtual directory loading similar to original
        // For now return empty list
        return new ArrayList<>();
    }

    public static void loadConfigs() {
        DEFAULT_CONFIG = new VillagerConfig("default");
        // TODO: Load from config files using ParametersManager equivalent
        // ParametersManager.loadAnnotedParameterData(
        // Mill.virtualLoadingDir.getChildDirectory("villagerconfig").getChildFile("default.txt"),
        // DEFAULT_CONFIG, null, "villager config", null
        // );
        DEFAULT_CONFIG.initData();

        for (File file : getVillagerConfigFiles()) {
            if (!file.getName().equals("default.txt")) {
                String configKey = file.getName().split("\\.")[0].toLowerCase();
                VillagerConfig config = copyDefault(configKey);
                // TODO: Load config from file
                config.initData();
                villagerConfigs.put(configKey, config);
            }
        }
    }

    public VillagerConfig(String key) {
        this.key = key;
    }

    public InvItem getBestAxe(Citizen villager) {
        return this.getBestItem(this.toolsAxeSorted, villager);
    }

    public InvItem getBestConceptionFood(Building house) {
        return this.getBestItemInBuilding(this.foodsConceptionSorted, house);
    }

    public InvItem getBestHoe(Citizen villager) {
        return this.getBestItem(this.toolsHoeSorted, villager);
    }

    private InvItem getBestItem(List<InvItem> sortedItems, Citizen villager) {
        if (sortedItems == null)
            return null;
        for (InvItem invItem : sortedItems) {
            if (villager.countInv(invItem) > 0) {
                return invItem;
            }
        }
        return null;
    }

    public InvItem getBestItemByCategoryName(String categoryName, Citizen villager) {
        return this.getBestItem(this.categories.get(categoryName), villager);
    }

    private InvItem getBestItemInBuilding(List<InvItem> sortedItems, Building house) {
        if (sortedItems == null || house == null)
            return null;
        for (InvItem invItem : sortedItems) {
            if (house.countGoods(invItem) > 0) {
                return invItem;
            }
        }
        return null;
    }

    public InvItem getBestPickaxe(Citizen villager) {
        return this.getBestItem(this.toolsPickaxeSorted, villager);
    }

    public InvItem getBestShovel(Citizen villager) {
        return this.getBestItem(this.toolsShovelSorted, villager);
    }

    public InvItem getBestSword(Citizen villager) {
        return this.getBestItem(this.toolsSwordSorted, villager);
    }

    public InvItem getBestWeapon(Citizen villager) {
        return this.getBestItem(this.weaponsSorted, villager);
    }

    public InvItem getBestWeaponHandToHand(Citizen villager) {
        return this.getBestItem(this.weaponsHandToHandSorted, villager);
    }

    public InvItem getBestWeaponRanged(Citizen villager) {
        return this.getBestItem(this.weaponsRangedSorted, villager);
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        this.weapons.putAll(this.weaponsHandToHand);
        this.weapons.putAll(this.weaponsRanged);

        for (Field field : VillagerConfig.class.getFields()) {
            try {
                if (field.getType() == Map.class) {
                    ParameterizedType pt = (ParameterizedType) field.getGenericType();
                    if (pt.getActualTypeArguments()[0] == InvItem.class &&
                            pt.getActualTypeArguments()[1] == Integer.class) {

                        Map<InvItem, Integer> map = (Map<InvItem, Integer>) field.get(this);

                        // Remove items with priority <= 0
                        for (InvItem item : new HashSet<>(map.keySet())) {
                            if (map.get(item) <= 0) {
                                map.remove(item);
                            }
                        }

                        // Sort by priority (highest first)
                        List<InvItem> sortedList = new ArrayList<>(map.keySet());
                        Collections.sort(sortedList, (key1, key2) -> map.get(key2).compareTo(map.get(key1)));

                        // Set the corresponding sorted list field
                        Field listField = VillagerConfig.class.getDeclaredField(field.getName() + "Sorted");
                        listField.set(this, sortedList);

                        // Add to categories map
                        this.categories.put(field.getName().toLowerCase(), sortedList);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Exception when creating sorted list for field: " + field, e);
            }
        }
    }
}
