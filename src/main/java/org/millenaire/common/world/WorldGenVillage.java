package org.millenaire.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingLocation;

import java.util.*;

/**
 * Handles village generation in the world.
 * This is the main entry point for spawning new villages.
 * 
 * NOTE: Many methods are stubbed pending full implementation of dependencies.
 */
public class WorldGenVillage {
    private static final int HAMLET_ATTEMPT_ANGLE_STEPS = 36;
    private static final int HAMLET_MAX_DISTANCE = 350;
    private static final int HAMLET_MIN_DISTANCE = 250;
    private static final double MINIMUM_USABLE_BLOCK_PERC = 0.7;

    private static final HashSet<Integer> chunkCoordsTried = new HashSet<>();

    /**
     * Main entry point for village generation.
     */
    public boolean generateVillageAtPoint(
            Level world,
            Random random,
            int x,
            int y,
            int z,
            Player generatingPlayer,
            boolean checkForUnloaded,
            boolean alwaysGenerate,
            boolean testBiomeValidity,
            int minDistance,
            VillageType specificVillageType,
            String name,
            Point parentVillage,
            float completionRatio) {
        if (world.isClientSide()) {
            return false;
        }

        MillWorldData mw = Mill.getMillWorld(world);
        if (mw == null) {
            MillLog.debug(this, "No MillWorldData available for world.");
            return false;
        }

        boolean generateVillages = MillConfigValues.generateVillages;
        if (mw.generateVillagesSet) {
            generateVillages = mw.generateVillages;
        }

        if (!Mill.loadingComplete) {
            return false;
        }

        if (!generateVillages && !alwaysGenerate) {
            return false;
        }

        Point p = new Point(x, y, z);

        if (isWithinSpawnRadiusProtection(world, specificVillageType, p)) {
            if (!alwaysGenerate) {
                return false;
            }
        }

        try {
            if (MillConfigValues.LogWorldGeneration >= 3) {
                MillLog.debug(this, "Called for point: " + x + "/" + y + "/" + z);
            }

            MillCommonUtilities.random = random;
            chunkCoordsTried.add(computeChunkCoordsHash(p.getiX(), p.getiZ()));

            if (generateVillages || alwaysGenerate) {
                VillageType villageType = specificVillageType;
                if (villageType == null) {
                    villageType = findVillageType(world, p.getiX(), p.getiZ(), mw, generatingPlayer);
                }

                if (villageType != null) {
                    boolean result = createVillageBuilding(
                            mw, world, villageType, p, name, generatingPlayer);
                    return result;
                }
            }

        } catch (Exception e) {
            MillLog.printException("Exception when generating village at " + p, e);
        }

        return false;
    }

    /**
     * Create a simple village building (stub implementation).
     */
    private boolean createVillageBuilding(
            MillWorldData mw,
            Level world,
            VillageType villageType,
            Point pos,
            String name,
            Player player) {
        // Find ground level
        int groundY = findTopSolidBlock(world, pos.getiX(), pos.getiZ());
        Point buildPos = new Point(pos.x, groundY, pos.z);

        // Create location
        BuildingLocation location = new BuildingLocation();
        location.pos = buildPos;

        // Create building
        Building townHall = new Building(mw, villageType.culture, villageType, location, true);
        townHall.setWorld(world);
        townHall.villageType = villageType;
        townHall.name = name != null ? name
                : (villageType.culture != null ? villageType.culture.key + " Village" : "Village");

        // Register
        mw.addBuilding(townHall, buildPos);

        String playerName = player != null ? player.getName().getString() : null;

        if (villageType.lonebuilding) {
            mw.registerLoneBuildingsLocation(world, buildPos, townHall.name, villageType, villageType.culture, true,
                    playerName);
        } else {
            mw.registerVillageLocation(world, buildPos, townHall.name, villageType, villageType.culture, true,
                    playerName);
        }

        if (MillConfigValues.LogWorldGeneration >= 1) {
            MillLog.major(this, "New village generated at " + buildPos);
        }

        return true;
    }

    // --- Helper Methods ---

    private VillageType findVillageType(Level world, int x, int z, MillWorldData mw, Player closestPlayer) {
        List<VillageType> acceptableVillageTypes = new ArrayList<>();
        Point p = new Point(x, 64, z);

        for (Culture c : Culture.ListCultures) {
            List<VillageType> cultureVillageTypes = c.getVillageTypes();
            if (cultureVillageTypes != null) {
                for (VillageType vt : cultureVillageTypes) {
                    // Basic validation - could add biome checks here
                    acceptableVillageTypes.add(vt);
                }
            }
        }

        if (acceptableVillageTypes.isEmpty()) {
            return null;
        }

        return MillCommonUtilities.getWeightedChoice(acceptableVillageTypes, closestPlayer);
    }

    private boolean isWithinSpawnRadiusProtection(Level world, VillageType villageType, Point villagePos) {
        if (MillConfigValues.spawnProtectionRadius == 0) {
            return false;
        }

        int villageRadius = MillConfigValues.VillageRadius;
        if (villageType != null) {
            villageRadius = villageType.radius;
        }

        BlockPos spawn = world.getSharedSpawnPos();
        double distance = villagePos.horizontalDistanceTo(new Point(spawn));

        return distance < MillConfigValues.spawnProtectionRadius + villageRadius;
    }

    private int findTopSolidBlock(Level world, int x, int z) {
        for (int y = world.getMaxBuildHeight() - 1; y > world.getMinBuildHeight(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir()) {
                return y;
            }
        }
        return 64;
    }

    private int computeChunkCoordsHash(int x, int z) {
        return (x >> 4) + ((z >> 4) << 16);
    }
}
