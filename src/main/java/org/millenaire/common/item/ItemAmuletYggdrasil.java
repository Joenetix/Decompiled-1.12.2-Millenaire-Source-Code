package org.millenaire.common.item;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.utilities.Point;

public class ItemAmuletYggdrasil extends ItemMill {
   public ItemAmuletYggdrasil(String itemName) {
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
               if (world.getTotalWorldTime() != this.lastUpdateTick) {
                  int level = 0;
                  Point p = new Point(entityIn);
                  level = (int)Math.floor(p.getiY());
                  if (level > 127) {
                     level = 127;
                  }

                  this.savedScore = level / 8;
                  return this.savedScore;
               } else {
                  return this.savedScore;
               }
            }
         }
      });
   }
}
