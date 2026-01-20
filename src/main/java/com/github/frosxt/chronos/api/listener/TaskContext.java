package com.github.frosxt.chronos.api.listener;

import com.github.frosxt.chronos.api.TaskType;

import java.time.Duration;
import java.time.Instant;

/**
 * Provides context information about a task execution event.
 *
 * <p>
 * This interface is passed to {@link TaskListener} callbacks to provide
 * metadata about the current execution.
 *
 * <p>
 * Implementations of this interface are immutable.
 */
public interface TaskContext {

    /**
     * Returns the unique identifier of the task.
     *
     * @return the task ID, never null
     */
    String taskId();

    /**
     * Returns the type of scheduling for this task.
     *
     * @return the task type, never null
     */
    TaskType type();

    /**
     * Returns the execution number for this task (1-based).
     *
     * <p>
     * The first execution is run number 1.
     *
     * @return the run number
     */
    long runNumber();

    /**
     * Returns the wall-clock time when this execution was scheduled to start.
     *
     * @return the scheduled time, never null
     */
    Instant scheduledTime();

    /**
     * Returns the wall-clock time when this execution actually started.
     *
     * @return the start time, never null
     */
    Instant startTime();

    /**
     * Returns the wall-clock time when this execution ended.
     *
     * <p>
     * This is only available in {@link TaskListener#onSuccess} and
     * {@link TaskListener#onFailure} callbacks.
     *
     * @return the end time, or null if the execution has not yet completed
     */
    Instant endTime();

    /**
     * Returns the duration of this execution.
     *
     * <p>
     * This is only available in {@link TaskListener#onSuccess} and
     * {@link TaskListener#onFailure} callbacks.
     *
     * @return the execution duration, or null if the execution has not yet
     *         completed
     */
    Duration duration();

    /**
     * Returns the wall-clock time of the next scheduled execution.
     *
     * <p>
     * For one-shot tasks, this is always null after execution completes.
     *
     * @return the next scheduled time, or null if no future execution is planned
     */
    Instant nextScheduledTime();
}
