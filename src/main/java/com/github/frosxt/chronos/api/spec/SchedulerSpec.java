package com.github.frosxt.chronos.api.spec;

import com.github.frosxt.chronos.api.listener.TaskListener;
import com.github.frosxt.chronos.api.policy.ExecutionPolicy;
import com.github.frosxt.chronos.api.policy.Jitter;
import com.github.frosxt.chronos.api.spec.builder.SchedulerSpecBuilder;
import com.github.frosxt.chronos.api.time.TimeSource;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.List;

/**
 * Immutable specification for creating a scheduler.
 *
 * <p>
 * Use {@link #builder()} to construct a specification.
 *
 * <p>
 * This class is immutable and thread-safe.
 */
public final class SchedulerSpec {
    private final int threadCount;
    private final String threadNamePrefix;
    private final ExecutionPolicy defaultExecutionPolicy;
    private final Jitter defaultJitter;
    private final ZoneId defaultZoneId;
    private final Duration shutdownGrace;
    private final TimeSource timeSource;
    private final Clock clock;
    private final List<TaskListener> listeners;

    public SchedulerSpec(final SchedulerSpecBuilder builder) {
        this.threadCount = builder.getThreadCount();
        this.threadNamePrefix = builder.getThreadNamePrefix();
        this.defaultExecutionPolicy = builder.getDefaultExecutionPolicy();
        this.defaultJitter = builder.getDefaultJitter();
        this.defaultZoneId = builder.getDefaultZoneId();
        this.shutdownGrace = builder.getShutdownGrace();
        this.timeSource = builder.getTimeSource();
        this.clock = builder.getClock();
        this.listeners = List.copyOf(builder.getListeners());
    }

    /**
     * Creates a new builder for a scheduler specification.
     *
     * @return a new builder
     */
    public static SchedulerSpecBuilder builder() {
        return new SchedulerSpecBuilder();
    }

    /**
     * Returns the number of threads in the scheduler's thread pool.
     *
     * @return the thread count
     */
    public int threadCount() {
        return threadCount;
    }

    /**
     * Returns the prefix for scheduler thread names.
     *
     * @return the thread name prefix
     */
    public String threadNamePrefix() {
        return threadNamePrefix;
    }

    /**
     * Returns the default execution policy for tasks.
     *
     * @return the default execution policy
     */
    public ExecutionPolicy defaultExecutionPolicy() {
        return defaultExecutionPolicy;
    }

    /**
     * Returns the default jitter configuration.
     *
     * @return the default jitter
     */
    public Jitter defaultJitter() {
        return defaultJitter;
    }

    /**
     * Returns the default timezone for cron expressions.
     *
     * @return the default zone ID
     */
    public ZoneId defaultZoneId() {
        return defaultZoneId;
    }

    /**
     * Returns the grace period for orderly shutdown.
     *
     * @return the shutdown grace period
     */
    public Duration shutdownGrace() {
        return shutdownGrace;
    }

    /**
     * Returns the monotonic time source for scheduling calculations.
     *
     * @return the time source, or null to use the system default
     */
    public TimeSource timeSource() {
        return timeSource;
    }

    /**
     * Returns the clock for wall-clock time operations.
     *
     * @return the clock, or null to use the system default
     */
    public Clock clock() {
        return clock;
    }

    /**
     * Returns the list of task listeners.
     *
     * @return an unmodifiable list of listeners
     */
    public List<TaskListener> listeners() {
        return listeners;
    }
}
