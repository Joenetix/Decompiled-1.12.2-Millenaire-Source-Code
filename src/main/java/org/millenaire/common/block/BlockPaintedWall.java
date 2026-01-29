package org.millenaire.common.block;

import net.minecraft.block.Block;
import net.minecraft.item.EnumDyeColor;

public class BlockPaintedWall extends BlockMillWall implements IPaintedBlock {
   private final String baseBlockName;
   private final EnumDyeColor colour;

   public BlockPaintedWall(String baseBlockName, Block baseBlock, EnumDyeColor colour) {
      super(baseBlockName + "_" + colour.getName(), baseBlock);
      this.baseBlockName = baseBlockName;
      this.colour = colour;
      this.setHarvestLevel("pickaxe", 0);
      this.useNeighborBrightness = true;
   }

   @Override
   public String getBlockType() {
      return this.baseBlockName;
   }

   @Override
   public EnumDyeColor getDyeColour() {
      return this.colour;
   }
}
