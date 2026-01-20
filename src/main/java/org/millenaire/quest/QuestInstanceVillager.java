package org.millenaire.quest;

import net.minecraft.core.BlockPos;

import java.util.UUID;

public class QuestInstanceVillager {
    public String key; // Key from Quest definition (e.g. "villager1")
    public long id; // QuestInstanceVillager ID (often same as Villager ID or random)
    public long villagerId; // The ID of the actual VillagerRecord
    public BlockPos townHallPos; // Required to find the villager

    public QuestInstanceVillager(String key, long villagerId, BlockPos townHallPos) {
        this.key = key;
        this.id = villagerId; // Simplified map
        this.villagerId = villagerId;
        this.townHallPos = townHallPos;
    }
}
