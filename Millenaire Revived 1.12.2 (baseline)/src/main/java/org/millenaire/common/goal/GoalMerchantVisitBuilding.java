package org.millenaire.common.goal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.item.TradeGood;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

@DocumentedElement.Documentation("For local merchants, pick up goods from village shops for exports and drop off goods brought from other villages.")
public class GoalMerchantVisitBuilding extends Goal {
   public GoalMerchantVisitBuilding() {
      this.icon = InvItem.createInvItem(Blocks.CHEST);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      for (TradeGood good : villager.getTownHall().culture.goodsList) {
         if (good.item.meta >= 0
            && villager.countInv(good.item.getItem(), good.item.meta) > 0
            && villager.getTownHall().nbGoodNeeded(good.item.getItem(), good.item.meta) > 0) {
            if (MillConfigValues.LogMerchant >= 3) {
               MillLog.debug(
                  villager,
                  "TH needs "
                     + villager.getTownHall().nbGoodNeeded(good.item.getItem(), good.item.meta)
                     + " good "
                     + good.item.getName()
                     + ", merchant has "
                     + villager.countInv(good.item.getItem(), good.item.meta)
               );
            }

            return this.packDest(villager.getTownHall().getResManager().getSellingPos(), villager.getTownHall());
         }
      }

      HashMap<TradeGood, Integer> neededGoods = villager.getTownHall().getImportsNeededbyOtherVillages();

      for (Building shop : villager.getTownHall().getShops()) {
         for (TradeGood goodx : villager.getTownHall().culture.goodsList) {
            if (goodx.item.meta >= 0
               && !shop.isInn
               && shop.nbGoodAvailable(goodx.item.getItem(), goodx.item.meta, false, true, false) > 0
               && neededGoods.containsKey(goodx)
               && neededGoods.get(goodx)
                  > villager.getHouse().countGoods(goodx.item.getItem(), goodx.item.meta) + villager.countInv(goodx.item.getItem(), goodx.item.meta)) {
               if (MillConfigValues.LogMerchant >= 3) {
                  MillLog.debug(
                     villager, "Shop " + shop + " has " + shop.nbGoodAvailable(goodx.item.getItem(), goodx.item.meta, false, true, false) + " good to pick up."
                  );
               }

               return this.packDest(shop.getResManager().getSellingPos(), shop);
            }
         }
      }

      return null;
   }

   @Override
   public ItemStack[] getHeldItemsTravelling(MillVillager villager) {
      List<ItemStack> items = new ArrayList<>();

      for (InvItem item : villager.getInventoryKeys()) {
         if (villager.countInv(item) > 0) {
            items.add(new ItemStack(item.getItem(), 1, item.meta));
         }
      }

      return items.toArray(new ItemStack[items.size()]);
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws Exception {
      return this.getDestination(villager) != null;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      Building shop = villager.getGoalBuildingDest();
      HashMap<TradeGood, Integer> neededGoods = villager.getTownHall().getImportsNeededbyOtherVillages();
      if (shop != null && !shop.isInn) {
         if (shop.isTownhall) {
            for (TradeGood good : villager.getTownHall().culture.goodsList) {
               if (good.item.meta >= 0) {
                  int nbNeeded = shop.nbGoodNeeded(good.item.getItem(), good.item.meta);
                  if (nbNeeded > 0) {
                     int nb = villager.putInBuilding(shop, good.item.getItem(), good.item.meta, nbNeeded);
                     if (nb > 0 && MillConfigValues.LogMerchant >= 2) {
                        MillLog.minor(shop, villager + " delivered " + nb + " " + good.getName() + ".");
                     }
                  }
               }
            }
         }

         for (TradeGood goodx : villager.getTownHall().culture.goodsList) {
            if (goodx.item.meta >= 0
               && neededGoods.containsKey(goodx)
               && shop.nbGoodAvailable(goodx.item.getItem(), goodx.item.meta, false, true, false) > 0
               && villager.getHouse().countGoods(goodx.item.getItem(), goodx.item.meta) + villager.countInv(goodx.item.getItem(), goodx.item.meta)
                  < neededGoods.get(goodx)) {
               int nb = Math.min(
                  shop.nbGoodAvailable(goodx.item.getItem(), goodx.item.meta, false, true, false),
                  neededGoods.get(goodx)
                     - villager.getHouse().countGoods(goodx.item.getItem(), goodx.item.meta)
                     - villager.countInv(goodx.item.getItem(), goodx.item.meta)
               );
               nb = villager.takeFromBuilding(shop, goodx.item.getItem(), goodx.item.meta, nb);
               if (MillConfigValues.LogMerchant >= 2) {
                  MillLog.minor(shop, villager + " took " + nb + " " + goodx.getName() + " for trading.");
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
      return 100;
   }
}
