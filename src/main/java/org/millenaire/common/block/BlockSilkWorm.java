package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
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

public class BlockSilkWorm extends Block implements IMetaBlockName {
   public static final PropertyEnum<BlockSilkWorm.EnumType> PROGRESS = PropertyEnum.create("progress", BlockSilkWorm.EnumType.class);

   public BlockSilkWorm(String blockName) {
      super(Material.WOOD);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setTickRandomly(true);
      this.setHarvestLevel("axe", 0);
      this.setHardness(2.0F);
      this.setResistance(5.0F);
      this.setSoundType(SoundType.WOOD);
      this.setDefaultState(this.blockState.getBaseState().withProperty(PROGRESS, BlockSilkWorm.EnumType.SILKWORMEMPTY));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{PROGRESS});
   }

   public int damageDropped(IBlockState state) {
      return 0;
   }

   public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      return BlockFaceShape.UNDEFINED;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockSilkWorm.EnumType)state.getValue(PROGRESS)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire." + ((BlockSilkWorm.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(PROGRESS)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(PROGRESS, BlockSilkWorm.EnumType.byMetadata(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      items.add(new ItemStack(this, 1, 0));
      items.add(new ItemStack(this, 1, 3));
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockSilkWorm.EnumType enumtype : BlockSilkWorm.EnumType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "progress=" + enumtype.getName())
         );
      }
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      int currentValue = ((BlockSilkWorm.EnumType)state.getValue(PROGRESS)).getMetadata();
      if (currentValue < 3 && worldIn.getLight(pos.up()) < 7 && rand.nextInt(2) == 0) {
         IBlockState newState = state.withProperty(PROGRESS, BlockSilkWorm.EnumType.byMetadata(++currentValue));
         worldIn.setBlockState(pos, newState);
      }
   }

   public static enum EnumType implements IStringSerializable {
      SILKWORMEMPTY(0, "silkwormempty"),
      SILKWORMIP1(1, "silkwormip1"),
      SILKWORMIP2(2, "silkwormip2"),
      SILKWORMFULL(3, "silkwormfull");

      private static final BlockSilkWorm.EnumType[] META_LOOKUP = new BlockSilkWorm.EnumType[values().length];
      private final int meta;
      private final String name;

      public static BlockSilkWorm.EnumType byMetadata(int meta) {
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
         BlockSilkWorm.EnumType[] var0 = values();

         for (BlockSilkWorm.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
