package io.github.sinri.keel.logger.issue.record;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.2.0
 * @since 4.0.0 package protected
 */
interface IssueRecordContextMixin<T> extends KeelIssueRecordCore<T> {
    String AttributeContext = "context";

    T context(@Nonnull Handler<JsonObject> contextHandler);

    default T context(@Nonnull String name, @Nullable Object item) {
        return context(j -> j.put(name, item));
    }

    String getThreadInfo();
}
