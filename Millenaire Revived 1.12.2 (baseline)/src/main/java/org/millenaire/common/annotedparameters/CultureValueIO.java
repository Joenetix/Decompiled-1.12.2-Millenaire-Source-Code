package org.millenaire.common.annotedparameters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.millenaire.common.buildingplan.BuildingCustomPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.culture.WallType;
import org.millenaire.common.utilities.MillLog;

public abstract class CultureValueIO extends ValueIO {
   @Override
   public void readValue(Object targetClass, Field field, String value) throws Exception {
      MillLog.error(this, "Using readValue on a CultureValueIO object.");
   }

   @Override
   public boolean useCulture() {
      return true;
   }

   public static class BuildingCustomAddIO extends CultureValueIO {
      public BuildingCustomAddIO() {
         this.description = "A custom building from the current culture. Multiple lines allowed.";
      }

      @Override
      public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
         if (culture.getBuildingCustom(value) != null) {
            ((List)field.get(targetClass)).add(culture.getBuildingCustom(value));
         } else {
            throw new MillLog.MillenaireException("Unknown custom building: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<BuildingCustomPlan> customPlans = (List<BuildingCustomPlan>)rawValue;
         List<String> results = new ArrayList<>();

         for (BuildingCustomPlan customPlan : customPlans) {
            results.add(customPlan.buildingKey);
         }

         return results;
      }
   }

   public static class BuildingCustomIO extends CultureValueIO {
      public BuildingCustomIO() {
         this.description = "A custom building from the current culture. One allowed.";
      }

      @Override
      public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
         if (culture.getBuildingCustom(value) != null) {
            field.set(targetClass, culture.getBuildingCustom(value));
         } else {
            throw new MillLog.MillenaireException("Unknown custom building: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         BuildingCustomPlan plan = (BuildingCustomPlan)rawValue;
         return createListFromValue(plan.buildingKey);
      }
   }

   public static class BuildingSetAddIO extends CultureValueIO {
      public BuildingSetAddIO() {
         this.description = "A building from the current culture. Multiple lines allowed.";
      }

      @Override
      public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
         if (culture.getBuildingPlanSet(value) != null) {
            ((List)field.get(targetClass)).add(culture.getBuildingPlanSet(value));
         } else {
            throw new MillLog.MillenaireException("Unknown building: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         List<BuildingPlanSet> plans = (List<BuildingPlanSet>)rawValue;
         List<String> results = new ArrayList<>();

         for (BuildingPlanSet plan : plans) {
            results.add(plan.key);
         }

         return results;
      }
   }

   public static class BuildingSetIO extends CultureValueIO {
      public BuildingSetIO() {
         this.description = "A building from the current culture. One allowed.";
      }

      @Override
      public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
         if (culture.getBuildingPlanSet(value) != null) {
            field.set(targetClass, culture.getBuildingPlanSet(value));
         } else {
            throw new MillLog.MillenaireException("Unknown building: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         BuildingPlanSet plan = (BuildingPlanSet)rawValue;
         return createListFromValue(plan.key);
      }
   }

   public static class ShopIO extends CultureValueIO {
      public ShopIO() {
         this.description = "A shop from the current culture.";
      }

      @Override
      public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
         value = value.toLowerCase();
         if (culture != null && !culture.shopBuys.containsKey(value) && !culture.shopSells.containsKey(value) && !culture.shopBuysOptional.containsKey(value)) {
            throw new MillLog.MillenaireException("Unknown shop: " + value);
         } else {
            field.set(targetClass, value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         String value = (String)rawValue;
         return createListFromValue(value);
      }
   }

   public static class VillagerAddIO extends CultureValueIO {
      public VillagerAddIO() {
         this.description = "A villager type from the current culture. Multiple lines allowed.";
      }

      @Override
      public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
         if (culture != null && culture.villagerTypes.get(value.toLowerCase()) == null) {
            throw new MillLog.MillenaireException("Unknown villager type: " + value);
         } else {
            ((List)field.get(targetClass)).add(value.toLowerCase());
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         return (List<String>)rawValue;
      }
   }

   public static class WallIO extends CultureValueIO {
      public WallIO() {
         this.description = "A wall type from the current culture. One allowed.";
      }

      @Override
      public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
         if (culture.wallTypes.containsKey(value)) {
            field.set(targetClass, culture.wallTypes.get(value));
         } else {
            throw new MillLog.MillenaireException("Unknown wall type: " + value);
         }
      }

      @Override
      public List<String> writeValue(Object rawValue) throws Exception {
         WallType wall = (WallType)rawValue;
         return createListFromValue(wall.key);
      }
   }
}
