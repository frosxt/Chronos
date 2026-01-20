package com.github.frosxt.chronos.runtime.core;

import com.github.frosxt.chronos.api.ScheduledHandle;
import com.github.frosxt.chronos.api.Scheduler;
import com.github.frosxt.chronos.api.SchedulerSnapshot;
import com.github.frosxt.chronos.api.TaskState;
import com.github.frosxt.chronos.api.factory.Chronos;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for scheduler lifecycle management.
 */
class SchedulerLifecycleTest {

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = Chronos.create(SchedulerSpec.builder()
                .threadCount(2)
                .threadNamePrefix("test-")
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
    void scheduleOnceExecutes() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger counter = new AtomicInteger(0);

        ScheduledHandle handle = scheduler.scheduleOnce(Duration.ofMillis(50), () -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        assertNotNull(handle.id());
        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertEquals(1, counter.get());

        Thread.sleep(100);
        assertEquals(TaskState.COMPLETED, handle.state());
        assertTrue(handle.isDone());
        assertEquals(1, handle.runCount());
    }

    @Test
    void scheduleOnceCanBeCancelled() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);

        ScheduledHandle handle = scheduler.scheduleOnce(Duration.ofSeconds(10), () -> {
            counter.incrementAndGet();
        });

        assertTrue(handle.cancel());
        assertTrue(handle.isCancelled());
        assertEquals(TaskState.CANCELLED, handle.state());
        assertTrue(handle.isDone());

        Thread.sleep(100);
        assertEquals(0, counter.get());
    }

    @Test
    void scheduleAfterShutdownThrows() {
        scheduler.shutdown();
        assertTrue(scheduler.isShutdown());

        assertThrows(IllegalStateException.class, () -> scheduler.scheduleOnce(Duration.ofSeconds(1), () -> {
        }));
    }

    @Test
    void shutdownNowCancelsPendingTasks() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        ScheduledHandle handle = scheduler.scheduleOnce(Duration.ofSeconds(10), () -> {
            latch.countDown();
        });

        scheduler.shutdownNow();

        assertFalse(latch.await(200, TimeUnit.MILLISECONDS));
        assertTrue(handle.isCancelled() || handle.isDone());
    }

    @Test
    void awaitTerminationWaits() throws InterruptedException {
        scheduler.scheduleOnce(Duration.ofMillis(100), () -> {
        });

        scheduler.shutdown();
        assertTrue(scheduler.awaitTermination(Duration.ofSeconds(5)));
        assertTrue(scheduler.isTerminated());
    }

    @Test
    void closePerformsOrderlyShutdown() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        scheduler.scheduleOnce(Duration.ofMillis(50), latch::countDown);

        scheduler.close();

        assertTrue(scheduler.isShutdown());
    }

    @Test
    void snapshotReturnsMetrics() {
        scheduler.scheduleOnce(Duration.ofMillis(50), () -> {
        });
        scheduler.scheduleOnce(Duration.ofSeconds(10), () -> {
        });

        SchedulerSnapshot snapshot = scheduler.snapshot();

        assertNotNull(snapshot);
        assertNotNull(snapshot.snapshotTime());
        assertTrue(snapshot.totalTaskCount() >= 2);
    }

    @Test
    void awaitTerminationValidatesTimeout() {
        assertThrows(IllegalArgumentException.class, () -> scheduler.awaitTermination(Duration.ZERO));

        assertThrows(IllegalArgumentException.class, () -> scheduler.awaitTermination(Duration.ofSeconds(-1)));
    }

    @Test
    void scheduleOnceValidatesDelay() {
        assertThrows(NullPointerException.class, () -> scheduler.scheduleOnce(null, () -> {
        }));

        assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleOnce(Duration.ZERO, () -> {
        }));

        assertThrows(IllegalArgumentException.class, () -> scheduler.scheduleOnce(Duration.ofSeconds(-1), () -> {
        }));

        assertThrows(NullPointerException.class, () -> scheduler.scheduleOnce(Duration.ofSeconds(1), null));
    }

    @Test
    void scheduleAtFixedRateValidatesArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> scheduler.scheduleAtFixedRate(Duration.ofSeconds(-1), Duration.ofSeconds(1), () -> {
                }));

        assertThrows(IllegalArgumentException.class,
                () -> scheduler.scheduleAtFixedRate(Duration.ZERO, Duration.ZERO, () -> {
                }));
    }

    @Test
    void scheduleWithFixedDelayValidatesArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> scheduler.scheduleWithFixedDelay(Duration.ofSeconds(-1), Duration.ofSeconds(1), () -> {
                }));

        assertThrows(IllegalArgumentException.class,
                () -> scheduler.scheduleWithFixedDelay(Duration.ZERO, Duration.ZERO, () -> {
                }));
    }
}
