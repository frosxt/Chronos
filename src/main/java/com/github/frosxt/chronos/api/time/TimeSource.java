package com.github.frosxt.chronos.api.time;

/**
 * A source of monotonic time for scheduling calculations.
 *
 * <p>
 * This interface allows for deterministic testing by injecting
 * a controllable time source instead of relying on system time.
 *
 * <p>
 * The time values returned are in nanoseconds and are monotonic,
 * meaning they never decrease (within the same JVM session).
 */
public interface TimeSource {

    /**
     * Returns the current monotonic time in nanoseconds.
     *
     * <p>
     * This value is suitable for measuring elapsed time but should
     * not be converted to wall-clock time directly.
     *
     * @return the current monotonic time in nanoseconds
     */
    long nanoTime();
}
