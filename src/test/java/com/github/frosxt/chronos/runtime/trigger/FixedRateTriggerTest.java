package com.github.frosxt.chronos.runtime.trigger;

import com.github.frosxt.chronos.runtime.trigger.impl.FixedRateTrigger;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FixedRateTrigger}.
 */
class FixedRateTriggerTest {

    @Test
    void initialDelayCalculation() {
        long now = 1_000_000_000L;
        long initialDelay = Duration.ofSeconds(5).toNanos();
        long period = Duration.ofSeconds(10).toNanos();

        FixedRateTrigger trigger = new FixedRateTrigger(now, initialDelay, period);

        long delay = trigger.nextDelayNanos(now, -1, -1, 0);
        assertEquals(initialDelay, delay);
    }

    @Test
    void subsequentDelaysFromInitialTime() {
        long now = 1_000_000_000L;
        long initialDelay = Duration.ofSeconds(5).toNanos();
        long period = Duration.ofSeconds(10).toNanos();

        FixedRateTrigger trigger = new FixedRateTrigger(now, initialDelay, period);

        long initialSchedule = now + initialDelay;
        long runEndTime = initialSchedule + Duration.ofSeconds(2).toNanos();

        long delay = trigger.nextDelayNanos(runEndTime, initialSchedule, runEndTime, 1);
        long expectedNextRun = initialSchedule + period;
        assertEquals(expectedNextRun - runEndTime, delay);
    }

    @Test
    void catchUpWhenFallingBehind() {
        long now = 1_000_000_000L;
        long initialDelay = Duration.ofSeconds(1).toNanos();
        long period = Duration.ofSeconds(5).toNanos();

        FixedRateTrigger trigger = new FixedRateTrigger(now, initialDelay, period);

        long initialSchedule = now + initialDelay;
        long longRunEnd = initialSchedule + Duration.ofSeconds(10).toNanos();

        long delay = trigger.nextDelayNanos(longRunEnd, initialSchedule, longRunEnd, 1);
        assertEquals(0, delay);
    }

    @Test
    void multipleRunsStayOnSchedule() {
        long now = 1_000_000_000L;
        long initialDelay = 0;
        long period = Duration.ofSeconds(10).toNanos();

        FixedRateTrigger trigger = new FixedRateTrigger(now, initialDelay, period);

        for (int run = 1; run <= 5; run++) {
            long expectedNext = now + (run * period);
            long currentTime = expectedNext - Duration.ofSeconds(1).toNanos();

            long delay = trigger.nextDelayNanos(currentTime, -1, -1, run);

            assertEquals(Duration.ofSeconds(1).toNanos(), delay,
                    "Run " + run + " should be 1 second away");
        }
    }

    @Test
    void isRecurring() {
        FixedRateTrigger trigger = new FixedRateTrigger(0, 0,
                Duration.ofSeconds(1).toNanos());
        assertTrue(trigger.isRecurring());
    }
}
