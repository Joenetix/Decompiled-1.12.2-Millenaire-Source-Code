package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public class BlockExtendedMudBrick extends BlockSlab implements IMetaBlockName {
   public static final PropertyEnum<BlockExtendedMudBrick.EnumType> VARIANT = PropertyEnum.create("variant", BlockExtendedMudBrick.EnumType.class);

   public BlockExtendedMudBrick(String blockName) {
      super(Material.ROCK);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setHarvestLevel("pickaxe", 0);
      this.setHardness(1.5F);
      this.setResistance(10.0F);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockExtendedMudBrick.EnumType.MUDBRICK_SMOOTH));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public int damageDropped(IBlockState state) {
      return ((BlockExtendedMudBrick.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return ((BlockExtendedMudBrick.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockExtendedMudBrick.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire." + ((BlockExtendedMudBrick.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(VARIANT)).getName();
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.getStateFromMeta(meta);
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, BlockExtendedMudBrick.EnumType.byMetadata(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (BlockExtendedMudBrick.EnumType enumtype : BlockExtendedMudBrick.EnumType.values()) {
         items.add(new ItemStack(this, 1, enumtype.getMetadata()));
      }
   }

   public Comparable<?> getTypeForItem(ItemStack stack) {
      return BlockExtendedMudBrick.EnumType.byMetadata(stack.getMetadata() & 7);
   }

   public String getTranslationKey(int meta) {
      return "tile.millenaire." + ((BlockExtendedMudBrick.EnumType)this.getStateFromMeta(meta).getValue(VARIANT)).getName();
   }

   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockExtendedMudBrick.EnumType enumtype : BlockExtendedMudBrick.EnumType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "variant=" + enumtype.getName())
         );
      }
   }

   public boolean isDouble() {
      return true;
   }

   public int quantityDropped(Random random) {
      return 1;
   }

   public static enum EnumType implements IStringSerializable {
      MUDBRICK_SMOOTH(0, "mudbrick_smooth", MapColor.BROWN, true),
      MUDBRICK_SELJUK_DECORATED(1, "mudbrick_seljuk_decorated", MapColor.BLUE, true),
      MUDBRICK_SELJUK_ORNAMENTED(2, "mudbrick_seljuk_ornamented", MapColor.BROWN, true);

      private static final BlockExtendedMudBrick.EnumType[] META_LOOKUP = new BlockExtendedMudBrick.EnumType[values().length];
      private final int meta;
      private final String name;
      private final MapColor mapColor;
      private final boolean hasSlab;

      public static BlockExtendedMudBrick.EnumType byMetadata(int meta) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      private EnumType(int meta, String name, MapColor mapColor, boolean hasSlab) {
         this.meta = meta;
         this.name = name;
         this.mapColor = mapColor;
         this.hasSlab = hasSlab;
      }

      public MapColor getMapColor() {
         return this.mapColor;
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

      public boolean hasSlab() {
         return this.hasSlab;
      }

      @Override
      public String toString() {
         return this.name;
      }

      static {
         BlockExtendedMudBrick.EnumType[] var0 = values();

         for (BlockExtendedMudBrick.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
