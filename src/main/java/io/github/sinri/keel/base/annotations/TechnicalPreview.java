package io.github.sinri.keel.base.annotations;

import java.lang.annotation.*;

/**
 * This annotation is designed while not fully tested in production environments.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE,
        ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER
})
public @interface TechnicalPreview {
    /**
     * Specifies the version since when this feature or component is in technical preview.
     *
     * @return the version string indicating when the technical preview started
     */
    String since() default "";

    /**
     * Provides a notice or additional information about the technical preview feature.
     *
     * @return a string containing the notice or additional information; an empty string if no notice is provided
     */
    String notice() default "";
}
