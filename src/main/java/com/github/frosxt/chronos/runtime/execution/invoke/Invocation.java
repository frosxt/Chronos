package com.github.frosxt.chronos.runtime.execution.invoke;

/**
 * Handles the actual execution of the user task.
 */
public final class Invocation {

    public Throwable execute(final Runnable task) {
        try {
            task.run();
            return null;
        } catch (final Throwable t) {
            return t;
        }
    }
}
