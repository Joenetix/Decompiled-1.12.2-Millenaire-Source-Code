package org.millenaire.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager for all Millénaire villages in a world.
 * Stores and retrieves village data using Minecraft's SavedData system.
 */
public class VillageManager extends SavedData {

    private static final String DATA_NAME = "millenaire_villages";

    private final Map<UUID, VillageData> villages = new HashMap<>();

    public VillageManager() {
    }

    public VillageManager(CompoundTag nbt) {
        // Load all villages
        CompoundTag villagesTag = nbt.getCompound("Villages");
        for (String key : villagesTag.getAllKeys()) {
            UUID villageId = UUID.fromString(key);
            CompoundTag villageTag = villagesTag.getCompound(key);
            VillageData village = new VillageData(villageTag);
            villages.put(villageId, village);
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag nbt) {
        CompoundTag villagesTag = new CompoundTag();
        for (Map.Entry<UUID, VillageData> entry : villages.entrySet()) {
            CompoundTag villageTag = new CompoundTag();
            entry.getValue().save(villageTag);
            villagesTag.put(entry.getKey().toString(), villageTag);
        }
        nbt.put("Villages", villagesTag);
        return nbt;
    }

    /**
     * Get the VillageManager for a world
     */
    public static VillageManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VillageManager::new,
                VillageManager::new,
                DATA_NAME);
    }

    /**
     * Register a new village
     */
    public VillageData createVillage(UUID villageId, String cultureId, net.minecraft.core.BlockPos centerPos) {
        VillageData village = new VillageData(villageId, cultureId, centerPos);
        villages.put(villageId, village);
        setDirty();
        return village;
    }

    /**
     * Get a village by ID
     */
    public VillageData getVillage(UUID villageId) {
        return villages.get(villageId);
    }

    /**
     * Get all villages
     */
    public Map<UUID, VillageData> getAllVillages() {
        return villages;
    }

    /**
     * Remove a village (e.g., if destroyed)
     */
    public void removeVillage(UUID villageId) {
        villages.remove(villageId);
        setDirty();
    }

    /**
     * Tick all villages
     */
    public void tickAll(ServerLevel level, long currentTick) {
        for (VillageData village : villages.values()) {
            village.tick(level, currentTick);
        }
    }
}
