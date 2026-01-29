package org.millenaire.common.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.buildingplan.BuildingImportExport;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;
import org.millenaire.common.world.MillWorldData;

public class ItemNegationWand extends ItemMill {
   public ItemNegationWand(String itemName) {
      super(itemName);
   }

   public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos bp, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
      Point pos = new Point(bp);
      IBlockState bs = world.getBlockState(bp);
      if (bs.getBlock() == MillBlocks.IMPORT_TABLE) {
         return EnumActionResult.PASS;
      } else if (world.isRemote) {
         if (bs.getBlock() == Blocks.STANDING_SIGN && world.isRemote) {
            BuildingImportExport.negationWandExportBuilding(player, world, pos);
            return EnumActionResult.SUCCESS;
         } else {
            return EnumActionResult.FAIL;
         }
      } else {
         MillWorldData mw = Mill.getMillWorld(world);

         for (int i = 0; i < 2; i++) {
            MillCommonUtilities.VillageList list;
            if (i == 0) {
               list = mw.loneBuildingsList;
            } else {
               list = mw.villagesList;
            }

            for (int j = 0; j < list.names.size(); j++) {
               Point p = list.pos.get(j);
               int distance = MathHelper.floor(p.horizontalDistanceTo(pos));
               if (distance <= 30) {
                  Building th = mw.getBuilding(p);
                  if (th != null && th.isTownhall) {
                     if (th.chestLocked && !MillConfigValues.DEV) {
                        ServerSender.sendTranslatedSentence(player, '6', "negationwand.villagelocked", th.villageType.name);
                        return EnumActionResult.SUCCESS;
                     }

                     ServerSender.displayNegationWandGUI(player, th);
                  }
               }
            }
         }

         return EnumActionResult.FAIL;
      }
   }
}
