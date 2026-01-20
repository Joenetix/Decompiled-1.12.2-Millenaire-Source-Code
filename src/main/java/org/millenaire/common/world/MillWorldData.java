package org.millenaire.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.VillagerRecord;

import java.io.*;
import java.util.*;

/**
 * Manages all Millenaire world data including villages, buildings, villagers,
 * and profiles.
 * This is the central data hub for the mod's server-side state.
 */
public class MillWorldData extends SavedData {
    private static final String DATA_NAME = "millenaire_data";

    // Constants
    public static final String CULTURE_CONTROL = "culturecontrol_";
    public static final String PUJAS = "pujas";
    public static final String MAYANSACRIFICES = "mayansacrifices";

    // Core data storage
    private final HashMap<Point, Building> buildings = new HashMap<>();
    private final HashMap<Long, MillVillager> villagers = new HashMap<>();
    private final HashMap<Long, VillagerRecord> villagerRecords = new HashMap<>();
    public final List<String> globalTags = new ArrayList<>();

    // Village lists
    public VillageList villagesList = new VillageList();
    public VillageList loneBuildingsList = new VillageList();

    // User profiles
    public HashMap<UUID, UserProfile> profiles = new HashMap<>();

    // World reference
    public Level world;
    public File millenaireDir;
    public File saveDir;

    // State tracking
    public long lastWorldUpdate = 0L;
    public boolean millenaireEnabled = false;
    public boolean generateVillages = true;
    public boolean generateVillagesSet = false;
    private int lastForcePreloadUpdate;

    public MillWorldData() {
        // Default constructor for SavedData
    }

    public MillWorldData(Level world) {
        this();
        this.world = world;
        if (!world.isClientSide()) {
            this.saveDir = MillCommonUtilities.getWorldSaveDir(world);
            this.millenaireEnabled = true;
            this.millenaireDir = new File(this.saveDir, "millenaire");
            if (!this.millenaireDir.exists()) {
                this.millenaireDir.mkdir();
            }
        }
        setDirty();
    }

    // --- Static Factory Methods ---

    public static MillWorldData get(Level level) {
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        return serverLevel.getDataStorage().computeIfAbsent(
                tag -> load(serverLevel, tag),
                () -> new MillWorldData(serverLevel),
                DATA_NAME);
    }

    public static MillWorldData load(Level level, CompoundTag tag) {
        MillWorldData data = new MillWorldData(level);
        data.loadFromNBT(tag);
        return data;
    }

    // --- Building Management ---

    public void addBuilding(Building b, Point p) {
        this.buildings.put(p, b);
        setDirty();
    }

    public void addBuilding(Building b, BlockPos pos) {
        addBuilding(b, new Point(pos));
    }

    /**
     * Register a building using its own position.
     * Alias for addBuilding(b, b.getPos()).
     */
    public void registerBuilding(Building b) {
        if (b != null && b.getPos() != null) {
            addBuilding(b, new Point(b.getPos()));
        }
    }

    public Collection<Building> allBuildings() {
        return this.buildings.values();
    }

    public Map<Point, Building> getBuildings() {
        return this.buildings;
    }

    public boolean buildingExists(Point p) {
        return this.buildings.containsKey(p);
    }

    public Building getBuilding(Point p) {
        if (this.buildings.containsKey(p)) {
            Building b = this.buildings.get(p);
            if (b == null) {
                MillLog.error(this, "Building record for " + p + " is null.");
            }
            return b;
        }
        return null;
    }

    public Building getBuilding(BlockPos pos) {
        return getBuilding(new Point(pos));
    }

    public Building getClosestVillage(Point p) {
        int bestDistance = Integer.MAX_VALUE;
        Building bestVillage = null;

        for (Point villageCoord : this.villagesList.pos) {
            int dist = (int) p.distanceToSquared(villageCoord);
            if (bestVillage == null || dist < bestDistance) {
                Building village = this.getBuilding(villageCoord);
                if (village != null) {
                    bestVillage = village;
                    bestDistance = dist;
                }
            }
        }

        return bestVillage;
    }

    public Building getBuildingAt(BlockPos pos) {
        Point p = new Point(pos);
        if (buildings.containsKey(p)) {
            return buildings.get(p);
        }

        for (Building b : buildings.values()) {
            if (b.location != null && b.location.isInsideZone(p)) {
                return b;
            }
        }
        return null;
    }

    public List<Point> getCombinedVillagesLoneBuildings() {
        List<Point> thPosLists = new ArrayList<>(this.villagesList.pos);
        thPosLists.addAll(this.loneBuildingsList.pos);
        return thPosLists;
    }

    // --- Villager Management ---

    public Collection<MillVillager> getAllKnownVillagers() {
        return this.villagers.values();
    }

    public MillVillager getVillagerById(long id) {
        return this.villagers.get(id);
    }

    public VillagerRecord getVillagerRecordById(long villagerId) {
        return this.villagerRecords.get(villagerId);
    }

    public void registerVillager(MillVillager villager) {
        this.villagers.put(villager.getVillagerId(), villager);
    }

    public void unregisterVillager(long id) {
        this.villagers.remove(id);
    }

    public void registerVillagerRecord(VillagerRecord vr) {
        registerVillagerRecord(vr, true);
    }

    public void registerVillagerRecord(VillagerRecord vr, boolean save) {
        this.villagerRecords.put(vr.villagerId, vr);
        if (save) {
            setDirty();
        }
    }

    public void clearVillagerOfId(long id) {
        MillVillager villager = this.villagers.get(id);
        if (villager != null) {
            if (MillConfigValues.LogVillagerSpawn >= 1) {
                MillLog.major(this, "Removing villager from global list: " + villager);
            }
            this.villagers.remove(id);

            if (villager.getHouse() != null) {
                villager.getHouse().rebuildVillagerList();
            }
            if (villager.getTownHall() != null && villager.getTownHall() != villager.getHouse()) {
                villager.getTownHall().rebuildVillagerList();
            }
        }
    }

    // --- Profile Management ---

    public UserProfile getProfile(Player player) {
        UUID uuid = player.getUUID();
        if (this.profiles.containsKey(uuid)) {
            return this.profiles.get(uuid);
        }

        UserProfile profile = new UserProfile(this, player);
        this.profiles.put(profile.uuid, profile);
        return profile;
    }

    public UserProfile getProfile(UUID uuid) {
        return getProfile(uuid, null);
    }

    public UserProfile getProfile(UUID uuid, String name) {
        if (this.profiles.containsKey(uuid)) {
            UserProfile p = this.profiles.get(uuid);
            if (p.playerName == null && name != null) {
                p.playerName = name;
                p.saveProfile();
            }
            return p;
        }

        UserProfile profile = new UserProfile(this, uuid, name);
        this.profiles.put(profile.uuid, profile);
        return profile;
    }

    public VillagerRecord getVillagerRecord(long villagerId) {
        return getVillagerRecordById(villagerId);
    }

    public org.millenaire.core.Village getVillage(UUID id) {
        if (id == null)
            return null;
        for (Building b : buildings.values()) {
            if (b.isTownHall && b.getVillage() != null && b.getVillage().getVillageId().equals(id)) {
                return b.getVillage();
            }
        }
        return null;
    }

    public void checkConnections() {
        for (UserProfile profile : this.profiles.values()) {
            if (profile.connected && profile.getPlayer() == null) {
                profile.disconnectUser();
            }
        }
    }

    // --- Global Tags ---

    public boolean isGlobalTagSet(String tag) {
        return this.globalTags.contains(tag);
    }

    public void setGlobalTag(String tag) {
        if (!this.globalTags.contains(tag)) {
            this.globalTags.add(tag);
            saveGlobalTags();
            setDirty();
        }
    }

    public void clearGlobalTag(String tag) {
        if (this.globalTags.contains(tag)) {
            this.globalTags.remove(tag);
            saveGlobalTags();
            setDirty();
        }
    }

    // --- Village Registration ---

    public void registerVillageLocation(Level world, Point pos, String name, VillageType type, Culture culture,
            boolean newVillage, String playerName) {
        if (type == null || culture == null) {
            MillLog.error(null, "Attempting to register village with null type or culture: " + pos);
            return;
        }

        boolean found = false;
        for (Point p : this.villagesList.pos) {
            if (p.equals(pos)) {
                found = true;
                break;
            }
        }

        if (!found) {
            if (!type.generatedForPlayer) {
                playerName = null;
            }

            this.villagesList.addVillage(pos, name, type.key, culture.key, playerName);
            if (MillConfigValues.LogWorldGeneration >= 1) {
                MillLog.major(null, "Registering village: " + name + " / " + type + " / " + culture + " / " + pos);
            }

            saveVillageList();
        }
    }

    public void registerLoneBuildingsLocation(Level world, Point pos, String name, VillageType type, Culture culture,
            boolean newVillage, String playerName) {
        boolean found = false;
        for (Point p : this.loneBuildingsList.pos) {
            if (p.equals(pos)) {
                found = true;
                break;
            }
        }

        if (!found) {
            if (type != null && !type.generatedForPlayer) {
                playerName = null;
            }

            this.loneBuildingsList.addVillage(pos, name, type != null ? type.key : "unknown",
                    culture != null ? culture.key : "unknown", playerName);
            if (MillConfigValues.LogWorldGeneration >= 1) {
                MillLog.major(null,
                        "Registering lone building: " + name + " / " + type + " / " + culture + " / " + pos);
            }

            saveLoneBuildingsList();
        }
    }

    // --- Save/Load ---

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Save buildings
        ListTag buildingList = new ListTag();
        for (Map.Entry<Point, Building> entry : buildings.entrySet()) {
            CompoundTag buildingTag = new CompoundTag();
            entry.getValue().writeToNBT(buildingTag);
            buildingList.add(buildingTag);
        }
        tag.put("buildings", buildingList);

        // Save global tags
        ListTag tagsList = new ListTag();
        for (String t : globalTags) {
            CompoundTag tagData = new CompoundTag();
            tagData.putString("tag", t);
            tagsList.add(tagData);
        }
        tag.put("globalTags", tagsList);

        return tag;
    }

    public void loadFromNBT(CompoundTag tag) {
        // Load buildings
        if (tag.contains("buildings")) {
            ListTag list = tag.getList("buildings", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag bTag = list.getCompound(i);
                Building b = new Building(this);
                b.readFromNBT(bTag);
                b.setWorld(this.world);
                if (b.getPos() != null) {
                    buildings.put(new Point(b.getPos()), b);
                }
            }
        }

        // Load global tags
        if (tag.contains("globalTags")) {
            ListTag tagsList = tag.getList("globalTags", 10);
            for (int i = 0; i < tagsList.size(); i++) {
                CompoundTag tagData = tagsList.getCompound(i);
                globalTags.add(tagData.getString("tag"));
            }
        }
    }

    public void loadData() {
        if (!this.world.isClientSide()) {
            loadWorldConfig();
            loadVillagesAndLoneBuildingsLists();
            loadGlobalTags();
            loadBuildings();
            loadVillagerRecords();
            loadProfiles();
        }
    }

    private void loadWorldConfig() {
        this.generateVillages = MillConfigValues.generateVillages;
        File configFile = new File(this.millenaireDir, "config.txt");
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() > 0 && !line.startsWith("//")) {
                        String[] temp = line.split("=");
                        if (temp.length == 2) {
                            String key = temp[0];
                            String value = temp[1];
                            if (key.equalsIgnoreCase("generate_villages")) {
                                this.generateVillages = Boolean.parseBoolean(value);
                                this.generateVillagesSet = true;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                MillLog.printException(e);
            }
        }
    }

    private void loadVillagesAndLoneBuildingsLists() {
        File villageLog = new File(this.millenaireDir, "villages.txt");
        if (villageLog.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(villageLog))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        String[] parts = line.split(";");
                        if (parts.length >= 4) {
                            String[] coords = parts[1].split("/");
                            Point pos = new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
                                    Integer.parseInt(coords[2]));
                            String type = parts[2];
                            String culture = parts[3];
                            String generatedFor = parts.length > 4 ? parts[4] : null;

                            Culture c = Culture.getCultureByName(culture);
                            if (c != null) {
                                this.villagesList.addVillage(pos, parts[0], type, culture, generatedFor);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                MillLog.printException(e);
            }
        }

        File loneBuildingLog = new File(this.millenaireDir, "lonebuildings.txt");
        if (loneBuildingLog.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(loneBuildingLog))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        String[] parts = line.split(";");
                        if (parts.length >= 4) {
                            String[] coords = parts[1].split("/");
                            Point pos = new Point(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]),
                                    Integer.parseInt(coords[2]));
                            String type = parts[2];
                            String culture = parts[3];
                            String generatedFor = parts.length > 4 ? parts[4] : null;

                            this.loneBuildingsList.addVillage(pos, parts[0], type, culture, generatedFor);
                        }
                    }
                }
            } catch (Exception e) {
                MillLog.printException(e);
            }
        }
    }

    private void loadGlobalTags() {
        File tagsFile = new File(this.millenaireDir, "tags.txt");
        this.globalTags.clear();
        if (tagsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(tagsFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        this.globalTags.add(line.trim());
                    }
                }
            } catch (Exception e) {
                MillLog.printException(e);
            }
        }
    }

    private void loadBuildings() {
        File buildingsDir = new File(this.millenaireDir, "buildings");
        if (!buildingsDir.exists()) {
            buildingsDir.mkdir();
            return;
        }

        File[] files = buildingsDir.listFiles((dir, name) -> name.endsWith(".gz"));
        if (files == null)
            return;

        for (File file : files) {
            try (FileInputStream fis = new FileInputStream(file)) {
                CompoundTag tag = NbtIo.readCompressed(fis);
                ListTag buildingList = tag.getList("buildings", 10);
                for (int i = 0; i < buildingList.size(); i++) {
                    CompoundTag bTag = buildingList.getCompound(i);
                    Building b = new Building(this);
                    b.readFromNBT(bTag);
                    if (b.getPos() != null) {
                        this.buildings.put(new Point(b.getPos()), b);
                    }
                }
            } catch (Exception e) {
                MillLog.printException("Error loading buildings from " + file.getName(), e);
            }
        }
    }

    private void loadVillagerRecords() {
        File recordsFile = new File(this.millenaireDir, "villagerRecords.gz");
        if (!recordsFile.exists())
            return;

        try (FileInputStream fis = new FileInputStream(recordsFile)) {
            CompoundTag tag = NbtIo.readCompressed(fis);
            ListTag recordsList = tag.getList("villagersrecords", 10);
            for (int i = 0; i < recordsList.size(); i++) {
                CompoundTag vrTag = recordsList.getCompound(i);
                VillagerRecord vr = VillagerRecord.load(this, vrTag, "vr");
                if (vr != null) {
                    this.registerVillagerRecord(vr, false);
                }
            }
        } catch (Exception e) {
            MillLog.printException("Error loading villager records", e);
        }
    }

    private void loadProfiles() {
        File profilesDir = new File(this.millenaireDir, "profiles");
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
            return;
        }

        File[] profileDirs = profilesDir.listFiles(File::isDirectory);
        if (profileDirs == null)
            return;

        for (File profileDir : profileDirs) {
            if (!profileDir.isHidden()) {
                UserProfile profile = UserProfile.readProfile(this, profileDir);
                if (profile != null) {
                    this.profiles.put(profile.uuid, profile);
                }
            }
        }
    }

    // --- Save Methods ---

    private void saveGlobalTags() {
        if (millenaireDir == null)
            return;
        File tagsFile = new File(this.millenaireDir, "tags.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tagsFile))) {
            for (String tag : globalTags) {
                writer.write(tag);
                writer.newLine();
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    private void saveVillageList() {
        if (millenaireDir == null)
            return;
        File villageLog = new File(this.millenaireDir, "villages.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(villageLog))) {
            for (int i = 0; i < villagesList.pos.size(); i++) {
                Point p = villagesList.pos.get(i);
                String line = villagesList.names.get(i) + ";" +
                        p.getiX() + "/" + p.getiY() + "/" + p.getiZ() + ";" +
                        villagesList.types.get(i) + ";" +
                        villagesList.cultures.get(i);
                if (villagesList.generatedFor.get(i) != null) {
                    line += ";" + villagesList.generatedFor.get(i);
                }
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    private void saveLoneBuildingsList() {
        if (millenaireDir == null)
            return;
        File buildingLog = new File(this.millenaireDir, "lonebuildings.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(buildingLog))) {
            for (int i = 0; i < loneBuildingsList.pos.size(); i++) {
                Point p = loneBuildingsList.pos.get(i);
                String line = loneBuildingsList.names.get(i) + ";" +
                        p.getiX() + "/" + p.getiY() + "/" + p.getiZ() + ";" +
                        loneBuildingsList.types.get(i) + ";" +
                        loneBuildingsList.cultures.get(i);
                if (loneBuildingsList.generatedFor.get(i) != null) {
                    line += ";" + loneBuildingsList.generatedFor.get(i);
                }
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            MillLog.printException(e);
        }
    }

    public void saveVillagerRecords() {
        if (millenaireDir == null)
            return;
        File recordsFile = new File(this.millenaireDir, "villagerRecords.gz");
        try (FileOutputStream fos = new FileOutputStream(recordsFile)) {
            CompoundTag tag = new CompoundTag();
            ListTag recordsList = new ListTag();
            for (VillagerRecord vr : villagerRecords.values()) {
                CompoundTag vrTag = new CompoundTag();
                vr.save(vrTag, "vr");
                recordsList.add(vrTag);
            }
            tag.put("villagersrecords", recordsList);
            NbtIo.writeCompressed(tag, fos);
        } catch (Exception e) {
            MillLog.printException("Error saving villager records", e);
        }
    }

    public void saveEverything() {
        saveVillageList();
        saveLoneBuildingsList();
        saveGlobalTags();
        saveVillagerRecords();
        setDirty();
    }

    // --- Update ---

    public void update() {
        for (Building b : buildings.values()) {
            b.update();
        }
    }

    @Override
    public String toString() {
        return "MillWorldData[" + buildings.size() + " buildings, " + villagers.size() + " villagers]";
    }

    /**
     * Village list data structure for tracking registered villages.
     */
    public static class VillageList {
        public List<Point> pos = new ArrayList<>();
        public List<String> names = new ArrayList<>();
        public List<String> types = new ArrayList<>();
        public List<String> cultures = new ArrayList<>();
        public List<String> generatedFor = new ArrayList<>();

        public void addVillage(Point p, String name, String type, String culture, String generatedForPlayer) {
            this.pos.add(p);
            this.names.add(name);
            this.types.add(type);
            this.cultures.add(culture);
            this.generatedFor.add(generatedForPlayer);
        }

        public void clear() {
            pos.clear();
            names.clear();
            types.clear();
            cultures.clear();
            generatedFor.clear();
        }
    }
}
