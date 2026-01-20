package org.millenaire.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable configuration class for building plans.
 * Used during config file parsing before creating the immutable BuildingPlan
 * record.
 */
public class BuildingPlanConfig {

    // Geometry (from PNG)
    public int width;
    public int length;
    public int nbFloors;
    public int buildingOrientation = 0;
    public int fixedOrientation = -1;

    // Terrain
    public int areaToClearLengthBefore = 1;
    public int areaToClearLengthAfter = 1;
    public int areaToClearWidthBefore = 1;
    public int areaToClearWidthAfter = 1;
    public int foundationDepth = 10;
    public int altitudeOffset = 0;

    // Positioning
    public float minDistance = 0.0f;
    public float maxDistance = 1.0f;
    public Map<String, Integer> farFromTag = new HashMap<>();
    public Map<String, Integer> closeToTag = new HashMap<>();

    // Villagers
    public List<String> maleResident = new ArrayList<>();
    public List<String> femaleResident = new ArrayList<>();
    public List<String> visitors = new ArrayList<>();
    public int priorityMoveIn = 10;

    // Hierarchy
    public boolean isSubBuilding = false;
    public String parentBuildingPlan = null;
    public List<String> subBuildings = new ArrayList<>();
    public List<String> startingSubBuildings = new ArrayList<>();

    // Economy
    public String shop = null;
    public int price = 0;
    public int reputation = 0;

    // Tags
    public List<String> tags = new ArrayList<>();
    public List<String> requiredTags = new ArrayList<>();
    public List<String> villageTags = new ArrayList<>();
    public List<String> requiredVillageTags = new ArrayList<>();
    public List<String> forbiddenTagsInVillage = new ArrayList<>();
    public List<String> clearTags = new ArrayList<>();

    // Simulation
    public int priority = 1;
    public int weight = 1;
    public int max = 1;
    public int irrigation = 0;
    public int extraSimultaneousConstructions = 0;
    public int pathLevel = 0;
    public int pathWidth = 2;
    public boolean rebuildPath = false;
    public boolean noPathsToBuilding = false;

    // Display
    public String nativeName = null;
    public String travelBookCategory = null;
    public boolean travelBookDisplay = true;
    public boolean showTownHallSigns = true;
    public boolean isWallSegment = false;
    public boolean isBorderBuilding = false;

    /**
     * Parse a single config line in "key=value" format.
     */
    public void parseLine(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("//"))
            return;

        String[] parts = line.split("=", 2);
        if (parts.length != 2)
            return;

        String key = parts[0].trim().toLowerCase();
        String value = parts[1].trim();

        switch (key) {
            case "buildingorientation" -> buildingOrientation = parseInt(value, 0);
            case "fixedorientation" -> fixedOrientation = parseInt(value, -1);
            case "areatoclearlengthbefore" -> areaToClearLengthBefore = parseInt(value, 1);
            case "areatoclearlengthafter" -> areaToClearLengthAfter = parseInt(value, 1);
            case "areatoclearwidthbefore" -> areaToClearWidthBefore = parseInt(value, 1);
            case "areatoclearwidthafter" -> areaToClearWidthAfter = parseInt(value, 1);
            case "foundationdepth" -> foundationDepth = parseInt(value, 10);
            case "altitudeoffset" -> altitudeOffset = parseInt(value, 0);
            case "mindistance" -> minDistance = parseFloat(value, 0.0f);
            case "maxdistance" -> maxDistance = parseFloat(value, 1.0f);
            case "male" -> maleResident.add(value);
            case "female" -> femaleResident.add(value);
            case "visitor" -> visitors.add(value);
            case "prioritymovein" -> priorityMoveIn = parseInt(value, 10);
            case "issubbuilding" -> isSubBuilding = parseBoolean(value);
            case "parentbuildingplan" -> parentBuildingPlan = value;
            case "subbuilding" -> subBuildings.add(value);
            case "startingsubbuilding" -> startingSubBuildings.add(value);
            case "shop" -> shop = value;
            case "price" -> price = parseInt(value, 0);
            case "reputation" -> reputation = parseInt(value, 0);
            case "tag" -> tags.add(value);
            case "requiredtag" -> requiredTags.add(value);
            case "villagetag" -> villageTags.add(value);
            case "requiredvillagetag" -> requiredVillageTags.add(value);
            case "forbiddentaginvillage" -> forbiddenTagsInVillage.add(value);
            case "cleartag" -> clearTags.add(value);
            case "priority" -> priority = parseInt(value, 1);
            case "weight" -> weight = parseInt(value, 1);
            case "max" -> max = parseInt(value, 1);
            case "irrigation" -> irrigation = parseInt(value, 0);
            case "extrasimultaneousconstructions" -> extraSimultaneousConstructions = parseInt(value, 0);
            case "pathlevel" -> pathLevel = parseInt(value, 0);
            case "pathwidth" -> pathWidth = parseInt(value, 2);
            case "rebuildpath" -> rebuildPath = parseBoolean(value);
            case "nopathstobuilding" -> noPathsToBuilding = parseBoolean(value);
            case "nativename" -> nativeName = value;
            case "travelbook_category" -> travelBookCategory = value;
            case "travelbook_display" -> travelBookDisplay = parseBoolean(value);
            case "showtownhallsigns" -> showTownHallSigns = parseBoolean(value);
            case "iswallsegment" -> isWallSegment = parseBoolean(value);
            case "isborderbuilding" -> isBorderBuilding = parseBoolean(value);
            case "farfromtag" -> parseTagDistance(value, farFromTag);
            case "closetotag" -> parseTagDistance(value, closeToTag);
        }
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private float parseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private void parseTagDistance(String value, Map<String, Integer> map) {
        // Format: tagname,distance
        String[] parts = value.split(",");
        if (parts.length >= 2) {
            map.put(parts[0].trim(), parseInt(parts[1].trim(), 0));
        }
    }
}
