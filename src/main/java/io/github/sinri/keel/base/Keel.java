package io.github.sinri.keel.base;


import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 需要被运行时实例初始化（比如在 keel-app 中）以发挥作用。
 * <p>
 * Make it a vertx wrapper that could be created as needed.
 *
 * @since 5.0.0
 * @deprecated for most usage, use {@link KeelAsyncMixin} directly
 */
@Deprecated(since = "5.0.0", forRemoval = true)
@NullMarked
public interface Keel extends KeelAsyncMixin {
    static Keel wrap(Vertx vertx) {
        return () -> vertx;
    }

    /**
     * @deprecated use {@link ConfigElement#root()} instead
     */
    @Deprecated(since = "5.0.0")
    default ConfigElement getConfiguration() {
        return ConfigElement.root();
    }

    /**
     * @deprecated use {@link LoggerFactory#getShared()} instead
     */
    @Deprecated(since = "5.0.0")
    default LoggerFactory getLoggerFactory() {
        return LoggerFactory.getShared();
    }

    /**
     * @deprecated use {@link ConfigElement#readString(String)} instead
     */
    @Deprecated(since = "5.0.0")
    default @Nullable String config(String dotJoinedKeyChain) {
        return getConfiguration().readString(dotJoinedKeyChain);
    }

    /**
     * @deprecated use {@link Vertx#isClustered()} of {@link VertxHolder#getVertx()} instead
     */
    @Deprecated(since = "5.0.0")
    default boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    default Future<Void> close() {
        return getVertx().close();
    }
}

