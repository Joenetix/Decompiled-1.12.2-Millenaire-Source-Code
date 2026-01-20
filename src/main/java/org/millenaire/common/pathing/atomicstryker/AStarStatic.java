package org.millenaire.common.pathing.atomicstryker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.millenaire.common.utilities.BlockItemUtilities;
import org.millenaire.common.utilities.BlockStateUtilities;
import org.millenaire.common.utilities.ThreadSafeUtilities;

public class AStarStatic {
    static final int[][] candidates = new int[][] {
            { 0, 0, -1, 1 }, { 0, 0, 1, 1 }, { 0, 1, 0, 1 }, { 1, 0, 0, 1 },
            { -1, 0, 0, 1 }, { 1, 1, 0, 2 }, { -1, 1, 0, 2 }, { 0, 1, 1, 2 },
            { 0, 1, -1, 2 }, { 1, -1, 0, 1 }, { -1, -1, 0, 1 }, { 0, -1, 1, 1 },
            { 0, -1, -1, 1 }
    };
    static final int[][] candidates_allowdrops = new int[][] {
            { 0, 0, -1, 1 }, { 0, 0, 1, 1 }, { 1, 0, 0, 1 }, { -1, 0, 0, 1 },
            { 1, 1, 0, 2 }, { -1, 1, 0, 2 }, { 0, 1, 1, 2 }, { 0, 1, -1, 2 },
            { 1, -1, 0, 1 }, { -1, -1, 0, 1 }, { 0, -1, 1, 1 }, { 0, -1, -1, 1 },
            { 1, -2, 0, 1 }, { -1, -2, 0, 1 }, { 0, -2, 1, 1 }, { 0, -2, -1, 1 }
    };

    public static AStarNode[] getAccessNodesSorted(Level world, int workerX, int workerY, int workerZ, int posX,
            int posY, int posZ, AStarConfig config) throws ThreadSafeUtilities.ChunkAccessException {
        ArrayList<AStarNode> resultList = new ArrayList<>();

        for (int xIter = -2; xIter <= 2; xIter++) {
            for (int zIter = -2; zIter <= 2; zIter++) {
                for (int yIter = -3; yIter <= 2; yIter++) {
                    AStarNode check = new AStarNode(posX + xIter, posY + yIter, posZ + zIter,
                            Math.abs(xIter) + Math.abs(yIter), null);
                    if (isViable(world, check, 1, config)) {
                        resultList.add(check);
                    }
                }
            }
        }

        Collections.sort(resultList);

        return resultList.toArray(new AStarNode[0]);
    }

    public static double getDistanceBetweenCoords(int x, int y, int z, int posX, int posY, int posZ) {
        return Math.sqrt(Math.pow(x - posX, 2.0) + Math.pow(y - posY, 2.0) + Math.pow(z - posZ, 2.0));
    }

    public static double getDistanceBetweenNodes(AStarNode a, AStarNode b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2.0) + Math.pow(a.y - b.y, 2.0) + Math.pow(a.z - b.z, 2.0));
    }

    public static double getEntityLandSpeed(LivingEntity entLiving) {
        return entLiving.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    public static int getIntCoordFromDoubleCoord(double input) {
        return Mth.floor(input);
    }

    public static boolean isLadder(Level world, Block b, int x, int y, int z) {
        // In 1.20, use BlockState or Tags. For now, check manually or assume simple
        // ladder.
        // Forge usually handles this via isLadder on state.
        // Assuming 'b' is the block type.
        return b == Blocks.LADDER || b == Blocks.VINE;
    }

    public static boolean isPassableBlock(Level world, int ix, int iy, int iz, AStarConfig config)
            throws ThreadSafeUtilities.ChunkAccessException {
        BlockState blockState = ThreadSafeUtilities.getBlockState(world, ix, iy, iz);
        Block block = blockState.getBlock();

        if (iy > 0) {
            Block blockBelow = ThreadSafeUtilities.getBlock(world, ix, iy - 1, iz);
            if (BlockItemUtilities.isFence(blockBelow)
                    || blockBelow == Blocks.COBBLESTONE_WALL // Fence like
                    || blockBelow instanceof WallBlock) {
                return false;
            }
        }

        if (block == Blocks.AIR) {
            return true;
        } else if (!config.canSwim && (block == Blocks.WATER)) {
            return false;
        } else if (!config.canUseDoors
                || !BlockItemUtilities.isWoodenDoor(block) && !BlockItemUtilities.isFenceGate(block)) {
            if (config.canClearLeaves && block instanceof LeavesBlock) {
                // In 1.20 LeavesBlock uses 'PERSISTENT' property instead of decayable check
                // logic
                return true;
            }

            return !blockState.canOcclude(); // Simplified passability check
        } else {
            return true;
        }
    }

    public static boolean isViable(Level world, AStarNode target, int yoffset, AStarConfig config)
            throws ThreadSafeUtilities.ChunkAccessException {
        return isViable(world, target.x, target.y, target.z, yoffset, config);
    }

    public static boolean isViable(Level world, int x, int y, int z, int yoffset, AStarConfig config)
            throws ThreadSafeUtilities.ChunkAccessException {
        Block block = ThreadSafeUtilities.getBlock(world, x, y, z);
        Block blockBelow = ThreadSafeUtilities.getBlock(world, x, y - 1, z);

        if (block == Blocks.VINE && isPassableBlock(world, x, y + 1, z, config)) {
            return true;
        } else if (isPassableBlock(world, x, y, z, config) && isPassableBlock(world, x, y + 1, z, config)) {
            if (blockBelow != Blocks.WATER) {
                if (isPassableBlock(world, x, y - 1, z, config)) {
                    if (!config.canSwim) {
                        return false;
                    }

                    if (block != Blocks.WATER) {
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

    public static AS_PathEntity translateAStarPathtoPathEntity(Level world, List<AStarNode> input, AStarConfig config)
            throws ThreadSafeUtilities.ChunkAccessException {
        // Logic mapped from 1.12.2 to create AS_PathEntity from nodes
        List<net.minecraft.world.level.pathfinder.Node> nodes = new ArrayList<>();
        for (AStarNode n : input) {
            nodes.add(new net.minecraft.world.level.pathfinder.Node(n.x, n.y, n.z));
        }
        BlockPos target = input.isEmpty() ? BlockPos.ZERO
                : new BlockPos(input.get(input.size() - 1).x, input.get(input.size() - 1).y,
                        input.get(input.size() - 1).z);
        return new AS_PathEntity(nodes, target, true);
    }
}
