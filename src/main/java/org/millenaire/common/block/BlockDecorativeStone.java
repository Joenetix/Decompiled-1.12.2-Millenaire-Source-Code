package org.millenaire.common.block;

import java.util.Random;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public class BlockDecorativeStone extends BlockSlab implements IMetaBlockName {
   public static final PropertyEnum<BlockDecorativeStone.EnumType> VARIANT = PropertyEnum.create("variant", BlockDecorativeStone.EnumType.class);

   public BlockDecorativeStone(String blockName) {
      super(Material.ROCK);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setHarvestLevel("pickaxe", 0);
      this.setHardness(1.5F);
      this.setResistance(10.0F);
      this.setSoundType(SoundType.STONE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockDecorativeStone.EnumType.MUDBRICK));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public int damageDropped(IBlockState state) {
      return ((BlockDecorativeStone.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return ((BlockDecorativeStone.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockDecorativeStone.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire." + ((BlockDecorativeStone.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(VARIANT)).getName();
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.getStateFromMeta(meta);
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, BlockDecorativeStone.EnumType.byMetadata(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (BlockDecorativeStone.EnumType enumtype : BlockDecorativeStone.EnumType.values()) {
         if (enumtype != BlockDecorativeStone.EnumType.COOKEDBRICK) {
            items.add(new ItemStack(this, 1, enumtype.getMetadata()));
         }
      }
   }

   public Comparable<?> getTypeForItem(ItemStack stack) {
      return BlockDecorativeStone.EnumType.byMetadata(stack.getMetadata() & 7);
   }

   public String getTranslationKey(int meta) {
      return "tile.millenaire." + ((BlockDecorativeStone.EnumType)this.getStateFromMeta(meta).getValue(VARIANT)).getName();
   }

   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockDecorativeStone.EnumType enumtype : BlockDecorativeStone.EnumType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName(), "variant=" + enumtype.getName())
         );
      }
   }

   public boolean isDouble() {
      return true;
   }

   public int quantityDropped(Random random) {
      return 1;
   }

   public static enum EnumType implements IStringSerializable {
      MUDBRICK(0, "mudbrick", MapColor.BROWN, true),
      COOKEDBRICK(1, "cookedbrick", MapColor.WHITE_STAINED_HARDENED_CLAY, true),
      MAYANGOLDBLOCK(2, "mayangoldblock", MapColor.GOLD, false),
      BYZANTINEMOSAICRED(3, "byzantine_mosaic_red", MapColor.RED, false),
      BYZANTINEMOSAICBLUE(4, "byzantine_mosaic_blue", MapColor.BLUE, false),
      LIGHTBLUEBRICK_BLOCK(5, "lightbluebrick_block", MapColor.BLUE, false),
      LIGHTBLUECHISELED_BLOCK(6, "lightbluechiseled_block", MapColor.BLUE, false);

      private static final BlockDecorativeStone.EnumType[] META_LOOKUP = new BlockDecorativeStone.EnumType[values().length];
      private final int meta;
      private final String name;
      private final MapColor mapColor;
      private final boolean hasSlab;

      public static BlockDecorativeStone.EnumType byMetadata(int meta) {
         if (meta < 0 || meta >= META_LOOKUP.length) {
            meta = 0;
         }

         return META_LOOKUP[meta];
      }

      private EnumType(int meta, String name, MapColor mapColor, boolean hasSlab) {
         this.meta = meta;
         this.name = name;
         this.mapColor = mapColor;
         this.hasSlab = hasSlab;
      }

      public MapColor getMapColor() {
         return this.mapColor;
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

      public boolean hasSlab() {
         return this.hasSlab;
      }

      @Override
      public String toString() {
         return this.name;
      }

      static {
         BlockDecorativeStone.EnumType[] var0 = values();

         for (BlockDecorativeStone.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
