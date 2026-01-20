package com.github.frosxt.chronos.runtime.task;

import com.github.frosxt.chronos.api.ScheduledHandle;
import com.github.frosxt.chronos.api.TaskState;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;

import java.time.Instant;

/**
 * Implementation of {@link ScheduledHandle} that delegates to a
 * {@link TaskControl}.
 */
public final class TaskHandleImpl implements ScheduledHandle {
    private final TaskControl control;
    private final InstantMapper instantMapper;

    /**
     * Creates a new task handle.
     *
     * @param control       the underlying task control
     * @param instantMapper the instant mapper for time conversions
     */
    public TaskHandleImpl(final TaskControl control, final InstantMapper instantMapper) {
        this.control = control;
        this.instantMapper = instantMapper;
    }

    @Override
    public String id() {
        return control.id();
    }

    @Override
    public boolean cancel() {
        if (control.stateMachine().cancel()) {
            control.cancelScheduledFuture();
            return true;
        }
        if (control.stateMachine().isRunning()) {
            control.requestCancellation();
            return true;
        }
        return control.state() == TaskState.CANCELLED;
    }

    @Override
    public boolean isCancelled() {
        return control.state() == TaskState.CANCELLED;
    }

    @Override
    public boolean isDone() {
        return control.stateMachine().isTerminal();
    }

    @Override
    public boolean isRunning() {
        return control.stateMachine().isRunning();
    }

    @Override
    public Instant firstScheduledTime() {
        final long nanos = control.firstScheduledNanos();
        return nanos >= 0 ? instantMapper.toInstant(nanos) : null;
    }

    @Override
    public Instant lastStartTime() {
        final long nanos = control.lastStartNanos();
        return nanos >= 0 ? instantMapper.toInstant(nanos) : null;
    }

    @Override
    public Instant lastEndTime() {
        final long nanos = control.lastEndNanos();
        return nanos >= 0 ? instantMapper.toInstant(nanos) : null;
    }

    @Override
    public Instant nextScheduledTime() {
        if (control.stateMachine().isTerminal()) {
            return null;
        }
        final long nanos = control.nextScheduledNanos();
        return nanos >= 0 ? instantMapper.toInstant(nanos) : null;
    }

    @Override
    public long runCount() {
        return control.runCount();
    }

    @Override
    public TaskState state() {
        return control.state();
    }

    /**
     * Returns the underlying task control (internal use only).
     *
     * @return the task control
     */
    public TaskControl control() {
        return control;
    }
}
