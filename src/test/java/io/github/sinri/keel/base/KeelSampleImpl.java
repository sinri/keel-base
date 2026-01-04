package io.github.sinri.keel.base;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.configuration.NotConfiguredException;
import io.github.sinri.keel.base.logger.factory.StdoutLoggerFactory;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Keel 体系的运行时锚点。
 *
 * @since 5.0.0
 */
@NullMarked
public final class KeelSampleImpl implements Keel {

    static {
        String loggingProperty = System.getProperty("vertx.logger-delegate-factory-class-name");
        if (loggingProperty == null) {
            // 显式设置 Vert.x 日志提供者，避免自动探测失败导致 LoggerFactory 初始化异常
            // 必须在任何 Vert.x 类被加载之前设置此属性
            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.JULLogDelegateFactory");
        }
    }

    /**
     * 测试用的单例 Keel 实例。
     */
    public static final KeelSampleImpl Keel = new KeelSampleImpl();

    private final ConfigElement configuration;
    @Nullable
    private Vertx vertx;
    private LoggerFactory loggerFactory;

    /**
     * 私有构造函数，用于创建单例实例。
     * <p>
     * 初始化配置树和默认的日志工厂。
     */
    public KeelSampleImpl() {
        this.configuration = new ConfigElement("");
        this.loggerFactory = StdoutLoggerFactory.getInstance();
    }

    /**
     * 获取配置树。
     *
     * @return 配置树实例
     */
    public ConfigElement getConfiguration() {
        return configuration;
    }

    /**
     * 获取日志工厂。
     *
     * @return 当前使用的日志工厂实例
     */
    public LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    /**
     * 设置日志工厂。
     *
     * @param loggerFactory 要设置的日志工厂
     */
    public void setLoggerFactory(LoggerFactory loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    /**
     * 获取 Vert.x 实例。
     * <p>
     * 如果 Vert.x 尚未初始化，将抛出 NullPointerException。
     *
     * @return Vert.x 实例
     * @throws NullPointerException 如果 Vert.x 尚未初始化
     */
    @Override
    public Vertx getVertx() {
        return Objects.requireNonNull(vertx);
    }


    /**
     * 检查 Vert.x 是否已初始化。
     *
     * @return 如果 Vert.x 已初始化则返回 true，否则返回 false
     */
    public boolean isVertxInitialized() {
        return vertx != null;
    }

    /**
     * 初始化 Vert.x 实例（非集群模式）。
     *
     * @param vertxOptions Vert.x 配置选项
     * @return 异步完成结果，表示初始化操作的完成状态
     * @throws IllegalStateException 如果 Vert.x 已经被初始化
     */
    public Future<Void> initializeVertx(VertxOptions vertxOptions) {
        return initializeVertx(vertxOptions, null);
    }

    /**
     * 初始化 Vert.x 实例（支持集群模式）。
     * <p>
     * 如果提供了集群管理器，将创建集群模式的 Vert.x 实例；否则创建单机模式的实例。
     *
     * @param vertxOptions   Vert.x 配置选项
     * @param clusterManager 集群管理器，如果为 null 则创建单机模式实例
     * @return 异步完成结果，表示初始化操作的完成状态
     * @throws IllegalStateException 如果 Vert.x 已经被初始化
     */
    public Future<Void> initializeVertx(
            VertxOptions vertxOptions,
            @Nullable ClusterManager clusterManager
    ) {
        if (isVertxInitialized()) {
            throw new IllegalStateException("Vertx has been initialized!");
        }
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

    /**
     * 同步初始化单机模式的 Vert.x 实例。
     * <p>
     * 此方法会阻塞当前线程直到 Vert.x 实例创建完成。
     *
     * @param vertxOptions Vert.x 配置选项
     * @throws IllegalStateException 如果 Vert.x 已经被初始化
     */
    public void initializeVertxStandalone(VertxOptions vertxOptions) {
        if (isVertxInitialized()) {
            throw new IllegalStateException("Vertx has been initialized!");
        }
        this.vertx = Vertx.builder().with(vertxOptions).build();
    }

    /**
     * 使用已存在的 Vert.x 实例进行初始化。
     * <p>
     * 此方法设计用于特定的自动使用场景，例如：
     * <ul>
     *   <li>JUnit5 单元测试中的 {@code @BeforeEach} 方法或构造函数</li>
     *   <li>Vert.x Application Launcher 的生命周期钩子</li>
     * </ul>
     * <p>
     * 如果已存在不同的 Vert.x 实例，将先关闭旧实例再设置新实例。
     * <p>
     * <strong>警告：不要在自己的代码中调用此方法！</strong>
     *
     * @param vertx 要使用的 Vert.x 实例
     */
    public void initializeVertx(Vertx vertx) {
        if (this.vertx != null && !Objects.equals(vertx, this.vertx)) {
            this.vertx.close();
        }
        this.vertx = vertx;
    }

    /**
     * 检查当前是否运行在 Vert.x 集群模式中。
     *
     * @return 如果运行在集群模式中则返回 true，否则返回 false
     */
    public boolean isRunningInVertxCluster() {
        return getVertx().isClustered();
    }

    /**
     * 优雅地关闭 Keel 实例。
     * <p>
     * 此方法先执行提供的清理处理程序，然后关闭 Vert.x 实例。
     * 清理处理程序可以用于执行应用特定的清理逻辑。
     *
     * @param promiseHandler 清理处理程序，用于执行应用特定的清理逻辑
     * @return 异步完成结果，表示关闭操作的完成状态
     */
    public Future<Void> gracefullyClose(io.vertx.core.Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future()
                      .compose(v -> getVertx().close())
                      .compose(closed -> {
                          this.vertx = null;
                          return Future.succeededFuture();
                      });
    }

    /**
     * 关闭 Keel 实例。
     * <p>
     * 此方法会立即关闭 Vert.x 实例，不执行额外的清理逻辑。
     *
     * @return 异步完成结果，表示关闭操作的完成状态
     */
    public Future<Void> close() {
        return gracefullyClose(Promise::complete);
    }
}
