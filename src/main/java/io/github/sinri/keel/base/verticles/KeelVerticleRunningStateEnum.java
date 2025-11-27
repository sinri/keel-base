package io.github.sinri.keel.base.verticles;

/**
 * Verticle 运行状态机枚举。
 * <p>
 * 在{@link KeelVerticle}中使用此枚举，通过{@link KeelVerticle#getRunningState()}方法表示这个 Verticle 处于部署前、运行中、解除部署后三个阶段中的哪一个。
 * 在{@link AbstractKeelVerticle}中提供了默认实现。
 *
 * @since 5.0.0
 */
public enum KeelVerticleRunningStateEnum {
    BEFORE_RUNNING,
    RUNNING,
    AFTER_RUNNING,
}
