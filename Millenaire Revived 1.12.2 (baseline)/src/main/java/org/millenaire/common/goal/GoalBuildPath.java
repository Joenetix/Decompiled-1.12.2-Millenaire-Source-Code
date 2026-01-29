package org.millenaire.common.goal;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.buildingplan.BuildingBlock;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.utilities.MillLog;

@DocumentedElement.Documentation("Build village paths.")
public class GoalBuildPath extends Goal {
   public GoalBuildPath() {
      this.maxSimultaneousTotal = 1;
      this.tags.add("tag_construction");
      this.icon = InvItem.createInvItem(MillBlocks.PATHDIRT);
   }

   @Override
   public int actionDuration(MillVillager villager) {
      int toolEfficiency = (int)villager.getBestShovel().getDestroySpeed(new ItemStack(villager.getBestShovel(), 1), Blocks.DIRT.getDefaultState());
      return 10 - toolEfficiency;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      BuildingBlock b = villager.getTownHall().getCurrentPathBuildingBlock();
      return b != null ? this.packDest(b.p) : null;
   }

   @Override
   public ItemStack[] getHeldItemsOffHandTravelling(MillVillager villager) {
      BuildingBlock bblock = villager.getTownHall().getCurrentPathBuildingBlock();
      return bblock != null && bblock.block != Blocks.AIR
         ? new ItemStack[]{new ItemStack(Item.getItemFromBlock(bblock.block), 1, bblock.getMeta())}
         : null;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return villager.getBestShovelStack();
   }

   @Override
   public AStarConfig getPathingConfig(MillVillager villager) {
      return !villager.canVillagerClearLeaves() ? JPS_CONFIG_BUILDING_NO_LEAVES : JPS_CONFIG_BUILDING;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      return MillConfigValues.BuildVillagePaths && villager.getTownHall().getCurrentPathBuildingBlock() != null;
   }

   @Override
   public boolean isStillValidSpecific(MillVillager villager) throws Exception {
      return villager.getTownHall().getCurrentPathBuildingBlock() != null;
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      BuildingBlock bblock = villager.getTownHall().getCurrentPathBuildingBlock();
      if (bblock == null) {
         return true;
      } else {
         if (MillConfigValues.LogVillagePaths >= 3) {
            MillLog.debug(villager, "Building path block: " + bblock);
         }

         bblock.pathBuild(villager.getTownHall());
         villager.getTownHall().pathsToBuildPathIndex++;
         BuildingBlock b = villager.getTownHall().getCurrentPathBuildingBlock();
         villager.swingArm(EnumHand.MAIN_HAND);
         if (b != null) {
            villager.setGoalDestPoint(b.p);
            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 50;
   }

   @Override
   public int range(MillVillager villager) {
      return 5;
   }

   @Override
   public boolean stopMovingWhileWorking() {
      return false;
   }

   @Override
   public boolean unreachableDestination(MillVillager villager) throws Exception {
      this.performAction(villager);
      return true;
   }
}
