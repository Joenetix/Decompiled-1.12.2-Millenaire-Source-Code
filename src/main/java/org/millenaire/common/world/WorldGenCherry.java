package org.millenaire.common.world;

import java.util.Random;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockSapling;
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

public class WorldGenCherry extends WorldGenAbstractTree {
   private final int minTreeHeight = 6;
   private final IBlockState metaWood = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, EnumType.SPRUCE);
   private final IBlockState metaLeaves = MillBlocks.CHERRY_LEAVES.getDefaultState().withProperty(BlockLeaves.CHECK_DECAY, false);

   public WorldGenCherry(boolean notify) {
      super(notify);
   }

   public boolean generate(World worldIn, Random rand, BlockPos position) {
      int treeHeight = rand.nextInt(2) + this.minTreeHeight;
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

               for (int yPos = position.getY() + 2; yPos <= position.getY() + treeHeight + 1; yPos++) {
                  int leavesRadius = 3;
                  if (yPos < position.getY() + 4) {
                     leavesRadius -= position.getY() + 4 - yPos;
                  } else if (yPos > position.getY() + treeHeight - 2) {
                     leavesRadius -= yPos - (position.getY() + treeHeight - 2);
                  }

                  for (int xPos = position.getX() - leavesRadius; xPos <= position.getX() + leavesRadius; xPos++) {
                     int distanceFromTrunkX = xPos - position.getX();

                     for (int zPos = position.getZ() - leavesRadius; zPos <= position.getZ() + leavesRadius; zPos++) {
                        int distanceFromTrunkZ = zPos - position.getZ();
                        int chanceOn100 = 95;
                        if (Math.abs(distanceFromTrunkX) == leavesRadius && Math.abs(distanceFromTrunkZ) == leavesRadius) {
                           chanceOn100 = 0;
                        } else if (Math.abs(distanceFromTrunkX) == leavesRadius || Math.abs(distanceFromTrunkZ) == leavesRadius) {
                           chanceOn100 = 80;
                        }

                        if (MillCommonUtilities.randomInt(100) < chanceOn100) {
                           BlockPos blockpos = new BlockPos(xPos, yPos, zPos);
                           state = worldIn.getBlockState(blockpos);
                           if (state.getBlock().isAir(state, worldIn, blockpos)
                              || state.getBlock().isLeaves(state, worldIn, blockpos)
                              || state.getMaterial() == Material.VINE) {
                              this.setBlockAndNotifyAdequately(worldIn, blockpos, this.metaLeaves);
                           }
                        }
                     }
                  }
               }

               for (int j3 = 0; j3 < treeHeight; j3++) {
                  BlockPos upN = position.up(j3);
                  state = worldIn.getBlockState(upN);
                  if (state.getBlock().isAir(state, worldIn, upN)
                     || state.getBlock().isLeaves(state, worldIn, upN)
                     || state.getMaterial() == Material.VINE) {
                     this.setBlockAndNotifyAdequately(worldIn, position.up(j3), this.metaWood);
                  }
               }

               for (EnumFacing enumfacing : Plane.HORIZONTAL.facings()) {
                  if (MillCommonUtilities.randomInt(100) < 60) {
                     int branchMaxY = treeHeight - rand.nextInt(2);
                     int branchMinY = 3 + rand.nextInt(2);
                     int horizontalOffset = 2 - rand.nextInt(2);
                     int xPos = position.getX();
                     int zPos = position.getZ();

                     for (int yPos = 0; yPos < branchMaxY; yPos++) {
                        int i2 = position.getY() + yPos;
                        if (yPos >= branchMinY && horizontalOffset > 0) {
                           xPos += enumfacing.getXOffset();
                           zPos += enumfacing.getZOffset();
                           horizontalOffset--;
                        }

                        BlockPos blockpos = new BlockPos(xPos, i2, zPos);
                        state = worldIn.getBlockState(blockpos);
                        if (state.getBlock().isAir(state, worldIn, blockpos) || state.getBlock().isLeaves(state, worldIn, blockpos)) {
                           this.setBlockAndNotifyAdequately(worldIn, blockpos, this.metaWood);
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
