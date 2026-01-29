package org.millenaire.common.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.entity.MillVillager;
import org.millenaire.common.item.MillItems;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.village.Building;

public class PujaSacrifice implements IInventory {
   public static final int TOOL = 1;
   public static final int ARMOUR = 2;
   public static final int HELMET = 3;
   public static final int BOOTS = 4;
   public static final int SWORD_AXE = 5;
   public static final int SWORD = 6;
   public static final int BOW = 7;
   public static final int UNBREAKABLE = 8;
   public static PujaSacrifice.PrayerTarget[] PUJA_TARGETS = new PujaSacrifice.PrayerTarget[]{
      new PujaSacrifice.PrayerTarget(Enchantments.EFFICIENCY, "pujas.god0", 0, 188, 46, 188, 1),
      new PujaSacrifice.PrayerTarget(Enchantments.UNBREAKING, "pujas.god1", 0, 205, 46, 205, 1),
      new PujaSacrifice.PrayerTarget(Enchantments.FORTUNE, "pujas.god2", 0, 222, 46, 222, 1),
      new PujaSacrifice.PrayerTarget(Enchantments.SILK_TOUCH, "pujas.god3", 0, 239, 46, 239, 1)
   };
   public static PujaSacrifice.PrayerTarget[] MAYAN_TARGETS = new PujaSacrifice.PrayerTarget[]{
      new PujaSacrifice.PrayerTarget(Enchantments.PROTECTION, "mayan.god0", 0, 188, 120, 188, 2),
      new PujaSacrifice.PrayerTarget(Enchantments.FIRE_PROTECTION, "mayan.god1", 20, 188, 140, 188, 2),
      new PujaSacrifice.PrayerTarget(Enchantments.BLAST_PROTECTION, "mayan.god2", 40, 188, 160, 188, 2),
      new PujaSacrifice.PrayerTarget(Enchantments.PROJECTILE_PROTECTION, "mayan.god3", 60, 188, 180, 188, 2),
      new PujaSacrifice.PrayerTarget(Enchantments.THORNS, "mayan.god4", 80, 188, 200, 188, 2),
      new PujaSacrifice.PrayerTarget(Enchantments.RESPIRATION, "mayan.god5", 100, 188, 120, 188, 3),
      new PujaSacrifice.PrayerTarget(Enchantments.AQUA_AFFINITY, "mayan.god6", 0, 208, 120, 208, 3),
      new PujaSacrifice.PrayerTarget(Enchantments.FEATHER_FALLING, "mayan.god7", 20, 208, 140, 208, 4),
      new PujaSacrifice.PrayerTarget(Enchantments.SHARPNESS, "mayan.god8", 40, 208, 160, 208, 5),
      new PujaSacrifice.PrayerTarget(Enchantments.SMITE, "mayan.god9", 0, 188, 120, 188, 5),
      new PujaSacrifice.PrayerTarget(Enchantments.BANE_OF_ARTHROPODS, "mayan.god10", 80, 188, 200, 188, 5),
      new PujaSacrifice.PrayerTarget(Enchantments.KNOCKBACK, "mayan.god11", 60, 208, 180, 208, 6),
      new PujaSacrifice.PrayerTarget(Enchantments.FIRE_ASPECT, "mayan.god12", 20, 188, 140, 188, 6),
      new PujaSacrifice.PrayerTarget(Enchantments.LOOTING, "mayan.god13", 80, 208, 200, 208, 6),
      new PujaSacrifice.PrayerTarget(Enchantments.POWER, "mayan.god14", 40, 208, 160, 208, 7),
      new PujaSacrifice.PrayerTarget(Enchantments.PUNCH, "mayan.god15", 60, 208, 180, 208, 7),
      new PujaSacrifice.PrayerTarget(Enchantments.FLAME, "mayan.god16", 20, 188, 140, 188, 7),
      new PujaSacrifice.PrayerTarget(Enchantments.INFINITY, "mayan.god17", 80, 208, 200, 208, 7),
      new PujaSacrifice.PrayerTarget(Enchantments.UNBREAKING, "mayan.god18", 100, 208, 220, 208, 8)
   };
   public static int PUJA_DURATION = 30;
   public static final short PUJA = 0;
   public static final short MAYAN = 1;
   private ItemStack[] items;
   public PujaSacrifice.PrayerTarget currentTarget = null;
   public int offeringProgress = 0;
   public int offeringNeeded = 1;
   public short pujaProgress = 0;
   public Building temple = null;
   public MillVillager priest = null;
   public short type = 0;

   public static boolean validForItem(int type, Item item) {
      if (type == 1) {
         return item instanceof ItemSpade || item instanceof ItemAxe || item instanceof ItemPickaxe;
      } else if (type == 2) {
         return item instanceof ItemArmor;
      } else if (type == 3) {
         return item instanceof ItemArmor && ((ItemArmor)item).armorType == EntityEquipmentSlot.HEAD;
      } else if (type == 4) {
         return item instanceof ItemArmor && ((ItemArmor)item).armorType == EntityEquipmentSlot.FEET;
      } else if (type == 5) {
         return item instanceof ItemSword || item instanceof ItemAxe;
      } else if (type == 6) {
         return item instanceof ItemSword;
      } else if (type == 7) {
         return item instanceof ItemBow;
      } else {
         return type != 8 ? false : item instanceof ItemSword || item instanceof ItemArmor || item instanceof ItemBow;
      }
   }

   public PujaSacrifice(Building temple, NBTTagCompound tag) {
      this.temple = temple;
      if (temple.containsTags("sacrifices")) {
         this.type = 1;
      }

      this.readFromNBT(tag);
   }

   public PujaSacrifice(Building temple, short type) {
      this.temple = temple;
      this.items = new ItemStack[this.getSizeInventory()];

      for (int i = 0; i < this.items.length; i++) {
         this.items[i] = ItemStack.EMPTY;
      }

      this.type = type;
   }

   public void calculateOfferingsNeeded() {
      this.offeringNeeded = 0;
      if (this.items[4] != ItemStack.EMPTY && this.currentTarget != null) {
         ItemStack tool = this.items[4];
         if (EnchantmentHelper.getEnchantmentLevel(this.currentTarget.enchantment, tool) < this.currentTarget.enchantment.getMaxLevel()) {
            if (this.currentTarget.enchantment.canApply(tool)) {
               int nbother = 0;
               if (tool.isItemEnchanted()) {
                  NBTTagList nbttaglist = tool.getEnchantmentTagList();
                  nbother = nbttaglist.tagCount();
                  Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(tool);

                  for (Enchantment enchId : existingEnchantments.keySet()) {
                     if (enchId != this.currentTarget.enchantment && !enchId.isCompatibleWith(this.currentTarget.enchantment)) {
                        return;
                     }
                  }
               }

               int currentLevel = EnchantmentHelper.getEnchantmentLevel(this.currentTarget.enchantment, tool);
               if (currentLevel > 0) {
                  nbother--;
               }

               int cost = 50 + this.currentTarget.enchantment.getMinEnchantability(currentLevel + 1) * 10;
               cost *= nbother / 2 + 1;
               if (MillConfigValues.LogPujas >= 2) {
                  MillLog.minor(this, "Offering needed: " + cost);
               }

               this.offeringNeeded = cost;
            }
         }
      }
   }

   public boolean canPray() {
      return this.offeringNeeded <= this.offeringProgress ? false : this.items[0] != ItemStack.EMPTY;
   }

   public void changeEnchantment(int i) {
      if (this.currentTarget != this.getTargets().get(i)) {
         this.currentTarget = this.getTargets().get(i);
         this.offeringProgress = 0;
         this.calculateOfferingsNeeded();
      }
   }

   public void clear() {
   }

   public void closeInventory(EntityPlayer player) {
   }

   private void completeOffering() {
      int currentlevel = EnchantmentHelper.getEnchantmentLevel(this.currentTarget.enchantment, this.items[4]);
      if (currentlevel == 0) {
         this.items[4].addEnchantment(this.currentTarget.enchantment, 1);
      } else {
         NBTTagList enchList = this.items[4].getEnchantmentTagList();

         for (int i = 0; i < enchList.tagCount(); i++) {
            Enchantment e = Enchantment.getEnchantmentByID(enchList.getCompoundTagAt(i).getShort("id"));
            if (e == this.currentTarget.enchantment) {
               enchList.getCompoundTagAt(i).setShort("lvl", (short)(currentlevel + 1));
            }
         }
      }

      this.offeringProgress = 0;
      this.calculateOfferingsNeeded();
      this.temple.getTownHall().requestSave("Puja/sacrifice offering complete");
   }

   public ItemStack decrStackSize(int slot, int nb) {
      if (this.items[slot] != ItemStack.EMPTY) {
         if (this.items[slot].getCount() <= nb) {
            ItemStack itemstack = this.items[slot];
            this.items[slot] = ItemStack.EMPTY;
            return itemstack;
         } else {
            ItemStack itemstack1 = this.items[slot].splitStack(nb);
            if (this.items[slot].getCount() == 0) {
               this.items[slot] = ItemStack.EMPTY;
            }

            return itemstack1;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   private void endPuja() {
      ItemStack offer = this.items[0];
      if (offer != ItemStack.EMPTY) {
         int offerValue = this.getOfferingValue(offer);
         this.offeringProgress += offerValue;
         offer.setCount(offer.getCount() - 1);
         if (offer.getCount() == 0) {
            this.items[0] = ItemStack.EMPTY;
         }

         if (this.offeringProgress >= this.offeringNeeded) {
            this.completeOffering();
         }
      }
   }

   public ITextComponent getDisplayName() {
      return null;
   }

   public int getField(int id) {
      return 0;
   }

   public int getFieldCount() {
      return 0;
   }

   public int getInventoryStackLimit() {
      return 64;
   }

   public String getName() {
      return LanguageUtilities.string("pujas.invanme");
   }

   public int getOfferingProgressScaled(int scale) {
      return this.offeringNeeded == 0 ? 0 : this.offeringProgress * scale / this.offeringNeeded;
   }

   public int getOfferingValue(ItemStack is) {
      if (this.type == 0) {
         return this.getOfferingValuePuja(is);
      } else {
         return this.type == 1 ? this.getOfferingValueMayan(is) : 0;
      }
   }

   public int getOfferingValueMayan(ItemStack is) {
      if (is.getItem() == Items.SKULL) {
         return 4096;
      } else if (is.getItem() == Items.GHAST_TEAR) {
         return 384;
      } else if (is.getItem() == Items.BLAZE_ROD) {
         return 64;
      } else if (is.getItem() == MillItems.CACAUHAA) {
         return 64;
      } else if (is.getItem() == Items.CHICKEN) {
         return 1;
      } else if (is.getItem() == Items.BEEF) {
         return 1;
      } else if (is.getItem() == Items.PORKCHOP) {
         return 1;
      } else if (is.getItem() == Items.FISH) {
         return 1;
      } else if (is.getItem() == Items.LEATHER) {
         return 1;
      } else if (is.getItem() == Items.DYE && is.getItemDamage() == 0) {
         return 1;
      } else if (is.getItem() == Items.SLIME_BALL) {
         return 1;
      } else if (is.getItem() == Items.ROTTEN_FLESH) {
         return 2;
      } else if (is.getItem() == Items.BONE) {
         return 2;
      } else if (is.getItem() == Items.MAGMA_CREAM) {
         return 4;
      } else if (is.getItem() == Items.GUNPOWDER) {
         return 4;
      } else if (is.getItem() == Items.SPIDER_EYE) {
         return 4;
      } else {
         return is.getItem() == Items.ENDER_PEARL ? 6 : 0;
      }
   }

   public int getOfferingValuePuja(ItemStack is) {
      if (is.getItem() == Items.DIAMOND) {
         return 384;
      } else if (is.getItem() == Items.MILK_BUCKET) {
         return 128;
      } else if (is.getItem() == Items.GOLDEN_APPLE) {
         return 96;
      } else if (is.getItem() == Items.GOLD_INGOT) {
         return 64;
      } else if (is.getItem() == MillItems.RICE) {
         return 8;
      } else if (is.getItem() == MillItems.RASGULLA) {
         return 64;
      } else if (is.getItem() == Item.getItemFromBlock(Blocks.RED_FLOWER) || is.getItem() == Item.getItemFromBlock(Blocks.YELLOW_FLOWER)) {
         return 16;
      } else if (is.getItem() == Item.getItemFromBlock(Blocks.TALLGRASS) || is.getItem() == Items.APPLE) {
         return 8;
      } else if (is.getItem() == Item.getItemFromBlock(Blocks.WOOL) && is.getItemDamage() == 0) {
         return 8;
      } else {
         return is.getItem() == Items.MELON ? 4 : 0;
      }
   }

   public int getPujaProgressScaled(int scale) {
      return this.pujaProgress * scale / PUJA_DURATION;
   }

   public int getSizeInventory() {
      return 5;
   }

   public ItemStack getStackInSlot(int par1) {
      return this.items[par1];
   }

   public List<PujaSacrifice.PrayerTarget> getTargets() {
      if (this.items[4] == ItemStack.EMPTY) {
         return new ArrayList<>();
      } else if (this.type == 0) {
         List<PujaSacrifice.PrayerTarget> targets = new ArrayList<>();

         for (PujaSacrifice.PrayerTarget t : PUJA_TARGETS) {
            if (t.validForItem(this.items[4].getItem())) {
               targets.add(t);
            }
         }

         return targets;
      } else if (this.type == 1) {
         List<PujaSacrifice.PrayerTarget> targets = new ArrayList<>();

         for (PujaSacrifice.PrayerTarget tx : MAYAN_TARGETS) {
            if (tx.validForItem(this.items[4].getItem())) {
               targets.add(tx);
            }
         }

         return targets;
      } else {
         return new ArrayList<>();
      }
   }

   public boolean hasCustomName() {
      return false;
   }

   public boolean isActive() {
      return false;
   }

   public boolean isEmpty() {
      return false;
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return true;
   }

   public boolean isUsableByPlayer(EntityPlayer player) {
      return false;
   }

   public void markDirty() {
   }

   public void openInventory(EntityPlayer player) {
   }

   public boolean performPuja(MillVillager priest) {
      this.priest = priest;
      if (this.pujaProgress == 0) {
         boolean success = this.startPuja();
         if (success) {
            this.pujaProgress = 1;
         }

         return success;
      } else if (this.pujaProgress >= PUJA_DURATION) {
         this.endPuja();
         this.pujaProgress = 0;
         return this.canPray();
      } else {
         this.pujaProgress++;
         return this.canPray();
      }
   }

   public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
      NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", 10);
      this.items = new ItemStack[this.getSizeInventory()];

      for (int i = 0; i < nbttaglist.tagCount(); i++) {
         NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
         byte byte0 = nbttagcompound.getByte("Slot");
         if (byte0 >= 0 && byte0 < this.items.length) {
            this.items[byte0] = new ItemStack(nbttagcompound);
         }
      }

      Enchantment ench = Enchantment.getEnchantmentByID(par1NBTTagCompound.getShort("enchantmentTarget"));
      if (ench != null) {
         for (PujaSacrifice.PrayerTarget t : this.getTargets()) {
            if (t.enchantment == ench) {
               this.currentTarget = t;
            }
         }
      }

      if (MillConfigValues.LogPujas >= 2) {
         MillLog.minor(this, "Reading enchantmentTarget: " + ench + ", " + this.currentTarget);
      }

      this.offeringProgress = par1NBTTagCompound.getShort("offeringProgress");
      this.pujaProgress = par1NBTTagCompound.getShort("pujaProgress");
      this.calculateOfferingsNeeded();
   }

   public ItemStack removeStackFromSlot(int par1) {
      if (this.items[par1] != ItemStack.EMPTY) {
         ItemStack itemstack = this.items[par1];
         this.items[par1] = ItemStack.EMPTY;
         this.markDirty();
         return itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }

   public void setField(int id, int value) {
   }

   public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
      this.items[par1] = par2ItemStack;
      if (par2ItemStack != ItemStack.EMPTY && par2ItemStack.getCount() > this.getInventoryStackLimit()) {
         par2ItemStack.setCount(this.getInventoryStackLimit());
      }

      this.markDirty();
   }

   private boolean startPuja() {
      int money = MillCommonUtilities.countMoney(this);
      if (money == 0) {
         return false;
      } else if (this.offeringNeeded != 0 && this.offeringProgress < this.offeringNeeded) {
         if (this.items[0] == ItemStack.EMPTY) {
            return false;
         } else {
            money -= 8;
            int denier = money % 64;
            int denier_argent = (money - denier) / 64 % 64;
            int denier_or = (money - denier - denier_argent * 64) / 4096;
            if (denier == 0) {
               this.items[1] = ItemStack.EMPTY;
            } else {
               this.items[1] = new ItemStack(MillItems.DENIER, denier);
            }

            if (denier_argent == 0) {
               this.items[2] = ItemStack.EMPTY;
            } else {
               this.items[2] = new ItemStack(MillItems.DENIER_ARGENT, denier_argent);
            }

            if (denier_or == 0) {
               this.items[3] = ItemStack.EMPTY;
            } else {
               this.items[3] = new ItemStack(MillItems.DENIER_OR, denier_or);
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
      if (this.currentTarget != null) {
         par1NBTTagCompound.setShort("enchantmentTarget", (short)Enchantment.getEnchantmentID(this.currentTarget.enchantment));
         if (MillConfigValues.LogPujas >= 2) {
            MillLog.minor(this, "Writing enchantmentTarget: " + this.currentTarget.enchantment + ", " + this.currentTarget);
         }
      }

      par1NBTTagCompound.setShort("offeringProgress", (short)this.offeringProgress);
      par1NBTTagCompound.setShort("pujaProgress", this.pujaProgress);
      NBTTagList nbttaglist = new NBTTagList();

      for (int i = 0; i < this.items.length; i++) {
         if (this.items[i] != null) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte)i);
            this.items[i].writeToNBT(nbttagcompound);
            nbttaglist.appendTag(nbttagcompound);
         }
      }

      par1NBTTagCompound.setTag("Items", nbttaglist);
   }

   public static class PrayerTarget {
      public final Enchantment enchantment;
      public final String mouseOver;
      public final int startX;
      public final int startY;
      public final int startXact;
      public final int startYact;
      public final int toolType;

      public PrayerTarget(Enchantment enchantment, String mouseOver, int startX, int startY, int startXact, int startYact, int toolType) {
         this.enchantment = enchantment;
         this.mouseOver = mouseOver;
         this.startX = startX;
         this.startY = startY;
         this.startXact = startXact;
         this.startYact = startYact;
         this.toolType = toolType;
      }

      public boolean validForItem(Item item) {
         return PujaSacrifice.validForItem(this.toolType, item);
      }
   }
}
