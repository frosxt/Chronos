package com.github.frosxt.chronos.runtime.task;

import com.github.frosxt.chronos.api.TaskState;
import com.github.frosxt.chronos.api.TaskType;
import com.github.frosxt.chronos.api.policy.ExecutionPolicy;
import com.github.frosxt.chronos.api.policy.Jitter;
import com.github.frosxt.chronos.runtime.task.cancel.TaskCancellation;
import com.github.frosxt.chronos.runtime.task.counter.TaskCounters;
import com.github.frosxt.chronos.runtime.task.future.TaskFutureSlot;
import com.github.frosxt.chronos.runtime.task.state.TaskStateMachine;
import com.github.frosxt.chronos.runtime.task.time.TaskTiming;
import com.github.frosxt.chronos.runtime.trigger.Trigger;

import java.util.concurrent.ScheduledFuture;

/**
 * Internal control structure for a scheduled task.
 * <p>
 * Aggregates state components for timing, execution, cancellation, and
 * lifecycle.
 */
public final class TaskControl {
    private final String id;
    private final TaskType type;
    private final Runnable task;
    private final Trigger trigger;
    private final ExecutionPolicy executionPolicy;
    private final Jitter jitter;
    private final TaskStateMachine stateMachine;

    private final TaskTiming timing;
    private final TaskCounters counters;
    private final TaskFutureSlot futureSlot;
    private final TaskCancellation cancellation;

    /**
     * Creates a new task control.
     *
     * @param id              the unique task identifier
     * @param type            the task type
     * @param task            the task to execute
     * @param trigger         the trigger controlling execution times
     * @param executionPolicy the failure handling policy
     * @param jitter          the jitter configuration
     */
    public TaskControl(final String id, final TaskType type, final Runnable task, final Trigger trigger,
                       final ExecutionPolicy executionPolicy, final Jitter jitter) {
        this.id = id;
        this.type = type;
        this.task = task;
        this.trigger = trigger;
        this.executionPolicy = executionPolicy;
        this.jitter = jitter;
        this.stateMachine = new TaskStateMachine();

        this.timing = new TaskTiming();
        this.counters = new TaskCounters();
        this.futureSlot = new TaskFutureSlot();
        this.cancellation = new TaskCancellation();
    }

    public String id() {
        return id;
    }

    public TaskType type() {
        return type;
    }

    public Runnable task() {
        return task;
    }

    public Trigger trigger() {
        return trigger;
    }

    public ExecutionPolicy executionPolicy() {
        return executionPolicy;
    }

    public Jitter jitter() {
        return jitter;
    }

    public TaskStateMachine stateMachine() {
        return stateMachine;
    }

    public TaskState state() {
        return stateMachine.get();
    }

    public long runCount() {
        return counters.runCount();
    }

    public long incrementRunCount() {
        return counters.incrementRunCount();
    }

    public int retryAttempt() {
        return counters.retryAttempt();
    }

    public void incrementRetryAttempt() {
        counters.incrementRetryAttempt();
    }

    public void resetRetryAttempt() {
        counters.resetRetryAttempt();
    }

    public ScheduledFuture<?> scheduledFuture() {
        return futureSlot.get();
    }

    public void setScheduledFuture(final ScheduledFuture<?> future) {
        futureSlot.set(future);
    }

    public boolean cancelScheduledFuture() {
        return futureSlot.cancel(false);
    }

    public long firstScheduledNanos() {
        return timing.firstScheduledNanos();
    }

    public void setFirstScheduledNanos(final long nanos) {
        timing.setFirstScheduledNanos(nanos);
    }

    public long lastStartNanos() {
        return timing.lastStartNanos();
    }

    public void setLastStartNanos(final long nanos) {
        timing.setLastStartNanos(nanos);
    }

    public long lastEndNanos() {
        return timing.lastEndNanos();
    }

    public void setLastEndNanos(final long nanos) {
        timing.setLastEndNanos(nanos);
    }

    public long nextScheduledNanos() {
        return timing.nextScheduledNanos();
    }

    public void setNextScheduledNanos(final long nanos) {
        timing.setNextScheduledNanos(nanos);
    }

    public boolean isCancellationRequested() {
        return cancellation.isRequested();
    }

    public void requestCancellation() {
        cancellation.request();
    }
}
