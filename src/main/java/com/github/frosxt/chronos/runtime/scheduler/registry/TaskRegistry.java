package com.github.frosxt.chronos.runtime.scheduler.registry;

import com.github.frosxt.chronos.runtime.task.TaskControl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Registry for tracking all scheduled tasks.
 *
 * <p>
 * This class is thread-safe.
 */
public final class TaskRegistry {
    private final ConcurrentHashMap<String, TaskControl> tasks = new ConcurrentHashMap<>();

    /**
     * Registers a task.
     *
     * @param control the task control to register
     */
    public void register(final TaskControl control) {
        tasks.put(control.id(), control);
    }

    /**
     * Unregisters a task.
     *
     * @param id the task ID
     * @return the removed task control, or null if not found
     */
    public TaskControl unregister(final String id) {
        return tasks.remove(id);
    }

    /**
     * Gets a task by ID.
     *
     * @param id the task ID
     * @return the task control, or null if not found
     */
    public TaskControl get(final String id) {
        return tasks.get(id);
    }

    /**
     * Returns the number of registered tasks.
     *
     * @return the task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Iterates over all registered tasks.
     *
     * @param action the action to perform on each task
     */
    public void forEach(final Consumer<TaskControl> action) {
        tasks.values().forEach(action);
    }

    /**
     * Cancels all registered tasks.
     */
    public void cancelAll() {
        tasks.values().forEach(control -> {
            control.stateMachine().forceCancel();
            control.cancelScheduledFuture();
        });
    }

    /**
     * Clears all registered tasks.
     */
    public void clear() {
        tasks.clear();
    }
}
