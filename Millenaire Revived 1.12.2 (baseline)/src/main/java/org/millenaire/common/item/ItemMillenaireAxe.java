package org.millenaire.common.item;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.block.MillBlocks;

public class ItemMillenaireAxe extends ItemAxe {
   public ItemMillenaireAxe(String itemName, ToolMaterial material, float damage, float speed) {
      super(material, damage, speed);
      this.setTranslationKey("millenaire." + itemName);
      this.setRegistryName(itemName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
   }
}
