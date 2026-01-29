package org.millenaire.common.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockWall;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMillWall extends Block {
   public static final PropertyBool UP = PropertyBool.create("up");
   public static final PropertyBool NORTH = PropertyBool.create("north");
   public static final PropertyBool EAST = PropertyBool.create("east");
   public static final PropertyBool SOUTH = PropertyBool.create("south");
   public static final PropertyBool WEST = PropertyBool.create("west");
   protected static final AxisAlignedBB[] AABB_BY_INDEX = new AxisAlignedBB[]{
      new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.0, 0.75),
      new AxisAlignedBB(0.25, 0.0, 0.25, 0.75, 1.0, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.25, 0.75, 1.0, 0.75),
      new AxisAlignedBB(0.0, 0.0, 0.25, 0.75, 1.0, 1.0),
      new AxisAlignedBB(0.25, 0.0, 0.0, 0.75, 1.0, 0.75),
      new AxisAlignedBB(0.3125, 0.0, 0.0, 0.6875, 0.875, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 0.75, 1.0, 0.75),
      new AxisAlignedBB(0.0, 0.0, 0.0, 0.75, 1.0, 1.0),
      new AxisAlignedBB(0.25, 0.0, 0.25, 1.0, 1.0, 0.75),
      new AxisAlignedBB(0.25, 0.0, 0.25, 1.0, 1.0, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.3125, 1.0, 0.875, 0.6875),
      new AxisAlignedBB(0.0, 0.0, 0.25, 1.0, 1.0, 1.0),
      new AxisAlignedBB(0.25, 0.0, 0.0, 1.0, 1.0, 0.75),
      new AxisAlignedBB(0.25, 0.0, 0.0, 1.0, 1.0, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 0.75),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
   };
   protected static final AxisAlignedBB[] CLIP_AABB_BY_INDEX = new AxisAlignedBB[]{
      AABB_BY_INDEX[0].setMaxY(1.5),
      AABB_BY_INDEX[1].setMaxY(1.5),
      AABB_BY_INDEX[2].setMaxY(1.5),
      AABB_BY_INDEX[3].setMaxY(1.5),
      AABB_BY_INDEX[4].setMaxY(1.5),
      AABB_BY_INDEX[5].setMaxY(1.5),
      AABB_BY_INDEX[6].setMaxY(1.5),
      AABB_BY_INDEX[7].setMaxY(1.5),
      AABB_BY_INDEX[8].setMaxY(1.5),
      AABB_BY_INDEX[9].setMaxY(1.5),
      AABB_BY_INDEX[10].setMaxY(1.5),
      AABB_BY_INDEX[11].setMaxY(1.5),
      AABB_BY_INDEX[12].setMaxY(1.5),
      AABB_BY_INDEX[13].setMaxY(1.5),
      AABB_BY_INDEX[14].setMaxY(1.5),
      AABB_BY_INDEX[15].setMaxY(1.5)
   };
   private final Block baseBlock;

   private static int getAABBIndex(IBlockState state) {
      int i = 0;
      if ((Boolean)state.getValue(NORTH)) {
         i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
      }

      if ((Boolean)state.getValue(EAST)) {
         i |= 1 << EnumFacing.EAST.getHorizontalIndex();
      }

      if ((Boolean)state.getValue(SOUTH)) {
         i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
      }

      if ((Boolean)state.getValue(WEST)) {
         i |= 1 << EnumFacing.WEST.getHorizontalIndex();
      }

      return i;
   }

   protected static boolean isExcepBlockForAttachWithPiston(Block p_194143_0_) {
      return Block.isExceptBlockForAttachWithPiston(p_194143_0_)
         || p_194143_0_ == Blocks.BARRIER
         || p_194143_0_ == Blocks.MELON_BLOCK
         || p_194143_0_ == Blocks.PUMPKIN
         || p_194143_0_ == Blocks.LIT_PUMPKIN;
   }

   public BlockMillWall(String blockName, Block baseBlock) {
      super(baseBlock.getMaterial(null));
      this.baseBlock = baseBlock;
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setDefaultState(
         this.blockState
            .getBaseState()
            .withProperty(UP, false)
            .withProperty(NORTH, false)
            .withProperty(EAST, false)
            .withProperty(SOUTH, false)
            .withProperty(WEST, false)
      );
      this.setHardness(baseBlock.getBlockHardness(null, null, null));
      this.setResistance(baseBlock.getExplosionResistance(null) * 5.0F / 3.0F);
      this.setSoundType(baseBlock.getSoundType());
   }

   public void addCollisionBoxToList(
      IBlockState state,
      World worldIn,
      BlockPos pos,
      AxisAlignedBB entityBox,
      List<AxisAlignedBB> collidingBoxes,
      @Nullable Entity entityIn,
      boolean isActualState
   ) {
      if (!isActualState) {
         state = this.getActualState(state, worldIn, pos);
      }

      addCollisionBoxToList(pos, entityBox, collidingBoxes, CLIP_AABB_BY_INDEX[getAABBIndex(state)]);
   }

   public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
      Block connector = world.getBlockState(pos.offset(facing)).getBlock();
      return connector instanceof BlockWall || connector instanceof BlockFenceGate || connector instanceof BlockMillWall;
   }

   private boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing p_176253_3_) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      Block block = iblockstate.getBlock();
      BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos, p_176253_3_);
      boolean flag = blockfaceshape == BlockFaceShape.MIDDLE_POLE_THICK || blockfaceshape == BlockFaceShape.MIDDLE_POLE && block instanceof BlockFenceGate;
      return !isExcepBlockForAttachWithPiston(block) && blockfaceshape == BlockFaceShape.SOLID || flag || block instanceof BlockPane;
   }

   public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
      return true;
   }

   private boolean canWallConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {
      BlockPos other = pos.offset(facing);
      Block block = world.getBlockState(other).getBlock();
      return block.canBeConnectedTo(world, other, facing.getOpposite()) || this.canConnectTo(world, other, facing.getOpposite());
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{UP, NORTH, EAST, WEST, SOUTH});
   }

   public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      boolean flag = this.canWallConnectTo(worldIn, pos, EnumFacing.NORTH);
      boolean flag1 = this.canWallConnectTo(worldIn, pos, EnumFacing.EAST);
      boolean flag2 = this.canWallConnectTo(worldIn, pos, EnumFacing.SOUTH);
      boolean flag3 = this.canWallConnectTo(worldIn, pos, EnumFacing.WEST);
      boolean flag4 = flag && !flag1 && flag2 && !flag3 || !flag && flag1 && !flag2 && flag3;
      return state.withProperty(UP, !flag4 || !worldIn.isAirBlock(pos.up()))
         .withProperty(NORTH, flag)
         .withProperty(EAST, flag1)
         .withProperty(SOUTH, flag2)
         .withProperty(WEST, flag3);
   }

   public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess world, BlockPos pos) {
      return PathNodeType.FENCE;
   }

   public Block getBaseBlock() {
      return this.baseBlock;
   }

   public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      return face != EnumFacing.UP && face != EnumFacing.DOWN ? BlockFaceShape.MIDDLE_POLE_THICK : BlockFaceShape.CENTER_BIG;
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      state = this.getActualState(state, source, pos);
      return AABB_BY_INDEX[getAABBIndex(state)];
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
      blockState = this.getActualState(blockState, worldIn, pos);
      return CLIP_AABB_BY_INDEX[getAABBIndex(blockState)];
   }

   public int getMetaFromState(IBlockState state) {
      return 0;
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState();
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), ""));
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
      return false;
   }

   public boolean onBlockActivated(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      return this.baseBlock.onBlockActivated(worldIn, pos, this.baseBlock.getDefaultState(), playerIn, hand, EnumFacing.DOWN, 0.0F, 0.0F, 0.0F);
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      return side == EnumFacing.DOWN ? super.shouldSideBeRendered(blockState, blockAccess, pos, side) : true;
   }
}
