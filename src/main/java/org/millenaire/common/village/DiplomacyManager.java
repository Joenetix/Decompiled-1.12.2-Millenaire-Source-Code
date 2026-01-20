package org.millenaire.common.village;

import org.millenaire.core.Village;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

/**
 * Manages diplomatic relations for a village.
 */
public class DiplomacyManager {
    private final Village village;
    private final Map<UUID, Integer> villageRelations = new HashMap<>(); // +ve = Good, -ve = Bad

    // Relation constants
    public static final int RELATION_WAR = -80;
    public static final int RELATION_HOSTILE = -40;
    public static final int RELATION_NEUTRAL = 0;
    public static final int RELATION_FRIENDLY = 40;
    public static final int RELATION_ALLY = 80;

    public DiplomacyManager(Village v) {
        this.village = v;
    }

    public int getRelation(UUID otherVillageId) {
        return villageRelations.getOrDefault(otherVillageId, RELATION_NEUTRAL);
    }

    public void setRelation(UUID otherVillageId, int value) {
        villageRelations.put(otherVillageId, value);
    }

    public void adjustRelation(UUID otherVillageId, int delta) {
        int current = getRelation(otherVillageId);
        setRelation(otherVillageId, current + delta);
    }

    public boolean isEnemy(UUID otherVillageId) {
        return getRelation(otherVillageId) <= RELATION_HOSTILE;
    }

    // NBT methods
    public void writeToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, Integer> entry : villageRelations.entrySet()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", entry.getKey());
            t.putInt("val", entry.getValue());
            list.add(t);
        }
        tag.put("relations", list);
    }

    public void readFromNBT(CompoundTag tag) {
        villageRelations.clear();
        if (tag.contains("relations")) {
            ListTag list = tag.getList("relations", 10);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag t = list.getCompound(i);
                villageRelations.put(t.getUUID("id"), t.getInt("val"));
            }
        }
    }

    public void tick() {
        // Future: decay relations or manage wars
    }

    public Map<UUID, Integer> getRelations() {
        return new HashMap<>(villageRelations);
    }
}
