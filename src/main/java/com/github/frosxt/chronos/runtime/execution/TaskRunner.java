package com.github.frosxt.chronos.runtime.execution;

import com.github.frosxt.chronos.api.listener.TaskListener;
import com.github.frosxt.chronos.runtime.execution.failure.FailureHandler;
import com.github.frosxt.chronos.runtime.execution.invoke.Invocation;
import com.github.frosxt.chronos.runtime.execution.listener.ListenerDispatcher;
import com.github.frosxt.chronos.runtime.execution.listener.TaskContextImpl;
import com.github.frosxt.chronos.runtime.execution.plan.NextRunPlanner;
import com.github.frosxt.chronos.runtime.execution.retry.RetryScheduler;
import com.github.frosxt.chronos.runtime.metrics.MetricsCollector;
import com.github.frosxt.chronos.runtime.scheduler.registry.TaskRegistry;
import com.github.frosxt.chronos.runtime.task.TaskControl;
import com.github.frosxt.chronos.runtime.time.mapper.InstantMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Executes scheduled tasks and manages their lifecycle via delegation.
 */
public final class TaskRunner implements Runnable {
    private final TaskControl control;
    private final InstantMapper instantMapper;
    private final TaskRegistry registry;
    private final MetricsCollector metricsCollector;

    private final Invocation invocation;
    private final ListenerDispatcher listeners;
    private final NextRunPlanner nextRunPlanner;
    private final RetryScheduler retryScheduler;

    public TaskRunner(final TaskControl control, final ScheduledExecutorService executor,
                      final InstantMapper instantMapper, final List<TaskListener> listeners,
                      final MetricsCollector metricsCollector, final TaskRegistry registry) {
        this.control = control;
        this.instantMapper = instantMapper;
        this.registry = registry;
        this.metricsCollector = metricsCollector;

        this.invocation = new Invocation();
        this.listeners = new ListenerDispatcher(listeners);
        this.nextRunPlanner = new NextRunPlanner(control, executor, instantMapper, registry, metricsCollector);
        this.retryScheduler = new RetryScheduler(control, executor, instantMapper);
    }

    @Override
    public void run() {
        if (!tryStartExecution()) {
            return;
        }

        final long startNanos = instantMapper.nanoTime();
        final Instant startInstant = instantMapper.now();
        control.setLastStartNanos(startNanos);

        final long runNumber = control.incrementRunCount();
        metricsCollector.recordExecution();

        TaskContextImpl context = new TaskContextImpl(control.id(),
                control.type(),
                runNumber,
                instantMapper.toInstant(control.nextScheduledNanos()),
                startInstant);

        listeners.notifyStart(context);

        final Throwable error = invocation.execute(control.task());

        final long endNanos = instantMapper.nanoTime();
        final Instant endInstant = instantMapper.now();
        control.setLastEndNanos(endNanos);

        final Duration duration = Duration.ofNanos(endNanos - startNanos);
        context = context.withEnd(endInstant, duration);

        if (error != null) {
            handleFailure(context, error);
        } else {
            handleSuccess(context);
        }
    }

    private boolean tryStartExecution() {
        if (control.stateMachine().startExecution()) {
            return true;
        }
        return control.stateMachine().startRetry();
    }

    private void handleSuccess(TaskContextImpl context) {
        control.resetRetryAttempt();

        if (control.isCancellationRequested()) {
            transitionToTerminal(true);
            return;
        }

        if (!control.trigger().isRecurring()) {
            control.stateMachine().completeOnce();
            metricsCollector.recordCompleted();
            registry.unregister(control.id());
            context = context.withNext(null);
            listeners.notifySuccess(context);
            return;
        }

        control.stateMachine().completeRecurring();
        final Instant nextInstant = nextRunPlanner.scheduleNext(this);
        context = context.withNext(nextInstant);
        listeners.notifySuccess(context);
    }

    private void handleFailure(final TaskContextImpl context, final Throwable error) {
        listeners.notifyFailure(context, error);

        if (control.isCancellationRequested()) {
            transitionToTerminal(true);
            return;
        }

        final FailureHandler.Action action = FailureHandler.handleFailure(control, error);

        switch (action) {
            case RETRY:
                control.incrementRetryAttempt();
                control.stateMachine().scheduleRetry();
                retryScheduler.scheduleRetry(this);
                break;

            case CONTINUE:
                control.resetRetryAttempt();
                control.stateMachine().completeRecurring();
                nextRunPlanner.scheduleNext(this);
                break;

            case FAIL:
                control.stateMachine().fail();
                metricsCollector.recordFailed();
                registry.unregister(control.id());
                break;
        }
    }

    private void transitionToTerminal(final boolean cancelled) {
        control.stateMachine().forceCancel();
        if (cancelled) {
            metricsCollector.recordCancelled();
        }
        registry.unregister(control.id());
    }
}
