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

public class MockBlockDecor extends Block implements IMetaBlockName {
   public static final PropertyEnum<MockBlockDecor.Type> TYPE = PropertyEnum.create("type", MockBlockDecor.Type.class);

   public MockBlockDecor(String blockName) {
      super(Material.ROCK);
      this.disableStats();
      this.translucent = true;
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setBlockUnbreakable();
      this.setCreativeTab(MillBlocks.tabMillenaireContentCreator);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TYPE});
   }

   public int damageDropped(IBlockState state) {
      return ((MockBlockDecor.Type)state.getValue(TYPE)).getMetadata();
   }

   public int getMetaFromState(IBlockState state) {
      return ((MockBlockDecor.Type)state.getValue(TYPE)).meta;
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire."
         + this.getRegistryName().getPath()
         + "."
         + ((MockBlockDecor.Type)this.getStateFromMeta(stack.getMetadata()).getValue(TYPE)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(TYPE, MockBlockDecor.Type.fromMeta(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (MockBlockDecor.Type enumtype : MockBlockDecor.Type.values()) {
         items.add(new ItemStack(this, 1, enumtype.getMetadata()));
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (MockBlockDecor.Type enumtype : MockBlockDecor.Type.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "type=" + enumtype.getName())
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
         if (MockBlockDecor.Type.fromMeta(meta) == null) {
            meta = 0;
         }

         worldIn.setBlockState(pos, state.withProperty(TYPE, MockBlockDecor.Type.fromMeta(meta)), 3);
         Mill.proxy.sendLocalChat(playerIn, 'a', MockBlockDecor.Type.fromMeta(meta).name);
         return true;
      } else {
         return false;
      }
   }

   public static enum Type implements IStringSerializable {
      TAPESTRY(0, "tapestry"),
      INDIAN_STATUE(1, "indianstatue"),
      BYZ_ICON_SMALL(2, "byzantineiconsmall"),
      BYZ_ICON_MEDIUM(3, "byzantineiconmedium"),
      BYZ_ICON_LARGE(4, "byzantineiconlarge"),
      MAYAN_STATUE(5, "mayanstatue"),
      HIDE_HANGING(6, "hidehanging"),
      WALL_CARPET_SMALL(7, "wallcarpetsmall"),
      WALL_CARPET_MEDIUM(8, "wallcarpetmedium"),
      WALL_CARPET_LARGE(9, "wallcarpetlarge");

      public final int meta;
      public final String name;

      public static MockBlockDecor.Type fromMeta(int meta) {
         for (MockBlockDecor.Type t : values()) {
            if (t.meta == meta) {
               return t;
            }
         }

         return null;
      }

      public static int getMetaFromName(String name) {
         for (MockBlockDecor.Type type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
               return type.meta;
            }
         }

         return -1;
      }

      private Type(int m, String n) {
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
         return "Decor Block (" + this.name + ")";
      }
   }
}
