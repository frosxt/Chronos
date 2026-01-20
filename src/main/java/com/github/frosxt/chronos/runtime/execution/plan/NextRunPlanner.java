package com.github.frosxt.chronos.runtime.execution.plan;

import com.github.frosxt.chronos.runtime.metrics.MetricsCollector;
import com.github.frosxt.chronos.runtime.scheduler.registry.TaskRegistry;
import com.github.frosxt.chronos.runtime.task.TaskControl;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;

import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Plans and schedules the next execution of a recurring task.
 */
public final class NextRunPlanner {
    private final TaskControl control;
    private final ScheduledExecutorService executor;
    private final InstantMapper instantMapper;
    private final TaskRegistry registry;
    private final MetricsCollector metricsCollector;

    public NextRunPlanner(final TaskControl control, final ScheduledExecutorService executor,
                          final InstantMapper instantMapper, final TaskRegistry registry,
                          final MetricsCollector metricsCollector) {
        this.control = control;
        this.executor = executor;
        this.instantMapper = instantMapper;
        this.registry = registry;
        this.metricsCollector = metricsCollector;
    }

    public Instant scheduleNext(final Runnable runner) {
        final long currentNanos = instantMapper.nanoTime();
        final long delay = control.trigger().nextDelayNanos(
                currentNanos,
                control.lastStartNanos(),
                control.lastEndNanos(),
                control.runCount());

        if (delay < 0) {
            control.stateMachine().completeFromScheduled();
            metricsCollector.recordCompleted();
            registry.unregister(control.id());
            return null;
        }

        final long nextNanos = currentNanos + delay;
        control.setNextScheduledNanos(nextNanos);

        final ScheduledFuture<?> future = executor.schedule(runner, delay, TimeUnit.NANOSECONDS);
        control.setScheduledFuture(future);

        return instantMapper.toInstant(nextNanos);
    }
}
