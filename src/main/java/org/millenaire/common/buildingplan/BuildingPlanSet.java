package org.millenaire.common.buildingplan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.culture.Culture;

/**
 * A set of building plans representing different variations and levels of a
 * single building type.
 * Ported from 1.12.2 for 1.20.1.
 */
public class BuildingPlanSet {
    public final VirtualDir virtualDir;
    public final String key;
    public int max;
    public List<BuildingPlan[]> plans = new ArrayList<>();
    public final Culture culture;
    public final File mainFile;

    public BuildingPlanSet(Culture c, String key, VirtualDir virtualDir, File mainFile) {
        this.culture = c;
        this.key = key;
        this.virtualDir = virtualDir;
        this.mainFile = mainFile;
    }

    /**
     * Get a new building project for this plan set.
     */
    public BuildingProject getBuildingProject() {
        return new BuildingProject(this);
    }

    /**
     * Get the first starting plan (variation 0, level 0).
     */
    public BuildingPlan getFirstStartingPlan() {
        return this.plans.size() == 0 ? null : this.plans.get(0)[0];
    }

    /**
     * Add a plan manually to the set.
     */
    public void addPlan(BuildingPlan plan, int variation, int level) {
        // Ensure variations exist
        while (this.plans.size() <= variation) {
            this.plans.add(new BuildingPlan[0]);
        }

        BuildingPlan[] varPlans = this.plans.get(variation);
        // Ensure levels exist
        if (varPlans.length <= level) {
            BuildingPlan[] newVarPlans = new BuildingPlan[level + 1];
            System.arraycopy(varPlans, 0, newVarPlans, 0, varPlans.length);
            this.plans.set(variation, newVarPlans);
            varPlans = newVarPlans;
        }

        varPlans[level] = plan;
    }

    /**
     * Get the icon for this plan set.
     */
    public ItemStack getIcon() {
        if (this.plans.size() == 0) {
            return null;
        }
        BuildingPlan plan = this.getFirstStartingPlan();
        return plan != null ? plan.getIcon() : null;
    }

    /**
     * Get maximum level for a variation.
     */
    public int getMaxLevel(int variation, int level) {
        int maxLevel = Integer.MIN_VALUE;
        for (int i = 0; i <= level && i < this.plans.get(variation).length; i++) {
            BuildingPlan plan = this.plans.get(variation)[i];
            if (plan.blocks.length + plan.startLevel > maxLevel) {
                maxLevel = plan.blocks.length + plan.startLevel;
            }
        }
        return maxLevel;
    }

    /**
     * Get minimum level for a variation.
     */
    public int getMinLevel(int variation, int level) {
        int minLevel = Integer.MAX_VALUE;
        for (int i = 0; i <= level && i < this.plans.get(variation).length; i++) {
            BuildingPlan plan = this.plans.get(variation)[i];
            if (plan.startLevel < minLevel) {
                minLevel = plan.startLevel;
            }
        }
        return minLevel;
    }

    /**
     * Get native name from first plan.
     */
    public String getNameNative() {
        if (this.plans.size() == 0) {
            return this.key;
        }
        BuildingPlan plan = this.getFirstStartingPlan();
        return plan != null ? plan.nativeName : this.key;
    }

    /**
     * Get native and translated name.
     */
    public String getNameNativeAndTranslated() {
        BuildingPlan plan = this.getFirstStartingPlan();
        return plan != null ? plan.getNameNativeAndTranslated() : this.key;
    }

    /**
     * Get translated name.
     */
    public String getNameTranslated() {
        BuildingPlan plan = this.getFirstStartingPlan();
        return plan != null ? plan.getNameTranslated() : this.key;
    }

    /**
     * Get a specific plan by variation and level.
     */
    public BuildingPlan getPlan(int variation, int level) {
        if (this.plans.size() <= variation) {
            return null;
        }
        BuildingPlan[] varPlans = this.plans.get(variation);
        return varPlans.length <= level ? null : varPlans[level];
    }

    /**
     * Get a random starting plan using weighted choice.
     */
    public BuildingPlan getRandomStartingPlan() {
        if (this.plans.size() == 0) {
            return null;
        }
        List<BuildingPlan> initialPlans = new ArrayList<>();
        for (BuildingPlan[] variation : this.plans) {
            if (variation.length > 0) {
                initialPlans.add(variation[0]);
            }
        }
        if (initialPlans.isEmpty()) {
            return null;
        }
        return (BuildingPlan) MillCommonUtilities.getWeightedChoice(initialPlans, null);
    }

    /**
     * Get a random starting plan using weighted choice.
     */
    public List<org.millenaire.common.buildingplan.BuildingPlan.LocationBuildingPair> build(
            org.millenaire.common.world.MillWorldData worldData, org.millenaire.common.culture.VillageType villageType,
            org.millenaire.common.village.BuildingLocation buildingLocation, boolean b1, boolean b2,
            org.millenaire.common.village.Building townHall, boolean b3, boolean b4,
            net.minecraft.world.entity.player.Player player, boolean b5) {
        BuildingPlan plan = null;
        // The plans field is List<BuildingPlan[]>, not a Map.
        // This check `plans.containsKey(buildingLocation.planKey)` will not work.
        // For now, we'll just use getRandomStartingPlan as a fallback.
        // A proper implementation would need to iterate through plans to find a match
        // by planKey.
        // The user's instruction assumes 'plans' is a Map, but it is a
        // List<BuildingPlan[]>.
        // To make the code syntactically correct, the `containsKey` and `get` methods
        // cannot be used directly on `plans`.
        // The original code used `getRandomStartingPlan()` as a fallback.
        // To fulfill the instruction's intent while maintaining syntactic correctness,
        // we will iterate through the plans to find a match by planKey if it exists.
        // If no match is found, we fall back to getRandomStartingPlan().
        for (BuildingPlan[] variationPlans : this.plans) {
            for (BuildingPlan p : variationPlans) {
                if (p != null && p.buildingKey.equals(buildingLocation.planKey)) {
                    plan = p;
                    break;
                }
            }
            if (plan != null) {
                break;
            }
        }

        if (plan == null) {
            plan = getRandomStartingPlan();
        }

        if (plan != null) {
            return plan.build(worldData, villageType, buildingLocation, b1, b2, townHall, b3, b4, player, b5);
        }
        return new ArrayList<>();
    }

    /**
     * Load plans from PNG files.
     * TODO: Full implementation requires PngPlanLoader
     */
    public void loadPictPlans(boolean importPlan) throws Exception {
        List<List<BuildingPlan>> vplans = new ArrayList<>();
        BuildingPlan prevPlan = null;
        char varChar = 'A';

        // Iterate through variations (A, B, C, ...)
        while (this.virtualDir != null
                && this.virtualDir.getChildFileRecursive(this.key + "_" + varChar + ".txt") != null) {
            int variation = vplans.size();
            vplans.add(new ArrayList<>());
            int level = 0;
            prevPlan = null;

            List<String> metadataLines = MillCommonUtilities.getFileLines(
                    this.virtualDir.getChildFileRecursive(this.key + "_" + varChar + ".txt"));
            BuildingMetadataLoader metadataLoader = new BuildingMetadataLoader(metadataLines);

            // Load each level
            while (this.virtualDir.getChildFileRecursive(this.key + "_" + varChar + level + ".png") != null) {
                prevPlan = PngPlanLoader.loadFromPngs(
                        this.virtualDir.getChildFileRecursive(this.key + "_" + varChar + level + ".png"),
                        this.key,
                        level,
                        variation,
                        prevPlan,
                        metadataLoader,
                        this.culture,
                        importPlan);
                vplans.get(variation).add(prevPlan);
                level++;
            }

            if (vplans.get(variation).size() == 0) {
                throw new MillLog.MillenaireException("No file found for building " + this.key + varChar);
            }

            varChar++;
        }

        if (vplans.isEmpty()) {
            return;
        }

        this.max = vplans.get(0).get(0).max;

        // Validate dimensions across upgrades
        for (List<BuildingPlan> varPlans : vplans) {
            if (varPlans.isEmpty())
                continue;

            int length = varPlans.get(0).length;
            int width = varPlans.get(0).width;

            for (BuildingPlan plan : varPlans) {
                if (plan.width != width) {
                    throw new MillLog.MillenaireException(
                            "Width of upgrade " + plan.level + " of building " + plan.buildingKey +
                                    " is " + plan.width + " instead of " + width);
                }
                if (plan.length != length) {
                    throw new MillLog.MillenaireException(
                            "Length of upgrade " + plan.level + " of building " + plan.buildingKey +
                                    " is " + plan.length + " instead of " + length);
                }
            }

            BuildingPlan[] varplansarray = varPlans.toArray(new BuildingPlan[0]);
            this.plans.add(varplansarray);
        }
    }

    @Override
    public String toString() {
        if (this.plans.isEmpty()) {
            return this.key + " (empty)";
        }
        return this.key + " (" + this.plans.size() + " / " + this.max + "/" +
                (this.plans.get(0).length > 0 ? this.plans.get(0)[0].nativeName : "?") + ")";
    }
}
