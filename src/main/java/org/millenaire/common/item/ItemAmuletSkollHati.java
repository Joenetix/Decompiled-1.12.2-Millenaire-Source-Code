package org.millenaire.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillLog;

public class ItemAmuletSkollHati extends ItemMill {
   public ItemAmuletSkollHati(String itemName) {
      super(itemName);
   }

   public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
      if (MillConfigValues.LogOther >= 3) {
         MillLog.debug(this, "Using skoll amulet.");
      }

      if (worldIn.isRemote) {
         return new ActionResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
      } else {
         long time = worldIn.getWorldTime() + 24000L;
         if (time % 24000L > 11000L && time % 24000L < 23500L) {
            worldIn.setWorldTime(time - time % 24000L - 500L);
         } else {
            worldIn.setWorldTime(time - time % 24000L + 13000L);
         }

         return new ActionResult(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
      }
   }
}
