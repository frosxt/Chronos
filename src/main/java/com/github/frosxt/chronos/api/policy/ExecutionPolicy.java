package com.github.frosxt.chronos.api.policy;

/**
 * Defines how task failures should be handled.
 *
 * <p>
 * An execution policy determines whether a task should stop, continue,
 * or retry when an exception occurs during execution.
 *
 * <p>
 * This class is immutable and thread-safe.
 */
public final class ExecutionPolicy {

    /**
     * The type of failure handling behavior.
     */
    public enum Type {
        /**
         * Stop the task on failure, transitioning to FAILED state.
         */
        STOP_ON_FAILURE,

        /**
         * Continue with the next scheduled execution on failure.
         * For one-shot tasks, this behaves the same as STOP_ON_FAILURE.
         */
        CONTINUE_ON_FAILURE,

        /**
         * Retry the execution according to a retry policy.
         */
        RETRY
    }

    private static final ExecutionPolicy STOP_ON_FAILURE = new ExecutionPolicy(Type.STOP_ON_FAILURE, null);
    private static final ExecutionPolicy CONTINUE_ON_FAILURE = new ExecutionPolicy(Type.CONTINUE_ON_FAILURE, null);

    private final Type type;
    private final RetryPolicy retryPolicy;

    private ExecutionPolicy(final Type type, final RetryPolicy retryPolicy) {
        this.type = type;
        this.retryPolicy = retryPolicy;
    }

    /**
     * Returns a policy that stops the task on failure.
     *
     * <p>
     * The task transitions to
     * {@link com.github.frosxt.chronos.api.TaskState#FAILED}
     * state after an unhandled exception.
     *
     * @return the stop-on-failure policy
     */
    public static ExecutionPolicy stopOnFailure() {
        return STOP_ON_FAILURE;
    }

    /**
     * Returns a policy that continues to the next scheduled execution on failure.
     *
     * <p>
     * For recurring tasks, the next execution proceeds as scheduled.
     * For one-shot tasks, this behaves identically to {@link #stopOnFailure()}.
     *
     * @return the continue-on-failure policy
     */
    public static ExecutionPolicy continueOnFailure() {
        return CONTINUE_ON_FAILURE;
    }

    /**
     * Returns a policy that retries failed executions according to the specified
     * retry policy.
     *
     * @param retryPolicy the retry policy to apply
     * @return the retry policy
     * @throws NullPointerException if retryPolicy is null
     */
    public static ExecutionPolicy retry(final RetryPolicy retryPolicy) {
        if (retryPolicy == null) {
            throw new NullPointerException("retryPolicy must not be null");
        }
        return new ExecutionPolicy(Type.RETRY, retryPolicy);
    }

    /**
     * Returns the type of this execution policy.
     *
     * @return the policy type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the retry policy if this is a retry execution policy.
     *
     * @return the retry policy, or null if this is not a retry policy
     */
    public RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    @Override
    public String toString() {
        if (type == Type.RETRY) {
            return "ExecutionPolicy[RETRY, " + retryPolicy + "]";
        }
        return "ExecutionPolicy[" + type + "]";
    }
}
