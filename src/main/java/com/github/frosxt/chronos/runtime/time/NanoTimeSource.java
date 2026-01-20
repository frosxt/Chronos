package com.github.frosxt.chronos.runtime.time;

import com.github.frosxt.chronos.api.time.TimeSource;

/**
 * A {@link TimeSource} implementation using {@link System#nanoTime()}.
 *
 * <p>
 * This is the default time source for production use.
 */
public final class NanoTimeSource implements TimeSource {
    private static final NanoTimeSource INSTANCE = new NanoTimeSource();

    private NanoTimeSource() {
    }

    /**
     * Returns the singleton instance.
     *
     * @return the nano time source instance
     */
    public static NanoTimeSource instance() {
        return INSTANCE;
    }

    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
