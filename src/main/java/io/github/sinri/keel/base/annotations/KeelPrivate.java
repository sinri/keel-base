package io.github.sinri.keel.base.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Anything annotated with this is designed for internal use only.
 * Do not use directly in biz scope.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface KeelPrivate {
}
