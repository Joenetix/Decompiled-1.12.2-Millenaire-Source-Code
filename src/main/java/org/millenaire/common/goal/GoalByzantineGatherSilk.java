package org.millenaire.common.goal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.block.BlockSilkWorm;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Gather ripe silk from a silk block.")
public class GoalByzantineGatherSilk extends Goal {
   private static ItemStack[] SHEARS = new ItemStack[]{new ItemStack(Items.SHEARS, 1)};
   private static ItemStack[] SILK = new ItemStack[]{new ItemStack(MillItems.SILK, 1)};

   public GoalByzantineGatherSilk() {
      this.maxSimultaneousInBuilding = 2;
      this.buildingLimit.put(InvItem.createInvItem(MillItems.SILK), 128);
      this.townhallLimit.put(InvItem.createInvItem(MillItems.SILK), 128);
      this.icon = InvItem.createInvItem(MillItems.SILK);
   }

   @Override
   public int actionDuration(MillVillager villager) {
      return 20;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      List<Point> vp = new ArrayList<>();
      List<Point> buildingp = new ArrayList<>();

      for (Building silkFarm : villager.getTownHall().getBuildingsWithTag("silkwormfarm")) {
         Point p = silkFarm.getResManager().getSilkwormHarvestLocation();
         if (p != null) {
            vp.add(p);
            buildingp.add(silkFarm.getPos());
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
      return SILK;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return SHEARS;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      boolean delayOver;
      if (!villager.lastGoalTime.containsKey(this)) {
         delayOver = true;
      } else {
         delayOver = villager.world.getWorldTime() > villager.lastGoalTime.get(this) + 2000L;
      }

      for (Building kiln : villager.getTownHall().getBuildingsWithTag("silkwormfarm")) {
         int nb = kiln.getResManager().getNbSilkWormHarvestLocation();
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
      if (WorldUtilities.getBlock(villager.world, villager.getGoalDestPoint()) == MillBlocks.SILK_WORM
         && WorldUtilities.getBlockState(villager.world, villager.getGoalDestPoint()).getValue(BlockSilkWorm.PROGRESS)
            == BlockSilkWorm.EnumType.SILKWORMFULL) {
         villager.addToInv(MillItems.SILK, 0, 1);
         villager.setBlockAndMetadata(villager.getGoalDestPoint(), MillBlocks.SILK_WORM, 0);
         villager.swingArm(EnumHand.MAIN_HAND);
         return false;
      } else {
         return true;
      }
   }

   @Override
   public int priority(MillVillager villager) {
      int p = 100 - villager.getTownHall().nbGoodAvailable(InvItem.createInvItem(MillItems.SILK, 1), false, false, false) * 2;

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
