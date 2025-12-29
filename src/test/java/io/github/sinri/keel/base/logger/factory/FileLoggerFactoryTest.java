package io.github.sinri.keel.base.logger.factory;

import io.github.sinri.keel.base.KeelSampleImpl;
import io.github.sinri.keel.base.logger.adapter.FileLogWriterAdapter;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileLoggerFactory单元测试。
 *
 * @since 5.0.0
 */
@ExtendWith(io.vertx.junit5.VertxExtension.class)
class FileLoggerFactoryTest {
    private TestFileLoggerFactory factory;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        File logFile = tempDir.resolve("factory-test.log").toFile();
        factory = new TestFileLoggerFactory(logFile);
    }

    @Test
    void testSharedAdapter() {
        FileLogWriterAdapter adapter = factory.sharedAdapter();
        assertNotNull(adapter, "sharedAdapter不应为null");
        assertInstanceOf(FileLogWriterAdapter.class, adapter, "sharedAdapter应为FileLogWriterAdapter实例");
    }

    @Test
    void testCreateLogger() {
        Logger logger = factory.createLogger("test-topic");
        assertNotNull(logger, "创建的Logger不应为null");
    }

    @Test
    void testCreateLoggerWithDifferentTopics() {
        Logger logger1 = factory.createLogger("topic1");
        Logger logger2 = factory.createLogger("topic2");

        assertNotNull(logger1, "topic1的Logger不应为null");
        assertNotNull(logger2, "topic2的Logger不应为null");
        assertNotSame(logger1, logger2, "不同topic的Logger应该是不同的实例");
    }

    @Test
    void testCreateSpecificLogger() {
        SpecificLogger<?> logger = factory.createLogger("test-topic", () -> null);
        assertNotNull(logger, "创建的SpecificLogger不应为null");
    }

    @Test
    void testCreateSpecificLoggerWithDifferentTopics() {
        SpecificLogger<?> logger1 = factory.createLogger("topic1", () -> null);
        SpecificLogger<?> logger2 = factory.createLogger("topic2", () -> null);

        assertNotNull(logger1, "topic1的SpecificLogger不应为null");
        assertNotNull(logger2, "topic2的SpecificLogger不应为null");
        assertNotSame(logger1, logger2, "不同topic的SpecificLogger应该是不同的实例");
    }

    @Test
    void testSameAdapterForSameFactory() {
        FileLogWriterAdapter adapter1 = factory.sharedAdapter();
        FileLogWriterAdapter adapter2 = factory.sharedAdapter();

        assertSame(adapter1, adapter2, "同一工厂的sharedAdapter应该是同一个实例");
    }

    /**
     * 测试用的FileLoggerFactory实现。
     */
    private static class TestFileLoggerFactory extends FileLoggerFactory {
        private final File logFile;
        private FileLogWriterAdapter adapter;

        TestFileLoggerFactory(@NotNull File logFile) {
            this.logFile = logFile;
        }

        @Override
        public @NotNull FileLogWriterAdapter sharedAdapter() {
            if (adapter == null) {
                adapter = new TestFileLogWriterAdapter(logFile);
            }
            return adapter;
        }
    }

    /**
     * 测试用的FileLogWriterAdapter实现。
     */
    private static class TestFileLogWriterAdapter extends FileLogWriterAdapter {
        private final File logFile;
        private final java.util.Map<String, FileWriter> fileWriterMap = new ConcurrentHashMap<>();

        TestFileLogWriterAdapter(@NotNull File logFile) {
            super(KeelSampleImpl.Keel);
            this.logFile = logFile;
        }

        @Override
        protected java.io.FileWriter getFileWriterForTopic(@NotNull String topic) {
            return fileWriterMap.computeIfAbsent(topic, k -> {
                try {
                    return new FileWriter(logFile, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        @Override
        public void close() {
            fileWriterMap.forEach((topic, writer) -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    // 忽略关闭错误
                }
            });
            fileWriterMap.clear();
            super.close();
        }
    }
}