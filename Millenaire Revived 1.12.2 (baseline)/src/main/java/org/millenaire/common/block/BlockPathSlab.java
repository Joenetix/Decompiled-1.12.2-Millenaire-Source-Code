package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public class BlockPathSlab extends Block implements IMetaBlockName, IBlockPath {
   protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.4375, 1.0);
   protected static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 0.9375, 1.0);
   private final String singleSlabBlockName;
   private final String doubleSlabName;

   public BlockPathSlab(String blockName, MapColor color, SoundType soundType) {
      super(Material.GROUND, color);
      this.setSoundType(soundType);
      this.singleSlabBlockName = blockName + "_slab";
      this.doubleSlabName = blockName;
      this.setTranslationKey("millenaire." + this.doubleSlabName);
      this.setRegistryName(this.singleSlabBlockName);
      this.useNeighborBrightness = true;
      IBlockState iblockstate = this.blockState.getBaseState();
      iblockstate = iblockstate.withProperty(IBlockPath.STABLE, false);
      iblockstate = iblockstate.withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM);
      this.setDefaultState(iblockstate);
      this.setHarvestLevel("shovel", 0);
      this.setHardness(0.8F);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setSoundType(SoundType.GROUND);
      this.fullBlock = false;
      this.setLightOpacity(255);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{BlockSlab.HALF, IBlockPath.STABLE});
   }

   public int damageDropped(IBlockState state) {
      return 0;
   }

   public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
      if (ForgeModContainer.disableStairSlabCulling) {
         return super.doesSideBlockRendering(state, world, pos, face);
      } else {
         EnumBlockHalf side = (EnumBlockHalf)state.getValue(BlockSlab.HALF);
         return side == EnumBlockHalf.TOP && face == EnumFacing.UP || side == EnumBlockHalf.BOTTOM && face == EnumFacing.DOWN;
      }
   }

   public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      return face == EnumFacing.DOWN && state.getValue(BlockSlab.HALF) == EnumBlockHalf.BOTTOM ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
   }

   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
   }

   @Override
   public BlockPath getDoubleSlab() {
      return (BlockPath)Block.getBlockFromName("millenaire:" + this.doubleSlabName);
   }

   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(Block.getBlockFromName("millenaire:" + this.singleSlabBlockName), 1, 0);
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemFromBlock(Block.getBlockFromName("millenaire:" + this.singleSlabBlockName));
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      if ((Boolean)state.getValue(IBlockPath.STABLE)) {
         i |= 1;
      }

      if (state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP) {
         i |= 8;
      }

      return i;
   }

   @Override
   public BlockPathSlab getSingleSlab() {
      return (BlockPathSlab)Block.getBlockFromName("millenaire:" + this.singleSlabBlockName);
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return this.getTranslationKey();
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
         .withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM)
         .withProperty(IBlockPath.STABLE, true);
      return facing == EnumFacing.DOWN || facing != EnumFacing.UP && !(hitY <= 0.5)
         ? iblockstate.withProperty(BlockSlab.HALF, EnumBlockHalf.TOP)
         : iblockstate;
   }

   public IBlockState getStateFromMeta(int meta) {
      IBlockState iblockstate = this.getDefaultState();
      if ((meta & 1) == 1) {
         iblockstate = iblockstate.withProperty(IBlockPath.STABLE, true);
      }

      return iblockstate.withProperty(BlockSlab.HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "half=bottom,stable=false"));
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   @Override
   public boolean isFullPath() {
      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isTopSolid(IBlockState state) {
      return state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      switch (side) {
         case UP:
            return true;
         case NORTH:
         case SOUTH:
         case WEST:
         case EAST:
            IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
            Block block = iblockstate.getBlock();
            return !iblockstate.isOpaqueCube() && block != Blocks.FARMLAND && block != Blocks.GRASS_PATH;
         default:
            return super.shouldSideBeRendered(blockState, blockAccess, pos, side);
      }
   }
}
