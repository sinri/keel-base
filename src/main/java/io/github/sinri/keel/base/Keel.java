package io.github.sinri.keel.base;


import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 需要被运行时实例初始化（比如在 keel-app 中）以发挥作用。
 *
 * @since 5.0.0
 */
public interface Keel extends KeelAsyncMixin {

    @NotNull ConfigElement getConfiguration();

    @NotNull LoggerFactory getLoggerFactory();

    /**
     *
     * @see Keel#getConfiguration()
     * @deprecated use {@link ConfigElement#readString(String)} instead
     */
    @Deprecated(since = "5.0.0")
    default @Nullable String config(@NotNull String dotJoinedKeyChain) {
        return getConfiguration().readString(dotJoinedKeyChain);
    }

    default boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    default @NotNull Future<Void> close() {
        return getVertx().close();
    }
}

