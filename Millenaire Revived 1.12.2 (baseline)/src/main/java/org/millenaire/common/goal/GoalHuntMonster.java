package org.millenaire.common.goal;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.WorldUtilities;

@DocumentedElement.Documentation("Seek out mobs around the village and attack them.")
public class GoalHuntMonster extends Goal {
   public GoalHuntMonster() {
      this.icon = InvItem.createInvItem(Items.DIAMOND_SWORD);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      List<Entity> mobs = WorldUtilities.getEntitiesWithinAABB(villager.world, EntityMob.class, villager.getTownHall().getPos(), 50, 10);
      if (mobs == null) {
         return null;
      } else {
         int bestDist = Integer.MAX_VALUE;
         Entity target = null;

         for (Entity ent : mobs) {
            if (ent instanceof EntityMob
               && !(ent instanceof EntityCreeper)
               && villager.getPos().distanceToSquared(ent) < bestDist
               && villager.getTownHall().getAltitude((int)ent.posX, (int)ent.posZ) < ent.posY) {
               target = ent;
               bestDist = (int)villager.getPos().distanceToSquared(ent);
            }
         }

         return target == null ? null : this.packDest(null, null, target);
      }
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      return new ItemStack[]{villager.getWeapon()};
   }

   @Override
   public boolean isFightingGoal() {
      return true;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws Exception {
      return this.getDestination(villager) != null;
   }

   @Override
   public boolean isStillValidSpecific(MillVillager villager) throws Exception {
      if (villager.world.getWorldTime() % 10L == 0L) {
         this.setVillagerDest(villager);
      }

      return villager.getGoalDestPoint() != null;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      for (Entity ent : WorldUtilities.getEntitiesWithinAABB(villager.world, EntityMob.class, villager.getPos(), 4, 4)) {
         if (!ent.isDead && ent instanceof EntityMob && villager.canEntityBeSeen(ent)) {
            EntityMob mob = (EntityMob)ent;
            villager.setAttackTarget(mob);
            if (MillConfigValues.LogGeneralAI >= 1) {
               MillLog.major(this, "Attacking entity: " + ent);
            }
         }
      }

      return true;
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 50;
   }
}
