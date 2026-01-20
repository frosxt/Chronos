package com.github.frosxt.chronos.api;

/**
 * Represents the type of scheduling for a task.
 */
public enum TaskType {

    /**
     * A one-shot task that executes once after a delay.
     */
    ONCE,

    /**
     * A recurring task that executes at a fixed rate, where the next
     * execution time is computed from the initial schedule time.
     */
    FIXED_RATE,

    /**
     * A recurring task that executes with a fixed delay between the
     * end of one execution and the start of the next.
     */
    FIXED_DELAY,

    /**
     * A recurring task that executes according to a cron expression.
     */
    CRON
}
