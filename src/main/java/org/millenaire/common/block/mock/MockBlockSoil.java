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

public class MockBlockSoil extends Block implements IMetaBlockName {
   public static final PropertyEnum<MockBlockSoil.CropType> CROPTYPE = PropertyEnum.create("croptype", MockBlockSoil.CropType.class);

   public MockBlockSoil(String blockName) {
      super(Material.ROCK);
      this.disableStats();
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setBlockUnbreakable();
      this.setCreativeTab(MillBlocks.tabMillenaireContentCreator);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{CROPTYPE});
   }

   public int damageDropped(IBlockState state) {
      return ((MockBlockSoil.CropType)state.getValue(CROPTYPE)).getMetadata();
   }

   public int getMetaFromState(IBlockState state) {
      return ((MockBlockSoil.CropType)state.getValue(CROPTYPE)).meta;
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire."
         + this.getRegistryName().getPath()
         + "."
         + ((MockBlockSoil.CropType)this.getStateFromMeta(stack.getMetadata()).getValue(CROPTYPE)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(CROPTYPE, MockBlockSoil.CropType.fromMeta(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (MockBlockSoil.CropType enumtype : MockBlockSoil.CropType.values()) {
         items.add(new ItemStack(this, 1, enumtype.getMetadata()));
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (MockBlockSoil.CropType enumtype : MockBlockSoil.CropType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "croptype=" + enumtype.getName())
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
         if (MockBlockSoil.CropType.fromMeta(meta) == null) {
            meta = 0;
         }

         worldIn.setBlockState(pos, state.withProperty(CROPTYPE, MockBlockSoil.CropType.fromMeta(meta)), 3);
         Mill.proxy.sendLocalChat(playerIn, 'a', MockBlockSoil.CropType.fromMeta(meta).name);
         return true;
      } else {
         return false;
      }
   }

   public static enum CropType implements IStringSerializable {
      WHEAT(0, "soil"),
      RICE(1, "ricesoil"),
      TURMERIC(2, "turmericsoil"),
      SUGAR_CANE(3, "sugarcanesoil"),
      POTATO(4, "potatosoil"),
      NETHER_WART(5, "netherwartsoil"),
      GRAPE(6, "vinesoil"),
      MAIZE(7, "maizesoil"),
      CACAO(8, "cacaospot"),
      CARROT(9, "carrotsoil"),
      FLOWER(10, "flowersoil"),
      COTTON(11, "cottonsoil");

      public final int meta;
      public final String name;

      public static MockBlockSoil.CropType fromMeta(int meta) {
         for (MockBlockSoil.CropType t : values()) {
            if (t.meta == meta) {
               return t;
            }
         }

         return null;
      }

      private CropType(int m, String n) {
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
