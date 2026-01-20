package org.millenaire.common.culture;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.annotedparameters.ParametersManager;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.virtualdir.VirtualDir;

/**
 * Defines a wall type (e.g., palisade, stoneware) for a culture.
 * Contains references to the building plans for wall segments, towers, and
 * gates.
 * Ported from 1.12.2 with full ConfigField annotations.
 */
public class WallType {

    // Core wall segments
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall")
    @ConfigAnnotations.FieldDocumentation(explanation = "Walls to surround the village.", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWall = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_tower")
    @ConfigAnnotations.FieldDocumentation(explanation = "Towers for the village walls.", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallTower = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_gate")
    @ConfigAnnotations.FieldDocumentation(explanation = "Gateways for the village wall", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallGateway = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_corner")
    @ConfigAnnotations.FieldDocumentation(explanation = "Corners for the village wall", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallCorner = null;

    // Caps (ends of walls)
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_cap_right")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that stops on the right (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallCapRight = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_cap_left")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that stops on the left (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallCapLeft = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_cap_both")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that stops on both sides", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallCapBoth = null;

    // Slopes (for terrain adaptation)
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope1_left")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the left by 1 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope1Left = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope1_right")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the right by 1 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope1Right = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope2_left")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the left by 2 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope2Left = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope2_right")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the right by 2 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope2Right = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope3_left")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the left by 3 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope3Left = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope3_right")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the right by 3 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope3Right = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope4_left")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the left by 4 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope4Left = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope4_right")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the right by 4 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope4Right = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope5_left")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the left by 5 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope5Left = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope5_right")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the right by 5 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope5Right = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope6_left")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the left by 6 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope6Left = null;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "village_wall_slope6_right")
    @ConfigAnnotations.FieldDocumentation(explanation = "Optional alternate plan for the wall that rises on the right by 6 (seen from outside)", explanationCategory = "Village Walls")
    public BuildingPlanSet villageWallSlope6Right = null;

    // Spawn configuration
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "village_wall_spawn", defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether to spawn the first level of walls", explanationCategory = "Village Walls")
    public boolean villageWallSpawn = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "village_wall_tower_spawn", defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether to spawn the first level of wall towers", explanationCategory = "Village Walls")
    public boolean villageWallTowerSpawn = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "village_wall_gate_spawn", defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether to spawn the first level of wall gateways", explanationCategory = "Village Walls")
    public boolean villageWallGatewaySpawn = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "village_wall_corner_spawn", defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether to spawn the first level of wall corner towers", explanationCategory = "Village Walls")
    public boolean villageWallCornerSpawn = false;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "village_wall_cap_spawn", defaultValue = "false")
    @ConfigAnnotations.FieldDocumentation(explanation = "Whether to spawn the first level of wall caps", explanationCategory = "Village Walls")
    public boolean villageWallCapSpawn = false;

    // Wall layout configuration
    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, paramName = "village_wall_nb_between_towers", defaultValue = "3")
    @ConfigAnnotations.FieldDocumentation(explanation = "How many wall segments to place between every towers. If no walls, space in block.", explanationCategory = "Village Walls")
    public int villageWallsBetweenTowers = 3;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, paramName = "nb_smooth_runs", defaultValue = "3")
    @ConfigAnnotations.FieldDocumentation(explanation = "How many times the smoothing algorithm should be applied. The more times, the 'flatter' walls will be.", explanationCategory = "Village Walls")
    public int nbSmoothRuns = 3;

    @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, paramName = "max_y_delta", defaultValue = "15")
    @ConfigAnnotations.FieldDocumentation(explanation = "Maximum difference between a wall segment's Y position and the town hall where a wall will be built.", explanationCategory = "Village Walls")
    public int maxYDelta = 15;

    public String key;
    public Culture culture;

    public WallType(Culture c, String key) {
        this.key = key;
        this.culture = c;
    }

    /**
     * Load all wall types from a culture's walls directory.
     */
    public static Map<String, WallType> loadWalls(VirtualDir cultureVirtualDir, Culture culture) {
        VirtualDir wallsVirtualDir = cultureVirtualDir.getChildDirectory("walls");
        Map<String, WallType> walls = new HashMap<>();

        for (File file : wallsVirtualDir.listFilesRecursive(new MillCommonUtilities.ExtFileFilter("txt"))) {
            try {
                if (MillConfigValues.LogVillage >= 1) {
                    MillLog.major(file, "Loading wall: " + file.getAbsolutePath());
                }

                WallType wall = loadWallType(file, culture);
                if (wall != null) {
                    walls.put(wall.key, wall);
                }
            } catch (Exception e) {
                MillLog.printException(e);
            }
        }

        return walls;
    }

    /**
     * Load a single wall type from a file.
     */
    private static WallType loadWallType(File file, Culture c) {
        WallType wallType = new WallType(c, file.getName().split("\\.")[0]);

        try {
            ParametersManager.loadAnnotedParameterData(file, wallType, null, "wall type", c);
            validateVillageWalls(wallType);
            return wallType;
        } catch (Exception e) {
            MillLog.printException(e);
            return null;
        }
    }

    /**
     * Validate that required wall segments exist for walls to function.
     */
    private static void validateVillageWalls(WallType wallType) {
        if (wallType.villageWall != null
                && (wallType.villageWallGateway == null || wallType.villageWallCorner == null)) {
            MillLog.error(wallType,
                    "For a village to have walls, it must have gateways and corner buildings. Disabling walls");
            wallType.villageWall = null;
        }

        if (wallType.villageWallTower == null) {
            wallType.villageWallsBetweenTowers = Integer.MAX_VALUE;
        }
    }

    @Override
    public String toString() {
        return "Wall Type:" + this.culture.key + ":" + this.key;
    }
}
