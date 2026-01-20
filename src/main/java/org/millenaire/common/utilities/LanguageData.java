package org.millenaire.common.utilities;

import com.google.common.collect.Lists;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
// import org.millenaire.common.buildingplan.BuildingPlanSet;
// import org.millenaire.common.culture.VillageType;
// import org.millenaire.common.culture.VillagerType;
// import org.millenaire.common.goal.Goal;
// import org.millenaire.common.item.TradeGood;
// import org.millenaire.common.quest.Quest;
// import org.millenaire.common.quest.QuestStep;

public class LanguageData {
    private static final int PARCHMENT = 0;
    private static final int HELP = 1;
    public String language;
    public String topLevelLanguage = null;
    public boolean serverContent;
    public HashMap<String, String> strings = new HashMap<>();
    public HashMap<String, String> questStrings = new HashMap<>();
    public HashMap<Integer, List<List<String>>> texts = new HashMap<>();
    public HashMap<Integer, String> textsVersion = new HashMap<>();
    public HashMap<Integer, List<List<String>>> help = new HashMap<>();
    public HashMap<Integer, String> helpVersion = new HashMap<>();

    public static void printErrors(String languageKey, BufferedWriter writer, Set<String> errors, String message)
            throws IOException {
        boolean consolePrint = MillConfigValues.DEV && languageKey.equals("en");
        if (errors.size() > 0) {
            List<String> errorsList = Lists.newArrayList(errors);
            Collections.sort(errorsList);
            writer.write(message + "\n" + "\n");
            if (consolePrint) {
                // MillLog.writeTextRaw(message);
            }

            for (String s : errorsList) {
                writer.write(s + "\n");
                if (consolePrint) {
                    // MillLog.writeTextRaw(s);
                }
            }

            writer.write("\n");
            errors.clear();
        }
    }

    public LanguageData(String key, boolean serverContent) {
        this.language = key;
        if (this.language.split("_").length > 1) {
            this.topLevelLanguage = this.language.split("_")[0];
        }

        this.serverContent = serverContent;
    }

    public void compareWithLanguage(List<File> languageDirs, HashMap<String, Integer> percentages, LanguageData ref,
            Map<String, String> referenceLangStrings) {
        File translationGapDir = new File(MillCommonUtilities.getMillenaireCustomContentDir(), "Translation gaps");
        if (!translationGapDir.exists()) {
            translationGapDir.mkdirs();
        }

        File file = new File(translationGapDir, this.language + "-" + ref.language + ".txt");
        if (file.exists()) {
            file.delete();
        }

        try {
            int translationsMissing = 0;
            int translationsDone = 0;
            BufferedWriter writer = MillCommonUtilities.getWriter(file);
            // writer.write("Translation comparison...");
            // ... implementation skipped for brevity/dependencies ...
            // TODO: Full implementation requires Goal, Quest, Culture complete objects

            writer.flush();
            writer.close();
        } catch (Exception var18) {
            MillLog.printException(var18);
        }
    }

    public void loadFromDisk(List<File> languageDirs) {
        for (File languageDir : languageDirs) {
            File effectiveLanguageDir = new File(languageDir, this.language);
            if (!effectiveLanguageDir.exists()) {
                effectiveLanguageDir = new File(languageDir, this.language.split("_")[0]);
            }

            File stringFile = new File(effectiveLanguageDir, "strings.txt");
            if (stringFile.exists()) {
                this.loadStrings(this.strings, stringFile);
            }

            stringFile = new File(effectiveLanguageDir, "travelbook.txt");
            if (stringFile.exists()) {
                this.loadStrings(this.strings, stringFile);
            }

            if (effectiveLanguageDir.exists()) {
                File[] questFiles = effectiveLanguageDir
                        .listFiles(new MillCommonUtilities.PrefixExtFileFilter("quests", "txt"));
                if (questFiles != null) {
                    for (File file : questFiles) {
                        this.loadStrings(this.questStrings, file);
                    }
                }
            }
        }

        /*
         * for (Quest q : Quest.quests.values()) {
         * for (QuestStep step : q.steps) {
         * // ... Quest string loading ...
         * }
         * }
         */

        this.loadTextFiles(languageDirs, 0);
        this.loadTextFiles(languageDirs, 1);
        if (!MillConfigValues.loadedLanguages.containsKey(this.language)) {
            MillConfigValues.loadedLanguages.put(this.language, this);
        }
    }

    public Map<String, String> loadLangFileFromDisk(List<File> languageDirs) {
        Map<String, String> values = new HashMap<>();

        for (File languageDir : languageDirs) {
            File effectiveLanguageDir = new File(languageDir, this.language);
            if (!effectiveLanguageDir.exists()) {
                effectiveLanguageDir = new File(languageDir, this.language.split("_")[0]);
            }

            if (effectiveLanguageDir.exists()) {
                File[] langFiles = effectiveLanguageDir
                        .listFiles((FilenameFilter) new MillCommonUtilities.ExtFileFilter("lang"));
                if (langFiles != null) {
                    for (File file : langFiles) {
                        this.loadStrings(values, file);
                    }
                }
            }
        }

        return values;
    }

    private void loadStrings(Map<String, String> strings, File file) {
        try {
            java.io.BufferedReader reader = MillCommonUtilities.getReader(file);

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0 && !line.startsWith("//")) {
                    String[] temp = line.split("=");
                    if (temp.length == 2) {
                        String key = temp[0].trim().toLowerCase();
                        String value = temp[1].trim();
                        if (strings.containsKey(key)) {
                            // MillLog.error(null, "Key " + key + " is present more than once in " +
                            // file.getAbsolutePath());
                        } else {
                            strings.put(key, value);
                        }
                    } else if (line.endsWith("=") && temp.length > 0) {
                        String key = temp[0].toLowerCase();
                        if (strings.containsKey(key)) {
                            // MillLog.error(null, "Key " + key + " is present more than once in " +
                            // file.getAbsolutePath());
                        } else {
                            strings.put(key, "");
                        }
                    }
                }
            }

            reader.close();
        } catch (Exception var8) {
            MillLog.printException("Excption reading file " + file.getAbsolutePath(), var8);
        }
    }

    public void loadTextFiles(List<File> languageDirs, int type) {
        String dirName;
        if (type == 0) {
            dirName = "parchments";
        } else {
            dirName = "help";
        }

        String filePrefix;
        if (type == 0) {
            filePrefix = "parchment";
        } else {
            filePrefix = "help";
        }

        for (File languageDir : languageDirs) {
            File parchmentsDir = new File(new File(languageDir, this.language), dirName);
            if (!parchmentsDir.exists()) {
                parchmentsDir = new File(new File(languageDir, this.language.split("_")[0]), dirName);
            }

            if (!parchmentsDir.exists()) {
                continue;
            }

            LanguageData.ParchmentFileFilter filter = new LanguageData.ParchmentFileFilter(filePrefix);
            File[] files = parchmentsDir.listFiles(filter);
            if (files == null)
                continue;

            for (File file : files) {
                String sId = file.getName().substring(filePrefix.length() + 1, file.getName().length() - 4);
                int id = 0;
                if (sId.length() > 0) {
                    try {
                        id = Integer.parseInt(sId);
                    } catch (Exception var20) {
                        MillLog.printException("Error when trying to read pachment id: ", var20);
                    }
                }

                List<List<String>> text = new ArrayList<>();
                String version = "unknown";

                try {
                    java.io.BufferedReader reader = MillCommonUtilities.getReader(file);
                    List<String> page = new ArrayList<>();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("NEW_PAGE")) {
                            text.add(page);
                            page = new ArrayList<>();
                        } else if (line.startsWith("version:")) {
                            version = line.split(":")[1];
                        } else {
                            page.add(line);
                        }
                    }

                    text.add(page);
                    if (type == 0) {
                        this.texts.put(id, text);
                        this.textsVersion.put(id, version);
                    } else {
                        this.help.put(id, text);
                        this.helpVersion.put(id, version);
                    }
                } catch (Exception var21) {
                    MillLog.printException(var21);
                }
            }
        }
    }

    public void testTravelBookCompletion() {
        // TODO: Implement dependency on Culture/VillagerType/TradeGood
    }

    @Override
    public String toString() {
        return this.language;
    }

    private static class ParchmentFileFilter implements FilenameFilter {
        private final String filePrefix;

        public ParchmentFileFilter(String filePrefix) {
            this.filePrefix = filePrefix;
        }

        @Override
        public boolean accept(File file, String name) {
            if (!name.startsWith(this.filePrefix)) {
                return false;
            } else if (!name.endsWith(".txt")) {
                return false;
            } else {
                String id = name.substring(this.filePrefix.length() + 1, name.length() - 4);
                return id.length() != 0 && Integer.parseInt(id) >= 1;
            }
        }
    }
}
