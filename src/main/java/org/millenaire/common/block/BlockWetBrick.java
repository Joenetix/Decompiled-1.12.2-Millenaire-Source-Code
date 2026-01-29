package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
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
import org.millenaire.common.utilities.MillCommonUtilities;

public class BlockWetBrick extends Block implements IMetaBlockName {
   static final PropertyEnum<BlockWetBrick.EnumType> PROGRESS = PropertyEnum.create("progress", BlockWetBrick.EnumType.class);

   public BlockWetBrick(String blockName) {
      super(Material.GROUND);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setTickRandomly(true);
      this.setHarvestLevel("shovel", 0);
      this.setHardness(0.8F);
      this.setSoundType(SoundType.GROUND);
      this.setDefaultState(this.blockState.getBaseState().withProperty(PROGRESS, BlockWetBrick.EnumType.WETBRICK0));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{PROGRESS});
   }

   public int damageDropped(IBlockState state) {
      return 0;
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockWetBrick.EnumType)state.getValue(PROGRESS)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire." + ((BlockWetBrick.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(PROGRESS)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(PROGRESS, BlockWetBrick.EnumType.byMetadata(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      items.add(new ItemStack(this, 1, 0));
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockWetBrick.EnumType enumtype : BlockWetBrick.EnumType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "progress=" + enumtype.getName())
         );
      }
   }

   public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
      int currentValue = ((BlockWetBrick.EnumType)state.getValue(PROGRESS)).getMetadata();
      if (worldIn.getLightFromNeighbors(pos.up()) > 14) {
         if (++currentValue < 2 && MillCommonUtilities.chanceOn(2)) {
            IBlockState newState = state.withProperty(PROGRESS, BlockWetBrick.EnumType.byMetadata(++currentValue));
            worldIn.setBlockState(pos, newState);
         } else if (currentValue < 3) {
            IBlockState newState = state.withProperty(PROGRESS, BlockWetBrick.EnumType.byMetadata(currentValue));
            worldIn.setBlockState(pos, newState);
         } else {
            worldIn.setBlockState(
               pos, MillBlocks.STONE_DECORATION.getDefaultState().withProperty(BlockDecorativeStone.VARIANT, BlockDecorativeStone.EnumType.MUDBRICK)
            );
         }
      }
   }

   public static enum EnumType implements IStringSerializable {
      WETBRICK0(0, "wetbrick0"),
      WETBRICK1(1, "wetbrick1"),
      WETBRICK2(2, "wetbrick2");

      private static final BlockWetBrick.EnumType[] META_LOOKUP = new BlockWetBrick.EnumType[values().length];
      private final int meta;
      private final String name;

      public static BlockWetBrick.EnumType byMetadata(int meta) {
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
         BlockWetBrick.EnumType[] var0 = values();

         for (BlockWetBrick.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
