package com.github.frosxt.chronos.runtime.cron;

import com.github.frosxt.chronos.api.cron.CronExpression;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CronNextFireCalculator}.
 */
class CronNextFireCalculatorTest {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    void nextFireMinuteAdvance() {
        CronExpression expr = CronExpression.parse("30 * * * *");
        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 15, 10, 15, 0, 0, UTC);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        assertEquals(30, next.getMinute());
        assertEquals(10, next.getHour());
    }

    @Test
    void nextFireHourAdvance() {
        CronExpression expr = CronExpression.parse("0 12 * * *");
        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 15, 13, 0, 0, 0, UTC);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        assertEquals(0, next.getMinute());
        assertEquals(12, next.getHour());
        assertEquals(16, next.getDayOfMonth());
    }

    @Test
    void nextFireDayAdvance() {
        CronExpression expr = CronExpression.parse("0 0 15 * *");
        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, UTC);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        assertEquals(15, next.getDayOfMonth());
        assertEquals(2, next.getMonthValue());
    }

    @Test
    void nextFireMonthAdvance() {
        CronExpression expr = CronExpression.parse("0 0 1 3 *");
        ZonedDateTime ref = ZonedDateTime.of(2024, 5, 1, 0, 0, 0, 0, UTC);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        assertEquals(1, next.getDayOfMonth());
        assertEquals(3, next.getMonthValue());
        assertEquals(2025, next.getYear());
    }

    @Test
    void nextFireDayOfWeek() {
        CronExpression expr = CronExpression.parse("0 9 * * 1");
        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 15, 0, 0, 0, 0, UTC);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        assertEquals(1, next.getDayOfWeek().getValue());
        assertEquals(9, next.getHour());
    }

    @Test
    void nextFireEveryFiveMinutes() {
        CronExpression expr = CronExpression.parse("*/5 * * * *");
        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 15, 10, 7, 0, 0, UTC);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        assertEquals(10, next.getMinute());
        assertEquals(10, next.getHour());
    }

    @Test
    void nextFireWorkdaysOnly() {
        CronExpression expr = CronExpression.parse("0 9 * * 1-5");
        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 13, 10, 0, 0, 0, UTC);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        int dow = next.getDayOfWeek().getValue();
        assertTrue(dow >= 1 && dow <= 5, "Should be weekday");
    }

    @Test
    void nextFireReturnsNullForImpossible() {
        CronExpression expr = CronExpression.parse("0 0 31 2 *");

        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 1, 0, 0, 0, 0, UTC);
        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, UTC, ref);

        assertNull(next);
    }

    @Test
    void nextFireDifferentTimezone() {
        CronExpression expr = CronExpression.parse("0 9 * * *");
        ZoneId tokyo = ZoneId.of("Asia/Tokyo");
        ZonedDateTime ref = ZonedDateTime.of(2024, 1, 15, 10, 0, 0, 0, tokyo);

        ZonedDateTime next = CronNextFireCalculator.nextFire(expr, tokyo, ref);

        assertEquals(9, next.getHour());
        assertEquals(16, next.getDayOfMonth());
        assertEquals(tokyo, next.getZone());
    }
}
