package com.github.frosxt.chronos.runtime.trigger.impl;

import com.github.frosxt.chronos.runtime.trigger.Trigger;

/**
 * A trigger for one-shot tasks that execute once after an initial delay.
 */
public final class OnceTrigger implements Trigger {
    private final long initialDelayNanos;
    private final long scheduledNanos;

    /**
     * Creates a new one-shot trigger.
     *
     * @param currentNanos      the current monotonic time
     * @param initialDelayNanos the delay until execution
     */
    public OnceTrigger(final long currentNanos, final long initialDelayNanos) {
        this.initialDelayNanos = initialDelayNanos;
        this.scheduledNanos = currentNanos + initialDelayNanos;
    }

    @Override
    public long nextDelayNanos(final long currentNanos, final long lastStartNanos, final long lastEndNanos, final long runCount) {
        if (runCount > 0) {
            return -1;
        }
        final long delay = scheduledNanos - currentNanos;
        return Math.max(0, delay);
    }

    @Override
    public boolean isRecurring() {
        return false;
    }

    /**
     * Returns the scheduled execution time in nanoseconds.
     */
    public long scheduledNanos() {
        return scheduledNanos;
    }
}
