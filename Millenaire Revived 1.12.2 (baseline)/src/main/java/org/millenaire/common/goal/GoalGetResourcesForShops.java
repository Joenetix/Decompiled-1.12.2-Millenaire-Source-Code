package org.millenaire.common.goal;

import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation(
   "Takes items from the villager's house or the town hall that are needed in a shop. Paired with deliverresourcesshop that delivers them."
)
public class GoalGetResourcesForShops extends Goal {
   public GoalGetResourcesForShops() {
      this.travelBookShow = false;
   }

   @Override
   public int actionDuration(MillVillager villager) {
      return 40;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      return this.getDestination(villager, false);
   }

   public Goal.GoalInformation getDestination(MillVillager villager, boolean test) {
      boolean delayOver;
      if (!test) {
         delayOver = true;
      } else if (!villager.lastGoalTime.containsKey(this)) {
         delayOver = true;
      } else {
         delayOver = villager.world.getWorldTime() > villager.lastGoalTime.get(this) + 2000L;
      }

      for (Building shop : villager.getTownHall().getShops()) {
         int nb = 0;
         if (villager.getCulture().shopNeeds.containsKey(shop.location.shop)) {
            for (InvItem item : villager.getCulture().shopNeeds.get(shop.location.shop)) {
               if (shop != villager.getHouse()) {
                  int nbcount = villager.getHouse().nbGoodAvailable(item, false, false, true);
                  if (nbcount > 0) {
                     nb += nbcount;
                     if (delayOver || nb > 16) {
                        return this.packDest(villager.getHouse().getResManager().getSellingPos(), villager.getHouse());
                     }
                  }
               }

               if (villager.getTownHall() != shop && villager.getTownHall().nbGoodAvailable(item, false, false, true) > 0) {
                  nb += villager.getTownHall().nbGoodAvailable(item, false, false, true);
                  if (delayOver || nb > 16) {
                     return this.packDest(villager.getTownHall().getResManager().getSellingPos(), villager.getTownHall());
                  }
               }
            }
         }
      }

      return null;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      return this.getDestination(villager, true) != null;
   }

   @Override
   public boolean performAction(MillVillager villager) {
      Building dest = villager.getGoalBuildingDest();
      if (dest == null) {
         MillLog.error(
            villager,
            "Invalid destination for GoalGetResourcesForShops goal: "
               + villager.getGoalBuildingDestPoint()
               + " (house: "
               + villager.getHouse().getPos()
               + ", TH: "
               + villager.getTownHall().getPos()
               + "), pathDestPoint: "
               + villager.getGoalDestPoint()
         );
         return true;
      } else {
         for (Building shop : villager.getTownHall().getShops()) {
            if (!shop.getPos().equals(villager.getGoalDestPoint()) && villager.getCulture().shopNeeds.containsKey(shop.location.shop)) {
               for (InvItem item : villager.getCulture().shopNeeds.get(shop.location.shop)) {
                  villager.takeFromBuilding(dest, item.getItem(), item.meta, dest.nbGoodAvailable(item, false, false, true));
               }
            }
         }

         return true;
      }
   }

   @Override
   public int priority(MillVillager villager) {
      int priority = 0;

      for (Building shop : villager.getTownHall().getShops()) {
         if (villager.getCulture().shopNeeds.containsKey(shop.location.shop)) {
            for (InvItem item : villager.getCulture().shopNeeds.get(shop.location.shop)) {
               priority += villager.getHouse().countGoods(item.getItem(), item.meta) * 5;
               if (villager.getTownHall() != shop) {
                  priority += villager.getTownHall().countGoods(item.getItem(), item.meta) * 5;
               }
            }
         }
      }

      return priority;
   }
}
