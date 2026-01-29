package org.millenaire.common.goal;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.Point;

@DocumentedElement.Documentation("Plant cacao seeds at home.")
public class GoalPlantCacao extends Goal {
   private static ItemStack[] cacao = new ItemStack[]{new ItemStack(Blocks.COCOA, 1)};

   public GoalPlantCacao() {
      this.icon = InvItem.createInvItem(Items.DYE, 3);
   }

   private int getCocoaMeta(World world, Point p) {
      Block var5 = p.getRelative(0.0, 0.0, -1.0).getBlock(world);
      Block var6 = p.getRelative(0.0, 0.0, 1.0).getBlock(world);
      Block var7 = p.getRelative(-1.0, 0.0, 0.0).getBlock(world);
      Block var8 = p.getRelative(1.0, 0.0, 0.0).getBlock(world);
      byte meta = 0;
      if (var5 == Blocks.LOG) {
         meta = 2;
      }

      if (var6 == Blocks.LOG) {
         meta = 0;
      }

      if (var7 == Blocks.LOG) {
         meta = 1;
      }

      if (var8 == Blocks.LOG) {
         meta = 3;
      }

      return meta;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      Point p = villager.getHouse().getResManager().getCocoaPlantingLocation();
      return this.packDest(p, villager.getHouse());
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return cacao;
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
      Point cropPoint = villager.getGoalDestPoint();
      block = villager.getBlock(cropPoint);
      if (block == Blocks.AIR) {
         villager.setBlockAndMetadata(cropPoint, Blocks.COCOA, this.getCocoaMeta(villager.world, cropPoint));
         villager.swingArm(EnumHand.MAIN_HAND);
      }

      return true;
   }

   @Override
   public int priority(MillVillager villager) {
      return 120;
   }
}
