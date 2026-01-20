package org.millenaire.common.pathing;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Surface pathfinding using extended tile graph.
 * Creates a walkable surface from a 3D region and computes paths on it.
 * Exact port from 1.12.2.
 */
public class PathingSurface {
    public LinkedList<ExtendedPathTile> alltiles;

    public PathingSurface(PathingPathCalcTile[][][] region, PathingPathCalcTile ct) {
        ExtendedPathTile[][][] surface = new ExtendedPathTile[region.length][region[0].length][region[0][0].length];

        for (int i = 0; i < region.length; i++) {
            for (int j = 0; j < region[0].length - 2; j++) {
                for (int k = 0; k < region[0][0].length; k++) {
                    if (region[i][j][k] == null
                            || (j + 2 >= region[0].length || region[i][j + 1][k] != null || region[i][j + 2][k] != null)
                                    && (!region[i][j][k].ladder
                                            || region[i][j + 1][k] != null && !region[i][j + 1][k].ladder)) {
                        surface[i][j][k] = null;
                    } else {
                        surface[i][j][k] = new ExtendedPathTile(region[i][j][k]);
                    }
                }
            }
        }

        ExtendedPathTile centraltile = new ExtendedPathTile(ct);
        this.alltiles = new LinkedList<>();
        LinkedList<ExtendedPathTile> toprocess = new LinkedList<>();
        if (surface[centraltile.position[0]][centraltile.position[1]][centraltile.position[2]] != null) {
            toprocess.add(surface[centraltile.position[0]][centraltile.position[1]][centraltile.position[2]]);
            surface[centraltile.position[0]][centraltile.position[1]][centraltile.position[2]].distance--;
        }

        while (!toprocess.isEmpty()) {
            ExtendedPathTile current = toprocess.pollFirst();
            this.alltiles.add(current);
            short i = current.position[0];
            short j = current.position[1];
            short kx = current.position[2];

            for (byte t = -1; t <= 1; t++) {
                if (surface[i][j][kx].ladder) {
                    if (j + t >= 0 && j + t < surface[0].length) {
                        if (surface[i][j + 1][kx].ladder) {
                            if (surface[i][j + t][kx].distance == 32767) {
                                toprocess.add(surface[i][j + t][kx]);
                                surface[i][j + t][kx].distance--;
                            }
                            current.neighbors.add(surface[i][j + t][kx]);
                        }

                        if (surface[i][j - 1][kx] != null) {
                            if (surface[i][j + t][kx].distance == 32767) {
                                toprocess.add(surface[i][j + t][kx]);
                                surface[i][j + t][kx].distance--;
                            }
                            current.neighbors.add(surface[i][j + t][kx]);
                        }
                    }
                } else if (j + t >= 0 && j + t < surface[0].length) {
                    if (i + 1 < surface.length && surface[i + 1][j + t][kx] != null) {
                        if (surface[i + 1][j + t][kx].ladder) {
                            if (t == 1 || t == 0 && surface[i + 1][j + t + 2][kx] == null) {
                                if (surface[i + 1][j + t][kx].distance == 32767) {
                                    toprocess.add(surface[i + 1][j + t][kx]);
                                    surface[i + 1][j + t][kx].distance--;
                                }
                                current.neighbors.add(surface[i + 1][j + t][kx]);
                            }
                        } else {
                            if (surface[i + 1][j + t][kx].distance == 32767) {
                                toprocess.add(surface[i + 1][j + t][kx]);
                                surface[i + 1][j + t][kx].distance--;
                            }

                            if (t == 0) {
                                current.neighbors.add(surface[i + 1][j + t][kx]);
                            } else if (t == 1 && surface[i][j + 3][kx] == null) {
                                current.neighbors.add(surface[i + 1][j + t][kx]);
                            } else if (t == -1 && surface[i + 1][j + t + 3][kx] == null) {
                                current.neighbors.add(surface[i + 1][j + t][kx]);
                            }
                        }
                    }

                    if (i - 1 >= 0 && surface[i - 1][j + t][kx] != null) {
                        if (surface[i - 1][j + t][kx].ladder) {
                            if (t == 1 || t == 0 && surface[i - 1][j + t + 2][kx] == null) {
                                if (surface[i - 1][j + t][kx].distance == 32767) {
                                    toprocess.add(surface[i - 1][j + t][kx]);
                                    surface[i - 1][j + t][kx].distance--;
                                }
                                current.neighbors.add(surface[i - 1][j + t][kx]);
                            }
                        } else {
                            if (surface[i - 1][j + t][kx].distance == 32767) {
                                toprocess.add(surface[i - 1][j + t][kx]);
                                surface[i - 1][j + t][kx].distance--;
                            }

                            if (t == 0) {
                                current.neighbors.add(surface[i - 1][j + t][kx]);
                            } else if (t == 1 && surface[i][j + 3][kx] == null) {
                                current.neighbors.add(surface[i - 1][j + t][kx]);
                            } else if (t == -1 && surface[i - 1][j + t + 3][kx] == null) {
                                current.neighbors.add(surface[i - 1][j + t][kx]);
                            }
                        }
                    }

                    if (kx - 1 >= 0 && surface[i][j + t][kx - 1] != null) {
                        if (surface[i][j + t][kx - 1].ladder) {
                            if (t == 1 || t == 0 && surface[i][j + t + 2][kx - 1] == null) {
                                if (surface[i][j + t][kx - 1].distance == 32767) {
                                    toprocess.add(surface[i][j + t][kx - 1]);
                                    surface[i][j + t][kx - 1].distance--;
                                }
                                current.neighbors.add(surface[i][j + t][kx - 1]);
                            }
                        } else {
                            if (surface[i][j + t][kx - 1].distance == 32767) {
                                toprocess.add(surface[i][j + t][kx - 1]);
                                surface[i][j + t][kx - 1].distance--;
                            }

                            if (t == 0) {
                                current.neighbors.add(surface[i][j + t][kx - 1]);
                            } else if (t == 1 && surface[i][j + 3][kx] == null) {
                                current.neighbors.add(surface[i][j + t][kx - 1]);
                            } else if (t == -1 && surface[i][j + t + 3][kx - 1] == null) {
                                current.neighbors.add(surface[i][j + t][kx - 1]);
                            }
                        }
                    }

                    if (kx + 1 < surface[0][0].length && surface[i][j + t][kx + 1] != null) {
                        if (surface[i][j + t][kx + 1].ladder) {
                            if (t == 1 || t == 0 && surface[i][j + t + 2][kx + 1] == null) {
                                if (surface[i][j + t][kx + 1].distance == 32767) {
                                    toprocess.add(surface[i][j + t][kx + 1]);
                                    surface[i][j + t][kx + 1].distance--;
                                }
                                current.neighbors.add(surface[i][j + t][kx + 1]);
                            }
                        } else {
                            if (surface[i][j + t][kx + 1].distance == 32767) {
                                toprocess.add(surface[i][j + t][kx + 1]);
                                surface[i][j + t][kx + 1].distance--;
                            }

                            if (t == 0) {
                                current.neighbors.add(surface[i][j + t][kx + 1]);
                            } else if (t == 1 && surface[i][j + 3][kx] == null) {
                                current.neighbors.add(surface[i][j + t][kx + 1]);
                            } else if (t == -1 && surface[i][j + t + 3][kx + 1] == null) {
                                current.neighbors.add(surface[i][j + t][kx + 1]);
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(this.alltiles);
    }

    public boolean contains(short[] pos) {
        boolean contains = false;
        int targetkey = pos[0] + (pos[1] << 10) + (pos[2] << 20);
        int currentindex = this.alltiles.size() / 2;
        int change = currentindex;
        ExtendedPathTile current = this.alltiles.get(currentindex);
        if (current.key == this.alltiles.get(0).key) {
            current = this.alltiles.get(0);
        } else {
            for (; current.key != targetkey && change > 1; current = this.alltiles.get(currentindex)) {
                if (current.key > targetkey) {
                    currentindex -= change / 2;
                    change = (change + 1) / 2;
                }

                if (current.key < targetkey) {
                    currentindex += change / 2;
                    change = (change + 1) / 2;
                }
            }
        }

        if (current.position[0] == pos[0] && current.position[1] == pos[1] && current.position[2] == pos[2]) {
            contains = true;
        }

        return contains;
    }

    public LinkedList<short[]> getPath(short[] start, short[] target) {
        LinkedList<short[]> way = new LinkedList<>();
        int targetkey = target[0] + (target[1] << 10) + (target[2] << 20);
        int currentindex = this.alltiles.size() / 2;
        int change = currentindex;
        ExtendedPathTile current = this.alltiles.get(currentindex);
        if (targetkey == this.alltiles.get(0).key) {
            current = this.alltiles.get(0);
        } else {
            for (; current.key != targetkey && change > 1; current = this.alltiles.get(currentindex)) {
                if (current.key > targetkey) {
                    currentindex -= change / 2;
                    change = (change + 1) / 2;
                }

                if (current.key < targetkey) {
                    currentindex += change / 2;
                    change = (change + 1) / 2;
                }
            }
        }

        LinkedList<ExtendedPathTile> processing = new LinkedList<>();
        LinkedList<ExtendedPathTile> processing2 = new LinkedList<>();
        Boolean wayfound = false;
        if (current.position[0] == target[0] && current.position[1] == target[1] && current.position[2] == target[2]) {
            processing.add(current);
            processing2.add(current);
            current.distance = 0;

            while (!processing.isEmpty()) {
                current = processing.pollFirst();

                for (int i = 0; i < current.neighbors.size(); i++) {
                    if (current.neighbors.get(i).distance > current.distance + 1) {
                        current.neighbors.get(i).distance = (short) (current.distance + 1);
                        processing.add(current.neighbors.get(i));
                        processing2.add(current.neighbors.get(i));
                    }
                }

                if (current.position[0] == start[0] && current.position[1] == start[1]
                        && current.position[2] == start[2]) {
                    wayfound = true;
                    break;
                }
            }

            ExtendedPathTile nexttile = current;
            if (wayfound) {
                way.addLast(current.position);

                while (current.distance > 0) {
                    for (int ix = 0; ix < current.neighbors.size(); ix++) {
                        if (current.neighbors.get(ix).distance < nexttile.distance) {
                            nexttile = current.neighbors.get(ix);
                        }
                    }

                    current = nexttile;
                    way.addLast(nexttile.position);
                }
            }

            while (!processing2.isEmpty()) {
                current = processing2.pollFirst();
                current.distance = 32766;
            }

            return way;
        } else {
            return null;
        }
    }

    public class ExtendedPathTile extends PathingPathCalcTile implements Comparable<ExtendedPathTile> {
        public LinkedList<ExtendedPathTile> neighbors = new LinkedList<>();
        public short distance;
        public int key;

        public ExtendedPathTile(boolean walkable, boolean lad, short[] pos) {
            super(walkable, lad, pos);
            this.key = pos[0] + (pos[1] << 10) + (pos[2] << 20);
            this.distance = 32767;
        }

        public ExtendedPathTile(PathingPathCalcTile c) {
            super(c);
            this.key = c.position[0] + (c.position[1] << 10) + (c.position[2] << 20);
            this.distance = 32767;
        }

        @Override
        public int compareTo(ExtendedPathTile arg0) {
            if (this.key == arg0.key) {
                return 0;
            } else {
                return this.key > arg0.key ? 1 : -1;
            }
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof ExtendedPathTile ? this.key == ((ExtendedPathTile) o).key : false;
        }

        @Override
        public int hashCode() {
            return this.key;
        }
    }
}
