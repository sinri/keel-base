package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FileLogWriterAdapter单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
class FileLogWriterAdapterTest {
    private Vertx vertx;

    @BeforeEach
    void setUp(Vertx vertx, VertxTestContext testContext) {
        this.vertx = vertx;
        KeelSampleImpl.Keel.initializeVertx(vertx);
        testContext.completeNow();
    }

    @Test
    void testWriteLogToFile(@TempDir Path tempDir, VertxTestContext testContext) throws IOException {
        File logFile = tempDir.resolve("test.log").toFile();
        TestFileLogWriterAdapter adapter = new TestFileLogWriterAdapter(logFile);

        // 直接测试 processLogRecords 方法
        List<SpecificLog<?>> batch = List.of(createTestLog("Test log message"));

        vertx.deployVerticle(adapter)
             .compose(deploymentId -> adapter.processLogRecords("test-topic", batch))
             .compose(v -> {
                 adapter.close();
                 return vertx.undeploy(adapter.deploymentID());
             })
             .onComplete(ar -> {
                 if (ar.failed()) {
                     testContext.failNow(ar.cause());
                     return;
                 }
                 try {
                     // 验证文件内容
                     String content = Files.readString(logFile.toPath());
                     assertTrue(content.contains("Test log message"), "日志文件应包含测试消息");
                     testContext.completeNow();
                 } catch (IOException e) {
                     testContext.failNow(e);
                 }
             });
    }

    @Test
    void testMultipleTopics(@TempDir Path tempDir, VertxTestContext testContext) throws IOException {
        File logFile1 = tempDir.resolve("topic1.log").toFile();
        File logFile2 = tempDir.resolve("topic2.log").toFile();
        TestFileLogWriterAdapter adapter = new TestFileLogWriterAdapter(logFile1, logFile2);

        List<SpecificLog<?>> batch1 = List.of(createTestLog("Topic 1 message"));
        List<SpecificLog<?>> batch2 = List.of(createTestLog("Topic 2 message"));

        vertx.deployVerticle(adapter)
             .compose(deploymentId -> {
                 return adapter.processLogRecords("topic1", batch1)
                               .compose(v -> adapter.processLogRecords("topic2", batch2));
             })
             .compose(v -> {
                 adapter.close();
                 return vertx.undeploy(adapter.deploymentID());
             })
             .onComplete(ar -> {
                 if (ar.failed()) {
                     testContext.failNow(ar.cause());
                     return;
                 }
                 try {
                     String content1 = Files.readString(logFile1.toPath());
                     String content2 = Files.readString(logFile2.toPath());
                     assertTrue(content1.contains("Topic 1 message"), "topic1日志文件应包含相应消息");
                     assertTrue(content2.contains("Topic 2 message"), "topic2日志文件应包含相应消息");
                     testContext.completeNow();
                 } catch (IOException e) {
                     testContext.failNow(e);
                 }
             });
    }

    @Test
    void testDiscardLogWhenFileWriterIsNull(VertxTestContext testContext) {
        TestFileLogWriterAdapter adapter = new TestFileLogWriterAdapter(null);

        List<SpecificLog<?>> batch = List.of(createTestLog("This should be discarded"));

        vertx.deployVerticle(adapter)
             .compose(deploymentId -> adapter.processLogRecords("discard-topic", batch))
             .compose(v -> {
                 adapter.close();
                 return vertx.undeploy(adapter.deploymentID());
             })
             .onComplete(ar -> {
                 if (ar.failed()) {
                     testContext.failNow(ar.cause());
                     return;
                 }
                 // 如果返回null，日志应该被丢弃，不应该抛出异常
                 testContext.completeNow();
             });
    }

    @Test
    void testMultipleLogsInBatch(@TempDir Path tempDir, VertxTestContext testContext) throws IOException {
        File logFile = tempDir.resolve("batch.log").toFile();
        TestFileLogWriterAdapter adapter = new TestFileLogWriterAdapter(logFile);

        List<SpecificLog<?>> batch = List.of(
                createTestLog("Log 1"),
                createTestLog("Log 2"),
                createTestLog("Log 3")
        );

        vertx.deployVerticle(adapter)
             .compose(deploymentId -> adapter.processLogRecords("batch-topic", batch))
             .compose(v -> {
                 adapter.close();
                 return vertx.undeploy(adapter.deploymentID());
             })
             .onComplete(ar -> {
                 if (ar.failed()) {
                     testContext.failNow(ar.cause());
                     return;
                 }
                 try {
                     String content = Files.readString(logFile.toPath());
                     assertTrue(content.contains("Log 1"), "应包含Log 1");
                     assertTrue(content.contains("Log 2"), "应包含Log 2");
                     assertTrue(content.contains("Log 3"), "应包含Log 3");
                     testContext.completeNow();
                 } catch (IOException e) {
                     testContext.failNow(e);
                 }
             });
    }

    @Test
    void testCloseClosesFileWriters(@TempDir Path tempDir, VertxTestContext testContext) throws IOException {
        File logFile = tempDir.resolve("close-test.log").toFile();
        TestFileLogWriterAdapter adapter = new TestFileLogWriterAdapter(logFile);

        List<SpecificLog<?>> batch = List.of(createTestLog("Test message"));

        vertx.deployVerticle(adapter)
             .compose(deploymentId -> {
                 // 保存 deploymentId，因为 close() 后可能会自动 undeploy
                 String savedDeploymentId = deploymentId;
                 return adapter.processLogRecords("close-topic", batch)
                               .compose(v -> {
                                   adapter.close();
                                   // 等待关闭完成
                                   Promise<Void> promise = Promise.promise();
                                   vertx.setTimer(200, id -> promise.complete());
                                   return promise.future();
                               })
                               .compose(v -> {
                                   // 尝试 undeploy，如果已经 undeploy 则忽略错误
                                   return vertx.undeploy(savedDeploymentId)
                                               .recover(throwable -> {
                                                   // 如果 deployment 不存在，忽略错误
                                                   if (throwable.getMessage() != null && throwable.getMessage()
                                                                                                  .contains("Unknown deployment")) {
                                                       return Future.succeededFuture();
                                                   }
                                                   return Future.failedFuture(throwable);
                                               });
                               });
             })
             .onComplete(ar -> {
                 if (ar.failed()) {
                     testContext.failNow(ar.cause());
                     return;
                 }
                 // 验证文件写入器已关闭
                 assertTrue(adapter.isFileWriterClosed("close-topic"), "文件写入器应该已关闭");
                 testContext.completeNow();
             });
    }

    /**
     * 创建测试用的SpecificLog对象。
     */
    @NotNull
    private SpecificLog<?> createTestLog(@NotNull String message) {
        return new TestSpecificLog(message);
    }

    /**
     * 测试用的SpecificLog实现类。
     */
    private static class TestSpecificLog extends SpecificLog<TestSpecificLog> {
        private final String message;

        TestSpecificLog(@NotNull String message) {
            this.message = message;
        }

        @Override
        public @NotNull String toString() {
            return message + "\n";
        }
    }

    /**
     * 测试用的FileLogWriterAdapter实现。
     */
    private static class TestFileLogWriterAdapter extends FileLogWriterAdapter {
        private final Map<String, File> topicFileMap = new ConcurrentHashMap<>();
        private final Map<String, FileWriter> fileWriterMap = new ConcurrentHashMap<>();
        private final Map<String, Boolean> closedMap = new ConcurrentHashMap<>();
        private final File defaultFile;

        TestFileLogWriterAdapter(@Nullable File defaultFile) {
            super(KeelSampleImpl.Keel);
            this.defaultFile = defaultFile;
        }

        TestFileLogWriterAdapter(@Nullable File file1, @Nullable File file2) {
            super(KeelSampleImpl.Keel);
            this.defaultFile = null;
            if (file1 != null) {
                topicFileMap.put("topic1", file1);
            }
            if (file2 != null) {
                topicFileMap.put("topic2", file2);
            }
        }

        @Override
        protected @Nullable FileWriter getFileWriterForTopic(@NotNull String topic) {
            if (defaultFile == null && !topicFileMap.containsKey(topic)) {
                return null;
            }

            return fileWriterMap.computeIfAbsent(topic, k -> {
                try {
                    File file = topicFileMap.getOrDefault(topic, defaultFile);
                    if (file == null) {
                        return null;
                    }
                    closedMap.put(topic, false);
                    return new FileWriter(file, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        protected @NotNull Future<Void> processLogRecords(@NotNull String topic, @NotNull List<SpecificLog<?>> batch) {
            Future<Void> result = super.processLogRecords(topic, batch);
            // 在processLogRecords之后检查是否需要关闭文件写入器
            return result;
        }

        @Override
        public void close() {
            // 关闭所有文件写入器
            fileWriterMap.forEach((topic, writer) -> {
                try {
                    writer.close();
                    closedMap.put(topic, true);
                } catch (IOException e) {
                    // 忽略关闭错误
                }
            });
            fileWriterMap.clear();
            super.close();
        }

        boolean isFileWriterClosed(@NotNull String topic) {
            return closedMap.getOrDefault(topic, true);
        }
    }
}