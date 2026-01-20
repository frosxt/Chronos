package com.github.frosxt.chronos.runtime.trigger;

import com.github.frosxt.chronos.runtime.trigger.impl.FixedDelayTrigger;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link FixedDelayTrigger}.
 */
class FixedDelayTriggerTest {

    @Test
    void initialDelayCalculation() {
        long now = 1_000_000_000L;
        long initialDelay = Duration.ofSeconds(5).toNanos();
        long delay = Duration.ofSeconds(10).toNanos();

        FixedDelayTrigger trigger = new FixedDelayTrigger(now, initialDelay, delay);

        long result = trigger.nextDelayNanos(now, -1, -1, 0);
        assertEquals(initialDelay, result);
    }

    @Test
    void subsequentDelaysFromEndTime() {
        long now = 1_000_000_000L;
        long initialDelay = Duration.ofSeconds(5).toNanos();
        long delayNanos = Duration.ofSeconds(10).toNanos();

        FixedDelayTrigger trigger = new FixedDelayTrigger(now, initialDelay, delayNanos);

        long startTime = now + initialDelay;
        long endTime = startTime + Duration.ofSeconds(3).toNanos();
        long currentTime = endTime;

        long result = trigger.nextDelayNanos(currentTime, startTime, endTime, 1);
        assertEquals(delayNanos, result);
    }

    @Test
    void delayCalculatesFromCurrentTime() {
        long now = 1_000_000_000L;
        long initialDelay = 0;
        long delayNanos = Duration.ofSeconds(10).toNanos();

        FixedDelayTrigger trigger = new FixedDelayTrigger(now, initialDelay, delayNanos);

        long endTime = now + Duration.ofSeconds(5).toNanos();
        long currentTime = endTime + Duration.ofSeconds(3).toNanos();

        long result = trigger.nextDelayNanos(currentTime, now, endTime, 1);
        long expected = (endTime + delayNanos) - currentTime;
        assertEquals(expected, result);
    }

    @Test
    void neverReturnsNegativeDelay() {
        long now = 1_000_000_000L;
        long initialDelay = 0;
        long delayNanos = Duration.ofSeconds(10).toNanos();

        FixedDelayTrigger trigger = new FixedDelayTrigger(now, initialDelay, delayNanos);

        long endTime = now;
        long currentTime = endTime + Duration.ofSeconds(15).toNanos();

        long result = trigger.nextDelayNanos(currentTime, now, endTime, 1);
        assertEquals(0, result);
    }

    @Test
    void longRunningTaskDoesNotAffectDelay() {
        long now = 1_000_000_000L;
        long initialDelay = 0;
        long delayNanos = Duration.ofSeconds(5).toNanos();

        FixedDelayTrigger trigger = new FixedDelayTrigger(now, initialDelay, delayNanos);

        long startTime = now;
        long endTime = startTime + Duration.ofMinutes(10).toNanos();

        long result = trigger.nextDelayNanos(endTime, startTime, endTime, 1);
        assertEquals(delayNanos, result);
    }

    @Test
    void isRecurring() {
        FixedDelayTrigger trigger = new FixedDelayTrigger(0, 0,
                Duration.ofSeconds(1).toNanos());
        assertTrue(trigger.isRecurring());
    }
}
