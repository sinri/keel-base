package io.github.sinri.keel.base;

import io.github.sinri.keel.base.internal.SharedVertxStorage;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

/**
 * 项目入口类应实现本接口以提供全局共享Vertx实例的初始化和获取功能。
 * <p>
 *
 * @since 5.0.0
 */
@NullMarked
public interface VertxInitializer extends SharedVertxHolder {
    /**
     * 初始化共享的 Vertx 实例。
     *
     * @param vertx 全局共享的 Vertx 实例
     * @throws IllegalStateException 如果 Vertx 实例已初始化
     */
    default void initializeVertx(Vertx vertx) throws IllegalStateException {
        SharedVertxStorage.setVertx(vertx);
    }
}

