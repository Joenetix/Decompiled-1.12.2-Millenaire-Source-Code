package org.millenaire.common.block;

import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockRosette extends BlockPane {
   public static final PropertyBool ROSETTE_NORTH = PropertyBool.create("ros_n");
   public static final PropertyBool ROSETTE_EAST = PropertyBool.create("ros_e");
   public static final PropertyBool ROSETTE_SOUTH = PropertyBool.create("ros_s");
   public static final PropertyBool ROSETTE_WEST = PropertyBool.create("ros_w");
   public static final PropertyBool ROSETTE_UP = PropertyBool.create("ros_u");
   public static final PropertyBool ROSETTE_DOWN = PropertyBool.create("ros_d");

   public BlockRosette(String blockName, Material material, SoundType soundType) {
      super(material, true);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setSoundType(soundType);
      this.setDefaultState(
         this.blockState
            .getBaseState()
            .withProperty(NORTH, false)
            .withProperty(EAST, false)
            .withProperty(SOUTH, false)
            .withProperty(WEST, false)
            .withProperty(ROSETTE_NORTH, false)
            .withProperty(ROSETTE_EAST, false)
            .withProperty(ROSETTE_SOUTH, false)
            .withProperty(ROSETTE_WEST, false)
            .withProperty(ROSETTE_UP, false)
            .withProperty(ROSETTE_DOWN, false)
      );
   }

   public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }
   }

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
      return new BlockStateContainer(
         this,
         new IProperty[]{
            NORTH, EAST, WEST, SOUTH, ROSETTE_NORTH, ROSETTE_EAST, ROSETTE_WEST, ROSETTE_SOUTH, ROSETTE_UP, ROSETTE_DOWN
         }
      );
   }

   public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
      return super.getActualState(state, world, pos)
         .withProperty(ROSETTE_NORTH, this.hasRosette(world, pos, EnumFacing.NORTH))
         .withProperty(ROSETTE_EAST, this.hasRosette(world, pos, EnumFacing.EAST))
         .withProperty(ROSETTE_SOUTH, this.hasRosette(world, pos, EnumFacing.SOUTH))
         .withProperty(ROSETTE_WEST, this.hasRosette(world, pos, EnumFacing.WEST))
         .withProperty(ROSETTE_UP, this.hasRosette(world, pos, EnumFacing.UP))
         .withProperty(ROSETTE_DOWN, this.hasRosette(world, pos, EnumFacing.DOWN));
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.TRANSLUCENT;
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return MapColor.GRAY;
   }

   private boolean hasRosette(IBlockAccess world, BlockPos pos, EnumFacing direction) {
      return world.getBlockState(pos.offset(direction)).getBlock() == this;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "variant=inventory"));
   }

   public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }
   }
}
