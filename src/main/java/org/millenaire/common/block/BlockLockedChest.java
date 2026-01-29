package org.millenaire.common.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.client.network.ClientSender;
import org.millenaire.common.entity.TileEntityLockedChest;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.ui.ContainerLockedChest;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;

public class BlockLockedChest extends BlockContainer {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   protected static final AxisAlignedBB NORTH_CHEST_AABB = new AxisAlignedBB(0.0625, 0.0, 0.0, 0.9375, 0.875, 0.9375);
   protected static final AxisAlignedBB SOUTH_CHEST_AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 1.0);
   protected static final AxisAlignedBB WEST_CHEST_AABB = new AxisAlignedBB(0.0, 0.0, 0.0625, 0.9375, 0.875, 0.9375);
   protected static final AxisAlignedBB EAST_CHEST_AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 1.0, 0.875, 0.9375);
   protected static final AxisAlignedBB NOT_CONNECTED_AABB = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375);

   public static ContainerLockedChest createContainer(World world, int i, int j, int k, EntityPlayer entityplayer) {
      TileEntityLockedChest lockedchest = (TileEntityLockedChest)world.getTileEntity(new BlockPos(i, j, k));
      IInventory chest = getInventory(lockedchest, world, i, j, k);
      Building building = Mill.getMillWorld(world).getBuilding(lockedchest.buildingPos);
      return new ContainerLockedChest(entityplayer.inventory, chest, entityplayer, building, lockedchest.isLockedFor(entityplayer));
   }

   public static IInventory getInventory(TileEntityLockedChest lockedchest, World world, int i, int j, int k) {
      String largename = lockedchest.getInvLargeName();
      ILockableContainer chest = lockedchest;
      Block block = world.getBlockState(new BlockPos(i, j, k)).getBlock();
      if (world.getBlockState(new BlockPos(i - 1, j, k)).getBlock() == block) {
         chest = new InventoryLargeChest(largename, (TileEntityLockedChest)world.getTileEntity(new BlockPos(i - 1, j, k)), lockedchest);
      }

      if (world.getBlockState(new BlockPos(i + 1, j, k)).getBlock() == block) {
         chest = new InventoryLargeChest(largename, chest, (TileEntityLockedChest)world.getTileEntity(new BlockPos(i + 1, j, k)));
      }

      if (world.getBlockState(new BlockPos(i, j, k - 1)).getBlock() == block) {
         chest = new InventoryLargeChest(largename, (TileEntityLockedChest)world.getTileEntity(new BlockPos(i, j, k - 1)), chest);
      }

      if (world.getBlockState(new BlockPos(i, j, k + 1)).getBlock() == block) {
         chest = new InventoryLargeChest(largename, chest, (TileEntityLockedChest)world.getTileEntity(new BlockPos(i, j, k + 1)));
      }

      return chest;
   }

   public BlockLockedChest(String blockName) {
      super(Material.WOOD);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setHarvestLevel("axe", 0);
      this.setHardness(50.0F);
      this.setResistance(2000.0F);
      this.setSoundType(SoundType.WOOD);
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      int i = 0;
      BlockPos blockpos = pos.west();
      BlockPos blockpos1 = pos.east();
      BlockPos blockpos2 = pos.north();
      BlockPos blockpos3 = pos.south();
      if (worldIn.getBlockState(blockpos).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos)) {
            return false;
         }

         i++;
      }

      if (worldIn.getBlockState(blockpos1).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos1)) {
            return false;
         }

         i++;
      }

      if (worldIn.getBlockState(blockpos2).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos2)) {
            return false;
         }

         i++;
      }

      if (worldIn.getBlockState(blockpos3).getBlock() == this) {
         if (this.isDoubleChest(worldIn, blockpos3)) {
            return false;
         }

         i++;
      }

      return i <= 1;
   }

   public boolean canProvidePower(IBlockState state) {
      return false;
   }

   public IBlockState checkForSurroundingChests(World worldIn, BlockPos pos, IBlockState state) {
      if (worldIn.isRemote) {
         return state;
      } else {
         IBlockState iblockstate = worldIn.getBlockState(pos.north());
         IBlockState iblockstate1 = worldIn.getBlockState(pos.south());
         IBlockState iblockstate2 = worldIn.getBlockState(pos.west());
         IBlockState iblockstate3 = worldIn.getBlockState(pos.east());
         EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
         if (iblockstate.getBlock() != this && iblockstate1.getBlock() != this) {
            boolean flag = iblockstate.isFullBlock();
            boolean flag1 = iblockstate1.isFullBlock();
            if (iblockstate2.getBlock() == this || iblockstate3.getBlock() == this) {
               BlockPos blockpos1 = iblockstate2.getBlock() == this ? pos.west() : pos.east();
               IBlockState iblockstate7 = worldIn.getBlockState(blockpos1.north());
               IBlockState iblockstate6 = worldIn.getBlockState(blockpos1.south());
               enumfacing = EnumFacing.SOUTH;
               EnumFacing enumfacing2;
               if (iblockstate2.getBlock() == this) {
                  enumfacing2 = (EnumFacing)iblockstate2.getValue(FACING);
               } else {
                  enumfacing2 = (EnumFacing)iblockstate3.getValue(FACING);
               }

               if (enumfacing2 == EnumFacing.NORTH) {
                  enumfacing = EnumFacing.NORTH;
               }

               if ((flag || iblockstate7.isFullBlock()) && !flag1 && !iblockstate6.isFullBlock()) {
                  enumfacing = EnumFacing.SOUTH;
               }

               if ((flag1 || iblockstate6.isFullBlock()) && !flag && !iblockstate7.isFullBlock()) {
                  enumfacing = EnumFacing.NORTH;
               }
            }
         } else {
            BlockPos blockpos = iblockstate.getBlock() == this ? pos.north() : pos.south();
            IBlockState iblockstate4 = worldIn.getBlockState(blockpos.west());
            IBlockState iblockstate5 = worldIn.getBlockState(blockpos.east());
            enumfacing = EnumFacing.EAST;
            EnumFacing enumfacing1;
            if (iblockstate.getBlock() == this) {
               enumfacing1 = (EnumFacing)iblockstate.getValue(FACING);
            } else {
               enumfacing1 = (EnumFacing)iblockstate1.getValue(FACING);
            }

            if (enumfacing1 == EnumFacing.WEST) {
               enumfacing = EnumFacing.WEST;
            }

            if ((iblockstate2.isFullBlock() || iblockstate4.isFullBlock()) && !iblockstate3.isFullBlock() && !iblockstate5.isFullBlock()) {
               enumfacing = EnumFacing.EAST;
            }

            if ((iblockstate3.isFullBlock() || iblockstate5.isFullBlock()) && !iblockstate2.isFullBlock() && !iblockstate4.isFullBlock()) {
               enumfacing = EnumFacing.WEST;
            }
         }

         state = state.withProperty(FACING, enumfacing);
         worldIn.setBlockState(pos, state, 3);
         return state;
      }
   }

   public IBlockState correctFacing(World worldIn, BlockPos pos, IBlockState state) {
      EnumFacing enumfacing = null;

      for (EnumFacing enumfacing1 : Plane.HORIZONTAL) {
         IBlockState iblockstate = worldIn.getBlockState(pos.offset(enumfacing1));
         if (iblockstate.getBlock() == this) {
            return state;
         }

         if (iblockstate.isFullBlock()) {
            if (enumfacing != null) {
               enumfacing = null;
               break;
            }

            enumfacing = enumfacing1;
         }
      }

      if (enumfacing != null) {
         return state.withProperty(FACING, enumfacing.getOpposite());
      } else {
         EnumFacing enumfacing2 = (EnumFacing)state.getValue(FACING);
         if (worldIn.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
            enumfacing2 = enumfacing2.getOpposite();
         }

         if (worldIn.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
            enumfacing2 = enumfacing2.rotateY();
         }

         if (worldIn.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
            enumfacing2 = enumfacing2.getOpposite();
         }

         return state.withProperty(FACING, enumfacing2);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   public TileEntity createNewTileEntity(World world, int p_149915_2_) {
      return new TileEntityLockedChest();
   }

   public TileEntity createTileEntity(World world, IBlockState state) {
      return new TileEntityLockedChest();
   }

   public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      return BlockFaceShape.UNDEFINED;
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      if (source.getBlockState(pos.north()).getBlock() == this) {
         return NORTH_CHEST_AABB;
      } else if (source.getBlockState(pos.south()).getBlock() == this) {
         return SOUTH_CHEST_AABB;
      } else if (source.getBlockState(pos.west()).getBlock() == this) {
         return WEST_CHEST_AABB;
      } else {
         return source.getBlockState(pos.east()).getBlock() == this ? EAST_CHEST_AABB : NOT_CONNECTED_AABB;
      }
   }

   public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
      return Container.calcRedstoneFromInventory(this.getLockableContainer(worldIn, pos));
   }

   public ILockableContainer getContainer(World worldIn, BlockPos pos, boolean allowBlocking) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (!(tileentity instanceof TileEntityLockedChest)) {
         return null;
      } else {
         ILockableContainer millChest = (TileEntityLockedChest)tileentity;
         if (this.isBlocked(worldIn, pos)) {
            return null;
         } else {
            for (EnumFacing enumfacing : Plane.HORIZONTAL) {
               BlockPos blockpos1 = pos.offset(enumfacing);
               Block block = worldIn.getBlockState(blockpos1).getBlock();
               if (block == this) {
                  if (this.isBlocked(worldIn, blockpos1)) {
                     return null;
                  }

                  TileEntity tileentity1 = worldIn.getTileEntity(blockpos1);
                  if (tileentity1 instanceof TileEntityLockedChest) {
                     if (enumfacing != EnumFacing.WEST && enumfacing != EnumFacing.NORTH) {
                        millChest = new TileEntityLockedChest.InventoryLockedLargeChest(
                           "container.chestDouble", (TileEntityLockedChest)millChest, (TileEntityLockedChest)tileentity1
                        );
                     } else {
                        millChest = new TileEntityLockedChest.InventoryLockedLargeChest(
                           "container.chestDouble", (TileEntityLockedChest)tileentity1, (TileEntityLockedChest)millChest
                        );
                     }
                  }
               }
            }

            return millChest;
         }
      }
   }

   @Nullable
   public ILockableContainer getLockableContainer(World worldIn, BlockPos pos) {
      return this.getContainer(worldIn, pos, false);
   }

   public int getMetaFromState(IBlockState state) {
      return ((EnumFacing)state.getValue(FACING)).getIndex();
   }

   public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
   }

   public IBlockState getStateFromMeta(int meta) {
      EnumFacing enumfacing = EnumFacing.byIndex(meta);
      if (enumfacing.getAxis() == Axis.Y) {
         enumfacing = EnumFacing.NORTH;
      }

      return this.getDefaultState().withProperty(FACING, enumfacing);
   }

   public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      return side == EnumFacing.UP ? blockState.getWeakPower(blockAccess, pos, side) : 0;
   }

   public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
      if (!blockState.canProvidePower()) {
         return 0;
      } else {
         int i = 0;
         TileEntity tileentity = blockAccess.getTileEntity(pos);
         if (tileentity instanceof TileEntityLockedChest) {
            i = ((TileEntityLockedChest)tileentity).numPlayersUsing;
         }

         return MathHelper.clamp(i, 0, 15);
      }
   }

   public boolean hasComparatorInputOverride(IBlockState state) {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean hasCustomBreakingProgress(IBlockState state) {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), ""));
   }

   private boolean isBelowSolidBlock(World worldIn, BlockPos pos) {
      return worldIn.getBlockState(pos.up()).doesSideBlockChestOpening(worldIn, pos.up(), EnumFacing.DOWN);
   }

   private boolean isBlocked(World worldIn, BlockPos pos) {
      return this.isBelowSolidBlock(worldIn, pos) || this.isOcelotSittingOnChest(worldIn, pos);
   }

   private boolean isDoubleChest(World worldIn, BlockPos pos) {
      if (worldIn.getBlockState(pos).getBlock() != this) {
         return false;
      } else {
         for (EnumFacing enumfacing : Plane.HORIZONTAL) {
            if (worldIn.getBlockState(pos.offset(enumfacing)).getBlock() == this) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   private boolean isOcelotSittingOnChest(World worldIn, BlockPos pos) {
      for (Entity entity : worldIn.getEntitiesWithinAABB(
         EntityOcelot.class,
         new AxisAlignedBB(
            pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1
         )
      )) {
         EntityOcelot entityocelot = (EntityOcelot)entity;
         if (entityocelot.isSitting()) {
            return true;
         }
      }

      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityLockedChest) {
         tileentity.updateContainingBlockInfo();
      }
   }

   public boolean onBlockActivated(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      if (worldIn.isRemote) {
         ClientSender.activateMillChest(playerIn, new Point(pos));
      }

      return true;
   }

   public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
      this.checkForSurroundingChests(worldIn, pos, state);

      for (EnumFacing enumfacing : Plane.HORIZONTAL) {
         BlockPos blockpos = pos.offset(enumfacing);
         IBlockState iblockstate = worldIn.getBlockState(blockpos);
         if (iblockstate.getBlock() == this) {
            this.checkForSurroundingChests(worldIn, blockpos, iblockstate);
         }
      }
   }

   public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer playerIn) {
      super.onBlockClicked(worldIn, pos, playerIn);
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      EnumFacing enumfacing = EnumFacing.byHorizontalIndex(MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5) & 3).getOpposite();
      state = state.withProperty(FACING, enumfacing);
      BlockPos blockpos = pos.north();
      BlockPos blockpos1 = pos.south();
      BlockPos blockpos2 = pos.west();
      BlockPos blockpos3 = pos.east();
      boolean flag = this == worldIn.getBlockState(blockpos).getBlock();
      boolean flag1 = this == worldIn.getBlockState(blockpos1).getBlock();
      boolean flag2 = this == worldIn.getBlockState(blockpos2).getBlock();
      boolean flag3 = this == worldIn.getBlockState(blockpos3).getBlock();
      if (!flag && !flag1 && !flag2 && !flag3) {
         worldIn.setBlockState(pos, state, 3);
      } else if (enumfacing.getAxis() != Axis.X || !flag && !flag1) {
         if (enumfacing.getAxis() == Axis.Z && (flag2 || flag3)) {
            if (flag2) {
               worldIn.setBlockState(blockpos2, state, 3);
            } else {
               worldIn.setBlockState(blockpos3, state, 3);
            }

            worldIn.setBlockState(pos, state, 3);
         }
      } else {
         if (flag) {
            worldIn.setBlockState(blockpos, state, 3);
         } else {
            worldIn.setBlockState(blockpos1, state, 3);
         }

         worldIn.setBlockState(pos, state, 3);
      }

      if (stack.hasDisplayName()) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityLockedChest) {
            ((TileEntityLockedChest)tileentity).setCustomName(stack.getDisplayName());
         }
      }
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
      return !this.isDoubleChest(world, pos) && super.rotateBlock(world, pos, axis);
   }

   public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
      return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
   }

   public IBlockState withRotation(IBlockState state, Rotation rot) {
      return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }
}
