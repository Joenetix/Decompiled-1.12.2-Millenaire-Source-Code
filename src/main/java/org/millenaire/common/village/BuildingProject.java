package org.millenaire.common.village;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
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
      return (BuildingProject)MillCommonUtilities.getWeightedChoice(possibleProjects, null);
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
         this.key = planSet.plans.get(0)[0].buildingKey;
      } catch (Exception var3) {
         MillLog.printException("Error when getting projet for " + this.key + ", " + planSet + ":", var3);
      }
   }

   public BuildingProject(BuildingPlanSet planSet, BuildingPlan parentPlan) {
      this.planSet = planSet;
      this.parentPlan = parentPlan;

      try {
         this.key = planSet.plans.get(0)[0].buildingKey;
      } catch (Exception var4) {
         MillLog.printException("Error when getting projet for " + this.key + ", " + planSet + ":", var4);
      }
   }

   private int adjustConstructionWeight(int weight) {
      if (this.projectTier == BuildingProject.EnumProjects.CENTRE
         || this.projectTier == BuildingProject.EnumProjects.START
         || this.projectTier == BuildingProject.EnumProjects.PLAYER) {
         return weight * 6;
      } else if (this.projectTier == BuildingProject.EnumProjects.CORE) {
         return weight * 4;
      } else {
         return this.projectTier == BuildingProject.EnumProjects.SECONDARY ? weight * 2 : weight;
      }
   }

   private int adjustUpgradeWeight(int weight) {
      return weight;
   }

   @Override
   public int getChoiceWeight(EntityPlayer player) {
      if (this.planSet == null) {
         return 0;
      } else if (this.location != null && this.location.level >= 0) {
         return this.location.level + 1 < ((BuildingPlan[])this.planSet.plans.get(this.location.getVariation())).length
            ? this.adjustUpgradeWeight(this.planSet.plans.get(this.location.getVariation())[this.location.level + 1].priority)
            : 0;
      } else {
         return this.adjustConstructionWeight(this.planSet.plans.get(0)[0].priority);
      }
   }

   public BuildingPlan getExistingPlan() {
      if (this.planSet == null) {
         return null;
      } else if (this.location == null) {
         return null;
      } else if (this.location.level < 0) {
         return null;
      } else {
         return this.location.level < ((BuildingPlan[])this.planSet.plans.get(this.location.getVariation())).length
            ? this.planSet.plans.get(this.location.getVariation())[this.location.level]
            : null;
      }
   }

   public String getFullName() {
      if (this.planSet != null) {
         return this.planSet.getNameNativeAndTranslated();
      } else {
         return this.customBuildingPlan != null ? this.customBuildingPlan.getFullDisplayName() : null;
      }
   }

   public String getGameName() {
      if (this.planSet != null) {
         return this.planSet.getNameTranslated();
      } else {
         return this.customBuildingPlan != null ? this.customBuildingPlan.getNameTranslated() : null;
      }
   }

   public int getLevelsNumber(int variation) {
      if (this.planSet == null) {
         return 0;
      } else {
         return variation >= this.planSet.plans.size() ? 1 : this.planSet.plans.get(variation).length;
      }
   }

   public String getNativeName() {
      if (this.planSet != null) {
         return this.planSet.getNameNative();
      } else {
         return this.customBuildingPlan != null ? this.customBuildingPlan.nativeName : null;
      }
   }

   public BuildingPlan getNextBuildingPlan(boolean randomStartingPlan) {
      if (this.planSet == null) {
         return null;
      } else if (this.location == null) {
         return randomStartingPlan ? this.planSet.getRandomStartingPlan() : this.planSet.getFirstStartingPlan();
      } else {
         return this.location.level + 1 < ((BuildingPlan[])this.planSet.plans.get(this.location.getVariation())).length
            ? this.planSet.plans.get(this.location.getVariation())[this.location.level + 1]
            : null;
      }
   }

   public BuildingPlan getPlan(int variation, int level) {
      if (this.planSet == null) {
         return null;
      } else if (variation >= this.planSet.plans.size()) {
         return null;
      } else {
         return level >= ((BuildingPlan[])this.planSet.plans.get(variation)).length ? null : this.planSet.plans.get(variation)[level];
      }
   }

   @Override
   public String toString() {
      return "Project " + this.key + " location: " + this.location;
   }

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

      public static BuildingProject.EnumProjects getById(int id) {
         for (BuildingProject.EnumProjects ep : values()) {
            if (ep.id == id) {
               return ep;
            }
         }

         return null;
      }

      private EnumProjects(int id, String labelKey) {
         this.id = id;
         this.labelKey = labelKey;
      }
   }
}
