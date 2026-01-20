package org.millenaire.common.village;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;

import java.util.ArrayList;
import java.util.List;

public class VillageMapInfo implements Cloneable {
    public static final int UPDATE_FREQUENCY = 1000;
    public int length = 0;
    public int width = 0;
    public int chunkStartX = 0;
    public int chunkStartZ = 0;
    public int mapStartX = 0;
    public int mapStartZ = 0;
    public int yBaseline = 0;

    public BuildingLocation[][] buildingLocRef;
    public boolean[][] canBuild;
    public boolean[][] buildingForbidden;
    public boolean[][] buildTested;

    // Terrain analysis data
    public short[][] topGround;
    public short[][] spaceAbove;
    public boolean[][] danger;
    public boolean[][] water;
    public boolean[][] tree;

    private List<BuildingLocation> buildingLocations = new ArrayList<>();
    public Level world;

    public List<BuildingLocation> getBuildingLocations() {
        return buildingLocations;
    }

    public VillageMapInfo() {
    }

    public VillageMapInfo(Level world, Point centre, int radius) {
        this.world = world;
        // Basic initialization stub
    }

    public void addBuildingLocationToMap(BuildingLocation bl) {
        this.buildingLocations.add(bl);
        // Logic to update grid
    }

    public boolean update(Level world, List<BuildingLocation> locations, Point centre, int radius) {
        this.world = world;
        this.length = radius * 2;
        this.width = radius * 2;
        this.mapStartX = centre.getiX() - radius;
        this.mapStartZ = centre.getiZ() - radius;
        this.yBaseline = centre.getiY();

        this.topGround = new short[length][width];
        this.spaceAbove = new short[length][width];
        this.danger = new boolean[length][width];
        this.water = new boolean[length][width];
        this.tree = new boolean[length][width];
        this.canBuild = new boolean[length][width];
        this.buildingForbidden = new boolean[length][width];
        this.buildingLocRef = new BuildingLocation[length][width];
        this.buildTested = new boolean[length][width];

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < length; i++) {
            for (int k = 0; k < width; k++) {
                int x = mapStartX + i;
                int z = mapStartZ + k;

                // Use MOTION_BLOCKING_NO_LEAVES to find ground level
                int surfaceY = world
                        .getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

                mutablePos.set(x, surfaceY - 1, z);
                net.minecraft.world.level.block.state.BlockState state = world.getBlockState(mutablePos);

                if (!state.getFluidState().isEmpty()) {
                    water[i][k] = true;
                }

                if (state.is(net.minecraft.tags.BlockTags.LEAVES) || state.is(net.minecraft.tags.BlockTags.LOGS)) {
                    tree[i][k] = true;
                }

                if (state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.CACTUS)
                        || state.is(Blocks.SWEET_BERRY_BUSH)) {
                    danger[i][k] = true;
                }

                topGround[i][k] = (short) (surfaceY - 1);

                // Calculate space above
                int space = 0;
                for (int h = 0; h < 10; h++) {
                    mutablePos.set(x, surfaceY + h, z);
                    if (!world.getBlockState(mutablePos).blocksMotion()) {
                        space++;
                    } else {
                        break;
                    }
                }
                spaceAbove[i][k] = (short) space;

                // CRITICAL FIX: Set canBuild based on terrain conditions
                // Previously this was never set, causing all placements to fail
                // RELAXED: Allow building on most terrain - only exclude water and danger
                canBuild[i][k] = !water[i][k] && !danger[i][k];
            }
        }

        // Map existing buildings
        if (locations != null) {
            for (BuildingLocation bl : locations) {
                // Mark occupied areas if needed (simplified for now as we just need basic
                // terrain for now)
                // In full implementation, we would register building footprints
            }
        }

        return true;
    }

}
