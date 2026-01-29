package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public abstract class BlockOrientedSlab extends BlockSlab implements IMetaBlockName {
   public static final PropertyEnum<BlockOrientedSlab.Variant> VARIANT = PropertyEnum.create("variant", BlockOrientedSlab.Variant.class);
   public static final PropertyEnum<Axis> AXIS = PropertyEnum.create("axis", Axis.class);

   public BlockOrientedSlab(String slabName) {
      super(Material.ROCK);
      IBlockState iblockstate = this.blockState.getBaseState();
      if (!this.isDouble()) {
         iblockstate = iblockstate.withProperty(HALF, EnumBlockHalf.BOTTOM);
         this.useNeighborBrightness = true;
      }

      iblockstate = iblockstate.withProperty(VARIANT, BlockOrientedSlab.Variant.DEFAULT);
      this.setDefaultState(iblockstate.withProperty(AXIS, Axis.X));
      this.setHarvestLevel("pickaxe", 0);
      this.setHardness(1.5F);
      this.setResistance(10.0F);
      this.setTranslationKey("millenaire." + slabName);
      this.setRegistryName(slabName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
   }

   protected BlockStateContainer createBlockState() {
      return this.isDouble()
         ? new BlockStateContainer(this, new IProperty[]{VARIANT, AXIS})
         : new BlockStateContainer(this, new IProperty[]{VARIANT, HALF, AXIS});
   }

   public int damageDropped(IBlockState state) {
      return 0;
   }

   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(state.getBlock());
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemFromBlock(state.getBlock());
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return MapColor.ADOBE;
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      Axis enumfacing$axis = (Axis)state.getValue(AXIS);
      if (enumfacing$axis == Axis.X) {
         i |= 4;
      } else if (enumfacing$axis == Axis.Z) {
         i |= 8;
      }

      if (!this.isDouble() && state.getValue(HALF) == EnumBlockHalf.TOP) {
         i |= 2;
      }

      return i;
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return this.getTranslationKey();
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      IBlockState iblockstate = this.getStateFromMeta(meta);
      if (facing.getAxis() == Axis.Y) {
         iblockstate = iblockstate.withProperty(AXIS, Axis.X);
      } else {
         iblockstate = iblockstate.withProperty(AXIS, facing.getAxis());
      }

      if (!this.isDouble()) {
         iblockstate = facing == EnumFacing.DOWN || facing != EnumFacing.UP && !(hitY <= 0.5)
            ? iblockstate.withProperty(HALF, EnumBlockHalf.TOP)
            : iblockstate.withProperty(HALF, EnumBlockHalf.BOTTOM);
      }

      return iblockstate;
   }

   public IBlockState getStateFromMeta(int meta) {
      Axis enumfacing$axis = Axis.Y;
      int i = meta & 12;
      if (i == 4) {
         enumfacing$axis = Axis.X;
      } else if (i == 8) {
         enumfacing$axis = Axis.Z;
      }

      IBlockState iblockstate = this.getDefaultState().withProperty(AXIS, enumfacing$axis);
      if (!this.isDouble()) {
         iblockstate = iblockstate.withProperty(HALF, (meta & 2) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
      }

      return iblockstate;
   }

   public Comparable<?> getTypeForItem(ItemStack stack) {
      return BlockOrientedSlab.Variant.DEFAULT;
   }

   public String getTranslationKey(int meta) {
      return this.getTranslationKey();
   }

   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      if (this.isDouble()) {
         ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "axis=x,variant=default"));
      } else {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "axis=x,half=bottom,variant=default")
         );
      }
   }

   public int quantityDropped(Random random) {
      return 1;
   }

   public static class BlockOrientedSlabDouble extends BlockOrientedSlab {
      public BlockOrientedSlabDouble(String slabName) {
         super(slabName);
      }

      public boolean isDouble() {
         return true;
      }
   }

   public static class BlockOrientedSlabSlab extends BlockOrientedSlab {
      public BlockOrientedSlabSlab(String slabName) {
         super(slabName);
      }

      public boolean isDouble() {
         return false;
      }
   }

   public static enum Variant implements IStringSerializable {
      DEFAULT;

      public String getName() {
         return "default";
      }
   }
}
