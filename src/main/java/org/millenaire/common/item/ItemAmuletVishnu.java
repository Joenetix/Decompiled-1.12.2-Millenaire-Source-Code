package org.millenaire.common.item;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

public class ItemAmuletVishnu extends ItemMill {
   private static final int radius = 20;

   public ItemAmuletVishnu(String itemName) {
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
                  double level = 0.0;
                  double closestDistance = Double.MAX_VALUE;
                  if (world != null && entityIn != null) {
                     Point p = new Point(entityIn);

                     for (Entity ent : WorldUtilities.getEntitiesWithinAABB(world, EntityMob.class, p, 20, 20)) {
                        if (p.distanceTo(ent) < closestDistance) {
                           closestDistance = p.distanceTo(ent);
                        }
                     }
                  }

                  if (closestDistance > 20.0) {
                     level = 0.0;
                  } else {
                     level = (20.0 - closestDistance) / 20.0;
                  }

                  this.savedScore = (float)(level * 15.0);
                  this.lastUpdateTick = world.getTotalWorldTime();
                  return this.savedScore;
               }
            }
         }
      });
   }
}
