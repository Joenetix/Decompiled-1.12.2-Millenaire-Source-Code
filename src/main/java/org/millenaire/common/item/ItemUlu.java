package org.millenaire.common.item;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.block.BlockSod;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.utilities.LanguageUtilities;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

public class ItemUlu extends ItemMill {
   private static final IBlockState COARSE_DIRT = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, DirtType.COARSE_DIRT);

   public ItemUlu(String itemName) {
      super(itemName);
   }

   private EnumActionResult attemptSodPlanks(EntityPlayer player, World world, BlockPos pos, EnumFacing side, EnumHand hand) {
      if (world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER) {
         side = EnumFacing.DOWN;
      } else {
         if (side == EnumFacing.DOWN) {
            pos = pos.down();
         }

         if (side == EnumFacing.UP) {
            pos = pos.up();
         }

         if (side == EnumFacing.EAST) {
            pos = pos.east();
         }

         if (side == EnumFacing.WEST) {
            pos = pos.west();
         }

         if (side == EnumFacing.SOUTH) {
            pos = pos.south();
         }

         if (side == EnumFacing.NORTH) {
            pos = pos.north();
         }
      }

      if (world.getBlockState(pos).getBlock() != Blocks.AIR) {
         return EnumActionResult.PASS;
      } else {
         ItemStack is = player.getHeldItem(hand);
         EnumType chosenPlankType = null;

         for (EnumType plankType : EnumType.values()) {
            if (chosenPlankType == null
               && MillCommonUtilities.countChestItems(
                     player.inventory, Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, plankType)
                  )
                  > 0) {
               chosenPlankType = plankType;
            }
         }

         if (chosenPlankType == null) {
            if (!world.isRemote) {
               ServerSender.sendTranslatedSentence(player, 'f', "ui.uluexplanations");
               ServerSender.sendTranslatedSentence(player, '6', "ui.ulunoplanks");
            }

            return EnumActionResult.PASS;
         } else {
            if (!is.hasTagCompound()) {
               is.setTagCompound(new NBTTagCompound());
            }

            int resUseCount = is.getTagCompound().getInteger("resUseCount");
            if (resUseCount == 0) {
               if (MillCommonUtilities.countChestItems(player.inventory, COARSE_DIRT) == 0) {
                  if (!world.isRemote) {
                     ServerSender.sendTranslatedSentence(player, '6', "ui.ulunodirt");
                  }

                  return EnumActionResult.PASS;
               }

               WorldUtilities.getItemsFromChest(player.inventory, COARSE_DIRT, 1);
               WorldUtilities.getItemsFromChest(
                  player.inventory, Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, chosenPlankType), 1
               );
               resUseCount = 3;
            } else {
               resUseCount--;
            }

            is.getTagCompound().setInteger("resUseCount", resUseCount);
            WorldUtilities.setBlockstate(world, new Point(pos), MillBlocks.SOD.getDefaultState().withProperty(BlockSod.VARIANT, chosenPlankType), true, true);
            is.damageItem(1, player);
            return EnumActionResult.SUCCESS;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public String getItemStackDisplayName(ItemStack stack) {
      if (stack.getTagCompound() != null) {
         int resUseCount = stack.getTagCompound().getInteger("resUseCount");
         return super.getItemStackDisplayName(stack) + " - " + LanguageUtilities.string("ui.ulusodplanksleft", "" + resUseCount);
      } else {
         return super.getItemStackDisplayName(stack);
      }
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      ItemStack uluIS = player.getHeldItem(hand);
      if (world.getBlockState(pos).getBlock() == Blocks.SNOW) {
         world.setBlockState(pos, Blocks.AIR.getDefaultState());
         MillCommonUtilities.putItemsInChest(player.inventory, MillBlocks.SNOW_BRICK, 0, 4);
         uluIS.damageItem(1, player);
         return EnumActionResult.SUCCESS;
      } else if (world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER) {
         int snowDepth = (Integer)world.getBlockState(pos).getValue(BlockSnow.LAYERS);
         world.setBlockState(pos, Blocks.AIR.getDefaultState());
         MillCommonUtilities.putItemsInChest(player.inventory, MillBlocks.SNOW_BRICK, 0, (snowDepth + 1) / 2);
         uluIS.damageItem(1, player);
         return EnumActionResult.SUCCESS;
      } else if (world.getBlockState(pos).getBlock() == Blocks.ICE) {
         MillCommonUtilities.putItemsInChest(player.inventory, MillBlocks.ICE_BRICK, 0, 4);
         world.setBlockState(pos, Blocks.AIR.getDefaultState());
         uluIS.damageItem(1, player);
         return EnumActionResult.SUCCESS;
      } else {
         return this.attemptSodPlanks(player, world, pos, side, hand);
      }
   }
}
