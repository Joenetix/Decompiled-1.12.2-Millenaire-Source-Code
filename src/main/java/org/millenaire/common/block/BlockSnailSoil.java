package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public class BlockSnailSoil extends Block implements IMetaBlockName {
   public static final PropertyEnum<BlockSnailSoil.EnumType> PROGRESS = PropertyEnum.create("progress", BlockSnailSoil.EnumType.class);

   public BlockSnailSoil(String blockName) {
      super(Material.GROUND);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setTickRandomly(true);
      this.setHarvestLevel("space", 0);
      this.setDefaultState(this.blockState.getBaseState().withProperty(PROGRESS, BlockSnailSoil.EnumType.SNAIL_SOIL_EMPTY));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{PROGRESS});
   }

   public int damageDropped(IBlockState state) {
      return 0;
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockSnailSoil.EnumType)state.getValue(PROGRESS)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire." + ((BlockSnailSoil.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(PROGRESS)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(PROGRESS, BlockSnailSoil.EnumType.byMetadata(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      items.add(new ItemStack(this, 1, 0));
      items.add(new ItemStack(this, 1, 3));
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockSnailSoil.EnumType enumtype : BlockSnailSoil.EnumType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "progress=" + enumtype.getName())
         );
      }
   }

   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      int currentValue = ((BlockSnailSoil.EnumType)state.getValue(PROGRESS)).getMetadata();
      if (currentValue < 3) {
         boolean waterAbove = worldIn.getBlockState(pos.up()).getBlock() == Blocks.WATER;
         boolean airAboveWater = !worldIn.getBlockState(pos.up().up()).causesSuffocation();
         if (waterAbove && airAboveWater && rand.nextInt(2) == 0) {
            IBlockState newState = state.withProperty(PROGRESS, BlockSnailSoil.EnumType.byMetadata(++currentValue));
            worldIn.setBlockState(pos, newState);
         }
      }
   }

   public static enum EnumType implements IStringSerializable {
      SNAIL_SOIL_EMPTY(0, "snail_soil_empty"),
      SNAIL_SOIL_IP1(1, "snail_soil_ip1"),
      SNAIL_SOIL_IP2(2, "snail_soil_ip2"),
      SNAIL_SOIL_FULL(3, "snail_soil_full");

      private static final BlockSnailSoil.EnumType[] META_LOOKUP = new BlockSnailSoil.EnumType[values().length];
      private final int meta;
      private final String name;

      public static BlockSnailSoil.EnumType byMetadata(int meta) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      private EnumType(int meta, String name) {
         this.meta = meta;
         this.name = name;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.name;
      }

      @Override
      public String toString() {
         return this.name;
      }

      static {
         BlockSnailSoil.EnumType[] var0 = values();

         for (BlockSnailSoil.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
