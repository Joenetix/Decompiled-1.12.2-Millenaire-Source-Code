package org.millenaire.common.goal;

import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("Go and get an item from a shop to keep in the villager's inventory. Typically a tool, or a new cloth in some cultures.")
public class GoalGetTool extends Goal {
   public GoalGetTool() {
      this.maxSimultaneousInBuilding = 2;
      this.travelBookShow = false;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      for (Building shop : villager.getTownHall().getShops()) {
         boolean validShop = this.testShopValidity(villager, shop);
         if (validShop) {
            return this.packDest(shop.getResManager().getSellingPos(), shop);
         }
      }

      return null;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws Exception {
      for (Building shop : villager.getTownHall().getShops()) {
         boolean validShop = this.testShopValidity(villager, shop);
         if (validShop) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      Building shop = villager.getGoalBuildingDest();
      if (shop == null) {
         return true;
      } else {
         for (InvItem key : villager.getItemsNeeded()) {
            if (villager.countInv(key.getItem(), key.meta) == 0 && shop.countGoods(key.getItem(), key.meta) > 0 && this.validateDest(villager, shop)) {
               villager.takeFromBuilding(shop, key.getItem(), key.meta, 1);
            }
         }

         for (String toolCategory : villager.getToolsCategoriesNeeded()) {
            InvItem bestItem = villager.getConfig().getBestItemByCategoryName(toolCategory, villager);

            for (InvItem keyx : villager.getConfig().categories.get(toolCategory)) {
               if (keyx == bestItem) {
                  break;
               }

               if (shop.countGoods(keyx.getItem(), keyx.meta) > 0 && this.validateDest(villager, shop)) {
                  villager.takeFromBuilding(shop, keyx.getItem(), keyx.meta, 1);
                  break;
               }
            }
         }

         return true;
      }
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 500;
   }

   private boolean testShopValidity(MillVillager villager, Building shop) throws MillLog.MillenaireException {
      for (InvItem key : villager.getItemsNeeded()) {
         if (villager.countInv(key.getItem(), key.meta) == 0 && shop.countGoods(key.getItem(), key.meta) > 0 && this.validateDest(villager, shop)) {
            return true;
         }
      }

      for (String toolCategory : villager.getToolsCategoriesNeeded()) {
         InvItem bestItem = villager.getConfig().getBestItemByCategoryName(toolCategory, villager);

         for (InvItem keyx : villager.getConfig().categories.get(toolCategory)) {
            if (keyx == bestItem) {
               break;
            }

            if (shop.countGoods(keyx.getItem(), keyx.meta) > 0 && this.validateDest(villager, shop)) {
               return true;
            }
         }
      }

      return false;
   }
}
