package io.github.sinri.keel.base.annotations;

import java.lang.annotation.*;

/**
 * 本注解表示对应类、方法、字段等在框架实现内尚处于技术预览阶段，应谨慎用于业务。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE,
        ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER
})
public @interface TechnicalPreview {
    /**
     * 指定该特性或组件从哪个版本开始处于技术预览阶段。
     *
     * @return 表示技术预览开始时的版本字符串
     */
    String since() default "";

    /**
     * 提供关于技术预览特性的通知或附加信息。
     *
     * @return 包含通知或附加信息的字符串；如果未提供通知则返回空字符串
     */
    String notice() default "";
}
