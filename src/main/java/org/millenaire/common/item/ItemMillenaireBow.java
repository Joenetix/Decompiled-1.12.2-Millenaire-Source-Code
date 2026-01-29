package org.millenaire.common.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBow;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.block.MillBlocks;

public class ItemMillenaireBow extends ItemBow {
   public float speedFactor = 1.0F;
   public float damageBonus = 0.0F;
   private final int enchantability;

   public ItemMillenaireBow(String itemName, float speedFactor, float damageBonus, int enchantability) {
      this.speedFactor = speedFactor;
      this.damageBonus = damageBonus;
      this.enchantability = enchantability;
      this.setTranslationKey("millenaire." + itemName);
      this.setRegistryName(itemName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
   }

   public int getItemEnchantability() {
      return this.enchantability;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
   }
}
