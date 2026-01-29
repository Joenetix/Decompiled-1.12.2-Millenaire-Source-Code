package org.millenaire.common.goal;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntityBrewingStand;
import org.millenaire.common.config.DocumentedElement;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.utilities.Point;

@DocumentedElement.Documentation("Brew alchemical potions from nether warts. Currently broken.")
public class GoalBrewPotions extends Goal {
   public GoalBrewPotions() {
      this.icon = InvItem.createInvItem(Items.POTIONITEM);
   }

   @Override
   public Goal.GoalInformation getDestination(MillVillager villager) throws Exception {
      int nbWarts = villager.getHouse().countGoods(Items.NETHER_WART);
      int nbBottles = villager.getHouse().countGoods(Items.GLASS_BOTTLE);
      int nbPotions = villager.getHouse().countGoods(Items.POTIONITEM, -1);

      for (Point p : villager.getHouse().getResManager().brewingStands) {
         TileEntityBrewingStand brewingStand = p.getBrewingStand(villager.world);
         if (brewingStand != null && brewingStand.getField(0) == 0) {
            if (brewingStand.getStackInSlot(3) == ItemStack.EMPTY && nbWarts > 0 && nbPotions < 64) {
               return this.packDest(p, villager.getHouse());
            }

            if (nbBottles > 2
               && (
                  brewingStand.getStackInSlot(0) == ItemStack.EMPTY
                     || brewingStand.getStackInSlot(1) == ItemStack.EMPTY
                     || brewingStand.getStackInSlot(2) == ItemStack.EMPTY
               )
               && nbPotions < 64) {
               return this.packDest(p, villager.getHouse());
            }

            for (int i = 0; i < 3; i++) {
               if (brewingStand.getStackInSlot(i) != null
                  && brewingStand.getStackInSlot(i).getItem() == Items.POTIONITEM
                  && brewingStand.getStackInSlot(i).getItemDamage() == 16) {
                  return this.packDest(p, villager.getHouse());
               }
            }
         }
      }

      return null;
   }

   @Override
   public boolean isPossibleSpecific(MillVillager villager) throws Exception {
      return this.getDestination(villager) != null;
   }

   @Override
   public boolean performAction(MillVillager villager) throws Exception {
      int nbWarts = villager.getHouse().countGoods(Items.NETHER_WART);
      int nbBottles = villager.getHouse().countGoods(Items.GLASS_BOTTLE);
      int nbPotions = villager.getHouse().countGoods(Items.POTIONITEM);
      TileEntityBrewingStand brewingStand = villager.getGoalDestPoint().getBrewingStand(villager.world);
      if (brewingStand == null) {
         return true;
      } else {
         if (brewingStand.getField(0) == 0) {
            if (brewingStand.getStackInSlot(3) == ItemStack.EMPTY && nbWarts > 0 && nbPotions < 64) {
               brewingStand.setInventorySlotContents(3, new ItemStack(Items.NETHER_WART, 1));
               villager.getHouse().takeGoods(Items.NETHER_WART, 1);
            }

            if (nbBottles > 2 && nbPotions < 64) {
               for (int i = 0; i < 3; i++) {
                  if (brewingStand.getStackInSlot(i) == ItemStack.EMPTY) {
                     ItemStack waterPotion = new ItemStack(Items.POTIONITEM, 1, 0);
                     waterPotion.setTagInfo("Potion", new NBTTagString("minecraft:water"));
                     brewingStand.setInventorySlotContents(i, waterPotion);
                     villager.getHouse().takeGoods(Items.GLASS_BOTTLE, 1);
                  }
               }
            }

            for (int ix = 0; ix < 3; ix++) {
               if (brewingStand.getStackInSlot(ix) != ItemStack.EMPTY
                  && brewingStand.getStackInSlot(ix).getItem() == Items.POTIONITEM
                  && brewingStand.getStackInSlot(ix).getItemDamage() == 16) {
                  brewingStand.setInventorySlotContents(ix, ItemStack.EMPTY);
                  villager.getHouse().storeGoods(Items.POTIONITEM, 16, 1);
               }
            }
         }

         return true;
      }
   }

   @Override
   public int priority(MillVillager villager) throws Exception {
      return 100;
   }

   @Override
   public boolean swingArms() {
      return true;
   }
}
