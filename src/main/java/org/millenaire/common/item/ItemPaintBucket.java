package org.millenaire.common.item;

import net.minecraft.item.EnumDyeColor;
import org.millenaire.common.block.BlockPaintedBricks;

public class ItemPaintBucket extends ItemMill {
   private final EnumDyeColor colour;

   public ItemPaintBucket(String baseName, EnumDyeColor colour) {
      super(baseName + "_" + BlockPaintedBricks.getColorName(colour));
      this.colour = colour;
   }

   public EnumDyeColor getColour() {
      return this.colour;
   }
}
