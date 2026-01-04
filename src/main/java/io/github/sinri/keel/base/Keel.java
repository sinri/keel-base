package io.github.sinri.keel.base;


import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 需要被运行时实例初始化（比如在 keel-app 中）以发挥作用。
 *
 * @since 5.0.0
 */
@NullMarked
public interface Keel extends KeelAsyncMixin {

    ConfigElement getConfiguration();

    LoggerFactory getLoggerFactory();

    /**
     *
     * @see Keel#getConfiguration()
     * @deprecated use {@link ConfigElement#readString(String)} instead
     */
    @Deprecated(since = "5.0.0")
    default @Nullable String config(String dotJoinedKeyChain) {
        return getConfiguration().readString(dotJoinedKeyChain);
    }

    default boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    default Future<Void> close() {
        return getVertx().close();
    }
}

