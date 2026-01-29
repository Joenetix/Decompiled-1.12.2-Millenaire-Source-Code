package org.millenaire.common.goal;

import net.minecraft.block.BlockCocoa;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.Point;

@DocumentedElement.Documentation("Goal that harvests ripe cacao.")
public class GoalHarvestCacao extends Goal {
   private static ItemStack[] CACAO = new ItemStack[]{new ItemStack(Items.DYE, 1, 3)};

   public GoalHarvestCacao() {
      this.icon = InvItem.createInvItem(Items.DYE, 3);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      Point p = villager.getHouse().getResManager().getCocoaHarvestLocation();
      return this.packDest(p, villager.getHouse());
   }

   @Override
   public ItemStack[] getHeldItemsOffHandDestination(MillVillager villager) {
      return CACAO;
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
      Point cropPoint = villager.getGoalDestPoint();
      if (cropPoint.getBlock(villager.world) == Blocks.COCOA) {
         IBlockState bs = cropPoint.getBlockActualState(villager.world);
         if ((Integer)bs.getValue(BlockCocoa.AGE) >= 2) {
            villager.setBlockAndMetadata(cropPoint, Blocks.AIR, 0);
            int nbcrop = 2;
            float irrigation = villager.getTownHall().getVillageIrrigation();
            double rand = Math.random();
            if (rand < irrigation / 100.0F) {
               nbcrop++;
            }

            villager.addToInv(Items.DYE, 3, nbcrop);
            villager.swingArm(EnumHand.MAIN_HAND);
         }
      }

      return true;
   }

   @Override
   public int priority(MillVillager villager) {
      return 100;
   }
}
