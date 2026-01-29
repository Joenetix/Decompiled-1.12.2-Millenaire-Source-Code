package org.millenaire.common.block;

import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRosetteBars extends BlockBars {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   static final PropertyEnum<EnumBlockHalf> TOP_BOTTOM = PropertyEnum.create("topbottom", EnumBlockHalf.class);

   public BlockRosetteBars(String blockName, Material material, SoundType soundType) {
      super(blockName);
      this.setDefaultState(
         this.blockState
            .getBaseState()
            .withProperty(NORTH, false)
            .withProperty(EAST, false)
            .withProperty(SOUTH, false)
            .withProperty(WEST, false)
            .withProperty(FACING, EnumFacing.SOUTH)
            .withProperty(TOP_BOTTOM, EnumBlockHalf.TOP)
      );
   }

   public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }
   }

   @Override
   public boolean canPaneConnectTo(IBlockAccess world, BlockPos pos, EnumFacing dir) {
      BlockPos other = pos.offset(dir);
      IBlockState state = world.getBlockState(other);
      return state.getBlock().canBeConnectedTo(world, other, dir.getOpposite())
         || this.attachesTo(world, state, other, dir.getOpposite())
         || state.getBlock() instanceof BlockMillWall;
   }

   protected boolean canSilkHarvest() {
      return false;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, WEST, SOUTH, FACING, TOP_BOTTOM});
   }

   public int damageDropped(IBlockState state) {
      return this.getMetaFromState(this.getDefaultState());
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.TRANSLUCENT;
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return MapColor.GRAY;
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      if (state.getValue(TOP_BOTTOM) == EnumBlockHalf.BOTTOM) {
         i |= 4;
      }

      return i | ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
   }

   public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      IBlockState iBlockStateAbove = world.getBlockState(pos.add(0, 1, 0));
      if (iBlockStateAbove.getBlock() == this && iBlockStateAbove.getValue(TOP_BOTTOM) == EnumBlockHalf.TOP) {
         return this.getDefaultState().withProperty(TOP_BOTTOM, EnumBlockHalf.BOTTOM).withProperty(FACING, iBlockStateAbove.getValue(FACING));
      } else {
         IBlockState iBlockStateWest = world.getBlockState(pos.add(-1, 0, 0));
         if (iBlockStateWest.getBlock() == this && iBlockStateWest.getValue(FACING) == EnumFacing.WEST) {
            return this.getDefaultState().withProperty(FACING, EnumFacing.EAST).withProperty(TOP_BOTTOM, iBlockStateWest.getValue(TOP_BOTTOM));
         } else {
            IBlockState iBlockStateSouth = world.getBlockState(pos.add(0, 0, 1));
            if (iBlockStateSouth.getBlock() == this && iBlockStateSouth.getValue(FACING) == EnumFacing.SOUTH) {
               return this.getDefaultState().withProperty(FACING, EnumFacing.NORTH).withProperty(TOP_BOTTOM, iBlockStateSouth.getValue(TOP_BOTTOM));
            } else {
               IBlockState iBlockStateBelow = world.getBlockState(pos.add(0, -1, 0));
               if (iBlockStateBelow.getBlock() == this && iBlockStateBelow.getValue(TOP_BOTTOM) == EnumBlockHalf.BOTTOM) {
                  return this.getDefaultState().withProperty(TOP_BOTTOM, EnumBlockHalf.TOP).withProperty(FACING, iBlockStateBelow.getValue(FACING));
               } else {
                  IBlockState iBlockStateEast = world.getBlockState(pos.add(1, 0, 0));
                  if (iBlockStateEast.getBlock() == this && iBlockStateEast.getValue(FACING) == EnumFacing.EAST) {
                     return this.getDefaultState().withProperty(FACING, EnumFacing.WEST).withProperty(TOP_BOTTOM, iBlockStateEast.getValue(TOP_BOTTOM));
                  } else {
                     IBlockState iBlockStateNorth = world.getBlockState(pos.add(0, 0, -1));
                     if (iBlockStateNorth.getBlock() == this && iBlockStateNorth.getValue(FACING) == EnumFacing.NORTH) {
                        return this.getDefaultState()
                           .withProperty(FACING, EnumFacing.SOUTH)
                           .withProperty(TOP_BOTTOM, iBlockStateNorth.getValue(TOP_BOTTOM));
                     } else {
                        IBlockState basicState = this.getDefaultState();
                        if (!iBlockStateAbove.isFullBlock() && iBlockStateBelow.isFullBlock()) {
                           basicState = basicState.withProperty(TOP_BOTTOM, EnumBlockHalf.BOTTOM);
                        }

                        if (!iBlockStateWest.isFullBlock() && iBlockStateEast.isFullBlock()) {
                           basicState = basicState.withProperty(FACING, EnumFacing.EAST);
                        } else if (!iBlockStateSouth.isFullBlock() && iBlockStateNorth.isFullBlock()) {
                           basicState = basicState.withProperty(FACING, EnumFacing.NORTH);
                        } else if (iBlockStateSouth.isFullBlock() && !iBlockStateNorth.isFullBlock()) {
                           basicState = basicState.withProperty(FACING, EnumFacing.SOUTH);
                        } else if (iBlockStateWest.isFullBlock() && !iBlockStateEast.isFullBlock()) {
                           basicState = basicState.withProperty(FACING, EnumFacing.WEST);
                        }

                        return basicState;
                     }
                  }
               }
            }
         }
      }
   }

   public IBlockState getStateFromMeta(int meta) {
      IBlockState iblockstate = this.getDefaultState();
      if ((meta & 4) == 4) {
         iblockstate = iblockstate.withProperty(TOP_BOTTOM, EnumBlockHalf.BOTTOM);
      }

      EnumFacing enumfacing = EnumFacing.byHorizontalIndex(meta & 3);
      if (enumfacing.getAxis() == Axis.Y) {
         enumfacing = EnumFacing.NORTH;
      }

      return iblockstate.withProperty(FACING, enumfacing);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "variant=inventory"));
   }

   public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }
   }

   public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
      switch (mirrorIn) {
         case LEFT_RIGHT:
            return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
         case FRONT_BACK:
            return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
         default:
            return super.withMirror(state, mirrorIn);
      }
   }

   public IBlockState withRotation(IBlockState state, Rotation rot) {
      switch (rot) {
         case CLOCKWISE_180:
            return state.withProperty(NORTH, state.getValue(SOUTH))
               .withProperty(EAST, state.getValue(WEST))
               .withProperty(SOUTH, state.getValue(NORTH))
               .withProperty(WEST, state.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return state.withProperty(NORTH, state.getValue(EAST))
               .withProperty(EAST, state.getValue(SOUTH))
               .withProperty(SOUTH, state.getValue(WEST))
               .withProperty(WEST, state.getValue(NORTH));
         case CLOCKWISE_90:
            return state.withProperty(NORTH, state.getValue(WEST))
               .withProperty(EAST, state.getValue(NORTH))
               .withProperty(SOUTH, state.getValue(EAST))
               .withProperty(WEST, state.getValue(SOUTH));
         default:
            return state;
      }
   }
}
