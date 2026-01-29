package org.millenaire.common.block;

import net.minecraft.block.Block;

public class BlockMillSlab extends BlockHalfSlab {
   public BlockMillSlab(String name, Block baseBlock) {
      super(baseBlock);
      this.setTranslationKey("millenaire." + name);
      this.setRegistryName(name);
      this.setHarvestLevel("pickaxe", 0);
      this.setHardness(1.5F);
      this.setResistance(10.0F);
   }
}
