package org.millenaire.common.quest;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.VillagerRecord;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.world.UserProfile;

public class Quest {
   public static HashMap<String, Quest> quests = new HashMap<>();
   private static final String REL_NEARBYVILLAGE = "nearbyvillage";
   private static final String REL_ANYVILLAGE = "anyvillage";
   private static final String REL_SAMEHOUSE = "samehouse";
   private static final String REL_SAMEVILLAGE = "samevillage";
   public static final String INDIAN_WQ = "sadhu";
   public static final String NORMAN_WQ = "alchemist";
   public static final String MAYAN_WQ = "fallenking";
   public static final Map<String, Integer> WORLD_MISSION_NB = new HashMap<>();
   public static final String[] WORLD_MISSION_KEYS = new String[]{"sadhu", "alchemist", "fallenking"};
   public double chanceperhour;
   public String key;
   public int maxsimultaneous;
   public int minreputation;
   public List<QuestStep> steps;
   public List<String> globalTagsForbidden;
   public List<String> globalTagsRequired;
   public List<String> profileTagsForbidden;
   public List<String> profileTagsRequired;
   public HashMap<String, QuestVillager> villagers;
   public List<QuestVillager> villagersOrdered;

   public Quest() {
      WORLD_MISSION_NB.put("sadhu", 15);
      WORLD_MISSION_NB.put("alchemist", 13);
      WORLD_MISSION_NB.put("fallenking", 10);
      this.chanceperhour = 0.0;
      this.maxsimultaneous = 5;
      this.minreputation = 0;
      this.steps = new ArrayList<>();
      this.globalTagsForbidden = new ArrayList<>();
      this.globalTagsRequired = new ArrayList<>();
      this.profileTagsForbidden = new ArrayList<>();
      this.profileTagsRequired = new ArrayList<>();
      this.villagers = new HashMap<>();
      this.villagersOrdered = new ArrayList<>();
   }

   private static Quest loadQuest(File file) {
      Quest q = new Quest();
      q.key = file.getName().split("\\.")[0];

      try {
         BufferedReader reader = MillCommonUtilities.getReader(file);
         QuestStep step = null;

         String line;
         while ((line = reader.readLine()) != null) {
            if (line.trim().length() > 0 && !line.startsWith("//")) {
               String[] temp = line.split(":");
               if (temp.length != 2) {
                  MillLog.error(null, "Invalid line when loading quest " + file.getName() + ": " + line);
               } else {
                  String key = temp[0].toLowerCase();
                  String value = temp[1];
                  if (key.equals("step")) {
                     step = new QuestStep(q, q.steps.size());
                     q.steps.add(step);
                  } else if (key.equals("minreputation")) {
                     q.minreputation = MillCommonUtilities.readInteger(value);
                  } else if (key.equals("chanceperhour")) {
                     q.chanceperhour = Double.parseDouble(value);
                  } else if (key.equals("maxsimultaneous")) {
                     q.maxsimultaneous = MillCommonUtilities.readInteger(value);
                  } else if (key.equals("definevillager")) {
                     QuestVillager v = q.loadQVillager(value);
                     if (v != null) {
                        q.villagers.put(v.key, v);
                        q.villagersOrdered.add(v);
                     }
                  } else if (key.equals("requiredglobaltag")) {
                     q.globalTagsRequired.add(value.trim().toLowerCase());
                  } else if (key.equals("forbiddenglobaltag")) {
                     q.globalTagsForbidden.add(value.trim().toLowerCase());
                  } else if (key.equals("requiredplayertag")) {
                     q.profileTagsRequired.add(value.trim().toLowerCase());
                  } else if (key.equals("forbiddenplayertag")) {
                     q.profileTagsForbidden.add(value.trim().toLowerCase());
                  } else if (step == null) {
                     MillLog.error(q, "Reached line while not in a step: " + line);
                  } else if (key.equals("villager")) {
                     step.villager = value;
                  } else if (key.equals("duration")) {
                     step.duration = MillCommonUtilities.readInteger(value);
                  } else if (key.equals("showrequiredgoods")) {
                     step.showRequiredGoods = Boolean.parseBoolean(value);
                  } else if (key.startsWith("label_")) {
                     step.labels.put(key, value);
                  } else if (key.startsWith("description_success_")) {
                     step.descriptionsSuccess.put(key, value);
                  } else if (key.startsWith("description_refuse_")) {
                     step.descriptionsRefuse.put(key, value);
                  } else if (key.startsWith("description_timeup_")) {
                     step.descriptionsTimeUp.put(key, value);
                  } else if (key.startsWith("description_")) {
                     step.descriptions.put(key, value);
                  } else if (key.startsWith("listing_")) {
                     step.listings.put(key, value);
                  } else if (key.equals("requiredgood")) {
                     if (InvItem.INVITEMS_BY_NAME.containsKey(value.split(",")[0].toLowerCase())) {
                        InvItem iv = InvItem.INVITEMS_BY_NAME.get(value.split(",")[0].toLowerCase());
                        step.requiredGood.put(iv, MillCommonUtilities.readInteger(value.split(",")[1]));
                     } else {
                        MillLog.error(null, "Unknown requiredgood found when loading quest " + file.getName() + ": " + value);
                     }
                  } else if (key.equals("rewardgood")) {
                     if (InvItem.INVITEMS_BY_NAME.containsKey(value.split(",")[0].toLowerCase())) {
                        InvItem iv = InvItem.INVITEMS_BY_NAME.get(value.split(",")[0].toLowerCase());
                        step.rewardGoods.put(iv, MillCommonUtilities.readInteger(value.split(",")[1]));
                     } else {
                        MillLog.error(null, "Unknown rewardGood found when loading quest " + file.getName() + ": " + value);
                     }
                  } else if (key.equals("rewardmoney")) {
                     step.rewardMoney = MillCommonUtilities.readInteger(value);
                  } else if (key.equals("rewardreputation")) {
                     step.rewardReputation = MillCommonUtilities.readInteger(value);
                  } else if (key.equals("penaltyreputation")) {
                     step.penaltyReputation = MillCommonUtilities.readInteger(value);
                  } else if (key.equals("setactiondatasuccess")) {
                     step.setActionDataSuccess.add(value.split(","));
                  } else if (key.equals("relationchange")) {
                     try {
                        QuestStep.QuestStepRelationChange relationChange = QuestStep.QuestStepRelationChange.parseString(value);
                        step.relationChanges.add(relationChange);
                     } catch (Exception var9) {
                        MillLog.error(null, "Error when loading relationchange: " + var9.getMessage() + " when loading quest " + file.getName() + ": " + value);
                     }
                  } else if (key.equals("settagsuccess")) {
                     step.setVillagerTagsSuccess.add(value.split(","));
                  } else if (key.equals("cleartagsuccess")) {
                     step.clearTagsSuccess.add(value.split(","));
                  } else if (key.equals("settagfailure")) {
                     step.setVillagerTagsFailure.add(value.split(","));
                  } else if (key.equals("cleartagfailure")) {
                     step.clearTagsFailure.add(value.split(","));
                  } else if (key.equals("setglobaltagsuccess")) {
                     step.setGlobalTagsSuccess.add(value);
                  } else if (key.equals("clearglobaltagsuccess")) {
                     step.clearGlobalTagsSuccess.add(value);
                  } else if (key.equals("setglobaltagfailure")) {
                     step.setGlobalTagsFailure.add(value);
                  } else if (key.equals("clearglobaltagfailure")) {
                     step.clearGlobalTagsFailure.add(value);
                  } else if (key.equals("setplayertagsuccess")) {
                     step.setPlayerTagsSuccess.add(value);
                  } else if (key.equals("clearplayertagsuccess")) {
                     step.clearPlayerTagsSuccess.add(value);
                  } else if (key.equals("setplayertagfailure")) {
                     step.setPlayerTagsFailure.add(value);
                  } else if (key.equals("clearplayertagfailure")) {
                     step.clearPlayerTagsFailure.add(value);
                  } else if (key.equals("steprequiredglobaltag")) {
                     step.stepRequiredGlobalTag.add(value);
                  } else if (key.equals("stepforbiddenglobaltag")) {
                     step.forbiddenGlobalTag.add(value);
                  } else if (key.equals("steprequiredplayertag")) {
                     step.stepRequiredPlayerTag.add(value);
                  } else if (key.equals("stepforbiddenplayertag")) {
                     step.forbiddenPlayerTag.add(value);
                  } else if (key.equals("bedrockbuilding")) {
                     step.bedrockbuildings.add(value.trim().toLowerCase());
                  } else {
                     MillLog.error(null, "Unknown parameter when loading quest " + file.getName() + ": " + line);
                  }
               }
            }
         }

         reader.close();
         if (q.steps.size() == 0) {
            MillLog.error(q, "No steps found in " + file.getName() + ".");
            return null;
         } else if (q.villagersOrdered.size() == 0) {
            MillLog.error(q, "No villagers defined in " + file.getName() + ".");
            return null;
         } else {
            if (MillConfigValues.LogQuest >= 1) {
               MillLog.major(q, "Loaded quest type: " + q.key);
            }

            return q;
         }
      } catch (Exception var10) {
         MillLog.printException(var10);
         return null;
      }
   }

   public static void loadQuests() {
      VirtualDir questVirtualDir = Mill.virtualLoadingDir.getChildDirectory("quests");

      for (File file : questVirtualDir.listFilesRecursive(new MillCommonUtilities.ExtFileFilter("txt"))) {
         Quest quest = loadQuest(file);
         if (quest != null) {
            quests.put(quest.key, quest);
         }
      }

      if (MillConfigValues.LogQuest >= 1) {
         MillLog.major(null, "Loaded " + quests.size() + " quests.");
      }
   }

   private QuestVillager loadQVillager(String line) {
      QuestVillager v = new QuestVillager();

      for (String s : line.split(",")) {
         String key = s.split("=")[0].toLowerCase();
         String val = s.split("=")[1];
         if (key.equals("key")) {
            v.key = val;
         } else if (key.equals("type")) {
            Culture c = Culture.getCultureByName(val.split("/")[0]);
            if (c == null) {
               MillLog.error(this, "Unknown culture when loading definevillager: " + line);
               return null;
            }

            VillagerType vtype = c.getVillagerType(val.split("/")[1]);
            if (vtype == null) {
               MillLog.error(this, "Unknown villager type when loading definevillager: " + line);
               return null;
            }

            v.types.add(vtype.key);
         } else if (key.equals("relatedto")) {
            v.relatedto = val;
         } else if (key.equals("relation")) {
            v.relation = val;
         } else if (key.equals("forbiddentag")) {
            v.forbiddenTags.add(val);
         } else if (key.equals("requiredtag")) {
            v.requiredTags.add(val);
         } else {
            MillLog.error(this, "Could not understand setting in definevillager:" + key + ", in line: " + line);
         }
      }

      if (v.key == null) {
         MillLog.error(this, "No key found when loading definevillager: " + line);
         return null;
      } else {
         return v;
      }
   }

   public QuestInstance testQuest(MillWorldData mw, UserProfile profile) {
      if (!MillCommonUtilities.probability(this.chanceperhour)) {
         return null;
      } else {
         int nb = 0;

         for (QuestInstance qi : profile.questInstances) {
            if (qi.quest == this) {
               nb++;
            }
         }

         if (nb >= this.maxsimultaneous) {
            return null;
         } else {
            for (String tag : this.globalTagsRequired) {
               if (!mw.isGlobalTagSet(tag)) {
                  return null;
               }
            }

            for (String tagx : this.profileTagsRequired) {
               if (!profile.isTagSet(tagx)) {
                  return null;
               }
            }

            for (String tagxx : this.globalTagsForbidden) {
               if (mw.isGlobalTagSet(tagxx)) {
                  return null;
               }
            }

            for (String tagxxx : this.profileTagsForbidden) {
               if (profile.isTagSet(tagxxx)) {
                  return null;
               }
            }

            if (MillConfigValues.LogQuest >= 3) {
               MillLog.debug(this, "Testing quest " + this.key);
            }

            QuestVillager startingVillager = this.villagersOrdered.get(0);
            List<HashMap<String, QuestInstanceVillager>> possibleVillagers = new ArrayList<>();

            for (Point p : mw.getCombinedVillagesLoneBuildings()) {
               Building th = mw.getBuilding(p);
               if (th != null && th.isActive && th.getReputation(profile.getPlayer()) >= this.minreputation) {
                  if (MillConfigValues.LogQuest >= 3) {
                     MillLog.debug(this, "Looking for starting villager in: " + th.getVillageQualifiedName());
                  }

                  for (VillagerRecord vr : th.getAllVillagerRecords()) {
                     if (startingVillager.testVillager(profile, vr)) {
                        HashMap<String, QuestInstanceVillager> villagers = new HashMap<>();
                        villagers.put(startingVillager.key, new QuestInstanceVillager(mw, p, vr.getVillagerId(), vr));
                        boolean error = false;
                        if (MillConfigValues.LogQuest >= 3) {
                           MillLog.debug(this, "Found possible starting villager: " + vr);
                        }

                        for (QuestVillager qv : this.villagersOrdered) {
                           if (!error && qv != startingVillager) {
                              if (MillConfigValues.LogQuest >= 3) {
                                 MillLog.debug(this, "Trying to find villager type: " + qv.relation + "/" + qv.relatedto);
                              }

                              if (villagers.get(qv.relatedto) == null) {
                                 error = true;
                                 break;
                              }

                              VillagerRecord relatedVillager = villagers.get(qv.relatedto).getVillagerRecord(mw.world);
                              if (relatedVillager == null) {
                                 error = true;
                                 break;
                              }

                              if ("samevillage".equals(qv.relation)) {
                                 List<VillagerRecord> newVillagers = new ArrayList<>();

                                 for (VillagerRecord vr2 : mw.getBuilding(relatedVillager.getTownHallPos()).getAllVillagerRecords()) {
                                    if (!vr2.getHousePos().equals(relatedVillager.getHousePos()) && qv.testVillager(profile, vr2)) {
                                       newVillagers.add(vr2);
                                    }
                                 }

                                 if (newVillagers.size() > 0) {
                                    VillagerRecord chosen = newVillagers.get(MillCommonUtilities.randomInt(newVillagers.size()));
                                    villagers.put(qv.key, new QuestInstanceVillager(mw, p, chosen.getVillagerId(), chosen));
                                 } else {
                                    error = true;
                                 }
                              } else if (!"nearbyvillage".equals(qv.relation) && !"anyvillage".equals(qv.relation)) {
                                 if (!"samehouse".equals(qv.relation)) {
                                    MillLog.error(this, "Unknown relation: " + qv.relation);
                                 } else {
                                    List<VillagerRecord> newVillagers = new ArrayList<>();

                                    for (VillagerRecord vr2x : mw.getBuilding(relatedVillager.getTownHallPos()).getAllVillagerRecords()) {
                                       if (vr2x.getHousePos().equals(relatedVillager.getHousePos()) && qv.testVillager(profile, vr2x)) {
                                          newVillagers.add(vr2x);
                                       }
                                    }

                                    if (newVillagers.size() > 0) {
                                       VillagerRecord chosen = newVillagers.get(MillCommonUtilities.randomInt(newVillagers.size()));
                                       villagers.put(qv.key, new QuestInstanceVillager(mw, p, chosen.getVillagerId(), chosen));
                                    } else {
                                       error = true;
                                    }
                                 }
                              } else {
                                 List<QuestInstanceVillager> newVillagers = new ArrayList<>();

                                 for (Point p2 : mw.getCombinedVillagesLoneBuildings()) {
                                    Building th2 = mw.getBuilding(p2);
                                    if (th2 != null && th2 != th && ("anyvillage".equals(qv.relation) || th.getPos().distanceTo(th2.getPos()) < 2000.0)) {
                                       if (MillConfigValues.LogQuest >= 3) {
                                          MillLog.debug(
                                             this, "Trying to find villager type: " + qv.relation + "/" + qv.relatedto + " in " + th2.getVillageQualifiedName()
                                          );
                                       }

                                       for (VillagerRecord vr2xx : th2.getAllVillagerRecords()) {
                                          if (MillConfigValues.LogQuest >= 3) {
                                             MillLog.debug(this, "Testing: " + vr2xx);
                                          }

                                          if (qv.testVillager(profile, vr2xx)) {
                                             newVillagers.add(new QuestInstanceVillager(mw, p2, vr2xx.getVillagerId(), vr2xx));
                                          }
                                       }
                                    }
                                 }

                                 if (newVillagers.size() > 0) {
                                    villagers.put(qv.key, newVillagers.get(MillCommonUtilities.randomInt(newVillagers.size())));
                                 } else {
                                    error = true;
                                 }
                              }
                           }
                        }

                        if (!error) {
                           possibleVillagers.add(villagers);
                           if (MillConfigValues.LogQuest >= 3) {
                              MillLog.debug(this, "Found all the villagers needed: " + villagers.size());
                           }
                        }
                     }
                  }
               }
            }

            if (possibleVillagers.isEmpty()) {
               return null;
            } else {
               HashMap<String, QuestInstanceVillager> selectedOption = possibleVillagers.get(MillCommonUtilities.randomInt(possibleVillagers.size()));
               QuestInstance qix = new QuestInstance(mw, this, profile, selectedOption, mw.world.getWorldTime());
               profile.questInstances.add(qix);

               for (QuestInstanceVillager qiv : selectedOption.values()) {
                  profile.villagersInQuests.put(qiv.id, qix);
               }

               return qix;
            }
         }
      }
   }

   @Override
   public String toString() {
      return "QT: " + this.key;
   }
}
