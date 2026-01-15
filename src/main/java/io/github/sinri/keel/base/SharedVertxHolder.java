package io.github.sinri.keel.base;

import io.github.sinri.keel.base.internal.SharedVertxStorage;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

/**
 * 提供共享的 Vertx 实例。
 * <p>
 * 项目入口类通过{@link VertxInitializer}实现本接口。
 * 此外用于不宜或不便额外传入 Vertx 实例参数的场景。
 *
 * @since 5.0.0
 */
@NullMarked
public interface SharedVertxHolder extends VertxHolder {
    /**
     * 获取共享的 Vertx 实例。
     *
     * @return 共享的 Vertx 实例
     * @throws IllegalStateException 如果 Vertx 实例尚未初始化
     */
    default Vertx getVertx() throws IllegalStateException {
        return SharedVertxStorage.getVertx();
    }
}
