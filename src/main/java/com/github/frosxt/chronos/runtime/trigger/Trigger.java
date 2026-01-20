package com.github.frosxt.chronos.runtime.trigger;

/**
 * Internal interface for determining when a task should next execute.
 *
 * <p>
 * Triggers encapsulate the scheduling logic for different task types.
 */
public interface Trigger {

    /**
     * Calculates the delay in nanoseconds until the next execution.
     *
     * @param currentNanos   the current monotonic time in nanoseconds
     * @param lastStartNanos the monotonic time when the last execution started (-1
     *                       if never)
     * @param lastEndNanos   the monotonic time when the last execution ended (-1 if
     *                       never)
     * @param runCount       the number of times the task has been executed
     * @return the delay in nanoseconds, or -1 if no more executions are scheduled
     */
    long nextDelayNanos(long currentNanos, long lastStartNanos, long lastEndNanos, long runCount);

    /**
     * Returns whether this trigger produces recurring executions.
     *
     * @return true if recurring, false for one-shot
     */
    boolean isRecurring();
}
