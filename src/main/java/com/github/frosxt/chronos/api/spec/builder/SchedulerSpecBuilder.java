package com.github.frosxt.chronos.api.spec.builder;

import com.github.frosxt.chronos.api.listener.TaskListener;
import com.github.frosxt.chronos.api.policy.ExecutionPolicy;
import com.github.frosxt.chronos.api.policy.Jitter;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import com.github.frosxt.chronos.api.time.TimeSource;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder for {@link SchedulerSpec}.
 */
public final class SchedulerSpecBuilder {
    private int threadCount = 1;
    private String threadNamePrefix = "chronos-";
    private ExecutionPolicy defaultExecutionPolicy = ExecutionPolicy.stopOnFailure();
    private Jitter defaultJitter = Jitter.none();
    private ZoneId defaultZoneId = ZoneId.systemDefault();
    private Duration shutdownGrace = Duration.ofSeconds(30);
    private TimeSource timeSource;
    private Clock clock;
    private final List<TaskListener> listeners = new ArrayList<>();

    public int getThreadCount() {
        return threadCount;
    }

    public String getThreadNamePrefix() {
        return threadNamePrefix;
    }

    public ExecutionPolicy getDefaultExecutionPolicy() {
        return defaultExecutionPolicy;
    }

    public Jitter getDefaultJitter() {
        return defaultJitter;
    }

    public ZoneId getDefaultZoneId() {
        return defaultZoneId;
    }

    public Duration getShutdownGrace() {
        return shutdownGrace;
    }

    public TimeSource getTimeSource() {
        return timeSource;
    }

    public Clock getClock() {
        return clock;
    }

    public List<TaskListener> getListeners() {
        return listeners;
    }

    /**
     * Sets the number of threads in the scheduler's thread pool.
     *
     * @param threadCount the thread count (must be at least 1)
     * @return this builder
     * @throws IllegalArgumentException if threadCount is less than 1
     */
    public SchedulerSpecBuilder threadCount(final int threadCount) {
        if (threadCount < 1) {
            throw new IllegalArgumentException("threadCount must be at least 1");
        }
        this.threadCount = threadCount;
        return this;
    }

    /**
     * Sets the prefix for scheduler thread names.
     *
     * @param threadNamePrefix the prefix
     * @return this builder
     * @throws NullPointerException if threadNamePrefix is null
     */
    public SchedulerSpecBuilder threadNamePrefix(final String threadNamePrefix) {
        this.threadNamePrefix = Objects.requireNonNull(threadNamePrefix, "threadNamePrefix must not be null");
        return this;
    }

    /**
     * Sets the default execution policy for tasks.
     *
     * @param policy the default policy
     * @return this builder
     * @throws NullPointerException if policy is null
     */
    public SchedulerSpecBuilder defaultExecutionPolicy(final ExecutionPolicy policy) {
        this.defaultExecutionPolicy = Objects.requireNonNull(policy, "policy must not be null");
        return this;
    }

    /**
     * Sets the default jitter configuration.
     *
     * @param jitter the default jitter
     * @return this builder
     * @throws NullPointerException if jitter is null
     */
    public SchedulerSpecBuilder defaultJitter(final Jitter jitter) {
        this.defaultJitter = Objects.requireNonNull(jitter, "jitter must not be null");
        return this;
    }

    /**
     * Sets the default timezone for cron expressions.
     *
     * @param zone the default zone
     * @return this builder
     * @throws NullPointerException if zone is null
     */
    public SchedulerSpecBuilder defaultZoneId(final ZoneId zone) {
        this.defaultZoneId = Objects.requireNonNull(zone, "zone must not be null");
        return this;
    }

    /**
     * Sets the grace period for orderly shutdown.
     *
     * @param grace the shutdown grace period (must not be negative)
     * @return this builder
     * @throws NullPointerException     if grace is null
     * @throws IllegalArgumentException if grace is negative
     */
    public SchedulerSpecBuilder shutdownGrace(final Duration grace) {
        Objects.requireNonNull(grace, "grace must not be null");
        if (grace.isNegative()) {
            throw new IllegalArgumentException("shutdownGrace must not be negative");
        }
        this.shutdownGrace = grace;
        return this;
    }

    /**
     * Sets the monotonic time source for scheduling calculations.
     *
     * <p>
     * If not set, the system's {@link System#nanoTime()} is used.
     *
     * @param timeSource the time source
     * @return this builder
     */
    public SchedulerSpecBuilder timeSource(final TimeSource timeSource) {
        this.timeSource = timeSource;
        return this;
    }

    /**
     * Sets the clock for wall-clock time operations.
     *
     * <p>
     * If not set, {@link Clock#systemDefaultZone()} is used.
     *
     * @param clock the clock
     * @return this builder
     */
    public SchedulerSpecBuilder clock(final Clock clock) {
        this.clock = clock;
        return this;
    }

    /**
     * Adds a task listener.
     *
     * @param listener the listener to add
     * @return this builder
     * @throws NullPointerException if listener is null
     */
    public SchedulerSpecBuilder addListener(final TaskListener listener) {
        this.listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
        return this;
    }

    /**
     * Builds the scheduler specification.
     *
     * @return the scheduler specification
     */
    public SchedulerSpec build() {
        return new SchedulerSpec(this);
    }
}
