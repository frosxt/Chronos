package com.github.frosxt.chronos.runtime.core;

import com.github.frosxt.chronos.api.ScheduledHandle;
import com.github.frosxt.chronos.api.Scheduler;
import com.github.frosxt.chronos.api.factory.Chronos;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency tests for the scheduler.
 */
class SchedulerConcurrencyTest {

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = Chronos.create(SchedulerSpec.builder()
                .threadCount(4)
                .threadNamePrefix("concurrency-test-")
                .shutdownGrace(Duration.ofSeconds(5))
                .build());
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    @Test
    void fixedRateDoesNotOverlap() throws InterruptedException {
        AtomicInteger concurrentRuns = new AtomicInteger(0);
        AtomicInteger maxConcurrent = new AtomicInteger(0);
        AtomicInteger totalRuns = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(3);

        scheduler.scheduleAtFixedRate(Duration.ofMillis(10), Duration.ofMillis(50), () -> {
            int current = concurrentRuns.incrementAndGet();
            maxConcurrent.updateAndGet(v -> Math.max(v, current));

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            concurrentRuns.decrementAndGet();
            if (totalRuns.incrementAndGet() <= 3) {
                latch.countDown();
            }
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertEquals(1, maxConcurrent.get(),
                "Same task should never run concurrently with itself");
    }

    @Test
    void cancelDuringExecutionSetsFlag() throws InterruptedException {
        CountDownLatch startedLatch = new CountDownLatch(1);
        CountDownLatch continueLatch = new CountDownLatch(1);
        AtomicReference<ScheduledHandle> handleRef = new AtomicReference<>();

        ScheduledHandle handle = scheduler.scheduleOnce(Duration.ofMillis(10), () -> {
            startedLatch.countDown();
            try {
                continueLatch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        handleRef.set(handle);

        assertTrue(startedLatch.await(1, TimeUnit.SECONDS));

        assertTrue(handle.isRunning());
        handle.cancel();

        continueLatch.countDown();
        Thread.sleep(100);

        assertTrue(handle.isDone());
    }

    @Test
    void multipleTasksRunInParallel() throws InterruptedException {
        CountDownLatch allStarted = new CountDownLatch(4);
        CountDownLatch allComplete = new CountDownLatch(4);

        for (int i = 0; i < 4; i++) {
            scheduler.scheduleOnce(Duration.ofMillis(10), () -> {
                allStarted.countDown();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                allComplete.countDown();
            });
        }

        assertTrue(allStarted.await(1, TimeUnit.SECONDS),
                "All 4 tasks should start within 1 second");
        assertTrue(allComplete.await(2, TimeUnit.SECONDS),
                "All 4 tasks should complete within 2 seconds");
    }

    @Test
    void shutdownDuringExecution() throws InterruptedException {
        CountDownLatch taskStarted = new CountDownLatch(1);
        CountDownLatch taskComplete = new CountDownLatch(1);
        AtomicInteger completionCount = new AtomicInteger(0);

        scheduler.scheduleOnce(Duration.ofMillis(10), () -> {
            taskStarted.countDown();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            completionCount.incrementAndGet();
            taskComplete.countDown();
        });

        assertTrue(taskStarted.await(1, TimeUnit.SECONDS));

        scheduler.shutdown();
        assertTrue(scheduler.isShutdown());

        assertTrue(scheduler.awaitTermination(Duration.ofSeconds(2)));
        assertEquals(1, completionCount.get());
    }

    @Test
    void rapidSchedulingAndCancellation() throws InterruptedException {
        int taskCount = 100;
        ScheduledHandle[] handles = new ScheduledHandle[taskCount];
        AtomicInteger completedCount = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            handles[i] = scheduler.scheduleOnce(Duration.ofMillis(100 + i * 10), () -> {
                completedCount.incrementAndGet();
            });
        }

        for (int i = 0; i < taskCount; i += 2) {
            handles[i].cancel();
        }

        Thread.sleep(2000);

        int completed = completedCount.get();
        assertTrue(completed > 0 && completed <= taskCount / 2 + 5,
                "About half the tasks should complete, got: " + completed);
    }

    @Test
    void fixedDelayMaintainsSequence() throws InterruptedException {
        AtomicInteger runCount = new AtomicInteger(0);
        long[] startTimes = new long[5];
        long[] endTimes = new long[5];
        CountDownLatch latch = new CountDownLatch(5);

        scheduler.scheduleWithFixedDelay(Duration.ofMillis(10), Duration.ofMillis(50), () -> {
            int run = runCount.getAndIncrement();
            if (run < 5) {
                startTimes[run] = System.nanoTime();
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                endTimes[run] = System.nanoTime();
                latch.countDown();
            }
        });

        assertTrue(latch.await(3, TimeUnit.SECONDS));

        for (int i = 1; i < 5; i++) {
            long delay = startTimes[i] - endTimes[i - 1];
            assertTrue(delay >= Duration.ofMillis(40).toNanos(),
                    "Delay between runs should be at least 40ms, got: " +
                            Duration.ofNanos(delay).toMillis() + "ms");
        }
    }
}
