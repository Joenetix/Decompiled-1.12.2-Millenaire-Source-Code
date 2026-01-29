package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant.EnumBlockHalf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class BlockGrapeVine extends BlockMillCrops {
   public static final PropertyEnum<EnumBlockHalf> HALF = PropertyEnum.create("half", EnumBlockHalf.class);

   public BlockGrapeVine(String cropName, boolean requireIrrigation, boolean slowGrowth, ResourceLocation seed) {
      super(cropName, requireIrrigation, slowGrowth, seed);
      this.setDefaultState(this.blockState.getBaseState().withProperty(HALF, EnumBlockHalf.LOWER));
   }

   @Override
   public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
      return state.getValue(HALF) == EnumBlockHalf.UPPER
         ? worldIn.getBlockState(pos.down()).getBlock() == this
            && worldIn.getBlockState(pos.down()).getValue(HALF) == EnumBlockHalf.LOWER
            && super.canBlockStay(worldIn, pos.down(), worldIn.getBlockState(pos.down()))
         : worldIn.getBlockState(pos.up()).getBlock() == this
            && worldIn.getBlockState(pos.up()).getValue(HALF) == EnumBlockHalf.UPPER
            && super.canBlockStay(worldIn, pos, state);
   }

   public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
      return super.canPlaceBlockAt(worldIn, pos) && worldIn.isAirBlock(pos.up());
   }

   protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
      if (!this.canBlockStay(worldIn, pos, state)) {
         boolean upper = state.getValue(HALF) == EnumBlockHalf.UPPER;
         BlockPos upperPos = upper ? pos : pos.up();
         BlockPos lowerPos = upper ? pos.down() : pos;
         Block upperBlock = (Block)(upper ? this : worldIn.getBlockState(upperPos).getBlock());
         Block lowerBlock = (Block)(upper ? worldIn.getBlockState(lowerPos).getBlock() : this);
         if (upperBlock == this) {
            worldIn.setBlockState(upperPos, Blocks.AIR.getDefaultState(), 2);
         }

         if (lowerBlock == this) {
            worldIn.setBlockState(lowerPos, Blocks.AIR.getDefaultState(), 3);
         }
      }
   }

   @Override
   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE, HALF});
   }

   @Override
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return FULL_BLOCK_AABB;
   }

   @Override
   public int getMetaFromState(IBlockState state) {
      int i = this.getAge(state);
      return i | (state.getValue(HALF) == EnumBlockHalf.UPPER ? 8 : 0);
   }

   @Override
   public IBlockState getStateFromMeta(int meta) {
      return this.withAge(meta & 7).withProperty(HALF, (meta & 8) > 0 ? EnumBlockHalf.UPPER : EnumBlockHalf.LOWER);
   }

   @Override
   public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      int i = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
      int j = this.getMaxAge();
      if (i > j) {
         i = j;
      }

      if (state.getValue(HALF) == EnumBlockHalf.UPPER) {
         worldIn.setBlockState(pos.down(), this.withAge(i), 2);
         worldIn.setBlockState(pos, this.withAge(i).withProperty(HALF, EnumBlockHalf.UPPER), 2);
      } else {
         worldIn.setBlockState(pos, this.withAge(i), 2);
         worldIn.setBlockState(pos.up(), this.withAge(i).withProperty(HALF, EnumBlockHalf.UPPER), 2);
      }
   }

   public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
      worldIn.setBlockState(pos.up(), this.getDefaultState().withProperty(HALF, EnumBlockHalf.UPPER), 2);
   }

   @Override
   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      this.checkAndDropBlock(worldIn, pos, state);
      if (state.getValue(HALF) != EnumBlockHalf.UPPER) {
         if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
            int i = this.getAge(state);
            if (i < this.getMaxAge()) {
               float growthChance = getGrowthChance(this, worldIn, pos);
               if (growthChance > 0.0F && ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((int)(25.0F / growthChance)) == 0)) {
                  worldIn.setBlockState(pos, this.withAge(i + 1), 2);
                  worldIn.setBlockState(pos.up(), this.withAge(i + 1).withProperty(HALF, EnumBlockHalf.UPPER), 2);
                  ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
               }
            }
         }
      }
   }
}
