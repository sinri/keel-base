package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * KeelAsyncMixinLock单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class KeelAsyncMixinLockUnitTest extends KeelJUnit5Test {

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public KeelAsyncMixinLockUnitTest(Vertx vertx) {
        super(vertx);
    }


    @Test
    void testAsyncCallExclusively(VertxTestContext testContext) {
        AtomicInteger counter = new AtomicInteger(0);

        asyncCallExclusively("test-lock", () -> {
            counter.incrementAndGet();
            return asyncSleep(10).map(v -> "success");
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals("success", ar.result());
                assertEquals(1, counter.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallExclusivelyWithTimeout(VertxTestContext testContext) {
        AtomicInteger counter = new AtomicInteger(0);

        asyncCallExclusively("test-lock-timeout", 1000, () -> {
            counter.incrementAndGet();
            return asyncSleep(10).map(v -> "success");
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals("success", ar.result());
                assertEquals(1, counter.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallExclusivelySequential(VertxTestContext testContext) {
        AtomicInteger counter = new AtomicInteger(0);

        // First call
        asyncCallExclusively("sequential-lock", () -> {
            counter.incrementAndGet();
            return asyncSleep(50).map(v -> "first");
        }).compose(firstResult -> {
            // Second call should wait for first to complete
            return asyncCallExclusively("sequential-lock", () -> {
                counter.incrementAndGet();
                return asyncSleep(10).map(v -> "second");
            });
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals("second", ar.result());
                assertEquals(2, counter.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testAsyncCallExclusivelyWithFailure(VertxTestContext testContext) {
        asyncCallExclusively("failure-lock", () -> {
            return Future.failedFuture(new RuntimeException("Test failure"));
        }).onComplete(ar -> {
            if (ar.failed()) {
                assertInstanceOf(RuntimeException.class, ar.cause());
                assertEquals("Test failure", ar.cause().getMessage());
                testContext.completeNow();
            } else {
                testContext.failNow(new AssertionError("Should have failed"));
            }
        });
    }
}

