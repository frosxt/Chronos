package com.github.frosxt.chronos.runtime.metrics.snapshot;

import com.github.frosxt.chronos.api.SchedulerSnapshot;

import java.time.Instant;

/**
 * Implementation of {@link SchedulerSnapshot}.
 */
public record SchedulerSnapshotImpl(Instant snapshotTime, long totalTaskCount, long scheduledCount, long runningCount,
                                    long retryWaitCount, long completedCount, long failedCount, long cancelledCount,
                                    long totalExecutionCount) implements SchedulerSnapshot {

    @Override
    public String toString() {
        return "SchedulerSnapshot[" +
                "time=" + snapshotTime +
                ", total=" + totalTaskCount +
                ", scheduled=" + scheduledCount +
                ", running=" + runningCount +
                ", retryWait=" + retryWaitCount +
                ", completed=" + completedCount +
                ", failed=" + failedCount +
                ", cancelled=" + cancelledCount +
                ", executions=" + totalExecutionCount +
                "]";
    }
}
