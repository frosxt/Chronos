package com.github.frosxt.chronos.runtime.trigger.cron;

import java.time.Duration;

/**
 * Encapsulates misfire handling logic.
 */
public final class MisfirePolicy {
    private final long misfireGraceNanos;

    public MisfirePolicy(final long misfireGraceNanos) {
        this.misfireGraceNanos = misfireGraceNanos;
    }

    public boolean isMisfire(final Duration elapsed) {
        return elapsed.toNanos() > misfireGraceNanos;
    }
}
