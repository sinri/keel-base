package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.KeelSampleImpl;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
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
class KeelAsyncMixinLockUnitTest {
    private Keel asyncMixin;

    @BeforeEach
    void setUp(Vertx vertx) {
        KeelSampleImpl.Keel.initializeVertx(vertx);
        asyncMixin = KeelSampleImpl.Keel;
    }

    @Test
    void testAsyncCallExclusively(VertxTestContext testContext) {
        AtomicInteger counter = new AtomicInteger(0);

        asyncMixin.asyncCallExclusively("test-lock", () -> {
            counter.incrementAndGet();
            return asyncMixin.asyncSleep(10).map(v -> "success");
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

        asyncMixin.asyncCallExclusively("test-lock-timeout", 1000, () -> {
            counter.incrementAndGet();
            return asyncMixin.asyncSleep(10).map(v -> "success");
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
        asyncMixin.asyncCallExclusively("sequential-lock", () -> {
            counter.incrementAndGet();
            return asyncMixin.asyncSleep(50).map(v -> "first");
        }).compose(firstResult -> {
            // Second call should wait for first to complete
            return asyncMixin.asyncCallExclusively("sequential-lock", () -> {
                counter.incrementAndGet();
                return asyncMixin.asyncSleep(10).map(v -> "second");
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
        asyncMixin.asyncCallExclusively("failure-lock", () -> {
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

