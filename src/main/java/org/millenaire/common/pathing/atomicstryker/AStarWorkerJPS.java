package org.millenaire.common.pathing.atomicstryker;

import java.util.ArrayList;
import java.util.PriorityQueue;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.ThreadSafeUtilities;

public class AStarWorkerJPS extends AStarWorker {
    private static final int MAX_SKIP_DISTANCE = 25;
    private static final int[][] neighbourOffsets = new int[][] { { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 }, { -1, 0 },
            { -1, -1 }, { 0, -1 }, { 1, -1 } };
    private final PriorityQueue<AStarNode> openQueue = new PriorityQueue<>();
    private AStarNode currentNode;

    public AStarWorkerJPS(AStarPathPlannerJPS creator) {
        super(creator);
    }

    private void addOrUpdateNode(AStarNode newNode) {
        boolean found = false;

        for (AStarNode toUpdate : this.closedNodes) {
            if (newNode.equals(toUpdate)) {
                toUpdate.updateDistance(newNode.getG(), newNode.parent);
                found = true;
                break;
            }
        }

        if (!found) {
            this.openQueue.offer(newNode);
        }
    }

    private ArrayList<AStarNode> backTrace(AStarNode start) throws ThreadSafeUtilities.ChunkAccessException {
        ArrayList<AStarNode> foundpath = new ArrayList<>();
        foundpath.add(this.currentNode);

        while (!this.currentNode.equals(start)) {
            int x = this.currentNode.x;
            int y = this.currentNode.y;
            int z = this.currentNode.z;
            int px = this.currentNode.parent.x;
            int pz = this.currentNode.parent.z;
            int dx = (px - x) / Math.max(Math.abs(x - px), 1);
            int dz = (pz - z) / Math.max(Math.abs(z - pz), 1);
            x += dx;

            for (int var11 = z + dz; x != px || var11 != pz; var11 += dz) {
                y = this.getGroundNodeHeight(x, y, var11);
                foundpath.add(new AStarNode(x, y, var11, 0, null));
                x += dx;
            }

            foundpath.add(this.currentNode.parent);
            this.currentNode = this.currentNode.parent;
        }

        return foundpath;
    }

    private ArrayList<AStarNode> findNeighbours(AStarNode node) throws ThreadSafeUtilities.ChunkAccessException {
        ArrayList<AStarNode> r = new ArrayList<>();
        int x = node.x;
        int y = node.y;
        int z = node.z;
        int dist = node.getG();
        if (node.parent != null) {
            int px = node.parent.x;
            int py = node.parent.y;
            int pz = node.parent.z;
            boolean stairs = py != y;
            int dx = (x - px) / Math.max(Math.abs(x - px), 1);
            int dz = (z - pz) / Math.max(Math.abs(z - pz), 1);
            if (dx != 0 && dz != 0) {
                if (stairs) {
                    return this.getAllNeighborsWithoutParent(x, y, z, dx, dz, node);
                }

                int left = 0;
                int right = 0;
                int nY = this.getGroundNodeHeight(x, y, z + dz);
                if (nY > 0) {
                    left = nY;
                    r.add(new AStarNode(x, nY, z + dz, dist + 1, node));
                }

                nY = this.getGroundNodeHeight(x + dx, y, z);
                if (nY > 0) {
                    right = nY;
                    r.add(new AStarNode(x + dx, nY, z, dist + 1, node));
                }

                if (left != 0 || right != 0) {
                    r.add(new AStarNode(x + dx, Math.max(left, right), z + dz, dist + 2, node));
                }

                if (left != 0 && this.getGroundNodeHeight(x - dx, py, z) == 0) {
                    r.add(new AStarNode(x - dx, left, z + dz, dist + 2, node));
                }

                if (right != 0 && this.getGroundNodeHeight(x, py, z - dz) == 0) {
                    r.add(new AStarNode(x + dx, right, z - dz, dist + 2, node));
                }
            } else if (dx == 0) {
                int nYx = this.getGroundNodeHeight(x, y, z + dz);
                if (nYx > 0) {
                    r.add(new AStarNode(x, nYx, z + dz, dist + 1, node));
                    if (stairs) {
                        r.add(new AStarNode(x + 1, nYx, z + dz, dist + 2, node));
                        r.add(new AStarNode(x - 1, nYx, z + dz, dist + 2, node));
                    } else {
                        int nnY = this.getGroundNodeHeight(x + 1, nYx, z);
                        if (nnY == 0) {
                            r.add(new AStarNode(x + 1, nYx, z + dz, dist + 2, node));
                        }

                        nnY = this.getGroundNodeHeight(x - 1, nYx, z);
                        if (nnY == 0) {
                            r.add(new AStarNode(x - 1, nYx, z + dz, dist + 2, node));
                        }
                    }
                }
            } else {
                int nYx = this.getGroundNodeHeight(x + dx, y, z);
                if (nYx > 0) {
                    r.add(new AStarNode(x + dx, nYx, z, dist + 1, node));
                    if (stairs) {
                        r.add(new AStarNode(x + dx, nYx, z + 1, dist + 2, node));
                        r.add(new AStarNode(x + dx, nYx, z - 1, dist + 2, node));
                    } else {
                        int nnYx = this.getGroundNodeHeight(x, nYx, z + 1);
                        if (nnYx == 0) {
                            r.add(new AStarNode(x + dx, nYx, z + 1, dist + 2, node));
                        }

                        nnYx = this.getGroundNodeHeight(x, nYx, z - 1);
                        if (nnYx == 0) {
                            r.add(new AStarNode(x + dx, nYx, z - 1, dist + 2, node));
                        }
                    }
                }
            }
        } else {
            for (int[] offset : neighbourOffsets) {
                int nYx = this.getGroundNodeHeight(x + offset[0], y, z + offset[1]);
                if (nYx > 0) {
                    r.add(new AStarNode(x + offset[0], nYx, z + offset[1], nYx, node));
                }
            }
        }

        return r;
    }

    private ArrayList<AStarNode> getAllNeighborsWithoutParent(int x, int y, int z, int dx, int dz, AStarNode node)
            throws ThreadSafeUtilities.ChunkAccessException {
        ArrayList<AStarNode> r = new ArrayList<>();

        for (int[] offset : neighbourOffsets) {
            if (offset[0] != -dx || offset[1] != -dz) {
                int nY = this.getGroundNodeHeight(x + offset[0], y, z + offset[1]);
                if (nY > 0) {
                    r.add(new AStarNode(x + offset[0], nY, z + offset[1], nY, node));
                }
            }
        }

        return r;
    }

    private int getGroundNodeHeight(int xN, int yN, int zN) throws ThreadSafeUtilities.ChunkAccessException {
        if (AStarStatic.isViable(this.world, xN, yN, zN, 0, this.config)) {
            return yN;
        } else if (AStarStatic.isViable(this.world, xN, yN - 1, zN, -1, this.config)) {
            return yN - 1;
        } else {
            return AStarStatic.isViable(this.world, xN, yN + 1, zN, 1, this.config) ? yN + 1 : 0;
        }
    }

    @Override
    public ArrayList<AStarNode> getPath(AStarNode start, AStarNode end, boolean searchMode)
            throws ThreadSafeUtilities.ChunkAccessException {
        this.openQueue.offer(start);
        this.targetNode = end;
        this.currentNode = start;

        for (int nbLoop = 0; !this.openQueue.isEmpty() && !this.shouldInterrupt(); nbLoop++) {
            this.currentNode = this.openQueue.poll();
            this.closedNodes.add(this.currentNode);
            if (this.isNodeEnd(this.currentNode, end) || this.identifySuccessors(this.currentNode, nbLoop)) {
                return this.backTrace(start);
            }
        }

        return null;
    }

    private boolean identifySuccessors(AStarNode node, int nbLoop) throws ThreadSafeUtilities.ChunkAccessException {
        int x = node.x;
        int y = node.y;
        int z = node.z;
        ArrayList<AStarNode> successors = this.findNeighbours(node);

        for (AStarNode s : successors) {
            AStarNode jumpPoint = this.jump(s.x, s.y, s.z, x, y, z);
            if (jumpPoint != null && !this.closedNodes.contains(jumpPoint)) {
                this.addOrUpdateNode(jumpPoint);
            }
        }

        if (nbLoop == 0 && this.openQueue.isEmpty() && MillConfigValues.LogChunkLoader >= 1) {
            MillLog.major(this, "Failed on first loop. Neighbours: " + successors.toArray());
        }

        return false;
    }

    private AStarNode jump(int nextX, int nextY, int nextZ, int px, int py, int pz)
            throws ThreadSafeUtilities.ChunkAccessException {
        int dist = this.currentNode.getG() + Math.abs(nextX - this.currentNode.x) + Math.abs(nextY - this.currentNode.y)
                + Math.abs(nextZ - this.currentNode.z);
        int dx = nextX - px;
        int dz = nextZ - pz;
        int y = this.getGroundNodeHeight(nextX, nextY, nextZ);
        if (y == 0) {
            return null;
        } else if (!this.isCoordsEnd(nextX, y, nextZ, this.targetNode) && dist < 25) {
            int nxY = dx != 0 ? this.getGroundNodeHeight(nextX + dx, y, nextZ) : 0;
            int nzY = dz != 0 ? this.getGroundNodeHeight(nextX, y, nextZ + dz) : 0;
            if (dx != 0 && dz != 0) {
                if (this.getGroundNodeHeight(nextX - dx, y, nextZ + dz) != 0
                        && this.getGroundNodeHeight(nextX - dx, nextY, nextZ) == 0
                        || this.getGroundNodeHeight(nextX + dx, y, nextZ - dz) != 0
                                && this.getGroundNodeHeight(nextX, nextY, nextZ - dz) == 0) {
                    return new AStarNode(nextX, y, nextZ, dist, this.currentNode, this.targetNode);
                }
            } else if (dx != 0) {
                if (nxY != y
                        || this.getGroundNodeHeight(nextX, y, nextZ + 1) == 0
                                && this.getGroundNodeHeight(nextX + dx, nxY, nextZ + 1) != 0
                        || this.getGroundNodeHeight(nextX, y, nextZ - 1) == 0
                                && this.getGroundNodeHeight(nextX + dx, nxY, nextZ - 1) != 0) {
                    return new AStarNode(nextX, y, nextZ, dist, this.currentNode, this.targetNode);
                }
            } else if (nzY != y
                    || this.getGroundNodeHeight(nextX + 1, y, nextZ) == 0
                            && this.getGroundNodeHeight(nextX + 1, nzY, nextZ + dz) != 0
                    || this.getGroundNodeHeight(nextX - 1, y, nextZ) == 0
                            && this.getGroundNodeHeight(nextX - 1, nzY, nextZ + dz) != 0) {
                return new AStarNode(nextX, y, nextZ, dist, this.currentNode, this.targetNode);
            }

            if (dx != 0 && dz != 0) {
                AStarNode jx = this.jump(nextX + dx, y, nextZ, nextX, y, nextZ);
                AStarNode jy = this.jump(nextX, y, nextZ + dz, nextX, y, nextZ);
                if (jx != null || jy != null) {
                    return new AStarNode(nextX, y, nextZ, dist, this.currentNode, this.targetNode);
                }
            }

            return nxY == 0 && nzY == 0 ? null : this.jump(nextX + dx, y, nextZ + dz, nextX, y, nextZ);
        } else {
            return new AStarNode(nextX, y, nextZ, dist, this.currentNode, this.targetNode);
        }
    }
}
