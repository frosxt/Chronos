package com.github.frosxt.chronos.api.listener;

/**
 * A listener for task execution events.
 *
 * <p>
 * Implementations receive callbacks at various points in the task lifecycle:
 * when execution starts, when it completes successfully, and when it fails.
 *
 * <p>
 * Listeners should not throw exceptions; any thrown exceptions are ignored.
 *
 * <p>
 * Implementations must be thread-safe as callbacks may be invoked from
 * multiple threads concurrently.
 */
public interface TaskListener {

    /**
     * Called when a task execution starts.
     *
     * <p>
     * The context's {@link TaskContext#endTime()} and
     * {@link TaskContext#duration()}
     * will be null at this point.
     *
     * @param context the execution context
     */
    void onStart(TaskContext context);

    /**
     * Called when a task execution completes successfully.
     *
     * @param context the execution context
     */
    void onSuccess(TaskContext context);

    /**
     * Called when a task execution fails with an exception.
     *
     * <p>
     * This is called before any retry behavior is applied.
     *
     * @param context the execution context
     * @param error   the exception that caused the failure
     */
    void onFailure(TaskContext context, Throwable error);
}
