package io.github.sinri.keel.base.logger.logger;

import io.github.sinri.keel.base.logger.adapter.StdoutLogWriter;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.github.sinri.keel.logger.api.logger.BaseSpecificLogger;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Keel 体系下的面向标准输出的特定问题记录器。
 *
 * @param <T> 特定问题记录的类型
 * @since 5.0.0
 */
public class StdoutSpecificLogger<T extends SpecificLog<T>> extends BaseSpecificLogger<T> {

    public StdoutSpecificLogger(@NotNull String topic, @NotNull Supplier<T> issueRecordSupplier) {
        super(topic, issueRecordSupplier, StdoutLogWriter.getInstance());
    }

}
