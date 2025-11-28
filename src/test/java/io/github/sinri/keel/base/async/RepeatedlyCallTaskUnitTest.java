package io.github.sinri.keel.base.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RepeatedlyCallTask单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class RepeatedlyCallTaskUnitTest {
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx) {
        this.vertx = vertx;
    }

    @Test
    void testRepeatedlyCallTaskStop(VertxTestContext testContext) {
        AtomicInteger count = new AtomicInteger(0);
        RepeatedlyCallTask task = new RepeatedlyCallTask(repeatedlyCallTask -> {
            int current = count.incrementAndGet();
            if (current >= 3) {
                repeatedlyCallTask.stop();
            }
            return Future.succeededFuture();
        });

        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(vertx, task, promise);

        promise.future().onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(3, count.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testRepeatedlyCallTaskWithFailure(VertxTestContext testContext) {
        RepeatedlyCallTask task = new RepeatedlyCallTask(repeatedlyCallTask -> {
            return Future.failedFuture(new RuntimeException("Test failure"));
        });

        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(vertx, task, promise);

        promise.future().onComplete(ar -> {
            if (ar.failed()) {
                assertInstanceOf(RuntimeException.class, ar.cause());
                assertEquals("Test failure", ar.cause().getMessage());
                testContext.completeNow();
            } else {
                testContext.failNow(new AssertionError("Should have failed"));
            }
        });
    }

    @Test
    void testRepeatedlyCallTaskStopImmediately(VertxTestContext testContext) {
        AtomicInteger count = new AtomicInteger(0);
        RepeatedlyCallTask task = new RepeatedlyCallTask(repeatedlyCallTask -> {
            count.incrementAndGet();
            repeatedlyCallTask.stop();
            return Future.succeededFuture();
        });

        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(vertx, task, promise);

        promise.future().onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(1, count.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    void testRepeatedlyCallTaskStopBeforeFirstCall(VertxTestContext testContext) {
        AtomicInteger count = new AtomicInteger(0);
        RepeatedlyCallTask task = new RepeatedlyCallTask(repeatedlyCallTask -> {
            count.incrementAndGet();
            return Future.succeededFuture();
        });

        // Stop before starting
        task.stop();

        Promise<Void> promise = Promise.promise();
        RepeatedlyCallTask.start(vertx, task, promise);

        promise.future().onComplete(ar -> {
            if (ar.succeeded()) {
                assertEquals(0, count.get());
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}

