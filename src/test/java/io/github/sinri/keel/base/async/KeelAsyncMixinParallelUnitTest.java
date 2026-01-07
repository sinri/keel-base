package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * KeelAsyncMixinParallel单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class KeelAsyncMixinParallelUnitTest extends KeelJUnit5Test {

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public KeelAsyncMixinParallelUnitTest(Vertx vertx) {
        super(vertx);
    }

    @Test
    void testParallelForAllSuccess(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c");
        AtomicInteger processed = new AtomicInteger(0);

        parallelForAllSuccess(items, item -> {
            processed.incrementAndGet();
            return asyncSleep(50);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, processed.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testParallelForAllSuccessWithFailure(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c");

        parallelForAllSuccess(items, item -> {
            if ("b".equals(item)) {
                return Future.failedFuture(new RuntimeException("Test failure"));
            }
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.failed()) {
                testContext.completeNow();
            } else {
                testContext.failNow(new AssertionError("Should have failed"));
            }
        });
    }

    @Test
    void testParallelForAllSuccessWithEmptyList(VertxTestContext testContext) {
        List<String> items = List.of();

        parallelForAllSuccess(items, item -> asyncSleep(10))
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        testContext.completeNow();
                    } else {
                        testContext.failNow(ar.cause());
                    }
                });
    }

    @Test
    void testParallelForAnySuccess(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c");
        AtomicInteger processed = new AtomicInteger(0);

        parallelForAnySuccess(items, item -> {
            processed.incrementAndGet();
            return asyncSleep(50);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                // At least one should be processed
                assertTrue(processed.get() >= 1);
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testParallelForAnySuccessWithOneSuccess(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c");

        parallelForAnySuccess(items, item -> {
            if ("a".equals(item)) {
                return asyncSleep(10);
            }
            return Future.failedFuture(new RuntimeException("Failure"));
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testParallelForAllComplete(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c");
        AtomicInteger processed = new AtomicInteger(0);

        // Test that all tasks complete successfully
        parallelForAllComplete(items, item -> {
            processed.incrementAndGet();
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                // All 3 items should be processed
                assertEquals(3, processed.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testParallelForAllCompleteWithEmptyList(VertxTestContext testContext) {
        List<String> items = List.of();

        parallelForAllComplete(items, item -> asyncSleep(10))
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        testContext.completeNow();
                    } else {
                        testContext.failNow(ar.cause());
                    }
                });
    }

    @Test
    void testParallelWithIterator(VertxTestContext testContext) {
        List<String> items = Arrays.asList("x", "y", "z");
        AtomicInteger processed = new AtomicInteger(0);

        parallelForAllSuccess(items.iterator(), item -> {
            processed.incrementAndGet();
            return asyncSleep(10);
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, processed.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}

