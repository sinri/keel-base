package io.github.sinri.keel.logger.issue.record;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.1.10
 * @since 4.0.0 package protected
 */
interface IssueRecordMessageMixin<T> extends KeelIssueRecordCore<T> {
    String AttributeMessage = "message";

    T message(@Nonnull String message);

    @Nullable
    String message();
}
