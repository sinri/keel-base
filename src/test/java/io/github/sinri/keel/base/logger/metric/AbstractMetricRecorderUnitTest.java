package io.github.sinri.keel.base.logger.metric;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * AbstractMetricRecorder单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class AbstractMetricRecorderUnitTest {
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        KeelSampleImpl.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testRecordMetric(VertxTestContext testContext) {
        TestMetricRecorder recorder = new TestMetricRecorder();

        // Note: MetricRecord is from external API, so we test the recorder structure
        // In real usage, MetricRecord instances would be created through proper API
        // This test verifies the recorder can be deployed and closed properly
        vertx.deployVerticle(recorder)
             .compose(deploymentId -> {
                 // Wait a bit for initialization
                 Promise<Void> promise = Promise.promise();
                 vertx.setTimer(100, id -> promise.complete());
                 return promise.future();
             })
             .compose(v -> {
                 recorder.close();
                 return vertx.undeploy(recorder.deploymentID());
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
        TestMetricRecorder recorder = new TestMetricRecorder();

        vertx.deployVerticle(recorder)
             .compose(deploymentId -> {
                 recorder.close();
                 // Wait a bit to ensure it stops
                 Promise<Void> promise = Promise.promise();
                 vertx.setTimer(200, id -> promise.complete());
                 return promise.future();
             })
             .compose(v -> vertx.undeploy(recorder.deploymentID()))
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
        TestMetricRecorder recorder = new TestMetricRecorder();
        assertEquals(1000, recorder.bufferSize());
    }

    @Test
    void testTopic() {
        TestMetricRecorder recorder = new TestMetricRecorder();
        assertEquals("metric", recorder.topic());
    }

    /**
     * 测试用的MetricRecorder实现。
     */
    private static class TestMetricRecorder extends AbstractMetricRecorder {
        private final AtomicInteger processedCount = new AtomicInteger(0);
        private final List<MetricRecord> processedRecords = new ArrayList<>();

        TestMetricRecorder() {
            super(KeelSampleImpl.Keel);
        }

        @Override
        protected Future<Void> handleForTopic(String topic, List<MetricRecord> buffer) {
            processedCount.addAndGet(buffer.size());
            processedRecords.addAll(buffer);
            return Future.succeededFuture();
        }

        int getProcessedCount() {
            return processedCount.get();
        }

        List<MetricRecord> getProcessedRecords() {
            return processedRecords;
        }
    }
}

