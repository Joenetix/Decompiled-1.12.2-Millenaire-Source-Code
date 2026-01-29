package org.millenaire.common.goal.generic;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class GoalGenericVisit extends GoalGeneric {
   public static final String GOAL_TYPE = "visit";
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.POS_TYPE, defaultValue = "sleeping")
   @ConfigAnnotations.FieldDocumentation(explanation = "Pos type where the goal occurs. Defaults to sleeping pos.")
   public AnnotedParameter.PosType targetPosition;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_LIST)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "If set, the goal will have villagers doing one of the provided goal as destination. Replaces buildings as destination."
   )
   public List<String> targetVillagerGoals = null;

   @Override
   public void applyDefaultSettings() {
      this.travelBookShow = false;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      if (this.targetVillagerGoals != null) {
         List<MillVillager> targets = new ArrayList<>();

         for (MillVillager v : villager.getTownHall().getKnownVillagers()) {
            if (v != villager && v.goalKey != null && this.validGoalForTargetting(v.goalKey)) {
               targets.add(v);
            }
         }

         return targets.isEmpty() ? null : this.packDest(null, null, (Entity)targets.get(MillCommonUtilities.randomInt(targets.size())));
      } else {
         for (Building dest : this.getBuildings(villager)) {
            if (this.isDestPossible(villager, dest)) {
               return this.packDest(this.targetPosition.getPosition(dest), dest);
            }
         }

         return null;
      }
   }

   @Override
   public String getTypeLabel() {
      return "visit";
   }

   @Override
   public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
      return villager.getPos().distanceTo(b.getResManager().getCraftingPos()) > 5.0;
   }

   @Override
   public boolean isPossibleGenericGoal(MillVillager villager) throws Exception {
      return true;
   }

   @Override
   protected boolean isStillValidSpecific(MillVillager villager) throws Exception {
      if (villager.getGoalDestEntity() != null && villager.getGoalDestEntity().isDead) {
         return false;
      } else {
         if (this.targetVillagerGoals != null && villager.getGoalDestEntity() != null && villager.getGoalDestEntity() instanceof MillVillager) {
            MillVillager targetVillager = (MillVillager)villager.getGoalDestEntity();
            if (targetVillager.goalKey == null || !this.validGoalForTargetting(targetVillager.goalKey)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      return true;
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return super.priority(villager);
   }

   @Override
   public boolean validateGoal() {
      return true;
   }

   private boolean validGoalForTargetting(String goalKey) {
      if (this.targetVillagerGoals == null) {
         return false;
      } else {
         Goal goal = Goal.goals.get(goalKey);
         if (goal == null) {
            MillLog.error(this, "Villager had unknown goal: " + goalKey);
            return false;
         } else {
            for (String target : this.targetVillagerGoals) {
               if (goal.tags.contains(target)) {
                  return true;
               }
            }

            return false;
         }
      }
   }
}
