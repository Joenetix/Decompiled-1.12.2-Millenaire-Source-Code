package org.millenaire.common.goal;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

@DocumentedElement.Documentation("Sheer sheeps present around the villager's house.")
public class GoalShearSheep extends Goal {
   public GoalShearSheep() {
      this.buildingLimit.put(InvItem.createInvItem(Blocks.WOOL, 0), 1024);
      this.townhallLimit.put(InvItem.createInvItem(Blocks.WOOL, 0), 1024);
      this.icon = InvItem.createInvItem(Items.SHEARS);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      Point pos = villager.getPos();
      Entity closestSheep = null;
      double sheepBestDist = Double.MAX_VALUE;

      for (Entity ent : WorldUtilities.getEntitiesWithinAABB(villager.world, EntitySheep.class, villager.getHouse().getPos(), 30, 10)) {
         if (!((EntitySheep)ent).getSheared() && !((EntitySheep)ent).isChild() && (closestSheep == null || pos.distanceTo(ent) < sheepBestDist)) {
            closestSheep = ent;
            sheepBestDist = pos.distanceTo(ent);
         }
      }

      return closestSheep != null ? this.packDest(null, villager.getHouse(), closestSheep) : null;
   }

   @Override
   public AStarConfig getPathingConfig(MillVillager villager) {
      return !villager.canVillagerClearLeaves() ? JPS_CONFIG_WIDE_NO_LEAVES : JPS_CONFIG_WIDE;
   }

   @Override
   public boolean isFightingGoal() {
      return true;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws Exception {
      if (!villager.getHouse().containsTags("sheeps")) {
         return false;
      } else {
         List<Entity> sheep = WorldUtilities.getEntitiesWithinAABB(villager.world, EntitySheep.class, villager.getHouse().getPos(), 30, 10);
         if (sheep == null) {
            return false;
         } else {
            for (Entity ent : sheep) {
               EntitySheep asheep = (EntitySheep)ent;
               if (!asheep.isChild() && !asheep.isDead && !((EntitySheep)ent).getSheared()) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      for (Entity ent : WorldUtilities.getEntitiesWithinAABB(villager.world, EntitySheep.class, villager.getPos(), 4, 4)) {
         if (!ent.isDead) {
            EntitySheep animal = (EntitySheep)ent;
            if (!animal.isChild() && !animal.getSheared()) {
               villager.addToInv(Blocks.WOOL, ((EntitySheep)ent).getFleeceColor().getMetadata(), 3);
               ((EntitySheep)ent).setSheared(true);
               if (MillConfigValues.LogCattleFarmer >= 1 && villager.extraLog) {
                  MillLog.major(this, "Shearing: " + ent);
               }

               villager.swingArm(EnumHand.MAIN_HAND);
            }
         }
      }

      return true;
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 50;
   }

   @Override
   public int range(MillVillager villager) {
      return 5;
   }
}
