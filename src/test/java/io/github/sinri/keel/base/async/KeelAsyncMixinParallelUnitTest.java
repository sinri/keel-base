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
class KeelAsyncMixinParallelUnitTest {
    private Keel asyncMixin;

    @BeforeEach
    void setUp(Vertx vertx) {
        KeelSampleImpl.Keel.initializeVertx(vertx);
        asyncMixin = KeelSampleImpl.Keel;
    }

    @Test
    void testParallelForAllSuccess(VertxTestContext testContext) {
        List<String> items = Arrays.asList("a", "b", "c");
        AtomicInteger processed = new AtomicInteger(0);

        asyncMixin.parallelForAllSuccess(items, item -> {
            processed.incrementAndGet();
            return asyncMixin.asyncSleep(50);
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

        asyncMixin.parallelForAllSuccess(items, item -> {
            if ("b".equals(item)) {
                return Future.failedFuture(new RuntimeException("Test failure"));
            }
            return asyncMixin.asyncSleep(10);
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

        asyncMixin.parallelForAllSuccess(items, item -> asyncMixin.asyncSleep(10))
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

        asyncMixin.parallelForAnySuccess(items, item -> {
            processed.incrementAndGet();
            return asyncMixin.asyncSleep(50);
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

        asyncMixin.parallelForAnySuccess(items, item -> {
            if ("a".equals(item)) {
                return asyncMixin.asyncSleep(10);
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
        asyncMixin.parallelForAllComplete(items, item -> {
            processed.incrementAndGet();
            return asyncMixin.asyncSleep(10);
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

        asyncMixin.parallelForAllComplete(items, item -> asyncMixin.asyncSleep(10))
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

        asyncMixin.parallelForAllSuccess(items.iterator(), item -> {
            processed.incrementAndGet();
            return asyncMixin.asyncSleep(10);
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

