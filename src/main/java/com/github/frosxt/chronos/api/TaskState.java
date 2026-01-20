package com.github.frosxt.chronos.api;

/**
 * Represents the current state of a scheduled task.
 *
 * <p>
 * Tasks progress through states based on their execution lifecycle
 * and the configured execution policy.
 */
public enum TaskState {

    /**
     * The task is scheduled and waiting for its next execution time.
     */
    SCHEDULED,

    /**
     * The task is currently executing.
     */
    RUNNING,

    /**
     * The task failed and is waiting for a retry attempt.
     */
    RETRY_WAIT,

    /**
     * The task was cancelled before completion.
     */
    CANCELLED,

    /**
     * The task completed successfully (applicable to one-shot tasks).
     */
    COMPLETED,

    /**
     * The task failed and no more retries are available.
     */
    FAILED
}
