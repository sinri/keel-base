package io.github.sinri.keel.base.verticles;

import org.jspecify.annotations.NullMarked;

/**
 * Verticle 运行状态机枚举。
 * <p>
 * 在{@link KeelVerticle}中使用此枚举，
 * 通过{@link KeelVerticle#getRunningState()}方法表示这个 Verticle 处于
 * {@code 部署前}、{@code 运行中}、{@code 解除部署后}、{@code 启动失败}
 * 这四个阶段中的哪一个。
 * 在{@link AbstractKeelVerticle}中提供了默认实现。
 *
 * @since 5.0.0
 */
@NullMarked
public enum KeelVerticleRunningStateEnum {
    BEFORE_RUNNING,
    RUNNING,
    AFTER_RUNNING,
    RUNNING_FAILED,
}
