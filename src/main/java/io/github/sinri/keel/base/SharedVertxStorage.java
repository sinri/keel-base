package io.github.sinri.keel.base;

import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 提供共享的 Vertx 实例的存储。
 */
@NullMarked
public final class SharedVertxStorage {
    private static @Nullable Vertx sharedVertx;

    /**
     * 获取共享的 Vertx 实例。
     *
     * @return 共享的 Vertx 实例
     * @throws IllegalStateException 如果 Vertx 实例尚未初始化
     */
    public static synchronized Vertx get() throws IllegalStateException {
        if (sharedVertx != null) {
            return sharedVertx;
        }
        throw new IllegalStateException("Vertx is not initialized yet!");
    }

    /**
     * 设置共享的 Vertx 实例。
     *
     * @param vertx 共享的 Vertx 实例
     * @throws IllegalStateException 如果 Vertx 实例已初始化
     */
    public static synchronized void set(Vertx vertx) throws IllegalStateException {
        if (sharedVertx == null) {
            sharedVertx = vertx;
        }
        throw new IllegalStateException("Vertx is already initialized!");
    }

    /**
     * 强行重新设置共享的 Vertx 实例，如果已经设置了 Vertx 实例会被关闭。
     *
     * @param vertx 共享的 Vertx 实例
     */
    public static synchronized void forceSet(Vertx vertx) throws IllegalStateException {
        if (sharedVertx != null) {
            sharedVertx.close();
        }
        sharedVertx = vertx;
    }
}
