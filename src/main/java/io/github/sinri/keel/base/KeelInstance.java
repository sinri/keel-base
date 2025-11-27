package io.github.sinri.keel.base;

import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.ConfigTree;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Keel 体系的运行时锚点。
 *
 * @since 5.0.0
 */
public final class KeelInstance implements KeelAsyncMixin {
    @NotNull
    public final static KeelInstance Keel;

    static {
        String loggingProperty = System.getProperty("vertx.logger-delegate-factory-class-name");
        if (loggingProperty == null) {
            // 显式设置 Vert.x 日志提供者，避免自动探测失败导致 LoggerFactory 初始化异常
            // 必须在任何 Vert.x 类被加载之前设置此属性
            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.JULLogDelegateFactory");
        }
        Keel = new KeelInstance();
    }

    @NotNull
    private final ConfigTree configuration;
    //    @Nullable
    //    private ClusterManager clusterManager;
    @Nullable
    private Vertx vertx;
    @NotNull
    private LoggerFactory loggerFactory;

    private KeelInstance() {
        this.configuration = new ConfigTree();
        this.loggerFactory = StdoutLoggerFactory.getInstance();
    }

    @NotNull
    public ConfigTree getConfiguration() {
        return configuration;
    }

    @NotNull
    public LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @NotNull
    public KeelInstance setLoggerFactory(@NotNull LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
        return this;
    }

    @Nullable
    public String config(@NotNull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        try {
            return this.getConfiguration().readString(List.of(split));
        } catch (ConfigTree.NotConfiguredException e) {
            return null;
        }
    }

    @Override
    @NotNull
    public Vertx getVertx() {
        return Objects.requireNonNull(vertx);
    }

    //    @Nullable
    //    public ClusterManager getClusterManager() {
    //        return clusterManager;
    //    }

    public boolean isVertxInitialized() {
        return vertx != null;
    }

    @NotNull
    public Future<Void> initializeVertx(@NotNull VertxOptions vertxOptions) {
        return initializeVertx(vertxOptions, null);
    }

    @NotNull
    public Future<Void> initializeVertx(
            @NotNull VertxOptions vertxOptions,
            @Nullable ClusterManager clusterManager
    ) {
        if (isVertxInitialized()) {
            throw new IllegalStateException("Vertx has been initialized!");
        }
        //        this.clusterManager = clusterManager;
        if (clusterManager == null) {
            this.vertx = Vertx.builder().with(vertxOptions).withClusterManager(null).build();
            return Future.succeededFuture();
        } else {
            return Vertx.builder()
                        .with(vertxOptions)
                        .withClusterManager(clusterManager)
                        .buildClustered()
                        .compose(vertx -> {
                            this.vertx = vertx;
                            return Future.succeededFuture();
                        });
        }
    }

    public void initializeVertxStandalone(@NotNull VertxOptions vertxOptions) {
        if (isVertxInitialized()) {
            throw new IllegalStateException("Vertx has been initialized!");
        }
        //        this.clusterManager = null;
        this.vertx = Vertx.builder().with(vertxOptions).build();
    }

    /**
     * This method is designed for certain automatic usage,
     * such as,
     * Unit Test with JUnit5, in {@code @BeforeEach} methods or constructor;
     * Vert.x Application Launcher, in life cycle hooks.
     * <p>
     * Do not call this method in your own code!
     */
    public void initializeVertx(@NotNull Vertx vertx) {
        if (this.vertx != null && !Objects.equals(vertx, this.vertx)) {
            this.vertx.close();
        }
        this.vertx = vertx;
    }

    public boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    @NotNull
    public Future<Void> gracefullyClose(@NotNull io.vertx.core.Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future()
                      .compose(v -> getVertx().close())
                      .compose(closed -> {
                          this.vertx = null;
                          //                          this.clusterManager = null;
                          return Future.succeededFuture();
                      });
    }

    @NotNull
    public Future<Void> close() {
        return gracefullyClose(Promise::complete);
    }
}
