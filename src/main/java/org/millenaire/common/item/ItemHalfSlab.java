package org.millenaire.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.block.BlockHalfSlab;

public class ItemHalfSlab extends ItemBlock {
   private final BlockHalfSlab singleSlab;
   private final Block fullBlock;

   public ItemHalfSlab(BlockHalfSlab singleSlab) {
      super(singleSlab);
      this.singleSlab = singleSlab;
      this.fullBlock = singleSlab.getBaseBlock();
      this.setMaxDamage(0);
   }

   @SideOnly(Side.CLIENT)
   public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == this.singleSlab) {
         boolean flag = iblockstate.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP;
         if (side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag) {
            return true;
         }
      }

      BlockPos var9 = pos.offset(side);
      IBlockState iblockstate1 = worldIn.getBlockState(var9);
      return iblockstate1.getBlock() == this.singleSlab ? true : super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
   }

   public int getMetadata(int damage) {
      return damage;
   }

   public String getTranslationKey(ItemStack stack) {
      return this.singleSlab.getTranslationKey();
   }

   public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
      ItemStack itemstack = player.getHeldItem(hand);
      if (!itemstack.isEmpty() && player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
         IBlockState iblockstate = worldIn.getBlockState(pos);
         if (iblockstate.getBlock() == this.singleSlab) {
            EnumBlockHalf blockslab$enumblockhalf = (EnumBlockHalf)iblockstate.getValue(BlockSlab.HALF);
            if (facing == EnumFacing.UP && blockslab$enumblockhalf == EnumBlockHalf.BOTTOM
               || facing == EnumFacing.DOWN && blockslab$enumblockhalf == EnumBlockHalf.TOP) {
               AxisAlignedBB axisalignedbb = this.fullBlock.getDefaultState().getCollisionBoundingBox(worldIn, pos);
               if (axisalignedbb != Block.NULL_AABB
                  && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos))
                  && worldIn.setBlockState(pos, this.fullBlock.getDefaultState(), 11)) {
                  SoundType soundtype = this.fullBlock.getSoundType(this.fullBlock.getDefaultState(), worldIn, pos, player);
                  worldIn.playSound(
                     player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F
                  );
                  itemstack.shrink(1);
                  if (player instanceof EntityPlayerMP) {
                     CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, itemstack);
                  }
               }

               return EnumActionResult.SUCCESS;
            }
         }

         return this.tryPlace(player, itemstack, worldIn, pos.offset(facing))
            ? EnumActionResult.SUCCESS
            : super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
      } else {
         return EnumActionResult.FAIL;
      }
   }

   private boolean tryPlace(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos) {
      IBlockState blockStateInPlace = worldIn.getBlockState(pos);
      if (blockStateInPlace.getBlock() == this.singleSlab) {
         AxisAlignedBB axisalignedbb = this.fullBlock.getDefaultState().getCollisionBoundingBox(worldIn, pos);
         if (axisalignedbb != Block.NULL_AABB
            && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos))
            && worldIn.setBlockState(pos, this.fullBlock.getDefaultState(), 11)) {
            SoundType soundtype = this.fullBlock.getSoundType(this.fullBlock.getDefaultState(), worldIn, pos, player);
            worldIn.playSound(
               player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F
            );
            stack.shrink(1);
         }

         return true;
      } else {
         return false;
      }
   }
}
