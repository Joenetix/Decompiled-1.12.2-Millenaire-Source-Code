package org.millenaire.common.culture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds localized content for a culture in a specific language.
 * Includes strings, dialogues, sentences, reputation levels, and building
 * names.
 */
public class CultureLanguage {

    private final Culture culture;
    private final String languageCode;

    // Key → localized string
    public Map<String, String> strings = new HashMap<>();

    // Building plan name (lowercase) → display name
    public Map<String, String> buildingNames = new HashMap<>();

    // Sentence set key → list of possible sentences
    public Map<String, List<String>> sentences = new HashMap<>();

    // Reputation levels for this culture
    public List<ReputationLevel> reputationLevels = new ArrayList<>();

    // Dialogues for villager interactions
    public Map<String, Dialogue> dialogues = new HashMap<>();

    public CultureLanguage(Culture culture, String languageCode) {
        this.culture = culture;
        this.languageCode = languageCode;
    }

    public Culture getCulture() {
        return culture;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getString(String key) {
        return strings.getOrDefault(key.toLowerCase(), key);
    }

    public String getBuildingName(String planName) {
        return buildingNames.get(planName.toLowerCase());
    }

    public List<String> getSentences(String key) {
        return sentences.get(key);
    }

    public ReputationLevel getReputationLevel(int reputation) {
        for (ReputationLevel level : reputationLevels) {
            if (reputation >= level.minRep && reputation < level.maxRep) {
                return level;
            }
        }
        return null;
    }

    /**
     * A reputation tier with localized label and description.
     */
    public static class ReputationLevel {
        public final int minRep;
        public final int maxRep;
        public final String label;
        public final String desc;

        public ReputationLevel(int minRep, int maxRep, String label, String desc) {
            this.minRep = minRep;
            this.maxRep = maxRep;
            this.label = label;
            this.desc = desc;
        }
    }

    /**
     * A dialogue tree for villager interactions.
     */
    public static class Dialogue {
        public final String key;
        public final List<String> lines = new ArrayList<>();
        public final Map<String, String> responses = new HashMap<>();

        public Dialogue(String key) {
            this.key = key;
        }
    }
}
