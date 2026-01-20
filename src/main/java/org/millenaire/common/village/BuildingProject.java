package org.millenaire.common.village;

import java.util.List;
import net.minecraft.world.entity.player.Player;
import org.millenaire.common.buildingplan.BuildingCustomPlan;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;

public class BuildingProject implements MillCommonUtilities.WeightedChoice {
    public BuildingPlanSet planSet = null;
    public BuildingPlan parentPlan = null;
    public BuildingLocation location = null;
    public BuildingCustomPlan customBuildingPlan = null;
    public String key;
    public boolean isCustomBuilding = false;
    public BuildingProject.EnumProjects projectTier = BuildingProject.EnumProjects.EXTRA;

    public static BuildingProject getRandomProject(List<BuildingProject> possibleProjects) {
        return (BuildingProject) MillCommonUtilities.getWeightedChoice(possibleProjects, null);
    }

    public BuildingProject() {
    }

    public BuildingProject(BuildingCustomPlan customPlan, BuildingLocation location) {
        this.customBuildingPlan = customPlan;
        this.key = this.customBuildingPlan.buildingKey;
        this.location = location;
        this.isCustomBuilding = true;
    }

    public BuildingProject(BuildingPlanSet planSet) {
        this.planSet = planSet;
        try {
            // Assuming plans structure: List<BuildingPlan[]>
            if (!planSet.plans.isEmpty() && planSet.plans.get(0).length > 0) {
                this.key = planSet.plans.get(0)[0].buildingKey;
            }
        } catch (Exception var3) {
            MillLog.printException("Error when getting project for " + this.key, var3);
        }
    }

    public BuildingProject(BuildingPlanSet planSet, BuildingPlan parentPlan) {
        this.planSet = planSet;
        this.parentPlan = parentPlan;
        try {
            if (!planSet.plans.isEmpty() && planSet.plans.get(0).length > 0) {
                this.key = planSet.plans.get(0)[0].buildingKey;
            }
        } catch (Exception var4) {
            MillLog.printException("Error when getting project for " + this.key, var4);
        }
    }

    @Override
    public int getChoiceWeight(Player player) {
        // Simplified weighing logic
        if (this.planSet == null) {
            return 0;
        }
        return 10;
    }

    // ... getters ...

    public static enum EnumProjects {
        CENTRE(0, "ui.buildingscentre"),
        START(1, "ui.buildingsstarting"),
        PLAYER(2, "ui.buildingsplayer"),
        CORE(3, "ui.buildingskey"),
        SECONDARY(4, "ui.buildingssecondary"),
        EXTRA(5, "ui.buildingsextra"),
        CUSTOMBUILDINGS(6, "ui.buildingcustom"),
        WALLBUILDING(7, "ui.buildingswall");

        public final int id;
        public final String labelKey;

        private EnumProjects(int id, String labelKey) {
            this.id = id;
            this.labelKey = labelKey;
        }
    }
}
