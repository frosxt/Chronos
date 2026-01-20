package com.github.frosxt.chronos.runtime.task.state;

import com.github.frosxt.chronos.api.TaskState;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Manages state transitions for a scheduled task.
 *
 * <p>
 * This class enforces valid state transitions and provides atomic
 * state updates.
 */
public final class TaskStateMachine {
    private final AtomicReference<TaskState> state;

    /**
     * Creates a new state machine in the SCHEDULED state.
     */
    public TaskStateMachine() {
        this.state = new AtomicReference<>(TaskState.SCHEDULED);
    }

    /**
     * Returns the current state.
     *
     * @return the current state
     */
    public TaskState get() {
        return state.get();
    }

    /**
     * Attempts to transition from SCHEDULED to RUNNING.
     *
     * @return true if the transition succeeded
     */
    public boolean startExecution() {
        return state.compareAndSet(TaskState.SCHEDULED, TaskState.RUNNING);
    }

    /**
     * Attempts to transition from RETRY_WAIT to RUNNING.
     *
     * @return true if the transition succeeded
     */
    public boolean startRetry() {
        return state.compareAndSet(TaskState.RETRY_WAIT, TaskState.RUNNING);
    }

    /**
     * Transitions from RUNNING to SCHEDULED for recurring tasks.
     *
     * @return true if the transition succeeded
     */
    public boolean completeRecurring() {
        return state.compareAndSet(TaskState.RUNNING, TaskState.SCHEDULED);
    }

    /**
     * Transitions from RUNNING to COMPLETED for one-shot tasks.
     *
     * @return true if the transition succeeded
     */
    public boolean completeOnce() {
        return state.compareAndSet(TaskState.RUNNING, TaskState.COMPLETED);
    }

    /**
     * Transitions from SCHEDULED to COMPLETED for tasks that complete without
     * running.
     * Used when a trigger indicates no execution is needed.
     *
     * @return true if the transition succeeded
     */
    public boolean completeFromScheduled() {
        return state.compareAndSet(TaskState.SCHEDULED, TaskState.COMPLETED);
    }

    /**
     * Transitions from SCHEDULED to FAILED.
     * Used when a task cannot be scheduled (e.g., cron returns no valid time).
     *
     * @return true if the transition succeeded
     */
    public boolean failFromScheduled() {
        return state.compareAndSet(TaskState.SCHEDULED, TaskState.FAILED);
    }

    /**
     * Transitions from RUNNING to RETRY_WAIT for failed tasks.
     *
     * @return true if the transition succeeded
     */
    public boolean scheduleRetry() {
        return state.compareAndSet(TaskState.RUNNING, TaskState.RETRY_WAIT);
    }

    /**
     * Transitions from RUNNING to FAILED.
     *
     * @return true if the transition succeeded
     */
    public boolean fail() {
        return state.compareAndSet(TaskState.RUNNING, TaskState.FAILED);
    }

    /**
     * Attempts to cancel the task from SCHEDULED or RETRY_WAIT state.
     *
     * @return true if the transition succeeded
     */
    public boolean cancel() {
        TaskState current;
        do {
            current = state.get();
            if (current == TaskState.CANCELLED) {
                return true;
            }
            if (current != TaskState.SCHEDULED && current != TaskState.RETRY_WAIT) {
                return false;
            }
        } while (!state.compareAndSet(current, TaskState.CANCELLED));
        return true;
    }

    /**
     * Marks the task as cancelled if it's currently running.
     * The actual cancellation takes effect after the current execution completes.
     *
     * @return true if the task was running
     */
    public boolean requestCancellation() {
        return state.get() == TaskState.RUNNING;
    }

    /**
     * Forces a transition to CANCELLED state regardless of current state.
     */
    public void forceCancel() {
        state.set(TaskState.CANCELLED);
    }

    /**
     * Returns whether the task is in a terminal state.
     *
     * @return true if completed, failed, or cancelled
     */
    public boolean isTerminal() {
        final TaskState s = state.get();
        return s == TaskState.COMPLETED || s == TaskState.FAILED || s == TaskState.CANCELLED;
    }

    /**
     * Returns whether the task is currently running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return state.get() == TaskState.RUNNING;
    }
}
