package com.github.frosxt.chronos.runtime.wiring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Factory for creating the scheduled executor service.
 */
public final class ExecutorFactory {

    private ExecutorFactory() {
        throw new UnsupportedOperationException("This class cannot be instantiated!");
    }

    /**
     * Creates a scheduled executor service.
     *
     * @param threadCount      the number of threads
     * @param threadNamePrefix the thread name prefix
     * @return the executor service
     */
    public static ScheduledExecutorService create(final int threadCount, final String threadNamePrefix) {
        final ThreadFactory threadFactory = new ChronosThreadFactory(threadNamePrefix);
        return Executors.newScheduledThreadPool(threadCount, threadFactory);
    }

    private static final class ChronosThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger counter = new AtomicInteger(0);

        ChronosThreadFactory(final String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(final Runnable r) {
            final Thread thread = new Thread(r);
            thread.setName(prefix + counter.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }
}
