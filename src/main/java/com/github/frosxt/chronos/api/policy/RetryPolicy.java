package com.github.frosxt.chronos.api.policy;

import java.time.Duration;

/**
 * Defines the retry behavior for failed task executions.
 *
 * <p>
 * A retry policy specifies how delays between retry attempts are calculated
 * and the maximum number of retry attempts allowed.
 *
 * <p>
 * This class is immutable and thread-safe.
 */
public final class RetryPolicy {

    /**
     * The type of retry delay calculation.
     */
    public enum Type {
        /**
         * Use a fixed delay between retries.
         */
        FIXED_DELAY,

        /**
         * Use exponential backoff with bounded maximum delay.
         */
        EXPONENTIAL_BACKOFF
    }

    private final Type type;
    private final long initialDelayNanos;
    private final long maxDelayNanos;
    private final double multiplier;
    private final int maxAttempts;

    private RetryPolicy(final Type type, final long initialDelayNanos, final long maxDelayNanos, final double multiplier, final int maxAttempts) {
        this.type = type;
        this.initialDelayNanos = initialDelayNanos;
        this.maxDelayNanos = maxDelayNanos;
        this.multiplier = multiplier;
        this.maxAttempts = maxAttempts;
    }

    /**
     * Creates a retry policy with a fixed delay between attempts.
     *
     * @param delay       the delay between retry attempts
     * @param maxAttempts the maximum number of retry attempts (after the initial
     *                    failure)
     * @return the retry policy
     * @throws NullPointerException     if delay is null
     * @throws IllegalArgumentException if delay is not positive or maxAttempts is
     *                                  less than 1
     */
    public static RetryPolicy fixedDelay(final Duration delay, final int maxAttempts) {
        if (delay == null) {
            throw new NullPointerException("delay must not be null");
        }
        if (delay.isNegative() || delay.isZero()) {
            throw new IllegalArgumentException("delay must be positive");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        final long delayNanos = toNanosSafe(delay);
        return new RetryPolicy(Type.FIXED_DELAY, delayNanos, delayNanos, 1.0, maxAttempts);
    }

    /**
     * Creates a retry policy with exponential backoff.
     *
     * <p>
     * Each retry delay is calculated as
     * {@code initialDelay * multiplier^attemptNumber},
     * capped at {@code maxDelay}.
     *
     * @param initialDelay the initial delay for the first retry
     * @param maxDelay     the maximum delay between retries
     * @param multiplier   the multiplier for each successive attempt (must be >=
     *                     1.0)
     * @param maxAttempts  the maximum number of retry attempts (after the initial
     *                     failure)
     * @return the retry policy
     * @throws NullPointerException     if initialDelay or maxDelay is null
     * @throws IllegalArgumentException if delays are not positive, multiplier <
     *                                  1.0, or maxAttempts < 1
     */
    public static RetryPolicy exponentialBackoff(final Duration initialDelay, final Duration maxDelay, final double multiplier, final int maxAttempts) {
        if (initialDelay == null) {
            throw new NullPointerException("initialDelay must not be null");
        }
        if (maxDelay == null) {
            throw new NullPointerException("maxDelay must not be null");
        }
        if (initialDelay.isNegative() || initialDelay.isZero()) {
            throw new IllegalArgumentException("initialDelay must be positive");
        }
        if (maxDelay.isNegative() || maxDelay.isZero()) {
            throw new IllegalArgumentException("maxDelay must be positive");
        }
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("multiplier must be at least 1.0");
        }
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be at least 1");
        }

        final long initialNanos = toNanosSafe(initialDelay);
        final long maxNanos = toNanosSafe(maxDelay);
        if (maxNanos < initialNanos) {
            throw new IllegalArgumentException("maxDelay must be at least as large as initialDelay");
        }

        return new RetryPolicy(Type.EXPONENTIAL_BACKOFF, initialNanos, maxNanos, multiplier, maxAttempts);
    }

    private static long toNanosSafe(final Duration duration) {
        try {
            return duration.toNanos();
        } catch (final ArithmeticException e) {
            throw new IllegalArgumentException("Duration too large to convert to nanoseconds", e);
        }
    }

    /**
     * Returns the type of this retry policy.
     *
     * @return the policy type
     */
    public Type type() {
        return type;
    }

    /**
     * Returns the initial delay in nanoseconds.
     *
     * @return the initial delay
     */
    public long initialDelayNanos() {
        return initialDelayNanos;
    }

    /**
     * Returns the maximum delay in nanoseconds.
     *
     * @return the maximum delay
     */
    public long maxDelayNanos() {
        return maxDelayNanos;
    }

    /**
     * Returns the multiplier for exponential backoff.
     *
     * @return the multiplier (1.0 for fixed delay)
     */
    public double multiplier() {
        return multiplier;
    }

    /**
     * Returns the maximum number of retry attempts.
     *
     * @return the max attempts
     */
    public int maxAttempts() {
        return maxAttempts;
    }

    /**
     * Calculates the delay in nanoseconds for a specific attempt number.
     *
     * @param attemptNumber the attempt number (1-based, first retry is 1)
     * @return the delay in nanoseconds
     */
    public long delayNanosForAttempt(final int attemptNumber) {
        if (attemptNumber < 1) {
            throw new IllegalArgumentException("attemptNumber must be at least 1");
        }

        if (type == Type.FIXED_DELAY) {
            return initialDelayNanos;
        }

        final double delay = initialDelayNanos * Math.pow(multiplier, attemptNumber - 1d);
        if (delay > maxDelayNanos || Double.isInfinite(delay)) {
            return maxDelayNanos;
        }
        return Math.min((long) delay, maxDelayNanos);
    }

    @Override
    public String toString() {
        if (type == Type.FIXED_DELAY) {
            return "RetryPolicy[FIXED_DELAY, delay=" + Duration.ofNanos(initialDelayNanos) + ", maxAttempts=" + maxAttempts + "]";
        }
        return "RetryPolicy[EXPONENTIAL_BACKOFF, initial=" + Duration.ofNanos(initialDelayNanos) +
                ", max=" + Duration.ofNanos(maxDelayNanos) +
                ", multiplier=" + multiplier +
                ", maxAttempts=" + maxAttempts + "]";
    }
}
