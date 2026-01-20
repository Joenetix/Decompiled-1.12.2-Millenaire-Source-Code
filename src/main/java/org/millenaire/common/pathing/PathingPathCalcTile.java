package org.millenaire.common.pathing;

/**
 * Tile data for path calculation - tracks walkability, ladder state, and
 * position.
 * Exact port from 1.12.2.
 */
public class PathingPathCalcTile {
    public boolean ladder;
    public boolean isWalkable;
    public short[] position;

    public PathingPathCalcTile(boolean walkable, boolean lad, short[] pos) {
        this.ladder = lad;
        if (this.ladder) {
            this.isWalkable = false;
        } else if (!this.ladder && walkable) {
            this.isWalkable = true;
        }
        this.position = pos.clone();
    }

    public PathingPathCalcTile(PathingPathCalcTile c) {
        this.ladder = c.ladder;
        this.isWalkable = c.isWalkable;
        this.position = c.position.clone();
    }
}
