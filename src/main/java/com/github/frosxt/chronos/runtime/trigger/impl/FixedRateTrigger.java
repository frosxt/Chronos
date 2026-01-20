package com.github.frosxt.chronos.runtime.trigger.impl;

import com.github.frosxt.chronos.runtime.trigger.Trigger;

/**
 * A trigger for fixed-rate scheduling.
 *
 * <p>
 * The next execution time is computed from the initial schedule time,
 * not from when the previous execution completed. If executions fall behind,
 * the scheduler will run immediately but will not run multiple catch-up
 * executions.
 */
public final class FixedRateTrigger implements Trigger {
    private final long initialScheduleNanos;
    private final long periodNanos;

    /**
     * Creates a new fixed-rate trigger.
     *
     * @param currentNanos      the current monotonic time
     * @param initialDelayNanos the delay before the first execution
     * @param periodNanos       the period between executions
     */
    public FixedRateTrigger(final long currentNanos, final long initialDelayNanos, final long periodNanos) {
        this.initialScheduleNanos = currentNanos + initialDelayNanos;
        this.periodNanos = periodNanos;
    }

    @Override
    public long nextDelayNanos(final long currentNanos, final long lastStartNanos, final long lastEndNanos, final long runCount) {
        if (runCount == 0) {
            return Math.max(0, initialScheduleNanos - currentNanos);
        }

        try {
            final long runs = Math.multiplyExact(runCount, periodNanos);
            final long targetNanos = Math.addExact(initialScheduleNanos, runs);

            if (targetNanos <= currentNanos) {
                return 0;
            }

            return targetNanos - currentNanos;
        } catch (final ArithmeticException e) {
            return -1;
        }
    }

    @Override
    public boolean isRecurring() {
        return true;
    }

    /**
     * Returns the initial schedule time in nanoseconds.
     */
    public long initialScheduleNanos() {
        return initialScheduleNanos;
    }

    /**
     * Returns the period in nanoseconds.
     */
    public long periodNanos() {
        return periodNanos;
    }
}
