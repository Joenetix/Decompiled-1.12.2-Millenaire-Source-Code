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

public class BlockDecorativeWood extends BlockSlab implements IMetaBlockName {
   static final PropertyEnum<BlockDecorativeWood.EnumType> VARIANT = PropertyEnum.create("variant", BlockDecorativeWood.EnumType.class);

   public BlockDecorativeWood(String blockName) {
      super(Material.WOOD);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setHarvestLevel("axe", 0);
      this.setHardness(2.0F);
      this.setResistance(5.0F);
      this.setSoundType(SoundType.WOOD);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockDecorativeWood.EnumType.TIMBERFRAMEPLAIN));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public int damageDropped(IBlockState state) {
      return ((BlockDecorativeWood.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return ((BlockDecorativeWood.EnumType)state.getValue(VARIANT)).getMapColor();
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockDecorativeWood.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire." + ((BlockDecorativeWood.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(VARIANT)).getName();
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.getStateFromMeta(meta);
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, BlockDecorativeWood.EnumType.byMetadata(meta));
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (BlockDecorativeWood.EnumType enumtype : BlockDecorativeWood.EnumType.values()) {
         items.add(new ItemStack(this, 1, enumtype.getMetadata()));
      }
   }

   public Comparable<?> getTypeForItem(ItemStack stack) {
      return BlockDecorativeWood.EnumType.byMetadata(stack.getMetadata() & 7);
   }

   public String getTranslationKey(int meta) {
      return "tile.millenaire." + ((BlockDecorativeWood.EnumType)this.getStateFromMeta(meta).getValue(VARIANT)).getName();
   }

   public IProperty<?> getVariantProperty() {
      return VARIANT;
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockDecorativeWood.EnumType enumtype : BlockDecorativeWood.EnumType.values()) {
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
      TIMBERFRAMEPLAIN(0, "timberframeplain", MapColor.BROWN, true),
      TIMBERFRAMECROSS(1, "timberframecross", MapColor.BROWN, false),
      THATCH(2, "thatch", MapColor.YELLOW, true),
      BEEHIVE(3, "beehive", MapColor.YELLOW, false);

      private static final BlockDecorativeWood.EnumType[] META_LOOKUP = new BlockDecorativeWood.EnumType[values().length];
      private final int meta;
      private final String name;
      private final MapColor mapColor;
      private final boolean hasSlab;

      public static BlockDecorativeWood.EnumType byMetadata(int meta) {
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
         BlockDecorativeWood.EnumType[] var0 = values();

         for (BlockDecorativeWood.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
