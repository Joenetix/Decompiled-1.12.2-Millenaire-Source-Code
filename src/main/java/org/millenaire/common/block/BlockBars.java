package org.millenaire.common.block;

import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBars extends BlockPane {
   protected BlockBars(String blockName) {
      super(Material.WOOD, true);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setHardness(5.0F);
      this.setResistance(10.0F);
      this.setSoundType(SoundType.WOOD);
      this.setCreativeTab(MillBlocks.tabMillenaire);
   }

   public boolean canPaneConnectTo(IBlockAccess world, BlockPos pos, EnumFacing dir) {
      BlockPos other = pos.offset(dir);
      IBlockState state = world.getBlockState(other);
      return state.getBlock().canBeConnectedTo(world, other, dir.getOpposite())
         || this.attachesTo(world, state, other, dir.getOpposite())
         || state.getBlock() instanceof BlockMillWall;
   }

   public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return state.withProperty(NORTH, this.canPaneConnectTo(worldIn, pos, EnumFacing.NORTH) || this.canPaneConnectTo(worldIn, pos, EnumFacing.SOUTH))
         .withProperty(SOUTH, this.canPaneConnectTo(worldIn, pos, EnumFacing.SOUTH) || this.canPaneConnectTo(worldIn, pos, EnumFacing.NORTH))
         .withProperty(WEST, this.canPaneConnectTo(worldIn, pos, EnumFacing.WEST) || this.canPaneConnectTo(worldIn, pos, EnumFacing.EAST))
         .withProperty(EAST, this.canPaneConnectTo(worldIn, pos, EnumFacing.EAST) || this.canPaneConnectTo(worldIn, pos, EnumFacing.WEST));
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), ""));
   }
}
