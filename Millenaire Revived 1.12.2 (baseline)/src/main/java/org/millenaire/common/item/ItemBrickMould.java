package org.millenaire.common.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

public class ItemBrickMould extends ItemMill {
   public ItemBrickMould(String itemName) {
      super(itemName);
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      if (world.getBlockState(pos).getBlock() == Blocks.SNOW) {
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

      if (!world.mayPlace(MillBlocks.WET_BRICK, pos, false, side, (Entity)null)) {
         return EnumActionResult.PASS;
      } else if (world.getBlockState(pos).getBlock() != Blocks.AIR) {
         return EnumActionResult.PASS;
      } else {
         ItemStack is = player.getHeldItem(hand);
         if (is.getItemDamage() % 4 == 0) {
            if (MillCommonUtilities.countChestItems(player.inventory, Blocks.DIRT, 0) == 0
               || MillCommonUtilities.countChestItems(player.inventory, Blocks.SAND, 0) == 0) {
               if (!world.isRemote) {
                  ServerSender.sendTranslatedSentence(player, 'f', "ui.brickinstructions");
               }

               return EnumActionResult.PASS;
            }

            WorldUtilities.getItemsFromChest(player.inventory, Blocks.DIRT, 0, 1);
            WorldUtilities.getItemsFromChest(player.inventory, Blocks.SAND, 0, 1);
         }

         WorldUtilities.setBlockstate(world, new Point(pos), MillBlocks.BS_WET_BRICK, true, false);
         is.damageItem(1, player);
         return EnumActionResult.SUCCESS;
      }
   }
}
