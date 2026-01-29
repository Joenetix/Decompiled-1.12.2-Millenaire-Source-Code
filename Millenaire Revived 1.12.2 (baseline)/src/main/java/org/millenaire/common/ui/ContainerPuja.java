package org.millenaire.common.ui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class ContainerPuja extends Container {
   PujaSacrifice shrine;
   ContainerPuja.ToolSlot slotTool;

   public ContainerPuja(EntityPlayer player, Building temple) {
      try {
         this.shrine = temple.pujas;
         this.slotTool = new ContainerPuja.ToolSlot(temple.pujas, 4, 86, 37);
         this.addSlotToContainer(new ContainerPuja.OfferingSlot(temple.pujas, 0, 26, 19));
         this.addSlotToContainer(new ContainerPuja.MoneySlot(temple.pujas, 1, 8, 55));
         this.addSlotToContainer(new ContainerPuja.MoneySlot(temple.pujas, 2, 26, 55));
         this.addSlotToContainer(new ContainerPuja.MoneySlot(temple.pujas, 3, 44, 55));
         this.addSlotToContainer(this.slotTool);

         for (int i = 0; i < 3; i++) {
            for (int k = 0; k < 9; k++) {
               this.addSlotToContainer(new Slot(player.inventory, k + i * 9 + 9, 8 + k * 18, 106 + i * 18));
            }
         }

         for (int j = 0; j < 9; j++) {
            this.addSlotToContainer(new Slot(player.inventory, j, 8 + j * 18, 164));
         }
      } catch (Exception var5) {
         MillLog.printException("Exception in ContainerPuja(): ", var5);
      }
   }

   public boolean canInteractWith(EntityPlayer entityplayer) {
      return true;
   }

   public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int stackID) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = (Slot)this.inventorySlots.get(stackID);
      if (slot != null && slot.getHasStack()) {
         ItemStack itemstack1 = slot.getStack();
         itemstack = itemstack1.copy();
         if (stackID == 4) {
            if (!this.mergeItemStack(itemstack1, 5, 41, true)) {
               return ItemStack.EMPTY;
            }

            slot.onSlotChange(itemstack1, itemstack);
         } else if (stackID > 4) {
            if (itemstack1.getItem() != MillItems.DENIER
               && itemstack1.getItem() != MillItems.DENIER_ARGENT
               && itemstack1.getItem() != MillItems.DENIER_OR) {
               if (this.shrine.getOfferingValue(itemstack1) > 0) {
                  if (!this.mergeItemStack(itemstack1, 0, 1, false)) {
                     return ItemStack.EMPTY;
                  }
               } else {
                  if (!this.slotTool.isItemValid(itemstack1)) {
                     return ItemStack.EMPTY;
                  }

                  if (!this.mergeItemStack(itemstack1, 4, 5, false)) {
                     return ItemStack.EMPTY;
                  }
               }
            } else if (!this.mergeItemStack(itemstack1, 1, 4, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.mergeItemStack(itemstack1, 5, 41, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.getCount() == 0) {
            slot.putStack(ItemStack.EMPTY);
         } else {
            slot.onSlotChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(par1EntityPlayer, itemstack1);
      }

      return itemstack;
   }

   public static class MoneySlot extends Slot {
      PujaSacrifice shrine;

      public MoneySlot(PujaSacrifice shrine, int par2, int par3, int par4) {
         super(shrine, par2, par3, par4);
         this.shrine = shrine;
      }

      public boolean isItemValid(ItemStack is) {
         return is.getItem() == MillItems.DENIER || is.getItem() == MillItems.DENIER_OR || is.getItem() == MillItems.DENIER_ARGENT;
      }

      public void onSlotChanged() {
         if (!this.shrine.temple.world.isRemote) {
            this.shrine.temple.getTownHall().requestSave("Puja money slot changed");
         }

         super.onSlotChanged();
      }
   }

   public static class OfferingSlot extends Slot {
      PujaSacrifice shrine;

      public OfferingSlot(PujaSacrifice shrine, int par2, int par3, int par4) {
         super(shrine, par2, par3, par4);
         this.shrine = shrine;
      }

      public boolean isItemValid(ItemStack par1ItemStack) {
         return this.shrine.getOfferingValue(par1ItemStack) > 0;
      }

      public void onSlotChanged() {
         if (!this.shrine.temple.world.isRemote) {
            this.shrine.temple.getTownHall().requestSave("Puja offering slot changed");
         }

         super.onSlotChanged();
      }
   }

   public static class ToolSlot extends Slot {
      PujaSacrifice shrine;

      public ToolSlot(PujaSacrifice shrine, int par2, int par3, int par4) {
         super(shrine, par2, par3, par4);
         this.shrine = shrine;
      }

      public boolean isItemValid(ItemStack is) {
         Item item = is.getItem();
         return this.shrine.type == 1
            ? item instanceof ItemSword || item instanceof ItemArmor || item instanceof ItemBow || item instanceof ItemAxe
            : item instanceof ItemSpade || item instanceof ItemAxe || item instanceof ItemPickaxe;
      }

      public void onSlotChanged() {
         this.shrine.calculateOfferingsNeeded();
         if (!this.shrine.temple.world.isRemote) {
            this.shrine.temple.getTownHall().requestSave("Puja tool slot changed");
         }

         super.onSlotChanged();
      }
   }
}
