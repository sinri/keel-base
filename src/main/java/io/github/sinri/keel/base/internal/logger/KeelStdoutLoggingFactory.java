package io.github.sinri.keel.base.internal.logger;


import io.github.sinri.keel.base.utils.StringUtils;
import io.github.sinri.keel.logger.api.consumer.BaseLogWriter;
import io.github.sinri.keel.logger.api.consumer.LogWriterAdapter;
import io.github.sinri.keel.logger.api.factory.LoggerFactory;
import io.github.sinri.keel.logger.api.log.SpecificLog;
import io.github.sinri.keel.logger.api.logger.BaseLogger;
import io.github.sinri.keel.logger.api.logger.BaseSpecificLogger;
import io.github.sinri.keel.logger.api.logger.Logger;
import io.github.sinri.keel.logger.api.logger.SpecificLogger;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class KeelStdoutLoggingFactory implements LoggerFactory {

    private final KeelTopicRecordConsumer consumer;

    public KeelStdoutLoggingFactory() {
        this.consumer = new KeelTopicRecordConsumer();
    }

    @Override
    public LogWriterAdapter sharedAdapter() {
        return consumer;
    }

    @Override
    public Logger createLogger(@NotNull String topic) {
        return new BaseLogger(topic, consumer);
    }

    @Override
    public <L extends SpecificLog<L>> SpecificLogger<L> createLogger(@NotNull String topic, @NotNull Supplier<L> specificLogSupplier) {
        return new BaseSpecificLogger<>(topic, specificLogSupplier, consumer);
    }

    static class KeelTopicRecordConsumer extends BaseLogWriter {
        @Override
        public String renderThrowable(@NotNull Throwable throwable) {
            return StringUtils.renderThrowableChain(throwable);
        }

        @Override
        public String renderClassification(@NotNull List<String> classification) {
            return new JsonArray(classification).encode();
        }

        @Override
        public String renderContext(@NotNull Map<String, Object> context) {
            return new JsonObject(context).encodePrettily();
        }
    }
}
