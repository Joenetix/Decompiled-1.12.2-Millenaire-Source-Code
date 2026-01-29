package org.millenaire.common.goal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.block.BlockSnailSoil;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Gather snails from a snail soil block to make purple dye.")
public class GoalByzantineGatherSnails extends Goal {
   private static ItemStack[] PURPLE_DYE = new ItemStack[]{new ItemStack(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage())};

   public GoalByzantineGatherSnails() {
      this.maxSimultaneousInBuilding = 2;
      this.buildingLimit.put(InvItem.createInvItem(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage()), 128);
      this.townhallLimit.put(InvItem.createInvItem(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage()), 128);
      this.icon = InvItem.createInvItem(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage());
   }

   @Override
   public int actionDuration(MillVillager villager) {
      return 20;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      List<Point> vp = new ArrayList<>();
      List<Point> buildingp = new ArrayList<>();

      for (Building snailFamr : villager.getTownHall().getBuildingsWithTag("snailsfarm")) {
         Point p = snailFamr.getResManager().getSnailSoilHarvestLocation();
         if (p != null) {
            vp.add(p);
            buildingp.add(snailFamr.getPos());
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
   public ItemStack[] getHeldItemsOffHandDestination(MillVillager villager) {
      return PURPLE_DYE;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return villager.getBestShovelStack();
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      boolean delayOver;
      if (!villager.lastGoalTime.containsKey(this)) {
         delayOver = true;
      } else {
         delayOver = villager.world.getWorldTime() > villager.lastGoalTime.get(this) + 2000L;
      }

      for (Building kiln : villager.getTownHall().getBuildingsWithTag("snailsfarm")) {
         int nb = kiln.getResManager().getNbSnailSoilHarvestLocation();
         if (nb > 0 && delayOver) {
            return true;
         }

         if (nb > 4) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) {
      if (WorldUtilities.getBlock(villager.world, villager.getGoalDestPoint()) == MillBlocks.SNAIL_SOIL
         && WorldUtilities.getBlockState(villager.world, villager.getGoalDestPoint()).getValue(BlockSnailSoil.PROGRESS)
            == BlockSnailSoil.EnumType.SNAIL_SOIL_FULL) {
         villager.addToInv(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage(), 1);
         villager.setBlockAndMetadata(villager.getGoalDestPoint(), MillBlocks.SNAIL_SOIL, 0);
         villager.swingArm(EnumHand.MAIN_HAND);
         return false;
      } else {
         return true;
      }
   }

   @Override
   public int priority(MillVillager villager) {
      int p = 100
         - villager.getTownHall().nbGoodAvailable(InvItem.createInvItem(Items.DYE, EnumDyeColor.PURPLE.getDyeDamage()), false, false, false) * 2;

      for (MillVillager v : villager.getTownHall().getKnownVillagers()) {
         if (this.key.equals(v.goalKey)) {
            p /= 2;
         }
      }

      return p;
   }

   @Override
   public boolean unreachableDestination(MillVillager villager) {
      return false;
   }
}
