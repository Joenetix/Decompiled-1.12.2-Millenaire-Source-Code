package org.millenaire.common.utilities;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
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
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.quest.Quest;
import org.millenaire.common.quest.QuestStep;

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

   public static void printErrors(String languageKey, BufferedWriter writer, Set<String> errors, String message) throws IOException {
      boolean consolePrint = MillConfigValues.DEV && languageKey.equals("en");
      if (errors.size() > 0) {
         List<String> errorsList = Lists.newArrayList(errors);
         Collections.sort(errorsList);
         writer.write(message + "\n" + "\n");
         if (consolePrint) {
            MillLog.writeTextRaw(message);
         }

         for (String s : errorsList) {
            writer.write(s + "\n");
            if (consolePrint) {
               MillLog.writeTextRaw(s);
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

   public void compareWithLanguage(List<File> languageDirs, HashMap<String, Integer> percentages, LanguageData ref, Map<String, String> referenceLangStrings) {
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
         writer.write(
            "Translation comparison between " + this.language + " and " + ref.language + ", version " + "8.1.2" + ", date: " + MillLog.now() + "\n" + "\n"
         );
         Set<String> errors = new HashSet<>();
         Set<String> errors2 = new HashSet<>();
         Map<String, String> langStrings = this.loadLangFileFromDisk(languageDirs);
         List<String> keys = new ArrayList<>(referenceLangStrings.keySet());
         Collections.sort(keys);

         for (String key : keys) {
            if (!langStrings.containsKey(key)) {
               errors.add(key + "=");
               translationsMissing++;
            } else {
               int nbValues = referenceLangStrings.get(key).split("<").length - 1;
               int nbValues2 = langStrings.get(key).split("<").length - 1;
               if (nbValues != nbValues2) {
                  errors2.add(key);
                  translationsMissing++;
               } else {
                  translationsDone++;
               }
            }
         }

         this.printErrors(writer, errors, "Gap with " + ref.language + " in .lang file: ");
         this.printErrors(writer, errors2, "Mismatched number of parameters with " + ref.language + " in the .lang file: ");
         keys = new ArrayList<>(ref.strings.keySet());
         Collections.sort(keys);

         for (String keyx : keys) {
            if (!this.strings.containsKey(keyx)) {
               errors.add(keyx + "=");
               translationsMissing++;
            } else {
               int nbValues = ref.strings.get(keyx).split("<").length - 1;
               int nbValues2 = this.strings.get(keyx).split("<").length - 1;
               if (nbValues != nbValues2) {
                  errors2.add(keyx);
                  translationsMissing++;
               } else {
                  translationsDone++;
               }
            }
         }

         this.printErrors(writer, errors, "Gap with " + ref.language + " in strings.txt file: ");
         this.printErrors(writer, errors2, "Mismatched number of parameters with " + ref.language + " in the strings.txt file: ");
         keys = new ArrayList<>(ref.questStrings.keySet());
         Collections.sort(keys);

         for (String keyxx : keys) {
            if (!this.questStrings.containsKey(keyxx)) {
               errors.add(keyxx);
               translationsMissing++;
            } else {
               translationsDone++;
            }
         }

         this.printErrors(writer, errors, "Gap with " + ref.language + " in quest files: ");

         for (Goal goal : Goal.goals.values()) {
            if (!this.strings.containsKey("goal." + goal.labelKey(null)) && !ref.strings.containsKey("goal." + goal.labelKey(null))) {
               errors.add("goal." + goal.labelKey(null) + "=");
            }
         }

         this.printErrors(writer, errors, "Goals with labels missing in both " + ref.language + " and " + this.language + ":");

         for (int id : ref.texts.keySet()) {
            if (!this.texts.containsKey(id)) {
               errors.add("Parchment " + id + " is missing.");
               translationsMissing += 10;
            } else if (!this.textsVersion.get(id).equals(ref.textsVersion.get(id))) {
               errors.add(
                  "Parchment "
                     + id
                     + " has a different version: it is at version "
                     + this.textsVersion.get(id)
                     + " while "
                     + ref.language
                     + " parchment is at "
                     + ref.textsVersion.get(id)
               );
               translationsMissing += 5;
            } else {
               translationsDone += 10;
            }
         }

         this.printErrors(writer, errors, "Differences in parchments with " + ref.language + ":");

         for (int idx : ref.help.keySet()) {
            if (!this.help.containsKey(idx)) {
               errors.add("Help " + idx + " is missing.");
               translationsMissing += 10;
            } else if (!this.helpVersion.get(idx).equals(ref.helpVersion.get(idx))) {
               errors.add(
                  "Help "
                     + idx
                     + " has a different version: it is at version "
                     + this.helpVersion.get(idx)
                     + " while "
                     + ref.language
                     + " parchment is at "
                     + ref.helpVersion.get(idx)
               );
               translationsMissing += 5;
            } else {
               translationsDone += 10;
            }
         }

         this.printErrors(writer, errors, "Differences in help files with " + ref.language + ":");

         for (Culture c : Culture.ListCultures) {
            int[] res = c.compareCultureLanguages(this.language, ref.language, writer);
            translationsDone += res[0];
            translationsMissing += res[1];
         }

         int percentDone;
         if (translationsDone + translationsMissing > 0) {
            percentDone = translationsDone * 100 / (translationsDone + translationsMissing);
         } else {
            percentDone = 0;
         }

         percentages.put(this.language, percentDone);
         writer.write("Traduction completness: " + percentDone + "%" + "\n");
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
            for (File file : effectiveLanguageDir.listFiles(new MillCommonUtilities.PrefixExtFileFilter("quests", "txt"))) {
               this.loadStrings(this.questStrings, file);
            }
         }
      }

      for (Quest q : Quest.quests.values()) {
         for (QuestStep step : q.steps) {
            if (step.labels.containsKey(this.language)) {
               this.questStrings.put(step.getStringKey() + "label", step.labels.get(this.language));
            } else if (this.topLevelLanguage != null && step.labels.containsKey(this.topLevelLanguage)) {
               this.questStrings.put(step.getStringKey() + "label", step.labels.get(this.topLevelLanguage));
            }

            if (step.descriptions.containsKey(this.language)) {
               this.questStrings.put(step.getStringKey() + "description", step.descriptions.get(this.language));
            } else if (this.topLevelLanguage != null && step.descriptions.containsKey(this.topLevelLanguage)) {
               this.questStrings.put(step.getStringKey() + "description", step.descriptions.get(this.topLevelLanguage));
            }

            if (step.descriptionsSuccess.containsKey(this.language)) {
               this.questStrings.put(step.getStringKey() + "description_success", step.descriptionsSuccess.get(this.language));
            } else if (this.topLevelLanguage != null && step.descriptionsSuccess.containsKey(this.topLevelLanguage)) {
               this.questStrings.put(step.getStringKey() + "description_success", step.descriptionsSuccess.get(this.topLevelLanguage));
            }

            if (step.descriptionsRefuse.containsKey(this.language)) {
               this.questStrings.put(step.getStringKey() + "description_refuse", step.descriptionsRefuse.get(this.language));
            } else if (this.topLevelLanguage != null && step.descriptionsRefuse.containsKey(this.topLevelLanguage)) {
               this.questStrings.put(step.getStringKey() + "description_refuse", step.descriptionsRefuse.get(this.topLevelLanguage));
            }

            if (step.descriptionsTimeUp.containsKey(this.language)) {
               this.questStrings.put(step.getStringKey() + "description_timeup", step.descriptionsTimeUp.get(this.language));
            } else if (this.topLevelLanguage != null && step.descriptionsTimeUp.containsKey(this.topLevelLanguage)) {
               this.questStrings.put(step.getStringKey() + "description_timeup", step.descriptionsTimeUp.get(this.topLevelLanguage));
            }

            if (step.listings.containsKey(this.language)) {
               this.questStrings.put(step.getStringKey() + "listing", step.listings.get(this.language));
            } else if (this.topLevelLanguage != null && step.listings.containsKey(this.topLevelLanguage)) {
               this.questStrings.put(step.getStringKey() + "listing", step.listings.get(this.topLevelLanguage));
            }
         }
      }

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
            for (File file : effectiveLanguageDir.listFiles(new MillCommonUtilities.ExtFileFilter("lang"))) {
               this.loadStrings(values, file);
            }
         }
      }

      return values;
   }

   private void loadStrings(Map<String, String> strings, File file) {
      try {
         BufferedReader reader = MillCommonUtilities.getReader(file);

         String line;
         while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() > 0 && !line.startsWith("//")) {
               String[] temp = line.split("=");
               if (temp.length == 2) {
                  String key = temp[0].trim().toLowerCase();
                  String value = temp[1].trim();
                  if (strings.containsKey(key)) {
                     MillLog.error(null, "Key " + key + " is present more than once in " + file.getAbsolutePath());
                  } else {
                     strings.put(key, value);
                  }
               } else if (line.endsWith("=") && temp.length > 0) {
                  String key = temp[0].toLowerCase();
                  if (strings.containsKey(key)) {
                     MillLog.error(null, "Key " + key + " is present more than once in " + file.getAbsolutePath());
                  } else {
                     strings.put(key, "");
                  }
               } else if (line.contains("====") || line.contains("<<<<<") || line.contains(">")) {
                  MillLog.error(null, "Git conflict lines present in " + file.getAbsolutePath());
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
            return;
         }

         LanguageData.ParchmentFileFilter filter = new LanguageData.ParchmentFileFilter(filePrefix);

         for (File file : parchmentsDir.listFiles(filter)) {
            String sId = file.getName().substring(filePrefix.length() + 1, file.getName().length() - 4);
            int id = 0;
            if (sId.length() > 0) {
               try {
                  id = Integer.parseInt(sId);
               } catch (Exception var20) {
                  MillLog.printException("Error when trying to read pachment id: ", var20);
               }
            } else {
               MillLog.error(null, "Couldn't read the ID of " + file.getAbsolutePath() + ". sId: " + sId);
            }

            if (MillConfigValues.LogOther >= 1) {
               MillLog.minor(file, "Loading " + dirName + ": " + file.getAbsolutePath());
            }

            List<List<String>> text = new ArrayList<>();
            String version = "unknown";

            try {
               BufferedReader reader = MillCommonUtilities.getReader(file);
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

   private void printErrors(BufferedWriter writer, Set<String> errors, String message) throws IOException {
      printErrors(this.language, writer, errors, message);
   }

   public void testTravelBookCompletion() {
      for (Culture culture : Culture.ListCultures) {
         try {
            int nbVillagers = 0;
            int nbVillagersDesc = 0;
            int nbVillages = 0;
            int nbVillagesDesc = 0;
            int nbBuildings = 0;
            int nbBuildingsDesc = 0;
            int nbTradeGoods = 0;
            int nbTradeGoodsDesc = 0;

            for (VillagerType vtype : culture.listVillagerTypes) {
               if (vtype.travelBookDisplay) {
                  nbVillagers++;
                  if (culture.hasCultureString("travelbook.villager." + vtype.key + ".desc")) {
                     nbVillagersDesc++;
                  }
               }
            }

            for (VillageType vtypex : culture.listVillageTypes) {
               if (vtypex.travelBookDisplay) {
                  nbVillages++;
                  if (culture.hasCultureString("travelbook.village." + vtypex.key + ".desc")) {
                     nbVillagesDesc++;
                  }
               }
            }

            for (BuildingPlanSet planSet : culture.ListPlanSets) {
               if (planSet.getFirstStartingPlan().travelBookDisplay) {
                  nbBuildings++;
                  if (culture.hasCultureString("travelbook.building." + planSet.key + ".desc")) {
                     nbBuildingsDesc++;
                  }
               }
            }

            for (TradeGood tradeGood : culture.goodsList) {
               if (tradeGood.travelBookDisplay) {
                  nbTradeGoods++;
                  if (culture.hasCultureString("travelbook.trade_good." + tradeGood.key + ".desc")) {
                     nbTradeGoodsDesc++;
                  }
               }
            }

            MillLog.temp(
               culture,
               "Travel book status: Villagers "
                  + nbVillagersDesc
                  + "/"
                  + nbVillagers
                  + ", village types "
                  + nbVillagesDesc
                  + "/"
                  + nbVillages
                  + ", buildings "
                  + nbBuildingsDesc
                  + "/"
                  + nbBuildings
                  + ", trade goods "
                  + nbTradeGoodsDesc
                  + "/"
                  + nbTradeGoods
            );
         } catch (Exception var13) {
            MillLog.printException("Error when testing Travel Book for culture " + culture.key + ":", var13);
         }
      }
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
