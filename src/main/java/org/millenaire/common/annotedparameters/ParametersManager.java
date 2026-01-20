package org.millenaire.common.annotedparameters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;
import org.millenaire.common.culture.Culture;
import org.millenaire.common.buildingplan.BuildingPlan;
import org.millenaire.common.config.MillConfigValues;
import org.millenaire.common.culture.VillageType;
import org.millenaire.common.culture.VillagerType;
import org.millenaire.common.culture.WallType;
import org.millenaire.entities.VillagerConfig;
import org.millenaire.common.goal.generic.GoalGeneric;

/**
 * Manages annotated parameters - loading from files, applying defaults, and
 * generating help docs.
 * Ported from 1.12.2 to 1.20.1.
 */
public class ParametersManager {
    private static Map<String, Map<String, List<AnnotedParameter>>> parametersCacheByCategory = new HashMap<>();
    private static Map<String, Map<String, AnnotedParameter>> parametersCache = new HashMap<>();

    private static void generateAnnotedParametersHelp(
            String directoryName, String fileName, Class<?> targetClass, String fieldCategory, boolean recursive,
            String explanations) {
        File directory;
        if (directoryName != null) {
            directory = new File(MillCommonUtilities.getMillenaireHelpDir(), directoryName);
        } else {
            directory = MillCommonUtilities.getMillenaireHelpDir();
        }

        directory.mkdirs();
        File file = new File(directory, fileName);

        try {
            BufferedWriter writer = MillCommonUtilities.getWriter(file);
            writer.write(explanations + "\n" + "\n");
            Map<String, List<AnnotedParameter>> parametersByExplanationCategory = null;
            if (!recursive) {
                parametersByExplanationCategory = getParametersByExplanationCategory(targetClass, fieldCategory);
            } else {
                for (Class<?> currentClass = targetClass; currentClass != null; currentClass = currentClass
                        .getSuperclass()) {
                    Map<String, List<AnnotedParameter>> parametersByExplanationCategoryTemp = getParametersByExplanationCategory(
                            currentClass, fieldCategory);
                    if (parametersByExplanationCategory == null) {
                        parametersByExplanationCategory = parametersByExplanationCategoryTemp;
                    } else {
                        for (String key : parametersByExplanationCategoryTemp.keySet()) {
                            if (!parametersByExplanationCategory.containsKey(key)) {
                                parametersByExplanationCategory.put(key, parametersByExplanationCategoryTemp.get(key));
                            } else {
                                parametersByExplanationCategory.get(key)
                                        .addAll(parametersByExplanationCategoryTemp.get(key));
                            }
                        }
                    }
                }
            }

            for (String category : parametersByExplanationCategory.keySet()) {
                if (category.length() > 0) {
                    writer.write("\n=== " + category + " ===" + "\n" + "\n");
                }

                for (AnnotedParameter parameter : parametersByExplanationCategory.get(category)) {
                    writer.write(parameter.configName + " (" + parameter.type.io.description + "):" + "\n");
                    writer.write(parameter.explanation + "\n");
                    if (parameter.defaultValueString != null) {
                        writer.write("Default value: " + parameter.defaultValueString + "\n");
                    }

                    writer.write("\n");
                }

                writer.write("\n");
            }

            writer.close();
        } catch (Exception var14) {
            MillLog.printException(var14);
        }
    }

    public static void generateHelpFiles() {
        generateAnnotedParametersHelp(null, "Cultures.txt", Culture.class, null, false,
                "List of valid parameters for the culture files.");
        generateAnnotedParametersHelp(null, "Village Types.txt", VillageType.class, null, false,
                "List of valid parameters for the village files.");
        generateAnnotedParametersHelp(null, "Wall Types.txt", WallType.class, null, false,
                "List of valid parameters for the wall files.");
        generateAnnotedParametersHelp(null, "Villager Types.txt", VillagerType.class, null, false,
                "List of valid parameters for the villager files.");
        generateAnnotedParametersHelp(
                null,
                "Villager Config.txt",
                VillagerConfig.class,
                null,
                false,
                "Series of parameters for various villager behaviour, such as which tools to use or what armour to wear. Every villager gets the default config applied, plus a specific one if defined.");
        generateAnnotedParametersHelp(
                null,
                "Buildings general parameters.txt",
                BuildingPlan.class,
                "init",
                false,
                "List of valid parameters for building files that applies for the entire building with the 'building.' prefix.");
        generateAnnotedParametersHelp(
                null,
                "Buildings upgrade parameters.txt",
                BuildingPlan.class,
                "upgrade",
                true,
                "List of valid parameters for building files that applies for a specific upgrade with the 'initial' or 'upgradeX' prefixes.");

        generateAnnotedParametersHelp("goals", "all goal parameters.txt",
                GoalGeneric.class, null, true,
                "List of parameters usable in all generic goals:");

        for (Class<?> genericGoalClass : GoalGeneric.GENERIC_GOAL_CLASSES) {
            try {
                String goalType = (String) genericGoalClass.getField("GOAL_TYPE").get(null);
                if (MillConfigValues.generateHelpData) {
                    generateAnnotedParametersHelp(
                            "goals", goalType + " goal parameters.txt", genericGoalClass, null, false,
                            "List of parameters usable in " + goalType + " goals:");
                }
            } catch (Exception var5) {
                MillLog.printException("Exception when generating goal help files:", var5);
            }
        }
    }

    private static String getCacheKey(Class<?> targetClass, String fieldCategory) {
        String cacheKey = targetClass.getCanonicalName();
        if (fieldCategory != null) {
            cacheKey = cacheKey + "_" + fieldCategory;
        }
        return cacheKey;
    }

    private static Map<String, AnnotedParameter> getGenericParametersForTarget(Object target, String fieldCategory) {
        Class<?> targetClass = target.getClass();

        Map<String, AnnotedParameter> parameters = null;
        while (targetClass != null) {
            Map<String, AnnotedParameter> parametersTemp = getParameters(targetClass, fieldCategory);
            if (parameters == null) {
                parameters = parametersTemp;
            } else {
                parameters.putAll(parametersTemp);
            }
            targetClass = targetClass.getSuperclass();
        }

        return parameters;
    }

    private static Map<String, AnnotedParameter> getParameters(Class<?> targetClass, String fieldCategory) {
        loadAnnotedParameters(targetClass, fieldCategory);
        String cacheKey = getCacheKey(targetClass, fieldCategory);
        return parametersCache.get(cacheKey);
    }

    private static Map<String, List<AnnotedParameter>> getParametersByExplanationCategory(Class<?> targetClass,
            String fieldCategory) {
        loadAnnotedParameters(targetClass, fieldCategory);
        String cacheKey = getCacheKey(targetClass, fieldCategory);
        return parametersCacheByCategory.get(cacheKey);
    }

    /**
     * Initialize annotated parameter data for a target object.
     * If previousTarget is null, apply default values. Otherwise, copy values from
     * previousTarget.
     */
    public static void initAnnotedParameterData(Object target, Object previousTarget, String fieldCategory,
            Culture culture) {
        Map<String, AnnotedParameter> parameters = getGenericParametersForTarget(target, fieldCategory);
        if (parameters == null)
            return;

        if (previousTarget == null) {
            for (AnnotedParameter param : parameters.values()) {
                if (param.defaultValueString != null) {
                    param.parseValue(culture, target, param.defaultValueString);
                }
            }
        } else {
            for (AnnotedParameter param : parameters.values()) {
                try {
                    Object previousValue = param.field.get(previousTarget);
                    if (previousValue != null) {
                        try {
                            param.field.setAccessible(true);
                            if (previousValue instanceof ArrayList) {
                                param.field.set(target, new ArrayList<>((List<?>) previousValue));
                            } else if (previousValue instanceof HashMap) {
                                param.field.set(target, new HashMap<>((HashMap<?, ?>) previousValue));
                            } else {
                                param.field.set(target, previousValue);
                            }
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            MillLog.printException(e);
                        }
                    }
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    MillLog.printException(e);
                }
            }
        }
    }

    /**
     * Load annotated parameter data from a file.
     */
    public static Object loadAnnotedParameterData(File file, Object target, String fieldCategory, String fileType,
            Culture culture) {
        Map<String, AnnotedParameter> parameters = getGenericParametersForTarget(target, fieldCategory);
        if (parameters == null)
            return null;

        for (AnnotedParameter param : parameters.values()) {
            if (param.defaultValueString != null) {
                param.parseValue(culture, target, param.defaultValueString);
            }
        }

        if (target instanceof DefaultValueOverloaded) {
            ((DefaultValueOverloaded) target).applyDefaultSettings();
        }

        boolean oldSeparatorWarning = false;

        try {
            BufferedReader reader = MillCommonUtilities.getReader(file);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().length() > 0 && !line.startsWith("//")) {
                    String[] temp = line.split("=");
                    if (temp.length < 2) {
                        temp = line.split(":");
                        if (temp.length >= 2) {
                            oldSeparatorWarning = true;
                        }
                    }

                    if (temp.length < 2) {
                        MillLog.error(null,
                                "Invalid line when loading " + fileType + ": " + file.getName() + ": " + line);
                    } else {
                        String key = temp[0].trim().toLowerCase();
                        String value = line.substring(key.length() + 1);
                        if (parameters.containsKey(key)) {
                            parameters.get(key).parseValue(culture, target, value);
                        } else {
                            MillLog.error(null, "Unknown line in " + fileType + ": " + file.getName() + ": " + line);
                        }
                    }
                }
            }

            reader.close();
        } catch (Exception e) {
            MillLog.printException(e);
            return null;
        }

        if (oldSeparatorWarning) {
            MillLog.minor(target, "File " + file.getAbsolutePath()
                    + " has legacy separator ( ':' instead of '='). It was loaded but should be converted.");
        }

        return target;
    }

    /**
     * Load annotated parameters from a class's fields.
     */
    private static void loadAnnotedParameters(Class<?> targetClass, String fieldCategory) {
        String cacheKey = getCacheKey(targetClass, fieldCategory);
        if (parametersCache.containsKey(cacheKey)) {
            return;
        }

        Map<String, List<AnnotedParameter>> parametersByExplanationCategory = new LinkedHashMap<>();

        for (Field field : targetClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(ConfigAnnotations.ConfigField.class)) {
                boolean shouldInclude = true;
                if (fieldCategory != null) {
                    String currentFieldCategory = field.getAnnotation(ConfigAnnotations.ConfigField.class)
                            .fieldCategory();
                    shouldInclude = fieldCategory.equals(currentFieldCategory);
                }

                if (shouldInclude) {
                    String explanationCategory = "";
                    if (field.isAnnotationPresent(ConfigAnnotations.FieldDocumentation.class)) {
                        explanationCategory = field.getAnnotation(ConfigAnnotations.FieldDocumentation.class)
                                .explanationCategory();
                    }

                    if (!parametersByExplanationCategory.containsKey(explanationCategory)) {
                        parametersByExplanationCategory.put(explanationCategory, new ArrayList<>());
                    }

                    parametersByExplanationCategory.get(explanationCategory).add(new AnnotedParameter(field));
                }
            }
        }

        Map<String, AnnotedParameter> parametersMap = new HashMap<>();

        for (List<AnnotedParameter> parameters : parametersByExplanationCategory.values()) {
            for (AnnotedParameter param : parameters) {
                if (parametersMap.containsKey(param.configName)) {
                    MillLog.error(targetClass, "Parameter present twice: " + param.configName);
                }
                parametersMap.put(param.configName, param);
            }
        }

        parametersCacheByCategory.put(cacheKey, parametersByExplanationCategory);
        parametersCache.put(cacheKey, parametersMap);
    }

    /**
     * Load prefixed annotated parameter data from a list of lines.
     */
    public static void loadPrefixedAnnotedParameterData(
            List<String> lines, String prefix, Object target, String fieldCategory, String fileType, String fileName,
            Culture culture) {
        Map<String, AnnotedParameter> parameters = getGenericParametersForTarget(target, fieldCategory);
        if (parameters == null)
            return;

        try {
            for (String line : lines) {
                if (line.trim().length() > 0 && line.startsWith(prefix + ".")) {
                    String[] temp = line.split("=");
                    if (temp.length < 2) {
                        MillLog.error(null, "Invalid line when loading " + fileType + ": " + fileName + ": " + line);
                    } else {
                        String key = temp[0].trim().toLowerCase().split("\\.")[1];
                        String value = line.substring(temp[0].length() + 1);
                        if (parameters.containsKey(key)) {
                            parameters.get(key).parseValue(culture, target, value);
                        } else {
                            MillLog.error(null, "Unknown line in " + fileType + ": " + fileName + ": " + line);
                        }
                    }
                }
            }
        } catch (Exception e) {
            MillLog.printException(e);
        }
    }

    /**
     * Write annotated parameters to a file.
     */
    public static void writeAnnotedParameterFile(File file, Object target, String fieldCategory) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Could not create file at " + file.getAbsolutePath());
            }
        }

        try {
            BufferedWriter writer = MillCommonUtilities.getWriter(file);
            writeAnnotedParameters(writer, target, fieldCategory, null, null);
            writer.close();
        } catch (Exception e) {
            MillLog.printException(e);
        }
    }

    /**
     * Write annotated parameters to a writer.
     */
    public static int writeAnnotedParameters(BufferedWriter writer, Object target, String fieldCategory,
            Object previousTarget, String prefix) throws Exception {
        Map<String, List<AnnotedParameter>> parametersByCategory = getParametersByExplanationCategory(target.getClass(),
                fieldCategory);
        if (parametersByCategory == null)
            return 0;

        int linesWritten = 0;

        for (String category : parametersByCategory.keySet()) {
            for (AnnotedParameter param : parametersByCategory.get(category)) {
                Object value = param.field.get(target);
                Object valueToWrite;
                if (value == null) {
                    valueToWrite = null;
                } else if (previousTarget == null) {
                    if (param.defaultValueString != null) {
                        List<String> valuesToWrite = param.type.io.writeValue(param.field.get(target));
                        if (valuesToWrite.size() == 1 && param.defaultValueString.equals(valuesToWrite.get(0))) {
                            valueToWrite = null;
                        } else {
                            valueToWrite = value;
                        }
                    } else if (!param.type.io.skipWritingValue(value)) {
                        valueToWrite = value;
                    } else {
                        valueToWrite = null;
                    }
                } else {
                    Object oldValue = param.field.get(previousTarget);
                    if (oldValue == null) {
                        valueToWrite = value;
                    } else if (value.equals(oldValue)) {
                        valueToWrite = null;
                    } else if (value instanceof List) {
                        List<Object> newList = new ArrayList<>((List<?>) value);
                        newList.removeAll((List<?>) oldValue);
                        valueToWrite = newList;
                    } else if (!(value instanceof Map)) {
                        valueToWrite = value;
                    } else {
                        @SuppressWarnings("unchecked")
                        Map<Object, Object> newMap = new HashMap<>((Map<?, ?>) value);
                        @SuppressWarnings("unchecked")
                        Map<Object, Object> previousMap = (Map<Object, Object>) oldValue;

                        for (Object key : previousMap.keySet()) {
                            if (newMap.containsKey(key) && newMap.get(key).equals(previousMap.get(key))) {
                                newMap.remove(key);
                            }
                        }
                        valueToWrite = newMap;
                    }
                }

                if (valueToWrite != null) {
                    for (String v : param.type.io.writeValue(valueToWrite)) {
                        if (prefix != null) {
                            writer.write(prefix + ".");
                        }
                        writer.write(param.configName + "=" + v + "\n");
                        linesWritten++;
                    }
                }
            }
        }

        return linesWritten;
    }

    /**
     * Interface for objects that need custom default value initialization.
     */
    public interface DefaultValueOverloaded {
        void applyDefaultSettings();
    }
}
