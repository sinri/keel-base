package io.github.sinri.keel.base.logger.factory;

/**
 * @since 5.0.0
 */
public final class VertxLoggerDelegateFactoryWorker {
    /**
     * 在任何 Vert.x 类被加载之前，调用本方法确保 Vert.x 日志提供者配置正确。
     * <p>
     * 检查系统属性是否指定 Vert.x 日志提供者。
     * 如果没有显式设置，则指定默认的 Vert.x 日志提供者，避免自动探测失败导致 LoggerFactory 初始化异常。
     * 必须在任何 Vert.x 类被加载之前设置此属性。
     */
    public static void ensureProperty() {
        String loggingProperty = System.getProperty("vertx.logger-delegate-factory-class-name");
        if (loggingProperty == null) {
            // 显式设置 Vert.x 日志提供者，避免自动探测失败导致 LoggerFactory 初始化异常
            // 必须在任何 Vert.x 类被加载之前设置此属性
            System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.JULLogDelegateFactory");
        }
    }
}
