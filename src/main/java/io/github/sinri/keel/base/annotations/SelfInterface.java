package io.github.sinri.keel.base.annotations;

import org.jetbrains.annotations.NotNull;

/**
 * 本接口定义了一种可用于链式调用的实体。
 */
public interface SelfInterface<T> {
    /**
     *
     * @return 类自身
     */
    @SuppressWarnings("unchecked")
    @NotNull
    default T getImplementation() {
        return (T) this;
    }
}
