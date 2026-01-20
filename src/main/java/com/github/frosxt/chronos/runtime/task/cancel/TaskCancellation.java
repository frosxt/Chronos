package com.github.frosxt.chronos.runtime.task.cancel;

/**
 * Manages cancellation state for a task.
 */
public final class TaskCancellation {
    private volatile boolean cancellationRequested = false;

    public boolean isRequested() {
        return cancellationRequested;
    }

    public void request() {
        this.cancellationRequested = true;
    }
}
