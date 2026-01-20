package org.millenaire.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.MillenaireCultures;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stores data for a single Millénaire village.
 * Tracks culture, location, buildings, resources, and growth stage.
 * 
 * Uses Minecraft's SavedData system to persist across world saves/loads.
 */
public class VillageData extends SavedData {

    private final UUID villageId;
    private String cultureId;
    private BlockPos centerPos;
    private final List<PlacedBuilding> buildings;
    private final List<String> plannedBuildings; // Buildings available to be built

    // Economy and progression
    private int reputation; // Player reputation with this village (0-100)
    private int woodStock; // Basic resource tracking
    private int stoneStock;
    private int foodStock;
    private int denier; // Village currency

    // Growth tracking
    private GrowthStage growthStage;
    private long lastUpdateTick;

    // Village type tracking
    private boolean loneBuilding = false; // Whether this is a lone building (for distance checks)

    /**
     * Create a new village
     */
    public VillageData(UUID villageId, String cultureId, BlockPos centerPos) {
        this.villageId = villageId;
        this.cultureId = cultureId;
        this.centerPos = centerPos;
        this.buildings = new ArrayList<>();
        this.plannedBuildings = new ArrayList<>();
        this.reputation = 50; // Neutral starting reputation
        this.woodStock = 0;
        this.stoneStock = 0;
        this.foodStock = 0;
        this.denier = 100; // Starting currency
        this.growthStage = GrowthStage.STARTER;
        this.lastUpdateTick = 0;
        setDirty(); // Mark for saving
    }

    /**
     * Load village from NBT
     */
    public VillageData(CompoundTag nbt) {
        this.villageId = nbt.getUUID("VillageId");
        this.cultureId = nbt.getString("Culture");
        this.centerPos = BlockPos.of(nbt.getLong("CenterPos"));
        this.buildings = new ArrayList<>();
        this.plannedBuildings = new ArrayList<>();

        // Load buildings
        ListTag buildingsList = nbt.getList("Buildings", Tag.TAG_COMPOUND);
        for (int i = 0; i < buildingsList.size(); i++) {
            CompoundTag buildingTag = buildingsList.getCompound(i);
            buildings.add(PlacedBuilding.fromNBT(buildingTag));
        }

        // Load planned buildings
        ListTag plannedList = nbt.getList("PlannedBuildings", Tag.TAG_STRING);
        for (int i = 0; i < plannedList.size(); i++) {
            plannedBuildings.add(plannedList.getString(i));
        }

        // Load resources
        this.reputation = nbt.getInt("Reputation");
        this.woodStock = nbt.getInt("WoodStock");
        this.stoneStock = nbt.getInt("StoneStock");
        this.foodStock = nbt.getInt("FoodStock");
        this.denier = nbt.getInt("Denier");

        // Load growth
        this.growthStage = GrowthStage.valueOf(nbt.getString("GrowthStage"));
        this.lastUpdateTick = nbt.getLong("LastUpdateTick");
        this.loneBuilding = nbt.getBoolean("LoneBuilding");
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        nbt.putUUID("VillageId", villageId);
        nbt.putString("Culture", cultureId);
        nbt.putLong("CenterPos", centerPos.asLong());

        // Save buildings
        ListTag buildingsList = new ListTag();
        for (PlacedBuilding building : buildings) {
            buildingsList.add(building.toNBT());
        }
        nbt.put("Buildings", buildingsList);

        // Save planned buildings
        ListTag plannedList = new ListTag();
        for (String building : plannedBuildings) {
            plannedList.add(net.minecraft.nbt.StringTag.valueOf(building));
        }
        nbt.put("PlannedBuildings", plannedList);

        // Save resources
        nbt.putInt("Reputation", reputation);
        nbt.putInt("WoodStock", woodStock);
        nbt.putInt("StoneStock", stoneStock);
        nbt.putInt("FoodStock", foodStock);
        nbt.putInt("Denier", denier);

        // Save growth
        nbt.putString("GrowthStage", growthStage.name());
        nbt.putLong("LastUpdateTick", lastUpdateTick);
        nbt.putBoolean("LoneBuilding", loneBuilding);

        return nbt;
    }

    /**
     * Add a building to the village
     */
    public void addBuilding(String buildingType, BlockPos position) {
        buildings.add(new PlacedBuilding(buildingType, position));
        // Remove from planned if present
        plannedBuildings.remove(buildingType);
        setDirty();
    }

    /**
     * Add a planned building (available to be built)
     */
    public void addPlannedBuilding(String buildingType) {
        if (!plannedBuildings.contains(buildingType)) {
            plannedBuildings.add(buildingType);
            setDirty();
        }
    }

    public List<String> getPlannedBuildings() {
        return plannedBuildings;
    }

    /**
     * Update village (called periodically)
     */
    private final transient List<org.millenaire.common.village.Building> activeBuildings = new ArrayList<>();

    /**
     * Update village (called periodically)
     */
    public void tick(ServerLevel level, long currentTick) {
        this.lastUpdateTick = currentTick;
        // com.mojang.logging.LogUtils.getLogger().info("VillageData tick. Buildings:
        // {}, Active: {}", buildings.size(), activeBuildings.size());

        // Initialize active buildings if needed
        if (activeBuildings.isEmpty() && !buildings.isEmpty()) {
            org.millenaire.common.world.MillWorldData mw = org.millenaire.common.world.MillWorldData.get(level);
            Culture c = getCulture();

            for (PlacedBuilding pb : buildings) {
                org.millenaire.common.village.Building b = new org.millenaire.common.village.Building(mw);
                b.setWorld(level);
                b.pos = pb.getPosition();
                b.culture = c;
                // b.villageType = ...; // Optional/TODO
                b.isActive = true;
                b.isTownHall = pb.getBuildingType().contains("townhall"); // Rough check

                // Load residents
                for (CompoundTag residentTag : pb.getInitialResidents()) {
                    VillagerRecord vr = VillagerRecord.load(mw, residentTag, "vr");
                    if (vr != null) {
                        b.vrecords.put(vr.villagerId, vr);
                    }
                }

                activeBuildings.add(b);
            }
        }

        // Update active buildings
        for (org.millenaire.common.village.Building b : activeBuildings) {
            b.update();
        }

        // TODO Phase 4: Resource generation, villager AI updates, building construction

        setDirty();
    }

    /**
     * Get the culture for this village
     */
    public Culture getCulture() {
        return MillenaireCultures.getCulture(cultureId);
    }

    // Getters and setters
    public UUID getVillageId() {
        return villageId;
    }

    public String getCultureId() {
        return cultureId;
    }

    public BlockPos getCenterPos() {
        return centerPos;
    }

    /**
     * Alias for getCenterPos() - used by spawn protection checks.
     */
    public BlockPos getCenter() {
        return centerPos;
    }

    /**
     * Whether this is a lone building (uses different distance rules).
     */
    public boolean isLoneBuilding() {
        return loneBuilding;
    }

    public void setLoneBuilding(boolean loneBuilding) {
        this.loneBuilding = loneBuilding;
        setDirty();
    }

    public List<PlacedBuilding> getBuildings() {
        return buildings;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = Math.max(0, Math.min(100, reputation));
        setDirty();
    }

    public void addReputation(int amount) {
        setReputation(reputation + amount);
    }

    public int getWoodStock() {
        return woodStock;
    }

    public void addWood(int amount) {
        this.woodStock += amount;
        setDirty();
    }

    public int getStoneStock() {
        return stoneStock;
    }

    public void addStone(int amount) {
        this.stoneStock += amount;
        setDirty();
    }

    public int getFoodStock() {
        return foodStock;
    }

    public void addFood(int amount) {
        this.foodStock += amount;
        setDirty();
    }

    public int getDenier() {
        return denier;
    }

    public void addDenier(int amount) {
        this.denier += amount;
        setDirty();
    }

    public GrowthStage getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(GrowthStage stage) {
        this.growthStage = stage;
        setDirty();
    }

    /**
     * Represents a building that has been placed in the village
     */
    public static class PlacedBuilding {
        private final String buildingType; // e.g., "norman/buildings/houses/farm"
        private final BlockPos position;
        private int level; // Upgrade level (0 = initial, 1+ = upgrades)
        private boolean underConstruction;
        private final java.util.List<CompoundTag> initialResidents = new java.util.ArrayList<>();

        public PlacedBuilding(String buildingType, BlockPos position) {
            this.buildingType = buildingType;
            this.position = position;
            this.level = 0;
            this.underConstruction = false;
        }

        public PlacedBuilding(String buildingType, BlockPos position, int level, boolean underConstruction) {
            this.buildingType = buildingType;
            this.position = position;
            this.level = level;
            this.underConstruction = underConstruction;
        }

        public CompoundTag toNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", buildingType);
            tag.putLong("Position", position.asLong());
            tag.putInt("Level", level);
            tag.putBoolean("UnderConstruction", underConstruction);

            ListTag residents = new ListTag();
            for (CompoundTag r : initialResidents) {
                residents.add(r);
            }
            tag.put("InitialResidents", residents);

            return tag;
        }

        public static PlacedBuilding fromNBT(CompoundTag tag) {
            PlacedBuilding pb = new PlacedBuilding(
                    tag.getString("Type"),
                    BlockPos.of(tag.getLong("Position")),
                    tag.getInt("Level"),
                    tag.getBoolean("UnderConstruction"));

            if (tag.contains("InitialResidents")) {
                ListTag residents = tag.getList("InitialResidents", 10);
                for (int i = 0; i < residents.size(); i++) {
                    pb.initialResidents.add(residents.getCompound(i));
                }
            }
            return pb;
        }

        public void addInitialResident(CompoundTag residentTag) {
            initialResidents.add(residentTag);
        }

        public java.util.List<CompoundTag> getInitialResidents() {
            return initialResidents;
        }

        public String getBuildingType() {
            return buildingType;
        }

        public BlockPos getPosition() {
            return position;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public boolean isUnderConstruction() {
            return underConstruction;
        }

        public void setUnderConstruction(boolean underConstruction) {
            this.underConstruction = underConstruction;
        }
    }

    /**
     * Add a resident to a building in the village data
     */
    public void addResident(BlockPos buildingPos, VillagerRecord vr) {
        for (PlacedBuilding pb : buildings) {
            if (pb.getPosition().equals(buildingPos)) {
                CompoundTag tag = new CompoundTag();
                vr.save(tag, "vr");
                pb.addInitialResident(tag);
                setDirty();
                return;
            }
        }
        com.mojang.logging.LogUtils.getLogger().warn("Tried to add resident to non-existent building at {}",
                buildingPos);
    }

    // New helper method to get placed building by pos
    public PlacedBuilding getPlacedBuildingAt(BlockPos pos) {
        for (PlacedBuilding pb : buildings) {
            if (pb.getPosition().equals(pos)) {
                return pb;
            }
        }
        return null;
    }

    /**
     * Village growth stages determine what buildings can be built
     */
    public enum GrowthStage {
        STARTER, // Just founded, 1-3 buildings
        HAMLET, // Small village, 4-8 buildings
        VILLAGE, // Medium village, 9-15 buildings
        TOWN, // Large town, 16-25 buildings
        CITY // Full city, 25+ buildings
    }
}
