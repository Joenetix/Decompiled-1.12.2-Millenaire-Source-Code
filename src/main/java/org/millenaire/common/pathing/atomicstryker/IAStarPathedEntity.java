package org.millenaire.common.pathing.atomicstryker;

import java.util.List;

public interface IAStarPathedEntity {
    void onFoundPath(List<AStarNode> var1);

    void onNoPathAvailable();
}
