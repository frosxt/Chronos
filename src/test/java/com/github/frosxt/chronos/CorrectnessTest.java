package com.github.frosxt.chronos;

import com.github.frosxt.chronos.api.ScheduledHandle;
import com.github.frosxt.chronos.api.Scheduler;
import com.github.frosxt.chronos.api.TaskState;
import com.github.frosxt.chronos.api.cron.CronExpression;
import com.github.frosxt.chronos.api.factory.Chronos;
import com.github.frosxt.chronos.api.spec.SchedulerSpec;
import com.github.frosxt.chronos.runtime.cron.CronNextFireCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests checking for specific correctness issues and regressions.
 */
class CorrectnessTest {

    private Scheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = Chronos.create(SchedulerSpec.builder()
                .threadCount(2)
                .build());
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    @Test
    void verifyCronDomDowUnionSemantics() {
        // "0 0 1 * 1" should fire on 1st of month OR Mondays
        CronExpression expr = CronExpression.parse("0 0 1 * 1");
        ZoneId zone = ZoneId.of("UTC");

        // Monday 15th (matches DOW)
        ZonedDateTime monday = ZonedDateTime.of(2024, 1, 15, 0, 0, 0, 0, zone);
        ZonedDateTime nextFromMon = CronNextFireCalculator.nextFire(expr, zone, monday.minusSeconds(1));
        assertNotNull(nextFromMon);
        assertEquals(monday, nextFromMon, "Should match Monday 15th (DOW match)");

        // Wednesday 1st (matches DOM)
        // May 1st 2024 is Wednesday
        ZonedDateTime wed1st = ZonedDateTime.of(2024, 5, 1, 0, 0, 0, 0, zone);
        ZonedDateTime nextFromWed = CronNextFireCalculator.nextFire(expr, zone, wed1st.minusSeconds(1));
        assertNotNull(nextFromWed);
        assertEquals(wed1st, nextFromWed, "Should match Wednesday 1st (DOM match)");

        // Tuesday 2nd (matches neither)
        ZonedDateTime tue2nd = ZonedDateTime.of(2024, 1, 2, 0, 0, 0, 0, zone);
        ZonedDateTime nextFromTue = CronNextFireCalculator.nextFire(expr, zone, tue2nd.minusSeconds(1));
        // Next should be either a Monday (8th) or 1st of next month
        assertNotEquals(tue2nd, nextFromTue, "Should not match Tuesday 2nd");
    }

    @Test
    void cancellationDuringExecutionStopsFutureRuns() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch continueLatch = new CountDownLatch(1);

        ScheduledHandle handle = scheduler.scheduleAtFixedRate(Duration.ofMillis(10), Duration.ofMillis(10), () -> {
            startLatch.countDown();
            try {
                if (!continueLatch.await(2, TimeUnit.SECONDS)) {
                    // Timeout
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // If we run again after cancellation, this checks it
            if (Thread.currentThread().isInterrupted()) {
                // Good
            }
        });

        assertTrue(startLatch.await(1, TimeUnit.SECONDS));

        // Cancel while running
        handle.cancel();

        // Let it finish
        continueLatch.countDown();

        Thread.sleep(100);

        assertEquals(TaskState.CANCELLED, handle.state());
        // Verify it doesn't run again (runCount should be 1)
        assertEquals(1, handle.runCount());
    }
}
