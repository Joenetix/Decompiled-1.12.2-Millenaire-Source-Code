package org.millenaire.quest;

import java.util.ArrayList;
import java.util.List;

public class QuestVillager {
    public String key;

    // Requirements for selecting this villager
    public List<String> types = new ArrayList<>(); // VillagerType keys
    public String relatedTo; // Key of another QuestVillager
    public String relation; // "samevillage", "nearbyvillage", "anyvillage", "samehouse"

    public List<String> forbiddenTags = new ArrayList<>();
    public List<String> requiredTags = new ArrayList<>();

    public QuestVillager(String key) {
        this.key = key;
    }
}
