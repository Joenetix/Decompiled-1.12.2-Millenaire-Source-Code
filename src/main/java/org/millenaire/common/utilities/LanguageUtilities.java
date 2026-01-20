package org.millenaire.common.utilities;

import java.io.File;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.InvItem;
import org.millenaire.core.MillItems;
// import org.millenaire.common.buildingplan.BuildingDevUtilities;
// import org.millenaire.common.buildingplan.BuildingPlan;
// import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.culture.Culture;

public class LanguageUtilities {
    public static final char BLACK = '0';
    public static final char DARKBLUE = '1';
    public static final char DARKGREEN = '2';
    public static final char LIGHTBLUE = '3';
    public static final char DARKRED = '4';
    public static final char PURPLE = '5';
    public static final char ORANGE = '6';
    public static final char LIGHTGREY = '7';
    public static final char DARKGREY = '8';
    public static final char BLUE = '9';
    public static final char LIGHTGREEN = 'a';
    public static final char CYAN = 'b';
    public static final char LIGHTRED = 'c';
    public static final char PINK = 'd';
    public static final char YELLOW = 'e';
    public static final char WHITE = 'f';
    public static String loadedLanguage = null;

    public static void applyLanguage() {
        // Stubbed for now as it relies on dev utilities
        MillLog.major(null, "LanguageData loaded: " + MillConfigValues.effective_language);
    }

    public static String fillInName(String s) {
        if (s == null) {
            return "";
        } else {
            Player player = Mill.proxy.getTheSinglePlayer();
            return player != null ? s.replaceAll("\\$name", player.getName().getString()) : s;
        }
    }

    public static List<List<String>> getHelp(int id) {
        if (MillConfigValues.mainLanguage.help.containsKey(id)) {
            return MillConfigValues.mainLanguage.help.get(id);
        } else {
            return MillConfigValues.fallbackLanguage.help.containsKey(id)
                    ? MillConfigValues.fallbackLanguage.help.get(id)
                    : null;
        }
    }

    public static List<File> getLanguageDirs() {
        List<File> languageDirs = new ArrayList<>();

        for (File dir : Mill.loadingDirs) {
            File languageDir = new File(dir, "languages");
            if (languageDir.exists()) {
                languageDirs.add(languageDir);
            }
        }

        languageDirs.add(new File(MillCommonUtilities.getMillenaireCustomContentDir(), "languages"));
        return languageDirs;
    }

    public static List<List<String>> getParchment(int id) {
        if (MillConfigValues.mainLanguage.texts.containsKey(id)) {
            return MillConfigValues.mainLanguage.texts.get(id);
        } else {
            return MillConfigValues.fallbackLanguage.texts.containsKey(id)
                    ? MillConfigValues.fallbackLanguage.texts.get(id)
                    : null;
        }
    }

    public static String getRawString(String key, boolean mustFind) {
        return getRawString(key, mustFind, true, true);
    }

    public static String getRawString(String key, boolean mustFind, boolean main, boolean fallback) {
        if (main && MillConfigValues.mainLanguage != null && MillConfigValues.mainLanguage.strings.containsKey(key)) {
            return MillConfigValues.mainLanguage.strings.get(key);
        } else if (main && MillConfigValues.serverMainLanguage != null
                && MillConfigValues.serverMainLanguage.strings.containsKey(key)) {
            return MillConfigValues.serverMainLanguage.strings.get(key);
        } else if (fallback && MillConfigValues.fallbackLanguage != null
                && MillConfigValues.fallbackLanguage.strings.containsKey(key)) {
            return MillConfigValues.fallbackLanguage.strings.get(key);
        } else if (fallback && MillConfigValues.serverFallbackLanguage != null
                && MillConfigValues.serverFallbackLanguage.strings.containsKey(key)) {
            return MillConfigValues.serverFallbackLanguage.strings.get(key);
        } else {
            if (mustFind && MillConfigValues.LogTranslation >= 1) {
                MillLog.error(null, "String not found: " + key);
            }

            return mustFind ? key : null;
        }
    }

    public static boolean hasString(String key) {
        if (!isTranslationLoaded()) {
            return false;
        } else {
            key = key.toLowerCase();
            String rawResult = getRawString(key, true);
            return !key.equalsIgnoreCase(rawResult);
        }
    }

    public static boolean isTranslationLoaded() {
        return MillConfigValues.mainLanguage != null;
    }

    public static void loadLanguages(String minecraftLanguage) {
        if (!MillConfigValues.main_language.equals("")) {
            MillConfigValues.effective_language = MillConfigValues.main_language;
        } else if (minecraftLanguage != null) {
            MillConfigValues.effective_language = minecraftLanguage;
        } else {
            MillConfigValues.effective_language = "fr";
        }

        // Simplified loading logic for now, omitting full traversal that requires all
        // deps
        if (loadedLanguage == null || !loadedLanguage.equals(MillConfigValues.effective_language)) {
            MillLog.major(null, "Loading language: " + MillConfigValues.effective_language);
            loadedLanguage = MillConfigValues.effective_language;
            List<File> languageDirs = getLanguageDirs();
            MillConfigValues.mainLanguage = new LanguageData(MillConfigValues.effective_language, false);
            MillConfigValues.mainLanguage.loadFromDisk(languageDirs);
            if (MillConfigValues.main_language.equals(MillConfigValues.fallback_language)) {
                MillConfigValues.fallbackLanguage = MillConfigValues.mainLanguage;
            } else {
                MillConfigValues.fallbackLanguage = new LanguageData(MillConfigValues.fallback_language, false);
                MillConfigValues.fallbackLanguage.loadFromDisk(languageDirs);
            }

            // TODO: Load all languages loop, Culture loop

            applyLanguage();
        }
    }

    public static String questString(String key, boolean required) {
        return questString(key, true, true, required);
    }

    public static String questString(String key, boolean main, boolean fallback, boolean required) {
        key = key.toLowerCase();
        if (main && MillConfigValues.mainLanguage != null
                && MillConfigValues.mainLanguage.questStrings.containsKey(key)) {
            return MillConfigValues.mainLanguage.questStrings.get(key);
        } else if (main && MillConfigValues.serverMainLanguage != null
                && MillConfigValues.serverMainLanguage.questStrings.containsKey(key)) {
            return MillConfigValues.serverMainLanguage.questStrings.get(key);
        } else if (fallback && MillConfigValues.fallbackLanguage != null
                && MillConfigValues.fallbackLanguage.questStrings.containsKey(key)) {
            return MillConfigValues.fallbackLanguage.questStrings.get(key);
        } else if (fallback && MillConfigValues.serverFallbackLanguage != null
                && MillConfigValues.serverFallbackLanguage.questStrings.containsKey(key)) {
            return MillConfigValues.serverFallbackLanguage.questStrings.get(key);
        } else {
            return required ? key : null;
        }
    }

    public static String removeAccent(String source) {
        return Normalizer.normalize(source, Form.NFD).replaceAll("[̀-ͯ]", "");
    }

    public static String string(String key) {
        if (!isTranslationLoaded()) {
            return "";
        } else {
            key = key.toLowerCase();
            String rawResult = getRawString(key, true);
            return fillInName(rawResult);
        }
    }

    public static String string(String key, String... values) {
        String s = string(key);
        if (!s.equalsIgnoreCase(key)) {
            int pos = 0;

            for (String value : values) {
                String v = (value != null) ? value : "";
                s = s.replaceAll("<" + pos + ">", unknownString(v));
                pos++;
            }
        } else {
            for (String value : values) {
                s = s + ":" + value;
            }
        }

        return s;
    }

    public static Component textComponent(String key) {
        return Component.literal(string(key));
    }

    public static Component textComponent(String key, String... values) {
        return Component.literal(string(key, values));
    }

    public static String unknownString(String key) {
        if (key == null) {
            return "";
        } else if (!isTranslationLoaded()) {
            return key;
        } else if (key.startsWith("_item:")) {
            // TODO implementation
            return key;
        } else {
            if (key.startsWith("culture:")) {
                String cultureKey = key.split(":")[1];
                String stringKey = key.split(":")[2];
                Culture culture = Culture.getCultureByName(cultureKey);
                if (culture != null) {
                    return culture.getCultureString(stringKey);
                }
            }

            String rawKey = getRawString(key, false);
            return rawKey != null ? fillInName(rawKey) : key;
        }
    }
}
