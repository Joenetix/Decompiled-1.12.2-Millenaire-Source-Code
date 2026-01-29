package org.millenaire.common.goal;

import java.util.List;
import net.minecraft.entity.item.EntityItem;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.pathing.atomicstryker.AStarConfig;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

@DocumentedElement.Documentation("Gather items around the villager, if they are of a type declared for that villager. For example, saplings for lumbermen.")
public class GoalGatherGoods extends Goal {
   public GoalGatherGoods() {
      this.travelBookShow = false;
   }

   @Override
   public int actionDuration(MillVillager villager) throws Exception {
      return 40;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) {
      EntityItem item = villager.getClosestItemVertical(villager.getGoodsToCollect(), villager.getGatheringRange(), 10);
      return item == null ? null : this.packDest(new Point(item));
   }

   @Override
   public AStarConfig getPathingConfig(MillVillager villager) {
      return !villager.canVillagerClearLeaves() ? JPS_CONFIG_WIDE_NO_LEAVES : JPS_CONFIG_WIDE;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) {
      if (villager.getGoodsToCollect().size() == 0) {
         return false;
      } else {
         EntityItem item = villager.getClosestItemVertical(villager.getGoodsToCollect(), villager.getGatheringRange(), 10);
         return item != null;
      }
   }

   @Override
   public boolean lookAtGoal() {
      return true;
   }

   @Override
   public boolean performAction(MillVillager villager) {
      return true;
   }

   @Override
   public int priority(MillVillager villager) {
      return 500;
   }

   @Override
   public int range(MillVillager villager) {
      return 5;
   }

   @Override
   public boolean stuckAction(MillVillager villager) {
      List<InvItem> goods = villager.getGoodsToCollect();
      if (goods != null) {
         EntityItem item = WorldUtilities.getClosestItemVertical(villager.world, villager.getGoalDestPoint(), goods, 3, 20);
         if (item != null) {
            item.setDead();
            villager.addToInv(item.getItem().getItem(), item.getItem().getItemDamage(), 1);
            return true;
         }
      }

      return false;
   }
}
