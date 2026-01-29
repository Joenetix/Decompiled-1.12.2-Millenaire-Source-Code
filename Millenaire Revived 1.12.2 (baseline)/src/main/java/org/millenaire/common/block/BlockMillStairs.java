package org.millenaire.common.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMillStairs extends BlockStairs {
   public BlockMillStairs(String blockName, IBlockState baseBlock) {
      super(baseBlock);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.useNeighborBrightness = true;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(
         Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "facing=east,half=bottom,shape=straight")
      );
   }
}
