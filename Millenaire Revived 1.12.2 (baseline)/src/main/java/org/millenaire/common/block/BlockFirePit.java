package org.millenaire.common.block;

import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.millenaire.common.entity.TileEntityFirePit;
import org.millenaire.common.forge.Mill;

public class BlockFirePit extends BlockContainer {
   public static final PropertyBool LIT = PropertyBool.create("lit");
   public static final PropertyEnum<BlockFirePit.EnumAlignment> ALIGNMENT = PropertyEnum.create("alignment", BlockFirePit.EnumAlignment.class);
   public static final AxisAlignedBB FIRE_PIT_BOX = new AxisAlignedBB(0.1875, 0.0, 0.1875, 0.8125, 0.5, 0.8125);
   private ItemStack stack;

   public BlockFirePit(String name) {
      super(Material.WOOD);
      this.setSoundType(SoundType.WOOD);
      this.setHardness(0.2F);
      this.setDefaultState(this.getDefaultState().withProperty(LIT, false));
      this.setRegistryName(name);
      this.setTranslationKey("millenaire." + name);
      this.setCreativeTab(MillBlocks.tabMillenaire);
   }

   public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
      TileEntity te = worldIn.getTileEntity(pos);
      if (te instanceof TileEntityFirePit) {
         ((TileEntityFirePit)te).dropAll();
      }

      super.breakBlock(worldIn, pos, state);
   }

   @Nonnull
   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{LIT, ALIGNMENT});
   }

   @Nullable
   public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
      return new TileEntityFirePit();
   }

   @Nonnull
   public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
      return BlockFaceShape.UNDEFINED;
   }

   @Nonnull
   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getRenderLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   @Nonnull
   public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
      return FIRE_PIT_BOX;
   }

   @Nullable
   public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
      return NULL_AABB;
   }

   public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
      return state.getValue(LIT) ? 15 : 0;
   }

   public int getMetaFromState(IBlockState state) {
      return (state.getValue(LIT) ? 1 : 0) | ((BlockFirePit.EnumAlignment)state.getValue(ALIGNMENT)).meta << 1;
   }

   public String getName() {
      if (this.stack == null) {
         this.stack = new ItemStack(this);
      }

      return this.stack.getDisplayName();
   }

   @Nonnull
   @SideOnly(Side.CLIENT)
   public EnumBlockRenderType getRenderType(IBlockState state) {
      return EnumBlockRenderType.MODEL;
   }

   @Nonnull
   public IBlockState getStateForPlacement(
      @Nonnull World world,
      @Nonnull BlockPos pos,
      @Nonnull EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ,
      int meta,
      EntityLivingBase placer,
      EnumHand hand
   ) {
      return this.getDefaultState().withProperty(ALIGNMENT, BlockFirePit.EnumAlignment.fromAxis(placer.getHorizontalFacing().getAxis()));
   }

   @Nonnull
   public IBlockState getStateFromMeta(int meta) {
      boolean lit = (meta & 1) != 0;
      BlockFirePit.EnumAlignment alignment = BlockFirePit.EnumAlignment.fromMeta(meta & 2);
      return this.getDefaultState().withProperty(LIT, lit).withProperty(ALIGNMENT, alignment);
   }

   @SideOnly(Side.CLIENT)
   public void initModel() {
      ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(this.getRegistryName(), "inventory"));
   }

   public boolean isFullCube(IBlockState state) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState state) {
      return false;
   }

   public boolean onBlockActivated(
      World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
   ) {
      if (!worldIn.isRemote) {
         playerIn.openGui(Mill.instance, 16, worldIn, pos.getX(), pos.getY(), pos.getZ());
      }

      return true;
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
      if ((Boolean)stateIn.getValue(LIT)) {
         worldIn.playSound(
            pos.getX() + 0.5,
            pos.getY() + this.getBoundingBox(stateIn, worldIn, pos).minY,
            pos.getZ() + 0.5,
            SoundEvents.BLOCK_FIRE_AMBIENT,
            SoundCategory.BLOCKS,
            1.0F + rand.nextFloat(),
            rand.nextFloat() * 0.7F + 0.3F,
            false
         );
         if (rand.nextInt(24) == 0) {
            for (int i = 0; i < 3; i++) {
               double x = pos.getX() + rand.nextDouble();
               double y = pos.getY() + rand.nextDouble() * 0.5 + 0.5;
               double z = pos.getZ() + rand.nextDouble();
               worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0.0, 0.0, 0.0, new int[0]);
            }
         }
      }
   }

   public static enum EnumAlignment implements IStringSerializable {
      X("x", 0, 90.0),
      Z("z", 1, 0.0);

      private final String name;
      private final int meta;
      public final double angle;

      public static BlockFirePit.EnumAlignment fromAxis(Axis axis) {
         if (axis == Axis.X) {
            return Z;
         } else if (axis == Axis.Z) {
            return X;
         } else {
            throw new UnsupportedOperationException("Y isn't horizontal!");
         }
      }

      public static BlockFirePit.EnumAlignment fromMeta(int flag) {
         return flag != 0 ? X : Z;
      }

      private EnumAlignment(String name, int meta, double angle) {
         this.name = name;
         this.meta = meta;
         this.angle = angle;
      }

      public int getMeta() {
         return this.meta;
      }

      @Nonnull
      public String getName() {
         return this.name;
      }
   }
}
