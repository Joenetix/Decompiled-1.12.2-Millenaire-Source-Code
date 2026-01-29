package org.millenaire.common.goal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Perform a puja or a Maya sacrifice.")
public class GoalPerformPuja extends Goal {
   public GoalPerformPuja() {
      this.icon = InvItem.createInvItem(MillItems.INDIAN_STATUE);
      this.floatingIcon = this.icon;
   }

   @Override
   public int actionDuration(MillVillager villager) throws Exception {
      return 5;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      Building temple = null;
      if (villager.canMeditate()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      return temple != null && temple.pujas != null && (temple.pujas.priest == null || temple.pujas.priest == villager) && temple.pujas.canPray()
         ? this.packDest(temple.getResManager().getCraftingPos(), temple)
         : null;
   }

   @Override
   public ItemStack[] getHeldItemsDestination(MillVillager villager) {
      Building temple = null;
      if (villager.canMeditate()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      return temple.pujas.getStackInSlot(0) != null ? new ItemStack[]{temple.pujas.getStackInSlot(0)} : null;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws Exception {
      if (villager.canMeditate()) {
         if (!villager.mw.isGlobalTagSet("pujas")) {
            return false;
         }
      } else if (villager.canPerformSacrifices() && !villager.mw.isGlobalTagSet("mayansacrifices")) {
         return false;
      }

      return this.getDestination(villager) != null;
   }

   @Override
   public String labelKey(MillVillager villager) {
      return villager != null && villager.canPerformSacrifices() ? "performsacrifices" : this.key;
   }

   @Override
   public String labelKeyWhileTravelling(MillVillager villager) {
      return villager != null && villager.canPerformSacrifices() ? "performsacrifices" : this.key;
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      Building temple = null;
      if (villager.canMeditate()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      boolean canContinue = temple.pujas.performPuja(villager);
      EntityPlayer player = villager.world.getClosestPlayerToEntity(villager, 16.0);
      if (player != null) {
         temple.sendBuildingPacket(player, false);
      }

      return !canContinue;
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 500;
   }

   @Override
   public boolean swingArms() {
      return true;
   }
}
