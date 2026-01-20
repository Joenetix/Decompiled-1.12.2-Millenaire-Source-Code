package org.millenaire.common.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.millenaire.common.advancements.GenericAdvancement;
import org.millenaire.common.advancements.MillAdvancements;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.utilities.LanguageData;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;

public class MillConfigValues {
    // === Village Activity Radius Settings ===
    public static int KeepActiveRadius = 200;
    public static int BackgroundRadius = 2000;
    public static int BanditRaidRadius = 1500;
    public static int VillageRadius = 80;
    public static int VillagersNamesDistance = 12;

    // === World Generation Settings ===
    public static boolean generateVillages = true;
    public static boolean generateLoneBuildings = true;
    public static boolean generateHamlets = false;
    public static boolean stopDefaultVillages = false;
    public static int villageSpawnCompletionMaxPercentage = 25;
    public static int villageSpawnCompletionMinDistance = 2000;
    public static int villageSpawnCompletionMaxDistance = 10000;
    public static int spawnProtectionRadius = 250;
    public static int minDistanceBetweenBuildings = 5;
    public static boolean BuildVillagePaths = true;

    // === Villager Settings ===
    public static boolean displayNames = true;
    public static int VillagersSentenceInChatDistanceClient = 0;
    public static int VillagersSentenceInChatDistanceSP = 6;
    public static boolean languageLearning = true;
    public static boolean TRAVEL_BOOK_LEARNING = true;
    public static boolean ignoreResourceCost = false;
    public static int RaidingRate = 20;

    // === JPS Pathing ===
    public static boolean jpsPathing = true;

    // === Quest Biomes ===
    public static String questBiomeForest = "forest";
    public static String questBiomeDesert = "desert";
    public static String questBiomeMountain = "mountain";

    // === Statistics and Online Features ===
    public static boolean sendStatistics = true;
    public static boolean sendAdvancementLogin = false;
    public static boolean autoConvertProfiles = false;

    // === Dev/Help Generation ===
    public static boolean generateHelpData = false;
    public static int forcePreload = 0;

    // === Legacy fields from 1.12.2 ===
    public static final boolean LOG_CHEST_LOCK = false;
    public static final boolean LOG_INVENTORY_HANDLING = false;
    public static final boolean LOG_PATHING = false;
    public static final boolean LOG_CONNECTIONS = false;
    public static final boolean LOG_PATHING_CACHE_MISS = false;
    public static final boolean LOG_PATHING_CACHE = false;
    public static boolean DEV = false;

    // === Logging Levels (0=silent, 1=major, 2=verbose) ===
    public static int LogVillage = 1;
    public static int LogBuilding = 0;
    public static int LogCulture = 0;
    public static int LogVillager = 0;
    public static int LogGeneralAI = 0;

    public static boolean EXPERIMENTAL_FEATURES = false;
    public static boolean disableNameTags = false;
    public static int LogNetwork = 0;
    public static int LogTranslation = 0;
    public static int LogWorldGeneration = 0;
    public static int LogVillagePaths = 0;
    public static int LogVillageScroll = 0;
    public static int LogSelling = 0;
    public static int LogChildren = 0;
    public static int LogConnections = 0;
    public static int LogChunkLoader = 0;
    public static int LogLumberman = 0;
    public static int LogCalculatePath = 0;
    public static int LogPathing = 0;
    public static int LogPathingCache = 0;
    public static int LogPathingAccess = 0;
    public static int LogPathingDetail = 0;
    public static int LogMiner = 0;
    public static int LogHybernation = 0;
    public static int LogVillagerSpawn = 0;
    public static int LogVillageInfo = 0;
    public static int LogBuildingPlan = 0;
    public static int LogTileEntityBuilding = 0;
    public static int LogQuest = 0;
    public static int LogWander = 0;
    public static int LogVillageList = 0;
    public static int LogData = 0;
    public static int LogOther = 0;
    public static int LogPacket = 0;
    public static int LogDisk = 0;
    public static int LogEntityUpdate = 0;
    public static int LogCitizenStats = 0;
    public static boolean logVillageGenerations = true;
    public static boolean logVillagerDeaths = true;
    public static boolean generateBuildingRes = false;
    public static boolean generateTravelBookExport = false;
    public static boolean generateTranslationGap = false;
    public static boolean loadAllLanguages = false;
    public static boolean displayStart = true;
    public static String main_language = "";
    public static String fallback_language = "en";
    public static String effective_language = "";
    public static boolean knownVillageNames = false;
    public static boolean villageNamesCapitalized = true;
    public static int maxDistanceForVillageName = 2000;
    public static int minDistanceBetweenVillages = 600;
    public static int minDistanceBetweenVillagesAndLoneBuildings = 300;
    public static int minDistanceBetweenLoneBuildings = 800;
    public static boolean builtInVillages = true;
    public static boolean builtInLoneBuildings = true;
    public static boolean villageGrid = true;
    public static int villageGridSize = 2500;
    public static int minVillageRadius = 60;
    public static int backgroundRadius = 256;
    public static int banditRaidChance = 20;
    public static int maxChildrenNumber = 10;
    public static int spawnDistance = 1500;
    public static boolean keepActive = true;
    public static boolean autoFriendship = false;
    public static List<Block> forbiddenBlocks = new ArrayList<>();
    public static int keyVillageList = 42;
    public static boolean bonusEnabled = false;
    public static String bonusCode = "";
    public static long randomUid = 0L;
    public static HashSet<String> advancementsSurvival = new HashSet<>();
    public static HashSet<String> advancementsCreative = new HashSet<>();
    public static LanguageData mainLanguage = null;
    public static LanguageData serverMainLanguage = null;
    public static LanguageData fallbackLanguage = null;
    public static LanguageData serverFallbackLanguage = null;
    public static HashMap<String, LanguageData> loadedLanguages = new HashMap<>();
    public static boolean languageLoaded = false;

    public static final LinkedHashMap<String, MillConfigParameter> configParameters = new LinkedHashMap<>();

    public static String calculateLoginMD5(String login) {
        String s = login + "qsd54fd677Q";

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(), 0, s.length());
            String md5 = String.format("%1$032x",
                    new Object[] { Integer.valueOf(new java.math.BigInteger(1, m.digest()).intValue()) });
            return md5;
        } catch (Exception var4) {
            MillLog.printException(var4);
            return "";
        }
    }

    public static void checkBonusCode(boolean serverStart) {
        if (serverStart && MillConfigValues.bonusCode.length() == 0) {
            MillCommonUtilities.BonusThread thread = new MillCommonUtilities.BonusThread(
                    Mill.proxy.getSinglePlayerName());
            thread.start();
        } else {
            String md5 = calculateLoginMD5(Mill.proxy.getSinglePlayerName());
            if (md5.equals(MillConfigValues.bonusCode)) {
                MillConfigValues.bonusEnabled = true;
            } else {
                MillConfigValues.bonusEnabled = false;
            }
        }
    }

    public static void initConfig() {
        initConfigItems();
        readConfigFile();
        MillLog.initLogFileWriter();
    }

    private static void initConfigItems() {
        // Configuration parameters setup
        configParameters.put(
                "main_language", new MillConfigParameter(getField("main_language"), "main_language", 1)
                        .setDisplayDev(true).setMaxStringLength(5));
        configParameters.put(
                "fallback_language", new MillConfigParameter(getField("fallback_language"), "fallback_language", 1)
                        .setDisplayDev(true).setMaxStringLength(5));
        configParameters.put("loadAllLanguages",
                new MillConfigParameter(getField("loadAllLanguages"), "loadAllLanguages", 0));
        configParameters.put("knownVillageNames",
                new MillConfigParameter(getField("knownVillageNames"), "knownVillageNames", 0));
        configParameters.put("villageNamesCapitalized",
                new MillConfigParameter(getField("villageNamesCapitalized"), "villageNamesCapitalized", 0));
        configParameters.put(
                "maxDistanceForVillageName",
                new MillConfigParameter(getField("maxDistanceForVillageName"), "maxDistanceForVillageName", 4));
        configParameters.put("displayStart", new MillConfigParameter(getField("displayStart"), "displayStart", 0));
        configParameters.put(
                "minDistanceBetweenVillages",
                new MillConfigParameter(getField("minDistanceBetweenVillages"), "minDistanceBetweenVillages", 4));
        configParameters.put(
                "minDistanceBetweenVillagesAndLoneBuildings",
                new MillConfigParameter(getField("minDistanceBetweenVillagesAndLoneBuildings"),
                        "minDistanceBetweenVillagesAndLoneBuildings", 4));
        configParameters.put(
                "minDistanceBetweenLoneBuildings",
                new MillConfigParameter(getField("minDistanceBetweenLoneBuildings"), "minDistanceBetweenLoneBuildings",
                        4));
        configParameters.put("builtInVillages",
                new MillConfigParameter(getField("builtInVillages"), "builtInVillages", 0));
        configParameters.put("builtInLoneBuildings",
                new MillConfigParameter(getField("builtInLoneBuildings"), "builtInLoneBuildings", 0));
        configParameters.put("villageGrid", new MillConfigParameter(getField("villageGrid"), "villageGrid", 0));
        configParameters.put("villageGridSize",
                new MillConfigParameter(getField("villageGridSize"), "villageGridSize", 4));
        configParameters.put("minVillageRadius",
                new MillConfigParameter(getField("minVillageRadius"), "minVillageRadius", 4));
        configParameters.put("backgroundRadius",
                new MillConfigParameter(getField("backgroundRadius"), "backgroundRadius", 4));
        configParameters.put("banditRaidChance",
                new MillConfigParameter(getField("banditRaidChance"), "banditRaidChance", 4));
        configParameters.put("maxChildrenNumber",
                new MillConfigParameter(getField("maxChildrenNumber"), "maxChildrenNumber", 4));
        configParameters.put("spawnDistance", new MillConfigParameter(getField("spawnDistance"), "spawnDistance", 4));
        configParameters.put("keepActive", new MillConfigParameter(getField("keepActive"), "keepActive", 0));
        // configParameters.put("keyVillageList", new
        // MillConfigParameter(getField("keyVillageList"), "keyVillageList", 3)); // Key
        // binding
        configParameters.put("autoFriendship",
                new MillConfigParameter(getField("autoFriendship"), "autoFriendship", 0));
        configParameters.put(
                "DEV", new MillConfigParameter(getField("DEV"), "dev", 0));
        // Adding log parameters
        addLogParameter("LogNetwork");
        addLogParameter("LogTranslation");
        addLogParameter("LogWorldGeneration");
        addLogParameter("LogVillagePaths");
        addLogParameter("LogVillageScroll");
        addLogParameter("LogSelling");
        addLogParameter("LogChildren");
        addLogParameter("LogConnections");
        addLogParameter("LogLumberman");
        addLogParameter("LogCalculatePath");
        addLogParameter("LogPathing");
        addLogParameter("LogPathingCache");
        addLogParameter("LogPathingAccess");
        addLogParameter("LogPathingDetail");
        addLogParameter("LogMiner");
        addLogParameter("LogHybernation");
        addLogParameter("LogVillagerSpawn");
        addLogParameter("LogVillageInfo");
        addLogParameter("LogBuildingPlan");
        addLogParameter("LogTileEntityBuilding");
        addLogParameter("LogQuest");
        addLogParameter("LogWander");
        addLogParameter("LogVillageList");
        addLogParameter("LogData");
        addLogParameter("LogOther");
        addLogParameter("LogPacket");
        addLogParameter("LogDisk");
        addLogParameter("LogEntityUpdate");
        addLogParameter("LogCitizenStats");

        configParameters.put("logVillageGenerations",
                new MillConfigParameter(getField("logVillageGenerations"), "logVillageGenerations", 0));
        configParameters.put("logVillagerDeaths",
                new MillConfigParameter(getField("logVillagerDeaths"), "logVillagerDeaths", 0));
        configParameters.put(
                "generateBuildingRes",
                new MillConfigParameter(getField("generateBuildingRes"), "generateBuildingRes", 0).setDisplayDev(true));
        configParameters.put(
                "generateTravelBookExport",
                new MillConfigParameter(getField("generateTravelBookExport"), "generateTravelBookExport", 0)
                        .setDisplayDev(true));
        configParameters.put(
                "generateTranslationGap",
                new MillConfigParameter(getField("generateTranslationGap"), "generateTranslationGap", 0)
                        .setDisplayDev(true));
        configParameters.put("disableNameTags",
                new MillConfigParameter(getField("disableNameTags"), "disableNameTags", 0).setDisplayDev(true));
        configParameters.put("bonusCode", new MillConfigParameter(getField("bonusCode"), "bonusCode", 6));
    }

    private static void addLogParameter(String key) {
        configParameters.put(key, new MillConfigParameter(getField(key), key, 5));
    }

    private static Field getField(String name) {
        try {
            return MillConfigValues.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            MillLog.printException(e);
            return null;
        }
    }

    public static void readConfigFile() {
        File file = Mill.proxy.getConfigFile();
        MillLog.major(null, "Reading config file: " + file.getAbsolutePath());
        if (file.exists()) {
            try {
                BufferedReader reader = MillCommonUtilities.getReader(file);

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("//") && line.split("=").length == 2) {
                        String key = line.split("=")[0].trim();
                        String val = line.split("=")[1].trim();
                        if (key.equals("forbiddenBlock")) {
                            for (String s : val.split(",")) {
                                Block b = Blocks.AIR; // TODO: properly parse block from string
                                if (b != Blocks.AIR) {
                                    forbiddenBlocks.add(b);
                                }
                            }
                        } else if (key.equals("randomUid")) {
                            try {
                                randomUid = Long.parseLong(val);
                            } catch (Exception var8) {
                                MillLog.printException(var8);
                            }
                        } else if (configParameters.containsKey(key)) {
                            MillConfigParameter param = configParameters.get(key);
                            param.setValueFromString(val, true);
                        }
                    }
                }

                // Stats reading omitted for brevity or implemented here
                // Note: Advancement keys reading commented out for now to ensure compile
                /*
                 * while ((line = reader.readLine()) != null) {
                 * if (line.trim().equals("[Advancements_Survival]")) {
                 * // ... logic ...
                 * }
                 * }
                 */

            } catch (Exception var9) {
                MillLog.printException(var9);
            }
        }

        if (randomUid == 0L) {
            randomUid = MillCommonUtilities.random.nextLong();
        }

        checkBonusCode(false);
        if (DEV) {
            writeBaseConfigFile();
        }

        // Compute keys
        for (GenericAdvancement adv : MillAdvancements.MILL_ADVANCEMENTS) {
            // We reference MillAdvancements here
        }

        writeConfigFile();
    }

    public static void writeBaseConfigFile() {
        // Implementation for writing base config in dev mode
    }

    public static void writeConfigFile() {
        // Implementation for writing current config (including stats)
    }
}
