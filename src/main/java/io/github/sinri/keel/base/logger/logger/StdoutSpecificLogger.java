package io.github.sinri.keel.base.logger.logger;

import io.github.sinri.keel.base.logger.adapter.StdoutLogWriter;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.github.sinri.keel.logger.api.logger.BaseSpecificLogger;
import org.jspecify.annotations.NullMarked;

import java.util.function.Supplier;

/**
 * 面向标准输出的特定日志记录器。
 *
 * @param <T> 特定日志的类型
 * @since 5.0.0
 */
@NullMarked
public class StdoutSpecificLogger<T extends SpecificLog<T>> extends BaseSpecificLogger<T> {

    public StdoutSpecificLogger(String topic, Supplier<T> issueRecordSupplier) {
        super(topic, issueRecordSupplier, StdoutLogWriter.getInstance());
    }

}
