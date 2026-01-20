package com.github.frosxt.chronos.runtime.trigger.impl;

import com.github.frosxt.chronos.runtime.trigger.Trigger;

/**
 * A trigger for fixed-delay scheduling.
 *
 * <p>
 * The next execution time is computed as the end time of the previous
 * execution plus the configured delay.
 */
public final class FixedDelayTrigger implements Trigger {
    private final long initialScheduleNanos;
    private final long delayNanos;

    /**
     * Creates a new fixed-delay trigger.
     *
     * @param currentNanos      the current monotonic time
     * @param initialDelayNanos the delay before the first execution
     * @param delayNanos        the delay between the end of one execution and the
     *                          start of the next
     */
    public FixedDelayTrigger(final long currentNanos, final long initialDelayNanos, final long delayNanos) {
        this.initialScheduleNanos = currentNanos + initialDelayNanos;
        this.delayNanos = delayNanos;
    }

    @Override
    public long nextDelayNanos(final long currentNanos, final long lastStartNanos, final long lastEndNanos, final long runCount) {
        if (runCount == 0) {
            return Math.max(0, initialScheduleNanos - currentNanos);
        }

        if (lastEndNanos < 0) {
            return 0;
        }

        final long targetNanos = lastEndNanos + delayNanos;
        return Math.max(0, targetNanos - currentNanos);
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
     * Returns the delay in nanoseconds.
     */
    public long delayNanos() {
        return delayNanos;
    }
}
