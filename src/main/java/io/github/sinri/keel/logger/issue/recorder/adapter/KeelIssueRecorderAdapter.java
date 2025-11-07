package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.vertx.core.Closeable;
import io.vertx.core.Completable;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.1.10
 */
public interface KeelIssueRecorderAdapter extends Closeable {
    KeelIssueRecordRender<?> issueRecordRender();

    void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord);

    /**
     * By default, this method calls {@link KeelIssueRecorderAdapter#gracefullyClose()} to close the adapter and return
     * immediately.
     *
     * @param completion the promise to signal when close has completed
     * @since 4.1.3
     */
    @Override
    default void close(@Nonnull Completable<Void> completion) {
        gracefullyClose()
                .andThen(completion);
    }

    /**
     * Gracefully close the adapter.
     * <p>
     * If closed, the method {@link KeelIssueRecorderAdapter#isClosed()} should return {@code true}.
     *
     * @return the close result
     */
    Future<Void> gracefullyClose();

    boolean isClosed();
}
