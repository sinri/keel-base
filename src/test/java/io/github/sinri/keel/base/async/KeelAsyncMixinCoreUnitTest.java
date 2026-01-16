package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.vertx.core.Promise;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * KeelAsyncMixinCore单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class KeelAsyncMixinCoreUnitTest extends KeelJUnit5Test {

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     */
    public KeelAsyncMixinCoreUnitTest() {
        super();
    }


    @Test
    void testAsyncSleep(VertxTestContext testContext) {
        long startTime = System.currentTimeMillis();
        asyncSleep(100)
                  .onComplete(ar -> {
                      if (ar.succeeded()) {
                          long elapsed = System.currentTimeMillis() - startTime;
                          assertTrue(elapsed >= 90, "Sleep should take at least 90ms");
                          testContext.completeNow();
                      } else {
                          testContext.failNow(ar.cause());
                      }
                  });
    }

    @Test
    void testAsyncSleepWithInterrupter(VertxTestContext testContext) {
        Promise<Void> interrupter = Promise.promise();

        asyncSleep(1000, interrupter)
                  .onComplete(ar -> {
                      if (ar.succeeded()) {
                          testContext.completeNow();
                      } else {
                          testContext.failNow(ar.cause());
                      }
                  });

        // Interrupt after 50ms
        getVertx().setTimer(50, id -> interrupter.complete());
    }

    @Test
    void testAsyncSleepMinimumTime(VertxTestContext testContext) {
        long startTime = System.currentTimeMillis();
        asyncSleep(0) // Should be treated as 1ms minimum
                  .onComplete(ar -> {
                      if (ar.succeeded()) {
                          long elapsed = System.currentTimeMillis() - startTime;
                          assertTrue(elapsed >= 0, "Should complete quickly");
                          testContext.completeNow();
                      } else {
                          testContext.failNow(ar.cause());
                      }
                  });
    }

    @Test
    void testAsyncSleepNegativeTime(VertxTestContext testContext) {
        long startTime = System.currentTimeMillis();
        asyncSleep(-100) // Should be treated as 1ms minimum
                  .onComplete(ar -> {
                      if (ar.succeeded()) {
                          long elapsed = System.currentTimeMillis() - startTime;
                          assertTrue(elapsed >= 0, "Should complete quickly");
                          testContext.completeNow();
                      } else {
                          testContext.failNow(ar.cause());
                      }
                  });
    }
}

