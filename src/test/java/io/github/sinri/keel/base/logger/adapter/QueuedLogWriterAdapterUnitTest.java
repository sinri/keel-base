package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.KeelInstance;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * QueuedLogWriterAdapter单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class QueuedLogWriterAdapterUnitTest {
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        KeelInstance.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testAccept(VertxTestContext testContext) {
        TestQueuedLogWriterAdapter adapter = new TestQueuedLogWriterAdapter();

        // Note: SpecificLog is from external API, so we test the adapter structure
        // In real usage, SpecificLog instances would be created through proper API
        // This test verifies the adapter can be deployed and closed properly
        vertx.deployVerticle(adapter)
             .compose(deploymentId -> {
                 // Wait a bit for initialization
                 Promise<Void> promise = Promise.promise();
                 vertx.setTimer(100, id -> promise.complete());
                 return promise.future();
             })
             .compose(v -> {
                 adapter.close();
                 return vertx.undeploy(adapter.deploymentID());
             })
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testClose(VertxTestContext testContext) {
        TestQueuedLogWriterAdapter adapter = new TestQueuedLogWriterAdapter();

        vertx.deployVerticle(adapter)
             .compose(deploymentId -> {
                 adapter.close();
                 // Wait a bit to ensure it stops
                 Promise<Void> promise = Promise.promise();
                 vertx.setTimer(200, id -> promise.complete());
                 return promise.future();
             })
             .onComplete(ar -> {
                 if (ar.succeeded()) {
                     testContext.completeNow();
                 } else {
                     testContext.failNow(ar.cause());
                 }
             });
    }

    @Test
    void testBufferSize() {
        TestQueuedLogWriterAdapter adapter = new TestQueuedLogWriterAdapter();
        assertEquals(128, adapter.bufferSize());
    }

    /**
     * 测试用的QueuedLogWriterAdapter实现。
     */
    private static class TestQueuedLogWriterAdapter extends QueuedLogWriterAdapter {
        private final AtomicInteger processedCount = new AtomicInteger(0);

        @Override
        protected Future<Void> processLogRecords(String topic, List<SpecificLog<?>> batch) {
            processedCount.addAndGet(batch.size());
            return Future.succeededFuture();
        }

        int getProcessedCount() {
            return processedCount.get();
        }
    }

}

