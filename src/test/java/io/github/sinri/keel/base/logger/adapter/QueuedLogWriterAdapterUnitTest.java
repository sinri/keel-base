package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.github.sinri.keel.logger.api.log.Log;
import io.github.sinri.keel.logger.api.log.SpecificLog;
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
 * QueuedLogWriterAdapter单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
@NullMarked
class QueuedLogWriterAdapterUnitTest extends KeelJUnit5Test {

    private TestQueuedLogWriterAdapter adapter;

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public QueuedLogWriterAdapterUnitTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    void setUp(VertxTestContext testContext) {
        adapter = new TestQueuedLogWriterAdapter();
        adapter.deployMe(
                getVertx(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).onComplete(testContext.succeedingThenComplete());
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        adapter.undeployMe()
               .onComplete(ar -> testContext.completeNow());
    }

    /**
     * 测试accept方法能够正确接收日志记录。
     * <p>
     * 验证日志记录被加入队列并最终被处理。
     */
    @Test
    void testAcceptLogRecord(VertxTestContext testContext) throws Throwable {
        String topic = "test-topic";
        Log log = new Log();
        log.message("Test message");

        adapter.accept(topic, log);

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        assertTrue(adapter.getProcessedCount() > 0, "At least one log should be processed");
        assertTrue(adapter.getProcessedLogs().containsKey(topic), "Topic should exist in processed logs");

        testContext.completeNow();
    }

    /**
     * 测试批处理功能。
     * <p>
     * 验证当提交多条日志时，能够按批次处理。
     */
    @Test
    void testBatchProcessing(VertxTestContext testContext) throws Throwable {
        String topic = "batch-topic";
        int logCount = 10;

        for (int i = 0; i < logCount; i++) {
            Log log = new Log();
            log.message("Batch message " + i);
            adapter.accept(topic, log);
        }

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        assertTrue(adapter.getProcessedCount() >= logCount,
                "All logs should be processed");

        List<SpecificLog<?>> logs = adapter.getProcessedLogs().get(topic);
        assertNotNull(logs, "Processed logs for topic should exist");
        assertEquals(logCount, logs.size(), "All logs should be processed");

        testContext.completeNow();
    }

    /**
     * 测试多个topic的处理。
     * <p>
     * 验证不同topic的日志能够独立处理。
     */
    @Test
    void testMultipleTopics(VertxTestContext testContext) throws Throwable {
        String topic1 = "topic1";
        String topic2 = "topic2";

        for (int i = 0; i < 3; i++) {
            Log log1 = new Log();
            log1.message("Topic1 message " + i);
            adapter.accept(topic1, log1);

            Log log2 = new Log();
            log2.message("Topic2 message " + i);
            adapter.accept(topic2, log2);
        }

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        Map<String, List<SpecificLog<?>>> processedLogs = adapter.getProcessedLogs();
        assertTrue(processedLogs.containsKey(topic1), "Topic1 should be processed");
        assertTrue(processedLogs.containsKey(topic2), "Topic2 should be processed");

        assertEquals(3, processedLogs.get(topic1).size(), "Topic1 should have 3 logs");
        assertEquals(3, processedLogs.get(topic2).size(), "Topic2 should have 3 logs");

        testContext.completeNow();
    }

    /**
     * 测试自定义bufferSize。
     * <p>
     * 验证可以通过重载bufferSize方法来改变批处理大小。
     */
    @Test
    void testCustomBufferSize(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestQueuedLogWriterAdapter customAdapter = new TestQueuedLogWriterAdapter(5);
        customAdapter.deployMe(
                vertx,
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).onSuccess(v -> {
            String topic = "custom-buffer-topic";
            for (int i = 0; i < 10; i++) {
                Log log = new Log();
                log.message("Custom buffer message " + i);
                customAdapter.accept(topic, log);
            }
        }).compose(v -> {
            return Future.future(promise -> {
                vertx.setTimer(2000, id -> promise.complete());
            });
        }).onComplete(ar -> {
            assertTrue(customAdapter.getProcessedCount() >= 10,
                    "All logs should be processed");
            assertEquals(5, customAdapter.getBufferSize(),
                    "Buffer size should be 5");

            customAdapter.undeployMe().onComplete(ar2 -> testContext.completeNow());
        });

        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    /**
     * 测试大量日志的处理。
     * <p>
     * 验证系统能够处理大批量日志而不会丢失。
     */
    @Test
    void testLargeVolumeProcessing(VertxTestContext testContext) throws Throwable {
        String topic = "large-volume-topic";
        int largeCount = 200;

        for (int i = 0; i < largeCount; i++) {
            Log log = new Log();
            log.message("Large volume message " + i);
            adapter.accept(topic, log);
        }

        testContext.awaitCompletion(5, TimeUnit.SECONDS);

        assertTrue(adapter.getProcessedCount() >= largeCount,
                "All logs should be processed eventually");

        List<SpecificLog<?>> logs = adapter.getProcessedLogs().get(topic);
        assertNotNull(logs, "Processed logs should exist");
        assertEquals(largeCount, logs.size(), "All logs should be processed without loss");

        testContext.completeNow();
    }

    /**
     * 测试verticle的停止功能。
     * <p>
     * 验证调用undeploy后，日志处理能够优雅停止。
     */
    @Test
    void testVerticleStop(Vertx vertx, VertxTestContext testContext) throws Throwable {
        TestQueuedLogWriterAdapter stopAdapter = new TestQueuedLogWriterAdapter();

        stopAdapter.deployMe(
                vertx,
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).compose(v -> {
            String topic = "stop-test-topic";
            for (int i = 0; i < 5; i++) {
                Log log = new Log();
                log.message("Stop test message " + i);
                stopAdapter.accept(topic, log);
            }
            return Future.future(promise -> {
                vertx.setTimer(1000, id -> promise.complete());
            });
        }).compose(v -> {
            return stopAdapter.undeployMe();
        }).onComplete(ar -> {
            assertTrue(ar.succeeded(), "Undeploy should succeed");
            testContext.completeNow();
        });

        testContext.awaitCompletion(5, TimeUnit.SECONDS);
    }

    /**
     * 测试用的 QueuedLogWriterAdapter 实现。
     * <p>
     * 记录所有处理的日志用于测试验证。
     */
    private static class TestQueuedLogWriterAdapter extends QueuedLogWriterAdapter {
        private final Map<String, List<SpecificLog<?>>> processedLogs = new ConcurrentHashMap<>();
        private final AtomicInteger processedCount = new AtomicInteger(0);
        private final int customBufferSize;

        public TestQueuedLogWriterAdapter() {
            super();
            this.customBufferSize = 128;
        }

        public TestQueuedLogWriterAdapter(int customBufferSize) {
            super();
            this.customBufferSize = customBufferSize;
        }

        @Override
        protected int bufferSize() {
            return customBufferSize;
        }

        @Override
        protected Future<Void> processLogRecords(String topic, List<SpecificLog<?>> batch) {
            processedLogs.computeIfAbsent(topic, k -> new ArrayList<>())
                         .addAll(batch);
            processedCount.addAndGet(batch.size());
            return Future.succeededFuture();
        }

        public Map<String, List<SpecificLog<?>>> getProcessedLogs() {
            return processedLogs;
        }

        public int getProcessedCount() {
            return processedCount.get();
        }

        public int getBufferSize() {
            return customBufferSize;
        }
    }
}

