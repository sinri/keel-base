package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.utils.StackUtils;

import javax.annotation.Nonnull;
import java.util.Set;


/**
 * @since 3.1.10
 */
public interface KeelIssueRecordRender<R> {

    @Nonnull
    R renderIssueRecord(@Nonnull String topic, @Nonnull KeelIssueRecord<?> issueRecord);

    @Nonnull
    R renderThrowable(@Nonnull Throwable throwable);

    @Nonnull
    default Set<String> ignorableStackPackageSet() {
        return StackUtils.IgnorableCallStackPackage;
    }
}
