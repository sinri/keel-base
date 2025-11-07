package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;

/**
 * The recorder of issues. A special implementation, {@code  KeelIssueRecorder<KeelEventLog>} is just called logger.
 *
 * @param <T> The type of the certain implementation of the issue record used.
 * @since 3.1.10
 * @since 4.0.1 T is strict.
 */
public interface KeelIssueRecorder<T extends KeelIssueRecord<T>>
        extends KeelIssueRecorderCommonMixin<T>, KeelIssueRecorderJsonMixin<T> {
}
