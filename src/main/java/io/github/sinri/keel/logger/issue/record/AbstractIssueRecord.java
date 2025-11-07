package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIssueRecord<T> implements KeelIssueRecord<T> {
    private final @Nonnull JsonObject attributes;
    private final @Nonnull List<String> classification;
    private final String threadInfo;
    private long timestamp;
    private @Nonnull KeelLogLevel level;
    private @Nullable Throwable exception;

    public AbstractIssueRecord() {
        this.timestamp = System.currentTimeMillis();
        this.attributes = new JsonObject();
        this.level = KeelLogLevel.INFO;
        this.classification = new ArrayList<>();
        //        this.threadId=Thread.currentThread().getId();
        //        this.threadName=Thread.currentThread().toString().getName();
        this.threadInfo = Thread.currentThread().toString();
    }

    /**
     * @since 4.0.0 for KeelEventLog
     */
    protected AbstractIssueRecord(@Nonnull KeelIssueRecord<?> baseIssueRecord) {
        this.attributes = baseIssueRecord.attributes();
        this.classification = baseIssueRecord.classification();
        this.timestamp = baseIssueRecord.timestamp();
        this.level = baseIssueRecord.level();
        this.exception = baseIssueRecord.exception();
        this.threadInfo = Thread.currentThread().toString();
    }

    @Override
    final public T timestamp(long timestamp) {
        this.timestamp = timestamp;
        return getImplementation();
    }

    @Override
    final public long timestamp() {
        return timestamp;
    }

    @Override
    final public T level(@Nonnull KeelLogLevel level) {
        this.level = level;
        return getImplementation();
    }

    @Nonnull
    @Override
    final public KeelLogLevel level() {
        return level;
    }

    @Override
    final public T classification(@Nonnull List<String> classification) {
        this.classification.clear();
        this.classification.addAll(classification);
        return getImplementation();
    }

    @Nonnull
    @Override
    final public List<String> classification() {
        return classification;
    }

    final protected void attribute(@Nonnull String name, @Nullable Object value) {
        if (
                AttributeLevel.equalsIgnoreCase(name)
                        || AttributeException.equalsIgnoreCase(name)
                        || AttributeClassification.equalsIgnoreCase(name)
        ) {
            throw new IllegalArgumentException("Attribute name `" + name + "` reserved");
        }
        attributes.put(name, value);
    }

    @Nonnull
    @Override
    public JsonObject attributes() {
        return attributes;
    }

    @Override
    final public T exception(@Nonnull Throwable throwable) {
        this.exception = throwable;
        return getImplementation();
    }

    @Nullable
    @Override
    final public Throwable exception() {
        return exception;
    }

    @Nullable
    @Override
    final public String message() {
        return this.attributes.getString(IssueRecordMessageMixin.AttributeMessage);
    }

    @Override
    final public T message(@Nonnull String message) {
        this.attribute(IssueRecordMessageMixin.AttributeMessage, message);
        return getImplementation();
    }

    @Nonnull
    private JsonObject ensureContextAttribute() {
        if (this.attributes.containsKey(AttributeContext)) {
            return this.attributes.getJsonObject(AttributeContext);
        }

        synchronized (this.attributes) {
            if (this.attributes.containsKey(AttributeContext)) {
                return this.attributes.getJsonObject(AttributeContext);
            }

            JsonObject j = new JsonObject();
            this.attribute(AttributeContext, j);
            return j;
        }
    }

    @Override
    public T context(@Nonnull Handler<JsonObject> contextHandler) {
        contextHandler.handle(ensureContextAttribute());
        return this.getImplementation();
    }

    /**
     * @since 4.1.0
     */
    @Override
    public String getThreadInfo() {
        return threadInfo;
    }
}
