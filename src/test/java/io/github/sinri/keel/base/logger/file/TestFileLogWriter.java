package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.base.logger.adapter.FileLogWriterAdapter;
import io.vertx.core.Future;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * 文件日志写入器的测试实现。
 * <p>
 * 用于测试场景下提供一个可工作的 {@link FileLogWriterAdapter} 实现，将日志按 topic 写入到配置的目录中。
 *
 * @since 5.0.0
 */
@NullMarked
public class TestFileLogWriter extends FileLogWriterAdapter {
    private final String logDir;

    public TestFileLogWriter() {
        super();
        this.logDir = Objects.requireNonNull(ConfigElement.root().readProperty("log_dir"));
    }

    @Override
    protected Future<Void> prepareForLoop() {
        return Future.succeededFuture();
    }

    @Override
    protected @Nullable FileWriter getFileWriterForTopic(String topic) {
        try {
            return new FileWriter(this.logDir + File.separator + topic + ".log");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
