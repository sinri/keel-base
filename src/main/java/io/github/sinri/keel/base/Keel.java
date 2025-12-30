package io.github.sinri.keel.base;


import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.NodeBasedConfigTree;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 需要被运行时实例初始化（比如在 keel-app 中）以发挥作用。
 *
 * @since 5.0.0
 */
public interface Keel extends KeelAsyncMixin {

    @NotNull NodeBasedConfigTree getConfiguration();

    @NotNull LoggerFactory getLoggerFactory();

    default @Nullable String config(@NotNull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        try {
            return this.getConfiguration().readString(List.of(split));
        } catch (NotConfiguredException e) {
            return null;
        }
    }

    default boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    @NotNull
    default Future<Void> close() {
        return getVertx().close();
    }
}

