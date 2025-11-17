package io.github.sinri.keel.base.verticles;

/**
 * Verticle 运行状态机枚举。
 * <p>
 * 在{@link KeelVerticle}中定义，在{@link AbstractKeelVerticle}默认实现。
 *
 * @since 5.0.0
 */
public enum KeelVerticleRunningStateEnum {
    BEFORE_RUNNING,
    RUNNING,
    AFTER_RUNNING,
}
