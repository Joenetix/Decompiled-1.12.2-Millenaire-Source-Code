package org.millenaire.common.goal;

import java.util.HashMap;
import java.util.List;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation(
   "Gets goods required by the household (food, inputs for crafting...) from the TH or shops and loads them in the inventory. Paired with delivergoodshousehold, which delivers them."
)
public class GoalGetGoodsForHousehold extends Goal {
   public GoalGetGoodsForHousehold() {
      this.travelBookShow = false;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      return this.getDestination(villager, false);
   }

   public Goal.GoalInformation getDestination(MillVillager villager, boolean test) throws Exception {
      List<Building> buildings = null;
      boolean delayOver;
      if (!test) {
         delayOver = true;
      } else if (!villager.lastGoalTime.containsKey(this)) {
         delayOver = true;
      } else {
         delayOver = villager.world.getWorldTime() > villager.lastGoalTime.get(this) + 2000L;
      }

      for (MillVillager v : villager.getHouse().getKnownVillagers()) {
         HashMap<InvItem, Integer> goods = v.requiresGoods();
         int nb = 0;

         for (InvItem key : goods.keySet()) {
            if (villager.getHouse().countGoods(key.getItem(), key.meta) < goods.get(key) / 2) {
               if (buildings == null) {
                  buildings = villager.getTownHall().getBuildings();
               }

               for (Building building : buildings) {
                  int nbav = building.nbGoodAvailable(key, false, false, false);
                  if (nbav > 0 && building != villager.getHouse()) {
                     nb += nbav;
                     if (delayOver || nb > 16) {
                        return this.packDest(building.getResManager().getSellingPos(), building);
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws Exception {
      return this.getDestination(villager, true) != null;
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public String nextGoal(MillVillager villager) throws Exception {
      return Goal.deliverGoodsHousehold.key;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      Building shop = villager.getGoalBuildingDest();
      if (shop != null && shop != villager.getHouse()) {
         for (MillVillager v : villager.getHouse().getKnownVillagers()) {
            HashMap<InvItem, Integer> goods = v.requiresGoods();

            for (InvItem key : goods.keySet()) {
               if (villager.getHouse().countGoods(key.getItem(), key.meta) < goods.get(key)) {
                  int nb = Math.min(shop.nbGoodAvailable(key, false, false, false), goods.get(key));
                  villager.takeFromBuilding(shop, key.getItem(), key.meta, nb);
               }
            }
         }

         return true;
      } else {
         return true;
      }
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      int nb = 0;
      List<Building> shops = villager.getTownHall().getShops();

      for (MillVillager v : villager.getHouse().getKnownVillagers()) {
         HashMap<InvItem, Integer> goods = v.requiresGoods();

         for (InvItem key : goods.keySet()) {
            if (villager.getHouse().countGoods(key.getItem(), key.meta) < goods.get(key) / 2) {
               for (Building shop : shops) {
                  int nbav = shop.nbGoodAvailable(key, false, false, false);
                  if (nbav > 0 && shop != villager.getHouse()) {
                     nb += nbav;
                  }
               }
            }
         }
      }

      return nb * 20;
   }
}
