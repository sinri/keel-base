package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.base.annotations.SelfInterface;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 3.2.0
 * @since 4.0.0 package protected
 */
interface KeelIssueRecordCore<T> extends SelfInterface<T> {
    String AttributeClassification = "classification";
    String AttributeLevel = "level";
    String AttributeException = "exception";

    T timestamp(long timestamp);

    long timestamp();

    //    @Nonnull
    //    UnmodifiableJsonifiableEntity attributes();

    @Nonnull
    JsonObject attributes();

    T exception(@Nonnull Throwable throwable);

    @Nullable
    Throwable exception();

    T classification(@Nonnull List<String> classification);

    default T classification(@Nonnull String... classification) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, classification);
        return classification(list);
    }

    @Nonnull
    List<String> classification();

    T level(@Nonnull KeelLogLevel level);

    @Nonnull
    KeelLogLevel level();
}
