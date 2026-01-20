package com.github.frosxt.chronos.runtime.execution.failure;

import com.github.frosxt.chronos.api.TaskType;
import com.github.frosxt.chronos.api.policy.ExecutionPolicy;
import com.github.frosxt.chronos.runtime.execution.plan.RetryPlanner;
import com.github.frosxt.chronos.runtime.task.TaskControl;

/**
 * Handles task execution failures according to the configured execution policy.
 */
public final class FailureHandler {

    /**
     * The action to take after handling a failure.
     */
    public enum Action {
        /**
         * Schedule a retry attempt.
         */
        RETRY,

        /**
         * Continue with the next scheduled execution (for recurring tasks).
         */
        CONTINUE,

        /**
         * Mark the task as failed (terminal state).
         */
        FAIL
    }

    private FailureHandler() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Determines the action to take for a failed task execution.
     *
     * @param control the task control
     * @param error   the exception that caused the failure
     * @return the action to take
     */
    public static Action handleFailure(final TaskControl control, final Throwable error) {
        final ExecutionPolicy policy = control.executionPolicy();

        switch (policy.type()) {
            case STOP_ON_FAILURE:
                return Action.FAIL;

            case CONTINUE_ON_FAILURE:
                if (control.type() == TaskType.ONCE) {
                    return Action.FAIL;
                }
                return Action.CONTINUE;

            case RETRY:
                if (RetryPlanner.hasMoreRetries(control)) {
                    return Action.RETRY;
                }
                if (control.type() == TaskType.ONCE) {
                    return Action.FAIL;
                }
                return Action.CONTINUE;

            default:
                return Action.FAIL;
        }
    }
}
