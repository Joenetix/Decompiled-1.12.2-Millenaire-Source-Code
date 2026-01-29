package org.millenaire.common.world;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.fml.common.Mod;

import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
// import org.millenaire.common.buildingplan.TreeClearer; // Not ported/needed yet?
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.network.ServerSender;
import org.millenaire.common.pathing.atomicstryker.RegionMapper;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.utilities.WorldUtilities;
import org.millenaire.common.village.Building;
import org.millenaire.common.village.BuildingLocation;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.village.VillageMapInfo;
import org.millenaire.worldgen.VillageWallGenerator;

public class WorldGenVillage {

    public static boolean generateBedrockLoneBuilding(Point p, Level world, VillageType village, Random random,
            int minDistance, int maxDistance, Player player) {
        if (world.isClientSide) {
            return false;
        } else if (isWithinSpawnRadiusProtection(world, village, p)) {
            return false;
        } else if (village.centreBuilding == null) {
            MillLog.error(null, "Tried to create a bedrock lone building without a centre.");
            return false;
        } else {
            if (MillConfigValues.LogWorldGeneration >= 1) {
                MillLog.major(null, "Generating bedrockbuilding: " + village);
            }

            BuildingPlan plan = village.centreBuilding.getRandomStartingPlan();
            BuildingLocation location = null;

            // Simple search for spot (not implementing testSpotBedrock fully yet if
            // complex)
            // Assuming flat ground logic for now or reusing findBuildingLocation
            // Decompiled code uses plan.testSpotBedrock

            // Stubbed retry loop for finding location
            for (int i = 0; i < 100 && location == null; i++) {
                int x = minDistance + MillCommonUtilities.randomInt(maxDistance - minDistance);
                int z = minDistance + MillCommonUtilities.randomInt(maxDistance - minDistance);
                if (MillCommonUtilities.chanceOn(2))
                    x = -x;
                if (MillCommonUtilities.chanceOn(2))
                    z = -z;

                // Stub: verify if spot is valid
                // location = new BuildingLocation(plan, new Point(p.getiX() + x, 64, p.getiZ()
                // + z), 0);
                location = plan.findBuildingLocation(new VillageMapInfo(world, p, 100), null, p, 100, random, 0);
            }

            if (location == null) {
                MillLog.major(null, "No spot found for: " + village);
                // Force spawn?
                // location = new BuildingLocation(plan, new Point(p.getiX() + 10, 64, p.getiZ()
                // + 10), 0);
                // location.bedrocklevel = true;
                return false;
            }

            if (isWithinSpawnRadiusProtection(world, village, location.pos)) {
                return false;
            } else {
                MillWorldData mw = MillWorldData.get(world);
                List<BuildingPlan.LocationBuildingPair> lbps = village.centreBuilding.build(mw, village, location, true,
                        true, null, false, false, null, true);

                if (lbps.isEmpty())
                    return false;

                // Redundant build call removed
                Building townHallEntity = lbps.get(0).location.getBuilding(world);
                // Wait, build() returns LBP list, and inside it creates entities.
                // We need to retrieve the created entity.
                // The LBP contains (Location, Plan). The entity is at the location.

                if (townHallEntity == null) {
                    // Fallback purely for safety
                    return false;
                }

                if (MillConfigValues.LogWorldGeneration >= 1) {
                    MillLog.major(null, "Registering building: " + townHallEntity);
                }

                // townHallEntity.villageType = village; // Already set in constructor
                // townHallEntity.findName(null);
                townHallEntity.initialiseBuildingProjects();
                townHallEntity.registerBuildingLocation(location);

                for (BuildingPlan.LocationBuildingPair lbp : lbps) {
                    if (lbp != lbps.get(0)) {
                        Building b = lbp.location.getBuilding(world);
                        if (b != null) {
                            townHallEntity.registerBuildingEntity(b);
                            townHallEntity.registerBuildingLocation(lbp.location);
                        }
                    }
                }

                townHallEntity.initialiseVillage();
                String playerName = null;
                if (player != null) {
                    playerName = player.getName().getString();
                }

                mw.registerLoneBuildingsLocation(world, new Point(townHallEntity.getPos()),
                        townHallEntity.getVillageQualifiedName(), townHallEntity.villageType, townHallEntity.culture,
                        true, playerName);
                MillLog.major(null, "Finished bedrock building " + village + " at " + townHallEntity.getPos());
                return true;
            }
        }
    }

    private static boolean isWithinSpawnRadiusProtection(Level world, VillageType villageType, Point villagePos) {
        if (MillConfigValues.spawnProtectionRadius == 0) {
            return false;
        } else {
            int villageRadius = MillConfigValues.VillageRadius;
            if (villageType != null) {
                villageRadius = villageType.radius;
            }

            BlockPos spawn = world.getSharedSpawnPos();
            if (villagePos.horizontalDistanceTo(spawn) < MillConfigValues.spawnProtectionRadius + villageRadius) {
                if (MillConfigValues.LogWorldGeneration >= 3) {
                    MillLog.debug(null, "Blocking spawn at " + villagePos + ". Distance to spawn: "
                            + villagePos.horizontalDistanceTo(spawn));
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean generateVillageAtPoint(Level world, Random random, int x, int y, int z, Player generatingPlayer,
            boolean checkForUnloaded, boolean alwaysGenerate, boolean testBiomeValidity, int minDistance,
            VillageType specificVillageType, String name, Point parentVillage, float completionRatio) {
        if (world.isClientSide) {
            return false;
        }

        MillWorldData mw = MillWorldData.get(world);
        if (mw == null)
            return false;

        boolean generateVillages = MillConfigValues.generateVillages;
        if (mw.generateVillagesSet) {
            generateVillages = mw.generateVillages;
        }

        if (Mill.loadingComplete && (generateVillages || MillConfigValues.generateLoneBuildings || alwaysGenerate)) {
            Point p = new Point(x, y, z);

            if (isWithinSpawnRadiusProtection(world, specificVillageType, p)) {
                // If player requested, send message
                if (generatingPlayer != null) {
                    // ServerSender.sendTranslatedSentence(generatingPlayer, '6',
                    // "ui.tooclosetospawn");
                }
                if (!alwaysGenerate)
                    return false;
            }

            Player closestPlayer = generatingPlayer;
            if (generatingPlayer == null) {
                closestPlayer = world.getNearestPlayer(x, y, z, 200.0, false);
            }

            try {
                // Check if chunk loaded? Decompiled did hash checks. Ignoring for strict port
                // simplicity.

                long startTime = System.nanoTime();

                // Stub: check if village can be attempted (distance checks etc)
                // Assuming passed checks for now if alwaysGenerate is true or standard gen

                VillageType villageType = specificVillageType;
                if (villageType == null) {
                    // Find valid village type for biome
                    // verify logic from decompiled
                    String biomeName = world.getBiome(new BlockPos(x, y, z)).toString(); // Simplified
                    // Actual biome check logic needed
                    // villageType = ...
                }

                // If specific type is passed, force it (ignoring biome if testBiomeValidity is
                // false)
                if (villageType != null) {
                    return generateVillage(p, world, villageType, generatingPlayer, closestPlayer, random,
                            minDistance, name, parentVillage, completionRatio, testBiomeValidity, alwaysGenerate);
                }

            } catch (Exception e) {
                MillLog.printException("Exception generating village", e);
            }
        }
        return false;
    }

    private boolean generateVillage(Point targetPos, Level world, VillageType villageType, Player player,
            Player closestPlayer, Random random, int minDistance, String name, Point parentVillage,
            float completionRatio, boolean testBiomeValidity, boolean alwaysSpawn) {
        // Biome validity check
        // ...

        VillageMapInfo winfo = new VillageMapInfo(world, targetPos, villageType.radius);
        List<BuildingLocation> plannedBuildings = new ArrayList<>();
        MillWorldData mw = MillWorldData.get(world);

        // Find top soil
        int topY = WorldUtilities.findTopSoilBlock(world, targetPos.getiX(), targetPos.getiZ());
        targetPos = new Point(targetPos.getiX(), topY, targetPos.getiZ());

        // Ensure chunks loaded?

        winfo.update(world, plannedBuildings, targetPos, villageType.radius);

        if (!alwaysSpawn && !isUsableArea(winfo, targetPos, villageType.radius)) {
            return false;
        }

        long startTime = System.nanoTime();

        // Find center building location
        BuildingLocation location = villageType.centreBuilding.getRandomStartingPlan().findBuildingLocation(winfo, null,
                targetPos, villageType.radius, random, 3);

        if (location == null) {
            MillLog.minor(this, "Could not find place for central building: " + villageType.centreBuilding);
            return false;
        }

        Point thPos = location.pos;

        // Check spawn protection again with precise pos
        if (isWithinSpawnRadiusProtection(world, villageType, thPos)) {
            return false;
        }

        // Distance checks to other villages
        if (!alwaysSpawn) {
            // ... check distances against mw.villagesList and mw.loneBuildingsList
        }

        plannedBuildings.add(location);
        winfo.update(world, plannedBuildings, thPos, villageType.radius);

        RegionMapper regionMapper = new RegionMapper();
        try {
            regionMapper.createConnectionsTable(winfo, thPos);
        } catch (Exception e) {
            org.millenaire.common.utilities.MillLog.error(null, "Error creating connections table: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // Wall Generation
        VillageWallGenerator wallGenerator = new VillageWallGenerator(world); // Fix constructor
        if (villageType.innerWallType != null) {
            // List<BuildingLocation> walls =
            // wallGenerator.computeWallBuildingLocations(...)
            // plannedBuildings.addAll(walls);
            // winfo.update...
        }

        // Starting buildings
        boolean couldBuildKeyBuildings = true;
        for (BuildingPlanSet planSet : villageType.startBuildings) {
            BuildingLocation l = planSet.getRandomStartingPlan().findBuildingLocation(winfo, regionMapper, thPos,
                    villageType.radius, random, -1);
            if (l != null) {
                plannedBuildings.add(l);
                winfo.update(world, plannedBuildings, thPos, villageType.radius);
                // update connections
            } else {
                couldBuildKeyBuildings = false;
            }
        }

        if (!couldBuildKeyBuildings) {
            MillLog.minor(this, "Couldn't build key buildings.");
            return false;
        }

        // Generate!
        MillLog.major(this, thPos + ": Generating village...");

        List<BuildingPlan.LocationBuildingPair> lbps = villageType.centreBuilding.build(mw, villageType,
                plannedBuildings.get(0), true, true, null, false, false, player, true);

        if (lbps.isEmpty())
            return false;

        Building townHallEntity = lbps.get(0).location.getBuilding(world);
        if (townHallEntity == null)
            return false; // Error

        townHallEntity.findName(name); // Ensure findName exists or is name assignment
        townHallEntity.initialiseBuildingProjects();
        townHallEntity.registerBuildingLocation(plannedBuildings.get(0));

        for (BuildingPlan.LocationBuildingPair lbp : lbps) {
            if (lbp != lbps.get(0)) {
                townHallEntity.registerBuildingEntity(lbp.location.getBuilding(world));
                townHallEntity.registerBuildingLocation(lbp.location);
            }
        }

        // Register rest of planned buildings
        for (int i = 1; i < plannedBuildings.size(); i++) {
            BuildingLocation bl = plannedBuildings.get(i);
            BuildingPlanSet planSet = villageType.culture.getBuildingPlanSet(bl.planKey);

            // Create project?
            // If completion ratio... logic

            List<BuildingPlan.LocationBuildingPair> subLbps = planSet.build(mw, villageType, bl, true, false,
                    townHallEntity, false, false, player, true);
            for (BuildingPlan.LocationBuildingPair subLbp : subLbps) {
                townHallEntity.registerBuildingEntity(subLbp.location.getBuilding(world));
                townHallEntity.registerBuildingLocation(subLbp.location);
            }
        }

        townHallEntity.initialiseVillage();

        // Register village
        String playerName = (closestPlayer != null) ? closestPlayer.getName().getString() : null;
        if (villageType.lonebuilding) {
            mw.registerLoneBuildingsLocation(world, new Point(townHallEntity.getPos()),
                    townHallEntity.getVillageQualifiedName(), villageType, townHallEntity.culture, true, playerName);
        } else {
            mw.registerVillageLocation(world, new Point(townHallEntity.getPos()),
                    townHallEntity.getVillageQualifiedName(), villageType, townHallEntity.culture, true, playerName);
            townHallEntity.initialiseRelations(parentVillage);
        }

        return true;
    }

    private boolean isUsableArea(VillageMapInfo winfo, Point p, int radius) {
        // Stub implementation
        return true;
    }
}
