package io.github.sinri.keel.logger.issue.record;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 * @since 4.0.0
 */
public final class KeelEventLog extends AbstractIssueRecord<KeelEventLog> {

    public KeelEventLog() {
        super();
    }

    /**
     * @since 4.0.0
     */
    public KeelEventLog(KeelIssueRecord<?> baseIssueRecord) {
        super(baseIssueRecord);
    }

    @Nonnull
    @Override
    public KeelEventLog getImplementation() {
        return this;
    }
}

