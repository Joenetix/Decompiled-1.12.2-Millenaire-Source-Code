package org.millenaire.quest;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.millenaire.MillenaireRevived;
import org.millenaire.common.utilities.MillLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

public class QuestLoader {

    public static void loadQuests(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("quests",
                location -> location.getPath().endsWith(".txt"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            if (location.getNamespace().equals(MillenaireRevived.MODID)) {
                loadQuest(location, entry.getValue());
            }
        }
    }

    private static void loadQuest(ResourceLocation location, Resource resource) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open()))) {
            String questKey = location.getPath().replace("quests/", "").replace(".txt", "");
            Quest quest = new Quest(questKey);

            String line;
            QuestStep currentStep = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//"))
                    continue;

                String[] parts = line.split(":", 2);
                if (parts.length != 2)
                    continue;

                String key = parts[0].toLowerCase().trim();
                String value = parts[1].trim();

                switch (key) {
                    case "step":
                        currentStep = new QuestStep(quest, quest.steps.size());
                        quest.addStep(currentStep);
                        break;
                    case "definevillager":
                        // Simplified parsing for definevillager: key=villager1,type=norman/farmer
                        String[] vParts = value.split(",");
                        String vKey = null;
                        for (String vp : vParts) {
                            if (vp.startsWith("key="))
                                vKey = vp.split("=")[1];
                        }
                        if (vKey != null) {
                            quest.addVillager(new QuestVillager(vKey));
                        }
                        break;
                    // Add more parsers here
                }
            }

            Quest.quests.put(quest.key, quest);
            MillLog.info("Loaded quest: " + quest.key);

        } catch (IOException e) {
            MillLog.error("Failed to load quest " + location + ": " + e.getMessage());
        }
    }
}
