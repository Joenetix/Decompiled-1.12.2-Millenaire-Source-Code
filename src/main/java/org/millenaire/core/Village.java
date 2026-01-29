package org.millenaire.core;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import org.millenaire.common.village.Building;
import org.millenaire.entities.Citizen;
import org.millenaire.resources.ResourceInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Iterator;
import org.millenaire.common.culture.VillageType;

/**
 * Represents a Millenaire village - the core unit of the mod.
 * 
 * A village manages:
 * - Citizens and their jobs
 * - Buildings and construction
 * - Resources and production
 * - Autonomous development
 */
public class Village {
    private final UUID villageId;
    private final BlockPos center;
    private final String culture;
    private final ServerLevel level;

    private final List<UUID> citizenIds = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();
    private final ResourceInventory resources = new ResourceInventory();

    // VillageType reference for configuration
    private VillageType villageType;
    private String villageName = "Unknown Village";

    // Path building
    private final List<BlockPos> pathPoints = new ArrayList<>();
    private final List<BlockPos> toBuildPaths = new ArrayList<>();

    // Simulation state
    private boolean autonomous = true;
    private int villageLevel = 0;
    private int activeConstructions = 0;
    private boolean underRaid = false;
    private long lastTickTime = 0;

    // Reputation tracking per player
    private final Map<UUID, Integer> playerReputation = new HashMap<>();

    // Tags accumulated from buildings
    private final List<String> villageTags = new ArrayList<>();

    // Raid Manager
    public final org.millenaire.common.village.RaidManager raidManager;

    // Diplomacy Manager
    public final org.millenaire.common.village.DiplomacyManager diplomacyManager;

    public Village(UUID villageId, BlockPos center, String culture, ServerLevel level) {
        this.villageId = villageId;
        this.center = center;
        this.culture = culture;
        this.level = level;
        this.raidManager = new org.millenaire.common.village.RaidManager(this);
        this.diplomacyManager = new org.millenaire.common.village.DiplomacyManager(this);
    }

    /**
     * Main village update tick - called every game tick
     */
    public void tick() {
        if (!autonomous)
            return;

        // Run village update once per second (approx)
        if (level.getGameTime() % 20 != 0)
            return;

        this.setLastTickTime(level.getGameTime());

        // Update citizens (find jobs, work, etc. - largely handled by goals, but we can
        // do high level assignment)
        tickCitizens();

        // Update all buildings
        for (Iterator<Building> iterator = buildings.iterator(); iterator.hasNext();) {
            Building b = iterator.next();
            b.update();
        }

        // Process building queue
        tickConstruction();

        // Update production
        tickProduction();

        // Check for upgrades
        checkUpgrades();

        // Update Raids
        raidManager.tick();
        diplomacyManager.tick();

        // Randomly trigger raid?
        // Chance: 1 in 500 update ticks (every ~8 minutes check)
        // Only at night
        if (!raidManager.active && level.isNight() && level.random.nextInt(500) == 0) {
            raidManager.triggerRaid();
        }

        // Decay bad reputation slowly (every minute)
        if (level.getGameTime() % 1200 == 0) {
            for (Map.Entry<UUID, Integer> entry : playerReputation.entrySet()) {
                if (entry.getValue() < 0) {
                    entry.setValue(entry.getValue() + 1);
                }
            }
        }
    }

    private void tickCitizens() {
        // Citizens will handle their own AI in their entity tick
        // Here we just manage assignments
        assignJobsToCitizens();
    }

    private void tickConstruction() {
        // Process any ongoing construction
        // Builders will handle actual block placement
    }

    private void tickProduction() {
        // Update resource production from farms, mines, etc.
    }

    private void checkUpgrades() {
        // Check if buildings can upgrade based on resources
    }

    private void assignJobsToCitizens() {
        // Match unemployed citizens to available jobs
    }

    /**
     * Add a citizen to this village
     */
    public void addCitizen(UUID citizenId) {
        citizenIds.add(citizenId);
    }

    /**
     * Add a building to this village
     */
    public void addBuilding(Building building) {
        buildings.add(building);
    }

    /**
     * Check if village can afford to build/upgrade
     */
    public boolean hasResources(ResourceInventory required) {
        return resources.hasAll(required);
    }

    /**
     * Deduct resources for construction
     */
    public void consumeResources(ResourceInventory cost) {
        resources.removeAll(cost);
    }

    /**
     * Add resources from production
     */
    public void addResources(ResourceInventory produced) {
        resources.addAll(produced);
    }

    // Getters
    public UUID getVillageId() {
        return villageId;
    }

    public BlockPos getCenter() {
        return center;
    }

    public String getCulture() {
        return culture;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public boolean isAutonomous() {
        return autonomous;
    }

    public int getVillageLevel() {
        return villageLevel;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public ResourceInventory getResources() {
        return resources;
    }

    // Setters
    public void setAutonomous(boolean autonomous) {
        this.autonomous = autonomous;
    }

    public void setVillageLevel(int level) {
        this.villageLevel = level;
    }

    // New field accessors
    public VillageType getVillageType() {
        return villageType;
    }

    public void setVillageType(VillageType type) {
        this.villageType = type;
    }

    public String getVillageName() {
        return villageName;
    }

    public void setVillageName(String name) {
        this.villageName = name;
    }

    public int getActiveConstructions() {
        return activeConstructions;
    }

    public void setActiveConstructions(int count) {
        this.activeConstructions = count;
    }

    public boolean isUnderRaid() {
        return underRaid;
    }

    public void setUnderRaid(boolean raid) {
        this.underRaid = raid;
    }

    public long getLastTickTime() {
        return lastTickTime;
    }

    public void setLastTickTime(long time) {
        this.lastTickTime = time;
    }

    public int getPlayerReputation(UUID playerId) {
        return playerReputation.getOrDefault(playerId, 0);
    }

    public void setPlayerReputation(UUID playerId, int rep) {
        playerReputation.put(playerId, rep);
    }

    public void addPlayerReputation(UUID playerId, int delta) {
        int current = getPlayerReputation(playerId);
        setPlayerReputation(playerId, current + delta);
    }

    public List<String> getVillageTags() {
        return villageTags;
    }

    public void addVillageTag(String tag) {
        if (!villageTags.contains(tag)) {
            villageTags.add(tag);
        }
    }

    public boolean hasVillageTag(String tag) {
        return villageTags.contains(tag);
    }

    public String getFormattedName() {
        // e.g. "VillageName (Culture)"
        // For now just return name
        return villageName;
    }

    public String getVillageQualifiedName() {
        return villageName + " (" + culture + ")";
    }

    public int getPopulationSize() {
        return citizenIds.size();
    }

    // Path management
    public void addPathPoint(BlockPos pos) {
        if (!pathPoints.contains(pos)) {
            pathPoints.add(pos);
            toBuildPaths.add(pos);
        }
    }

    public BlockPos getNextPathToBuild(BlockPos near) {
        // Simple heuristic: find nearest unbuilt path
        BlockPos nearest = null;
        double minDst = Double.MAX_VALUE;

        // Validate toBuildPaths
        List<BlockPos> done = new ArrayList<>();

        for (BlockPos p : toBuildPaths) {
            // Check if already built (assuming path material is gravel or matching village
            // type)
            // For now, just check if it's NOT a path block.
            // In reality, we should check against VillageType path materials.
            // Simplified: if it's air or grass, it needs building.
            // Actually, we should simpler return the pos and let the Goal decide if it
            // needs building.

            double dst = p.distSqr(near);
            if (dst < minDst) {
                minDst = dst;
                nearest = p;
            }
        }

        return nearest;
    }

    public void markPathBuilt(BlockPos pos) {
        toBuildPaths.remove(pos);
    }

    /**
     * Save village data to NBT
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("VillageId", villageId);
        tag.putLong("Center", center.asLong());
        tag.putString("Culture", culture);
        tag.putBoolean("Autonomous", autonomous);
        tag.putInt("VillageLevel", villageLevel);

        // Save citizen IDs
        ListTag citizenList = new ListTag();
        for (UUID citizenId : citizenIds) {
            CompoundTag citizenTag = new CompoundTag();
            citizenTag.putUUID("Id", citizenId);
            citizenList.add(citizenTag);
        }
        tag.put("Citizens", citizenList);

        // Save buildings
        ListTag buildingList = new ListTag();
        for (Building building : buildings) {
            buildingList.add(building.save());
        }
        tag.put("Buildings", buildingList);

        // Save Raid Data
        CompoundTag raidTag = new CompoundTag();
        raidManager.writeToNBT(raidTag);
        tag.put("RaidData", raidTag);

        // Save Diplomacy
        CompoundTag diplomacyTag = new CompoundTag();
        diplomacyManager.writeToNBT(diplomacyTag);
        tag.put("Diplomacy", diplomacyTag);

        // Save resources
        tag.put("Resources", resources.save());

        // Save Paths
        ListTag pathList = new ListTag();
        for (BlockPos p : pathPoints) {
            pathList.add(net.minecraft.nbt.LongTag.valueOf(p.asLong()));
        }
        tag.put("PathPoints", pathList);

        ListTag toBuildList = new ListTag();
        for (BlockPos p : toBuildPaths) {
            toBuildList.add(net.minecraft.nbt.LongTag.valueOf(p.asLong()));
        }
        tag.put("ToBuildPaths", toBuildList);

        return tag;
    }

    /**
     * Load village data from NBT
     */
    public static Village load(CompoundTag tag, ServerLevel level) {
        UUID villageId = tag.getUUID("VillageId");
        BlockPos center = BlockPos.of(tag.getLong("Center"));
        String culture = tag.getString("Culture");

        Village village = new Village(villageId, center, culture, level);
        village.setAutonomous(tag.getBoolean("Autonomous"));
        village.setVillageLevel(tag.getInt("VillageLevel"));

        // Load citizens
        ListTag citizenList = tag.getList("Citizens", Tag.TAG_COMPOUND);
        for (Tag citizenTag : citizenList) {
            CompoundTag ct = (CompoundTag) citizenTag;
            village.addCitizen(ct.getUUID("Id"));
        }

        // Load buildings
        ListTag buildingList = tag.getList("Buildings", Tag.TAG_COMPOUND);
        for (Tag buildingTag : buildingList) {
            Building building = Building.load((CompoundTag) buildingTag, village);
            village.addBuilding(building);
        }

        // Load Raid Data
        if (tag.contains("RaidData")) {
            village.raidManager.readFromNBT(tag.getCompound("RaidData"));
        }

        // Load Diplomacy
        if (tag.contains("Diplomacy")) {
            village.diplomacyManager.readFromNBT(tag.getCompound("Diplomacy"));
        }

        // Load resources
        village.resources.load(tag.getCompound("Resources"));

        // Load Paths
        if (tag.contains("PathPoints", Tag.TAG_LIST)) {
            ListTag pathList = tag.getList("PathPoints", Tag.TAG_LONG);
            for (Tag t : pathList) {
                // LongTag is not directly exposed as Tag subclass in logic sometimes?
                // Using generic getter
                village.pathPoints.add(BlockPos.of(((net.minecraft.nbt.LongTag) t).getAsLong()));
            }
        }
        // Actually, better to iterate by index if unsure about casting in loop
        if (tag.contains("PathPoints", Tag.TAG_LIST)) {
            ListTag pl = tag.getList("PathPoints", 6); // 6 = LONG
            for (int i = 0; i < pl.size(); i++) {
                // getLong not on ListTag?
                // ListTag has generic get.
                // let's use the explicit loop logic:
            }
            // Re-implementing correctly:
        }

        village.pathPoints.clear();
        if (tag.contains("PathPoints")) {
            long[] longs = tag.getLongArray("PathPoints"); // Optimization: Use LongArrayTag instead?
            // ListTag of LongTag is different from LongArrayTag.
            // I wrote ListTag above.
            ListTag pl = tag.getList("PathPoints", 4); // 4 = LONG? No, 4 is LONG.
            // Tag.TAG_LONG is 4.
            for (int i = 0; i < pl.size(); i++) {
                village.pathPoints.add(BlockPos.of(((net.minecraft.nbt.LongTag) pl.get(i)).getAsLong()));
            }
        }

        village.toBuildPaths.clear();
        if (tag.contains("ToBuildPaths")) {
            ListTag pl = tag.getList("ToBuildPaths", 4);
            for (int i = 0; i < pl.size(); i++) {
                village.toBuildPaths.add(BlockPos.of(((net.minecraft.nbt.LongTag) pl.get(i)).getAsLong()));
            }
        }

        return village;
    }
}
