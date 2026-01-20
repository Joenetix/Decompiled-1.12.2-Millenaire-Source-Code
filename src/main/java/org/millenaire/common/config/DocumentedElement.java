package org.millenaire.common.config;

import java.io.BufferedWriter;
import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.millenaire.common.utilities.MillCommonUtilities;
import org.millenaire.common.utilities.MillLog;

/**
 * Represents a documented element with a key and description.
 * Used for generating help files for configuration options, tags, etc.
 * Ported from 1.12.2 to 1.20.1.
 */
public class DocumentedElement implements Comparable<DocumentedElement> {

    public final String key;
    public final String description;

    public DocumentedElement(String key, String description) {
        this.key = key;
        this.description = description;
    }

    @Override
    public int compareTo(DocumentedElement other) {
        return this.key.compareTo(other.key);
    }

    /**
     * Generate a help file from a map of items with Documentation annotations.
     */
    public static void generateClassHelp(String directoryName, String fileName, Map<?, ?> items, String explanations) {
        try {
            File directory;
            if (directoryName != null) {
                directory = new File(MillCommonUtilities.getMillenaireHelpDir(), directoryName);
            } else {
                directory = MillCommonUtilities.getMillenaireHelpDir();
            }

            directory.mkdirs();
            File file = new File(directory, fileName);
            BufferedWriter writer = MillCommonUtilities.getWriter(file);
            writer.write(explanations + "\n\n");

            List<DocumentedElement> tags = new ArrayList<>();

            for (Object rawKey : items.keySet()) {
                String key = (String) rawKey;
                Object value = items.get(key);
                if (value.getClass().isAnnotationPresent(Documentation.class)) {
                    Documentation doc = value.getClass().getAnnotation(Documentation.class);
                    tags.add(new DocumentedElement(key, doc.value()));
                } else {
                    MillLog.warning(null, "No description for: " + key);
                }
            }

            Collections.sort(tags);

            for (DocumentedElement tag : tags) {
                writer.write(tag.key + ": " + tag.description + "\n\n");
            }

            writer.close();
        } catch (Exception e) {
            MillLog.printException("Could not write help file: ", e);
        }
    }

    /**
     * Generate help files for static tags in a class.
     */
    public static void generateStaticTagsHelp(String directoryName, String fileName, Class<?> targetClass,
            String explanations) {
        try {
            File directory;
            if (directoryName != null) {
                directory = new File(MillCommonUtilities.getMillenaireHelpDir(), directoryName);
            } else {
                directory = MillCommonUtilities.getMillenaireHelpDir();
            }

            directory.mkdirs();
            File file = new File(directory, fileName);
            BufferedWriter writer = MillCommonUtilities.getWriter(file);
            writer.write(explanations + "\n\n");

            List<DocumentedElement> tags = new ArrayList<>();

            for (Field field : targetClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Documentation.class) && Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    if (value != null) {
                        Documentation doc = field.getAnnotation(Documentation.class);
                        tags.add(new DocumentedElement(value.toString(), doc.value()));
                    }
                }
            }

            Collections.sort(tags);

            for (DocumentedElement tag : tags) {
                writer.write(tag.key + ": " + tag.description + "\n\n");
            }

            writer.close();
        } catch (Exception e) {
            MillLog.printException("Could not write tags description file: ", e);
        }
    }

    /**
     * Generate all help files for the mod.
     * Called during initialization if generateHelpData is enabled.
     */
    public static void generateHelpFiles() {
        // TODO: Call when VillagerType and BuildingTags are fully ported
        // generateStaticTagsHelp(null, "Villager Types Tags.txt", VillagerType.class,
        // "List of tags for villager types that changes their behaviours.");
        // generateStaticTagsHelp(null, "Building Tags.txt", BuildingTags.class,
        // "List of tags for buildings that trigger special features.");
        MillLog.minor(null, "Help file generation requested (pending full culture port)");
    }

    /**
     * Annotation for documenting fields and classes.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD, ElementType.TYPE })
    public @interface Documentation {
        String value() default "";
    }
}
