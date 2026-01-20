package org.millenaire.common.pathing.atomicstryker;

public class AStarNode implements Comparable<AStarNode> {
    public final int x;
    public final int y;
    public final int z;
    final AStarNode target;
    public AStarNode parent;
    private int g;
    private double h;

    public AStarNode(int ix, int iy, int iz) {
        this.x = ix;
        this.y = iy;
        this.z = iz;
        this.g = 0;
        this.parent = null;
        this.target = null;
    }

    public AStarNode(int ix, int iy, int iz, int dist, AStarNode p) {
        this.x = ix;
        this.y = iy;
        this.z = iz;
        this.g = dist;
        this.parent = p;
        this.target = null;
    }

    public AStarNode(int ix, int iy, int iz, int dist, AStarNode p, AStarNode t) {
        this.x = ix;
        this.y = iy;
        this.z = iz;
        this.g = dist;
        this.parent = p;
        this.target = t;
        this.updateTargetCostEstimate();
    }

    public AStarNode clone() {
        return new AStarNode(this.x, this.y, this.z, this.g, this.parent);
    }

    @Override
    public int compareTo(AStarNode other) {
        if (this.getF() < other.getF()) {
            return -1;
        }
        if (this.getF() > other.getF()) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object checkagainst) {
        if (checkagainst instanceof AStarNode) {
            AStarNode check = (AStarNode) checkagainst;
            if (check.x == this.x && check.y == this.y && check.z == this.z) {
                return true;
            }
        }
        return false;
    }

    public double getF() {
        return this.g + this.h;
    }

    public int getG() {
        return this.g;
    }

    @Override
    public int hashCode() {
        return this.x << 16 ^ this.z ^ this.y << 24;
    }

    @Override
    public String toString() {
        return this.parent == null
                ? String.format("[%d|%d|%d], dist %d, F: %f", this.x, this.y, this.z, this.g, this.getF())
                : String.format(
                        "[%d|%d|%d], dist %d, parent [%d|%d|%d], F: %f", this.x, this.y, this.z, this.g, this.parent.x,
                        this.parent.y, this.parent.z, this.getF());
    }

    public boolean updateDistance(int checkingDistance, AStarNode parentOtherNode) {
        if (checkingDistance < this.g) {
            this.g = checkingDistance;
            this.parent = parentOtherNode;
            this.updateTargetCostEstimate();
            return true;
        } else {
            return false;
        }
    }

    private void updateTargetCostEstimate() {
        if (this.target != null) {
            this.h = this.g + AStarStatic.getDistanceBetweenNodes(this, this.target) * 10.0;
        } else {
            this.h = 0.0;
        }
    }
}
