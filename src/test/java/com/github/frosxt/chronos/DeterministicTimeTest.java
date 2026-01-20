package com.github.frosxt.chronos;

import com.github.frosxt.chronos.api.ScheduledHandle;
import com.github.frosxt.chronos.api.Scheduler;
import com.github.frosxt.chronos.api.TaskState;
import com.github.frosxt.chronos.api.factory.Chronos;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import com.github.frosxt.chronos.api.time.TimeSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests using controllable time sources for deterministic behavior.
 */
class DeterministicTimeTest {

    private Scheduler scheduler;

    @AfterEach
    void tearDown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    @Test
    void customTimeSourceIsUsed() throws InterruptedException {
        AtomicLong currentNanos = new AtomicLong(1_000_000_000L);
        TimeSource timeSource = currentNanos::get;

        Instant fixedInstant = Instant.parse("2024-01-15T10:00:00Z");
        Clock clock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        scheduler = Chronos.create(SchedulerSpec.builder()
                .timeSource(timeSource)
                .clock(clock)
                .threadCount(1)
                .build());

        CountDownLatch latch = new CountDownLatch(1);
        ScheduledHandle handle = scheduler.scheduleOnce(Duration.ofMillis(100), latch::countDown);

        assertNotNull(handle.firstScheduledTime());

        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test
    void timestampsUseProvidedClock() throws InterruptedException {
        Instant now = Instant.parse("2024-06-15T12:00:00Z");
        Clock clock = Clock.fixed(now, ZoneId.of("UTC"));

        scheduler = Chronos.create(SchedulerSpec.builder()
                .clock(clock)
                .threadCount(1)
                .build());

        CountDownLatch latch = new CountDownLatch(1);
        ScheduledHandle handle = scheduler.scheduleOnce(Duration.ofMillis(50), latch::countDown);

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        Thread.sleep(100);

        assertEquals(TaskState.COMPLETED, handle.state());
    }

    @Test
    void multipleTasksWithSameTimeSource() throws InterruptedException {
        AtomicLong nanos = new AtomicLong(0);
        TimeSource timeSource = nanos::get;

        scheduler = Chronos.create(SchedulerSpec.builder()
                .timeSource(timeSource)
                .threadCount(2)
                .build());

        CountDownLatch latch = new CountDownLatch(3);
        AtomicInteger counter = new AtomicInteger(0);

        scheduler.scheduleOnce(Duration.ofMillis(10), () -> {
            counter.incrementAndGet();
            latch.countDown();
        });
        scheduler.scheduleOnce(Duration.ofMillis(20), () -> {
            counter.incrementAndGet();
            latch.countDown();
        });
        scheduler.scheduleOnce(Duration.ofMillis(30), () -> {
            counter.incrementAndGet();
            latch.countDown();
        });

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(3, counter.get());
    }

    @Test
    void runCountTracksExecutions() throws InterruptedException {
        scheduler = Chronos.create(SchedulerSpec.builder()
                .threadCount(1)
                .build());

        CountDownLatch latch = new CountDownLatch(3);
        ScheduledHandle handle = scheduler.scheduleAtFixedRate(
                Duration.ofMillis(10),
                Duration.ofMillis(30),
                latch::countDown);

        assertTrue(latch.await(2, TimeUnit.SECONDS));

        assertTrue(handle.runCount() >= 3);
    }

    @Test
    void taskStateTransitions() throws InterruptedException {
        scheduler = Chronos.create(SchedulerSpec.builder()
                .threadCount(1)
                .build());

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch continueLatch = new CountDownLatch(1);

        ScheduledHandle handle = scheduler.scheduleOnce(Duration.ofMillis(10), () -> {
            startLatch.countDown();
            try {
                continueLatch.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertEquals(TaskState.SCHEDULED, handle.state());

        assertTrue(startLatch.await(1, TimeUnit.SECONDS));
        assertEquals(TaskState.RUNNING, handle.state());
        assertTrue(handle.isRunning());

        continueLatch.countDown();
        Thread.sleep(100);

        assertEquals(TaskState.COMPLETED, handle.state());
        assertTrue(handle.isDone());
        assertFalse(handle.isRunning());
    }
}
