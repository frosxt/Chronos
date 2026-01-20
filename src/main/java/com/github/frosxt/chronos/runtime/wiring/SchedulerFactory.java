package com.github.frosxt.chronos.runtime.wiring;

import com.github.frosxt.chronos.api.Scheduler;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import com.github.frosxt.chronos.api.time.TimeSource;
import com.github.frosxt.chronos.runtime.scheduler.SchedulerImpl;
import com.github.frosxt.chronos.runtime.time.NanoTimeSource;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;

import java.time.Clock;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Factory for creating {@link Scheduler} instances from a
 * {@link SchedulerSpec}.
 */
public final class SchedulerFactory {

    private SchedulerFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a new scheduler from the given specification.
     *
     * @param spec the scheduler specification
     * @return the scheduler
     */
    public static Scheduler create(final SchedulerSpec spec) {
        TimeSource timeSource = spec.timeSource();
        if (timeSource == null) {
            timeSource = NanoTimeSource.instance();
        }

        Clock clock = spec.clock();
        if (clock == null) {
            clock = Clock.systemDefaultZone();
        }

        final InstantMapper instantMapper = new InstantMapper(timeSource, clock);

        final ScheduledExecutorService executor = ExecutorFactory.create(
                spec.threadCount(),
                spec.threadNamePrefix());

        return new SchedulerImpl(executor, instantMapper, clock, spec);
    }
}
