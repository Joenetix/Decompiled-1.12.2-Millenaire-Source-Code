package org.millenaire.common.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMillStatue extends BlockDirectional {
   private static final AxisAlignedBB CARVING_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

   public BlockMillStatue(String blockName, SoundType sound, Material material) {
      super(material);
      this.setCreativeTab(MillBlocks.tabMillenaire);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
      this.setHarvestLevel("pickaxe", 0);
      this.setHardness(0.5F);
      this.setResistance(2.0F);
      this.setSoundType(sound);
      this.setTranslationKey("millenaire." + blockName);
      this.setRegistryName(blockName);
      this.setLightOpacity(0);
   }

   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return CARVING_AABB;
   }

   public void addCollisionBoxToList(
      IBlockState state,
      World worldIn,
      BlockPos pos,
      AxisAlignedBB entityBox,
      List<AxisAlignedBB> collidingBoxes,
      @Nullable Entity entityIn,
      boolean isActualState
   ) {
      addCollisionBoxToList(pos, entityBox, collidingBoxes, state.getBoundingBox(worldIn, pos));
   }

   public IBlockState getStateForPlacement(
      World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand
   ) {
      EnumFacing f = EnumFacing.getDirectionFromEntityLiving(pos, placer);
      return f != EnumFacing.DOWN && f != EnumFacing.UP
         ? this.getDefaultState().withProperty(FACING, f)
         : this.getDefaultState().withProperty(FACING, EnumFacing.SOUTH);
   }

   @Nullable
   public static EnumFacing getFacing(int meta) {
      int i = meta & 7;
      return i > 5 ? null : EnumFacing.byIndex(i);
   }

   public IBlockState getStateFromMeta(int meta) {
      return this.getDefaultState().withProperty(FACING, getFacing(meta));
   }

   public int getMetaFromState(IBlockState state) {
      int i = 0;
      return i | ((EnumFacing)state.getValue(FACING)).getIndex();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "facing=down"));
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }
}
