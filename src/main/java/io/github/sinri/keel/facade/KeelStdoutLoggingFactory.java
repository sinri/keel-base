package io.github.sinri.keel.facade;


import io.github.sinri.keel.logger.api.event.EventRecorder;
import io.github.sinri.keel.logger.api.factory.RecorderFactory;
import io.github.sinri.keel.logger.api.issue.IssueRecord;
import io.github.sinri.keel.logger.api.issue.IssueRecorder;
import io.github.sinri.keel.logger.base.adapter.BaseTopicRecordConsumer;
import io.github.sinri.keel.logger.base.event.BaseEventRecorder;
import io.github.sinri.keel.logger.base.issue.BaseIssueRecorder;
import io.github.sinri.keel.utils.StringUtils;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

class KeelStdoutLoggingFactory implements RecorderFactory {

    private final KeelTopicRecordConsumer consumer;

    public KeelStdoutLoggingFactory() {
        this.consumer = new KeelTopicRecordConsumer();
    }

    @Override
    public EventRecorder createEventRecorder(@Nonnull String topic) {
        return new BaseEventRecorder(topic, consumer);
    }

    @Override
    public <L extends IssueRecord<L>> IssueRecorder<L> createIssueRecorder(@Nonnull String topic, @Nonnull Supplier<L> issueRecordSupplier) {
        return new BaseIssueRecorder<>(topic, issueRecordSupplier, consumer);
    }

    static class KeelTopicRecordConsumer extends BaseTopicRecordConsumer {
        @Override
        public String renderThrowable(@Nonnull Throwable throwable) {
            return StringUtils.renderThrowableChain(throwable);
        }

        @Override
        public String renderClassification(@Nonnull List<String> classification) {
            return new JsonArray(classification).encode();
        }

        @Override
        public String renderContext(@Nonnull Map<String, Object> context) {
            return new JsonObject(context).encodePrettily();
        }
    }
}
