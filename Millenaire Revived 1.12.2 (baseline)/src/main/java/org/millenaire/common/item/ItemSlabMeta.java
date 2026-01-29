package org.millenaire.common.item;

import net.minecraft.block.BlockSlab;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;

public class ItemSlabMeta extends ItemSlab {
   public ItemSlabMeta(BlockSlab halfBlock, BlockSlab fullBlock) {
      super(halfBlock, halfBlock, fullBlock);
      if (!(this.block instanceof IMetaBlockName)) {
         throw new IllegalArgumentException(String.format("The given Block %s is not an instance of IMetaBlockName!", this.block.getTranslationKey()));
      } else {
         this.setMaxDamage(0);
         this.setHasSubtypes(true);
      }
   }

   public int getMetadata(int damage) {
      return damage;
   }

   public String getTranslationKey(ItemStack stack) {
      return ((IMetaBlockName)this.block).getSpecialName(stack);
   }
}
