package io.github.sinri.keel.base.internal;

import io.github.sinri.keel.logger.api.LateObject;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;

/**
 * 提供共享的 Vertx 实例的存储。
 */
@NullMarked
public final class SharedVertxStorage {
    private static final LateObject<Vertx> lateVertx = new LateObject<>();

    /**
     * 获取共享的 Vertx 实例。
     *
     * @return 共享的 Vertx 实例
     * @throws IllegalStateException 如果 Vertx 实例尚未初始化
     */
    public static Vertx getVertx() throws IllegalStateException {
        return lateVertx.get();
    }

    /**
     * 设置共享的 Vertx 实例。
     *
     * @param vertx 共享的 Vertx 实例
     * @throws IllegalStateException 如果 Vertx 实例已初始化
     */
    public static void setVertx(Vertx vertx) throws IllegalStateException {
        lateVertx.set(vertx);
    }
}
