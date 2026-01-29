package org.millenaire.common.block;

import net.minecraft.block.BlockBeacon;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.item.IMetaBlockName;

public class BlockMillStainedGlass extends BlockPane implements IMetaBlockName {
   public static final PropertyEnum<BlockMillStainedGlass.EnumType> VARIANT = PropertyEnum.create("variant", BlockMillStainedGlass.EnumType.class);
   private final String blockName;

   public BlockMillStainedGlass(String blockName) {
      super(Material.GLASS, true);
      this.blockName = blockName;
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setSoundType(SoundType.GLASS);
      this.setDefaultState(
         this.blockState
            .getBaseState()
            .withProperty(NORTH, false)
            .withProperty(EAST, false)
            .withProperty(SOUTH, false)
            .withProperty(WEST, false)
            .withProperty(VARIANT, BlockMillStainedGlass.EnumType.WHITE)
      );
   }

   public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{NORTH, EAST, WEST, SOUTH, VARIANT});
   }

   public int damageDropped(IBlockState state) {
      return ((BlockMillStainedGlass.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.TRANSLUCENT;
   }

   public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      return MapColor.GRAY;
   }

   public int getMetaFromState(IBlockState state) {
      return ((BlockMillStainedGlass.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   @Override
   public String getSpecialName(ItemStack stack) {
      return "tile.millenaire."
         + this.blockName
         + "."
         + ((BlockMillStainedGlass.EnumType)this.getStateFromMeta(stack.getMetadata()).getValue(VARIANT)).getName();
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(VARIANT, BlockMillStainedGlass.EnumType.byMetadata(meta));
   }

   public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
      for (int i = 0; i < BlockMillStainedGlass.EnumType.values().length; i++) {
         items.add(new ItemStack(this, 1, i));
      }
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      for (BlockMillStainedGlass.EnumType enumtype : BlockMillStainedGlass.EnumType.values()) {
         ModelLoader.setCustomModelResourceLocation(
            Item.getItemFromBlock(this), enumtype.getMetadata(), new ModelResourceLocation(this.getRegistryName() + "_" + enumtype.name, "variant=inventory")
         );
      }
   }

   public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
      if (!worldIn.isRemote) {
         BlockBeacon.updateColorAsync(worldIn, pos);
      }
   }

   public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
      switch (mirrorIn) {
         case LEFT_RIGHT:
            return state.withProperty(NORTH, state.getValue(SOUTH)).withProperty(SOUTH, state.getValue(NORTH));
         case FRONT_BACK:
            return state.withProperty(EAST, state.getValue(WEST)).withProperty(WEST, state.getValue(EAST));
         default:
            return super.withMirror(state, mirrorIn);
      }
   }

   public IBlockState withRotation(IBlockState state, Rotation rot) {
      switch (rot) {
         case CLOCKWISE_180:
            return state.withProperty(NORTH, state.getValue(SOUTH))
               .withProperty(EAST, state.getValue(WEST))
               .withProperty(SOUTH, state.getValue(NORTH))
               .withProperty(WEST, state.getValue(EAST));
         case COUNTERCLOCKWISE_90:
            return state.withProperty(NORTH, state.getValue(EAST))
               .withProperty(EAST, state.getValue(SOUTH))
               .withProperty(SOUTH, state.getValue(WEST))
               .withProperty(WEST, state.getValue(NORTH));
         case CLOCKWISE_90:
            return state.withProperty(NORTH, state.getValue(WEST))
               .withProperty(EAST, state.getValue(NORTH))
               .withProperty(SOUTH, state.getValue(EAST))
               .withProperty(WEST, state.getValue(SOUTH));
         default:
            return state;
      }
   }

   public static enum EnumType implements IStringSerializable {
      WHITE(0, "white"),
      YELLOW(1, "yellow"),
      YELLOW_RED(2, "yellow_red"),
      RED_BLUE(3, "red_blue"),
      GREEN_BLUE(4, "green_blue");

      private static final BlockMillStainedGlass.EnumType[] META_LOOKUP = new BlockMillStainedGlass.EnumType[values().length];
      private final int meta;
      private final String name;

      public static BlockMillStainedGlass.EnumType byMetadata(int meta) {
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
         BlockMillStainedGlass.EnumType[] var0 = values();

         for (BlockMillStainedGlass.EnumType var3 : var0) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }
      }
   }
}
