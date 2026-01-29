package org.millenaire.common.culture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import org.millenaire.common.annotedparameters.AnnotedParameter;
import org.millenaire.common.annotedparameters.ConfigAnnotations;
import org.millenaire.common.annotedparameters.ParametersManager;
import org.millenaire.common.buildingplan.BuildingCustomPlan;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.item.InvItem;
import org.millenaire.common.network.StreamReadWrite;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.utilities.Point;
import org.millenaire.common.utilities.virtualdir.VirtualDir;
import org.millenaire.common.village.BuildingProject;
import org.millenaire.common.world.MillWorldData;
import org.millenaire.common.world.UserProfile;

public class VillageType implements MillCommonUtilities.WeightedChoice {
   private static final String VILLAGE_TYPE_HAMEAU = "hameau";
   private static final String VILLAGE_TYPE_MARVEL = "marvel";
   private static final float MINIMUM_VALID_BIOME_PERC = 0.6F;
   public String key = null;
   public Culture culture;
   public boolean lonebuilding = false;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Name of the villager in the culture's language.")
   public String name = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM)
   @ConfigAnnotations.FieldDocumentation(explanation = "Name of a good whose icon represents this village.")
   private final InvItem icon = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_basecolor")
   @ConfigAnnotations.FieldDocumentation(explanation = "A color the village's banner can have as its base color.")
   public List<String> banner_baseColors = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_patterncolor")
   @ConfigAnnotations.FieldDocumentation(explanation = "A color the village's banner can have as its pattern color.")
   public List<String> banner_patternsColors = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_chargecolor")
   @ConfigAnnotations.FieldDocumentation(explanation = "A color the village's banner can have as its charge color.")
   public List<String> banner_chargeColors = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_pattern")
   @ConfigAnnotations.FieldDocumentation(explanation = "A pattern for the banner. Uses one of the patterncolors.")
   public List<String> banner_Patterns = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_chargepattern")
   @ConfigAnnotations.FieldDocumentation(explanation = "A charge pattern for the banner. Uses one of the chargecolors.")
   public List<String> banner_chargePatterns = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "banner_json")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A JSON object that specifies the banner's appearance. Used instead of the patterns and colors entries."
   )
   public List<String> banner_JSONs = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "travelbook_display", defaultValue = "true")
   @ConfigAnnotations.FieldDocumentation(explanation = "Whether to display this villager type in the Travel Book.")
   public boolean travelBookDisplay = true;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Generation weight. The higher it is, the more chance that this village type will be picked.",
      explanationCategory = "World Generation"
   )
   public int weight;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "biome")
   @ConfigAnnotations.FieldDocumentation(explanation = "A biome the village can spawn in.", explanationCategory = "World Generation")
   public List<String> biomes = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "-1")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Maximum number of this village type that can be generated in a given world. -1 for no limits.",
      explanationCategory = "World Generation"
   )
   public int max;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.FLOAT, defaultValue = "0.6")
   @ConfigAnnotations.FieldDocumentation(explanation = "% of village that must in the appropriate biome.", explanationCategory = "World Generation")
   private float minimumBiomeValidity;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "true")
   @ConfigAnnotations.FieldDocumentation(explanation = "Whether this village type can be generated on an MP server.", explanationCategory = "World Generation")
   public boolean generateOnServer;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, paramName = "generateforplayer", defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Whether this village type is generated for a specific player and will be listed only for him (used for 'hidden' quest buildings).",
      explanationCategory = "World Generation"
   )
   public boolean generatedForPlayer;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "-1")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Minimum distance from spawn point at which this village can appear. -1 for no limits.",
      explanationCategory = "World Generation"
   )
   public int minDistanceFromSpawn;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "requiredtag")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A global tag that has to be set for this village type to generate.",
      explanationCategory = "World Generation"
   )
   List<String> requiredTags = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "forbiddentag")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A global tag that stops the village from generating if present.",
      explanationCategory = "World Generation"
   )
   List<String> forbiddenTags = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Key lone buildings like the alchemist' tower have priority in generation and get listed in the village list.",
      explanationCategory = "World Generation"
   )
   public boolean keyLonebuilding;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Player-specific tag (given by missions) that activate the higher generation chance.",
      explanationCategory = "World Generation"
   )
   public String keyLoneBuildingGenerateTag = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(explanation = "Player-controlled village, always spawned with a wand.", explanationCategory = "Village type")
   public boolean playerControlled;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_ADD, paramName = "hameau")
   @ConfigAnnotations.FieldDocumentation(explanation = "Hamlet type that will be generated around this village.", explanationCategory = "Village type")
   public List<String> hamlets = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING, paramName = "type")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Special type of village. For example, 'hamlet', which excludes extra buildings from the project list.",
      explanationCategory = "Village type"
   )
   private String specialType = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Whether this village type can be spawned with a wand. Defaults to true for villages, false for lone buildings.",
      explanationCategory = "Village type"
   )
   public boolean spawnable;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING, paramName = "centre")
   @ConfigAnnotations.FieldDocumentation(explanation = "The building at the centre of the village.", explanationCategory = "Village Buildings")
   public BuildingPlanSet centreBuilding = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDINGCUSTOM)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "The custom building template at the centre of a custom controlled village.",
      explanationCategory = "Village Buildings"
   )
   public BuildingCustomPlan customCentre = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "start")
   @ConfigAnnotations.FieldDocumentation(explanation = "A starting building.", explanationCategory = "Village Buildings")
   public List<BuildingPlanSet> startBuildings = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "player")
   @ConfigAnnotations.FieldDocumentation(explanation = "A player-purchasable building.", explanationCategory = "Village Buildings")
   public List<BuildingPlanSet> playerBuildings = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "core")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A core building, to be built with high priority by the village type.",
      explanationCategory = "Village Buildings"
   )
   public List<BuildingPlanSet> coreBuildings = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "secondary")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A secondary building, to be build with reduced priority by the village type.",
      explanationCategory = "Village Buildings"
   )
   public List<BuildingPlanSet> secondaryBuildings = new ArrayList<>();
   public List<BuildingPlanSet> extraBuildings = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDING_ADD, paramName = "never")
   @ConfigAnnotations.FieldDocumentation(explanation = "A building this village will never build.", explanationCategory = "Village Buildings")
   public List<BuildingPlanSet> excludedBuildings = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BUILDINGCUSTOM_ADD, paramName = "customBuilding")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A custom building template usable in this custom controlled village.",
      explanationCategory = "Village Buildings"
   )
   public List<BuildingCustomPlan> customBuildings = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Radius of the village. Overwrites the default value from the settings.",
      explanationCategory = "Village Behaviour"
   )
   public int radius = MillConfigValues.VillageRadius;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.WALL_TYPE)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Type of the outer village walls, if any (built on the village's edge).",
      explanationCategory = "Village Behaviour"
   )
   public WallType outerWallType = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.WALL_TYPE)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Type of the inner village walls (built at a set radius inside the village), if any.",
      explanationCategory = "Village Behaviour"
   )
   public WallType innerWallType = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "50")
   @ConfigAnnotations.FieldDocumentation(explanation = "Radius of the inner village walls.", explanationCategory = "Village Behaviour")
   public int innerWallRadius = 0;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "1")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Maximum number of builders that can work at the same time in the village.",
      explanationCategory = "Village Behaviour"
   )
   public int maxSimultaneousConstructions;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INTEGER, defaultValue = "0")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Maximum number of builders that can work on wall buildings at the same time in the village.",
      explanationCategory = "Village Behaviour"
   )
   public int maxSimultaneousWallConstructions;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BOOLEAN, defaultValue = "false")
   @ConfigAnnotations.FieldDocumentation(explanation = "Whether this village type carries out raids.", explanationCategory = "Village Behaviour")
   public boolean carriesRaid;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_ADD)
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A block to use as path material. If more than one in the file, they will be upgraded in the same order.",
      explanationCategory = "Village Behaviour"
   )
   public List<InvItem> pathMaterial = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_PRICE_ADD, paramName = "sellingPrice")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A custom selling price for this good in the village type, overriding the culture one.",
      explanationCategory = "Village Behaviour"
   )
   public HashMap<InvItem, Integer> sellingPrices = new HashMap<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.INVITEM_PRICE_ADD, paramName = "buyingPrice")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "A custom buying price for this good in the village type, overriding the culture one.",
      explanationCategory = "Village Behaviour"
   )
   public HashMap<InvItem, Integer> buyingPrices = new HashMap<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.BRICK_COLOUR_THEME_ADD, paramName = "brickColourTheme")
   @ConfigAnnotations.FieldDocumentation(
      explanation = "Colour bricks 'themes' for Indian villages, used to defined what colours houses will have.",
      explanationCategory = "Village Behaviour"
   )
   public List<VillageType.BrickColourTheme> brickColourThemes = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING)
   @ConfigAnnotations.FieldDocumentation(explanation = "Name list to use for this village. 'villages' by default.", explanationCategory = "Village Name")
   public String nameList = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRING_CASE_SENSITIVE_ADD, paramName = "qualifier")
   @ConfigAnnotations.FieldDocumentation(explanation = "Village qualifier applicable without further conditions.", explanationCategory = "Village Name")
   public List<String> qualifiers = new ArrayList<>();
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for the village if spawned next to hills.", explanationCategory = "Village Name")
   public String hillQualifier = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for the village if spawned next to mountains.", explanationCategory = "Village Name")
   public String mountainQualifier = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for the village if spawned next to deserts.", explanationCategory = "Village Name")
   public String desertQualifier = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for the village if spawned next to forests.", explanationCategory = "Village Name")
   public String forestQualifier = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for the village if spawned next to lava.", explanationCategory = "Village Name")
   public String lavaQualifier = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for the village if spawned next to lakes.", explanationCategory = "Village Name")
   public String lakeQualifier = null;
   @ConfigAnnotations.ConfigField(type = AnnotedParameter.ParameterType.STRINGDISPLAY)
   @ConfigAnnotations.FieldDocumentation(explanation = "Qualifier for the village if spawned next to seas.", explanationCategory = "Village Name")
   public String oceanQualifier = null;

   public static List<VillageType> loadLoneBuildings(VirtualDir cultureVirtualDir, Culture culture) {
      VirtualDir lonebuildingsVirtualDir = cultureVirtualDir.getChildDirectory("lonebuildings");
      List<VillageType> v = new ArrayList<>();

      for (File file : lonebuildingsVirtualDir.listFilesRecursive(new MillCommonUtilities.ExtFileFilter("txt"))) {
         try {
            if (MillConfigValues.LogVillage >= 1) {
               MillLog.major(file, "Loading lone building: " + file.getAbsolutePath());
            }

            VillageType village = loadVillageType(file, culture, true);
            v.remove(village);
            v.add(village);
         } catch (Exception var7) {
            MillLog.printException(var7);
         }
      }

      return v;
   }

   public static List<VillageType> loadVillages(VirtualDir cultureVirtualDir, Culture culture) {
      VirtualDir villagesVirtualDir = cultureVirtualDir.getChildDirectory("villages");
      List<VillageType> villages = new ArrayList<>();

      for (File file : villagesVirtualDir.listFilesRecursive(new MillCommonUtilities.ExtFileFilter("txt"))) {
         try {
            if (MillConfigValues.LogVillage >= 1) {
               MillLog.major(file, "Loading village: " + file.getAbsolutePath());
            }

            VillageType village = loadVillageType(file, culture, false);
            villages.remove(village);
            villages.add(village);
         } catch (Exception var7) {
            MillLog.printException(var7);
         }
      }

      return villages;
   }

   public static VillageType loadVillageType(File file, Culture c, boolean lonebuilding) {
      VillageType villageType = new VillageType(c, file.getName().split("\\.")[0], lonebuilding);

      try {
         ParametersManager.loadAnnotedParameterData(file, villageType, null, "village type", c);
         if (villageType.name == null) {
            throw new MillLog.MillenaireException("No name found for village: " + villageType.key);
         } else if (villageType.centreBuilding == null && villageType.customCentre == null) {
            throw new MillLog.MillenaireException("No central building found for village: " + villageType.key);
         } else {
            if (!villageType.playerControlled
               && !"hameau".equalsIgnoreCase(villageType.specialType)
               && !"marvel".equalsIgnoreCase(villageType.specialType)
               && !villageType.lonebuilding) {
               for (BuildingPlanSet set : villageType.culture.ListPlanSets) {
                  if (!villageType.excludedBuildings.contains(set)) {
                     int nb = 0;

                     for (BuildingPlanSet aset : villageType.startBuildings) {
                        if (aset == set) {
                           nb++;
                        }
                     }

                     for (BuildingPlanSet asetx : villageType.coreBuildings) {
                        if (asetx == set) {
                           nb++;
                        }
                     }

                     for (BuildingPlanSet asetxx : villageType.secondaryBuildings) {
                        if (asetxx == set) {
                           nb++;
                        }
                     }

                     for (int i = nb; i < set.max; i++) {
                        villageType.extraBuildings.add(set);
                     }
                  }
               }
            }

            if (villageType.pathMaterial.size() == 0) {
               villageType.pathMaterial.add(InvItem.INVITEMS_BY_NAME.get("pathgravel"));
            }

            if (MillConfigValues.LogVillage >= 1) {
               MillLog.major(villageType, "Loaded village type " + villageType.name + ". NameList: " + villageType.nameList);
            }

            return villageType;
         }
      } catch (Exception var9) {
         MillLog.printException(var9);
         return null;
      }
   }

   public static List<VillageType> spawnableVillages(EntityPlayer player) {
      List<VillageType> villages = new ArrayList<>();
      UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);

      for (Culture culture : Culture.ListCultures) {
         for (VillageType village : culture.listVillageTypes) {
            if (village.spawnable && village.playerControlled && (MillConfigValues.DEV || profile.isTagSet("culturecontrol_" + village.culture.key))) {
               villages.add(village);
            }
         }

         for (VillageType villagex : culture.listVillageTypes) {
            if (villagex.spawnable && !villagex.playerControlled) {
               villages.add(villagex);
            }
         }

         for (VillageType villagexx : culture.listLoneBuildingTypes) {
            if (villagexx.spawnable && (MillConfigValues.DEV || !villagexx.playerControlled || profile.isTagSet("culturecontrol_" + villagexx.culture.key))) {
               villages.add(villagexx);
            }
         }
      }

      return villages;
   }

   public VillageType(Culture c, String key, boolean lone) {
      this.key = key;
      this.culture = c;
      this.lonebuilding = lone;
      this.spawnable = !this.lonebuilding;
      if (this.lonebuilding) {
         this.nameList = null;
      } else {
         this.nameList = "villages";
      }
   }

   public int computeTotalVillageBuildingProjects() {
      int nbBuildingsProjects = this.centreBuilding.plans.get(0).length - 1;

      for (BuildingPlanSet planSet : this.startBuildings) {
         nbBuildingsProjects += ((BuildingPlan[])planSet.plans.get(0)).length;
      }

      for (BuildingPlanSet planSet : this.coreBuildings) {
         nbBuildingsProjects += ((BuildingPlan[])planSet.plans.get(0)).length;
      }

      for (BuildingPlanSet planSet : this.secondaryBuildings) {
         nbBuildingsProjects += ((BuildingPlan[])planSet.plans.get(0)).length;
      }

      for (BuildingPlanSet planSet : this.extraBuildings) {
         nbBuildingsProjects += ((BuildingPlan[])planSet.plans.get(0)).length;
      }

      return nbBuildingsProjects;
   }

   public Map<InvItem, Integer> computeVillageTypeCost() {
      HashMap<InvItem, Integer> villageCost = new HashMap<>();

      for (BuildingPlanSet planSet : this.getAllBuildingPlanSets()) {
         for (BuildingPlan plan : planSet.plans.get(0)) {
            for (InvItem key : plan.resCost.keySet()) {
               if (villageCost.containsKey(key)) {
                  villageCost.put(key, villageCost.get(key) + plan.resCost.get(key));
               } else {
                  villageCost.put(key, plan.resCost.get(key));
               }
            }
         }
      }

      return villageCost;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof VillageType)) {
         return false;
      } else {
         VillageType v = (VillageType)obj;
         return v.culture == this.culture && v.key.equals(this.key);
      }
   }

   public List<BuildingPlanSet> getAllBuildingPlanSets() {
      List<BuildingPlanSet> planSets = new ArrayList<>();
      if (this.centreBuilding != null) {
         planSets.add(this.centreBuilding);
      }

      for (BuildingPlanSet set : this.startBuildings) {
         planSets.add(set);
      }

      if (!this.playerControlled) {
         for (BuildingPlanSet set : this.playerBuildings) {
            planSets.add(set);
         }

         for (BuildingPlanSet set : this.coreBuildings) {
            planSets.add(set);
         }

         for (BuildingPlanSet set : this.secondaryBuildings) {
            planSets.add(set);
         }

         for (BuildingPlanSet set : this.extraBuildings) {
            planSets.add(set);
         }
      } else {
         for (BuildingPlanSet set : this.playerBuildings) {
            planSets.add(set);
         }

         for (BuildingPlanSet set : this.coreBuildings) {
            planSets.add(set);
         }
      }

      for (BuildingPlanSet planSet : new ArrayList<>(planSets)) {
         BuildingPlan plan = planSet.plans.get(0)[((BuildingPlan[])planSet.plans.get(0)).length - 1];

         for (String buildingKey : plan.subBuildings) {
            planSets.add(this.culture.getBuildingPlanSet(buildingKey));
         }

         for (String buildingKey : plan.startingSubBuildings) {
            planSets.add(this.culture.getBuildingPlanSet(buildingKey));
         }
      }

      return planSets;
   }

   public ConcurrentHashMap<BuildingProject.EnumProjects, CopyOnWriteArrayList<BuildingProject>> getBuildingProjects() {
      CopyOnWriteArrayList<BuildingProject> centre = new CopyOnWriteArrayList<>();
      if (this.centreBuilding != null) {
         centre.add(this.centreBuilding.getBuildingProject());
      }

      CopyOnWriteArrayList<BuildingProject> start = new CopyOnWriteArrayList<>();

      for (BuildingPlanSet set : this.startBuildings) {
         start.add(set.getBuildingProject());
      }

      CopyOnWriteArrayList<BuildingProject> players = new CopyOnWriteArrayList<>();
      if (!this.playerControlled) {
         for (BuildingPlanSet set : this.playerBuildings) {
            players.add(set.getBuildingProject());
         }
      }

      CopyOnWriteArrayList<BuildingProject> core = new CopyOnWriteArrayList<>();
      if (!this.playerControlled) {
         for (BuildingPlanSet set : this.coreBuildings) {
            core.add(set.getBuildingProject());
         }
      }

      CopyOnWriteArrayList<BuildingProject> secondary = new CopyOnWriteArrayList<>();
      if (!this.playerControlled) {
         for (BuildingPlanSet set : this.secondaryBuildings) {
            secondary.add(set.getBuildingProject());
         }
      }

      CopyOnWriteArrayList<BuildingProject> extra = new CopyOnWriteArrayList<>();

      for (BuildingPlanSet set : this.extraBuildings) {
         extra.add(set.getBuildingProject());
      }

      ConcurrentHashMap<BuildingProject.EnumProjects, CopyOnWriteArrayList<BuildingProject>> v = new ConcurrentHashMap<>();
      v.put(BuildingProject.EnumProjects.CENTRE, centre);
      v.put(BuildingProject.EnumProjects.START, start);
      v.put(BuildingProject.EnumProjects.PLAYER, players);
      v.put(BuildingProject.EnumProjects.CORE, core);
      v.put(BuildingProject.EnumProjects.SECONDARY, secondary);
      v.put(BuildingProject.EnumProjects.EXTRA, extra);
      v.put(BuildingProject.EnumProjects.CUSTOMBUILDINGS, new CopyOnWriteArrayList<>());
      return v;
   }

   @Override
   public int getChoiceWeight(EntityPlayer player) {
      return this.isKeyLoneBuildingForGeneration(player) ? 10000 : this.weight;
   }

   public ItemStack getIcon() {
      return this.icon == null ? null : this.icon.getItemStack();
   }

   public float getMinimumBiomeValidity() {
      return this.minimumBiomeValidity;
   }

   public String getNameNative() {
      return this.name;
   }

   public String getNameNativeAndTranslated() {
      String fullName = this.getNameNative();
      if (this.getNameTranslated() != null && this.getNameTranslated().length() > 0) {
         fullName = fullName + " (" + this.getNameTranslated() + ")";
      }

      return fullName;
   }

   public String getNameTranslated() {
      return this.culture.canReadBuildingNames() ? this.culture.getCultureString("village." + this.key) : null;
   }

   public String getNameTranslationKey(UserProfile profile) {
      return profile.getCultureLanguageKnowledge(this.culture.key) <= 100 && MillConfigValues.languageLearning
         ? null
         : "culture:" + this.culture.key + ":village." + this.key;
   }

   @Override
   public int hashCode() {
      return this.culture.hashCode() + this.key.hashCode();
   }

   public boolean isHamlet() {
      return "hameau".equals(this.specialType);
   }

   public boolean isKeyLoneBuildingForGeneration(EntityPlayer player) {
      if (this.keyLonebuilding) {
         return true;
      } else {
         if (player != null) {
            UserProfile profile = Mill.getMillWorld(player.world).getProfile(player);
            if (this.keyLoneBuildingGenerateTag != null && profile.isTagSet(this.keyLoneBuildingGenerateTag)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isMarvel() {
      return "marvel".equals(this.specialType);
   }

   public boolean isRegularVillage() {
      return this.specialType == null && !this.lonebuilding;
   }

   public boolean isValidForGeneration(
      MillWorldData mw, EntityPlayer player, HashMap<String, Integer> nbVillages, Point pos, String biome, boolean keyLoneBuildingsOnly
   ) {
      if (!this.generateOnServer && Mill.proxy.isTrueServer()) {
         return false;
      } else if (this.minDistanceFromSpawn >= 0 && pos.horizontalDistanceTo(mw.world.getSpawnPoint()) <= this.minDistanceFromSpawn) {
         return false;
      } else if (!MillConfigValues.generateHamlets && !this.hamlets.isEmpty()) {
         return false;
      } else {
         for (String tag : this.requiredTags) {
            if (!mw.isGlobalTagSet(tag)) {
               return false;
            }
         }

         for (String tagx : this.forbiddenTags) {
            if (mw.isGlobalTagSet(tagx)) {
               return false;
            }
         }

         if (keyLoneBuildingsOnly && !this.isKeyLoneBuildingForGeneration(player)) {
            return false;
         } else if (!this.biomes.contains(biome)) {
            return false;
         } else {
            if (!this.isKeyLoneBuildingForGeneration(player)) {
               if (this.max != -1 && nbVillages.containsKey(this.key) && nbVillages.get(this.key) >= this.max) {
                  return false;
               }
            } else {
               boolean existingOneInRange = false;

               for (int i = 0; i < mw.loneBuildingsList.pos.size(); i++) {
                  if (mw.loneBuildingsList.types.get(i).equals(this.key) && pos.horizontalDistanceTo(mw.loneBuildingsList.pos.get(i)) < 2000.0) {
                     existingOneInRange = true;
                  }
               }

               if (existingOneInRange) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   public void readVillageTypeInfoPacket(PacketBuffer data) throws IOException {
      this.playerControlled = data.readBoolean();
      this.spawnable = data.readBoolean();
      this.name = StreamReadWrite.readNullableString(data);
      this.specialType = StreamReadWrite.readNullableString(data);
      this.radius = data.readInt();
   }

   @Override
   public String toString() {
      return this.key;
   }

   public void writeVillageTypeInfo(PacketBuffer data) throws IOException {
      data.writeString(this.key);
      data.writeBoolean(this.playerControlled);
      data.writeBoolean(this.spawnable);
      StreamReadWrite.writeNullableString(this.name, data);
      StreamReadWrite.writeNullableString(this.specialType, data);
      data.writeInt(this.radius);
   }

   public static class BrickColourTheme implements MillCommonUtilities.WeightedChoice {
      public final String key;
      public final int weight;
      public final Map<EnumDyeColor, Map<EnumDyeColor, Integer>> colours;

      public BrickColourTheme(String key, int weight, Map<EnumDyeColor, Map<EnumDyeColor, Integer>> colours) {
         this.key = key;
         this.weight = weight;
         this.colours = colours;
      }

      @Override
      public int getChoiceWeight(EntityPlayer player) {
         return this.weight;
      }

      public EnumDyeColor getRandomDyeColour(EnumDyeColor colour) {
         int totalWeight = 0;
         Map<EnumDyeColor, Integer> colourMap = this.colours.get(colour);

         for (EnumDyeColor possibleColor : colourMap.keySet()) {
            totalWeight += colourMap.get(possibleColor);
         }

         int pickedValue = MillCommonUtilities.randomInt(totalWeight);
         int currentWeightTotal = 0;

         for (EnumDyeColor possibleColor : colourMap.keySet()) {
            currentWeightTotal += colourMap.get(possibleColor);
            if (pickedValue < currentWeightTotal) {
               return possibleColor;
            }
         }

         return EnumDyeColor.WHITE;
      }

      @Override
      public String toString() {
         return "theme: " + this.key;
      }
   }
}
