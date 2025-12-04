package io.github.sinri.keel.base.logger.factory;

import io.github.sinri.keel.base.logger.adapter.FileLogWriterAdapter;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.github.sinri.keel.logger.api.logger.BaseLogger;
import io.github.sinri.keel.logger.api.logger.BaseSpecificLogger;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 一个基本的向文件记录日志内容的实现。
 *
 * @since 5.0.0
 */
public abstract class FileLoggerFactory implements LoggerFactory {
    @Override
    abstract public @NotNull FileLogWriterAdapter sharedAdapter();

    @Override
    public @NotNull Logger createLogger(@NotNull String topic) {
        return new BaseLogger(topic, sharedAdapter());
    }

    @Override
    public @NotNull <L extends SpecificLog<L>> SpecificLogger<L> createLogger(@NotNull String topic, @NotNull Supplier<L> specificLogSupplier) {
        return new BaseSpecificLogger<>(topic, specificLogSupplier, sharedAdapter());
    }
}
