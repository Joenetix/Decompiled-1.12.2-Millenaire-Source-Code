package org.millenaire.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;

public class QuestStep {
    public Quest quest;
    public int index;
    public int duration = 0; // In seconds
    public String villagerKey; // Key of the QuestVillager to interact with

    // Descriptions
    public Map<String, String> labels = new HashMap<>();
    public Map<String, String> descriptions = new HashMap<>();
    public Map<String, String> descriptionsSuccess = new HashMap<>();
    public Map<String, String> descriptionsRefuse = new HashMap<>();
    public Map<String, String> descriptionsTimeUp = new HashMap<>();
    public Map<String, String> listings = new HashMap<>();

    // Requirements & Rewards
    public Map<Item, Integer> requiredGoods = new HashMap<>();
    public Map<Item, Integer> rewardGoods = new HashMap<>();
    public int rewardMoney = 0;
    public int rewardReputation = 0;
    public int penaltyReputation = 0;

    // Actions
    public List<String> setGlobalTagsSuccess = new ArrayList<>();
    public List<String> clearGlobalTagsSuccess = new ArrayList<>();
    public List<String> setPlayerTagsSuccess = new ArrayList<>();
    public List<String> clearPlayerTagsSuccess = new ArrayList<>();
    public List<String> setPlayerTagsFailure = new ArrayList<>();
    public List<String> clearPlayerTagsFailure = new ArrayList<>();

    // Logic
    public List<String[]> setActionDataSuccess = new ArrayList<>();

    public QuestStep(Quest quest, int index) {
        this.quest = quest;
        this.index = index;
    }

    public String getDescription() {
        return descriptions.get("description");
    }

    public String getDescriptionSuccess() {
        return descriptionsSuccess.get("description_success");
    }

    public String getDescriptionRefuse() {
        return descriptionsRefuse.get("description_refuse");
    }

    public String getDescriptionTimeUp() {
        return descriptionsTimeUp.get("description_timeup");
    }

    public String getLabel() {
        return labels.get("label");
    }

    public String getListing() {
        return listings.get("listing");
    }

}
