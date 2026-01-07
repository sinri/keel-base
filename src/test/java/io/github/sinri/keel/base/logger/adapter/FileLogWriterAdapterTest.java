package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.KeelJUnit5Test;
import io.github.sinri.keel.logger.api.log.Log;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FileLogWriterAdapter单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(VertxExtension.class)
@NullMarked
class FileLogWriterAdapterTest extends KeelJUnit5Test {

    @TempDir
    Path tempDir;

    private TestFileLogWriterAdapter adapter;
    private TestLoggerFactory loggerFactory;

    /**
     * 构造方法。
     * <p>本方法在 {@code @BeforeAll} 注解的静态方法运行后运行。
     * <p>注意，本构造方法会注册 {@code JsonifiableSerializer} 所载 JSON 序列化能力。
     *
     * @param vertx 由 VertxExtension 提供的 Vertx 实例。
     */
    public FileLogWriterAdapterTest(Vertx vertx) {
        super(vertx);
    }

    @BeforeEach
    void setUp(VertxTestContext testContext) {
        adapter = new TestFileLogWriterAdapter(tempDir);
        loggerFactory = new TestLoggerFactory(adapter);

        adapter.deployMe(
                getVertx(),
                new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER)
        ).onComplete(testContext.succeedingThenComplete());
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) throws IOException {
        adapter.close();
        adapter.undeployMe()
               .onComplete(ar -> testContext.completeNow());
    }

    /**
     * 测试写入单个日志记录到文件。
     * <p>
     * 验证日志是否正确写入文件。
     */
    @Test
    void testWriteSingleLogRecord(VertxTestContext testContext) throws Throwable {
        String topic = "test-topic";
        Logger logger = loggerFactory.createLogger(topic);
        logger.info("Test log message");

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        Path logFile = tempDir.resolve(topic + ".log");
        assertTrue(Files.exists(logFile), "Log file should exist");

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty(), "Log file should not be empty");

        boolean found = lines.stream()
                             .anyMatch(line -> line.contains("Test log message"));
        assertTrue(found, "Log message should be found in the file");

        testContext.completeNow();
    }

    /**
     * 测试写入多个日志记录到同一个文件。
     * <p>
     * 验证多条日志是否都正确写入文件。
     */
    @Test
    void testWriteMultipleLogRecordsToSameTopic(VertxTestContext testContext) throws Throwable {
        String topic = "multi-test";
        Logger logger = loggerFactory.createLogger(topic);

        for (int i = 1; i <= 5; i++) {
            logger.info("Log message " + i);
        }

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        Path logFile = tempDir.resolve(topic + ".log");
        assertTrue(Files.exists(logFile), "Log file should exist");

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty(), "Log file should not be empty");

        for (int i = 1; i <= 5; i++) {
            final int index = i;
            boolean found = lines.stream()
                                 .anyMatch(line -> line.contains("Log message " + index));
            assertTrue(found, "Log message " + i + " should be found in the file");
        }

        testContext.completeNow();
    }

    /**
     * 测试写入日志到不同的主题。
     * <p>
     * 验证不同主题的日志是否写入到不同的文件。
     */
    @Test
    void testWriteLogsToDifferentTopics(VertxTestContext testContext) throws Throwable {
        String topic1 = "topic1";
        String topic2 = "topic2";

        Logger logger1 = loggerFactory.createLogger(topic1);
        Logger logger2 = loggerFactory.createLogger(topic2);

        logger1.info("Message for topic1");
        logger2.info("Message for topic2");

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        Path logFile1 = tempDir.resolve(topic1 + ".log");
        Path logFile2 = tempDir.resolve(topic2 + ".log");

        assertTrue(Files.exists(logFile1), "Log file 1 should exist");
        assertTrue(Files.exists(logFile2), "Log file 2 should exist");

        List<String> lines1 = Files.readAllLines(logFile1);
        List<String> lines2 = Files.readAllLines(logFile2);

        boolean found1 = lines1.stream()
                               .anyMatch(line -> line.contains("Message for topic1"));
        assertTrue(found1, "Topic1 message should be in file 1");

        boolean found2 = lines2.stream()
                               .anyMatch(line -> line.contains("Message for topic2"));
        assertTrue(found2, "Topic2 message should be in file 2");

        testContext.completeNow();
    }

    /**
     * 测试当getFileWriterForTopic返回null时，日志应被丢弃。
     * <p>
     * 验证返回null的主题不会创建日志文件。
     */
    @Test
    void testDiscardLogsWhenFileWriterIsNull(VertxTestContext testContext) throws Throwable {
        String nullTopic = "null-topic";
        Logger logger = loggerFactory.createLogger(nullTopic);
        logger.info("This should be discarded");

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        Path logFile = tempDir.resolve(nullTopic + ".log");
        assertFalse(Files.exists(logFile), "Log file should not exist for null-topic");

        testContext.completeNow();
    }

    /**
     * 测试日志记录的渲染功能。
     * <p>
     * 验证render方法是否被正确调用并格式化日志内容。
     */
    @Test
    void testLogRendering(VertxTestContext testContext) throws Throwable {
        String topic = "render-test";
        Logger logger = loggerFactory.createLogger(topic);
        logger.info("Test rendering");

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        Path logFile = tempDir.resolve(topic + ".log");
        assertTrue(Files.exists(logFile), "Log file should exist");

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty(), "Log file should not be empty");

        boolean found = lines.stream()
                             .anyMatch(line -> line.contains("[render-test]") && line.contains("Test rendering"));
        assertTrue(found, "Rendered log should contain topic and message");

        testContext.completeNow();
    }

    /**
     * 测试直接通过accept方法写入日志。
     * <p>
     * 验证adapter的accept方法可以直接接收SpecificLog并写入文件。
     */
    @Test
    void testDirectAccept(VertxTestContext testContext) throws Throwable {
        String topic = "direct-test";
        Log log = new Log();
        log.message("Direct accept test");

        adapter.accept(topic, log);

        testContext.awaitCompletion(3, TimeUnit.SECONDS);

        Path logFile = tempDir.resolve(topic + ".log");
        assertTrue(Files.exists(logFile), "Log file should exist");

        List<String> lines = Files.readAllLines(logFile);
        assertFalse(lines.isEmpty(), "Log file should not be empty");

        boolean found = lines.stream()
                             .anyMatch(line -> line.contains("Direct accept test"));
        assertTrue(found, "Direct accept message should be found in the file");

        testContext.completeNow();
    }

    /**
     * 测试用的 FileLogWriterAdapter 实现。
     * <p>
     * 为每个主题创建独立的日志文件，除了 "null-topic" 返回 null。
     */
    private static class TestFileLogWriterAdapter extends FileLogWriterAdapter {
        private final Path logDir;
        private final Map<String, FileWriter> fileWriters = new ConcurrentHashMap<>();

        public TestFileLogWriterAdapter(Path logDir) {
            super();
            this.logDir = logDir;
        }

        @Override
        protected @Nullable FileWriter getFileWriterForTopic(String topic) {
            if ("null-topic".equals(topic)) {
                return null;
            }

            return fileWriters.computeIfAbsent(topic, t -> {
                try {
                    File file = logDir.resolve(t + ".log").toFile();
                    return new FileWriter(file, true);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create FileWriter for topic: " + t, e);
                }
            });
        }

        @Override
        public String render(String topic, SpecificLog<?> log) {
            String message = log.message();
            if (message == null) {
                message = "";
            }
            return String.format("[%s] %s", topic, message);
        }

        public void close() throws IOException {
            for (FileWriter writer : fileWriters.values()) {
                writer.flush();
                writer.close();
            }
            fileWriters.clear();
        }
    }

    /**
     * 测试用的 LoggerFactory 实现。
     * <p>
     * 使用指定的 FileLogWriterAdapter 创建 Logger 实例。
     */
    private static class TestLoggerFactory extends io.github.sinri.keel.logger.api.factory.BaseLoggerFactory {
        public TestLoggerFactory(TestFileLogWriterAdapter adapter) {
            super(adapter);
        }
    }
}