package io.github.sinri.keel.base.logger.file;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.base.logger.adapter.FileLogWriterAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class TestFileLogWriter extends FileLogWriterAdapter {
    private final @NotNull String logDir;

    public TestFileLogWriter(@NotNull Keel keel) {
        super(keel);
        this.logDir = Objects.requireNonNull(keel.config("log_dir"));
    }

    @Override
    protected @Nullable FileWriter getFileWriterForTopic(@NotNull String topic) {
        try {
            return new FileWriter(this.logDir + File.separator + topic + ".log");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
