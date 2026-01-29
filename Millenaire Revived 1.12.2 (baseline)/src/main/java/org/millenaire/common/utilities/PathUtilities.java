package org.millenaire.common.utilities;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import org.millenaire.common.block.IBlockPath;
import org.millenaire.common.block.MillBlocks;
import org.millenaire.common.buildingplan.BuildingBlock;
import org.millenaire.common.pathing.atomicstryker.AStarNode;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingLocation;

public class PathUtilities {
   private static final boolean PATH_RAISE = false;
   private static final boolean PATH_DROP = true;

   private static boolean attemptPathBuild(Building th, World world, List<BuildingBlock> pathPoints, Point p, Block pathBlock, int pathMeta) {
      IBlockState blockState = p.getBlockActualState(world);
      if (th.isPointProtectedFromPathBuilding(p)) {
         return false;
      } else if (p.getRelative(0.0, 2.0, 0.0).isBlockPassable(world) && p.getAbove().isBlockPassable(world) && canPathBeBuiltHere(blockState)) {
         pathPoints.add(new BuildingBlock(p, pathBlock, pathMeta));
         return true;
      } else {
         return false;
      }
   }

   public static List<BuildingBlock> buildPath(Building th, List<AStarNode> path, Block pathBlock, int pathMeta, int pathWidth) {
      List<BuildingBlock> pathPoints = new ArrayList<>();
      boolean lastNodeHalfSlab = false;
      boolean[] pathShouldBuild = new boolean[path.size()];

      for (int ip = 0; ip < path.size(); ip++) {
         pathShouldBuild[ip] = true;
      }

      for (int ip = 0; ip < path.size(); ip++) {
         AStarNode node = path.get(ip);
         Point p = new Point(node);
         BuildingLocation l = th.getLocationAtCoordPlanar(p);
         if (l != null) {
            if (ip == 0) {
               pathShouldBuild[ip] = true;
               clearPathForward(path, pathShouldBuild, th, l, ip);
            } else if (ip == path.size() - 1) {
               pathShouldBuild[ip] = true;
               clearPathBackward(path, pathShouldBuild, th, l, ip);
            } else {
               boolean stablePath = isPointOnStablePath(p, th.world);
               if (stablePath) {
                  pathShouldBuild[ip] = true;
                  clearPathBackward(path, pathShouldBuild, th, l, ip);
                  clearPathForward(path, pathShouldBuild, th, l, ip);
               }
            }
         }
      }

      for (int ipx = 0; ipx < path.size(); ipx++) {
         if (pathShouldBuild[ipx]) {
            AStarNode node = path.get(ipx);
            AStarNode lastNode = null;
            AStarNode nextNode = null;
            if (ipx > 0) {
               lastNode = path.get(ipx - 1);
            }

            if (ipx + 1 < path.size()) {
               nextNode = path.get(ipx + 1);
            }

            boolean halfSlab = false;
            if (lastNode != null && nextNode != null) {
               Point p = new Point(node);
               Point nextp = new Point(nextNode);
               Point lastp = new Point(lastNode);
               if (!isStairsOrSlabOrChest(th.world, nextp.getBelow()) && !isStairsOrSlabOrChest(th.world, lastp.getBelow())) {
                  if ((p.x != lastp.x || p.x != nextp.x) && p.z == lastp.z && p.z != nextp.z) {
                  }

                  if (lastNode.y == nextNode.y
                     && node.y < lastNode.y
                     && p.getRelative(0.0, lastNode.y - node.y, 0.0).isBlockPassable(th.world)
                     && p.getRelative(0.0, lastNode.y - node.y + 1, 0.0).isBlockPassable(th.world)) {
                     halfSlab = true;
                  } else if (!lastNodeHalfSlab && node.y == lastNode.y && node.y > nextNode.y) {
                     halfSlab = true;
                  } else if (!lastNodeHalfSlab && node.y == nextNode.y && node.y > lastNode.y) {
                     halfSlab = true;
                  }
               } else {
                  Block block = p.getBelow().getBlock(th.world);
                  if (BlockItemUtilities.isPathSlab(block)) {
                     halfSlab = true;
                  }
               }
            }

            Point p = new Point(node).getBelow();
            Block nodePathBlock = pathBlock;
            if (BlockItemUtilities.isPath(pathBlock) && halfSlab) {
               nodePathBlock = ((IBlockPath)pathBlock).getSingleSlab();
            }

            attemptPathBuild(th, th.world, pathPoints, p, nodePathBlock, pathMeta);
            if (lastNode != null) {
               int dx = p.getiX() - lastNode.x;
               int dz = p.getiZ() - lastNode.z;
               int nbPass = 1;
               if (dx != 0 && dz != 0) {
                  nbPass = 2;
               }

               for (int i = 0; i < nbPass; i++) {
                  int direction = i == 0 ? 1 : -1;
                  Point secondPoint = null;
                  Point secondPointAlternate = null;
                  Point thirdPoint = null;
                  if (pathWidth > 1) {
                     if (dx == 0 && direction == 1) {
                        secondPoint = p.getRelative(direction, 0.0, 0.0);
                        secondPointAlternate = p.getRelative(-direction, 0.0, 0.0);
                     } else if (dz == 0 && direction == 1) {
                        secondPoint = p.getRelative(0.0, 0.0, direction);
                        secondPointAlternate = p.getRelative(0.0, 0.0, -direction);
                     } else {
                        secondPoint = p.getRelative(dx * direction, 0.0, 0.0);
                        thirdPoint = p.getRelative(0.0, 0.0, dz * direction);
                     }
                  } else if (dx != 0 && dz != 0) {
                     secondPoint = p.getRelative(dx * direction, 0.0, 0.0);
                     secondPointAlternate = p.getRelative(0.0, 0.0, dz * direction);
                  }

                  if (secondPoint != null) {
                     boolean success = attemptPathBuild(th, th.world, pathPoints, secondPoint, nodePathBlock, pathMeta);
                     if (!success && secondPointAlternate != null) {
                        attemptPathBuild(th, th.world, pathPoints, secondPointAlternate, nodePathBlock, pathMeta);
                     }
                  }

                  if (thirdPoint != null) {
                     attemptPathBuild(th, th.world, pathPoints, thirdPoint, nodePathBlock, pathMeta);
                  }
               }
            }

            lastNodeHalfSlab = halfSlab;
         } else {
            lastNodeHalfSlab = false;
         }
      }

      return pathPoints;
   }

   public static boolean canPathBeBuiltHere(IBlockState blockState) {
      Block block = blockState.getBlock();
      return BlockItemUtilities.isPath(block)
         ? !(Boolean)blockState.getValue(IBlockPath.STABLE)
         : BlockItemUtilities.isBlockPathReplaceable(block) || BlockItemUtilities.isBlockDecorativePlant(block);
   }

   public static boolean canPathBeBuiltHereOld(IBlockState blockState) {
      Block block = blockState.getBlock();
      return block == Blocks.DIRT
            || block == Blocks.GRASS
            || block == Blocks.SAND
            || block == Blocks.GRAVEL
            || block == Blocks.HARDENED_CLAY
            || BlockItemUtilities.isBlockDecorativePlant(block)
         ? true
         : BlockItemUtilities.isPath(block) && !(Boolean)blockState.getValue(IBlockPath.STABLE);
   }

   private static void clearPathBackward(List<AStarNode> path, boolean[] pathShouldBuild, Building th, BuildingLocation l, int index) {
      boolean exit = false;
      boolean leadsToBorder = false;

      for (int i = index - 1; i >= 0 && !exit; i--) {
         Point np = new Point(path.get(i));
         BuildingLocation l2 = th.getLocationAtCoordPlanar(np);
         if (l2 != l) {
            leadsToBorder = true;
            exit = true;
         } else if (isPointOnStablePath(np, th.world)) {
            exit = true;
         }
      }

      if (!leadsToBorder) {
         exit = false;

         for (int ix = index - 1; ix >= 0 && !exit; ix--) {
            Point np = new Point(path.get(ix));
            BuildingLocation l2 = th.getLocationAtCoordPlanar(np);
            if (l2 != l) {
               exit = true;
            } else if (isPointOnStablePath(np, th.world)) {
               exit = true;
            } else {
               pathShouldBuild[ix] = false;
            }
         }
      }
   }

   public static void clearPathBlock(Point p, World world) {
      IBlockState bs = p.getBlockActualState(world);
      if (bs.getBlock() instanceof IBlockPath && !(Boolean)bs.getValue(IBlockPath.STABLE)) {
         IBlockState blockStateBelow = p.getBelow().getBlockActualState(world);
         if (WorldUtilities.getBlockStateValidGround(blockStateBelow, true) != null) {
            p.setBlockState(world, WorldUtilities.getBlockStateValidGround(blockStateBelow, true));
         } else {
            p.setBlock(world, Blocks.DIRT, 0, true, false);
         }
      }
   }

   private static void clearPathForward(List<AStarNode> path, boolean[] pathShouldBuild, Building th, BuildingLocation l, int index) {
      boolean exit = false;
      boolean leadsToBorder = false;

      for (int i = index + 1; i < path.size() && !exit; i++) {
         Point np = new Point(path.get(i));
         BuildingLocation l2 = th.getLocationAtCoordPlanar(np);
         if (l2 != l) {
            leadsToBorder = true;
            exit = true;
         } else if (isPointOnStablePath(np, th.world)) {
            exit = true;
         }
      }

      if (!leadsToBorder) {
         exit = false;

         for (int ix = index + 1; ix < path.size() && !exit; ix++) {
            Point np = new Point(path.get(ix));
            BuildingLocation l2 = th.getLocationAtCoordPlanar(np);
            if (l2 != l) {
               exit = true;
            } else if (isPointOnStablePath(np, th.world)) {
               exit = true;
            } else {
               pathShouldBuild[ix] = false;
            }
         }
      }
   }

   public static boolean isPointOnStablePath(Point p, World world) {
      Block block = p.getBlock(world);
      if (block instanceof IBlockPath) {
         IBlockState bs = p.getBlockActualState(world);
         if ((Boolean)bs.getValue(IBlockPath.STABLE)) {
            return true;
         }
      }

      block = p.getBelow().getBlock(world);
      if (block instanceof IBlockPath) {
         IBlockState bs = p.getBelow().getBlockActualState(world);
         if ((Boolean)bs.getValue(IBlockPath.STABLE)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isStairsOrSlabOrChest(World world, Point p) {
      Block block = p.getBlock(world);
      if (block == Blocks.CHEST
         || block == MillBlocks.LOCKED_CHEST
         || block == Blocks.CRAFTING_TABLE
         || block == Blocks.FURNACE
         || block == Blocks.LIT_FURNACE) {
         return true;
      } else {
         return block instanceof BlockStairs ? true : block instanceof BlockSlab && !block.getDefaultState().isOpaqueCube();
      }
   }
}
