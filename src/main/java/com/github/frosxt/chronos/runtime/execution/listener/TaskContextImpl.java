package com.github.frosxt.chronos.runtime.execution.listener;

import com.github.frosxt.chronos.api.TaskType;
import com.github.frosxt.chronos.api.listener.TaskContext;

import java.time.Duration;
import java.time.Instant;

public final class TaskContextImpl implements TaskContext {
    private final String taskId;
    private final TaskType type;
    private final long runNumber;
    private final Instant scheduledTime;
    private final Instant startTime;
    private final Instant endTime;
    private final Duration duration;
    private final Instant nextScheduledTime;

    public TaskContextImpl(final String taskId, final TaskType type, final long runNumber, final Instant scheduledTime, final Instant startTime) {
        this(taskId, type, runNumber, scheduledTime, startTime, null, null, null);
    }

    public TaskContextImpl(final String taskId, final TaskType type, final long runNumber, final Instant scheduledTime, final Instant startTime,
                           final Instant endTime, final Duration duration, final Instant nextScheduledTime) {
        this.taskId = taskId;
        this.type = type;
        this.runNumber = runNumber;
        this.scheduledTime = scheduledTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.nextScheduledTime = nextScheduledTime;
    }

    public TaskContextImpl withEnd(final Instant endTime, final Duration duration) {
        return new TaskContextImpl(taskId, type, runNumber, scheduledTime, startTime, endTime, duration, nextScheduledTime);
    }

    public TaskContextImpl withNext(final Instant nextScheduledTime) {
        return new TaskContextImpl(taskId, type, runNumber, scheduledTime, startTime, endTime, duration, nextScheduledTime);
    }

    @Override
    public String taskId() {
        return taskId;
    }

    @Override
    public TaskType type() {
        return type;
    }

    @Override
    public long runNumber() {
        return runNumber;
    }

    @Override
    public Instant scheduledTime() {
        return scheduledTime;
    }

    @Override
    public Instant startTime() {
        return startTime;
    }

    @Override
    public Instant endTime() {
        return endTime;
    }

    @Override
    public Duration duration() {
        return duration;
    }

    @Override
    public Instant nextScheduledTime() {
        return nextScheduledTime;
    }
}
