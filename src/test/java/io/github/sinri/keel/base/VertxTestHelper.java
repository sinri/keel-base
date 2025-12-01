package io.github.sinri.keel.base;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Vertx测试辅助类，提供Vertx测试环境设置和清理。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
public class VertxTestHelper {
    /**
     * 初始化Vertx实例用于测试。
     *
     * @param vertx Vertx实例
     */
    public static void setupVertx(Vertx vertx) {
        KeelInstance.Keel.initializeVertx(vertx);
    }

    /**
     * 清理Vertx实例。
     *
     * @param testContext 测试上下文
     */
    public static void cleanupVertx(VertxTestContext testContext) {
        if (KeelInstance.Keel.isVertxInitialized()) {
            KeelInstance.Keel.close()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        testContext.completeNow();
                    } else {
                        testContext.failNow(ar.cause());
                    }
                });
        } else {
            testContext.completeNow();
        }
    }

    /**
     * 创建测试用的VertxOptions。
     *
     * @return VertxOptions实例
     */
    public static VertxOptions createTestVertxOptions() {
        return new VertxOptions()
                .setEventLoopPoolSize(2)
                .setWorkerPoolSize(2);
    }
}

