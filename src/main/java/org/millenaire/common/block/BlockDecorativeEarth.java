package org.millenaire.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public class BlockDecorativeEarth extends Block implements IMetaBlockName {
   static final PropertyEnum<BlockDecorativeEarth.EnumType> VARIANT = PropertyEnum.create("variant", BlockDecorativeEarth.EnumType.class);

   public BlockDecorativeEarth(String blockName) {
      super(Material.GROUND);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setHarvestLevel("shovel", 0);
      this.setHardness(0.8F);
      this.setSoundType(SoundType.GROUND);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockDecorativeEarth.EnumType.DIRTWALL));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public int damageDropped(IBlockState state) {
      return ((BlockDecorativeEarth.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockDecorativeEarth.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire." + ((BlockDecorativeEarth.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(VARIANT)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, BlockDecorativeEarth.EnumType.byMetadata(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (BlockDecorativeEarth.EnumType enumtype : BlockDecorativeEarth.EnumType.values()) {
         items.add(new ItemStack(this, 1, enumtype.getMetadata()));
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockDecorativeEarth.EnumType enumtype : BlockDecorativeEarth.EnumType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "variant=" + enumtype.getName())
         );
      }
   }

   public static enum EnumType implements IStringSerializable {
      DIRTWALL(0, "dirtwall");

      private static final BlockDecorativeEarth.EnumType[] META_LOOKUP = new BlockDecorativeEarth.EnumType[values().length];
      private final int meta;
      private final String name;

      public static BlockDecorativeEarth.EnumType byMetadata(int meta) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      private EnumType(int meta, String name) {
         this.meta = meta;
         this.name = name;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      static {
         BlockDecorativeEarth.EnumType[] var0 = values();

         for (BlockDecorativeEarth.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
