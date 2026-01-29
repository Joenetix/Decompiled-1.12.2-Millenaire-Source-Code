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

public class BlockSlabStone extends BlockSlab implements IMetaBlockName {
   public static final PropertyBool SEAMLESS = PropertyBool.create("seamless");
   public static final PropertyEnum<BlockDecorativeStone.EnumType> VARIANT = PropertyEnum.create("variant", BlockDecorativeStone.EnumType.class);

   public BlockSlabStone(String blockName) {
      super(Material.ROCK);
      IBlockState iblockstate = this.blockState.getBaseState();
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, false);
      } else {
         iblockstate = iblockstate.withProperty(HALF, EnumBlockHalf.BOTTOM);
      }

      this.setDefaultState(iblockstate.withProperty(VARIANT, BlockDecorativeStone.EnumType.MUDBRICK));
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setHardness(2.0F);
      this.setResistance(10.0F);
      this.setSoundType(SoundType.STONE);
      this.useNeighborBrightness = true;
   }

   protected BlockStateContainer createBlockState() {
      return this.isDouble()
         ? new BlockStateContainer(this, new IProperty[]{SEAMLESS, VARIANT})
         : new BlockStateContainer(this, new IProperty[]{HALF, VARIANT});
   }

   public int damageDropped(IBlockState state) {
      return ((BlockDecorativeStone.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return ((BlockDecorativeStone.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      i |= ((BlockDecorativeStone.EnumType)state.getValue(VARIANT)).getMetadata();
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
      IBlockState iblockstate = this.getDefaultState().withProperty(VARIANT, BlockDecorativeStone.EnumType.byMetadata(meta & 7));
      if (this.isDouble()) {
         iblockstate = iblockstate.withProperty(SEAMLESS, (meta & 8) != 0);
      } else {
         iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
      }

      return iblockstate;
   }

   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (BlockDecorativeStone.EnumType enumtype : BlockDecorativeStone.EnumType.values()) {
         if (enumtype.hasSlab()) {
            items.add(new ItemStack(this, 1, enumtype.getMetadata()));
         }
      }
   }

   public Comparable<?> getTypeForItem(ItemStack stack) {
      return BlockDecorativeStone.EnumType.byMetadata(stack.getMetadata() & 7);
   }

   public String getTranslationKey(int meta) {
      return "tile.millenaire.slabs_" + ((BlockDecorativeStone.EnumType)this.getStateFromMeta(meta).getValue(VARIANT)).getName();
   }

   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockDecorativeStone.EnumType enumtype : BlockDecorativeStone.EnumType.values()) {
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
