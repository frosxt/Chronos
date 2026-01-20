package com.github.frosxt.chronos.runtime.metrics.snapshot;

import com.github.frosxt.chronos.api.SchedulerSnapshot;
import com.github.frosxt.chronos.api.TaskState;
import com.github.frosxt.chronos.runtime.metrics.MetricsCollector;
import com.github.frosxt.chronos.runtime.scheduler.registry.TaskRegistry;

import java.time.Clock;

/**
 * Builds scheduler snapshots from current state.
 */
public final class SnapshotBuilder {
    private final TaskRegistry registry;
    private final MetricsCollector metrics;
    private final Clock clock;

    public SnapshotBuilder(final TaskRegistry registry, final MetricsCollector metrics, final Clock clock) {
        this.registry = registry;
        this.metrics = metrics;
        this.clock = clock;
    }

    public SchedulerSnapshot build() {
        final long[] counts = new long[6];

        registry.forEach(control -> {
            final TaskState state = control.state();
            switch (state) {
                case SCHEDULED -> counts[0]++;
                case RUNNING -> counts[1]++;
                case RETRY_WAIT -> counts[2]++;
                case COMPLETED -> counts[3]++;
                case FAILED -> counts[4]++;
                case CANCELLED -> counts[5]++;
            }
        });

        final long total = counts[0] + counts[1] + counts[2] + metrics.completedTasks() + metrics.failedTasks() + metrics.cancelledTasks();

        return new SchedulerSnapshotImpl(
                clock.instant(),
                total,
                counts[0],
                counts[1],
                counts[2],
                metrics.completedTasks(),
                metrics.failedTasks(),
                metrics.cancelledTasks(),
                metrics.totalExecutions());
    }
}
