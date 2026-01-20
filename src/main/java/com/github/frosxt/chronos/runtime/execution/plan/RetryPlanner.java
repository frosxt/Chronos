package com.github.frosxt.chronos.runtime.execution.plan;

import com.github.frosxt.chronos.api.policy.RetryPolicy;
import com.github.frosxt.chronos.runtime.task.TaskControl;

/**
 * Computes retry delays based on the configured retry policy.
 */
public final class RetryPlanner {

    private RetryPlanner() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Computes the delay for the next retry attempt.
     *
     * @param control the task control
     * @return the delay in nanoseconds, or -1 if no more retries are allowed
     */
    public static long computeRetryDelay(final TaskControl control) {
        final RetryPolicy policy = control.executionPolicy().retryPolicy();
        if (policy == null) {
            return -1;
        }

        final int attempt = control.retryAttempt();
        if (attempt >= policy.maxAttempts()) {
            return -1;
        }

        final long baseDelay = policy.delayNanosForAttempt(attempt + 1);
        return control.jitter().apply(baseDelay);
    }

    /**
     * Returns whether more retries are available.
     *
     * @param control the task control
     * @return true if more retries are available
     */
    public static boolean hasMoreRetries(final TaskControl control) {
        final RetryPolicy policy = control.executionPolicy().retryPolicy();
        if (policy == null) {
            return false;
        }
        return control.retryAttempt() < policy.maxAttempts();
    }
}
