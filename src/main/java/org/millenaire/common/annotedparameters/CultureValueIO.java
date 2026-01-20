package org.millenaire.common.annotedparameters;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.millenaire.common.buildingplan.BuildingCustomPlan;
import org.millenaire.common.buildingplan.BuildingPlanSet;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.culture.Culture;

/**
 * Culture-specific ValueIO handlers that require a Culture reference.
 * Ported from 1.12.2 to 1.20.1.
 */
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
        @SuppressWarnings("unchecked")
        public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
            // TODO: Implement when Culture.getBuildingCustom() is ported
            // For now, just store the key
            ((List<String>) field.get(targetClass)).add(value.toLowerCase().trim());
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            // If it's a list of BuildingCustomPlan, get their keys
            if (rawValue instanceof List) {
                List<?> list = (List<?>) rawValue;
                List<String> results = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof BuildingCustomPlan) {
                        results.add(((BuildingCustomPlan) item).buildingKey);
                    } else if (item instanceof String) {
                        results.add((String) item);
                    }
                }
                return results;
            }
            return new ArrayList<>();
        }
    }

    public static class BuildingCustomIO extends CultureValueIO {
        public BuildingCustomIO() {
            this.description = "A custom building from the current culture. One allowed.";
        }

        @Override
        public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
            // TODO: Implement when Culture.getBuildingCustom() is ported
            // For now, store as string
            field.set(targetClass, value.toLowerCase().trim());
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            if (rawValue instanceof BuildingCustomPlan) {
                return createListFromValue(((BuildingCustomPlan) rawValue).buildingKey);
            } else if (rawValue instanceof String) {
                return createListFromValue((String) rawValue);
            }
            return createListFromValue("");
        }
    }

    public static class BuildingSetAddIO extends CultureValueIO {
        public BuildingSetAddIO() {
            this.description = "A building from the current culture. Multiple lines allowed.";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
            BuildingPlanSet set = culture.getBuildingPlanSet(value);
            if (set != null) {
                ((List<BuildingPlanSet>) field.get(targetClass)).add(set);
            } else {
                MillLog.error(culture, "Unknown building plan set: " + value);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            if (rawValue instanceof List) {
                List<?> list = (List<?>) rawValue;
                List<String> results = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof BuildingPlanSet) {
                        results.add(((BuildingPlanSet) item).key);
                    } else if (item instanceof String) {
                        results.add((String) item);
                    }
                }
                return results;
            }
            return new ArrayList<>();
        }
    }

    public static class BuildingSetIO extends CultureValueIO {
        public BuildingSetIO() {
            this.description = "A building from the current culture. One allowed.";
        }

        @Override
        public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
            BuildingPlanSet set = culture.getBuildingPlanSet(value);
            if (set != null) {
                field.set(targetClass, set);
            } else {
                MillLog.error(culture, "Unknown building plan set: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            if (rawValue instanceof BuildingPlanSet) {
                return createListFromValue(((BuildingPlanSet) rawValue).key);
            } else if (rawValue instanceof String) {
                return createListFromValue((String) rawValue);
            }
            return createListFromValue("");
        }
    }

    public static class ShopIO extends CultureValueIO {
        public ShopIO() {
            this.description = "A shop from the current culture.";
        }

        @Override
        public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
            value = value.toLowerCase().trim();
            if (culture.shopBuys.containsKey(value) || culture.shopSells.containsKey(value)) {
                field.set(targetClass, value);
            } else {
                MillLog.error(culture, "Unknown shop: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            String value = (String) rawValue;
            return createListFromValue(value);
        }
    }

    public static class VillagerAddIO extends CultureValueIO {
        public VillagerAddIO() {
            this.description = "A villager type from the current culture. Multiple lines allowed.";
        }

        @Override
        @SuppressWarnings("unchecked")
        public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
            value = value.toLowerCase().trim();
            if (culture.getVillagerType(value) != null) {
                ((List<String>) field.get(targetClass)).add(value);
            } else {
                MillLog.error(culture, "Unknown villager type: " + value);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<String> writeValue(Object rawValue) throws Exception {
            return (List<String>) rawValue;
        }
    }

    public static class WallIO extends CultureValueIO {
        public WallIO() {
            this.description = "A wall type from the current culture. One allowed.";
        }

        @Override
        public void readValueCulture(Culture culture, Object targetClass, Field field, String value) throws Exception {
            org.millenaire.common.culture.WallType wall = culture.getWallType(value.toLowerCase().trim());
            if (wall != null) {
                field.set(targetClass, wall);
            } else {
                MillLog.error(culture, "Unknown wall type: " + value);
            }
        }

        @Override
        public List<String> writeValue(Object rawValue) throws Exception {
            if (rawValue instanceof org.millenaire.common.culture.WallType) {
                return createListFromValue(((org.millenaire.common.culture.WallType) rawValue).key);
            } else if (rawValue instanceof String) {
                return createListFromValue((String) rawValue);
            }
            return createListFromValue("");
        }
    }
}
