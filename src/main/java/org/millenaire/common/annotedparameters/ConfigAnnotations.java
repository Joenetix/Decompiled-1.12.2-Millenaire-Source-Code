package org.millenaire.common.annotedparameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotations for configuration fields.
 * Ported from 1.12.2 to 1.20.1.
 */
public class ConfigAnnotations {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ConfigField {
        String defaultValue() default "";

        String fieldCategory() default "";

        String paramName() default "";

        AnnotedParameter.ParameterType type();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldDocumentation {
        String explanation();

        String explanationCategory() default "";
    }
}
