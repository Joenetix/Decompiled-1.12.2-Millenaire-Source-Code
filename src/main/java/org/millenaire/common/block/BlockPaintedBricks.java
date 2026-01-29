package org.millenaire.common.block;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.advancements.MillAdvancements;
import org.millenaire.common.item.ItemPaintBucket;
import org.millenaire.common.utilities.Point;

public class BlockPaintedBricks extends Block implements IPaintedBlock {
   public static final PropertyBool TOP_FRIEZE = PropertyBool.create("top_frieze");
   public static final PropertyBool BOTTOM_FRIEZE = PropertyBool.create("bottom_frieze");
   private final String blockName;
   private final String baseBlockName;
   private final EnumDyeColor colour;

   public static IBlockState getBlockStateWithColour(IBlockState input, EnumDyeColor colour) {
      IPaintedBlock paintedBlock = (IPaintedBlock)input.getBlock();
      Block newBlock = MillBlocks.PAINTED_BRICK_MAP.get(paintedBlock.getBlockType()).get(colour);
      return newBlock.getStateFromMeta(input.getBlock().getMetaFromState(input));
   }

   public static String getColorName(EnumDyeColor colour) {
      String colourName = colour.getTranslationKey();
      if (colourName.equalsIgnoreCase("lightBlue")) {
         colourName = "light_blue";
      }

      return colourName;
   }

   public static EnumDyeColor getColourFromBlockState(IBlockState bs) {
      return bs.getBlock() instanceof IPaintedBlock ? ((IPaintedBlock)bs.getBlock()).getDyeColour() : null;
   }

   public BlockPaintedBricks(String baseBlockName, EnumDyeColor colour) {
      super(Material.ROCK);
      this.baseBlockName = baseBlockName;
      String colourName = getColorName(colour);
      this.blockName = baseBlockName + "_" + colourName;
      this.colour = colour;
      this.setTranslationKey("millenaire." + this.blockName);
      this.setRegistryName(this.blockName);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setHarvestLevel("pickaxe", 0);
      this.setHardness(1.5F);
      this.setResistance(10.0F);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{TOP_FRIEZE, BOTTOM_FRIEZE});
   }

   public int friezePriority(IBlockAccess worldIn, BlockPos pos, IBlockState ourState, IBlockState otherState, EnumFacing side) {
      if (getColourFromBlockState(otherState) == this.colour) {
         return otherState.isSideSolid(worldIn, pos, side) ? 0 : 3;
      } else if (otherState.getCollisionBoundingBox(worldIn, pos) == NULL_AABB) {
         return 5;
      } else if (otherState.getBlock() instanceof BlockPane) {
         return 2;
      } else {
         return otherState.getBlock() instanceof IPaintedBlock ? 1 : 10;
      }
   }

   public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
      int topPriority = this.friezePriority(worldIn, pos.up(), state, worldIn.getBlockState(pos.up()), EnumFacing.DOWN);
      int bottomPriority = this.friezePriority(worldIn, pos.down(), state, worldIn.getBlockState(pos.down()), EnumFacing.UP);
      if (topPriority > bottomPriority) {
         return state.withProperty(TOP_FRIEZE, true).withProperty(BOTTOM_FRIEZE, false);
      } else {
         return bottomPriority > 0
            ? state.withProperty(BOTTOM_FRIEZE, true).withProperty(TOP_FRIEZE, false)
            : state.withProperty(BOTTOM_FRIEZE, false).withProperty(TOP_FRIEZE, false);
      }
   }

   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.CUTOUT_MIPPED;
   }

   @Override
   public String getBlockType() {
      return this.baseBlockName;
   }

   @Override
   public EnumDyeColor getDyeColour() {
      return this.colour;
   }

   public int getMetaFromState(IBlockState state) {
      return 0;
   }

   public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
      return this.getStateFromMeta(meta);
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState();
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "up=true,down=true"));
   }

   public boolean onBlockActivated(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      if (player.inventory.getCurrentItem() != ItemStack.EMPTY && player.inventory.getCurrentItem().getItem() instanceof ItemPaintBucket) {
         ItemStack bucket = player.inventory.getCurrentItem();
         EnumDyeColor targetColor = ((ItemPaintBucket)player.inventory.getCurrentItem().getItem()).getColour();
         if (targetColor != null && this.colour != targetColor) {
            List<Point> pointsToTest = new ArrayList<>();
            pointsToTest.add(new Point(pos));
            int blockColoured = 0;

            while (!pointsToTest.isEmpty()) {
               Point p = pointsToTest.get(pointsToTest.size() - 1);
               IBlockState bs = p.getBlockActualState(worldIn);
               if (getColourFromBlockState(bs) == this.colour) {
                  p.setBlockState(worldIn, getBlockStateWithColour(bs, targetColor));
                  blockColoured++;
                  pointsToTest.add(p.getAbove());
                  pointsToTest.add(p.getBelow());
                  pointsToTest.add(p.getNorth());
                  pointsToTest.add(p.getEast());
                  pointsToTest.add(p.getSouth());
                  pointsToTest.add(p.getWest());
               }

               pointsToTest.remove(p);
            }

            if (blockColoured < bucket.getMaxDamage() - bucket.getItemDamage()) {
               bucket.damageItem(blockColoured, player);
            } else {
               player.inventory.removeStackFromSlot(player.inventory.currentItem);
               player.inventory.add(player.inventory.currentItem, new ItemStack(Items.BUCKET));
            }

            MillAdvancements.RAINBOW.grant(player);
            return true;
         }
      }

      return false;
   }
}
