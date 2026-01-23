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
