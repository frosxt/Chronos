package com.github.frosxt.chronos.runtime.scheduler.facade;

import com.github.frosxt.chronos.api.ScheduledHandle;
import com.github.frosxt.chronos.api.TaskType;
import com.github.frosxt.chronos.api.cron.CronExpression;
import com.github.frosxt.chronos.api.listener.TaskListener;
import com.github.frosxt.chronos.api.policy.ExecutionPolicy;
import com.github.frosxt.chronos.api.policy.Jitter;
import com.github.frosxt.chronos.runtime.execution.TaskRunner;
import com.github.frosxt.chronos.runtime.metrics.MetricsCollector;
import com.github.frosxt.chronos.runtime.scheduler.lifecycle.LifecycleController;
import com.github.frosxt.chronos.runtime.scheduler.registry.TaskRegistry;
import com.github.frosxt.chronos.runtime.task.TaskControl;
import com.github.frosxt.chronos.runtime.task.TaskHandleImpl;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;
import com.github.frosxt.chronos.runtime.trigger.Trigger;
import com.github.frosxt.chronos.runtime.trigger.impl.CronTrigger;
import com.github.frosxt.chronos.runtime.trigger.impl.FixedDelayTrigger;
import com.github.frosxt.chronos.runtime.trigger.impl.FixedRateTrigger;
import com.github.frosxt.chronos.runtime.trigger.impl.OnceTrigger;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Facade for scheduling operations.
 */
public final class SchedulingFacade {
    private final ScheduledExecutorService executor;
    private final InstantMapper instantMapper;
    private final TaskRegistry registry;
    private final MetricsCollector metricsCollector;
    private final List<TaskListener> listeners;
    private final LifecycleController lifecycle;
    private final ExecutionPolicy defaultExecutionPolicy;
    private final Jitter defaultJitter;

    public SchedulingFacade(final ScheduledExecutorService executor,
                            final InstantMapper instantMapper,
                            final TaskRegistry registry,
                            final MetricsCollector metricsCollector,
                            final List<TaskListener> listeners,
                            final LifecycleController lifecycle,
                            final ExecutionPolicy defaultExecutionPolicy,
                            final Jitter defaultJitter) {
        this.executor = executor;
        this.instantMapper = instantMapper;
        this.registry = registry;
        this.metricsCollector = metricsCollector;
        this.listeners = listeners;
        this.lifecycle = lifecycle;
        this.defaultExecutionPolicy = defaultExecutionPolicy;
        this.defaultJitter = defaultJitter;
    }

    public ScheduledHandle scheduleOnce(final Duration delay, final Runnable task) {
        Objects.requireNonNull(delay, "delay must not be null");
        Objects.requireNonNull(task, "task must not be null");
        if (delay.isNegative() || delay.isZero()) {
            throw new IllegalArgumentException("delay must be positive");
        }
        lifecycle.checkNotShutdown();

        final long delayNanos = toNanosSafe(delay);
        final long currentNanos = instantMapper.nanoTime();

        final OnceTrigger trigger = new OnceTrigger(currentNanos, delayNanos);
        return scheduleTask(TaskType.ONCE, task, trigger);
    }

    public ScheduledHandle scheduleAtFixedRate(final Duration initialDelay, final Duration period, final Runnable task) {
        Objects.requireNonNull(initialDelay, "initialDelay must not be null");
        Objects.requireNonNull(period, "period must not be null");
        Objects.requireNonNull(task, "task must not be null");
        if (initialDelay.isNegative()) {
            throw new IllegalArgumentException("initialDelay must not be negative");
        }
        if (period.isNegative() || period.isZero()) {
            throw new IllegalArgumentException("period must be positive");
        }
        lifecycle.checkNotShutdown();

        final long initialDelayNanos = toNanosSafe(initialDelay);
        final long periodNanos = toNanosSafe(period);
        final long currentNanos = instantMapper.nanoTime();

        final FixedRateTrigger trigger = new FixedRateTrigger(currentNanos, initialDelayNanos, periodNanos);
        return scheduleTask(TaskType.FIXED_RATE, task, trigger);
    }

    public ScheduledHandle scheduleWithFixedDelay(final Duration initialDelay, final Duration delay, final Runnable task) {
        Objects.requireNonNull(initialDelay, "initialDelay must not be null");
        Objects.requireNonNull(delay, "delay must not be null");
        Objects.requireNonNull(task, "task must not be null");
        if (initialDelay.isNegative()) {
            throw new IllegalArgumentException("initialDelay must not be negative");
        }
        if (delay.isNegative() || delay.isZero()) {
            throw new IllegalArgumentException("delay must be positive");
        }
        lifecycle.checkNotShutdown();

        final long initialDelayNanos = toNanosSafe(initialDelay);
        final long delayNanos = toNanosSafe(delay);
        final long currentNanos = instantMapper.nanoTime();

        final FixedDelayTrigger trigger = new FixedDelayTrigger(currentNanos, initialDelayNanos, delayNanos);
        return scheduleTask(TaskType.FIXED_DELAY, task, trigger);
    }

    public ScheduledHandle scheduleCron(final CronExpression cron, final ZoneId zone, final Duration misfireGrace, final Runnable task) {
        Objects.requireNonNull(cron, "cron must not be null");
        Objects.requireNonNull(zone, "zone must not be null");
        Objects.requireNonNull(misfireGrace, "misfireGrace must not be null");
        if (misfireGrace.isNegative()) {
            throw new IllegalArgumentException("misfireGrace must not be negative");
        }
        Objects.requireNonNull(task, "task must not be null");
        lifecycle.checkNotShutdown();

        final long misfireGraceNanos = toNanosSafe(misfireGrace);

        final CronTrigger trigger = new CronTrigger(cron, zone, misfireGraceNanos, instantMapper);
        return scheduleTask(TaskType.CRON, task, trigger);
    }

    private ScheduledHandle scheduleTask(final TaskType type, final Runnable task, final Trigger trigger) {
        final String id = UUID.randomUUID().toString();

        final TaskControl control = new TaskControl(id, type, task, trigger, defaultExecutionPolicy, defaultJitter);

        registry.register(control);

        final long currentNanos = instantMapper.nanoTime();
        final long delay = trigger.nextDelayNanos(currentNanos, -1, -1, 0);

        if (delay < 0) {
            control.stateMachine().completeFromScheduled();
            metricsCollector.recordCompleted();
            registry.unregister(control.id());
        } else {
            final long scheduledNanos = currentNanos + delay;
            control.setFirstScheduledNanos(scheduledNanos);
            control.setNextScheduledNanos(scheduledNanos);

            final TaskRunner runner = new TaskRunner(control, executor, instantMapper, listeners, metricsCollector, registry);
            final ScheduledFuture<?> future = executor.schedule(runner, delay, TimeUnit.NANOSECONDS);
            control.setScheduledFuture(future);
        }

        return new TaskHandleImpl(control, instantMapper);
    }

    private static long toNanosSafe(final Duration duration) {
        try {
            return duration.toNanos();
        } catch (final ArithmeticException e) {
            throw new IllegalArgumentException("Duration too large to convert to nanoseconds", e);
        }
    }
}
