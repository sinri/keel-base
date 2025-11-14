package io.github.sinri.keel.base.internal.logger;


import io.github.sinri.keel.base.utils.StringUtils;
import io.github.sinri.keel.logger.api.consumer.BaseTopicRecordConsumer;
import io.github.sinri.keel.logger.api.consumer.TopicRecordConsumer;
import io.github.sinri.keel.logger.api.event.BaseEventRecorder;
import io.github.sinri.keel.logger.api.event.EventRecorder;
import io.github.sinri.keel.logger.api.factory.RecorderFactory;
import io.github.sinri.keel.logger.api.issue.BaseIssueRecorder;
import io.github.sinri.keel.logger.api.issue.IssueRecord;
import io.github.sinri.keel.logger.api.issue.IssueRecorder;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class KeelStdoutLoggingFactory implements RecorderFactory {

    private final KeelTopicRecordConsumer consumer;

    public KeelStdoutLoggingFactory() {
        this.consumer = new KeelTopicRecordConsumer();
    }

    @Override
    public TopicRecordConsumer sharedTopicRecordConsumer() {
        return consumer;
    }

    @Override
    public EventRecorder createEventRecorder(@NotNull String topic) {
        return new BaseEventRecorder(topic, consumer);
    }

    @Override
    public <L extends IssueRecord<L>> IssueRecorder<L> createIssueRecorder(@NotNull String topic, @NotNull Supplier<L> issueRecordSupplier) {
        return new BaseIssueRecorder<>(topic, issueRecordSupplier, consumer);
    }

    static class KeelTopicRecordConsumer extends BaseTopicRecordConsumer {
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
