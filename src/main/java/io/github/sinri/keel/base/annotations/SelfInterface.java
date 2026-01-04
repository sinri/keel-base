package io.github.sinri.keel.base.annotations;

import org.jspecify.annotations.NullMarked;

/**
 * 本接口定义了一种可用于链式调用的实体。
 */
@NullMarked
public interface SelfInterface<T> {
    /**
     *
     * @return 类自身
     */
    @SuppressWarnings("unchecked")
    default T getImplementation() {
        return (T) this;
    }
}
