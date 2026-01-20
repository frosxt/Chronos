package com.github.frosxt.chronos.runtime.task.time;

/**
 * Manages timing information for a task.
 */
public final class TaskTiming {
    private volatile long firstScheduledNanos = -1;
    private volatile long lastStartNanos = -1;
    private volatile long lastEndNanos = -1;
    private volatile long nextScheduledNanos = -1;

    public long firstScheduledNanos() {
        return firstScheduledNanos;
    }

    public void setFirstScheduledNanos(final long nanos) {
        if (this.firstScheduledNanos < 0) {
            this.firstScheduledNanos = nanos;
        }
    }

    public long lastStartNanos() {
        return lastStartNanos;
    }

    public void setLastStartNanos(final long nanos) {
        this.lastStartNanos = nanos;
    }

    public long lastEndNanos() {
        return lastEndNanos;
    }

    public void setLastEndNanos(final long nanos) {
        this.lastEndNanos = nanos;
    }

    public long nextScheduledNanos() {
        return nextScheduledNanos;
    }

    public void setNextScheduledNanos(final long nanos) {
        this.nextScheduledNanos = nanos;
    }
}
