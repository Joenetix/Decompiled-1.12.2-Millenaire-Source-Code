package org.millenaire.common.pathing.atomicstryker;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.world.level.Level;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.ThreadSafeUtilities;

public class AStarPathPlannerJPS {
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private AStarWorker worker = new AStarWorker(this);
    private final Level world;
    private final IAStarPathedEntity pathedEntity;
    private boolean accesslock;
    private final boolean isJPS;
    private boolean classicFallback;
    private AStarNode lastStart;
    private AStarNode lastEnd;
    public AStarConfig config;
    private long pathCalculationStartTime;

    public AStarPathPlannerJPS(Level world, IAStarPathedEntity ent, boolean isJPS) {
        this.world = world;
        this.accesslock = false;
        this.pathedEntity = ent;
        this.isJPS = isJPS;
        this.classicFallback = false;
    }

    private void flushWorker() {
        if (!this.accesslock) {
            this.worker = (AStarWorker) (this.isJPS && !this.classicFallback ? new AStarWorkerJPS(this)
                    : new AStarWorker(this));
        }
    }

    public void getPath(AStarNode start, AStarNode end, AStarConfig config) {
        if (this.isBusy() || this.worker.isRunning) {
            this.stopPathSearch(true);
        }

        while (this.accesslock) {
            Thread.yield();
        }

        this.flushWorker();
        this.accesslock = true;
        this.lastStart = start;
        this.lastEnd = end;
        this.config = config;
        this.worker.setup(this.world, start, end, config);

        try {
            this.worker.isRunning = true;
            this.pathCalculationStartTime = System.currentTimeMillis();
            executorService.submit(this.worker);
        } catch (Exception var5) {
            MillLog.printException(var5);
        }

        this.accesslock = false;
    }

    public ArrayList<AStarNode> getPathSync(AStarNode start, AStarNode end, AStarConfig config) {
        this.worker.setup(this.world, start, end, config);
        return this.worker.runSync();
    }

    public void getPath(int startx, int starty, int startz, int destx, int desty, int destz, AStarConfig config)
            throws ThreadSafeUtilities.ChunkAccessException {
        if (!AStarStatic.isViable(this.world, startx, starty, startz, 0, config)) {
            starty--;
        }

        if (!AStarStatic.isViable(this.world, startx, starty, startz, 0, config)) {
            starty += 2;
        }

        if (!AStarStatic.isViable(this.world, startx, starty, startz, 0, config)) {
            starty--;
        }

        AStarNode starter = new AStarNode(startx, starty, startz, 0, null);
        AStarNode finish = new AStarNode(destx, desty, destz, -1, null);
        this.getPath(starter, finish, config);
    }

    public boolean isBusy() {
        return this.worker.isBusy();
    }

    public void onFoundPath(ArrayList<AStarNode> result) {
        this.setClassicFallback(false);
        if (this.pathedEntity != null) {
            this.pathedEntity.onFoundPath(result);
        }
    }

    public void onNoPathAvailable() {
        if (this.isJPS && !this.classicFallback) {
            this.setClassicFallback(true);
            this.getPath(this.lastStart, this.lastEnd, this.config);
        } else {
            if (this.pathedEntity != null) {
                if (MillConfigValues.LogPathing >= 2) {
                    MillLog.minor(
                            this,
                            "No path found between " + this.lastStart + " and " + this.lastEnd + " in "
                                    + (System.currentTimeMillis() - this.pathCalculationStartTime));
                }

                this.pathedEntity.onNoPathAvailable();
            }
        }
    }

    public void setClassicFallback(boolean b) {
        this.classicFallback = b;
        this.flushWorker();
    }

    public void stopPathSearch(boolean interrupted) {
        this.flushWorker();
        if (this.pathedEntity != null && !interrupted) {
            this.pathedEntity.onNoPathAvailable();
        }
    }
}
