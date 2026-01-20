package com.github.frosxt.chronos.runtime.task.future;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages the current scheduled future for a task.
 */
public final class TaskFutureSlot {
    private final AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();

    public ScheduledFuture<?> get() {
        return futureRef.get();
    }

    public void set(final ScheduledFuture<?> future) {
        futureRef.set(future);
    }

    public boolean cancel(final boolean mayInterruptIfRunning) {
        final ScheduledFuture<?> future = futureRef.getAndSet(null);
        if (future != null) {
            return future.cancel(mayInterruptIfRunning);
        }
        return false;
    }
}
