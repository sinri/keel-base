package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * An interface for recording issues, providing core functionality for issue management and logging
 * at various levels with support for customization and delegation.
 * This interface is designed to facilitate detailed issue recording and can be extended for
 * additional functionality.
 *
 * @param <T> The type of the {@link KeelIssueRecord} used by this recorder.
 * @since 4.0.1
 */
interface KeelIssueRecorderCore<T extends KeelIssueRecord<T>> {
    @Nonnull
    KeelLogLevel getVisibleLevel();

    void setVisibleLevel(@Nonnull KeelLogLevel level);

    KeelIssueRecordCenter issueRecordCenter();

    /**
     * @return an instance of issue, to be modified for details. If NULL supplied, no log would be handled.
     */
    @Nonnull
    Supplier<T> issueRecordBuilder();

    @Nonnull
    String topic();

    @Nullable
    Handler<T> getRecordFormatter();

    void setRecordFormatter(@Nullable Handler<T> handler);

    /**
     * @since 3.2.0
     */
    void addBypassIssueRecorder(@Nonnull KeelIssueRecorder<T> bypassIssueRecorder);

    /**
     * @since 3.2.0
     */
    @Nonnull
    List<KeelIssueRecorder<T>> getBypassIssueRecorders();

    /**
     * Record an issue (created with `issueRecordBuilder` and modified with `issueHandler`).
     * It may be handled later async, actually.
     *
     * @param issueHandler the handler to modify the base issue.
     */
    default void record(@Nonnull Handler<T> issueHandler) {
        T issue = this.issueRecordBuilder().get();
        if (issue == null) {
            return;
        }
        issueHandler.handle(issue);

        Handler<T> recordFormatter = getRecordFormatter();
        if (recordFormatter != null) {
            recordFormatter.handle(issue);
        }

        if (issue.level().isEnoughSeriousAs(getVisibleLevel())) {
            this.issueRecordCenter().getAdapter().record(topic(), issue);
        }

        getBypassIssueRecorders().forEach(keelIssueRecorder -> {
            if (issue.level().isEnoughSeriousAs(keelIssueRecorder.getVisibleLevel())) {
                keelIssueRecorder.issueRecordCenter().getAdapter().record(topic(), issue);
            }
        });
    }

    default void debug(@Nonnull Handler<T> issueHandler) {
        if (KeelLogLevel.DEBUG.isNegligibleThan(getVisibleLevel())) {
            return;
        }
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.DEBUG);
        });
    }

    default void info(@Nonnull Handler<T> issueHandler) {
        if (KeelLogLevel.INFO.isNegligibleThan(getVisibleLevel())) {
            return;
        }
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.INFO);
        });
    }

    default void notice(@Nonnull Handler<T> issueHandler) {
        if (KeelLogLevel.NOTICE.isNegligibleThan(getVisibleLevel())) {
            return;
        }
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.NOTICE);
        });
    }

    default void warning(@Nonnull Handler<T> issueHandler) {
        if (KeelLogLevel.WARNING.isNegligibleThan(getVisibleLevel())) {
            return;
        }
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.WARNING);
        });
    }

    default void error(@Nonnull Handler<T> issueHandler) {
        if (KeelLogLevel.ERROR.isNegligibleThan(getVisibleLevel())) {
            return;
        }
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.ERROR);
        });
    }

    default void fatal(@Nonnull Handler<T> issueHandler) {
        if (KeelLogLevel.FATAL.isNegligibleThan(getVisibleLevel())) {
            return;
        }
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.FATAL);
        });
    }

    default void exception(@Nonnull Throwable throwable, @Nonnull Handler<T> issueHandler) {
        error(t -> {
            t.exception(throwable);
            issueHandler.handle(t);
        });
    }

}
