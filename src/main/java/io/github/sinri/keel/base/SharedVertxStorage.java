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
     * 确保所给定的 Vertx 实例为共享的 Vertx 实例。
     * <p>
     * 当当前共享的 Vertx 不存在时，设置之；
     * 当当前共享的 Vertx 与给定的 Vertx 实例不一致时，关闭当前 Vertx 实例并重新设置。
     *
     * @param vertx 需要共享的 Vertx 实例
     * @throws IllegalStateException 如果发生意外
     */
    public static synchronized void ensure(Vertx vertx) throws IllegalStateException {
        if (sharedVertx == null) {
            sharedVertx = vertx;
        } else if (sharedVertx != vertx) {
            System.err.println("Vertx is changed from " + sharedVertx + " to " + vertx);
            sharedVertx.close();
            sharedVertx = vertx;
        }
    }
}
