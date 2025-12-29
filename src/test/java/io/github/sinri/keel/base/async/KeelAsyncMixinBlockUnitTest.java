package io.github.sinri.keel.base.async;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.KeelSampleImpl;
import io.github.sinri.keel.base.logger.logger.StdoutLogger;
import io.github.sinri.keel.base.verticles.KeelVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * KeelAsyncMixinBlock单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
public class KeelAsyncMixinBlockUnitTest {
    private Keel asyncMixin;

    @BeforeEach
    public void setUp(Vertx vertx) {
        KeelSampleImpl.Keel.initializeVertx(vertx);
        asyncMixin = KeelSampleImpl.Keel;
    }

    @Test
    void testAsyncTransformCompletableFuture(VertxTestContext testContext) {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> "test-result");

        asyncMixin.asyncTransformCompletableFuture(cf)
                  .onComplete(ar -> {
                      if (ar.succeeded()) {
                          assertEquals("test-result", ar.result());
                          testContext.completeNow();
                      } else {
                          testContext.failNow(ar.cause());
                      }
                  });
    }

    @Test
    void testAsyncTransformCompletableFutureWithException(VertxTestContext testContext) {
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("Test exception");
        });

        asyncMixin.asyncTransformCompletableFuture(cf)
                  .onComplete(ar -> {
                      if (ar.failed()) {
                          assertInstanceOf(RuntimeException.class, ar.cause());
                          testContext.completeNow();
                      } else {
                          testContext.failNow(new AssertionError("Should have failed"));
                      }
                  });
    }

    @Test
    void testAsyncTransformRawFuture(VertxTestContext testContext) {
        java.util.concurrent.Future<String> rawFuture = CompletableFuture.completedFuture("result");

        asyncMixin.asyncTransformRawFuture(rawFuture)
                  .onComplete(ar -> {
                      if (ar.succeeded()) {
                          assertEquals("result", ar.result());
                          testContext.completeNow();
                      } else {
                          testContext.failNow(ar.cause());
                      }
                  });
    }

    @Test
    void testAsyncTransformRawFutureWithSleep(VertxTestContext testContext) {
        CompletableFuture<String> cf = new CompletableFuture<>();

        // Complete after 100ms
        asyncMixin.getVertx().setTimer(100, id -> cf.complete("delayed-result"));

        asyncMixin.asyncTransformRawFuture(cf, 50)
                  .onComplete(ar -> {
                      if (ar.succeeded()) {
                          assertEquals("delayed-result", ar.result());
                          testContext.completeNow();
                      } else {
                          testContext.failNow(ar.cause());
                      }
                  });
    }

    @Test
    void testAsyncTransformRawFutureCancelled(VertxTestContext testContext) {
        CompletableFuture<String> cf = new CompletableFuture<>();
        cf.cancel(true);

        asyncMixin.asyncTransformRawFuture(cf, 50)
                  .onComplete(ar -> {
                      if (ar.failed()) {
                          assertInstanceOf(CancellationException.class, ar.cause());
                          testContext.completeNow();
                      } else {
                          testContext.failNow(new AssertionError("Should have failed"));
                      }
                  });
    }

    @Test
    public void testBlockAwait(VertxTestContext testContext) {
        // This test needs to run in a worker thread context
        Future<String> future = asyncMixin.asyncSleep(50).map(v -> "success");
        var verticle = KeelVerticle.instant(KeelSampleImpl.Keel, keelVerticle -> {

            keelVerticle.getVertx().setTimer(100L, id -> {
                try {
                    ThreadingModel threadingModel = Vertx.currentContext().threadingModel();
                    System.out.println("Current thread is " + threadingModel.toString());
                    String result = asyncMixin.blockAwait(future);
                    assertEquals("success", result);
                    System.out.println("Block await success");
                    testContext.completeNow();
                } catch (Exception e) {
                    new StdoutLogger("testBlockAwait").error(log -> log.message("Block await failed").exception(e));
                    testContext.failNow(e);
                }
                keelVerticle.undeployMe();
                testContext.completeNow();
            });

            return Future.succeededFuture();
        });
        verticle.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .onComplete(ar -> {
                    if (ar.failed()) {
                        testContext.failNow(ar.cause());
                    } else {
                        System.out.println("Deployed: " + ar.result());
                    }
                });
    }

    @Test
    void testBlockAwaitInEventLoop(VertxTestContext testContext) {
        // blockAwait should throw exception in event loop
        asyncMixin.getVertx().setTimer(100, id -> {
            assertThrows(IllegalThreadStateException.class, () -> {
                asyncMixin.blockAwait(asyncMixin.asyncSleep(10).map(v -> "test"));
            });
            testContext.completeNow();
        });
    }
}

