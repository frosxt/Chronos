package com.github.frosxt.chronos.api.factory;

import com.github.frosxt.chronos.api.Scheduler;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import com.github.frosxt.chronos.runtime.wiring.SchedulerFactory;

/**
 * Factory for creating {@link Scheduler} instances.
 *
 * <p>
 * This is the main entry point for creating schedulers.
 */
public final class Chronos {

    private Chronos() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a new scheduler with the specified configuration.
     *
     * @param spec the scheduler specification
     * @return a new scheduler instance
     * @throws NullPointerException if spec is null
     */
    public static Scheduler create(final SchedulerSpec spec) {
        if (spec == null) {
            throw new NullPointerException("spec must not be null");
        }
        return SchedulerFactory.create(spec);
    }
}
