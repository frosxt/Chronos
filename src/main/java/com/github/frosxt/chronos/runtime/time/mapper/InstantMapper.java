package com.github.frosxt.chronos.runtime.time.mapper;

import com.github.frosxt.chronos.api.time.TimeSource;

import java.time.Clock;
import java.time.Instant;

/**
 * Maps monotonic nanosecond times to wall-clock {@link Instant} values.
 *
 * <p>
 * This class maintains the relationship between a monotonic time base
 * and wall-clock time, allowing conversion between the two.
 *
 * <p>
 * The mapping is computed at initialization and remains stable,
 * so returned Instants are best-effort approximations.
 */
public final class InstantMapper {
    private final TimeSource timeSource;
    private final Clock clock;
    private final long baseNanos;
    private final Instant baseInstant;

    /**
     * Creates a new instant mapper.
     *
     * @param timeSource the monotonic time source
     * @param clock      the wall-clock source
     */
    public InstantMapper(final TimeSource timeSource, final Clock clock) {
        this.timeSource = timeSource;
        this.clock = clock;
        this.baseNanos = timeSource.nanoTime();
        this.baseInstant = clock.instant();
    }

    /**
     * Converts a monotonic nanosecond time to a wall-clock instant.
     *
     * @param nanoTime the monotonic time in nanoseconds
     * @return the corresponding wall-clock instant (best-effort)
     */
    public Instant toInstant(final long nanoTime) {
        final long deltaNanos = nanoTime - baseNanos;
        return baseInstant.plusNanos(deltaNanos);
    }

    /**
     * Returns the current wall-clock instant.
     *
     * @return the current instant
     */
    public Instant now() {
        return clock.instant();
    }

    /**
     * Returns the current monotonic time in nanoseconds.
     *
     * @return the current nano time
     */
    public long nanoTime() {
        return timeSource.nanoTime();
    }

    /**
     * Converts a duration from now (in nanoseconds) to a wall-clock instant.
     *
     * @param delayNanos the delay from now in nanoseconds
     * @return the projected instant
     */
    public Instant instantAfter(final long delayNanos) {
        return toInstant(timeSource.nanoTime() + delayNanos);
    }
}
