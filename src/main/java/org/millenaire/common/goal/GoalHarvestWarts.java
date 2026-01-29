package org.millenaire.common.goal;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.Point;

@DocumentedElement.Documentation("Harvest grown nether warts froom home.")
public class GoalHarvestWarts extends Goal {
   private static ItemStack[] WARTS = new ItemStack[]{new ItemStack(Items.NETHER_WART, 1)};

   public GoalHarvestWarts() {
      this.icon = InvItem.createInvItem(Items.NETHER_WART);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      return this.packDest(villager.getHouse().getResManager().getNetherWartsHarvestLocation(), villager.getHouse());
   }

   @Override
   public ItemStack[] getHeldItemsOffHandDestination(MillVillager villager) {
      return WARTS;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return villager.getBestHoeStack();
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
      Point cropPoint = villager.getGoalDestPoint().getAbove();
      if (villager.getBlock(cropPoint) == Blocks.NETHER_WART && villager.getBlockMeta(cropPoint) == 3) {
         villager.setBlockAndMetadata(cropPoint, Blocks.AIR, 0);
         villager.getHouse().storeGoods(Items.NETHER_WART, 1);
         villager.swingArm(EnumHand.MAIN_HAND);
      }

      return true;
   }

   @Override
   public int priority(MillVillager villager) {
      return 100 - villager.getHouse().countGoods(Items.NETHER_WART) * 4;
   }
}
