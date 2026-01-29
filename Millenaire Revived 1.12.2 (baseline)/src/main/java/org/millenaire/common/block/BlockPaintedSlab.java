package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPaintedSlab extends BlockHalfSlab implements IPaintedBlock {
   private final EnumDyeColor colour;
   private final String blockType;

   public BlockPaintedSlab(String blockType, Block baseBlock, EnumDyeColor colour) {
      super(baseBlock);
      this.blockType = blockType;
      this.colour = colour;
      this.setTranslationKey("millenaire." + blockType + "_" + colour.getName());
      this.setRegistryName(blockType + "_" + colour.getName());
      this.setHarvestLevel("pickaxe", 0);
      this.setHardness(1.5F);
      this.setResistance(10.0F);
   }

   @Override
   public String getBlockType() {
      return this.blockType;
   }

   @Override
   public EnumDyeColor getDyeColour() {
      return this.colour;
   }

   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(MillBlocks.PAINTED_BRICK_MAP.get(this.getBlockType()).get(this.colour));
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getItemFromBlock(MillBlocks.PAINTED_BRICK_MAP.get(this.getBlockType()).get(this.colour));
   }
}
