package com.github.frosxt.chronos.runtime.scheduler;

import com.github.frosxt.chronos.api.ScheduledHandle;
import com.github.frosxt.chronos.api.Scheduler;
import com.github.frosxt.chronos.api.SchedulerSnapshot;
import com.github.frosxt.chronos.api.cron.CronExpression;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import com.github.frosxt.chronos.runtime.metrics.MetricsCollector;
import com.github.frosxt.chronos.runtime.scheduler.facade.SchedulingFacade;
import com.github.frosxt.chronos.runtime.scheduler.facade.SnapshotFacade;
import com.github.frosxt.chronos.runtime.scheduler.lifecycle.LifecycleController;
import com.github.frosxt.chronos.runtime.scheduler.registry.TaskRegistry;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Implementation of {@link Scheduler}.
 * <p>
 * This class now acts as a thin facade, delegating to specialized controllers.
 */
public final class SchedulerImpl implements Scheduler {
    private static final Duration DEFAULT_MISFIRE_GRACE = Duration.ofMinutes(1);

    private final LifecycleController lifecycle;
    private final SchedulingFacade scheduling;
    private final SnapshotFacade snapshot;

    /**
     * Creates a new scheduler.
     *
     * @param executor      the scheduled executor service
     * @param instantMapper the instant mapper
     * @param clock         the wall clock
     * @param spec          the scheduler specification
     */
    public SchedulerImpl(final ScheduledExecutorService executor, final InstantMapper instantMapper, final Clock clock, final SchedulerSpec spec) {
        final TaskRegistry registry = new TaskRegistry();
        final MetricsCollector metricsCollector = new MetricsCollector();

        this.lifecycle = new LifecycleController(executor, registry, spec.shutdownGrace());
        this.snapshot = new SnapshotFacade(registry, metricsCollector, clock);
        this.scheduling = new SchedulingFacade(
                executor,
                instantMapper,
                registry,
                metricsCollector,
                spec.listeners(),
                lifecycle,
                spec.defaultExecutionPolicy(),
                spec.defaultJitter());
    }

    @Override
    public ScheduledHandle scheduleOnce(final Duration delay, final Runnable task) {
        return scheduling.scheduleOnce(delay, task);
    }

    @Override
    public ScheduledHandle scheduleAtFixedRate(final Duration initialDelay, final Duration period, final Runnable task) {
        return scheduling.scheduleAtFixedRate(initialDelay, period, task);
    }

    @Override
    public ScheduledHandle scheduleWithFixedDelay(final Duration initialDelay, final Duration delay, final Runnable task) {
        return scheduling.scheduleWithFixedDelay(initialDelay, delay, task);
    }

    @Override
    public ScheduledHandle scheduleCron(final CronExpression cron, final ZoneId zone, final Runnable task) {
        return scheduling.scheduleCron(cron, zone, DEFAULT_MISFIRE_GRACE, task);
    }

    @Override
    public ScheduledHandle scheduleCron(final CronExpression cron, final ZoneId zone, final Duration misfireGrace, final Runnable task) {
        return scheduling.scheduleCron(cron, zone, misfireGrace, task);
    }

    @Override
    public SchedulerSnapshot snapshot() {
        return snapshot.snapshot();
    }

    @Override
    public void shutdown() {
        lifecycle.shutdown();
    }

    @Override
    public void shutdownNow() {
        lifecycle.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return lifecycle.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return lifecycle.isTerminated();
    }

    @Override
    public boolean awaitTermination(final Duration timeout) throws InterruptedException {
        return lifecycle.awaitTermination(timeout);
    }

    @Override
    public void close() {
        lifecycle.close();
    }
}
