package com.github.frosxt.chronos.api.policy;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Applies randomized jitter to retry delays.
 *
 * <p>
 * Jitter helps prevent thundering herd problems when multiple tasks
 * retry simultaneously.
 *
 * <p>
 * This class is immutable and thread-safe.
 */
public final class Jitter {
    private static final Jitter NONE = new Jitter(0.0);
    private final double factor;

    private Jitter(final double factor) {
        this.factor = factor;
    }

    /**
     * Returns a jitter that applies no randomization.
     *
     * @return the no-jitter instance
     */
    public static Jitter none() {
        return NONE;
    }

    /**
     * Returns a jitter that applies uniform randomization.
     *
     * <p>
     * The delay is adjusted by a random value in the range
     * {@code [-factor*delay, +factor*delay]}.
     *
     * @param factor the jitter factor in the range [0.0, 1.0]
     * @return the jitter instance
     * @throws IllegalArgumentException if factor is not in [0.0, 1.0]
     */
    public static Jitter uniform(final double factor) {
        if (factor < 0.0 || factor > 1.0) {
            throw new IllegalArgumentException("factor must be in range [0.0, 1.0]");
        }
        if (factor == 0.0) {
            return NONE;
        }
        return new Jitter(factor);
    }

    /**
     * Returns the jitter factor.
     *
     * @return the factor in range [0.0, 1.0]
     */
    public double factor() {
        return factor;
    }

    /**
     * Applies jitter to a delay value.
     *
     * @param delayNanos the original delay in nanoseconds
     * @return the jittered delay in nanoseconds (always positive)
     */
    public long apply(final long delayNanos) {
        if (factor == 0.0 || delayNanos <= 0) {
            return delayNanos;
        }

        final double jitterRange = factor * delayNanos;
        final double jitter = ThreadLocalRandom.current().nextDouble(-jitterRange, jitterRange);
        final long jitteredDelay = (long) (delayNanos + jitter);

        return Math.max(1, jitteredDelay);
    }

    @Override
    public String toString() {
        if (factor == 0.0) {
            return "Jitter[NONE]";
        }
        return "Jitter[uniform=" + factor + "]";
    }
}
