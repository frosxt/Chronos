package com.github.frosxt.chronos.runtime.scheduler.facade;

import com.github.frosxt.chronos.api.SchedulerSnapshot;
import com.github.frosxt.chronos.runtime.metrics.MetricsCollector;
import com.github.frosxt.chronos.runtime.metrics.snapshot.SnapshotBuilder;
import com.github.frosxt.chronos.runtime.scheduler.registry.TaskRegistry;

import java.time.Clock;

/**
 * Facade for creating scheduler snapshots.
 */
public final class SnapshotFacade {
    private final SnapshotBuilder builder;

    public SnapshotFacade(final TaskRegistry registry, final MetricsCollector metricsCollector, final Clock clock) {
        this.builder = new SnapshotBuilder(registry, metricsCollector, clock);
    }

    public SchedulerSnapshot snapshot() {
        return builder.build();
    }
}
