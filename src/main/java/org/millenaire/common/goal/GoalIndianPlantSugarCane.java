package org.millenaire.common.goal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Plant sugarcane in a building with the sugar cane plantation tag.")
public class GoalIndianPlantSugarCane extends Goal {
   private static ItemStack[] SUGARCANE = new ItemStack[]{new ItemStack(Items.REEDS, 1)};

   public GoalIndianPlantSugarCane() {
      this.tags.add("tag_agriculture");
      this.icon = InvItem.createInvItem(Items.REEDS);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      List<Point> vp = new ArrayList<>();
      List<Point> buildingp = new ArrayList<>();

      for (Building plantation : villager.getTownHall().getBuildingsWithTag("sugarplantation")) {
         Point p = plantation.getResManager().getSugarCanePlantingLocation();
         if (p != null) {
            vp.add(p);
            buildingp.add(plantation.getPos());
         }
      }

      if (vp.isEmpty()) {
         return null;
      } else {
         Point p = vp.get(0);
         Point buildingP = buildingp.get(0);

         for (int i = 1; i < vp.size(); i++) {
            if (vp.get(i).horizontalDistanceToSquared(villager) < p.horizontalDistanceToSquared(villager)) {
               p = vp.get(i);
               buildingP = buildingp.get(i);
            }
         }

         return this.packDest(p, buildingP);
      }
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return SUGARCANE;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      int nbsimultaneous = 0;

      for (MillVillager v : villager.getTownHall().getKnownVillagers()) {
         if (v != villager && this.key.equals(v.goalKey)) {
            nbsimultaneous++;
         }
      }

      if (nbsimultaneous > 2) {
         return false;
      } else {
         boolean delayOver;
         if (!villager.lastGoalTime.containsKey(this)) {
            delayOver = true;
         } else {
            delayOver = villager.world.getWorldTime() > villager.lastGoalTime.get(this) + 2000L;
         }

         for (Building kiln : villager.getTownHall().getBuildingsWithTag("sugarplantation")) {
            int nb = kiln.getResManager().getNbSugarCanePlantingLocation();
            if (nb > 0 && delayOver) {
               return true;
            }

            if (nb > 4) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) {
      Block block = villager.getBlock(villager.getGoalDestPoint());
      Point cropPoint = villager.getGoalDestPoint().getAbove();
      block = villager.getBlock(cropPoint);
      if (block == Blocks.AIR || block == Blocks.LEAVES) {
         villager.setBlock(cropPoint, Blocks.REEDS);
         villager.swingArm(EnumHand.MAIN_HAND);
      }

      return true;
   }

   @Override
   public int priority(MillVillager villager) {
      int p = 120;

      for (MillVillager v : villager.getTownHall().getKnownVillagers()) {
         if (this.key.equals(v.goalKey)) {
            p /= 2;
         }
      }

      return p;
   }
}
