package com.github.frosxt.chronos.runtime.task.counter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages execution counters for a task.
 */
public final class TaskCounters {
    private final AtomicLong runCount = new AtomicLong(0);
    private final AtomicInteger retryAttempt = new AtomicInteger(0);

    public long runCount() {
        return runCount.get();
    }

    public long incrementRunCount() {
        return runCount.incrementAndGet();
    }

    public int retryAttempt() {
        return retryAttempt.get();
    }

    public void incrementRetryAttempt() {
        retryAttempt.incrementAndGet();
    }

    public void resetRetryAttempt() {
        retryAttempt.set(0);
    }
}
