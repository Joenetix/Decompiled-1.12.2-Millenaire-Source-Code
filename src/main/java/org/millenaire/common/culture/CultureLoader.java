package org.millenaire.common.culture;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads culture configurations from resource files.
 */
public class CultureLoader {

    private static final Logger LOGGER = LogUtils.getLogger();

    // Global culture registry
    public static List<Culture> ListCultures = new ArrayList<>();
    private static Map<String, Culture> cultures = new HashMap<>();

    public static Culture getCultureByName(String name) {
        return cultures.get(name);
    }

    public static List<Culture> getAllCultures() {
        return ListCultures;
    }

    /**
     * Load all cultures from the mod resources.
     * Expected structure: assets/millenaire/cultures/{cultureName}/...
     */
    public static void loadCultures(ResourceManager resourceManager) {
        LOGGER.info("Loading Millénaire cultures...");

        // For now, we load known cultures explicitly
        // In the future, this could scan for culture directories
        loadCulture("norman", resourceManager);
        loadCulture("japanese", resourceManager);
        loadCulture("mayan", resourceManager);
        loadCulture("indian", resourceManager);
        loadCulture("byzantine", resourceManager);
        loadCulture("inuit", resourceManager);

        LOGGER.info("Loaded {} cultures.", ListCultures.size());
    }

    private static void loadCulture(String cultureId, ResourceManager resourceManager) {
        try {
            // Check if culture already exists (statically defined)
            Culture culture = MillenaireCultures.getCulture(cultureId);
            if (culture == null) {
                // Only create new if not defined in MillenaireCultures
                culture = new Culture(cultureId, capitalizeFirst(cultureId));
                ListCultures.add(culture);
                cultures.put(cultureId, culture);
            }

            // Load name lists
            loadNameLists(culture, cultureId, resourceManager);

            // Load goods
            loadGoods(culture, cultureId, resourceManager);

            // Load shops
            loadShops(culture, cultureId, resourceManager);

            // Load wall types - CRITICAL for village walls to work!
            loadWallTypes(culture, cultureId, resourceManager);

            LOGGER.info("Loaded culture data for: {}", cultureId);
        } catch (Exception e) {
            LOGGER.warn("Could not load culture {}: {}", cultureId, e.getMessage());
        }
    }

    private static void loadNameLists(Culture culture, String cultureId, ResourceManager resourceManager) {
        // Try to load common name lists
        String[] listNames = { "malenames", "femalenames", "surnames", "villagenames" };

        for (String listName : listNames) {
            ResourceLocation loc = new ResourceLocation("millenaire",
                    "cultures/" + cultureId + "/names/" + listName + ".txt");

            try {
                if (resourceManager.getResource(loc).isPresent()) {
                    Resource resource = resourceManager.getResource(loc).get();
                    List<String> names = new ArrayList<>();

                    try (InputStream is = resource.open();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty() && !line.startsWith("//")) {
                                names.add(line);
                            }
                        }
                    }

                    culture.addNameList(listName, names);
                }
            } catch (Exception e) {
                // Name list not found, skip
            }
        }
    }

    private static void loadGoods(Culture culture, String cultureId, ResourceManager resourceManager) {
        ResourceLocation loc = new ResourceLocation("millenaire",
                "cultures/" + cultureId + "/traded_goods.txt");

        try {
            if (resourceManager.getResource(loc).isPresent()) {
                Resource resource = resourceManager.getResource(loc).get();

                try (InputStream is = resource.open();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("//")) {
                            parseGood(culture, line);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("No traded_goods.txt for culture {}", cultureId);
        }
    }

    private static void parseGood(Culture culture, String line) {
        // Format: key;itemId;buyPrice;sellPrice
        String[] parts = line.split(";");
        if (parts.length >= 2) {
            String key = parts[0].trim();
            String itemId = parts[1].trim();
            int buyPrice = parts.length > 2 ? parseInt(parts[2], 0) : 0;
            int sellPrice = parts.length > 3 ? parseInt(parts[3], 0) : 0;

            // For now, just add to trade goods list as a string
            culture.addTradeGood(itemId);
        }
    }

    private static void loadShops(Culture culture, String cultureId, ResourceManager resourceManager) {
        // Shop files would be in cultures/{cultureId}/shops/
        // For now, this is a stub that can be expanded
    }

    /**
     * Load wall types from cultures/{cultureId}/walls/*.txt
     * This is CRITICAL for village walls to appear!
     */
    private static void loadWallTypes(Culture culture, String cultureId, ResourceManager resourceManager) {
        // Known wall type files for each culture - expand as needed
        String[] wallTypeNames = { "stonewalls", "palisade", "borderposts", "mudwall" };

        LOGGER.info("[WALLS] Starting wall load for culture '{}'", cultureId);

        for (String wallTypeName : wallTypeNames) {
            ResourceLocation loc = new ResourceLocation("millenaire",
                    "cultures/" + cultureId + "/walls/" + wallTypeName + ".txt");

            LOGGER.info("[WALLS] Checking for wall config at: {}", loc);

            try {
                if (resourceManager.getResource(loc).isPresent()) {
                    Resource resource = resourceManager.getResource(loc).get();
                    WallType wallType = parseWallType(wallTypeName, culture, resource);

                    if (wallType != null) {
                        culture.addWallType(wallType);
                        LOGGER.info(
                                "[WALLS] SUCCESS: Loaded wall type '{}' for culture '{}'. Keys: wall={}, tower={}, gate={}",
                                wallTypeName, cultureId, wallType.villageWallKey, wallType.villageWallTowerKey,
                                wallType.villageWallGatewayKey);
                    } else {
                        LOGGER.error("[WALLS] FAILED: Parsed wall type '{}' was null", wallTypeName);
                    }
                } else {
                    LOGGER.warn("[WALLS] Wall config not found: {}", loc);
                }
            } catch (Exception e) {
                LOGGER.error("[WALLS] EXCEPTION loading wall '{}': {}", loc, e.getMessage(), e);
            }
        }
    }

    /**
     * Parse a wall type from its config file.
     */
    private static WallType parseWallType(String key, Culture culture, Resource resource) {
        WallType wallType = new WallType(culture, key);

        try (InputStream is = resource.open();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length < 2) {
                    continue;
                }

                String configKey = parts[0].trim().toLowerCase();
                String configValue = parts[1].trim();

                // Parse wall building plan references
                // These will be resolved to BuildingPlanSets later
                switch (configKey) {
                    case "village_wall":
                        wallType.villageWallKey = configValue;
                        break;
                    case "village_wall_gate":
                        wallType.villageWallGatewayKey = configValue;
                        break;
                    case "village_wall_corner":
                        wallType.villageWallCornerKey = configValue;
                        break;
                    case "village_wall_tower":
                        wallType.villageWallTowerKey = configValue;
                        break;
                }
            }

            return wallType;
        } catch (Exception e) {
            LOGGER.warn("Failed to parse wall type '{}': {}", key, e.getMessage());
            return null;
        }
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty())
            return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
