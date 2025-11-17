package io.github.sinri.keel.base.logger.factory;


import io.github.sinri.keel.base.logger.adapter.StdoutLogWriter;
import io.github.sinri.keel.base.logger.logger.StdoutLogger;
import io.github.sinri.keel.base.logger.logger.StdoutSpecificLogger;
import io.github.sinri.keel.logger.api.adapter.LogWriterAdapter;
import io.github.sinri.keel.logger.api.factory.BaseLoggerFactory;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 面向标准输出的日志记录器工厂。
 *
 * @since 5.0.0
 */
public final class StdoutLoggerFactory extends BaseLoggerFactory {
    private static final StdoutLoggerFactory instance = new StdoutLoggerFactory();

    private StdoutLoggerFactory() {
    }

    public static StdoutLoggerFactory getInstance() {
        return instance;
    }

    @Override
    public LogWriterAdapter sharedAdapter() {
        return StdoutLogWriter.getInstance();
    }

    @Override
    public Logger createLogger(@NotNull String topic) {
        return new StdoutLogger(topic);
    }

    @Override
    public <L extends SpecificLog<L>> SpecificLogger<L> createLogger(@NotNull String topic, @NotNull Supplier<L> specificLogSupplier) {
        return new StdoutSpecificLogger<>(topic, specificLogSupplier);
    }
}
