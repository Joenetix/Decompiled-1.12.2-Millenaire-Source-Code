package org.millenaire.common.pathing.atomicstryker;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class AS_PathEntity extends Path {
    private long timeLastPathIncrement = 0L;
    public final List<Node> nodesCopy;
    private int pathIndexCopy;

    public AS_PathEntity(List<Node> nodes, BlockPos target, boolean reached) {
        super(nodes, target, reached);
        this.timeLastPathIncrement = System.currentTimeMillis();
        this.nodesCopy = nodes;
        this.pathIndexCopy = 0;
    }

    @Override
    public void advance() {
        this.timeLastPathIncrement = System.currentTimeMillis();
        this.pathIndexCopy++;
        super.advance();
    }

    public Node getCurrentTargetPathPoint() {
        return this.isDone() ? null : this.nodesCopy.get(this.getNextNodeIndex());
    }

    public Node getFuturePathPoint(int jump) {
        return this.getNextNodeIndex() >= this.nodesCopy.size() - jump ? null
                : this.nodesCopy.get(this.getNextNodeIndex() + jump);
    }

    public Node getNextTargetPathPoint() {
        return this.getNextNodeIndex() >= this.nodesCopy.size() - 1 ? null
                : this.nodesCopy.get(this.getNextNodeIndex() + 1);
    }

    public Node getPastTargetPathPoint(int jump) {
        return this.getNextNodeIndex() >= jump && !this.nodesCopy.isEmpty()
                ? this.nodesCopy.get(this.getNextNodeIndex() - jump)
                : null;
    }

    @Override
    public Vec3 getEntityPosAtNode(Entity entity, int index) {
        // Logic from 1.12.2 or default to super
        return super.getEntityPosAtNode(entity, index);
    }

    public Node getPreviousTargetPathPoint() {
        return this.getNextNodeIndex() >= 1 && !this.nodesCopy.isEmpty()
                ? this.nodesCopy.get(this.getNextNodeIndex() - 1)
                : null;
    }

    public long getTimeSinceLastPathIncrement() {
        return System.currentTimeMillis() - this.timeLastPathIncrement;
    }

    public void setNodeIndex(int index) {
        this.timeLastPathIncrement = System.currentTimeMillis();
        this.pathIndexCopy = index;
        super.setNextNodeIndex(index);
    }
}
