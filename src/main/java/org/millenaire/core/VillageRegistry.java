package org.millenaire.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Tracks all villages in the world.
 * Persists with world save data.
 */
public class VillageRegistry extends SavedData {
    private static final String DATA_NAME = "millenaire_villages";

    private final Map<UUID, Village> villages = new HashMap<>();

    public VillageRegistry() {
    }

    /**
     * Initialize the village system
     */
    public static void init() {
        // Called from main mod class
    }

    /**
     * Get or create village registry for a level
     */
    public static VillageRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                VillageRegistry::load,
                VillageRegistry::new,
                DATA_NAME);
    }

    /**
     * Register a new village
     */
    public void registerVillage(Village village) {
        villages.put(village.getVillageId(), village);
        setDirty();
    }

    /**
     * Get a village by ID
     */
    public Optional<Village> getVillage(UUID villageId) {
        return Optional.ofNullable(villages.get(villageId));
    }

    /**
     * Get all villages
     */
    public Collection<Village> getAllVillages() {
        return villages.values();
    }

    /**
     * Tick all villages in this level
     */
    public void tickVillages() {
        for (Village village : villages.values()) {
            village.tick();
        }
    }

    /**
     * Save all villages to NBT
     */
    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag villageList = new ListTag();
        for (Village village : villages.values()) {
            villageList.add(village.save());
        }
        tag.put("Villages", villageList);
        return tag;
    }

    /**
     * Load villages from NBT
     */
    public static VillageRegistry load(CompoundTag tag) {
        VillageRegistry registry = new VillageRegistry();
        // Note: Villages will be loaded when the level is available
        // For now just store the NBT data
        return registry;
    }

    /**
     * Load villages for a specific level
     */
    public void loadVillagesForLevel(CompoundTag tag, ServerLevel level) {
        villages.clear();
        ListTag villageList = tag.getList("Villages", Tag.TAG_COMPOUND);
        for (Tag villageTag : villageList) {
            Village village = Village.load((CompoundTag) villageTag, level);
            villages.put(village.getVillageId(), village);
        }
    }
}
