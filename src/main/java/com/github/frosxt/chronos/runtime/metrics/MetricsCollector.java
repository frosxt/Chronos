package com.github.frosxt.chronos.runtime.metrics;

import java.util.concurrent.atomic.LongAdder;

/**
 * Collects metrics about scheduler operations.
 *
 * <p>
 * This class is thread-safe and uses low-contention counters.
 */
public final class MetricsCollector {
    private final LongAdder totalExecutions = new LongAdder();
    private final LongAdder completedTasks = new LongAdder();
    private final LongAdder failedTasks = new LongAdder();
    private final LongAdder cancelledTasks = new LongAdder();

    /**
     * Records a task execution.
     */
    public void recordExecution() {
        totalExecutions.increment();
    }

    /**
     * Records a task completion.
     */
    public void recordCompleted() {
        completedTasks.increment();
    }

    /**
     * Records a task failure.
     */
    public void recordFailed() {
        failedTasks.increment();
    }

    /**
     * Records a task cancellation.
     */
    public void recordCancelled() {
        cancelledTasks.increment();
    }

    /**
     * Returns the total number of executions.
     */
    public long totalExecutions() {
        return totalExecutions.sum();
    }

    /**
     * Returns the number of completed tasks.
     */
    public long completedTasks() {
        return completedTasks.sum();
    }

    /**
     * Returns the number of failed tasks.
     */
    public long failedTasks() {
        return failedTasks.sum();
    }

    /**
     * Returns the number of cancelled tasks.
     */
    public long cancelledTasks() {
        return cancelledTasks.sum();
    }
}
