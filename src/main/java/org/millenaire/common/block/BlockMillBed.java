package org.millenaire.common.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.entity.TileEntityMillBed;

public class BlockMillBed extends BlockBed {
   protected final AxisAlignedBB BED_AABB;
   private final int bedHeight;

   public BlockMillBed(String blockName, int height) {
      this.bedHeight = height;
      this.BED_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, height / 16.0, 1.0);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
   }

   public TileEntity createNewTileEntity(World worldIn, int meta) {
      return new TileEntityMillBed();
   }

   public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
      if (state.getValue(PART) == EnumPartType.HEAD) {
         ItemStack itemstack = this.getItem(worldIn, pos, state);
         spawnAsEntity(worldIn, pos, itemstack);
      }
   }

   public int getBedHeight() {
      return this.bedHeight;
   }

   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.CUTOUT_MIPPED;
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return this.BED_AABB;
   }

   @Deprecated
   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(Item.getItemFromBlock(this), 1, this.damageDropped(state));
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return state.getValue(PART) == EnumPartType.FOOT ? Items.AIR : Item.getItemFromBlock(this);
   }

   public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.MODEL;
   }

   public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
      if (state.getValue(PART) == EnumPartType.HEAD && te instanceof TileEntityBed) {
         ItemStack itemstack = this.getItem(worldIn, pos, state);
         spawnAsEntity(worldIn, pos, itemstack);
      } else {
         super.harvestBlock(worldIn, player, pos, state, (TileEntity)null, stack);
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), ""));
   }

   public boolean isBed(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable Entity player) {
      return true;
   }
}
