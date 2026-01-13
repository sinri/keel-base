package io.github.sinri.keel.base.logger.metric;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.github.sinri.keel.logger.api.metric.MetricRecord;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AbstractMetricRecorder单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
@NullMarked
class AbstractMetricRecorderTest extends KeelJUnit5Test {

    private TestMetricRecorder recorder;

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public AbstractMetricRecorderTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    void setUp(VertxTestContext testContext) {
        recorder = new TestMetricRecorder();
        recorder.deployMe(
                getVertx(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).onComplete(testContext.succeedingThenComplete());
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        recorder.undeployMe()
                .onComplete(ar -> testContext.completeNow());
    }

    /**
     * 测试记录单个指标。
     * <p>
     * 验证指标能够被正确记录和处理。
     */
    @Test
    void testRecordSingleMetric(VertxTestContext testContext) throws Throwable {
        TestMetricRecord metric = new TestMetricRecord("test-metric", 100.0);
        recorder.recordMetric(metric);

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        assertTrue(recorder.getProcessedCount() > 0, "At least one metric should be processed");
        assertTrue(recorder.getProcessedMetrics().containsKey("metric"),
                "Default topic should exist");

        testContext.completeNow();
    }

    /**
     * 测试记录多个指标。
     * <p>
     * 验证多个指标能够被批量处理。
     */
    @Test
    void testRecordMultipleMetrics(VertxTestContext testContext) throws Throwable {
        int metricCount = 10;

        for (int i = 0; i < metricCount; i++) {
            TestMetricRecord metric = new TestMetricRecord("metric-" + i, i);
            recorder.recordMetric(metric);
        }

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        assertTrue(recorder.getProcessedCount() >= metricCount,
                "All metrics should be processed");

        List<MetricRecord> metrics = recorder.getProcessedMetrics().get("metric");
        assertNotNull(metrics, "Processed metrics should exist");
        assertEquals(metricCount, metrics.size(), "All metrics should be processed");

        testContext.completeNow();
    }

    /**
     * 测试批处理功能。
     * <p>
     * 验证当指标数量超过bufferSize时，能够分批处理。
     */
    @Test
    void testBatchProcessing(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestMetricRecorder customRecorder = new TestMetricRecorder(5);
        customRecorder.deployMe(
                vertx,
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).onSuccess(v -> {
            for (int i = 0; i < 12; i++) {
                TestMetricRecord metric = new TestMetricRecord("batch-metric-" + i, i);
                customRecorder.recordMetric(metric);
            }
        }).compose(v -> {
            return Future.future(promise -> {
                vertx.setTimer(2000, id -> promise.complete());
            });
        }).onComplete(ar -> {
            assertTrue(customRecorder.getProcessedCount() >= 12,
                    "All metrics should be processed");
            assertEquals(5, customRecorder.getBufferSize(),
                    "Buffer size should be 5");

            customRecorder.undeployMe().onComplete(ar2 -> testContext.completeNow());
        });

        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    /**
     * 测试自定义主题。
     * <p>
     * 验证可以通过重载topic方法来改变指标记录的主题。
     */
    @Test
    void testCustomTopic(Vertx vertx, VertxTestContext testContext) throws Throwable {
        String customTopic = "custom-metrics";
        CustomTopicMetricRecorder customRecorder = new CustomTopicMetricRecorder(customTopic);

        customRecorder.deployMe(
                vertx,
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).onSuccess(v -> {
            TestMetricRecord metric = new TestMetricRecord("test", 123.45);
            customRecorder.recordMetric(metric);
        }).compose(v -> {
            return Future.future(promise -> {
                vertx.setTimer(1000, id -> promise.complete());
            });
        }).onComplete(ar -> {
            Map<String, List<MetricRecord>> processed = customRecorder.getProcessedMetrics();
            assertTrue(processed.containsKey(customTopic),
                    "Custom topic should be used");
            assertEquals(customTopic, customRecorder.getUsedTopic(),
                    "Topic should match custom value");

            customRecorder.undeployMe().onComplete(ar2 -> testContext.completeNow());
        });

        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    /**
     * 测试大量指标的处理。
     * <p>
     * 验证系统能够处理大批量指标而不会丢失。
     */
    @Test
    void testLargeVolumeProcessing(VertxTestContext testContext) throws Throwable {
        int largeCount = 500;

        for (int i = 0; i < largeCount; i++) {
            TestMetricRecord metric = new TestMetricRecord("large-volume-" + i, i);
            recorder.recordMetric(metric);
        }

        testContext.awaitCompletion(8, TimeUnit.SECONDS);

        assertTrue(recorder.getProcessedCount() >= largeCount,
                "All metrics should be processed eventually");

        List<MetricRecord> metrics = recorder.getProcessedMetrics().get("metric");
        assertNotNull(metrics, "Processed metrics should exist");
        assertEquals(largeCount, metrics.size(), "All metrics should be processed without loss");

        testContext.completeNow();
    }

    /**
     * 测试默认的bufferSize。
     * <p>
     * 验证默认的bufferSize为1000。
     */
    @Test
    void testDefaultBufferSize(VertxTestContext testContext) throws Throwable {
        assertEquals(1000, recorder.getBufferSize(),
                "Default buffer size should be 1000");
        testContext.completeNow();
    }

    /**
     * 测试默认的topic。
     * <p>
     * 验证默认的topic为"metric"。
     */
    @Test
    void testDefaultTopic(VertxTestContext testContext) throws Throwable {
        TestMetricRecord metric = new TestMetricRecord("test", 1.0);
        recorder.recordMetric(metric);

        testContext.awaitCompletion(2, TimeUnit.SECONDS);

        assertTrue(recorder.getProcessedMetrics().containsKey("metric"),
                "Default topic 'metric' should be used");
        testContext.completeNow();
    }

    /**
     * 测试verticle的停止功能。
     * <p>
     * 验证调用undeploy后，指标处理能够优雅停止。
     */
    @Test
    void testVerticleStop(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestMetricRecorder stopRecorder = new TestMetricRecorder();

        stopRecorder.deployMe(
                vertx,
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).compose(v -> {
            for (int i = 0; i < 5; i++) {
                TestMetricRecord metric = new TestMetricRecord("stop-test-" + i, i);
                stopRecorder.recordMetric(metric);
            }
            return Future.future(promise -> {
                vertx.setTimer(1000, id -> promise.complete());
            });
        }).compose(v -> {
            return stopRecorder.undeployMe();
        }).onComplete(ar -> {
            assertTrue(ar.succeeded(), "Undeploy should succeed");
            testContext.completeNow();
        });

        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    /**
     * 测试空队列的处理。
     * <p>
     * 验证当队列为空时，处理循环能够正常运行而不会出错。
     */
    @Test
    void testEmptyQueueProcessing(VertxTestContext testContext) throws Throwable {
        // 不添加任何指标，让处理循环运行
        testContext.awaitCompletion(2, TimeUnit.SECONDS);

        assertEquals(0, recorder.getProcessedCount(),
                "No metrics should be processed when queue is empty");
        testContext.completeNow();
    }

    /**
     * 测试连续批次的处理。
     * <p>
     * 验证多个批次的指标能够依次被处理。
     */
    @Test
    void testContinuousBatchProcessing(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestMetricRecorder batchRecorder = new TestMetricRecorder(10);

        batchRecorder.deployMe(
                vertx,
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).onSuccess(v -> {
            // 第一批
            for (int i = 0; i < 10; i++) {
                TestMetricRecord metric = new TestMetricRecord("batch1-" + i, i);
                batchRecorder.recordMetric(metric);
            }

            // 延迟后添加第二批
            vertx.setTimer(500, id -> {
                for (int i = 0; i < 10; i++) {
                    TestMetricRecord metric = new TestMetricRecord("batch2-" + i, i);
                    batchRecorder.recordMetric(metric);
                }
            });
        }).compose(v -> {
            return Future.future(promise -> {
                vertx.setTimer(2000, id -> promise.complete());
            });
        }).onComplete(ar -> {
            assertTrue(batchRecorder.getProcessedCount() >= 20,
                    "All metrics from both batches should be processed");

            batchRecorder.undeployMe().onComplete(ar2 -> testContext.completeNow());
        });

        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    /**
     * 测试用的 MetricRecord 实现。
     * <p>
     * 简单的指标记录，包含名称和数值。
     */
    private static class TestMetricRecord implements MetricRecord {
        private final String name;
        private final double metricValue;
        private final long metricTimestamp;

        public TestMetricRecord(String name, double value) {
            this.name = name;
            this.metricValue = value;
            this.metricTimestamp = System.currentTimeMillis();
        }

        @Override
        public String metricName() {
            return name;
        }

        @Override
        public long timestamp() {
            return metricTimestamp;
        }

        @Override
        public double value() {
            return metricValue;
        }

        @Override
        public Map<String, String> labels() {
            return new ConcurrentHashMap<>();
        }

        @Override
        public String toString() {
            return String.format("MetricRecord{name='%s', value=%f, timestamp=%d}",
                    name, metricValue, metricTimestamp);
        }
    }

    /**
     * 测试用的 AbstractMetricRecorder 实现。
     * <p>
     * 记录所有处理的指标用于测试验证。
     */
    private static class TestMetricRecorder extends AbstractMetricRecorder {
        private final Map<String, List<MetricRecord>> processedMetrics = new ConcurrentHashMap<>();
        private final AtomicInteger processedCount = new AtomicInteger(0);
        private final int customBufferSize;

        public TestMetricRecorder() {
            super();
            this.customBufferSize = 1000;
        }

        public TestMetricRecorder(int customBufferSize) {
            super();
            this.customBufferSize = customBufferSize;
        }

        @Override
        protected int bufferSize() {
            return customBufferSize;
        }

        @Override
        protected Future<Void> prepareForLoop() {
            return Future.succeededFuture();
        }

        @Override
        protected Future<Void> handleForTopic(String topic, List<MetricRecord> buffer) {
            processedMetrics.computeIfAbsent(topic, k -> new ArrayList<>())
                            .addAll(buffer);
            processedCount.addAndGet(buffer.size());
            return Future.succeededFuture();
        }

        public Map<String, List<MetricRecord>> getProcessedMetrics() {
            return processedMetrics;
        }

        public int getProcessedCount() {
            return processedCount.get();
        }

        public int getBufferSize() {
            return customBufferSize;
        }
    }

    /**
     * 测试用的自定义主题 MetricRecorder。
     * <p>
     * 使用自定义的主题名称。
     */
    private static class CustomTopicMetricRecorder extends AbstractMetricRecorder {
        private final String customTopic;
        private final Map<String, List<MetricRecord>> processedMetrics = new ConcurrentHashMap<>();
        private volatile String usedTopic = "";

        public CustomTopicMetricRecorder(String customTopic) {
            super();
            this.customTopic = customTopic;
        }

        @Override
        protected String topic() {
            return customTopic;
        }

        @Override
        protected Future<Void> prepareForLoop() {
            return Future.succeededFuture();
        }

        @Override
        protected Future<Void> handleForTopic(String topic, List<MetricRecord> buffer) {
            this.usedTopic = topic;
            processedMetrics.computeIfAbsent(topic, k -> new ArrayList<>())
                            .addAll(buffer);
            return Future.succeededFuture();
        }

        public Map<String, List<MetricRecord>> getProcessedMetrics() {
            return processedMetrics;
        }

        public String getUsedTopic() {
            return usedTopic;
        }
    }
}

