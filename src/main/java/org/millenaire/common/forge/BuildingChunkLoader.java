package org.millenaire.common.forge;

import org.millenaire.common.village.Building;
import org.millenaire.common.utilities.MillLog;

public class BuildingChunkLoader {

    Building townHall;
    public boolean chunksLoaded = false;

    public BuildingChunkLoader(Building th) {
        this.townHall = th;
    }

    public void loadChunks() {
        // TODO: Implement 1.20.1 Forge Chunk Loading
        // e.g. using ForgeChunkManager.loadingValidation or vanilla ForceChunk
        if (!chunksLoaded) {
            // Stub logic
            // MillLog.info(townHall, "Chunk loading requested but not implemented for
            // 1.20.1 yet.");
            this.chunksLoaded = true;
        }
    }

    public void unloadChunks() {
        // TODO: Implement Logic
        this.chunksLoaded = false;
    }

    // Inner class required by old references
    public static class ChunkLoaderCallback {
        // Stub
    }
}
