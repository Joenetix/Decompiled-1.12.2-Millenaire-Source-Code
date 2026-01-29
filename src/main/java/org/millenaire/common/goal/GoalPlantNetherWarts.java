package org.millenaire.common.goal;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.Point;

@DocumentedElement.Documentation("Plant nether warts at home (for free).")
public class GoalPlantNetherWarts extends Goal {
   private static ItemStack[] WARTS = new ItemStack[]{new ItemStack(Items.NETHER_WART, 1)};

   public GoalPlantNetherWarts() {
      this.icon = InvItem.createInvItem(Items.NETHER_WART);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      return this.packDest(villager.getHouse().getResManager().getNetherWartsPlantingLocation(), villager.getHouse());
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return WARTS;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      return this.getDestination(villager).getDest() != null;
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
      if (block == Blocks.AIR) {
         villager.setBlockAndMetadata(cropPoint, Blocks.NETHER_WART, 0);
         villager.swingArm(EnumHand.MAIN_HAND);
      }

      return true;
   }

   @Override
   public int priority(MillVillager villager) {
      return 100;
   }
}
