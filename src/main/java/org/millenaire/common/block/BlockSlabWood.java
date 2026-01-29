package org.millenaire.common.block;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public class BlockSlabWood extends BlockSlab implements IMetaBlockName {
   public static final PropertyBool SEAMLESS = PropertyBool.create("seamless");
   public static final PropertyEnum<BlockDecorativeWood.EnumType> VARIANT = PropertyEnum.create("variant", BlockDecorativeWood.EnumType.class);

   public BlockSlabWood(String blockName) {
      super(Material.WOOD);
      IBlockState iblockstate = this.blockState.getBaseState();
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, false);
      } else {
         iblockstate = iblockstate.withProperty(HALF, EnumBlockHalf.BOTTOM);
      }

      this.setDefaultState(iblockstate.withProperty(VARIANT, BlockDecorativeWood.EnumType.TIMBERFRAMEPLAIN));
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setHardness(2.0F);
      this.setResistance(5.0F);
      this.setSoundType(SoundType.WOOD);
      this.useNeighborBrightness = true;
   }

   protected BlockStateContainer createBlockState() {
      return this.isDouble()
         ? new BlockStateContainer(this, new IProperty[]{SEAMLESS, VARIANT})
         : new BlockStateContainer(this, new IProperty[]{HALF, VARIANT});
   }

   public int damageDropped(IBlockState state) {
      return ((BlockDecorativeWood.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return ((BlockDecorativeWood.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      i |= ((BlockDecorativeWood.EnumType)state.getValue(VARIANT)).getMetadata();
      if (this.isDouble()) {
         if ((Boolean)state.getValue(SEAMLESS)) {
            i |= 8;
         }
      } else if (state.getValue(HALF) == EnumBlockHalf.TOP) {
         i |= 8;
      }

      return i;
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return this.getTranslationKey(stack.getMetadata());
   }

   public IBlockState getStateFromMeta(int meta) {
      IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, BlockDecorativeWood.EnumType.byMetadata(meta & 7));
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, (meta & 8) != 0);
      } else {
         iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
      }

      return iblockstate;
   }

   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (BlockDecorativeWood.EnumType enumtype : BlockDecorativeWood.EnumType.values()) {
         if (enumtype.hasSlab()) {
            items.add(new ItemStack(this, 1, enumtype.getMetadata()));
         }
      }
   }

   public Comparable<?> getTypeForItem(ItemStack stack) {
      return BlockDecorativeWood.EnumType.byMetadata(stack.getMetadata() & 7);
   }

   public String getTranslationKey(int meta) {
      return "tile.millenaire.slabs_" + BlockDecorativeWood.EnumType.byMetadata(meta).getUnlocalizedName();
   }

   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockDecorativeWood.EnumType enumtype : BlockDecorativeWood.EnumType.values()) {
         if (enumtype.hasSlab()) {
            ModelLoader.setCustomModelResourceLocation(
               Item.getItemFromBlock(this),
               enumtype.getMetadata(),
               new ModelResourceLocation(this.getRegistryName(), "half=bottom,variant=" + enumtype.getName())
            );
         }
      }
   }

   public boolean isDouble() {
      return false;
   }
}
