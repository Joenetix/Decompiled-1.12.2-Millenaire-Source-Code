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
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Pick up dried bricks from a kiln.")
public class GoalIndianGatherBrick extends Goal {
   private static ItemStack[] MUD_BRICK = new ItemStack[]{BlockItemUtilities.getItemStackFromBlockState(MillBlocks.BS_MUD_BRICK, 1)};

   public GoalIndianGatherBrick() {
      this.maxSimultaneousInBuilding = 1;
      this.townhallLimit.put(InvItem.createInvItem(MillBlocks.BS_MUD_BRICK), 4096);
      this.tags.add("tag_construction");
      this.icon = InvItem.createInvItem(MillBlocks.BS_MUD_BRICK);
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
            int nb = kiln.getResManager().getNbFullBrickLocation();
            boolean validTarget = false;
            if (nb > 0 && minimumBricksNotRequired) {
               validTarget = true;
            }

            if (nb > 4) {
               validTarget = true;
            }

            if (validTarget) {
               Point p = kiln.getResManager().getFullBrickLocation();
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
   public ItemStack[] getHeldItemsOffHandDestination(MillVillager villager) {
      return MUD_BRICK;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return villager.getBestPickaxeStack();
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
      if (WorldUtilities.getBlockState(villager.world, villager.getGoalDestPoint()) == MillBlocks.BS_MUD_BRICK) {
         villager.addToInv(MillBlocks.BS_MUD_BRICK, 1);
         villager.setBlockAndMetadata(villager.getGoalDestPoint(), Blocks.AIR, 0);
         villager.swingArm(EnumHand.MAIN_HAND);
      }

      if (villager.getGoalBuildingDest().getResManager().getNbFullBrickLocation() > 0) {
         villager.setGoalInformation(this.getDestination(villager));
         return false;
      } else {
         return true;
      }
   }

   @Override
   public int priority(MillVillager villager) {
      int p = 100 - villager.getTownHall().nbGoodAvailable(MillBlocks.BS_MUD_BRICK, false, false, false) * 2;

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
