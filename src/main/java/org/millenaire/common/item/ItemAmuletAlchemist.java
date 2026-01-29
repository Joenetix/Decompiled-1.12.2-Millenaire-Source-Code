package org.millenaire.common.item;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

public class ItemAmuletAlchemist extends ItemMill {
   private static final int radius = 5;

   public ItemAmuletAlchemist(String itemName) {
      super(itemName);
      this.addPropertyOverride(new ResourceLocation("score"), new IItemPropertyGetter() {
         @SideOnly(Side.CLIENT)
         long lastUpdateTick;
         @SideOnly(Side.CLIENT)
         float savedScore;

         @SideOnly(Side.CLIENT)
         public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entityIn) {
            if (entityIn == null) {
               return 0.0F;
            } else {
               world = entityIn.world;
               if (world.getTotalWorldTime() == this.lastUpdateTick) {
                  return this.savedScore;
               } else {
                  float score = 0.0F;
                  if (world != null && entityIn != null) {
                     Point p = new Point(entityIn);
                     int startY = Math.max(p.getiY() - 5, 0);
                     int endY = Math.min(p.getiY() + 5, 127);

                     for (int i = p.getiX() - 5; i < p.getiX() + 5; i++) {
                        for (int j = p.getiZ() - 5; j < p.getiZ() + 5; j++) {
                           for (int k = startY; k < endY; k++) {
                              Block block = WorldUtilities.getBlock(world, i, k, j);
                              if (block == Blocks.COAL_ORE) {
                                 score++;
                              } else if (block == Blocks.DIAMOND_ORE) {
                                 score += 30.0F;
                              } else if (block == Blocks.EMERALD_ORE) {
                                 score += 30.0F;
                              } else if (block == Blocks.GOLD_ORE) {
                                 score += 10.0F;
                              } else if (block == Blocks.IRON_ORE) {
                                 score += 5.0F;
                              } else if (block == Blocks.LAPIS_ORE) {
                                 score += 10.0F;
                              } else if (block == Blocks.REDSTONE_ORE) {
                                 score += 5.0F;
                              } else if (block == Blocks.LIT_REDSTONE_ORE) {
                                 score += 5.0F;
                              }
                           }
                        }
                     }
                  }

                  if (score > 100.0F) {
                     score = 100.0F;
                  }

                  this.savedScore = score * 15.0F / 100.0F;
                  this.lastUpdateTick = world.getTotalWorldTime();
                  return this.savedScore;
               }
            }
         }
      });
   }
}
