package org.millenaire.common.goal.generic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.annotedparameters.ParametersManager;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.Building;

public abstract class GoalGeneric extends Goal implements ParametersManager.DefaultValueOverloaded {
   public static final String GOAL_TYPE_FIELD_NAME = "GOAL_TYPE";
   public static final Class[] GENERIC_GOAL_CLASSES = new Class[]{
      GoalGenericCooking.class,
      GoalGenericTendFurnace.class,
      GoalGenericTakeFromBuilding.class,
      GoalGenericCrafting.class,
      GoalGenericSlaughterAnimal.class,
      GoalGenericHarvestCrop.class,
      GoalGenericPlantCrop.class,
      GoalGenericVisit.class,
      GoalGenericMining.class,
      GoalGenericGatherBlocks.class,
      GoalGenericPlantSapling.class
   };
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
   @ConfigAnnotations.FieldDocumentation(explanation = "Tag a building must have for action to be possible. If absent, then the villager's house is used.")
   public String buildingTag = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
   @ConfigAnnotations.FieldDocumentation(explanation = "Extra tag required for the destination to be valid.")
   public String requiredTag = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(explanation = "If true, this goal happens in the central building.")
   public boolean townHallGoal;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "50")
   @ConfigAnnotations.FieldDocumentation(explanation = "The goal's priority. The higher it is the more likely villagers will pick it.")
   public int priority;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "10")
   @ConfigAnnotations.FieldDocumentation(explanation = "A random value between 0 and this to add to the goal's priority.")
   public int priorityRandom;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.MILLISECONDS, defaultValue = "5000")
   @ConfigAnnotations.FieldDocumentation(explanation = "Duration in ms of the action.")
   public int duration = 100;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
   @ConfigAnnotations.FieldDocumentation(explanation = "Specify if the sentences for this goal is not the name of the goal itself.")
   public String sentenceKey = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
   @ConfigAnnotations.FieldDocumentation(explanation = "Specify if the label for this goal is not the name of the goal itself.")
   public String labelKey = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.ITEMSTACK_ARRAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Items held by the villager, including when traveling.")
   public ItemStack[] heldItems = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.ITEMSTACK_ARRAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Items held by the villager in his off hand, including when traveling.")
   public ItemStack[] heldItemsOffHand = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.ITEMSTACK_ARRAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Items held by the villager, at destination only.")
   public ItemStack[] heldItemsDestination = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.ITEMSTACK_ARRAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Items held by the villager in his off hand, at destination only.")
   public ItemStack[] heldItemsOffHandDestination = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
   @ConfigAnnotations.FieldDocumentation(explanation = "Sound to play when the goal is performed (metal, wool...).")
   public String sound = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(explanation = "If true, the villager can move 'randomly' after reaching the destination.")
   public boolean allowRandomMoves;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.MILLISECONDS, defaultValue = "5000")
   @ConfigAnnotations.FieldDocumentation(explanation = "Duration in ms before the action can reoccur.")
   public int reoccurDelay = 0;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(explanation = "If true, the villager will hold his best weapon while doing this goal.")
   public boolean holdWeapons;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Whether the villager should look at the goal's destination. True or false by default depending on the generic goal."
   )
   public boolean lookAtGoal;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "3")
   @ConfigAnnotations.FieldDocumentation(explanation = "Range from destination from which the goal is doable.")
   public int range;
   File file = null;

   private static List<File> getGenericGoalFiles(String directoryName) {
      VirtualDir virtualGoalDir = Mill.virtualLoadingDir.getChildDirectory("goals").getChildDirectory(directoryName);
      return virtualGoalDir.listFilesRecursive(new MillCommonUtilities.ExtFileFilter("txt"));
   }

   public static void loadGenericGoals() {
      for (Class<?> genericGoalClass : GENERIC_GOAL_CLASSES) {
         try {
            String goalType = (String)genericGoalClass.getField("GOAL_TYPE").get(null);

            for (File file : getGenericGoalFiles("generic" + goalType)) {
               try {
                  GoalGeneric goal = (GoalGeneric)genericGoalClass.newInstance();
                  String key = file.getName().split("\\.")[0].toLowerCase();
                  goal.file = file;
                  ParametersManager.loadAnnotedParameterData(file, goal, null, "generic " + goal.getTypeLabel() + " goal", null);
                  goal.applyDefaultSettings();
                  if (goal != null && goal.validateGoal()) {
                     if (MillConfigValues.LogGeneralAI >= 1) {
                        MillLog.major(goal, "loaded " + goalType + " goal");
                     }

                     goals.put(key, goal);
                  }
               } catch (Exception var9) {
                  MillLog.printException(var9);
               }
            }
         } catch (Exception var10) {
            MillLog.printException("Exception when loading generic goal type:", var10);
         }
      }
   }

   @Override
   public int actionDuration(MillVillager villager) throws Exception {
      return this.duration;
   }

   @Override
   public boolean allowRandomMoves() {
      return this.allowRandomMoves;
   }

   public List<Building> getBuildings(MillVillager villager) {
      List<Building> buildings = new ArrayList<>();
      if (this.townHallGoal) {
         if (this.requiredTag == null || villager.getTownHall().containsTags(this.requiredTag)) {
            buildings.add(villager.getTownHall());
         }
      } else if (this.buildingTag == null) {
         if (this.requiredTag == null || villager.getHouse().containsTags(this.requiredTag)) {
            buildings.add(villager.getHouse());
         }
      } else {
         for (Building b : villager.getTownHall().getBuildingsWithTag(this.buildingTag)) {
            if (this.requiredTag == null || b.containsTags(this.requiredTag)) {
               buildings.add(b);
            }
         }
      }

      return buildings;
   }

   @Override
   public ItemStack[] getHeldItemsDestination(MillVillager villager) throws Exception {
      if (this.holdWeapons) {
         return new ItemStack[]{villager.getWeapon()};
      } else {
         return this.heldItemsDestination != null ? this.heldItemsDestination : this.heldItems;
      }
   }

   @Override
   public ItemStack[] getHeldItemsOffHandDestination(MillVillager villager) throws Exception {
      return this.heldItemsOffHandDestination != null ? this.heldItemsOffHandDestination : this.heldItemsOffHand;
   }

   @Override
   public ItemStack[] getHeldItemsOffHandTravelling(MillVillager villager) throws Exception {
      return this.heldItemsOffHand;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) throws Exception {
      return this.holdWeapons ? new ItemStack[]{villager.getWeapon()} : this.heldItems;
   }

   public abstract String getTypeLabel();

   public final boolean isDestPossible(MillVillager villager, Building dest) throws MillLog.MillenaireException {
      return this.validateDest(villager, dest) && this.isDestPossibleSpecific(villager, dest);
   }

   public abstract boolean isDestPossibleSpecific(MillVillager var1, Building var2);

   public abstract boolean isPossibleGenericGoal(MillVillager var1) throws Exception;

   @Override
   public final boolean isPossibleSpecific(MillVillager villager) throws Exception {
      if (this.reoccurDelay > 0
         && villager.lastGoalTime.containsKey(this)
         && villager.lastGoalTime.get(this) + this.reoccurDelay > villager.world.getWorldTime()) {
         return false;
      } else if (!this.isPossibleGenericGoal(villager)) {
         return false;
      } else {
         List<Building> buildings = this.getBuildings(villager);
         boolean destFound = false;
         if (!buildings.isEmpty()) {
            for (Building dest : buildings) {
               if (!destFound) {
                  destFound = this.isDestPossible(villager, dest);
               }
            }

            return destFound;
         } else {
            return false;
         }
      }
   }

   @Override
   public String labelKey(MillVillager villager) {
      return this.labelKey == null ? this.key : this.labelKey;
   }

   @Override
   public String labelKeyWhileTravelling(MillVillager villager) {
      return this.labelKey == null ? this.key : this.labelKey;
   }

   @Override
   public final boolean lookAtGoal() {
      return this.lookAtGoal;
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return this.priority + villager.getRNG().nextInt(this.priorityRandom);
   }

   @Override
   public int range(MillVillager villager) {
      return this.range;
   }

   @Override
   public String sentenceKey() {
      return this.sentenceKey == null ? this.key : this.sentenceKey;
   }

   @Override
   public String toString() {
      if (this.key != null) {
         return "goal:" + this.key;
      } else {
         return this.file != null ? "goal:" + this.file.getName() : "goal:unknownkey";
      }
   }

   public abstract boolean validateGoal();
}
