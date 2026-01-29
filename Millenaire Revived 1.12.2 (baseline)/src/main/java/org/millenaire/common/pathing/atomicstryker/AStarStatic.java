package org.millenaire.common.pathing.atomicstryker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.millenaire.common.block.BlockMillWall;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.BlockStateUtilities;
import org.millenaire.common.utilities.ThreadSafeUtilities;

public class AStarStatic {
   static final int[][] candidates = new int[][]{
      {0, 0, -1, 1},
      {0, 0, 1, 1},
      {0, 1, 0, 1},
      {1, 0, 0, 1},
      {-1, 0, 0, 1},
      {1, 1, 0, 2},
      {-1, 1, 0, 2},
      {0, 1, 1, 2},
      {0, 1, -1, 2},
      {1, -1, 0, 1},
      {-1, -1, 0, 1},
      {0, -1, 1, 1},
      {0, -1, -1, 1}
   };
   static final int[][] candidates_allowdrops = new int[][]{
      {0, 0, -1, 1},
      {0, 0, 1, 1},
      {1, 0, 0, 1},
      {-1, 0, 0, 1},
      {1, 1, 0, 2},
      {-1, 1, 0, 2},
      {0, 1, 1, 2},
      {0, 1, -1, 2},
      {1, -1, 0, 1},
      {-1, -1, 0, 1},
      {0, -1, 1, 1},
      {0, -1, -1, 1},
      {1, -2, 0, 1},
      {-1, -2, 0, 1},
      {0, -2, 1, 1},
      {0, -2, -1, 1}
   };

   public static AStarNode[] getAccessNodesSorted(World world, int workerX, int workerY, int workerZ, int posX, int posY, int posZ, AStarConfig config) throws ThreadSafeUtilities.ChunkAccessException {
      ArrayList<AStarNode> resultList = new ArrayList<>();

      for (int xIter = -2; xIter <= 2; xIter++) {
         for (int zIter = -2; zIter <= 2; zIter++) {
            for (int yIter = -3; yIter <= 2; yIter++) {
               AStarNode check = new AStarNode(posX + xIter, posY + yIter, posZ + zIter, Math.abs(xIter) + Math.abs(yIter), null);
               if (isViable(world, check, 1, config)) {
                  resultList.add(check);
               }
            }
         }
      }

      Collections.sort(resultList);
      int count = 0;

      AStarNode check;
      AStarNode[] returnVal;
      for (returnVal = new AStarNode[resultList.size()]; !resultList.isEmpty() && (check = resultList.get(0)) != null; count++) {
         returnVal[count] = check;
         resultList.remove(0);
      }

      return returnVal;
   }

   public static double getDistanceBetweenCoords(int x, int y, int z, int posX, int posY, int posZ) {
      return Math.sqrt(Math.pow(x - posX, 2.0) + Math.pow(y - posY, 2.0) + Math.pow(z - posZ, 2.0));
   }

   public static double getDistanceBetweenNodes(AStarNode a, AStarNode b) {
      return Math.sqrt(Math.pow(a.x - b.x, 2.0) + Math.pow(a.y - b.y, 2.0) + Math.pow(a.z - b.z, 2.0));
   }

   public static double getEntityLandSpeed(EntityLiving entLiving) {
      return Math.sqrt(entLiving.motionX * entLiving.motionX + entLiving.motionZ * entLiving.motionZ);
   }

   public static int getIntCoordFromDoubleCoord(double input) {
      return MathHelper.floor(input);
   }

   public static boolean isLadder(World world, Block b, int x, int y, int z) {
      return b != null ? b.isLadder(b.getDefaultState(), world, new BlockPos(x, y, z), null) : false;
   }

   public static boolean isPassableBlock(World world, int ix, int iy, int iz, AStarConfig config) throws ThreadSafeUtilities.ChunkAccessException {
      IBlockState blockState = ThreadSafeUtilities.getBlockState(world, ix, iy, iz);
      Block block = blockState.getBlock();
      if (iy > 0) {
         Block blockBelow = ThreadSafeUtilities.getBlock(world, ix, iy - 1, iz);
         if (BlockItemUtilities.isFence(blockBelow)
            || blockBelow == Blocks.IRON_BARS
            || blockBelow == Blocks.NETHER_BRICK_FENCE
            || blockBelow instanceof BlockWall
            || blockBelow instanceof BlockMillWall) {
            return false;
         }
      }

      if (block == null) {
         return true;
      } else if (!config.canSwim && (block == Blocks.WATER || block == Blocks.FLOWING_WATER)) {
         return false;
      } else if (!config.canUseDoors || !BlockItemUtilities.isWoodenDoor(block) && !BlockItemUtilities.isFenceGate(block)) {
         if (config.canClearLeaves && block instanceof BlockLeaves) {
            if (block != Blocks.LEAVES && block != Blocks.LEAVES2) {
               if (!BlockStateUtilities.hasPropertyByName(blockState, "decayable")) {
                  return true;
               }

               if ((Boolean)blockState.getValue(BlockLeaves.DECAYABLE)) {
                  return true;
               }
            } else if ((Boolean)blockState.getValue(BlockLeaves.DECAYABLE)) {
               return true;
            }
         }

         return ThreadSafeUtilities.isBlockPassable(block, world, ix, iy, iz);
      } else {
         return true;
      }
   }

   public static boolean isViable(World world, AStarNode target, int yoffset, AStarConfig config) throws ThreadSafeUtilities.ChunkAccessException {
      return isViable(world, target.x, target.y, target.z, yoffset, config);
   }

   public static boolean isViable(World world, int x, int y, int z, int yoffset, AStarConfig config) throws ThreadSafeUtilities.ChunkAccessException {
      Block block = ThreadSafeUtilities.getBlock(world, x, y, z);
      Block blockBelow = ThreadSafeUtilities.getBlock(world, x, y - 1, z);
      if (block == Blocks.LADDER && isPassableBlock(world, x, y + 1, z, config)) {
         return true;
      } else if (isPassableBlock(world, x, y, z, config) && isPassableBlock(world, x, y + 1, z, config)) {
         if (blockBelow != Blocks.WATER && blockBelow != Blocks.FLOWING_WATER) {
            if (isPassableBlock(world, x, y - 1, z, config)) {
               if (!config.canSwim) {
                  return false;
               }

               if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
                  return false;
               }
            }

            if (yoffset < 0) {
               yoffset *= -1;
            }

            for (int ycheckhigher = 1; ycheckhigher <= yoffset; ycheckhigher++) {
               if (!isPassableBlock(world, x, y + yoffset, z, config)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static AS_PathEntity translateAStarPathtoPathEntity(World world, List<AStarNode> input, AStarConfig config) throws ThreadSafeUtilities.ChunkAccessException {
      if (!config.canTakeDiagonals) {
         List<AStarNode> oldInput = input;
         input = new ArrayList<>();

         for (int i = 0; i < oldInput.size() - 1; i++) {
            input.add(oldInput.get(i));
            if (oldInput.get(i).x != oldInput.get(i + 1).x && oldInput.get(i).z != oldInput.get(i + 1).z && oldInput.get(i).y == oldInput.get(i + 1).y) {
               if (!isPassableBlock(world, oldInput.get(i).x, oldInput.get(i).y - 1, oldInput.get(i + 1).z, config)
                  && isPassableBlock(world, oldInput.get(i).x, oldInput.get(i).y, oldInput.get(i + 1).z, config)
                  && isPassableBlock(world, oldInput.get(i).x, oldInput.get(i).y + 1, oldInput.get(i + 1).z, config)) {
                  AStarNode newNode = new AStarNode(oldInput.get(i).x, oldInput.get(i).y, oldInput.get(i + 1).z, 0, null);
                  input.add(newNode);
               } else {
                  AStarNode newNode = new AStarNode(oldInput.get(i + 1).x, oldInput.get(i).y, oldInput.get(i).z, 0, null);
                  input.add(newNode);
               }
            }
         }
      }

      AS_PathPoint[] points = new AS_PathPoint[input.size()];
      int ix = 0;

      for (int size = input.size(); size > 0; ix++) {
         AStarNode reading = input.get(size - 1);
         points[ix] = new AS_PathPoint(reading.x, reading.y, reading.z);
         points[ix].setIndex(ix);
         points[ix].setTotalPathDistance(ix);
         points[ix].setDistanceToNext(1.0F);
         points[ix].setDistanceToTarget(size);
         if (ix > 0) {
            points[ix].setPrevious(points[ix - 1]);
         }

         input.remove(size - 1);
         size--;
      }

      return new AS_PathEntity(points);
   }
}
