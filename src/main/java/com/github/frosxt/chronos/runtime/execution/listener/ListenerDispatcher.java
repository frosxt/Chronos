package com.github.frosxt.chronos.runtime.execution.listener;

import com.github.frosxt.chronos.api.listener.TaskContext;
import com.github.frosxt.chronos.api.listener.TaskListener;

import java.util.List;

/**
 * Dispatches events to task listeners.
 */
public final class ListenerDispatcher {
    private final List<TaskListener> listeners;

    public ListenerDispatcher(final List<TaskListener> listeners) {
        this.listeners = listeners;
    }

    public void notifyStart(final TaskContext context) {
        for (final TaskListener listener : listeners) {
            try {
                listener.onStart(context);
            } catch (final Throwable t) {
            }
        }
    }

    public void notifySuccess(final TaskContext context) {
        for (final TaskListener listener : listeners) {
            try {
                listener.onSuccess(context);
            } catch (final Throwable t) {
            }
        }
    }

    public void notifyFailure(final TaskContext context, final Throwable error) {
        for (final TaskListener listener : listeners) {
            try {
                listener.onFailure(context, error);
            } catch (final Throwable t) {
            }
        }
    }
}
