package com.github.frosxt.chronos.api;

import java.time.Instant;

/**
 * A snapshot of scheduler metrics at a point in time.
 *
 * <p>
 * Provides observability into the scheduler's operational state.
 */
public interface SchedulerSnapshot {

    /**
     * Returns the time when this snapshot was taken.
     *
     * @return the snapshot time, never null
     */
    Instant snapshotTime();

    /**
     * Returns the total number of tasks currently registered.
     *
     * @return the total task count
     */
    long totalTaskCount();

    /**
     * Returns the number of tasks in the scheduled state.
     *
     * @return the scheduled task count
     */
    long scheduledCount();

    /**
     * Returns the number of tasks currently running.
     *
     * @return the running task count
     */
    long runningCount();

    /**
     * Returns the number of tasks waiting for retry.
     *
     * @return the retry wait count
     */
    long retryWaitCount();

    /**
     * Returns the number of completed tasks.
     *
     * @return the completed count
     */
    long completedCount();

    /**
     * Returns the number of failed tasks.
     *
     * @return the failed count
     */
    long failedCount();

    /**
     * Returns the number of cancelled tasks.
     *
     * @return the cancelled count
     */
    long cancelledCount();

    /**
     * Returns the total number of executions across all tasks.
     *
     * @return the total execution count
     */
    long totalExecutionCount();
}
