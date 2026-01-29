package org.millenaire.common.world;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockLog.EnumAxis;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.utilities.MillCommonUtilities;

public class WorldGenAppleTree extends WorldGenAbstractTree {
   private static final int MIN_TREE_HEIGHT = 5;
   private static final IBlockState WOOD_BS = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, EnumType.OAK);
   private static final IBlockState LEAVES_BS = MillBlocks.LEAVES_APPLETREE.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false);

   public WorldGenAppleTree(boolean notify) {
      super(notify);
   }

   public boolean generate(World worldIn, Random rand, BlockPos position) {
      int treeHeight = rand.nextInt(2) + 5;
      boolean obstacleMet = true;
      if (position.getY() >= 1 && position.getY() + treeHeight + 1 <= worldIn.getHeight()) {
         for (int j = position.getY(); j <= position.getY() + 1 + treeHeight; j++) {
            int k = 1;
            if (j == position.getY()) {
               k = 0;
            }

            if (j >= position.getY() + 1 + treeHeight - 2) {
               k = 2;
            }

            MutableBlockPos blockpos$mutableblockpos = new MutableBlockPos();

            for (int l = position.getX() - k; l <= position.getX() + k && obstacleMet; l++) {
               for (int i1 = position.getZ() - k; i1 <= position.getZ() + k && obstacleMet; i1++) {
                  if (j < 0 || j >= worldIn.getHeight()) {
                     obstacleMet = false;
                  } else if (!this.isReplaceable(worldIn, blockpos$mutableblockpos.setPos(l, j, i1))) {
                     obstacleMet = false;
                  }
               }
            }
         }

         if (!obstacleMet) {
            return false;
         } else {
            IBlockState state = worldIn.getBlockState(position.down());
            if (state.getBlock().canSustainPlant(state, worldIn, position.down(), EnumFacing.UP, (BlockSapling)Blocks.SAPLING)
               && position.getY() < worldIn.getHeight() - treeHeight - 1) {
               state.getBlock().onPlantGrow(state, worldIn, position.down(), position);

               for (int yPos = 0; yPos < 5; yPos++) {
                  BlockPos upN = position.up(yPos);
                  state = worldIn.getBlockState(upN);
                  if (state.getBlock().isAir(state, worldIn, upN)
                     || state.getBlock().isLeaves(state, worldIn, upN)
                     || state.getMaterial() == Material.VINE) {
                     this.setBlockAndNotifyAdequately(worldIn, position.up(yPos), WOOD_BS);
                  }
               }

               Set<EnumFacing> branchFacings = new HashSet<>();
               branchFacings.add(Plane.HORIZONTAL.random(rand));

               for (int i = 0; i < 3; i++) {
                  branchFacings.add(Plane.HORIZONTAL.random(rand));
               }

               for (EnumFacing enumfacing : Plane.HORIZONTAL) {
                  int branchStartY = 3 + rand.nextInt(1);
                  int horizontalSize = 3 - rand.nextInt(2);
                  int xPos = position.getX();
                  int zPos = position.getZ();
                  int yPosx = position.getY() + branchStartY;
                  int curve;
                  if (Math.random() < 0.5) {
                     curve = 1;
                  } else {
                     curve = -1;
                  }

                  for (int hPos = 0; hPos < horizontalSize; hPos++) {
                     if (yPosx < position.getY() + treeHeight && Math.random() < 0.7) {
                        yPosx++;
                     }

                     if (enumfacing.getXOffset() != 0) {
                        xPos += enumfacing.getXOffset();
                        if (Math.random() < 0.15) {
                           zPos += curve;
                        }
                     } else {
                        zPos += enumfacing.getZOffset();
                        if (Math.random() < 0.15) {
                           xPos += curve;
                        }
                     }

                     BlockPos blockpos = new BlockPos(xPos, yPosx, zPos);
                     state = worldIn.getBlockState(blockpos);
                     if (state.getBlock().isAir(state, worldIn, blockpos) || state.getBlock().isLeaves(state, worldIn, blockpos)) {
                        this.setBlockAndNotifyAdequately(
                           worldIn, blockpos, WOOD_BS.withProperty(BlockLog.LOG_AXIS, EnumAxis.fromFacingAxis(enumfacing.getAxis()))
                        );

                        for (int dx = -1; dx < 2; dx++) {
                           for (int dz = -1; dz < 2; dz++) {
                              for (int dy = -1; dy < 2; dy++) {
                                 BlockPos leavePos = blockpos.add(dx, dy, dz);
                                 state = worldIn.getBlockState(leavePos);
                                 if (state.getBlock().isAir(state, worldIn, leavePos) && MillCommonUtilities.randomInt(100) < 50) {
                                    this.setBlockAndNotifyAdequately(worldIn, leavePos, LEAVES_BS);
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               return true;
            } else {
               return false;
            }
         }
      } else {
         return false;
      }
   }
}
