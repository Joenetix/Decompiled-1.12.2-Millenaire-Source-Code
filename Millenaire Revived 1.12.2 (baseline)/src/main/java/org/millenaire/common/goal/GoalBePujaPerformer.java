package org.millenaire.common.goal;

import net.minecraft.entity.player.EntityPlayer;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Go to the temple to be available for pujas or sacrifices.")
public class GoalBePujaPerformer extends Goal {
   public static final int sellingRadius = 7;

   public GoalBePujaPerformer() {
      this.travelBookShow = false;
      this.floatingIcon = InvItem.createInvItem(MillItems.INDIAN_STATUE);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      Building temple = null;
      if (villager.canMeditate()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      if (temple != null && temple.pujas != null && (temple.pujas.priest == null || temple.pujas.priest == villager)) {
         if (MillConfigValues.LogPujas >= 3) {
            MillLog.debug(villager, "Destination for bepujaperformer: " + temple);
         }

         return this.packDest(temple.getResManager().getCraftingPos(), temple);
      } else {
         return null;
      }
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      Building temple = null;
      if (villager.canMeditate()) {
         if (!villager.mw.isGlobalTagSet("pujas")) {
            return false;
         }

         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         if (!villager.mw.isGlobalTagSet("mayansacrifices")) {
            return false;
         }

         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      if (temple == null) {
         return false;
      } else {
         EntityPlayer player = villager.world
            .getClosestPlayer(
               temple.getResManager().getCraftingPos().getiX(),
               temple.getResManager().getCraftingPos().getiY(),
               temple.getResManager().getCraftingPos().getiZ(),
               7.0,
               false
            );
         boolean valid = player != null && temple.getResManager().getCraftingPos().distanceTo(player) < 7.0;
         return !valid ? false : this.getDestination(villager) != null;
      }
   }

   @Override
   public boolean isStillValidSpecific(MillVillager villager) throws Exception {
      Building temple = null;
      if (villager.canMeditate()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      if (temple == null) {
         return false;
      } else {
         EntityPlayer player = villager.world
            .getClosestPlayer(
               temple.getResManager().getCraftingPos().getiX(),
               temple.getResManager().getCraftingPos().getiY(),
               temple.getResManager().getCraftingPos().getiZ(),
               7.0,
               false
            );
         boolean valid = player != null && temple.getResManager().getCraftingPos().distanceTo(player) < 7.0;
         if (!valid && MillConfigValues.LogPujas >= 1) {
            MillLog.major(this, "Be Puja Performer no longer valid.");
         }

         return valid && !temple.pujas.canPray();
      }
   }

   @Override
   public String labelKey(MillVillager villager) {
      return villager != null && villager.canPerformSacrifices() ? "besacrificeperformer" : this.key;
   }

   @Override
   public String labelKeyWhileTravelling(MillVillager villager) {
      return villager != null && villager.canPerformSacrifices() ? "besacrificeperformer" : this.key;
   }

   @Override
   public boolean lookAtPlayer() {
      return true;
   }

   @Override
   public void onAccept(MillVillager villager) {
      Building temple = null;
      if (villager.canMeditate()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      if (temple != null) {
         EntityPlayer player = villager.world
            .getClosestPlayer(
               temple.getResManager().getCraftingPos().getiX(),
               temple.getResManager().getCraftingPos().getiY(),
               temple.getResManager().getCraftingPos().getiZ(),
               7.0,
               false
            );
         if (villager.canMeditate()) {
            ServerSender.sendTranslatedSentence(player, 'f', "pujas.priestcoming", villager.getName());
         } else if (villager.canPerformSacrifices()) {
            ServerSender.sendTranslatedSentence(player, 'f', "sacrifices.priestcoming", villager.getName());
         }
      }
   }

   @Override
   public boolean performAction(MillVillager villager) {
      Building temple = null;
      if (villager.canMeditate()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("pujas");
      } else if (villager.canPerformSacrifices()) {
         temple = villager.getTownHall().getFirstBuildingWithTag("sacrifices");
      }

      if (temple == null) {
         return true;
      } else {
         temple.pujas.priest = villager;
         return temple.pujas.canPray();
      }
   }

   @Override
   public int priority(MillVillager villager) {
      return 300;
   }

   @Override
   public int range(MillVillager villager) {
      return 2;
   }
}
