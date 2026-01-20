package com.github.frosxt.chronos.runtime.execution.retry;

import com.github.frosxt.chronos.runtime.execution.plan.RetryPlanner;
import com.github.frosxt.chronos.runtime.task.TaskControl;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Schedules retries for failed tasks.
 */
public final class RetryScheduler {
    private final TaskControl control;
    private final ScheduledExecutorService executor;
    private final InstantMapper instantMapper;

    public RetryScheduler(final TaskControl control, final ScheduledExecutorService executor, final InstantMapper instantMapper) {
        this.control = control;
        this.executor = executor;
        this.instantMapper = instantMapper;
    }

    public void scheduleRetry(final Runnable runner) {
        final long delayNanos = RetryPlanner.computeRetryDelay(control);
        final long nextNanos = instantMapper.nanoTime() + delayNanos;
        control.setNextScheduledNanos(nextNanos);

        final ScheduledFuture<?> future = executor.schedule(runner, delayNanos, TimeUnit.NANOSECONDS);
        control.setScheduledFuture(future);
    }
}
