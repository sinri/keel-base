package io.github.sinri.keel.base.logger.adapter;

import io.github.sinri.keel.base.Keel;
import io.github.sinri.keel.logger.api.adapter.LogTextRender;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * 将日志写入文件的一个基础定义。
 *
 * @since 5.0.0
 */
public abstract class FileLogWriterAdapter extends QueuedLogWriterAdapter implements LogTextRender {
    public FileLogWriterAdapter(@NotNull Keel keel) {
        super(keel);
    }

    /**
     * 根据给定的 topic 获取对应的 FileWriter，返回 null 视为丢弃日志。
     * <p>
     * 通过实现这个方法，可以实现多个 topic 写入一个日志文件。
     * <p>
     * 从性能角度考虑，避免频繁打开和关闭文件，可以考虑使用缓冲区或线程安全的文件写入器。
     * 所使用的 FileWriter 应在本类的 close 方法内关闭。
     */
    abstract protected @Nullable FileWriter getFileWriterForTopic(@NotNull String topic);

    @Override
    protected @NotNull Future<Void> processLogRecords(@NotNull String topic, @NotNull List<@NotNull SpecificLog<?>> batch) {
        FileWriter fileWriterForTopic = getFileWriterForTopic(topic);
        if (fileWriterForTopic == null) {
            // System.err.println("Discarding logs for topic " + topic + " as fileWriter is null");
            return Future.succeededFuture();
        }
        try {
            for (@NotNull SpecificLog<?> log : batch) {
                String text = render(topic, log);
                fileWriterForTopic.append(text).append("\n");
            }
            fileWriterForTopic.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Future.succeededFuture();
    }
}
