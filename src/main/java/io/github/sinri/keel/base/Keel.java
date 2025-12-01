package io.github.sinri.keel.base;


import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.ConfigTree;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Keel extends KeelAsyncMixin {

    @NotNull
    ConfigTree getConfiguration();

    @NotNull
    LoggerFactory getLoggerFactory();

    void setLoggerFactory(@NotNull LoggerFactory loggerFactory);

    @Nullable
    default String config(@NotNull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        try {
            return this.getConfiguration().readString(List.of(split));
        } catch (ConfigTree.NotConfiguredException e) {
            return null;
        }
    }

    @NotNull
    Vertx getVertx();

    default boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    @NotNull
    Future<Void> close();
}

