package org.millenaire.common.buildingplan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.item.InvItem;

/**
 * Loads building metadata from text configuration file lines.
 * Ported from 1.12.2 to 1.20.1.
 */
public class BuildingMetadataLoader {
    private List<String> lines = new ArrayList<>();
    private boolean legacyFile = false;

    public BuildingMetadataLoader(List<String> lines) {
        this.lines = lines;
        if (lines.size() > 0 && lines.get(0).contains("length:")) {
            this.legacyFile = true;
        }
    }

    /**
     * Load data for a building plan from metadata.
     */
    public void loadDataForPlan(BuildingPlan plan, BuildingPlan previousPlan, boolean importPlan) {
        if (this.legacyFile) {
            legacyInitialisePlanConfig(plan, previousPlan);
            if (this.lines.size() > plan.level) {
                legacyReadConfigLine(plan, this.lines.get(plan.level), importPlan);
            }
        } else {
            // New format loading using annotated parameters
            initAnnotedParameterData(plan, previousPlan);
            if (plan.level == 0) {
                loadPrefixedData("building", plan);
                initParametersPostHandling(plan);
            }

            String prefix = plan.level == 0 ? "initial" : "upgrade" + plan.level;
            loadPrefixedData(prefix, plan);
        }
    }

    /**
     * Initialize annotated parameter data.
     */
    private void initAnnotedParameterData(BuildingPlan plan, BuildingPlan previousPlan) {
        if (previousPlan == null) {
            // Set defaults
            plan.max = 1;
            plan.priority = 1;
            plan.priorityMoveIn = 10;
            plan.areaToClear = 5;
            plan.startLevel = 0;
            plan.buildingOrientation = 1;
            plan.minDistance = 0.0f;
            plan.maxDistance = 1.0f;
            plan.reputation = 0;
            plan.price = 0;
            plan.showTownHallSigns = true;
        } else {
            // Copy from previous
            copyFromPrevious(plan, previousPlan);
        }
    }

    /**
     * Copy configuration from previous upgrade plan.
     */
    private void copyFromPrevious(BuildingPlan plan, BuildingPlan prev) {
        plan.max = prev.max;
        plan.priority = prev.priority;
        plan.priorityMoveIn = prev.priorityMoveIn;
        plan.nativeName = prev.nativeName;
        plan.areaToClear = prev.areaToClear;
        plan.startLevel = prev.startLevel;
        plan.buildingOrientation = prev.buildingOrientation;
        plan.signOrder = prev.signOrder.clone();
        plan.tags = new ArrayList<>(prev.tags);
        plan.villageTags = new ArrayList<>(prev.villageTags);
        plan.parentTags = new ArrayList<>(prev.parentTags);
        plan.requiredTags = new ArrayList<>(prev.requiredTags);
        plan.requiredParentTags = new ArrayList<>(prev.requiredParentTags);
        plan.requiredVillageTags = new ArrayList<>(prev.requiredVillageTags);
        plan.farFromTag = new HashMap<>(prev.farFromTag);
        plan.maleResident = new ArrayList<>(prev.maleResident);
        plan.femaleResident = new ArrayList<>(prev.femaleResident);
        plan.shop = prev.shop;
        plan.width = prev.width;
        plan.length = prev.length;
        plan.minDistance = prev.minDistance;
        plan.maxDistance = prev.maxDistance;
        plan.reputation = prev.reputation;
        plan.isgift = prev.isgift;
        plan.price = prev.price;
        plan.pathLevel = prev.pathLevel;
        plan.pathWidth = prev.pathWidth;
        plan.subBuildings = new ArrayList<>(prev.subBuildings);
        plan.startingSubBuildings = new ArrayList<>();
        plan.startingGoods = new ArrayList<>();
        plan.parent = prev;
        plan.showTownHallSigns = prev.showTownHallSigns;
        plan.exploreTag = prev.exploreTag;
        plan.irrigation = prev.irrigation;
        plan.isSubBuilding = prev.isSubBuilding;
        plan.abstractedProduction = new HashMap<>(prev.abstractedProduction);
    }

    /**
     * Post-processing after parameter initialization.
     */
    private void initParametersPostHandling(BuildingPlan plan) {
        if (plan.areaToClearLengthBefore == -1) {
            plan.areaToClearLengthBefore = plan.areaToClear;
        }
        if (plan.areaToClearLengthAfter == -1) {
            plan.areaToClearLengthAfter = plan.areaToClear;
        }
        if (plan.areaToClearWidthBefore == -1) {
            plan.areaToClearWidthBefore = plan.areaToClear;
        }
        if (plan.areaToClearWidthAfter == -1) {
            plan.areaToClearWidthAfter = plan.areaToClear;
        }
    }

    /**
     * Load prefixed data from lines into plan.
     */
    private void loadPrefixedData(String prefix, BuildingPlan plan) {
        for (String line : lines) {
            if (line.trim().isEmpty() || line.startsWith("//"))
                continue;

            String[] parts = line.split("=", 2);
            if (parts.length != 2)
                continue;

            String key = parts[0].trim();
            String value = parts[1].trim();

            // Check for prefix
            if (key.startsWith(prefix + "_")) {
                String actualKey = key.substring(prefix.length() + 1).toLowerCase();
                applyConfigValue(plan, actualKey, value);
            } else if (!key.contains("_")) {
                // Non-prefixed values for building section
                applyConfigValue(plan, key.toLowerCase(), value);
            }
        }
    }

    /**
     * Apply a configuration value to a plan field.
     */
    private void applyConfigValue(BuildingPlan plan, String key, String value) {
        switch (key) {
            case "max" -> plan.max = parseInt(value, plan.max);
            case "priority" -> plan.priority = parseInt(value, plan.priority);
            case "moveinpriority", "prioritymovein" -> plan.priorityMoveIn = parseInt(value, plan.priorityMoveIn);
            case "native", "nativename" -> plan.nativeName = value;
            case "around", "areatoclear" -> plan.areaToClear = parseInt(value, plan.areaToClear);
            case "startlevel" -> plan.startLevel = parseInt(value, plan.startLevel);
            case "orientation", "buildingorientation" ->
                plan.buildingOrientation = parseInt(value, plan.buildingOrientation);
            case "pathlevel" -> plan.pathLevel = parseInt(value, plan.pathLevel);
            case "pathwidth" -> plan.pathWidth = parseInt(value, plan.pathWidth);
            case "rebuildpath" -> plan.rebuildPath = parseBoolean(value);
            case "isgift" -> plan.isgift = parseBoolean(value);
            case "reputation" -> plan.reputation = parseInt(value, plan.reputation);
            case "price" -> plan.price = parseInt(value, plan.price);
            case "version" -> plan.version = parseInt(value, plan.version);
            case "length" -> plan.length = parseInt(value, plan.length);
            case "width" -> plan.width = parseInt(value, plan.width);
            case "mindistance" -> plan.minDistance = parseFloat(value, plan.minDistance) / 100.0f;
            case "maxdistance" -> plan.maxDistance = parseFloat(value, plan.maxDistance) / 100.0f;
            case "shop" -> plan.shop = value;
            case "tag" -> plan.tags.add(value.toLowerCase());
            case "villagetag" -> plan.villageTags.add(value.toLowerCase());
            case "parenttag" -> plan.parentTags.add(value.toLowerCase());
            case "requiredtag" -> plan.requiredTags.add(value.toLowerCase());
            case "requiredvillagetag" -> plan.requiredVillageTags.add(value.toLowerCase());
            case "requiredparenttag" -> plan.requiredParentTags.add(value.toLowerCase());
            case "subbuilding" -> plan.subBuildings.add(value);
            case "startingsubbuilding" -> plan.startingSubBuildings.add(value);
            case "type" -> {
                if ("subbuilding".equalsIgnoreCase(value))
                    plan.isSubBuilding = true;
            }
            case "showtownhallsigns" -> plan.showTownHallSigns = parseBoolean(value);
            case "exploretag" -> plan.exploreTag = value.toLowerCase();
            case "irrigation" -> plan.irrigation = parseInt(value, plan.irrigation);
            case "issubbuilding" -> plan.isSubBuilding = parseBoolean(value);
            case "parentbuildingplan" -> plan.parentBuildingPlan = value;
            case "iswallsegment" -> plan.isWallSegment = parseBoolean(value);
            case "isborderbuilding" -> plan.isBorderBuilding = parseBoolean(value);
            case "nopathstobuilding" -> plan.noPathsToBuilding = parseBoolean(value);
            case "altitudeoffset" -> plan.altitudeOffset = parseInt(value, plan.altitudeOffset);
            case "foundationdepth" -> plan.foundationDepth = parseInt(value, plan.foundationDepth);
            case "weight" -> plan.weight = parseInt(value, plan.weight);
            case "travelbook_category" -> plan.travelBookCategory = value;
            case "travelbook_display" -> plan.travelBookDisplay = parseBoolean(value);
        }
    }

    /**
     * Legacy format initialization.
     */
    private void legacyInitialisePlanConfig(BuildingPlan plan, BuildingPlan prev) {
        if (prev == null) {
            plan.max = 1;
            plan.priority = 1;
            plan.priorityMoveIn = 10;
            plan.areaToClear = 1;
            plan.startLevel = 0;
            plan.buildingOrientation = 1;
            plan.minDistance = 0.0f;
            plan.maxDistance = 1.0f;
            plan.reputation = 0;
            plan.price = 0;
            plan.showTownHallSigns = true;
        } else {
            copyFromPrevious(plan, prev);
        }
    }

    /**
     * Legacy format config line parsing (semicolon-separated key:value pairs).
     */
    private void legacyReadConfigLine(BuildingPlan plan, String line, boolean importPlan) {
        String[] configs = line.split(";", -1);
        for (String config : configs) {
            String[] kv = config.split(":");
            if (kv.length == 2) {
                String key = kv[0].toLowerCase().trim();
                String value = kv[1].trim();
                applyConfigValue(plan, key, value);
            }
        }

        if (plan.isSubBuilding) {
            plan.max = 0;
        }
        if (plan.priority < 1) {
            MillLog.error(plan, "Null or negative weight found in config!");
        }
    }

    // Parsing helpers
    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private float parseFloat(String value, float defaultValue) {
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String value) {
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }
}
