package org.millenaire.common.goal.generic;

import java.util.HashMap;
import net.minecraft.item.ItemStack;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

public class GoalGenericCrafting extends GoalGeneric {
   public static final String GOAL_TYPE = "crafting";
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_NUMBER_ADD)
   @ConfigAnnotations.FieldDocumentation(explanation = "Each action will require and use all the inputs.")
   public HashMap<InvItem, Integer> input = new HashMap<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_NUMBER_ADD)
   @ConfigAnnotations.FieldDocumentation(explanation = "Each action will produce all the outputs.")
   public HashMap<InvItem, Integer> output = new HashMap<>();

   @Override
   public void applyDefaultSettings() {
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      for (Building dest : this.getBuildings(villager)) {
         if (this.isDestPossible(villager, dest)) {
            return this.packDest(dest.getResManager().getCraftingPos(), dest);
         }
      }

      return null;
   }

   @Override
   public ItemStack getIcon() {
      if (this.icon != null) {
         return this.icon.getItemStack();
      } else {
         return !this.output.isEmpty() ? this.output.keySet().iterator().next().getItemStack() : null;
      }
   }

   @Override
   public String getTypeLabel() {
      return "crafting";
   }

   @Override
   public boolean isDestPossibleSpecific(MillVillager villager, Building b) {
      for (InvItem item : this.input.keySet()) {
         if (villager.countInv(item) + b.countGoods(item) < this.input.get(item)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean isPossibleGenericGoal(MillVillager villager) throws Exception {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      Building dest = villager.getGoalBuildingDest();
      if (dest == null) {
         return true;
      } else {
         for (InvItem item : this.input.keySet()) {
            if (villager.countInv(item) + dest.countGoods(item) < this.input.get(item)) {
               return true;
            }
         }

         for (InvItem itemx : this.input.keySet()) {
            int nbTaken = villager.takeFromInv(itemx, this.input.get(itemx));
            if (nbTaken < this.input.get(itemx)) {
               dest.takeGoods(itemx, this.input.get(itemx) - nbTaken);
            }
         }

         for (InvItem itemxx : this.output.keySet()) {
            dest.storeGoods(itemxx, this.output.get(itemxx));
         }

         if (this.sound != null) {
            WorldUtilities.playSoundByMillName(villager.world, villager.getPos(), this.sound, 1.0F);
         }

         return true;
      }
   }

   @Override
   public boolean swingArms() {
      return true;
   }

   @Override
   public boolean validateGoal() {
      if (this.output.isEmpty()) {
         MillLog.error(this, "Generic crafting goals require at least one output.");
         return false;
      } else {
         return true;
      }
   }
}
