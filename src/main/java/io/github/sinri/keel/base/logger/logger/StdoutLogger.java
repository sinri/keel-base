package io.github.sinri.keel.base.logger.logger;

import io.github.sinri.keel.base.logger.adapter.StdoutLogWriter;
import io.github.sinri.keel.logger.api.logger.BaseLogger;
import org.jetbrains.annotations.NotNull;

/**
 * 面向标准输出的日志记录器。
 * <p>
 * 将日志输出到标准输出。
 *
 * @since 5.0.0
 */
public final class StdoutLogger extends BaseLogger {

    public StdoutLogger(@NotNull String topic) {
        super(topic, StdoutLogWriter.getInstance());
    }
}
