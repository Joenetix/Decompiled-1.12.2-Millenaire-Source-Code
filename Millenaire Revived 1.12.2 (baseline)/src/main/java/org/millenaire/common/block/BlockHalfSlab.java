package org.millenaire.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockHalfSlab extends Block {
   public static final PropertyEnum<EnumBlockHalf> HALF = PropertyEnum.create("half", EnumBlockHalf.class);
   protected static final AxisAlignedBB AABB_BOTTOM_HALF = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);
   protected static final AxisAlignedBB AABB_TOP_HALF = new AxisAlignedBB(0.0, 0.5, 0.0, 1.0, 1.0, 1.0);
   private final Block baseBlock;

   public BlockHalfSlab(Block fullBlock) {
      super(fullBlock.getDefaultState().getMaterial(), fullBlock.getDefaultState().getMapColor(null, null));
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.baseBlock = fullBlock;
      this.fullBlock = false;
      this.setLightOpacity(255);
      this.useNeighborBrightness = true;
   }

   protected boolean canSilkHarvest() {
      return false;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HALF});
   }

   public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
      if (ForgeModContainer.disableStairSlabCulling) {
         return super.doesSideBlockRendering(state, world, pos, face);
      } else if (state.isOpaqueCube()) {
         return true;
      } else {
         EnumBlockHalf side = (EnumBlockHalf)state.getValue(HALF);
         return side == EnumBlockHalf.TOP && face == EnumFacing.UP || side == EnumBlockHalf.BOTTOM && face == EnumFacing.DOWN;
      }
   }

   public Block getBaseBlock() {
      return this.baseBlock;
   }

   public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      if (face == EnumFacing.UP && state.getValue(HALF) == EnumBlockHalf.TOP) {
         return BlockFaceShape.SOLID;
      } else {
         return face == EnumFacing.DOWN && state.getValue(HALF) == EnumBlockHalf.BOTTOM ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
      }
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return state.getValue(HALF) == EnumBlockHalf.TOP ? AABB_TOP_HALF : AABB_BOTTOM_HALF;
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      if (state.getValue(HALF) == EnumBlockHalf.TOP) {
         i |= 8;
      }

      return i;
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(HALF, EnumBlockHalf.BOTTOM);
      return facing == EnumFacing.DOWN || facing != EnumFacing.UP && !(hitY <= 0.5) ? iblockstate.withProperty(HALF, EnumBlockHalf.TOP) : iblockstate;
   }

   public IBlockState getStateFromMeta(int meta) {
      IBlockState iblockstate = this.getDefaultState();
      return iblockstate.withProperty(HALF, (meta & 8) == 0 ? EnumBlockHalf.BOTTOM : EnumBlockHalf.TOP);
   }

   public String getUnlocalizedName(int meta) {
      return super.getTranslationKey();
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "half=bottom"));
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
      IBlockState state = this.getActualState(base_state, world, pos);
      return state.getValue(HALF) == EnumBlockHalf.TOP && side == EnumFacing.UP
         || state.getValue(HALF) == EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN;
   }

   public boolean isTopSolid(IBlockState state) {
      return state.getValue(HALF) == EnumBlockHalf.TOP;
   }

   public boolean onBlockActivated(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      return this.baseBlock.onBlockActivated(worldIn, pos, this.baseBlock.getDefaultState(), playerIn, hand, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
   }
}
