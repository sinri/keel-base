package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class SilentIssueRecorder<T extends KeelIssueRecord<T>> implements KeelIssueRecorder<T> {
    @Nonnull
    @Override
    public KeelLogLevel getVisibleLevel() {
        return KeelLogLevel.SILENT;
    }

    @Override
    public void setVisibleLevel(@Nonnull KeelLogLevel level) {
    }

    @Override
    public KeelIssueRecordCenter issueRecordCenter() {
        return null;
    }

    @Nonnull
    @Override
    public Supplier<T> issueRecordBuilder() {
        return () -> null;
    }

    @Nonnull
    @Override
    public String topic() {
        return "";
    }

    @Nullable
    @Override
    public Handler<T> getRecordFormatter() {
        return null;
    }

    @Override
    public void setRecordFormatter(@Nullable Handler<T> handler) {

    }

    @Override
    public void addBypassIssueRecorder(@Nonnull KeelIssueRecorder<T> bypassIssueRecorder) {

    }

    @Nonnull
    @Override
    public List<KeelIssueRecorder<T>> getBypassIssueRecorders() {
        return List.of();
    }
}
