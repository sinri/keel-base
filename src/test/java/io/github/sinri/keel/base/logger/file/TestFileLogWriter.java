package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.logger.adapter.FileLogWriterAdapter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

@NullMarked
public class TestFileLogWriter extends FileLogWriterAdapter {
    private final String logDir;

    public TestFileLogWriter(Keel keel) {
        super(keel);
        this.logDir = Objects.requireNonNull(keel.getConfiguration().readString("log_dir"));
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
