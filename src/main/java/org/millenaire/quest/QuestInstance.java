package org.millenaire.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.millenaire.common.world.UserProfile;
import org.millenaire.common.world.MillWorldData;

import java.util.HashMap;
import java.util.Map;

public class QuestInstance {
    private static final String TAG_QUEST_KEY = "QuestKey";
    private static final String TAG_START_TIME = "StartTime";
    private static final String TAG_CURRENT_STEP = "CurrentStep";
    private static final String TAG_STEP_START_TIME = "StepStartTime";
    private static final String TAG_VILLAGERS = "Villagers";

    public MillWorldData mw;
    public Quest quest;
    public UserProfile profile;

    public long startTime;
    public int currentStepIndex = 0;
    public long currentStepStartTime;

    public Map<String, QuestInstanceVillager> villagers = new HashMap<>();

    public long uniqueid;

    public QuestInstance(MillWorldData mw, Quest quest, UserProfile profile, long startTime) {
        this.mw = mw;
        this.quest = quest;
        this.profile = profile;
        this.startTime = startTime;
        this.currentStepStartTime = startTime;
    }

    public QuestInstance(MillWorldData mw, Quest quest, UserProfile profile,
            Map<String, QuestInstanceVillager> villagers, long startTime, int currentStepIndex,
            long currentStepStartTime) {
        this.mw = mw;
        this.quest = quest;
        this.profile = profile;
        this.villagers = villagers;
        this.startTime = startTime;
        this.currentStepIndex = currentStepIndex;
        this.currentStepStartTime = currentStepStartTime;
    }

    public QuestStep getCurrentStep() {
        if (currentStepIndex >= 0 && currentStepIndex < quest.steps.size()) {
            return quest.steps.get(currentStepIndex);
        }
        return null;
    }

    public void writeToNBT(CompoundTag tag) {
        tag.putString(TAG_QUEST_KEY, quest.key);
        tag.putLong(TAG_START_TIME, startTime);
        tag.putInt(TAG_CURRENT_STEP, currentStepIndex);
        tag.putLong(TAG_STEP_START_TIME, currentStepStartTime);

        ListTag villagersList = new ListTag();
        for (QuestInstanceVillager qiv : villagers.values()) {
            CompoundTag vTag = new CompoundTag();
            vTag.putString("Key", qiv.key);
            vTag.putLong("VillagerID", qiv.villagerId);
            // vTag.putLong("TownHallPos", ...); // Serialization logic for BlockPos needed
            villagersList.add(vTag);
        }
        tag.put(TAG_VILLAGERS, villagersList);
    }

    // Static loader would go here, requiring looking up Quest by key from
    // Quest.quests
}
