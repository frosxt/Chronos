package com.github.frosxt.chronos.runtime.scheduler.lifecycle;

import com.github.frosxt.chronos.runtime.scheduler.registry.TaskRegistry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages the lifecycle state of the scheduler (running, shutting down,
 * terminated).
 */
public final class LifecycleController {
    private final ScheduledExecutorService executor;
    private final TaskRegistry registry;
    private final Duration shutdownGrace;
    private final AtomicBoolean shutdown;

    public LifecycleController(final ScheduledExecutorService executor, final TaskRegistry registry, final Duration shutdownGrace) {
        this.executor = executor;
        this.registry = registry;
        this.shutdownGrace = shutdownGrace;
        this.shutdown = new AtomicBoolean(false);
    }

    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            executor.shutdown();
        }
    }

    public void shutdownNow() {
        if (shutdown.compareAndSet(false, true)) {
            registry.cancelAll();
            registry.clear();
            executor.shutdownNow();
        }
    }

    public boolean isShutdown() {
        return shutdown.get();
    }

    public boolean isTerminated() {
        return executor.isTerminated();
    }

    public boolean awaitTermination(final Duration timeout) throws InterruptedException {
        Objects.requireNonNull(timeout, "timeout must not be null");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        return executor.awaitTermination(toNanosSafe(timeout), TimeUnit.NANOSECONDS);
    }

    public void close() {
        shutdown();
        try {
            if (!awaitTermination(shutdownGrace)) {
                shutdownNow();
            }
        } catch (final InterruptedException e) {
            shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void checkNotShutdown() {
        if (shutdown.get()) {
            throw new IllegalStateException("Scheduler has been shut down");
        }
    }

    private static long toNanosSafe(final Duration duration) {
        try {
            return duration.toNanos();
        } catch (final ArithmeticException e) {
            throw new IllegalArgumentException("Duration too large to convert to nanoseconds", e);
        }
    }
}
