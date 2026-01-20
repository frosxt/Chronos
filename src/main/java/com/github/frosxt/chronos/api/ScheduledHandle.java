package com.github.frosxt.chronos.api;

import java.time.Instant;

/**
 * A handle to a scheduled task that provides control and status information.
 *
 * <p>
 * This interface allows clients to monitor task execution, cancel tasks,
 * and retrieve timing information.
 */
public interface ScheduledHandle {

    /**
     * Returns the unique identifier for this scheduled task.
     *
     * @return the task identifier, never null
     */
    String id();

    /**
     * Attempts to cancel this scheduled task.
     *
     * <p>
     * If the task is in {@link TaskState#SCHEDULED} or {@link TaskState#RETRY_WAIT}
     * state, it will be cancelled immediately. If the task is currently running,
     * a cancellation flag is set but the running execution is not interrupted.
     *
     * @return {@code true} if the task was successfully cancelled or was already
     *         cancelled, {@code false} if cancellation was not possible
     */
    boolean cancel();

    /**
     * Returns whether this task has been cancelled.
     *
     * @return {@code true} if cancelled, {@code false} otherwise
     */
    boolean isCancelled();

    /**
     * Returns whether this task has completed, either normally or exceptionally.
     *
     * <p>
     * A task is considered done if its state is one of:
     * {@link TaskState#COMPLETED}, {@link TaskState#FAILED}, or
     * {@link TaskState#CANCELLED}.
     *
     * @return {@code true} if the task is done, {@code false} otherwise
     */
    boolean isDone();

    /**
     * Returns whether this task is currently executing.
     *
     * @return {@code true} if the task is running, {@code false} otherwise
     */
    boolean isRunning();

    /**
     * Returns the wall-clock time when this task was first scheduled.
     *
     * <p>
     * This is a best-effort projection and may not be exact.
     *
     * @return the first scheduled time, or null if not yet scheduled
     */
    Instant firstScheduledTime();

    /**
     * Returns the wall-clock time when the last execution started.
     *
     * @return the last start time, or null if the task has never started
     */
    Instant lastStartTime();

    /**
     * Returns the wall-clock time when the last execution ended.
     *
     * @return the last end time, or null if no execution has completed
     */
    Instant lastEndTime();

    /**
     * Returns the wall-clock time of the next scheduled execution.
     *
     * <p>
     * This is a best-effort projection based on the current trigger state.
     *
     * @return the next scheduled time, or null if no future execution is planned
     */
    Instant nextScheduledTime();

    /**
     * Returns the number of times this task has been executed.
     *
     * @return the run count, always non-negative
     */
    long runCount();

    /**
     * Returns the current state of this task.
     *
     * @return the task state, never null
     */
    TaskState state();
}
