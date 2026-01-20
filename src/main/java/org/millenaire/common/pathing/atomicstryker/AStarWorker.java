package org.millenaire.common.pathing.atomicstryker;

import java.util.ArrayList;
import java.util.PriorityQueue;
import net.minecraft.world.level.Level;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.ThreadSafeUtilities;

public class AStarWorker implements Runnable {
    private final long SEARCH_TIME_LIMIT = 150L;
    public AStarPathPlannerJPS boss;
    AStarConfig config;
    public boolean isRunning = false;
    public final ArrayList<AStarNode> closedNodes;
    private AStarNode startNode;
    protected AStarNode targetNode;
    protected Level world;
    private long timeLimit;
    private final PriorityQueue<AStarNode> queue;
    private boolean isBusy = false;

    public AStarWorker() {
        this.boss = null;
        this.closedNodes = new ArrayList<>();
        this.queue = new PriorityQueue<>(500);
    }

    public AStarWorker(AStarPathPlannerJPS creator) {
        this.boss = creator;
        this.closedNodes = new ArrayList<>();
        this.queue = new PriorityQueue<>(500);
    }

    private void addToBinaryHeap(AStarNode input) {
        this.queue.offer(input);
    }

    // checkPossibleLadder omitted/stubbed as getBlockState logic covers most of it,
    // or adapted if needed

    private int getCostNodeToNode(AStarNode a, AStarNode b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y) + Math.abs(a.z - b.z);
    }

    public void getNextCandidates(AStarNode parent, boolean droppingAllowed)
            throws ThreadSafeUtilities.ChunkAccessException {
        int x = parent.x;
        int y = parent.y;
        int z = parent.z;
        int[][] c = droppingAllowed ? AStarStatic.candidates_allowdrops : AStarStatic.candidates;

        for (int i = 0; i < c.length; i++) {
            AStarNode check = new AStarNode(x + c[i][0], y + c[i][1], z + c[i][2], parent.getG() + c[i][3], parent,
                    this.targetNode);

            try {
                boolean found = false;

                for (AStarNode toUpdate : this.closedNodes) {
                    if (check.equals(toUpdate)) {
                        toUpdate.updateDistance(check.getG() + this.getCostNodeToNode(toUpdate, check), parent);
                        found = true;
                        break;
                    }
                }

                if (!found && !this.tryToUpdateExistingHeapNode(parent, check)
                        && AStarStatic.isViable(this.world, check, c[i][1], this.config)) {
                    this.addToBinaryHeap(check);
                }
            } catch (Exception var12) {
                MillLog.printException(var12);
            }
        }
    }

    public ArrayList<AStarNode> getPath(AStarNode start, AStarNode end, boolean searchMode)
            throws ThreadSafeUtilities.ChunkAccessException {
        this.queue.offer(start);
        this.targetNode = end;

        AStarNode current;
        for (current = start; !this.isNodeEnd(current, end); current = this.queue.peek()) {
            this.closedNodes.add(this.queue.poll());
            this.getNextCandidates(current, searchMode);
            if (this.queue.isEmpty() || this.shouldInterrupt()) {
                return null;
            }
        }

        ArrayList<AStarNode> foundpath = new ArrayList<>();
        foundpath.add(current);

        while (current != start) {
            foundpath.add(current.parent);
            current = current.parent;
        }

        return foundpath;
    }

    public boolean isBusy() {
        return this.isBusy;
    }

    protected boolean isCoordsEnd(int x, int y, int z, AStarNode end) {
        return this.config.tolerance
                ? Math.abs(x - end.x) <= this.config.toleranceHorizontal
                        && Math.abs(z - end.z) <= this.config.toleranceHorizontal
                        && Math.abs(y - end.y) <= this.config.toleranceVertical
                : x == end.x && y == end.y && z == end.z;
    }

    protected boolean isNodeEnd(AStarNode cn, AStarNode end) {
        return this.isCoordsEnd(cn.x, cn.y, cn.z, end);
    }

    @Override
    public void run() {
        this.isBusy = true;
        this.timeLimit = System.currentTimeMillis() + 150L;
        ArrayList<AStarNode> result = null;

        try {
            result = this.getPath(this.startNode, this.targetNode, this.config.allowDropping);
        } catch (ThreadSafeUtilities.ChunkAccessException var3) {
            MillLog.error(this, "Chunk access violation while calculating a path for " + this.boss);
            this.boss.onNoPathAvailable();
        } catch (Throwable var4) {
            MillLog.printException("Exception while calculating a path:", var4);
            this.boss.onNoPathAvailable();
        }

        if (result == null) {
            this.boss.onNoPathAvailable();
        } else {
            this.boss.onFoundPath(result);
        }

        this.isBusy = false;
    }

    public ArrayList<AStarNode> runSync() {
        this.timeLimit = System.currentTimeMillis() + 150L;
        try {
            return this.getPath(this.startNode, this.targetNode, this.config.allowDropping);
        } catch (ThreadSafeUtilities.ChunkAccessException var3) {
            MillLog.error(this, "Chunk access violation while calculating a path for " + this.boss);
            return null;
        } catch (Throwable var4) {
            MillLog.printException("Exception while calculating a path:", var4);
            return null;
        }
    }

    public void setup(Level winput, AStarNode start, AStarNode end, AStarConfig config) {
        this.world = winput;
        this.startNode = start;
        this.targetNode = end;
        this.config = config;
    }

    protected boolean shouldInterrupt() {
        return System.currentTimeMillis() > this.timeLimit;
    }

    private boolean tryToUpdateExistingHeapNode(AStarNode parent, AStarNode checkedOne) {
        for (AStarNode itNode : this.queue) {
            if (itNode.equals(checkedOne)) {
                itNode.updateDistance(checkedOne.getG(), parent);
                return true;
            }
        }
        return false;
    }
}
