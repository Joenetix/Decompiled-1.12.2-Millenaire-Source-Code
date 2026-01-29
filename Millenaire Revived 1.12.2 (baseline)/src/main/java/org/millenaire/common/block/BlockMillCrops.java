package org.millenaire.common.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.WorldUtilities;

public class BlockMillCrops extends BlockBush implements IGrowable {
   public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
   private static final AxisAlignedBB[] CROPS_AABB = new AxisAlignedBB[]{
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.125, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.25, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.375, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.625, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.75, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.875, 1.0),
      new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
   };
   private final boolean requireIrrigation;
   private final boolean slowGrowth;
   private final ResourceLocation seed;

   protected static float getGrowthChance(BlockMillCrops blockIn, World worldIn, BlockPos pos) {
      int irrigation = WorldUtilities.getBlockMeta(worldIn, new Point(pos).getBelow());
      if (blockIn.requireIrrigation && irrigation == 0) {
         return 0.0F;
      } else {
         return !blockIn.slowGrowth ? 8.0F : 4.0F;
      }
   }

   public BlockMillCrops(String cropName, boolean requireIrrigation, boolean slowGrowth, ResourceLocation seed) {
      this.requireIrrigation = requireIrrigation;
      this.slowGrowth = slowGrowth;
      this.setTickRandomly(true);
      this.setCreativeTab((CreativeTabs)null);
      this.setHardness(0.0F);
      this.setSoundType(SoundType.PLANT);
      this.seed = seed;
      this.setTranslationKey("millenaire." + cropName);
      this.setRegistryName(cropName);
      this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
   }

   public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
      IBlockState soil = worldIn.getBlockState(pos.down());
      return (worldIn.getLight(pos) >= 8 || worldIn.canSeeSky(pos))
         && soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this);
   }

   public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
      return !this.isMaxAge(state);
   }

   protected boolean canSustainBush(IBlockState state) {
      return state.getBlock() == Blocks.FARMLAND;
   }

   public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      return true;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{AGE});
   }

   protected int getAge(IBlockState state) {
      return (Integer)state.getValue(this.getAgeProperty());
   }

   protected PropertyInteger getAgeProperty() {
      return AGE;
   }

   protected int getBonemealAgeIncrease(World worldIn) {
      return MathHelper.getInt(worldIn.rand, 2, 5);
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return CROPS_AABB[state.getValue(this.getAgeProperty())];
   }

   public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      super.getDrops(drops, world, pos, state, 0);
      int age = this.getAge(state);
      Random rand = world instanceof World ? ((World)world).rand : new Random();
      if (age >= this.getMaxAge()) {
         for (int i = 0; i < 3 + fortune; i++) {
            if (rand.nextInt(2 * this.getMaxAge()) <= age) {
               drops.add(new ItemStack(this.getSeed(), 1, 0));
            }
         }
      }
   }

   public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
      return new ItemStack(this.getSeed());
   }

   public Item getItemDropped(IBlockState state, Random rand, int fortune) {
      return Item.getByNameOrId(this.seed.toString());
   }

   public int getMaxAge() {
      return 7;
   }

   public int getMetaFromState(IBlockState state) {
      return this.getAge(state);
   }

   protected Item getSeed() {
      return Item.getByNameOrId(this.seed.toString());
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.withAge(meta);
   }

   public void grow(World worldIn, BlockPos pos, IBlockState state) {
      int i = this.getAge(state) + this.getBonemealAgeIncrease(worldIn);
      int j = this.getMaxAge();
      if (i > j) {
         i = j;
      }

      worldIn.setBlockState(pos, this.withAge(i), 2);
   }

   public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
      this.grow(worldIn, pos, state);
   }

   public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
      super.harvestBlock(worldIn, player, pos, state, te, stack);
      BlockItemUtilities.checkForHarvestTheft(player, pos);
   }

   public boolean isMaxAge(IBlockState state) {
      return (Integer)state.getValue(this.getAgeProperty()) >= this.getMaxAge();
   }

   public int quantityDropped(Random par1Random) {
      return 1;
   }

   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      super.updateTick(worldIn, pos, state, rand);
      if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
         int i = this.getAge(state);
         if (i < this.getMaxAge()) {
            float growthChance = getGrowthChance(this, worldIn, pos);
            if (growthChance > 0.0F && ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((int)(25.0F / growthChance)) == 0)) {
               worldIn.setBlockState(pos, this.withAge(i + 1), 2);
               ForgeHooks.onCropsGrowPost(worldIn, pos, state, worldIn.getBlockState(pos));
            }
         }
      }
   }

   public IBlockState withAge(int age) {
      return this.getDefaultState().withProperty(this.getAgeProperty(), age);
   }
}
