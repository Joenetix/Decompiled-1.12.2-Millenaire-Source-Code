package org.millenaire.common.world;

import net.minecraft.world.entity.player.Player;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.quest.QuestInstance;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

import java.io.*;
import java.util.*;

/**
 * User profile containing per-player data: reputation, tags, quests, and
 * unlocked content.
 */
public class UserProfile {
    // Tag constants
    public static final String CROP_PLANTING = "cropplanting_";
    public static final String HUNTING_DROP = "huntingdrop_";

    // Reputation constants
    private static final int CULTURE_MAX_REPUTATION = 4096;
    private static final int CULTURE_MIN_REPUTATION = -640;
    public static final int FRIEND_OF_THE_VILLAGE = 8192;
    public static final int ONE_OF_US = 32768;

    // Update type constants
    public static final int UPDATE_ALL = 1;
    public static final int UPDATE_REPUTATION = 2;
    public static final int UPDATE_DIPLOMACY = 3;
    public static final int UPDATE_ACTIONDATA = 4;
    public static final int UPDATE_TAGS = 5;
    public static final int UPDATE_LANGUAGE = 6;
    public static final int UPDATE_GLOBAL_TAGS = 7;
    public static final int UPDATE_UNLOCKED_CONTENT = 8;

    // Unlocked content type constants
    public static final int UNLOCKED_BUILDING = 1;
    public static final int UNLOCKED_VILLAGE = 2;
    public static final int UNLOCKED_VILLAGER = 3;
    public static final int UNLOCKED_TRADE_GOOD = 4;

    // Unlocked content
    private final Set<String> unlockedVillagers = new HashSet<>();
    private final Set<String> unlockedVillages = new HashSet<>();
    private final Set<String> unlockedBuildings = new HashSet<>();
    private final Set<String> unlockedTradeGoods = new HashSet<>();

    // Reputation tracking
    private final HashMap<Point, Integer> villageReputations = new HashMap<>();
    private final HashMap<Point, Byte> villageDiplomacy = new HashMap<>();
    private final HashMap<String, Integer> cultureReputations = new HashMap<>();
    private final HashMap<String, Integer> cultureLanguages = new HashMap<>();

    // Profile data
    private final List<String> profileTags = new ArrayList<>();
    private final HashMap<String, String> actionData = new HashMap<>();

    // Identity
    public UUID uuid;
    public String playerName;

    // Quest tracking
    public List<QuestInstance> questInstances = new ArrayList<>();
    public HashMap<Long, QuestInstance> villagersInQuests = new HashMap<>();

    // Connection state
    public boolean connected = false;
    public boolean donationActivated = false;

    // Reference
    private final MillWorldData mw;

    // --- Constructors ---

    public UserProfile(MillWorldData mw, Player player) {
        this.uuid = player.getUUID();
        this.playerName = player.getName().getString();
        this.mw = mw;
    }

    public UserProfile(MillWorldData mw, UUID uuid, String name) {
        this.uuid = uuid;
        this.playerName = name;
        this.mw = mw;
    }

    // --- Static Factory ---

    public static UserProfile readProfile(MillWorldData world, File dir) {
        String key = dir.getName();
        boolean legacyProfile = key.split("-").length != 5;
        UserProfile profile;

        if (legacyProfile) {
            // Legacy profiles need UUID lookup
            profile = new UserProfile(world, UUID.randomUUID(), key);
        } else {
            UUID uuid = UUID.fromString(key);
            profile = new UserProfile(world, uuid, null);
        }

        // Load profile data
        profile.loadProfileConfig(new File(profile.getDir(), "config.txt"));
        profile.loadProfileTags();
        profile.loadActionData(new File(profile.getDir(), "actiondata.txt"));
        profile.loadUnlockedContent(new File(profile.getDir(), "unlockedcontent.txt"));

        return profile;
    }

    // --- Directory ---

    private File getDir() {
        File dir = new File(new File(this.mw.millenaireDir, "profiles"), this.uuid.toString());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    // --- Player Access ---

    public Player getPlayer() {
        if (mw.world == null)
            return null;
        return mw.world.getPlayerByUUID(uuid);
    }

    // --- Tags ---

    public boolean isTagSet(String tag) {
        return this.profileTags.contains(tag);
    }

    public void setTag(String tag) {
        if (!this.profileTags.contains(tag)) {
            this.profileTags.add(tag);
            saveProfileTags();
        }
    }

    public void clearTag(String tag) {
        if (this.profileTags.contains(tag)) {
            this.profileTags.remove(tag);
            saveProfileTags();
        }
    }

    // --- Action Data ---

    public String getActionData(String key) {
        return this.actionData.get(key);
    }

    public void setActionData(String key, String value) {
        this.actionData.put(key, value);
        saveActionData();
    }

    public void clearActionData(String key) {
        if (this.actionData.containsKey(key)) {
            this.actionData.remove(key);
            saveActionData();
        }
    }

    // --- Reputation ---

    public int getReputation(Point p) {
        if (p != null && this.villageReputations.containsKey(p)) {
            return this.villageReputations.get(p);
        }
        return 0;
    }

    public int getReputation(Building b) {
        if (b == null)
            return 0;

        int rep = 0;
        Point pos = b.getPos() != null ? new Point(b.getPos()) : null;

        if (pos != null && this.villageReputations.containsKey(pos)) {
            rep = this.villageReputations.get(pos);
        }

        if (b.culture != null && this.cultureReputations.containsKey(b.culture.key)) {
            rep += this.cultureReputations.get(b.culture.key);
        }

        return rep;
    }

    public void adjustReputation(Point p, int change) {
        int newReputation = change;
        if (this.villageReputations.containsKey(p)) {
            newReputation += this.villageReputations.get(p);
        }
        this.villageReputations.put(p, newReputation);
        saveProfile();
    }

    public void adjustReputation(Building b, int change) {
        if (b == null || b.getPos() == null)
            return;

        Point pos = new Point(b.getPos());
        int newReputation = change;

        if (this.villageReputations.containsKey(pos)) {
            newReputation = change + this.villageReputations.get(pos);
        }

        this.villageReputations.put(pos, newReputation);

        // Culture reputation (10%)
        if (b.culture != null) {
            int cultureRep = 0;
            if (this.cultureReputations.containsKey(b.culture.key)) {
                cultureRep = this.cultureReputations.get(b.culture.key);
            }

            cultureRep += change / 10;
            cultureRep = Math.max(CULTURE_MIN_REPUTATION, cultureRep);
            cultureRep = Math.min(CULTURE_MAX_REPUTATION, cultureRep);
            this.cultureReputations.put(b.culture.key, cultureRep);
        }

        saveProfileConfig();
    }

    public int getCultureReputation(String key) {
        return this.cultureReputations.getOrDefault(key, 0);
    }

    // --- Diplomacy ---

    public int getDiplomacyPoints(Building b) {
        if (b == null || b.getPos() == null)
            return 0;
        Point pos = new Point(b.getPos());
        return this.villageDiplomacy.getOrDefault(pos, (byte) 0);
    }

    public void adjustDiplomacyPoint(Building b, int change) {
        if (b == null || b.getPos() == null)
            return;

        Point pos = new Point(b.getPos());
        int dp = this.villageDiplomacy.getOrDefault(pos, (byte) 0);

        dp += change;
        dp = Math.max(0, Math.min(5, dp));

        this.villageDiplomacy.put(pos, (byte) dp);
        saveProfileConfig();
    }

    // --- Language ---

    public int getCultureLanguageKnowledge(String key) {
        return this.cultureLanguages.getOrDefault(key, 0);
    }

    public void adjustLanguage(String culture, int change) {
        int current = this.cultureLanguages.getOrDefault(culture, 0);
        this.cultureLanguages.put(culture, current + change);
        saveProfileConfig();
    }

    // --- Unlocked Content ---

    public boolean isBuildingUnlocked(Culture culture, BuildingPlanSet planSet) {
        String combinedKey = culture.key + "_" + planSet.key;
        return this.unlockedBuildings.contains(combinedKey);
    }

    public boolean isVillageUnlocked(Culture culture, VillageType villageType) {
        String combinedKey = culture.key + "_" + villageType.key;
        return this.unlockedVillages.contains(combinedKey);
    }

    public boolean isVillagerUnlocked(Culture culture, VillagerType villagerType) {
        String combinedKey = culture.key + "_" + villagerType.key;
        return this.unlockedVillagers.contains(combinedKey);
    }

    public boolean isTradeGoodUnlocked(Culture culture, TradeGood tradeGood) {
        String combinedKey = culture.key + "_" + tradeGood.key;
        return this.unlockedTradeGoods.contains(combinedKey);
    }

    public boolean isCultureUnlocked(Culture culture) {
        String prefix = culture.key + "_";
        for (String key : this.unlockedBuildings) {
            if (key.startsWith(prefix))
                return true;
        }
        for (String key : this.unlockedVillagers) {
            if (key.startsWith(prefix))
                return true;
        }
        for (String key : this.unlockedVillages) {
            if (key.startsWith(prefix))
                return true;
        }
        for (String key : this.unlockedTradeGoods) {
            if (key.startsWith(prefix))
                return true;
        }
        return false;
    }

    public void unlockBuilding(Culture culture, BuildingPlanSet planSet) {
        String combinedKey = culture.key + "_" + planSet.key;
        if (!this.unlockedBuildings.contains(combinedKey)) {
            this.unlockedBuildings.add(combinedKey);
            saveUnlockedContent();
        }
    }

    public void unlockVillage(Culture culture, VillageType villageType) {
        String combinedKey = culture.key + "_" + villageType.key;
        if (!this.unlockedVillages.contains(combinedKey)) {
            this.unlockedVillages.add(combinedKey);
            saveUnlockedContent();
        }
    }

    public void unlockVillager(Culture culture, VillagerType villagerType) {
        String combinedKey = culture.key + "_" + villagerType.key;
        if (!this.unlockedVillagers.contains(combinedKey)) {
            this.unlockedVillagers.add(combinedKey);
            saveUnlockedContent();
        }
    }

    public void unlockTradeGood(Culture culture, TradeGood tradeGood) {
        String combinedKey = culture.key + "_" + tradeGood.key;
        if (!this.unlockedTradeGoods.contains(combinedKey)) {
            this.unlockedTradeGoods.add(combinedKey);
            saveUnlockedContent();
        }
    }

    public void unlockTradeGoods(Culture culture, List<TradeGood> goods) {
        boolean changed = false;
        for (TradeGood tg : goods) {
            String combinedKey = culture.key + "_" + tg.key;
            if (!this.unlockedTradeGoods.contains(combinedKey)) {
                this.unlockedTradeGoods.add(combinedKey);
                changed = true;
            }
        }
        if (changed) {
            saveUnlockedContent();
        }
    }

    // --- Connection Management ---

    public void connectUser() {
        this.connected = true;
    }

    public void disconnectUser() {
        this.connected = false;
    }

    // --- Load Methods ---

    private void loadProfileConfig(File configFile) {
        if (configFile == null || !configFile.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0 && !line.startsWith("//")) {
                    String[] temp = line.split("=");
                    if (temp.length == 2) {
                        String key = temp[0];
                        String value = temp[1];

                        if (key.equalsIgnoreCase("culture_reputation")) {
                            String[] parts = value.split(",");
                            if (parts.length == 2) {
                                this.cultureReputations.put(parts[0], Integer.parseInt(parts[1]));
                            }
                        } else if (key.equalsIgnoreCase("culture_language")) {
                            String[] parts = value.split(",");
                            if (parts.length == 2) {
                                this.cultureLanguages.put(parts[0], Integer.parseInt(parts[1]));
                            }
                        } else if (key.equalsIgnoreCase("village_reputations")) {
                            String[] parts = value.split(",");
                            if (parts.length == 2) {
                                Point p = new Point(parts[0]);
                                this.villageReputations.put(p, Integer.parseInt(parts[1]));
                            }
                        } else if (key.equalsIgnoreCase("village_diplomacy")) {
                            String[] parts = value.split(",");
                            if (parts.length == 2) {
                                Point p = new Point(parts[0]);
                                this.villageDiplomacy.put(p, (byte) Integer.parseInt(parts[1]));
                            }
                        } else if (key.equalsIgnoreCase("player_name")) {
                            this.playerName = value.trim();
                        } else if (key.equalsIgnoreCase("donation_mode")) {
                            this.donationActivated = Boolean.parseBoolean(value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    private void loadProfileTags() {
        File tagsFile = new File(this.getDir(), "tags.txt");
        this.profileTags.clear();
        if (!tagsFile.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(tagsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    this.profileTags.add(line.trim());
                }
            }
        } catch (Exception e) {
            MillLog.printException(e);
        }
    }

    private void loadActionData(File dataFile) {
        this.actionData.clear();
        if (dataFile == null || !dataFile.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        this.actionData.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (Exception e) {
            MillLog.printException(e);
        }
    }

    private void loadUnlockedContent(File dataFile) {
        this.unlockedVillagers.clear();
        this.unlockedVillages.clear();
        this.unlockedBuildings.clear();
        this.unlockedTradeGoods.clear();

        if (dataFile == null || !dataFile.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String type = parts[0];
                        String key = parts[1];
                        switch (type) {
                            case "villager" -> this.unlockedVillagers.add(key);
                            case "village" -> this.unlockedVillages.add(key);
                            case "building" -> this.unlockedBuildings.add(key);
                            case "tradegood" -> this.unlockedTradeGoods.add(key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            MillLog.printException(e);
        }
    }

    // --- Save Methods ---

    public void saveProfile() {
        saveProfileConfig();
        saveProfileTags();
        saveActionData();
        saveUnlockedContent();
    }

    private void saveProfileConfig() {
        File configFile = new File(this.getDir(), "config.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            if (playerName != null) {
                writer.write("player_name=" + playerName);
                writer.newLine();
            }
            writer.write("donation_mode=" + donationActivated);
            writer.newLine();

            for (Map.Entry<String, Integer> entry : cultureReputations.entrySet()) {
                writer.write("culture_reputation=" + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            for (Map.Entry<String, Integer> entry : cultureLanguages.entrySet()) {
                writer.write("culture_language=" + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            for (Map.Entry<Point, Integer> entry : villageReputations.entrySet()) {
                writer.write("village_reputations=" + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }

            for (Map.Entry<Point, Byte> entry : villageDiplomacy.entrySet()) {
                writer.write("village_diplomacy=" + entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    private void saveProfileTags() {
        File tagsFile = new File(this.getDir(), "tags.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tagsFile))) {
            for (String tag : profileTags) {
                writer.write(tag);
                writer.newLine();
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    private void saveActionData() {
        File dataFile = new File(this.getDir(), "actiondata.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (Map.Entry<String, String> entry : actionData.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    private void saveUnlockedContent() {
        File dataFile = new File(this.getDir(), "unlockedcontent.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (String key : unlockedVillagers) {
                writer.write("villager:" + key);
                writer.newLine();
            }
            for (String key : unlockedVillages) {
                writer.write("village:" + key);
                writer.newLine();
            }
            for (String key : unlockedBuildings) {
                writer.write("building:" + key);
                writer.newLine();
            }
            for (String key : unlockedTradeGoods) {
                writer.write("tradegood:" + key);
                writer.newLine();
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    @Override
    public String toString() {
        return "UserProfile[" + (playerName != null ? playerName : uuid.toString()) + "]";
    }
}
