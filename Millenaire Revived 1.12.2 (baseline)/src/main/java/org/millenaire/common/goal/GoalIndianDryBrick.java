package org.millenaire.common.goal;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Go and dry bricks in a building with the kiln tag.")
public class GoalIndianDryBrick extends Goal {
   public GoalIndianDryBrick() {
      this.maxSimultaneousInBuilding = 1;
      this.tags.add("tag_construction");
      this.icon = InvItem.createInvItem(MillBlocks.BS_WET_BRICK);
   }

   @Override
   public int actionDuration(MillVillager villager) {
      return 20;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws MillLog.MillenaireException {
      boolean minimumBricksNotRequired;
      if (villager.goalKey != null && villager.goalKey.equals(this.key)) {
         minimumBricksNotRequired = true;
      } else if (!villager.lastGoalTime.containsKey(this)) {
         minimumBricksNotRequired = true;
      } else {
         minimumBricksNotRequired = villager.world.getWorldTime() > villager.lastGoalTime.get(this) + 2000L;
      }

      List<Point> vp = new ArrayList<>();
      List<Point> buildingp = new ArrayList<>();

      for (Building kiln : villager.getTownHall().getBuildingsWithTag("brickkiln")) {
         if (this.validateDest(villager, kiln)) {
            int nb = kiln.getResManager().getNbEmptyBrickLocation();
            boolean validTarget = false;
            if (nb > 0 && minimumBricksNotRequired) {
               validTarget = true;
            }

            if (nb > 4) {
               validTarget = true;
            }

            if (validTarget) {
               Point p = kiln.getResManager().getEmptyBrickLocation();
               if (p != null) {
                  vp.add(p);
                  buildingp.add(kiln.getPos());
               }
            }
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
      return new ItemStack[]{new ItemStack(MillItems.BRICK_MOULD, 1, 0)};
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws MillLog.MillenaireException {
      return this.getDestination(villager) != null;
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) throws MillLog.MillenaireException {
      if (WorldUtilities.getBlock(villager.world, villager.getGoalDestPoint()) == Blocks.AIR) {
         villager.setBlockstate(villager.getGoalDestPoint(), MillBlocks.BS_WET_BRICK);
         villager.swingArm(EnumHand.MAIN_HAND);
      }

      if (villager.getGoalBuildingDest().getResManager().getNbEmptyBrickLocation() > 0) {
         villager.setGoalInformation(this.getDestination(villager));
         return false;
      } else {
         return true;
      }
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

   @Override
   public boolean unreachableDestination(MillVillager villager) {
      return false;
   }
}
