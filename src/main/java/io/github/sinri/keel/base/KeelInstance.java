package io.github.sinri.keel.base;

import io.github.sinri.keel.base.annotations.TechnicalPreview;
import io.github.sinri.keel.base.async.KeelAsyncMixin;
import io.github.sinri.keel.base.configuration.KeelConfigElement;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * As of 4.0.0, make it final and implement KeelAsyncMixin.
 *
 * @since 3.1.0
 *
 */
public final class KeelInstance implements KeelAsyncMixin {
    public final static KeelInstance Keel;

    static {
        // As of 4.1.3
        String loggingProperty = System.getProperty("vertx.logger-delegate-factory-class-name");
        if (loggingProperty == null) {
            // 显式设置 Vert.x 日志提供者，避免自动探测失败导致 LoggerFactory 初始化异常
            // 必须在任何 Vert.x 类被加载之前设置此属性
            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.JULLogDelegateFactory");
        }

        Keel = new KeelInstance();
    }

    @NotNull
    private final KeelConfigElement configuration;
    //private final Map<String, Object> register = new ConcurrentHashMap<>();
    @Nullable
    private ClusterManager clusterManager;
    @Nullable
    private Vertx vertx;
    @NotNull
    private LoggerFactory loggerFactory;

    private KeelInstance() {
        this.configuration = new KeelConfigElement("");
        this.loggerFactory = StdoutLoggerFactory.getInstance();
    }

    @NotNull
    public KeelConfigElement getConfiguration() {
        return configuration;
    }

    @NotNull
    public LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public KeelInstance setLoggerFactory(@NotNull LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
        return this;
    }

    @Nullable
    public String config(@NotNull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        KeelConfigElement keelConfigElement = this.getConfiguration().extract(split);
        if (keelConfigElement == null) {
            return null;
        }
        return keelConfigElement.getValueAsString();
    }

    @NotNull
    public Vertx getVertx() {
        return Objects.requireNonNull(vertx);
    }

    @Nullable
    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public boolean isVertxInitialized() {
        return vertx != null;
    }

    public Future<Void> initializeVertx(@NotNull VertxOptions vertxOptions) {
        return initializeVertx(vertxOptions, null);
    }

    public Future<Void> initializeVertx(
            @NotNull VertxOptions vertxOptions,
            @Nullable ClusterManager clusterManager
    ) {
        if (isVertxInitialized()) {
            throw new IllegalStateException("Vertx has been initialized!");
        }
        this.clusterManager = clusterManager;
        if (this.clusterManager == null) {
            this.vertx = Vertx.builder().with(vertxOptions).withClusterManager(null).build();
            return Future.succeededFuture();
        } else {
            return Vertx.builder().with(vertxOptions).withClusterManager(clusterManager).buildClustered()
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
        this.clusterManager = null;
        this.vertx = Vertx.builder().with(vertxOptions).build();
    }

    /**
     * This method is designed for Unit Test with JUnit5, in {@code @BeforeEach} methods or constructor.
     * <p>
     * Do not call this method in your own code!
     *
     * @since 4.1.1
     */
    @TechnicalPreview(since = "4.1.1")
    public void initializeVertx(@NotNull Vertx vertx) {
        if (this.vertx != null && !Objects.equals(vertx, this.vertx)) {
            // Keel.getLogger().info("Re-initialize Vertx from " + this.vertx + " to " + vertx + ".");
            this.vertx.close();
        }
        this.vertx = vertx;
    }

    public boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    //    /**
    //     * @since 4.0.2 To acquire an instant logger for those logs without designed topic. By default, it is print to
    //     *         stdout and only WARNING and above may be recorded. If you want to debug locally, just get it and reset
    //     *         its visible level.
    //     */
    //    @Nonnull
    //    public EventRecorder<String> getLogger() {
    //        return Objects.requireNonNull(logger);
    //    }
    //
    //    public void setLogger(@Nonnull EventRecorder<String> logger) {
    //        this.logger = logger;
    //    }

    public Future<Void> gracefullyClose(@NotNull io.vertx.core.Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future()
                      .compose(v -> getVertx().close())
                      .compose(closed -> {
                          this.vertx = null;
                          this.clusterManager = null;
                          return Future.succeededFuture();
                      });
    }

    public Future<Void> close() {
        return gracefullyClose(Promise::complete);
    }

    //    public void saveToRegister(@Nonnull String name, @Nullable Object anything) {
    //        if (anything == null) {
    //            register.remove(name);
    //        } else {
    //            register.put(name, anything);
    //        }
    //    }
    //
    //    @Nullable
    //    @SuppressWarnings("unchecked")
    //    public <R> R getFromRegister(String name) {
    //        var r = register.get(name);
    //        if (r == null) return null;
    //        return (R) r;
    //    }
}
