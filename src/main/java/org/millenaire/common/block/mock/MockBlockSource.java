package org.millenaire.common.block.mock;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.IMetaBlockName;
import org.millenaire.common.item.MillItems;

public class MockBlockSource extends Block implements IMetaBlockName {
   public static final PropertyEnum<MockBlockSource.Resource> RESOURCE = PropertyEnum.create("resource", MockBlockSource.Resource.class);

   public MockBlockSource(String blockName) {
      super(Material.ROCK);
      this.disableStats();
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setBlockUnbreakable();
      this.setCreativeTab(MillBlocks.tabMillenaireContentCreator);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{RESOURCE});
   }

   public int damageDropped(IBlockState state) {
      return ((MockBlockSource.Resource)state.getValue(RESOURCE)).getMetadata();
   }

   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.CUTOUT_MIPPED;
   }

   public int getMetaFromState(IBlockState state) {
      return ((MockBlockSource.Resource)state.getValue(RESOURCE)).meta;
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire."
         + this.getRegistryName().getPath()
         + "."
         + ((MockBlockSource.Resource)this.getStateFromMeta(stack.getMetadata()).getValue(RESOURCE)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(RESOURCE, MockBlockSource.Resource.fromMeta(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (MockBlockSource.Resource enumtype : MockBlockSource.Resource.values()) {
         items.add(new ItemStack(this, 1, enumtype.getMetadata()));
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (MockBlockSource.Resource enumtype : MockBlockSource.Resource.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "resource=" + enumtype.getName())
         );
      }
   }

   public boolean onBlockActivated(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      if (worldIn.isRemote) {
         return true;
      } else if (playerIn.getHeldItem(EnumHand.MAIN_HAND).getItem() == MillItems.NEGATION_WAND) {
         int meta = state.getBlock().getMetaFromState(state) + 1;
         if (MockBlockSource.Resource.fromMeta(meta) == null) {
            meta = 0;
         }

         worldIn.setBlockState(pos, state.withProperty(RESOURCE, MockBlockSource.Resource.fromMeta(meta)), 3);
         Mill.proxy.sendLocalChat(playerIn, 'a', MockBlockSource.Resource.fromMeta(meta).name);
         return true;
      } else {
         return false;
      }
   }

   public static enum Resource implements IStringSerializable {
      STONE(0, "stone"),
      SAND(1, "sand"),
      SANDSTONE(2, "sandstone"),
      CLAY(3, "clay"),
      GRAVEL(4, "gravel"),
      GRANITE(5, "granite"),
      DORITE(6, "diorite"),
      ANDERITE(7, "andesite"),
      SNOW(8, "snow"),
      ICE(9, "ice"),
      RED_SANDSTONE(10, "redsandstone"),
      QUARTZ(11, "quartz");

      public final int meta;
      public final String name;

      public static MockBlockSource.Resource fromMeta(int meta) {
         for (MockBlockSource.Resource t : values()) {
            if (t.meta == meta) {
               return t;
            }
         }

         return null;
      }

      private Resource(int m, String n) {
         this.meta = m;
         this.name = n;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String getName() {
         return this.name;
      }

      @Override
      public String toString() {
         return "Source Block (" + this.name + ")";
      }
   }
}
