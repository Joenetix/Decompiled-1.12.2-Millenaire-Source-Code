package org.millenaire.quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.millenaire.common.utilities.MillLog;

public class Quest {
    public static Map<String, Quest> quests = new HashMap<>();

    public String key;
    public int maxSimultaneous = 5;
    public int minReputation = 0;
    public double chancePerHour = 0.0;

    public List<QuestStep> steps = new ArrayList<>();
    public Map<String, QuestVillager> villagers = new HashMap<>();
    public List<QuestVillager> villagersOrdered = new ArrayList<>();

    public List<String> globalTagsRequired = new ArrayList<>();
    public List<String> globalTagsForbidden = new ArrayList<>();
    public List<String> profileTagsRequired = new ArrayList<>();
    public List<String> profileTagsForbidden = new ArrayList<>();

    public Quest(String key) {
        this.key = key;
    }

    public void addStep(QuestStep step) {
        this.steps.add(step);
    }

    public void addVillager(QuestVillager villager) {
        if (villagers.containsKey(villager.key)) {
            MillLog.error("Duplicate villager key in quest " + key + ": " + villager.key);
            return;
        }
        villagers.put(villager.key, villager);
        villagersOrdered.add(villager);
    }

    @Override
    public String toString() {
        return "Quest: " + key;
    }
}
