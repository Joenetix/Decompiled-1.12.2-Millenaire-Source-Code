package org.millenaire.common.goal;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;

@DocumentedElement.Documentation("Defend the village when attacked in a raid.")
public class GoalDefendVillage extends Goal {
   public GoalDefendVillage() {
      this.icon = InvItem.createInvItem(Items.SHIELD);
   }

   @Override
   public boolean autoInterruptIfNoTarget() {
      return false;
   }

   @Override
   public boolean canBeDoneAtNight() {
      return true;
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      return villager.getPos().distanceToSquared(villager.getTownHall().getResManager().getDefendingPos()) <= 9.0
         ? null
         : this.packDest(villager.getTownHall().getResManager().getDefendingPos(), villager.getTownHall());
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
      return true;
   }

   @Override
   public boolean isStillValidSpecific(MillVillager villager) throws Exception {
      return villager.getTownHall().underAttack;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      return false;
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 0;
   }

   @Override
   public int range(MillVillager villager) {
      return 1;
   }
}
