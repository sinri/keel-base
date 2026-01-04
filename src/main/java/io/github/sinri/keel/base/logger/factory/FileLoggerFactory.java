package io.github.sinri.keel.base.logger.factory;

import io.github.sinri.keel.base.logger.adapter.FileLogWriterAdapter;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.github.sinri.keel.logger.api.logger.BaseLogger;
import io.github.sinri.keel.logger.api.logger.BaseSpecificLogger;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

/**
 * 一个基本的向文件记录日志内容的实现。
 *
 * @since 5.0.0
 */
@NullMarked
public abstract class FileLoggerFactory implements LoggerFactory {
    @Override
    abstract public FileLogWriterAdapter sharedAdapter();

    @Override
    public Logger createLogger(String topic) {
        return new BaseLogger(topic, sharedAdapter());
    }

    @Override
    public <L extends SpecificLog<L>> SpecificLogger<L> createLogger(String topic, Supplier<L> specificLogSupplier) {
        return new BaseSpecificLogger<>(topic, specificLogSupplier, sharedAdapter());
    }
}
